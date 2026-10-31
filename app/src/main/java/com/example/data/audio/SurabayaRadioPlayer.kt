package com.example.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class RadioPlaybackState {
    STOPPED,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR,
}

data class RadioStationInfo(
    val callSign: String = "Suara Surabaya",
    val frequency: String = "100.0 FM",
    val currentProgram: String = "Kelana Kota Suara Surabaya Live",
    val description: String = "Pusat Pantauan Lalu Lintas, Info Mudik, & Solusi Kedaruratan Arek Surabaya",
    val hotlinePhone: String = "03199000000",
    val hotlineWhatsApp: String = "628113041000",
    val streamUrls: List<String> = listOf(
        "https://stream.suarasurabaya.net/live",
        "https://cast1.asurahosting.com:8587/stream",
        "http://stream.suarasurabaya.net:8000/live",
    ),
)

/**
 * Manages live audio streaming for Radio Suara Surabaya (100.0 FM).
 * Provides reactive playback states, volume control, and network fallback recovery.
 */
class SurabayaRadioPlayer private constructor(
    private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _playbackState = MutableStateFlow(RadioPlaybackState.STOPPED)
    val playbackState: StateFlow<RadioPlaybackState> = _playbackState.asStateFlow()

    private val _volume = MutableStateFlow(0.85f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _stationInfo = MutableStateFlow(RadioStationInfo())
    val stationInfo: StateFlow<RadioStationInfo> = _stationInfo.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var currentUrlIndex = 0

    @Synchronized
    fun play() {
        if (_playbackState.value == RadioPlaybackState.PLAYING) return

        if (_playbackState.value == RadioPlaybackState.PAUSED && mediaPlayer != null) {
            try {
                mediaPlayer?.start()
                _playbackState.value = RadioPlaybackState.PLAYING
                _errorMessage.value = null
                return
            } catch (e: Exception) {
                Log.w(TAG, "Resume failed, re-initializing stream", e)
                releasePlayer()
            }
        }

        initializeAndStream(urlIndex = 0)
    }

    @Synchronized
    fun pause() {
        if (_playbackState.value == RadioPlaybackState.PLAYING) {
            try {
                mediaPlayer?.pause()
                _playbackState.value = RadioPlaybackState.PAUSED
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing media player", e)
                stop()
            }
        }
    }

    @Synchronized
    fun toggle() {
        when (_playbackState.value) {
            RadioPlaybackState.PLAYING -> pause()
            RadioPlaybackState.PAUSED -> play()
            RadioPlaybackState.STOPPED, RadioPlaybackState.ERROR -> play()
            RadioPlaybackState.BUFFERING -> stop()
        }
    }

    @Synchronized
    fun stop() {
        releasePlayer()
        _playbackState.value = RadioPlaybackState.STOPPED
        _errorMessage.value = null
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        try {
            mediaPlayer?.setVolume(clamped, clamped)
        } catch (e: Exception) {
            Log.w(TAG, "Could not adjust player volume", e)
        }
    }

    private fun initializeAndStream(urlIndex: Int) {
        val urls = _stationInfo.value.streamUrls
        if (urls.isEmpty()) {
            _playbackState.value = RadioPlaybackState.ERROR
            _errorMessage.value = "Tidak ada URL streaming yang tersedia."
            return
        }

        currentUrlIndex = urlIndex.coerceIn(0, urls.lastIndex)
        val targetUrl = urls[currentUrlIndex]

        releasePlayer()
        _playbackState.value = RadioPlaybackState.BUFFERING
        _errorMessage.value = null

        scope.launch(Dispatchers.IO) {
            try {
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(context, Uri.parse(targetUrl))
                    val curVol = _volume.value
                    setVolume(curVol, curVol)

                    setOnPreparedListener { mp ->
                        synchronized(this@SurabayaRadioPlayer) {
                            try {
                                mp.start()
                                _playbackState.value = RadioPlaybackState.PLAYING
                                _errorMessage.value = null
                                Log.i(TAG, "Suara Surabaya stream started successfully: $targetUrl")
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to start after prepare", e)
                                handleError("Gagal memutar audio siaran.")
                            }
                        }
                    }

                    setOnErrorListener { _, what, extra ->
                        Log.w(TAG, "MediaPlayer error: what=$what, extra=$extra on url=$targetUrl")
                        handleStreamFailure(targetUrl)
                        true
                    }

                    setOnCompletionListener {
                        // Live streams rarely complete normally unless the stream disconnected
                        Log.i(TAG, "Stream ended, reconnecting...")
                        if (_playbackState.value == RadioPlaybackState.PLAYING) {
                            play()
                        }
                    }

                    prepareAsync()
                }

                synchronized(this@SurabayaRadioPlayer) {
                    mediaPlayer = player
                }
            } catch (e: Exception) {
                Log.e(TAG, "Initialization failed for stream: $targetUrl", e)
                handleStreamFailure(targetUrl)
            }
        }
    }

    private fun handleStreamFailure(failedUrl: String) {
        val urls = _stationInfo.value.streamUrls
        val nextIndex = currentUrlIndex + 1
        if (nextIndex < urls.size) {
            Log.i(TAG, "Attempting fallback stream url at index $nextIndex: ${urls[nextIndex]}")
            initializeAndStream(nextIndex)
        } else {
            handleError("Koneksi ke siaran Radio SS 100 FM terputus. Silakan coba kembali.")
        }
    }

    private fun handleError(message: String) {
        releasePlayer()
        _playbackState.value = RadioPlaybackState.ERROR
        _errorMessage.value = message
    }

    @Synchronized
    private fun releasePlayer() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.reset()
                player.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing MediaPlayer", e)
        } finally {
            mediaPlayer = null
        }
    }

    companion object {
        private const val TAG = "SurabayaRadioPlayer"

        @Volatile
        private var instance: SurabayaRadioPlayer? = null

        fun getInstance(context: Context): SurabayaRadioPlayer {
            return instance ?: synchronized(this) {
                instance ?: SurabayaRadioPlayer(context.applicationContext).also { instance = it }
            }
        }
    }
}
