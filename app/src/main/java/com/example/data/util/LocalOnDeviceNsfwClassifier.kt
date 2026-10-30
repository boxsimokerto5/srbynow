package com.example.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Environment
import com.google.ai.edge.litert.LiteRtException
import com.google.ai.edge.litert.TensorBuffer
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.math.max

private const val LOCAL_NSFW_TAG = "LocalOnDeviceNsfw"
private const val MODEL_ASSET_NAME = "open_nsfw.tflite"
private const val RESIZE_SIDE = 256
private const val INPUT_WIDTH = 224
private const val INPUT_HEIGHT = 224
private const val INPUT_ELEMENTS = INPUT_WIDTH * INPUT_HEIGHT * 3

// OpenNSFW Caffe BGR channel mean subtraction constants
private const val MEAN_BLUE = 103.939f
private const val MEAN_GREEN = 116.779f
private const val MEAN_RED = 123.68f

data class LocalNsfwPrediction(
    val sfwProbability: Float,
    val nsfwProbability: Float,
)

interface LocalNsfwClassifier {
    suspend fun classifyJpeg(jpegBytes: ByteArray): LocalNsfwPrediction?
}

/**
 * Pure Kotlin preprocessing helper (testable on JVM/Robolectric without native JNI).
 * Resizes to 256x256, center-crops 224x224, converts ARGB -> BGR FloatArray [0..255] minus VGG means.
 */
object OpenNsfwPreprocessor {
    fun preprocessBitmapToBgrFloatArray(source: Bitmap): FloatArray {
        val scaled = Bitmap.createScaledBitmap(source, RESIZE_SIDE, RESIZE_SIDE, true)
        val argbBitmap = if (scaled.config == Bitmap.Config.ARGB_8888) {
            scaled
        } else {
            scaled.copy(Bitmap.Config.ARGB_8888, false).also {
                if (scaled !== source && !scaled.isRecycled) {
                    scaled.recycle()
                }
            }
        }

        return try {
            val cropX = max((argbBitmap.width - INPUT_WIDTH) / 2, 0)
            val cropY = max((argbBitmap.height - INPUT_HEIGHT) / 2, 0)
            val pixels = IntArray(INPUT_WIDTH * INPUT_HEIGHT)
            argbBitmap.getPixels(
                pixels,
                0,
                INPUT_WIDTH,
                cropX,
                cropY,
                INPUT_WIDTH,
                INPUT_HEIGHT,
            )

            val output = FloatArray(INPUT_ELEMENTS)
            var outIdx = 0
            for (pixel in pixels) {
                val r = ((pixel shr 16) and 0xFF).toFloat()
                val g = ((pixel shr 8) and 0xFF).toFloat()
                val b = (pixel and 0xFF).toFloat()
                output[outIdx++] = b - MEAN_BLUE
                output[outIdx++] = g - MEAN_GREEN
                output[outIdx++] = r - MEAN_RED
            }
            output
        } finally {
            if (argbBitmap !== source && !argbBitmap.isRecycled) {
                argbBitmap.recycle()
            }
        }
    }
}

/**
 * Single-thread-confined LiteRT CompiledModel wrapper following official Android LiteRT mandates:
 * - Single Environment shared across process
 * - Single-thread executor owns CompiledModel creation, warm-up, reusable TensorBuffers, and inference
 * - GPU preferred with automatic CPU fallback
 * - Graceful null fallback on JVM unit tests where native Android arm64/x86_64 .so isn't loaded
 */
