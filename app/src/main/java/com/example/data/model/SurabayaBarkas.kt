package com.example.data.model

import java.util.Locale

data class BarkasItem(
    val id: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val title: String = "",
    val category: String = "ELEKTRONIK", // ELEKTRONIK, KENDARAAN, PERABOT, PAKAIAN_HOBI, LAINNYA
    val itemCondition: String = "BEKAS_TERAWAT", // SEPERTI_BARU, BEKAS_TERAWAT, LAYAK_PAKAI, MINUS_SERVIS
    val price: Long = 0L,
    val isNegotiable: Boolean = true,
    val district: String = "Genteng",
    val codLocation: String = "",
    val description: String = "",
    val whatsappNumber: String = "",
    val photoUrl: String = "",
    val photoUrls: List<String> = emptyList(),
    val isSold: Boolean = false,
    val commentCount: Int = 0,
    val contentReportCount: Int = 0,
    val contentReportedByUids: List<String> = emptyList(),
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
            "Gratis / Hibah Warga"
        } else {
            "Rp %,d".format(Locale.GERMANY, price).replace(',', '.')
        }

    val negotiableLabel: String
        get() = if (price <= 0L) "Hibah Gratis" else if (isNegotiable) "Bisa Nego" else "Harga Pas"

    val conditionLabel: String
        get() = formatBarkasConditionLabel(itemCondition)

    val categoryLabel: String
        get() = formatBarkasCategoryLabel(category)

    val isContentBlurred: Boolean
        get() = contentReportCount >= 5

    val isContentQuarantined: Boolean
        get() = contentReportCount >= 10

    fun isOwnedBy(userId: String): Boolean = userId.isNotBlank() && sellerId == userId
}

data class BarkasComment(
    val id: String = "",
    val barkasId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorDistrict: String = "Genteng",
    val text: String = "",
    val createdAt: Long? = null,
)

fun formatBarkasCategoryLabel(raw: String): String = when (raw.trim().uppercase()) {
    "ELEKTRONIK" -> "Elektronik & Gadget"
    "KENDARAAN" -> "Sepeda & Otomotif"
    "PERABOT" -> "Perabot & Kos"
    "PAKAIAN_HOBI" -> "Pakaian & Hobi"
    else -> "Lainnya"
}

fun formatBarkasConditionLabel(raw: String): String = when (raw.trim().uppercase()) {
    "SEPERTI_BARU" -> "Seperti Baru"
    "BEKAS_TERAWAT" -> "Bekas Terawat"
    "LAYAK_PAKAI" -> "Layak Pakai"
    "MINUS_SERVIS" -> "Ada Minus / Bahan"
    else -> "Bekas Terawat"
}

object SurabayaBarkasSeed {
    fun starterItems(seedAuthorId: String = "system_seed"): List<BarkasItem> = emptyList()
    fun starterComments(): Map<String, List<BarkasComment>> = emptyMap()
}

