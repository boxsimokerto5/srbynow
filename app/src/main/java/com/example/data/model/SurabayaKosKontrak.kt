package com.example.data.model

import com.example.data.util.LinkSecurityValidator
import com.example.data.util.LinkValidationResult
import com.example.data.util.SafeLinkCategory
import java.util.Locale

data class KosKontrakItem(
    val id: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val title: String = "",
    val propertyType: String = "KOS_PUTRA", // KOS_PUTRA, KOS_PUTRI, KOS_CAMPUR, KONTRAKAN
    val billingPeriod: String = "BULANAN", // BULANAN, TAHUNAN
    val price: Long = 0L,
    val availableRooms: Int = 1,
    val facilities: List<String> = emptyList(),
    val district: String = "Gubeng",
    val address: String = "",
    val mapsUrl: String = "",
    val whatsappNumber: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val photoUrls: List<String> = emptyList(),
    val isFull: Boolean = false,
    val commentCount: Int = 0,
    val contentReportCount: Int = 0,
    val contentReportedByUids: List<String> = emptyList(),
    val linkReportCount: Int = 0,
    val linkReportedByUids: List<String> = emptyList(),
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
) {
    val allPhotos: List<String>
        get() = when {
            photoUrls.isNotEmpty() -> photoUrls.filter { it.isNotBlank() }
            photoUrl.isNotBlank() -> listOf(photoUrl)
            else -> emptyList()
        }

    val firstPhotoUrl: String
        get() = allPhotos.firstOrNull().orEmpty()

    val formattedPrice: String
        get() = if (price <= 0L) {
            "Hubungi Pemilik"
        } else {
            "Rp %,d".format(Locale.GERMANY, price).replace(',', '.')
        }

    val billingSuffix: String
        get() = when (billingPeriod.trim().uppercase()) {
            "TAHUNAN" -> "/ tahun"
            else -> "/ bulan"
        }

    val billingBadgeLabel: String
        get() = when (billingPeriod.trim().uppercase()) {
            "TAHUNAN" -> "Sewa Tahunan"
            else -> "Sewa Bulanan"
        }

    val priceWithPeriod: String
        get() = if (price <= 0L) formattedPrice else "$formattedPrice $billingSuffix"

    val propertyTypeLabel: String
        get() = formatKosPropertyTypeLabel(propertyType)

    val availabilityLabel: String
        get() = when {
            isFull -> "PENUH / TERSEWA"
            propertyType.equals("KONTRAKAN", ignoreCase = true) -> "Siap Huni"
            availableRooms > 0 -> "Sisa $availableRooms Kamar"
            else -> "Tersedia"
        }

    val safeMapsUrl: String
        get() {
            if (linkReportCount >= 3 || mapsUrl.isBlank()) return ""
            val res = LinkSecurityValidator.validateOptionalLink(mapsUrl, SafeLinkCategory.GOOGLE_MAPS)
            return (res as? LinkValidationResult.Valid)?.normalizedUrl.orEmpty()
        }

    val hasCustomMapsLink: Boolean
        get() = safeMapsUrl.isNotBlank()

    val hasAnyExternalLink: Boolean
        get() = mapsUrl.isNotBlank()

    val isContentBlurred: Boolean
        get() = contentReportCount >= 5

    val isContentQuarantined: Boolean
        get() = contentReportCount >= 10

    fun isOwnedBy(userId: String): Boolean = userId.isNotBlank() && ownerId == userId
}

data class KosComment(
    val id: String = "",
    val kosId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorDistrict: String = "Genteng",
    val text: String = "",
    val createdAt: Long? = null,
)

fun formatKosPropertyTypeLabel(raw: String): String = when (raw.trim().uppercase()) {
    "KOS_PUTRA" -> "Kos Putra"
    "KOS_PUTRI" -> "Kos Putri"
    "KOS_CAMPUR" -> "Kos Pasutri / Campur"
    "KONTRAKAN" -> "Kontrakan Rumah"
    else -> "Kos & Kontrakan"
}

val KOS_STANDARD_FACILITIES = listOf(
    "KM Dalam",
    "AC",
    "Kipas Angin",
    "WiFi",
    "Kasur & Lemari",
    "Listrik Termasuk",
    "Listrik Token",
    "Parkir Motor",
    "Parkir Mobil",
    "Dapur Bersama",
    "Akses 24 Jam",
)

object SurabayaKosSeed {
    fun starterListings(seedOwnerPrefix: String = "system_seed"): List<KosKontrakItem> = emptyList()
    fun starterComments(): Map<String, List<KosComment>> = emptyMap()
}

