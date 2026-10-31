package com.example.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PreparedPhoto(
    val fileName: String,
    val mimeType: String = "image/webp",
    val bytes: ByteArray,
) {
    val dataUri: String
        get() = "data:$mimeType;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PreparedPhoto) return false
        return fileName == other.fileName &&
            mimeType == other.mimeType &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

object ImageUploadHelper {
    private const val TAG = "ImageUploadHelper"
    private const val DEFAULT_MAX_DIMENSION_PX = 1280
    private const val MAX_TARGET_BYTES = 4_200_000 // Safely below 5 MB PocketBase limit

    /**
     * Reads an image selected from Android Photo Picker [uri], downscales proportionally
     * to [maxDimensionPx], applies MediaStore orientation if present, and compresses to WebP
     * (Lossy) so uploads over mobile carriers are ultra-lightweight and lightning fast on PocketBase VPS storage.
     */
    suspend fun preparePhotoFromUri(
        context: Context,
        uri: Uri,
        prefix: String = "sby",
        maxDimensionPx: Int = DEFAULT_MAX_DIMENSION_PX,
        initialQuality: Int = 80,
        initialJpegQuality: Int = initialQuality,
    ): PreparedPhoto? = withContext(Dispatchers.IO) {
        try {
            val quality = if (initialQuality != 80) initialQuality else initialJpegQuality
            val resolver = context.contentResolver
            val rotationDegrees = queryRotationDegrees(context, uri)

            // 1. Decode bounds only to compute memory-safe inSampleSize
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }

            val rawWidth = boundsOptions.outWidth
            val rawHeight = boundsOptions.outHeight
            if (rawWidth <= 0 || rawHeight <= 0) {
                Log.w(TAG, "Unable to decode image bounds from $uri")
                return@withContext null
            }

            val sampleSize = calculateInSampleSize(rawWidth, rawHeight, maxDimensionPx)
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val decodedBitmap = resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return@withContext null

            val transformedBitmap = transformBitmap(
                source = decodedBitmap,
                rotationDegrees = rotationDegrees,
                maxDimensionPx = maxDimensionPx,
            )
            if (transformedBitmap !== decodedBitmap) {
                decodedBitmap.recycle()
            }

            var (compressedBytes, mimeType) = compressBitmap(transformedBitmap, quality)
            if (compressedBytes.size > MAX_TARGET_BYTES) {
                val (fallbackBytes, fallbackMime) = compressBitmap(transformedBitmap, 60)
                compressedBytes = fallbackBytes
                mimeType = fallbackMime
            }
            transformedBitmap.recycle()

            if (compressedBytes.isEmpty()) return@withContext null

            val extension = if (mimeType == "image/webp") "webp" else "jpg"
            val cleanPrefix = prefix.lowercase().replace(Regex("[^a-z0-9]"), "").ifBlank { "sby" }
            PreparedPhoto(
                fileName = "${cleanPrefix}_${System.currentTimeMillis()}.$extension",
                mimeType = mimeType,
                bytes = compressedBytes,
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed preparing photo upload from $uri: ${e.message}", e)
            null
        }
    }

    private fun queryRotationDegrees(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.ImageColumns.ORIENTATION),
                null,
                null,
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getInt(0) else 0
            } ?: 0
        } catch (_: Exception) {
            0
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var inSampleSize = 1
        val largestSide = max(width, height)
        if (largestSide > maxDim) {
            val halfSide = largestSide / 2
            while ((halfSide / inSampleSize) >= maxDim) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun transformBitmap(
        source: Bitmap,
        rotationDegrees: Int,
        maxDimensionPx: Int,
    ): Bitmap {
        val srcW = source.width
        val srcH = source.height
        if (srcW <= 0 || srcH <= 0) return source

        val effectiveLargest = max(srcW, srcH)
        val scale = if (effectiveLargest > maxDimensionPx) {
            maxDimensionPx.toFloat() / effectiveLargest.toFloat()
        } else {
            1f
        }
        val normalizedRotation = ((rotationDegrees % 360) + 360) % 360

        if (scale >= 0.999f && normalizedRotation == 0) {
            return source
        }

        return if (normalizedRotation == 0) {
            val dstW = (srcW * scale).roundToInt().coerceAtLeast(1)
            val dstH = (srcH * scale).roundToInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(source, dstW, dstH, true)
        } else {
            val matrix = Matrix().apply {
                if (scale < 0.999f) {
                    postScale(scale, scale)
                }
                postRotate(normalizedRotation.toFloat())
            }
            Bitmap.createBitmap(source, 0, 0, srcW, srcH, matrix, true)
        }
    }

    private fun getWebpLossyFormat(): Bitmap.CompressFormat {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }
    }

    private fun compressBitmap(bitmap: Bitmap, quality: Int): Pair<ByteArray, String> {
        val output = ByteArrayOutputStream()
        val format = getWebpLossyFormat()
        val success = bitmap.compress(format, quality.coerceIn(30, 95), output)
        val bytes = output.toByteArray()
        if (success && bytes.isNotEmpty()) {
            return Pair(bytes, "image/webp")
        }
        // Fallback to JPEG if WebP encoding failed on legacy OEM device
        output.reset()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(40, 95), output)
        return Pair(output.toByteArray(), "image/jpeg")
    }
}
