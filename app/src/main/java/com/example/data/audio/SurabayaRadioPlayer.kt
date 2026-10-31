package com.example.data.audio

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

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
    val description: String = "Pusat Pantauan Lalu Lintas, Berita Cepat & Solusi Kedaruratan Arek Surabaya",
    val hotlinePhone: String = "03199000000",
    val hotlineWhatsApp: String = "628113041000",
    val streamUrls: List<String> = listOf(
        "https://c5.siar.us/proxy/ssfm/stream",
        "https://c5.siar.us/proxy/ssfm/",
        "http://stream.suarasurabaya.net:8000/stream",
    ),
)

/**
 * Controller for Radio Suara Surabaya (100.0 FM).
 * Delegates playback and background execution to SurabayaRadioService (Jetpack Media3 ExoPlayer).
 */
class SurabayaRadioPlayer private constructor(
    private val context: Context,
) {
    val playbackState: StateFlow<RadioPlaybackState> = SurabayaRadioService.playbackState
    val volume: StateFlow<Float> = SurabayaRadioService.volume
    val errorMessage: StateFlow<String?> = SurabayaRadioService.errorMessage
    val stationInfo: StateFlow<RadioStationInfo> = SurabayaRadioService.stationInfo

    fun play() {
        SurabayaRadioService.startPlay(context)
    }

    fun pause() {
        SurabayaRadioService.pause(context)
    }

    fun toggle() {
        SurabayaRadioService.toggle(context)
    }

    fun stop() {
        SurabayaRadioService.stop(context)
    }

    fun setVolume(vol: Float) {
        SurabayaRadioService.setVolume(context, vol)
    }

    companion object {
        @Volatile
        private var instance: SurabayaRadioPlayer? = null

        fun getInstance(context: Context): SurabayaRadioPlayer {
            return instance ?: synchronized(this) {
                instance ?: SurabayaRadioPlayer(context.applicationContext).also { instance = it }
            }
        }
    }
}
