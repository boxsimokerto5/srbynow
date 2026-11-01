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
        get() = contentReportCount >= 1

    val isContentQuarantined: Boolean
        get() = contentReportCount >= 3

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
    fun starterItems(seedAuthorId: String = "system_seed"): List<BarkasItem> {
        val now = System.currentTimeMillis()
        return listOf(
            BarkasItem(
                id = "brk_polygon_sby",
                sellerId = "${seedAuthorId}_hendra",
                sellerName = "Cak Hendra",
                title = "Sepeda Lipat Polygon Urbano 3 Mulus Siap Gowes",
                category = "KENDARAAN",
                itemCondition = "BEKAS_TERAWAT",
                price = 2150000L,
                isNegotiable = true,
                district = "Wonokromo",
                codLocation = "Area Taman Bungkul / Raya Darmo",
                description = "Sepeda lipat jarang dipakai sejak kerja luar kota. Operan gigi Shimano 7 speed lancar jaya, ban masih tebal, lipatan kencang tidak oblak. Siap cek sepuasnya pas COD Minggu pagi.",
                whatsappNumber = "6281234509811",
                isSold = false,
                commentCount = 2,
                createdAt = now - 1000L * 60 * 35,
                updatedAt = now - 1000L * 60 * 35,
            ),
            BarkasItem(
                id = "brk_kulkas_gbg1",
                sellerId = "${seedAuthorId}_nadia",
                sellerName = "Ning Nadia",
                title = "Kulkas 1 Pintu Sharp Hemat Listrik Eks Anak Kos",
                category = "PERABOT",
                itemCondition = "SEPERTI_BARU",
                price = 950000L,
                isNegotiable = true,
                district = "Gubeng",
                codLocation = "Dekat Kampus B Unair Dharmawangsa",
                description = "Dijual karena baru lulus kuliah dan mau pindah pulang kampung. Freezer dingin cepat beku, karet pintu rapat, pemakaian pribadi 1 tahun bersih terawat.",
                whatsappNumber = "6285730112233",
                isSold = false,
                commentCount = 1,
                createdAt = now - 1000L * 60 * 95,
                updatedAt = now - 1000L * 60 * 95,
            ),
            BarkasItem(
                id = "brk_helm_kyt001",
                sellerId = "${seedAuthorId}_dimas",
                sellerName = "Cak Dimas",
                title = "Helm Fullface KYT TT Course Size L + Visor Iridium",
                category = "KENDARAAN",
                itemCondition = "BEKAS_TERAWAT",
                price = 780000L,
                isNegotiable = false,
                district = "Rungkut",
                codLocation = "McD Rungkut Industri / UPN Veteran Jatim",
                description = "Busa masih tebal wangi baru dicuci, visor bening bawaan pabrik masih disimpan lengkap beserta sarung & kardus. Lecet pemakaian wajar tipis.",
                whatsappNumber = "6281331889900",
                isSold = false,
                commentCount = 1,
                createdAt = now - 1000L * 60 * 180,
                updatedAt = now - 1000L * 60 * 180,
            ),
            BarkasItem(
                id = "brk_meja_skl001",
                sellerId = "${seedAuthorId}_ratna",
                sellerName = "Ning Ratna",
                title = "Meja Kerja & Rak Buku Kayu Jati Belanda Minimalis",
                category = "PERABOT",
                itemCondition = "LAYAK_PAKAI",
                price = 320000L,
                isNegotiable = true,
                district = "Sukolilo",
                codLocation = "Bundaran ITS / Jl. Arif Rahman Hakim",
                description = "Ukuran 120x60 cm kokoh untuk laptop + monitor. Dijual murah karena ganti meja lebih besar, ambil sendiri pakai motor/pickup.",
                whatsappNumber = "6282233445566",
                isSold = false,
                commentCount = 0,
                createdAt = now - 1000L * 60 * 300,
                updatedAt = now - 1000L * 60 * 300,
            ),
            BarkasItem(
                id = "brk_mon_sold001",
                sellerId = "${seedAuthorId}_bagus",
                sellerName = "Cak Bagus",
                title = "Monitor LG 24 Inch IPS 75Hz Full HD HDMI Lengkap",
                category = "ELEKTRONIK",
                itemCondition = "SEPERTI_BARU",
                price = 1050000L,
                isNegotiable = false,
                district = "Tegalsari",
                codLocation = "TP / Jl. Basuki Rahmat",
                description = "Layar jernih bebas dead pixel, kabel HDMI & adaptor ori lengkap. Alhamdulillah sudah laku COD tadi sore sama dulur Tegalsari.",
                whatsappNumber = "",
                isSold = true,
                commentCount = 2,
                createdAt = now - 1000L * 60 * 520,
                updatedAt = now - 1000L * 60 * 60,
            ),
        )
    }

    fun starterComments(): Map<String, List<BarkasComment>> {
        val now = System.currentTimeMillis()
        return mapOf(
            "brk_polygon_sby" to listOf(
                BarkasComment(
                    id = "bcom_seed_00001",
                    barkasId = "brk_polygon_sby",
                    authorId = "citizen_reza",
                    authorName = "Cak Reza",
                    authorDistrict = "Sawahan",
                    text = "Cak, nego 1,9 juta angkut besok pagi di Taman Bungkul boleh ta?",
                    createdAt = now - 1000L * 60 * 25,
                ),
                BarkasComment(
                    id = "bcom_seed_00002",
                    barkasId = "brk_polygon_sby",
                    authorId = "system_seed_hendra",
                    authorName = "Cak Hendra",
                    authorDistrict = "Wonokromo",
                    text = "Naik dikit 2 juta pas yo Cak Reza, bonus lampu belakang LED & tempat botol minum!",
                    createdAt = now - 1000L * 60 * 18,
                ),
            ),
            "brk_kulkas_gbg1" to listOf(
                BarkasComment(
                    id = "bcom_seed_00003",
                    barkasId = "brk_kulkas_gbg1",
                    authorId = "citizen_sinta",
                    authorName = "Ning Sinta",
                    authorDistrict = "Mulyorejo",
                    text = "Watt listriknya berapa mbak? Kosku meteran 900 VA kuat nggak ya?",
                    createdAt = now - 1000L * 60 * 50,
                ),
            ),
            "brk_helm_kyt001" to listOf(
                BarkasComment(
                    id = "bcom_seed_00004",
                    barkasId = "brk_helm_kyt001",
                    authorId = "citizen_farhan",
                    authorName = "Cak Farhan",
                    authorDistrict = "Gunung Anyar",
                    text = "Tali penguncinya sudah DD Ring atau microlock cak?",
                    createdAt = now - 1000L * 60 * 110,
                ),
            ),
            "brk_mon_sold001" to listOf(
                BarkasComment(
                    id = "bcom_seed_00005",
                    barkasId = "brk_mon_sold001",
                    authorId = "citizen_yoga",
                    authorName = "Cak Yoga",
                    authorDistrict = "Genteng",
                    text = "Masih ada mas monitornya? Siap COD sore ini.",
                    createdAt = now - 1000L * 60 * 120,
                ),
                BarkasComment(
                    id = "bcom_seed_00006",
                    barkasId = "brk_mon_sold001",
                    authorId = "system_seed_bagus",
                    authorName = "Cak Bagus",
                    authorDistrict = "Tegalsari",
                    text = "Matur nuwun rek, barusan sudah terjual COD sama Cak Yoga.",
                    createdAt = now - 1000L * 60 * 60,
                ),
            ),
        )
    }
}
