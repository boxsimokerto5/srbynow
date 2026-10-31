package com.example.data.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Foreground Service for robust, continuous background live streaming of Radio Suara Surabaya (100.0 FM).
 * Utilizes Jetpack Media3 (ExoPlayer) with automatic network reconnects, audio focus ducking, and wake locks.
 */
class SurabayaRadioService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var exoPlayer: ExoPlayer? = null
    private var currentUrlIndex = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializePlayer()
    }

    private fun initializePlayer() {
        if (exoPlayer != null) return

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("ArekSurabaya/1.0 (Linux; Android; RadioPlayer)")
            .setConnectTimeoutMs(8000)
            .setReadTimeoutMs(10000)
            .setAllowCrossProtocolRedirects(true)

        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(httpDataSourceFactory)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        exoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
            .apply {
                volume = _serviceVolume.value

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                _servicePlaybackState.value = RadioPlaybackState.BUFFERING
                                updateNotification()
                            }
                            Player.STATE_READY -> {
                                if (playWhenReady) {
                                    _servicePlaybackState.value = RadioPlaybackState.PLAYING
                                    _serviceErrorMessage.value = null
                                } else {
                                    _servicePlaybackState.value = RadioPlaybackState.PAUSED
                                }
                                updateNotification()
                            }
                            Player.STATE_ENDED -> {
                                Log.i(TAG, "Stream ended, attempting auto-reconnect...")
                                playStream(currentUrlIndex)
                            }
                            Player.STATE_IDLE -> {
                                if (_servicePlaybackState.value != RadioPlaybackState.ERROR) {
                                    _servicePlaybackState.value = RadioPlaybackState.STOPPED
                                }
                                updateNotification()
                            }
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            _servicePlaybackState.value = RadioPlaybackState.PLAYING
                            _serviceErrorMessage.value = null
                        } else if (playbackState == Player.STATE_READY) {
                            _servicePlaybackState.value = RadioPlaybackState.PAUSED
                        }
                        updateNotification()
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.w(TAG, "ExoPlayer error on stream index $currentUrlIndex: ${error.message}", error)
                        handleStreamFallback()
                    }
                })
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_PLAY
        when (action) {
            ACTION_PLAY -> handlePlay()
            ACTION_PAUSE -> handlePause()
            ACTION_TOGGLE -> handleToggle()
            ACTION_STOP -> handleStop()
            ACTION_SET_VOLUME -> {
                val vol = intent?.getFloatExtra(EXTRA_VOLUME, 0.85f) ?: 0.85f
                setPlayerVolume(vol)
            }
        }
        return START_NOT_STICKY
    }

    private fun handlePlay() {
        startForegroundWithNotification()
        val player = exoPlayer ?: return
        if (_servicePlaybackState.value == RadioPlaybackState.PAUSED && player.playbackState == Player.STATE_READY) {
            player.playWhenReady = true
            _servicePlaybackState.value = RadioPlaybackState.PLAYING
            updateNotification()
        } else {
            playStream(urlIndex = 0)
        }
    }

    private fun handlePause() {
        exoPlayer?.playWhenReady = false
        _servicePlaybackState.value = RadioPlaybackState.PAUSED
        updateNotification()
    }

    private fun handleToggle() {
        if (_servicePlaybackState.value == RadioPlaybackState.PLAYING) {
            handlePause()
        } else {
            handlePlay()
        }
    }

    private fun handleStop() {
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        _servicePlaybackState.value = RadioPlaybackState.STOPPED
        _serviceErrorMessage.value = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun playStream(urlIndex: Int) {
        val urls = _serviceStationInfo.value.streamUrls
        if (urls.isEmpty()) {
            _servicePlaybackState.value = RadioPlaybackState.ERROR
            _serviceErrorMessage.value = "Tidak ada URL streaming yang tersedia."
            return
        }

        currentUrlIndex = urlIndex.coerceIn(0, urls.lastIndex)
        val targetUrl = urls[currentUrlIndex]
        Log.i(TAG, "Connecting to stream: $targetUrl (index $currentUrlIndex)")

        _servicePlaybackState.value = RadioPlaybackState.BUFFERING
        _serviceErrorMessage.value = null
        updateNotification()

        val mediaItem = MediaItem.Builder()
            .setUri(targetUrl)
            .setLiveConfiguration(
                MediaItem.LiveConfiguration.Builder()
                    .setMaxPlaybackSpeed(1.02f)
                    .setMinPlaybackSpeed(0.98f)
                    .build()
            )
            .build()

        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    private fun handleStreamFallback() {
        val urls = _serviceStationInfo.value.streamUrls
        val nextIndex = currentUrlIndex + 1
        if (nextIndex < urls.size) {
            Log.i(TAG, "Switching to fallback stream at index $nextIndex: ${urls[nextIndex]}")
            playStream(nextIndex)
        } else {
            _servicePlaybackState.value = RadioPlaybackState.ERROR
            _serviceErrorMessage.value = "Koneksi ke siaran Radio SS 100 FM terputus. Silakan coba kembali."
            updateNotification()
        }
    }

    private fun setPlayerVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _serviceVolume.value = clamped
        exoPlayer?.volume = clamped
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification()
        val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, foregroundServiceType)
    }

    private fun updateNotification() {
        if (_servicePlaybackState.value == RadioPlaybackState.STOPPED) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val isPlaying = _servicePlaybackState.value == RadioPlaybackState.PLAYING
        val isBuffering = _servicePlaybackState.value == RadioPlaybackState.BUFFERING

        val toggleActionIntent = Intent(this, SurabayaRadioService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleActionIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopActionIntent = Intent(this, SurabayaRadioService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopActionIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val statusText = when (_servicePlaybackState.value) {
            RadioPlaybackState.PLAYING -> "Mengudara Live 100.0 FM • Kelana Kota"
            RadioPlaybackState.BUFFERING -> "Menghubungkan ke siaran SSFM…"
            RadioPlaybackState.PAUSED -> "Siaran dijeda"
            RadioPlaybackState.ERROR -> "Gangguan koneksi siaran"
            RadioPlaybackState.STOPPED -> "Siaran berhenti"
        }

        val toggleLabel = if (isPlaying) "Jeda" else "Putar"
        val toggleIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Radio Suara Surabaya (100.0 FM)")
            .setContentText(statusText)
            .setSubText("Radio Warga Surabaya")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(contentPendingIntent)
            .setOngoing(isPlaying || isBuffering)
            .addAction(toggleIcon, toggleLabel, togglePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Tutup", stopPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Siaran Radio Suara Surabaya"
            val descriptionText = "Notifikasi pemutaran latar belakang Radio Suara Surabaya 100 FM"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        exoPlayer?.release()
        exoPlayer = null
        _servicePlaybackState.value = RadioPlaybackState.STOPPED
        super.onDestroy()
    }

    companion object {
        private const val TAG = "SurabayaRadioService"
        private const val CHANNEL_ID = "surabaya_radio_stream_channel"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.example.action.RADIO_PLAY"
        const val ACTION_PAUSE = "com.example.action.RADIO_PAUSE"
        const val ACTION_TOGGLE = "com.example.action.RADIO_TOGGLE"
        const val ACTION_STOP = "com.example.action.RADIO_STOP"
        const val ACTION_SET_VOLUME = "com.example.action.RADIO_SET_VOLUME"
        const val EXTRA_VOLUME = "extra_volume"

        private val _servicePlaybackState = MutableStateFlow(RadioPlaybackState.STOPPED)
        val playbackState: StateFlow<RadioPlaybackState> = _servicePlaybackState.asStateFlow()

        private val _serviceVolume = MutableStateFlow(0.85f)
        val volume: StateFlow<Float> = _serviceVolume.asStateFlow()

        private val _serviceErrorMessage = MutableStateFlow<String?>(null)
        val errorMessage: StateFlow<String?> = _serviceErrorMessage.asStateFlow()

        private val _serviceStationInfo = MutableStateFlow(
            RadioStationInfo(
                callSign = "Suara Surabaya",
                frequency = "100.0 FM",
                currentProgram = "Kelana Kota Suara Surabaya Live",
                description = "Pusat Pantauan Lalu Lintas, Berita Cepat & Solusi Kedaruratan Arek Surabaya",
                hotlinePhone = "03199000000",
                hotlineWhatsApp = "628113041000",
                streamUrls = listOf(
                    "https://c5.siar.us/proxy/ssfm/stream",
                    "https://c5.siar.us/proxy/ssfm/",
                    "http://stream.suarasurabaya.net:8000/stream"
                )
            )
        )
        val stationInfo: StateFlow<RadioStationInfo> = _serviceStationInfo.asStateFlow()

        fun startPlay(context: Context) {
            val intent = Intent(context, SurabayaRadioService::class.java).apply {
                action = ACTION_PLAY
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun pause(context: Context) {
            val intent = Intent(context, SurabayaRadioService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun toggle(context: Context) {
            val intent = Intent(context, SurabayaRadioService::class.java).apply {
                action = ACTION_TOGGLE
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, SurabayaRadioService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun setVolume(context: Context, volume: Float) {
            val intent = Intent(context, SurabayaRadioService::class.java).apply {
                action = ACTION_SET_VOLUME
                putExtra(EXTRA_VOLUME, volume)
            }
            context.startService(intent)
        }
    }
}
