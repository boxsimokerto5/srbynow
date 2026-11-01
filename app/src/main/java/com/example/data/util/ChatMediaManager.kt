package com.example.data.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.ChatMessage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

/**
 * WhatsApp-style Media Manager for Chat Photos in Arek Surabaya.
 *
 * Implements the "Store-and-Forward" architecture:
 * 1. Sender compresses photo and saves a copy to local cache & Android Gallery (Pictures/ArekSurabaya).
 * 2. Photo is sent through the server as a temporary bridge.
 * 3. Recipient automatically downloads the incoming photo to local cache and saves it to Android Gallery.
 * 4. Once stored locally on both devices, the server storage copy can be safely pruned, ensuring
 *    database/storage never gets full while users retain full permanent offline access in their Galeri!
 */
object ChatMediaManager {
    private const val TAG = "ChatMediaManager"
    private const val PREFS_NAME = "arek_chat_media_prefs"
    private const val KEY_PREFIX_GALLERY_URI = "gallery_uri_"
    private const val GALLERY_SUBDIRECTORY = "Pictures/ArekSurabaya"
    private const val ALBUM_NAME = "ArekSurabaya"

    private val _cachedMessageIds = MutableStateFlow<Set<String>>(emptySet())
    val cachedMessageIds: StateFlow<Set<String>> = _cachedMessageIds.asStateFlow()

    private val _downloadingMessageIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadingMessageIds: StateFlow<Set<String>> = _downloadingMessageIds.asStateFlow()

    private var isInitialized = false

