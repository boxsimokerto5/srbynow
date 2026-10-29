package com.example.data.util

import android.util.Log
import com.example.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun logBackendError(
    exception: Exception,
    operationType: OperationType,
    path: String?,
) {
    Log.e("PocketBaseRepo", "Error [${operationType.value}] on $path: ${exception.message}", exception)
}

fun parsePocketBaseTimestamp(raw: String?): Long? {
    if (raw.isNullOrBlank()) return null
    val normalized = raw.trim().replace('T', ' ')
    val patterns = listOf(
        "yyyy-MM-dd HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm:ss.SSS",
        "yyyy-MM-dd HH:mm:ss",
    )
    for (pattern in patterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val parsed = sdf.parse(normalized)
            if (parsed != null) return parsed.time
        } catch (_: Exception) {
            // Try next format
        }
    }
    return null
}

data class SurabayaDistrictCoordinate(
    val name: String,
    val lat: Double,
    val lng: Double,
)

object SurabayaDistricts {
    const val DEFAULT_LAT = -7.2575
    const val DEFAULT_LNG = 112.7521

    val all: List<SurabayaDistrictCoordinate> = listOf(
        SurabayaDistrictCoordinate("Genteng", -7.2590, 112.7435),
        SurabayaDistrictCoordinate("Gubeng", -7.2738, 112.7563),
        SurabayaDistrictCoordinate("Wonokromo", -7.2995, 112.7382),
        SurabayaDistrictCoordinate("Tegalsari", -7.2775, 112.7392),
        SurabayaDistrictCoordinate("Rungkut", -7.3228, 112.7852),
        SurabayaDistrictCoordinate("Sukolilo", -7.2896, 112.7834),
        SurabayaDistrictCoordinate("Sawahan", -7.2692, 112.7195),
        SurabayaDistrictCoordinate("Wiyung", -7.3130, 112.6968),
        SurabayaDistrictCoordinate("Kenjeran", -7.2298, 112.7795),
        SurabayaDistrictCoordinate("Pabean Cantian", -7.2355, 112.7388),
        SurabayaDistrictCoordinate("Gayungan", -7.3340, 112.7285),
        SurabayaDistrictCoordinate("Sambikerep", -7.2798, 112.6625),
    )

    fun names(): List<String> = all.map { it.name }

    fun coordinateFor(districtName: String): Pair<Double, Double> {
        val found = all.firstOrNull { it.name.equals(districtName, ignoreCase = true) }
        return if (found != null) found.lat to found.lng else DEFAULT_LAT to DEFAULT_LNG
    }

    fun nearestDistrict(lat: Double, lng: Double): String {
        return all.minByOrNull { calculateDistanceKm(lat, lng, it.lat, it.lng) }?.name ?: "Genteng"
    }
}

fun calculateDistanceKm(
    lat1: Double,
    lng1: Double,
    lat2: Double,
    lng2: Double,
): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2.0) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val raw = earthRadiusKm * c
    return (raw * 10.0).roundToInt() / 10.0
}

fun formatDistanceLabel(km: Double): String {
    return when {
        km < 0.2 -> "< 200 m"
        km < 1.0 -> "${(km * 1000).roundToInt()} m"
        else -> String.format(Locale.forLanguageTag("id-ID"), "%.1f km", km)
    }
}

fun isCulinaryOpenNow(openHour: Int, closeHour: Int): Boolean {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return if (openHour <= closeHour) {
        currentHour in openHour until closeHour
    } else {
        currentHour >= openHour || currentHour < closeHour
    }
}

fun formatOperatingHours(openHour: Int, closeHour: Int): String {
    val openStr = String.format(Locale.US, "%02d.00", openHour)
    val closeStr = String.format(Locale.US, "%02d.00", closeHour % 24)
    return "$openStr – $closeStr WIB"
}

fun formatRelativeTimestamp(epochMs: Long?): String {
    if (epochMs == null || epochMs <= 0L) return "Baru saja"
    val diffMs = System.currentTimeMillis() - epochMs
    val minutes = diffMs / 60_000L
    val hours = minutes / 60L
    return when {
        minutes < 1 -> "Baru saja"
        minutes < 60 -> "$minutes mnt lalu"
        hours < 24 -> "$hours jam lalu"
        else -> SimpleDateFormat("dd MMM, HH:mm", Locale.forLanguageTag("id-ID")).format(Date(epochMs))
    }
}

fun resolveImageDrawable(imageKey: String): Int {
    return when (imageKey) {
        "hero_surabaya" -> R.drawable.img_hero_surabaya_1793207382246
        "kuliner_rawon" -> R.drawable.img_kuliner_rawon_1793207383497
        "event_festival" -> R.drawable.img_event_festival_1793207386551
        "kejadian_kota" -> R.drawable.img_kejadian_kota_1793207386068
        else -> R.drawable.img_hero_surabaya_1793207382246
    }
}
