package com.example.data.model

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

fun normalizeCitizenGender(raw: String): String {
    return when (raw.trim().uppercase()) {
        "PEREMPUAN", "WANITA", "FEMALE", "P", "CEWEK", "NING" -> "PEREMPUAN"
        "LAKI_LAKI", "LAKI-LAKI", "LAKI LAKI", "PRIA", "MALE", "L", "COWOK", "CAK" -> "LAKI_LAKI"
        else -> ""
    }
}

fun formatCitizenGenderLabel(raw: String): String {
    return when (normalizeCitizenGender(raw)) {
        "PEREMPUAN" -> "Perempuan"
        "LAKI_LAKI" -> "Laki-laki"
        else -> ""
    }
}

data class PbUserSession(
    val token: String,
    val userId: String,
    val email: String,
    val displayName: String,
    val district: String = "Genteng",
    val gender: String = "",
    val isAdmin: Boolean = false,
)

fun isValidCitizenDisplayName(raw: String): Boolean {
    val trimmed = raw.trim()
    if (trimmed.length < 2) return false
    val normalized = trimmed.lowercase()
    return normalized != "warga surabaya" &&
        normalized != "warga" &&
        normalized != "pengguna" &&
        normalized != "anonymous" &&
        normalized != "anonim"
}

data class UserAccount(
    val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val district: String = "Genteng",
    val gender: String = "",
    val bio: String = "",
    val searchRadiusKm: Double = 10.0,
    val isDiscoverable: Boolean = true,
    val isAdmin: Boolean = false,
    val avatarUrl: String = "",
    val blockedUids: List<String> = emptyList(),
    val isBanned: Boolean = false,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val rawName: String = displayName,
) {
    val normalizedGender: String
        get() = normalizeCitizenGender(gender)

    val genderLabel: String
        get() = formatCitizenGenderLabel(gender)
}

data class BookmarkItem(
    val id: String = "",
    val userId: String = "",
    val targetId: String = "",
    val targetType: String = "KULINER",
    val title: String = "",
    val subtitle: String = "",
    val district: String = "Genteng",
    val createdAt: Long? = null,
)

data class NearbyProfile(
    val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val gender: String = "",
    val avatarColorHex: String = "#006B5E",
    val avatarUrl: String = "",
    val bio: String = "",
    val district: String = "Genteng",
    val interests: String = "Kuliner, Ngopi, Keliling Kota",
    val lat: Double = -7.2575,
    val lng: Double = 112.7521,
    val isDiscoverable: Boolean = true,
    val isBanned: Boolean = false,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
) {
    val normalizedGender: String
        get() = normalizeCitizenGender(gender)

    val genderLabel: String
        get() = formatCitizenGenderLabel(gender)
}

fun effectiveCitizenAvatarUrl(account: UserAccount?, nearby: NearbyProfile?): String {
    return account?.avatarUrl?.takeIf { it.isNotBlank() }
        ?: nearby?.avatarUrl?.takeIf { it.isNotBlank() }
        ?: ""
}

fun effectiveCitizenDisplayName(account: UserAccount?, nearby: NearbyProfile?): String {
    val accRawName = account?.rawName?.trim().orEmpty()
    if (isValidCitizenDisplayName(accRawName)) return accRawName
    val nearbyName = nearby?.displayName?.trim().orEmpty()
    if (isValidCitizenDisplayName(nearbyName)) return nearbyName
    return ""
}

fun effectiveCitizenGender(account: UserAccount?, nearby: NearbyProfile?): String {
    val accGender = normalizeCitizenGender(account?.gender.orEmpty())
    if (accGender.isNotBlank()) return accGender
    return normalizeCitizenGender(nearby?.gender.orEmpty())
}

fun isCitizenProfileComplete(account: UserAccount?, nearby: NearbyProfile?): Boolean {
    val hasAvatar = effectiveCitizenAvatarUrl(account, nearby).isNotBlank()
    val hasValidName = isValidCitizenDisplayName(effectiveCitizenDisplayName(account, nearby))
    val gender = effectiveCitizenGender(account, nearby)
    val hasValidGender = gender == "LAKI_LAKI" || gender == "PEREMPUAN"
    return hasAvatar && hasValidName && hasValidGender
}

