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
        get() = contentReportCount >= 1

    val isContentQuarantined: Boolean
        get() = contentReportCount >= 3

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
    fun starterListings(seedOwnerPrefix: String = "system_seed"): List<KosKontrakItem> {
        val now = System.currentTimeMillis()
        return listOf(
            KosKontrakItem(
                id = "kos_unair_gbg01",
                ownerId = "${seedOwnerPrefix}_bu_sri",
                ownerName = "Bu Sri Wahyuni",
                title = "Kos Putri Asri Dharmawangsa 5 Menit Jalan Kaki ke Unair B",
                propertyType = "KOS_PUTRI",
                billingPeriod = "BULANAN",
                price = 1150000L,
                availableRooms = 2,
                facilities = listOf("KM Dalam", "AC", "WiFi", "Kasur & Lemari", "Dapur Bersama", "Parkir Motor"),
                district = "Gubeng",
                address = "Jl. Dharmawangsa Dalam, dekat Kampus B Unair & RSUD Dr. Soetomo",
                mapsUrl = "https://maps.google.com/?q=-7.2738,112.7582",
                whatsappNumber = "6281231004455",
                description = "Lingkungan tenang, bersih, bebas banjir, cocok untuk mahasiswi atau karyawati. Sudah termasuk air PDAM & WiFi kencang 100 Mbps. Listrik token per kamar 1300 VA.",
                isFull = false,
                commentCount = 2,
                createdAt = now - 1000L * 60 * 40,
                updatedAt = now - 1000L * 60 * 40,
            ),
            KosKontrakItem(
                id = "kos_its_skl0002",
                ownerId = "${seedOwnerPrefix}_pak_bambang",
                ownerName = "Pak Bambang",
                title = "Kos Putra Gebang Putih Nyaman Dekat Bundaran ITS & Pakuwon City",
                propertyType = "KOS_PUTRA",
                billingPeriod = "BULANAN",
                price = 750000L,
                availableRooms = 3,
                facilities = listOf("Kipas Angin", "WiFi", "Kasur & Lemari", "Listrik Termasuk", "Parkir Motor", "Akses 24 Jam"),
                district = "Sukolilo",
                address = "Jl. Gebang Lor, 3 menit motor ke gerbang ITS Sukolilo",
                mapsUrl = "https://maps.google.com/?q=-7.2819,112.7915",
                whatsappNumber = "6285732998811",
                description = "Harga mahasiswa sudah termasuk listrik pemakaian wajar (laptop + kipas) dan air PDAM. Parkiran motor luas di dalam pagar tertutup, kunci gerbang pegang sendiri 24 jam.",
                isFull = false,
                commentCount = 1,
                createdAt = now - 1000L * 60 * 115,
                updatedAt = now - 1000L * 60 * 115,
            ),
            KosKontrakItem(
                id = "ktr_rungkut_003",
                ownerId = "${seedOwnerPrefix}_cak_taufik",
                ownerName = "Cak Taufik",
                title = "Kontrakan Rumah Minimalis 2 Kamar Tidur Carport Mobil Rungkut Asri",
                propertyType = "KONTRAKAN",
                billingPeriod = "TAHUNAN",
                price = 22500000L,
                availableRooms = 1,
                facilities = listOf("KM Dalam", "Parkir Mobil", "Parkir Motor", "Listrik Token", "Akses 24 Jam"),
                district = "Rungkut",
                address = "Perumahan Rungkut Asri Timur, dekat UPN Veteran & Kawasan SIER",
                mapsUrl = "https://maps.google.com/?q=-7.3305,112.7889",
                whatsappNumber = "6281330776622",
                description = "Rumah baru dicat ulang, 2 kamar tidur, 1 kamar mandi shower, ruang tamu lega, dapur wastafel keramik, dan carport berkanopi muat 1 mobil + 2 motor. Jalan depan lebar 2 mobil simpangan lancar.",
                isFull = false,
                commentCount = 1,
                createdAt = now - 1000L * 60 * 220,
                updatedAt = now - 1000L * 60 * 220,
            ),
            KosKontrakItem(
                id = "kos_wnk_pasutri",
                ownerId = "${seedOwnerPrefix}_bu_hj_lilis",
                ownerName = "Bu Hj. Lilis",
                title = "Kos Eksklusif Karyawan & Pasutri Resmi Dekat Royal Plaza Wonokromo",
                propertyType = "KOS_CAMPUR",
                billingPeriod = "BULANAN",
                price = 1450000L,
                availableRooms = 1,
                facilities = listOf("KM Dalam", "AC", "WiFi", "Kasur & Lemari", "Listrik Token", "Parkir Motor", "Parkir Mobil"),
                district = "Wonokromo",
                address = "Jl. Ketintang Baru, dekat Royal Plaza, RSI Wonokromo & Stasiun Wonokromo",
                mapsUrl = "",
                whatsappNumber = "6282140556677",
                description = "Khusus karyawan/karyawati atau pasutri sah (wajib tunjukkan buku nikah/KTP suami-istri saat daftar). Kamar luas 4x4 meter, springbed queen, bangunan baru bersih.",
                isFull = false,
                commentCount = 0,
                createdAt = now - 1000L * 60 * 340,
                updatedAt = now - 1000L * 60 * 340,
            ),
            KosKontrakItem(
                id = "kos_full_unesa1",
                ownerId = "${seedOwnerPrefix}_pak_eko",
                ownerName = "Pak Eko Santoso",
                title = "Kos Putra Ketintang Dekat Kampus Unesa & Telkom",
                propertyType = "KOS_PUTRA",
                billingPeriod = "BULANAN",
                price = 800000L,
                availableRooms = 0,
                facilities = listOf("KM Dalam", "WiFi", "Kasur & Lemari", "Parkir Motor"),
                district = "Gayungan",
                address = "Jl. Ketintang Madya, Gayungan",
                mapsUrl = "",
                whatsappNumber = "6281938001122",
                description = "Alhamdulillah sementara seluruh 8 kamar sudah terisi penuh semester ini. Nanti dibuka kembali di aplikasi bila ada kamar kosong.",
                isFull = true,
                commentCount = 1,
                createdAt = now - 1000L * 60 * 600,
                updatedAt = now - 1000L * 60 * 90,
            ),
        )
    }

    fun starterComments(): Map<String, List<KosComment>> {
        val now = System.currentTimeMillis()
        return mapOf(
            "kos_unair_gbg01" to listOf(
                KosComment(
                    id = "kcom_seed_00001",
                    kosId = "kos_unair_gbg01",
                    authorId = "citizen_amelia",
                    authorName = "Ning Amelia",
                    authorDistrict = "Sidoarjo",
                    text = "Bu Sri, kalau survei lihat kamar besok Sabtu jam 10 pagi bisa kah bu?",
                    createdAt = now - 1000L * 60 * 30,
                ),
                KosComment(
                    id = "kcom_seed_00002",
                    kosId = "kos_unair_gbg01",
                    authorId = "system_seed_bu_sri",
                    authorName = "Bu Sri Wahyuni",
                    authorDistrict = "Gubeng",
                    text = "Bisa nak Amelia, monggo langsung WA ibu dulu ya biar dibukakan pagarnya.",
                    createdAt = now - 1000L * 60 * 20,
                ),
            ),
            "kos_its_skl0002" to listOf(
                KosComment(
                    id = "kcom_seed_00003",
                    kosId = "kos_its_skl0002",
                    authorId = "citizen_rafi",
                    authorName = "Cak Rafi",
                    authorDistrict = "Sukolilo",
                    text = "Pak Bambang, kalau bawa PC komputer tambah biaya listrik berapa pak per bulan?",
                    createdAt = now - 1000L * 60 * 75,
                ),
            ),
            "ktr_rungkut_003" to listOf(
                KosComment(
                    id = "kcom_seed_00004",
                    kosId = "ktr_rungkut_003",
                    authorId = "citizen_danu",
                    authorName = "Cak Danu",
                    authorDistrict = "Waru",
                    text = "Airnya PDAM lancar 24 jam dan bebas banjir pas hujan deras cak?",
                    createdAt = now - 1000L * 60 * 130,
                ),
            ),
            "kos_full_unesa1" to listOf(
                KosComment(
                    id = "kcom_seed_00005",
                    kosId = "kos_full_unesa1",
                    authorId = "system_seed_pak_eko",
                    authorName = "Pak Eko Santoso",
                    authorDistrict = "Gayungan",
                    text = "Matur nuwun dulur-dulur, kamar terakhir baru saja terisi siang ini.",
                    createdAt = now - 1000L * 60 * 90,
                ),
            ),
        )
    }
}