    /**
     * Initializes the manager and loads existing cached media records from disk.
     */
    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        val mediaDir = getChatMediaDir(context)
        if (!mediaDir.exists()) {
            mediaDir.mkdirs()
        }
        val existingFiles = mediaDir.listFiles() ?: emptyArray()
        val ids = existingFiles.mapNotNull { file ->
            val name = file.name
            if (name.startsWith("IMG_SBY_")) {
                name.removePrefix("IMG_SBY_").substringBefore(".")
            } else null
        }.toSet()
        _cachedMessageIds.value = ids
        Log.d(TAG, "ChatMediaManager initialized with ${ids.size} cached photos.")
    }

    /**
     * Directory inside app internal storage dedicated to chat photos.
     */
    fun getChatMediaDir(context: Context): File {
        val dir = File(context.filesDir, "chat_media")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Check if a photo for [messageId] is already saved locally on this device.
     */
    fun isPhotoSavedLocally(context: Context, messageId: String): Boolean {
        if (messageId.isBlank()) return false
        val file = getLocalMediaFile(context, messageId)
        return file != null && file.exists() && file.length() > 0
    }

    /**
     * Retrieves the local File for [messageId] if it exists.
     */
    fun getLocalMediaFile(context: Context, messageId: String): File? {
        if (messageId.isBlank()) return null
        val mediaDir = getChatMediaDir(context)
        val webpFile = File(mediaDir, "IMG_SBY_$messageId.webp")
        if (webpFile.exists() && webpFile.length() > 0) return webpFile

        val jpgFile = File(mediaDir, "IMG_SBY_$messageId.jpg")
        if (jpgFile.exists() && jpgFile.length() > 0) return jpgFile

        return null
    }

    /**
     * Returns either the local File (if already saved) or the remote photoUrl.
     * This allows Coil to load directly from local disk whenever available without network requests.
     */
    fun getLocalOrRemoteModel(context: Context, message: ChatMessage): Any {
        val local = getLocalMediaFile(context, message.id)
        return if (local != null && local.exists()) local else message.photoUrl
    }

    /**
     * Saves an outgoing photo (sent by the user) to local cache and to the Android device Gallery.
     */
    suspend fun saveOutgoingPhoto(
        context: Context,
        messageId: String,
        photoBytes: ByteArray,
        mimeType: String = "image/webp",
    ): File? = withContext(Dispatchers.IO) {
        init(context)
        try {
            val extension = if (mimeType.contains("webp", ignoreCase = true)) "webp" else "jpg"
            val localFile = File(getChatMediaDir(context), "IMG_SBY_$messageId.$extension")

            // 1. Save to app private chat_media folder
            FileOutputStream(localFile).use { fos ->
                fos.write(photoBytes)
                fos.flush()
            }

            // 2. Insert into Android MediaStore (Galeri)
            val galleryUri = insertIntoMediaStore(
                context = context,
                photoBytes = photoBytes,
                fileName = "SBY_CHAT_${System.currentTimeMillis()}.$extension",
                mimeType = mimeType,
            )

            if (galleryUri != null) {
                saveGalleryUriPref(context, messageId, galleryUri)
            }

            _cachedMessageIds.update { it + messageId }
            Log.i(TAG, "Outgoing photo $messageId saved locally: ${localFile.absolutePath}, galleryUri: $galleryUri")
            localFile
        } catch (e: Exception) {
            Log.e(TAG, "Error saving outgoing chat photo $messageId: ${e.message}", e)
            null
        }
    }

    /**
     * Automatically downloads incoming photo from [photoUrl], saves to internal cache,
     * and inserts into Android Gallery (MediaStore).
     */
    suspend fun downloadAndSaveIncomingPhoto(
        context: Context,
        messageId: String,
        photoUrl: String,
    ): File? = withContext(Dispatchers.IO) {
        init(context)
        if (messageId.isBlank() || photoUrl.isBlank()) return@withContext null

        val existing = getLocalMediaFile(context, messageId)
        if (existing != null && existing.exists() && existing.length() > 0) {
            _cachedMessageIds.update { it + messageId }
            return@withContext existing
        }

        _downloadingMessageIds.update { it + messageId }
        try {
            val url = URL(photoUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 20_000
                instanceFollowRedirects = true
            }

            if (connection.responseCode !in 200..299) {
                Log.w(TAG, "Download failed for $photoUrl with HTTP ${connection.responseCode}")
                return@withContext null
            }

            val contentType = connection.contentType ?: "image/webp"
            val extension = if (contentType.contains("jpeg", ignoreCase = true) || contentType.contains("jpg", ignoreCase = true)) "jpg" else "webp"
            val localFile = File(getChatMediaDir(context), "IMG_SBY_$messageId.$extension")

            val bytes = connection.inputStream.use { input ->
                input.readBytes()
            }

            if (bytes.isEmpty()) {
                Log.w(TAG, "Empty payload downloaded for $messageId")
                return@withContext null
            }

            // 1. Write to internal storage
            FileOutputStream(localFile).use { fos ->
                fos.write(bytes)
                fos.flush()
            }

            // 2. Insert into Android device Gallery
            val galleryUri = insertIntoMediaStore(
                context = context,
                photoBytes = bytes,
                fileName = "SBY_CHAT_${System.currentTimeMillis()}.$extension",
                mimeType = contentType,
            )

            if (galleryUri != null) {
                saveGalleryUriPref(context, messageId, galleryUri)
            }

            _cachedMessageIds.update { it + messageId }
            Log.i(TAG, "Incoming chat photo $messageId downloaded and saved to Galeri: $galleryUri")
            localFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed downloading chat photo $messageId from $photoUrl: ${e.message}", e)
            null
        } finally {
            _downloadingMessageIds.update { it - messageId }
        }
    }

    /**
     * Inserts photo bytes into Android MediaStore under Pictures/ArekSurabaya.
     * Complies with Scoped Storage (Android 10+ / Q+ and legacy).
     */
    private fun insertIntoMediaStore(
        context: Context,
        photoBytes: ByteArray,
        fileName: String,
        mimeType: String,
    ): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, GALLERY_SUBDIRECTORY)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val itemUri = resolver.insert(collectionUri, contentValues) ?: return null

        try {
            resolver.openOutputStream(itemUri)?.use { outputStream ->
                outputStream.write(photoBytes)
                outputStream.flush()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(itemUri, contentValues, null, null)
            }
            return itemUri
        } catch (e: Exception) {
            Log.e(TAG, "Error writing image to MediaStore: ${e.message}", e)
            resolver.delete(itemUri, null, null)
            return null
        }
    }

    /**
     * Saves the Gallery Uri mapping for quick access later.
     */
    private fun saveGalleryUriPref(context: Context, messageId: String, uri: Uri) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PREFIX_GALLERY_URI + messageId, uri.toString()).apply()
    }

    /**
     * Retrieves the saved Gallery Uri if it was registered.
     */
    fun getGalleryUri(context: Context, messageId: String): Uri? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uriStr = prefs.getString(KEY_PREFIX_GALLERY_URI + messageId, null) ?: return null
        return Uri.parse(uriStr)
    }

    /**
     * Opens the photo in the system Gallery or preferred Image Viewer app.
     */
    fun openInGallery(context: Context, messageId: String): Boolean {
        try {
            val galleryUri = getGalleryUri(context, messageId)
            val uriToOpen = if (galleryUri != null) {
                galleryUri
            } else {
                val localFile = getLocalMediaFile(context, messageId) ?: return false
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    localFile,
                )
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uriToOpen, "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            Log.w(TAG, "Could not open photo in gallery: ${e.message}")
            return false
        }
    }

    /**
     * Shares the photo to external apps via Android ShareSheet.
     */
    fun sharePhoto(context: Context, messageId: String, caption: String = ""): Boolean {
        try {
            val localFile = getLocalMediaFile(context, messageId) ?: return false
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                localFile,
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                if (caption.isNotBlank()) {
                    putExtra(Intent.EXTRA_TEXT, caption)
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Bagikan Foto Chat Arek Surabaya").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            return true
        } catch (e: Exception) {
            Log.w(TAG, "Could not share photo: ${e.message}")
            return false
        }
    }

    /**
     * Saves an event poster directly to local cache and Android device Gallery (Pictures/ArekSurabaya).
     * If the event has a remote photoUrl, downloads and stores it.
     * If not, extracts bytes from the local drawable resource.
     * No user confirmation is requested—this runs silently in the background so all citizens
     * have the event poster readily available in their phone's gallery!
     */
    suspend fun saveEventPosterToGallery(
        context: Context,
        eventId: String,
        photoUrl: String,
        drawableResId: Int? = null,
    ): File? = withContext(Dispatchers.IO) {
        init(context)
        val msgId = "evdm_${eventId}"
        val existing = getLocalMediaFile(context, msgId)
        if (existing != null && existing.exists() && existing.length() > 0) {
            _cachedMessageIds.update { it + msgId }
            return@withContext existing
        }

        // 1. Try downloading remote photoUrl if present
        if (photoUrl.isNotBlank()) {
            val downloaded = downloadAndSaveIncomingPhoto(context, msgId, photoUrl)
            if (downloaded != null) return@withContext downloaded
        }

        // 2. Fallback to local curated drawable resource
        if (drawableResId != null && drawableResId != 0) {
            try {
                val bitmap = BitmapFactory.decodeResource(context.resources, drawableResId)
                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    val bytes = stream.toByteArray()
                    return@withContext saveOutgoingPhoto(context, msgId, bytes, "image/jpeg")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to save drawable poster to gallery: ${e.message}")
            }
        }
        null
    }
}