data class IncidentReport(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "INFO_WARGA",
    val severity: String = "INFO",
    val district: String = "Genteng",
    val address: String = "",
    val lat: Double = -7.2575,
    val lng: Double = 112.7521,
    val imageKey: String = "kejadian_kota",
    val photoUrl: String = "",
    val photoUrls: List<String> = emptyList(),
    val mapsUrl: String = "",
    val linkReportCount: Int = 0,
    val linkReportedByUids: List<String> = emptyList(),
    val contentReportCount: Int = 0,
    val contentReportedByUids: List<String> = emptyList(),
    val verifiedCount: Int = 0,
    val verifiedByUids: List<String> = emptyList(),
    val commentCount: Int = 0,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
) {
    val allPhotos: List<String>
        get() = when {
            photoUrls.isNotEmpty() -> photoUrls.filter { it.isNotBlank() }
            photoUrl.isNotBlank() -> listOf(photoUrl)
            else -> emptyList()
        }

    val isLinkQuarantined: Boolean
        get() = linkReportCount >= 3

    val safeMapsUrl: String
        get() = if (isLinkQuarantined) "" else mapsUrl

    val isContentBlurred: Boolean
        get() = contentReportCount >= 5

    val isContentQuarantined: Boolean
        get() = contentReportCount >= 10
}

data class IncidentComment(
    val id: String = "",
    val incidentId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val text: String = "",
    val createdAt: Long? = null,
)

data class EventComment(
    val id: String = "",
    val eventId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val text: String = "",
    val createdAt: Long? = null,
)

data class SurabayaEvent(
    val id: String = "",
    val proposerId: String = "",
    val proposerName: String = "",
    val title: String = "",
    val category: String = "FESTIVAL",
    val eventDateText: String = "",
    val timeText: String = "",
    val venueName: String = "",
    val district: String = "Genteng",
    val address: String = "",
    val lat: Double = -7.2575,
    val lng: Double = 112.7521,
    val priceLabel: String = "Gratis",
    val description: String = "",
    val imageKey: String = "event_festival",
    val photoUrl: String = "",
    val photoUrls: List<String> = emptyList(),
    val mapsUrl: String = "",
    val linkReportCount: Int = 0,
    val linkReportedByUids: List<String> = emptyList(),
    val contentReportCount: Int = 0,
    val contentReportedByUids: List<String> = emptyList(),
    val status: String = "PENDING",
    val adminNote: String = "",
    val interestedCount: Int = 0,
    val interestedUids: List<String> = emptyList(),
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
) {
    val allPhotos: List<String>
        get() = when {
            photoUrls.isNotEmpty() -> photoUrls.filter { it.isNotBlank() }
            photoUrl.isNotBlank() -> listOf(photoUrl)
            else -> emptyList()
        }

    val isLinkQuarantined: Boolean
        get() = linkReportCount >= 3

    val safeMapsUrl: String
        get() = if (isLinkQuarantined) "" else mapsUrl

    val isContentBlurred: Boolean
        get() = contentReportCount >= 5

    val isContentQuarantined: Boolean
        get() = contentReportCount >= 10
}

data class PbOAuth2Provider(
    val name: String,
    val displayName: String,
    val state: String,
    val codeVerifier: String,
    val codeChallenge: String,
    val authUrl: String,
)

data class CulinarySpot(
    val id: String = "",
    val createdBy: String = "",
    val authorName: String = "",
    val name: String = "",
    val category: String = "LEGENDARIS",
    val description: String = "",
    val district: String = "Genteng",
    val address: String = "",
    val priceRange: String = "Rp 20.000 - Rp 35.000",
    val openHour: Int = 8,
    val closeHour: Int = 22,
    val lat: Double = -7.2575,
    val lng: Double = 112.7521,
    val imageKey: String = "kuliner_rawon",
    val photoUrl: String = "",
    val photoUrls: List<String> = emptyList(),
    val mapsUrl: String = "",
    val gofoodUrl: String = "",
    val grabfoodUrl: String = "",
    val shopeefoodUrl: String = "",
    val linkReportCount: Int = 0,
    val linkReportedByUids: List<String> = emptyList(),
    val contentReportCount: Int = 0,
    val contentReportedByUids: List<String> = emptyList(),
    val ratingAvg: Double = 4.8,
    val reviewCount: Int = 1,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
) {
    val allPhotos: List<String>
        get() = when {
            photoUrls.isNotEmpty() -> photoUrls.filter { it.isNotBlank() }
            photoUrl.isNotBlank() -> listOf(photoUrl)
            else -> emptyList()
        }

    val isLinkQuarantined: Boolean
        get() = linkReportCount >= 3

    val hasAnyExternalLink: Boolean
        get() = mapsUrl.isNotBlank() || gofoodUrl.isNotBlank() || grabfoodUrl.isNotBlank() || shopeefoodUrl.isNotBlank()

    val safeMapsUrl: String
        get() = if (isLinkQuarantined) "" else mapsUrl

    val safeGofoodUrl: String
        get() = if (isLinkQuarantined) "" else gofoodUrl

    val safeGrabfoodUrl: String
        get() = if (isLinkQuarantined) "" else grabfoodUrl

    val safeShopeefoodUrl: String
        get() = if (isLinkQuarantined) "" else shopeefoodUrl

    val isContentBlurred: Boolean
        get() = contentReportCount >= 5

    val isContentQuarantined: Boolean
        get() = contentReportCount >= 10
}