class LiteRtOnDeviceNsfwClassifier private constructor(
    private val appContext: Context,
) : LocalNsfwClassifier {

    companion object {
        @Volatile
        private var instance: LiteRtOnDeviceNsfwClassifier? = null

        private val sharedEnvironment: Environment by lazy {
            Environment.create()
        }

        private val singleThreadDispatcher by lazy {
            Executors.newSingleThreadExecutor { runnable ->
                Thread(runnable, "ArekSby-LiteRT-NSFW").apply { isDaemon = true }
            }.asCoroutineDispatcher()
        }

        fun getInstance(context: Context): LiteRtOnDeviceNsfwClassifier {
            return instance ?: synchronized(this) {
                instance ?: LiteRtOnDeviceNsfwClassifier(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    // Owned exclusively by singleThreadDispatcher
    private var compiledRunner: CompiledNsfwModelRunner? = null
    private var initializationFailedPermanently: Boolean = false

    override suspend fun classifyJpeg(jpegBytes: ByteArray): LocalNsfwPrediction? {
        if (jpegBytes.isEmpty() || initializationFailedPermanently) return null

        val decoded = decodeBoundedArgbBitmap(jpegBytes) ?: return null
        val inputFloats = try {
            OpenNsfwPreprocessor.preprocessBitmapToBgrFloatArray(decoded)
        } catch (e: Throwable) {
            Log.w(LOCAL_NSFW_TAG, "Failed to preprocess bitmap for on-device NSFW check", e)
            return null
        } finally {
            if (!decoded.isRecycled) {
                decoded.recycle()
            }
        }

        return withContext(singleThreadDispatcher) {
            if (initializationFailedPermanently) return@withContext null

            val runner = compiledRunner ?: try {
                createRunnerWithGpuFallback().also { compiledRunner = it }
            } catch (t: Throwable) {
                // Catches UnsatisfiedLinkError on host JVM tests or LiteRtException if asset unavailable
                initializationFailedPermanently = true
                Log.w(LOCAL_NSFW_TAG, "On-device LiteRT unavailable in this runtime (${t::class.java.simpleName}); falling back to Cloud AI batch verifier.")
                return@withContext null
            }

            try {
                val probabilities = runner.infer(inputFloats)
                if (probabilities.size >= 2) {
                    val sfw = probabilities[0].coerceIn(0f, 1f)
                    val nsfw = probabilities[1].coerceIn(0f, 1f)
                    LocalNsfwPrediction(sfwProbability = sfw, nsfwProbability = nsfw)
                } else {
                    null
                }
            } catch (t: Throwable) {
                Log.w(LOCAL_NSFW_TAG, "On-device NSFW inference failed; delegating to Cloud AI", t)
                null
            }
        }
    }

    private fun createRunnerWithGpuFallback(): CompiledNsfwModelRunner {
        return try {
            CompiledNsfwModelRunner(appContext, Accelerator.GPU, sharedEnvironment)
        } catch (gpuError: LiteRtException) {
            Log.i(LOCAL_NSFW_TAG, "LiteRT GPU unavailable (${gpuError.message}), falling back to CPU accelerator")
            CompiledNsfwModelRunner(appContext, Accelerator.CPU, sharedEnvironment)
        }
    }

    private fun decodeBoundedArgbBitmap(bytes: ByteArray, maxDim: Int = 512): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            var sample = 1
            while (bounds.outWidth / sample > maxDim || bounds.outHeight / sample > maxDim) {
                sample *= 2
            }
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        } catch (e: Exception) {
            null
        }
    }
}

private class CompiledNsfwModelRunner(
    context: Context,
    accelerator: Accelerator,
    environment: Environment,
) : AutoCloseable {

    private val model: CompiledModel = CompiledModel.create(
        context.assets,
        MODEL_ASSET_NAME,
        CompiledModel.Options(accelerator),
        environment,
    )
    private val inputs = mutableListOf<TensorBuffer>()
    private val outputs = mutableListOf<TensorBuffer>()

    init {
        try {
            inputs += model.createInputBuffers()
            outputs += model.createOutputBuffers()
            // Mandatory LiteRT warm-up inference on the owning background thread
            infer(FloatArray(INPUT_ELEMENTS))
        } catch (t: Throwable) {
            close()
            throw t
        }
    }

    fun infer(inputFloats: FloatArray): FloatArray {
        inputs[0].writeFloat(inputFloats)
        model.run(inputs, outputs)
        return outputs[0].readFloat()
    }

    override fun close() {
        (inputs + outputs).forEach { runCatching { it.close() } }
        inputs.clear()
        outputs.clear()
        runCatching { model.close() }
    }
}
