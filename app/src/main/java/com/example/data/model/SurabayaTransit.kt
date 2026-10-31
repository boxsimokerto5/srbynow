package com.example.data.model

enum class TransitCategory(val label: String) {
    SEMUA("Semua"),
    FEEDER_WIRAWIRI("WiraWiri Feeder"),
    SUROBOYO_BUS("Suroboyo Bus"),
    TRANS_SEMANGGI("Trans Semanggi"),
}

data class TransitRoute(
    val id: String,
    val code: String,
    val name: String,
    val category: TransitCategory,
    val fleetType: String,
    val fleetDetails: String,
    val operator: String,
    val operatingHours: String,
    val fare: String,
    val intervalMinutes: String,
    val description: String,
    val origin: String,
    val destination: String,
    val keyStops: List<String>,
    val transferHubs: List<String>,
    val badgeColorHex: Long = 0xFFD32F2F,
)