data class CulinaryReview(
    val id: String = "",
    val culinaryId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val createdAt: Long? = null,
)

enum class MessageDeliveryStatus {
    SENT,       // Centang 1 abu-abu
    DELIVERED,  // Centang 2 abu-abu
    READ,       // Centang 2 toska/biru terang
}

data class ChatRoom(
    val id: String = "",
    val roomKey: String = "",
    val roomType: String = "PUBLIC",
    val title: String = "",
    val subtitle: String = "",
    val participantUids: List<String> = emptyList(),
    val participantNames: String = "",
    val lastMessage: String = "",
    val lastSenderName: String = "",
    val typingStatus: String = "",
    val createdBy: String = "",
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
) {
    val isOfficialAccount: Boolean
        get() = title.contains("Arek Surabaya", ignoreCase = true) ||
            participantNames.contains("Arek Surabaya", ignoreCase = true) ||
            participantUids.contains("arek_surabaya_official")

    fun displayTitleFor(currentUserId: String): String {
        if (roomType != "DIRECT") return title
        if (participantNames.contains("|")) {
            val parts = participantNames.split("|").map { it.trim() }
            if (participantUids.size == 2 && parts.size == 2) {
                return when (currentUserId) {
                    participantUids[0] -> parts[1].ifBlank { title }
                    participantUids[1] -> parts[0].ifBlank { title }
                    else -> title
                }
            }
        }
        return title
    }

    fun otherParticipantIdFor(currentUserId: String): String? {
        if (roomType != "DIRECT") return null
        return participantUids.firstOrNull { it.isNotBlank() && it != currentUserId }
    }

    fun activeTypingPeerName(currentUserId: String, timeoutMs: Long = 4000L): String? {
        if (typingStatus.isBlank()) return null
        val parts = typingStatus.split(":")
        if (parts.size >= 3) {
            val uid = parts[0]
            val name = parts[1]
            val timestamp = parts[2].toLongOrNull() ?: 0L
            if (uid != currentUserId && (System.currentTimeMillis() - timestamp < timeoutMs)) {
                return name.ifBlank { "Lawan bicara" }
            }
        }
        return null
    }
}

data class ChatMessage(
    val id: String = "",
    val roomId: String = "",
    val roomKey: String = "",
    val roomType: String = "PUBLIC",
    val participantUids: List<String> = emptyList(),
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val photoUrl: String = "",
    val isDelivered: Boolean = true,
    val isRead: Boolean = false,
    val createdAt: Long? = null,
    val hasPhotoAttachment: Boolean = false,
) {
    val hasPhoto: Boolean
        get() = photoUrl.isNotBlank() || hasPhotoAttachment || text.contains("📷 Foto")

    val deliveryStatus: MessageDeliveryStatus
        get() = when {
            isRead -> MessageDeliveryStatus.READ
            isDelivered -> MessageDeliveryStatus.DELIVERED
            else -> MessageDeliveryStatus.SENT
        }
}

const val POST_TYPE_MOMEN = "MOMEN"
const val POST_TYPE_LENSA_WARGA = "LENSA_WARGA"

data class UserMoment(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorDistrict: String = "Genteng",
    val caption: String = "",
    val locationName: String = "",
    val district: String = "Genteng",
    val postType: String = POST_TYPE_MOMEN,
    val imageKey: String = "hero_surabaya",
    val photoUrl: String = "",
    val photoUrls: List<String> = emptyList(),
    val likesCount: Int = 0,
    val likedByUids: List<String> = emptyList(),
    val commentCount: Int = 0,
    val contentReportCount: Int = 0,
    val contentReportedByUids: List<String> = emptyList(),
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
) {
    val isLensaWarga: Boolean
        get() = postType == POST_TYPE_LENSA_WARGA

    val allPhotos: List<String>
        get() = when {
            photoUrls.isNotEmpty() -> photoUrls.filter { it.isNotBlank() }
            photoUrl.isNotBlank() -> listOf(photoUrl)
            else -> emptyList()
        }

    val isContentBlurred: Boolean
        get() = contentReportCount >= 5

    val isContentQuarantined: Boolean
        get() = contentReportCount >= 10

    val imageResName: String
        get() = imageKey

    val timestamp: Long?
        get() = createdAt
}

