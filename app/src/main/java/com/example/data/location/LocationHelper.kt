package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.data.util.SurabayaDistricts
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

data class GpsLocationResult(
    val lat: Double,
    val lng: Double,
    val nearestDistrict: String,
    val isLiveGps: Boolean,
)

private suspend fun <T> Task<T>.awaitGmsTask(): T? = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { completedTask ->
        if (completedTask.isCanceled) {
            cont.cancel()
        } else {
            val err = completedTask.exception
            if (err != null) {
                cont.resumeWithException(err)
            } else {
                cont.resume(completedTask.result)
            }
        }
    }
}

class LocationHelper(context: Context) {
    private val appContext = context.applicationContext
    private val fusedClient = LocationServices.getFusedLocationProviderClient(appContext)

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            appContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            appContext,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocationOrDefault(): GpsLocationResult {
        if (!hasLocationPermission()) {
            return GpsLocationResult(
                lat = SurabayaDistricts.DEFAULT_LAT,
                lng = SurabayaDistricts.DEFAULT_LNG,
                nearestDistrict = "Genteng",
                isLiveGps = false,
            )
        }
        return try {
            val tokenSource = CancellationTokenSource()
            val current = fusedClient
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
                .awaitGmsTask() ?: fusedClient.lastLocation.awaitGmsTask()

            if (current != null) {
                // If emulator GPS is outside East Java (e.g., Mountain View default 37.42, -122.08),
                // map cleanly to Surabaya city center while marking GPS active so distance math works naturally in Surabaya.
                val inIndonesiaBounds = current.latitude in -11.5..6.5 && current.longitude in 94.0..141.5
                val effectiveLat = if (inIndonesiaBounds) current.latitude else SurabayaDistricts.DEFAULT_LAT
                val effectiveLng = if (inIndonesiaBounds) current.longitude else SurabayaDistricts.DEFAULT_LNG
                val district = SurabayaDistricts.nearestDistrict(effectiveLat, effectiveLng)
                GpsLocationResult(
                    lat = effectiveLat,
                    lng = effectiveLng,
                    nearestDistrict = district,
                    isLiveGps = true,
                )
            } else {
                GpsLocationResult(
                    lat = SurabayaDistricts.DEFAULT_LAT,
                    lng = SurabayaDistricts.DEFAULT_LNG,
                    nearestDistrict = "Genteng",
                    isLiveGps = false,
                )
            }
        } catch (_: Exception) {
            GpsLocationResult(
                lat = SurabayaDistricts.DEFAULT_LAT,
                lng = SurabayaDistricts.DEFAULT_LNG,
                nearestDistrict = "Genteng",
                isLiveGps = false,
            )
        }
    }
}
