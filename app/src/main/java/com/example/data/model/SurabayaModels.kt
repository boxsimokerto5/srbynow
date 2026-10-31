package com.example.data.model

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class PbUserSession(
    val token: String,
    val userId: String,
    val email: String,
    val displayName: String,
    val district: String = "Genteng",
    val isAdmin: Boolean = false,
)

data class UserAccount(
    val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val district: String = "Genteng",
    val bio: String = "",
    val searchRadiusKm: Double = 10.0,
    val isDiscoverable: Boolean = true,
    val isAdmin: Boolean = false,
    val avatarUrl: String = "",
    val blockedUids: List<String> = emptyList(),
    val isBanned: Boolean = false,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
)

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
)

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
        get() = contentReportCount >= 1

    val isContentQuarantined: Boolean
        get() = contentReportCount >= 3
}

data class IncidentComment(
    val id: String = "",
    val incidentId: String = "",
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
        get() = contentReportCount >= 1

    val isContentQuarantined: Boolean
        get() = contentReportCount >= 3
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
        get() = contentReportCount >= 1

    val isContentQuarantined: Boolean
        get() = contentReportCount >= 3
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
    val createdBy: String = "",
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
) {
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
    val createdAt: Long? = null,
)

data class UserMoment(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorDistrict: String = "Genteng",
    val caption: String = "",
    val locationName: String = "",
    val district: String = "Genteng",
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
    val allPhotos: List<String>
        get() = when {
            photoUrls.isNotEmpty() -> photoUrls.filter { it.isNotBlank() }
            photoUrl.isNotBlank() -> listOf(photoUrl)
            else -> emptyList()
        }

    val isContentBlurred: Boolean
        get() = contentReportCount >= 1

    val isContentQuarantined: Boolean
        get() = contentReportCount >= 3

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
        get() = contentReportCount >= 1

    val isContentQuarantined: Boolean
        get() = contentReportCount >= 3
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
        get() = contentReportCount >= 1

    val isContentQuarantined: Boolean
        get() = contentReportCount >= 3
}