data class MomentComment(
    val id: String = "",
    val momentId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val text: String = "",
    val createdAt: Long? = null,
) {
    val comment: String
        get() = text

    val timestamp: Long?
        get() = createdAt
}

data class SurabayaDestination(
    val id: String = "",
    val createdBy: String = "",
    val authorName: String = "Admin Surabaya",
    val title: String = "",
    val category: String = "WISATA_SEJARAH",
    val description: String = "",
    val district: String = "Genteng",
    val address: String = "",
    val ticketPrice: String = "Gratis",
    val operationalHours: String = "08.00 - 22.00 WIB",
    val bestTime: String = "Sore hari",
    val lat: Double = -7.2575,
    val lng: Double = 112.7521,
    val imageKey: String = "hero_surabaya",
    val photoUrl: String = "",
    val photoUrls: List<String> = emptyList(),
    val mapsUrl: String = "",
    val ratingAvg: Double = 4.8,
    val reviewCount: Int = 0,
    val linkReportCount: Int = 0,
    val linkReportedByUids: List<String> = emptyList(),
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

    val imageResName: String
        get() = imageKey

    val rating: Double
        get() = ratingAvg

    val reviewsCount: Int
        get() = reviewCount

    val bestTimeToVisit: String
        get() = bestTime

    val isLinkQuarantined: Boolean
        get() = linkReportCount >= 3

    val safeMapsUrl: String
        get() = if (isLinkQuarantined) "" else mapsUrl

    val isContentBlurred: Boolean
        get() = contentReportCount >= 5

    val isContentQuarantined: Boolean
        get() = contentReportCount >= 10
}

data class DestinationReview(
    val id: String = "",
    val destinationId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val createdAt: Long? = null,
) {
    val review: String
        get() = comment

    val timestamp: Long?
        get() = createdAt
}

data class DestinationDraft(
    val title: String = "",
    val category: String = "Wisata Sejarah",
    val description: String = "",
    val address: String = "",
    val district: String = "Genteng",
    val ticketPrice: String = "Gratis",
    val operationalHours: String = "08.00 - 17.00 WIB",
    val bestTimeToVisit: String = "Pagi / Sore Hari",
    val mapsUrl: String = "",
    val photoUris: List<String> = emptyList(),
    val savedAt: Long = System.currentTimeMillis(),
) {
    fun hasContent(): Boolean =
        title.isNotBlank() || description.isNotBlank() || address.isNotBlank() || mapsUrl.isNotBlank() || photoUris.isNotEmpty()
}

data class EventDraft(
    val title: String = "",
    val category: String = "FESTIVAL",
    val dateText: String = "Minggu, 29 Nov 2026",
    val timeText: String = "18.30 - 21.30 WIB",
    val venueName: String = "",
    val district: String = "Genteng",
    val address: String = "",
    val priceLabel: String = "Gratis",
    val description: String = "",
    val mapsUrl: String = "",
    val photoUris: List<String> = emptyList(),
    val savedAt: Long = System.currentTimeMillis(),
) {
    fun hasContent(): Boolean =
        title.isNotBlank() || venueName.isNotBlank() || address.isNotBlank() ||
            description.isNotBlank() || mapsUrl.isNotBlank() || photoUris.isNotEmpty()
}

data class TambalBanDraft(
    val name: String = "",
    val description: String = "",
    val district: String = "Genteng",
    val address: String = "",
    val mapsUrl: String = "",
    val phoneNumber: String = "",
    val whatsappNumber: String = "",
    val is24Hours: Boolean = false,
    val isTubeless: Boolean = true,
    val forMotor: Boolean = true,
    val forMobil: Boolean = false,
    val canCallOut: Boolean = false,
    val photoUri: String = "",
    val savedAt: Long = System.currentTimeMillis(),
) {
    fun hasContent(): Boolean =
        name.isNotBlank() || description.isNotBlank() || address.isNotBlank() ||
            phoneNumber.isNotBlank() || whatsappNumber.isNotBlank() || mapsUrl.isNotBlank() ||
            photoUri.isNotBlank()
}

enum class AppNotificationType {
    CHAT,
    EVENT,
    INCIDENT,
    SYSTEM,
}

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val type: AppNotificationType = AppNotificationType.SYSTEM,
    val title: String = "",
    val message: String = "",
    val targetId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
)

data class CitizenService(
    val id: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val title: String = "",
    val category: String = "ELEKTRONIK_AC",
    val price: Long = 75000L,
    val priceUnit: String = "unit",
    val district: String = "Gubeng",
    val coverageArea: String = "Seluruh Surabaya & Sekitarnya",
    val phoneNumber: String = "",
    val whatsappNumber: String = "",
    val description: String = "",
    val flyerPhotoUrl: String = "",
    val flyerPhotoUrls: List<String> = emptyList(),
    val ratingAvg: Double = 4.9,
    val reviewCount: Int = 0,
    val verifiedBadge: Boolean = true,
    val contentReportCount: Int = 0,
    val contentReportedByUids: List<String> = emptyList(),
    val isBanned: Boolean = false,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
) {
    val allPhotos: List<String>
        get() = when {
            flyerPhotoUrls.isNotEmpty() -> flyerPhotoUrls.filter { it.isNotBlank() }
            flyerPhotoUrl.isNotBlank() -> listOf(flyerPhotoUrl)
            else -> emptyList()
        }

    val firstPhotoUrl: String
        get() = allPhotos.firstOrNull().orEmpty()

    val formattedPrice: String
        get() = "Rp %,d".format(java.util.Locale.GERMANY, price).replace(',', '.')

    val priceDisplay: String
        get() = if (price <= 0) "Tarif Negosiasi / Survei Gratis" else "$formattedPrice / $priceUnit"

    val isContentBlurred: Boolean
        get() = contentReportCount >= 5

    val isContentQuarantined: Boolean
        get() = contentReportCount >= 10
}

data class PollOption(
    val id: String = "",
    val text: String = "",
    val voteCount: Int = 0,
)

data class CitizenPoll(
    val id: String = "",
    val proposerId: String = "",
    val proposerName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "UMUM", // TRANSPORTASI, TATA_KOTA, KULINER_WISATA, LINGKUNGAN, LAYANAN_PUBLIK
    val district: String = "Genteng",
    val status: String = "APPROVED", // PENDING, APPROVED, REJECTED
    val adminNote: String = "",
    val options: List<PollOption> = emptyList(),
    val totalVotes: Int = 0,
    val voterUids: List<String> = emptyList(),
    val votesByUser: Map<String, String> = emptyMap(), // userId -> optionId
    val commentCount: Int = 0,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
) {
    fun userVotedOptionId(userId: String): String? {
        return votesByUser[userId]
    }

    fun hasUserVoted(userId: String): Boolean {
        return userId in voterUids || votesByUser.containsKey(userId)
    }

    fun percentageForOption(optionId: String): Float {
        if (totalVotes <= 0) return 0f
        val opt = options.firstOrNull { it.id == optionId } ?: return 0f
        return (opt.voteCount.toFloat() / totalVotes.toFloat()) * 100f
    }
}

data class PollComment(
    val id: String = "",
    val pollId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorDistrict: String = "Genteng",
    val text: String = "",
    val createdAt: Long? = null,
)

data class TambalBanSpot(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val name: String = "",
    val description: String = "",
    val district: String = "Genteng",
    val address: String = "",
    val latitude: Double = -7.2575,
    val longitude: Double = 112.7521,
    val is24Hours: Boolean = true,
    val isTubeless: Boolean = true,
    val forMotor: Boolean = true,
    val forMobil: Boolean = false,
    val canCallOut: Boolean = false, // Bisa dipanggil / keliling
    val photoUrl: String = "",
    val mapsUrl: String = "",
    val phoneNumber: String = "",
    val whatsappNumber: String = "",
    val verifiedCount: Int = 0,
    val verifiedUserIds: List<String> = emptyList(),
    val commentCount: Int = 0,
    val distanceKm: Double? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
) {
    fun isVerifiedBy(userId: String): Boolean = userId in verifiedUserIds

    val formattedDistance: String
        get() = distanceKm?.let { d ->
            if (d < 1.0) "${(d * 1000).toInt()} m" else "%.1f km".format(java.util.Locale.US, d)
        } ?: "Lokasi Surabaya"

    val vehicleSupportText: String
        get() = when {
            forMotor && forMobil -> "Motor & Mobil"
            forMobil -> "Khusus Mobil"
            else -> "Khusus Motor"
        }
}

data class TambalBanComment(
    val id: String = "",
    val spotId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorDistrict: String = "Genteng",
    val text: String = "",
    val createdAt: Long? = null,
)




