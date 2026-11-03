package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.audio.RadioPlaybackState
import com.example.data.audio.RadioStationInfo
import com.example.data.audio.SurabayaRadioPlayer
import com.example.data.location.LocationHelper
import com.example.data.model.AppNotification
import com.example.data.model.AppNotificationType
import com.example.data.model.BarkasComment
import com.example.data.model.BarkasItem
import com.example.data.model.BookmarkItem
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRoom
import com.example.data.model.CitizenPoll
import com.example.data.model.CitizenService
import com.example.data.model.CulinaryReview
import com.example.data.model.CulinarySpot
import com.example.data.model.DestinationDraft
import com.example.data.model.DestinationReview
import com.example.data.model.EventComment
import com.example.data.model.EventDraft
import com.example.data.model.IncidentComment
import com.example.data.model.IncidentReport
import com.example.data.model.KosComment
import com.example.data.model.KosKontrakItem
import com.example.data.model.MomentComment
import com.example.data.model.NearbyProfile
import com.example.data.model.PollComment
import com.example.data.model.PollOption
import com.example.data.model.SurabayaDestination
import com.example.data.model.SurabayaEvent
import com.example.data.model.TambalBanComment
import com.example.data.model.TambalBanDraft
import com.example.data.model.TambalBanSpot
import com.example.data.model.UiState
import com.example.data.model.UserAccount
import com.example.data.model.UserMoment
import com.example.data.model.POST_TYPE_MOMEN
import com.example.data.model.POST_TYPE_LENSA_WARGA
import com.example.data.model.effectiveCitizenAvatarUrl
import com.example.data.model.isValidCitizenDisplayName
import com.example.data.model.normalizeCitizenGender
import com.example.data.repository.SurabayaRepository
import com.example.ui.components.HomeShortcutIds
import com.example.data.util.PollImageExporter
import com.example.data.util.GeminiExplicitImageScanner
import com.example.data.util.ImageSafetyScanResult
import com.example.data.util.ChatMediaManager
import com.example.data.util.DestinationDraftManager
import com.example.data.util.EventDraftManager
import com.example.data.util.TambalBanDraftManager
import com.example.data.util.ImageSafetyScanner
import com.example.data.util.ImageUploadHelper
import com.example.data.util.PreparedPhoto
import com.example.data.util.DuplicateContentException
import com.example.data.util.SpamCheckResult
import com.example.data.util.SpamRateLimitException
import com.example.data.util.SurabayaDistricts
import com.example.data.util.CommunityGroupPinHelper
import com.example.data.util.SurabayaNotificationHelper
import com.example.data.util.SurabayaSpamProtector
import com.example.data.util.calculateDistanceKm
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    HOME,
    MOMEN,
    KEJADIAN,
    DESTINASI,
    CHAT,
    EVENT,
    KULINER,
    SEKITAR,
    JASA,
    POLLING,
    TAMBAL_BAN,
    TRANSIT,
    BARKAS,
    KOS_KONTRAK,
    ADMIN_DASHBOARD,
}

sealed interface ActiveBottomSheet {
    data class IncidentDetail(val incidentId: String) : ActiveBottomSheet
    data object CreateIncident : ActiveBottomSheet
    data class EventDetail(val eventId: String) : ActiveBottomSheet
    data object ProposeEvent : ActiveBottomSheet
    data class EditEvent(val event: SurabayaEvent) : ActiveBottomSheet
    data class CulinaryDetail(val culinaryId: String) : ActiveBottomSheet
    data object CreateCulinary : ActiveBottomSheet
    data class MomentDetail(val momentId: String) : ActiveBottomSheet
    data class CreateMoment(val initialPostType: String = POST_TYPE_MOMEN) : ActiveBottomSheet
    data class DestinationDetail(val destinationId: String) : ActiveBottomSheet
    data object CreateDestination : ActiveBottomSheet
    data class EditDestination(val destination: SurabayaDestination) : ActiveBottomSheet
    data object GpsPrivacySettings : ActiveBottomSheet
    data object BookmarksList : ActiveBottomSheet
    data object Notifications : ActiveBottomSheet
    data object ProfileAndAdmin : ActiveBottomSheet
    data class CitizenProfile(
        val userId: String,
        val initialDisplayName: String = "",
        val returnSheet: ActiveBottomSheet? = null,
    ) : ActiveBottomSheet
    data object StartDirectChat : ActiveBottomSheet
    data object CreatePublicGroup : ActiveBottomSheet
    data class ChatConversation(val room: ChatRoom) : ActiveBottomSheet
    data object RadioPlayer : ActiveBottomSheet
    data class ServiceDetail(val serviceId: String) : ActiveBottomSheet
    data object CreateService : ActiveBottomSheet
    data class PollDetail(val pollId: String) : ActiveBottomSheet
    data object CreatePoll : ActiveBottomSheet
    data object AdminPendingPolls : ActiveBottomSheet
    data class TambalBanDetail(val spotId: String) : ActiveBottomSheet
    data object CreateTambalBan : ActiveBottomSheet
    data class EditTambalBan(val spot: TambalBanSpot) : ActiveBottomSheet
    data class BarkasDetail(val barkasId: String) : ActiveBottomSheet
    data object CreateBarkas : ActiveBottomSheet
    data class KosDetail(val kosId: String) : ActiveBottomSheet
    data object CreateKos : ActiveBottomSheet
}

data class NearbyPeerItem(
    val profile: NearbyProfile,
    val distanceKm: Double,
)

@OptIn(ExperimentalCoroutinesApi::class)
class SurabayaViewModel(
    private val repository: SurabayaRepository,
    private val locationHelper: LocationHelper,
    val currentUserId: String,
    private val initialDisplayName: String,
    val currentUserEmail: String,
    private val imageSafetyScanner: ImageSafetyScanner = GeminiExplicitImageScanner(),
    private val radioPlayer: SurabayaRadioPlayer? = null,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(MainTab.HOME)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _chatSubTab = MutableStateFlow(0)
    val chatSubTab: StateFlow<Int> = _chatSubTab.asStateFlow()

    private val _activeSheet = MutableStateFlow<ActiveBottomSheet?>(null)
    val activeSheet: StateFlow<ActiveBottomSheet?> = _activeSheet.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _userLat = MutableStateFlow(SurabayaDistricts.DEFAULT_LAT)
    val userLat: StateFlow<Double> = _userLat.asStateFlow()

    private val _userLng = MutableStateFlow(SurabayaDistricts.DEFAULT_LNG)
    val userLng: StateFlow<Double> = _userLng.asStateFlow()

    private val _isLiveGpsActive = MutableStateFlow(false)
    val isLiveGpsActive: StateFlow<Boolean> = _isLiveGpsActive.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _isScanningPhoto = MutableStateFlow(false)
    val isScanningPhoto: StateFlow<Boolean> = _isScanningPhoto.asStateFlow()

    private val _isSendingChatMessage = MutableStateFlow(false)
    val isSendingChatMessage: StateFlow<Boolean> = _isSendingChatMessage.asStateFlow()

    private val _unlockedGroupPinHashes = MutableStateFlow<Map<String, String>>(emptyMap())
    val unlockedGroupPinHashes: StateFlow<Map<String, String>> = _unlockedGroupPinHashes.asStateFlow()

    private val _pendingPinUnlockRoom = MutableStateFlow<ChatRoom?>(null)
    val pendingPinUnlockRoom: StateFlow<ChatRoom?> = _pendingPinUnlockRoom.asStateFlow()

    private val _cachedCitizenProfiles = MutableStateFlow<Map<String, NearbyProfile>>(emptyMap())
    val cachedCitizenProfiles: StateFlow<Map<String, NearbyProfile>> = _cachedCitizenProfiles.asStateFlow()

    private val activeInterestJobs = ConcurrentHashMap<String, Job>()
    private val _pendingEventInterests = MutableStateFlow<Set<String>>(emptySet())
    val pendingEventInterests: StateFlow<Set<String>> = _pendingEventInterests.asStateFlow()

    private val _hasLoadedUserAccount = MutableStateFlow(false)
    private val _hasLoadedMyNearby = MutableStateFlow(false)

    val isProfileCheckReady: StateFlow<Boolean> = combine(
        _hasLoadedUserAccount,
        _hasLoadedMyNearby,
    ) { accountLoaded, nearbyLoaded ->
        accountLoaded && nearbyLoaded
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), false)

    // User account & Admin streams
    val userAccount: StateFlow<UserAccount?> = repository.observeUserAccount(currentUserId)
        .onEach { _hasLoadedUserAccount.value = true }
        .catch { e ->
            Log.w(TAG, "observeUserAccount error", e)
            _hasLoadedUserAccount.value = true
            emit(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), null)

    val blockedUserIds: StateFlow<Set<String>> = userAccount
        .map { it?.blockedUids?.toSet().orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptySet())

    private val isBootstrappedEmailAdmin: Boolean =
        com.example.data.pocketbase.PocketBaseClient.isOfficialAdminEmail(currentUserEmail)

    val isAdmin: StateFlow<Boolean> = repository.observeAdminRole(currentUserId)
        .map { repoAdmin -> repoAdmin || isBootstrappedEmailAdmin }
        .catch { e ->
            Log.w(TAG, "observeAdminRole error", e)
            emit(isBootstrappedEmailAdmin)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), isBootstrappedEmailAdmin)

    val bannedProfiles: StateFlow<List<NearbyProfile>> = repository.observeBannedProfiles()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    val homeShortcutOrder: StateFlow<List<String>> = repository.observeHomeShortcutOrder()
        .catch { emit(HomeShortcutIds.DEFAULT_ORDER) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), HomeShortcutIds.DEFAULT_ORDER)

    val bookmarks: StateFlow<List<BookmarkItem>> = repository.observeBookmarks(currentUserId)
        .catch { e ->
            Log.w(TAG, "observeBookmarks error", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    val notifications: StateFlow<List<AppNotification>> = repository.observeNotifications(currentUserId)
        .catch { e ->
            Log.w(TAG, "observeNotifications error", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    val unreadNotificationCount: StateFlow<Int> = notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), 0)

    // 1. Kejadian Surabaya (filtered for blocked authors & quarantined posts)
    val incidentsState: StateFlow<UiState<List<IncidentReport>>> = combine(
        repository.observeIncidents(),
        blockedUserIds,
        isAdmin,
    ) { items, blockedSet, admin ->
        val filtered = items.filter { item ->
            item.authorId !in blockedSet && (!item.isContentQuarantined || admin)
        }
        UiState.Success(filtered) as UiState<List<IncidentReport>>
    }
        .catch { e ->
            Log.w(TAG, "observeIncidents error", e)
            emit(UiState.Error(e.localizedMessage ?: "Gagal memuat kejadian Surabaya"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    private val selectedIncidentIdFlow = _activeSheet.map { (it as? ActiveBottomSheet.IncidentDetail)?.incidentId }

    val activeIncidentComments: StateFlow<List<IncidentComment>> = combine(
        selectedIncidentIdFlow.flatMapLatest { incidentId ->
            if (incidentId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeIncidentComments(incidentId)
                    .catch { emit(emptyList()) }
            }
        },
        blockedUserIds,
    ) { comments, blockedSet ->
        comments.filter { it.authorId !in blockedSet }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    // 2. Event Surabaya & Moderation
    val approvedEventsState: StateFlow<UiState<List<SurabayaEvent>>> = combine(
        repository.observeApprovedEvents(),
        blockedUserIds,
        isAdmin,
    ) { items, blockedSet, admin ->
        val filtered = items.filter { item ->
            item.proposerId !in blockedSet && (!item.isContentQuarantined || admin)
        }
        UiState.Success(filtered) as UiState<List<SurabayaEvent>>
    }
        .catch { e ->
            Log.w(TAG, "observeApprovedEvents error", e)
            emit(UiState.Error(e.localizedMessage ?: "Gagal memuat agenda event"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    val pendingEvents: StateFlow<List<SurabayaEvent>> = combine(
        repository.observePendingEvents(),
        blockedUserIds,
    ) { items, blockedSet ->
        items.filter { it.proposerId !in blockedSet }
    }
        .catch { e ->
            Log.w(TAG, "observePendingEvents error", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    val myProposedEvents: StateFlow<List<SurabayaEvent>> = repository.observeMyProposedEvents(currentUserId)
        .catch { e ->
            Log.w(TAG, "observeMyProposedEvents error", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    private val selectedEventIdFlow = _activeSheet.map { (it as? ActiveBottomSheet.EventDetail)?.eventId }

    val activeEventComments: StateFlow<List<EventComment>> = combine(
        selectedEventIdFlow.flatMapLatest { eventId ->
            if (eventId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeEventComments(eventId)
                    .catch { emit(emptyList()) }
            }
        },
        blockedUserIds,
    ) { comments, blockedSet ->
        comments.filter { it.authorId !in blockedSet }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    // 3. Kuliner Surabaya (selalu diurutkan otomatis berdasarkan jarak GPS terdekat warga)
    val culinaryState: StateFlow<UiState<List<CulinarySpot>>> = combine(
        repository.observeCulinarySpots(),
        blockedUserIds,
        isAdmin,
        _userLat,
        _userLng,
    ) { items, blockedSet, admin, lat, lng ->
        val filteredAndSorted = items
            .filter { item ->
                item.createdBy !in blockedSet && (!item.isContentQuarantined || admin)
            }
            .sortedWith(
                compareBy<CulinarySpot> { calculateDistanceKm(lat, lng, it.lat, it.lng) }
                    .thenByDescending { it.ratingAvg }
            )
        UiState.Success(filteredAndSorted) as UiState<List<CulinarySpot>>
    }
        .catch { e ->
            Log.w(TAG, "observeCulinarySpots error", e)
            emit(UiState.Error(e.localizedMessage ?: "Gagal memuat daftar kuliner"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    private val selectedCulinaryIdFlow = _activeSheet.map { (it as? ActiveBottomSheet.CulinaryDetail)?.culinaryId }

    val activeCulinaryReviews: StateFlow<List<CulinaryReview>> = combine(
        selectedCulinaryIdFlow.flatMapLatest { culinaryId ->
            if (culinaryId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeCulinaryReviews(culinaryId)
                    .catch { emit(emptyList()) }
            }
        },
        blockedUserIds,
    ) { reviews, blockedSet ->
        reviews.filter { it.authorId !in blockedSet }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    // 4. Teman Sekitar (Nearby Peers sorted & filtered by radius & blocked list)
    val nearbyPeersState: StateFlow<UiState<List<NearbyPeerItem>>> = combine(
        repository.observeDiscoverableProfiles(),
        _userLat,
        _userLng,
        userAccount,
    ) { profiles, lat, lng, account ->
        val radiusKm = account?.searchRadiusKm ?: 15.0
        val blockedSet = account?.blockedUids?.toSet().orEmpty()
        val peers = profiles
            .filter {
                it.id != currentUserId &&
                    !it.isBanned &&
                    it.id !in blockedSet &&
                    it.userId !in blockedSet
            }
            .map { profile ->
                val dist = calculateDistanceKm(lat, lng, profile.lat, profile.lng)
                NearbyPeerItem(profile = profile, distanceKm = dist)
            }
            .filter { it.distanceKm <= radiusKm }
            .sortedBy { it.distanceKm }
        UiState.Success(peers) as UiState<List<NearbyPeerItem>>
    }
        .catch { e ->
            Log.w(TAG, "observeDiscoverableProfiles error", e)
            emit(UiState.Error(e.localizedMessage ?: "Gagal memuat teman sekitar"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    val myNearbyProfile: StateFlow<NearbyProfile?> = repository.observeMyNearbyProfile(currentUserId)
        .onEach { _hasLoadedMyNearby.value = true }
        .catch {
            _hasLoadedMyNearby.value = true
            emit(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), null)

    val allDiscoverableCitizens: StateFlow<List<NearbyProfile>> = combine(
        repository.observeDiscoverableProfiles(),
        blockedUserIds,
    ) { list, blockedSet ->
        list.filter {
            it.id != currentUserId &&
                !it.isBanned &&
                it.id !in blockedSet &&
                it.userId !in blockedSet
        }.sortedBy { it.displayName.lowercase() }
    }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    // 5. Chat Rooms & Messages
    val publicChatRooms: StateFlow<List<ChatRoom>> = repository.observePublicChatRooms()
        .catch { e ->
            Log.w(TAG, "observePublicChatRooms error", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    val myActivePublicGroupCount: StateFlow<Int> = publicChatRooms
        .map { rooms -> rooms.count { it.roomType == "PUBLIC" && it.createdBy == currentUserId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), 0)

    val directChatRooms: StateFlow<List<ChatRoom>> = combine(
        repository.observeDirectChatRooms(currentUserId),
        blockedUserIds,
    ) { rooms, blockedSet ->
        rooms.filter { room ->
            val otherId = room.otherParticipantIdFor(currentUserId)
            otherId == null || otherId !in blockedSet
        }.sortedWith(
            compareByDescending<ChatRoom> { it.isOfficialAccount }
                .thenByDescending { it.updatedAt ?: it.createdAt ?: 0L }
        )
    }
        .catch { e ->
            Log.w(TAG, "observeDirectChatRooms error", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    private val activeChatRoomFlow = _activeSheet.map { (it as? ActiveBottomSheet.ChatConversation)?.room }

    val activeChatMessages: StateFlow<List<ChatMessage>> = combine(
        activeChatRoomFlow.flatMapLatest { room ->
            if (room == null) {
                flowOf(emptyList())
            } else {
                repository.observeMessages(room, currentUserId)
                    .catch { emit(emptyList()) }
            }
        },
        blockedUserIds,
    ) { messages, blockedSet ->
        messages.filter { it.senderId !in blockedSet }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    // 5. Momen Arek (Moments feed)
    val momentsState: StateFlow<UiState<List<UserMoment>>> = combine(
        repository.observeMoments(),
        blockedUserIds,
        isAdmin,
    ) { items, blockedSet, admin ->
        val filtered = items.filter { item ->
            item.authorId !in blockedSet && (!item.isContentQuarantined || admin)
        }
        UiState.Success(filtered) as UiState<List<UserMoment>>
    }
        .catch { e ->
            Log.w(TAG, "observeMoments error", e)
            emit(UiState.Error(e.localizedMessage ?: "Gagal memuat Momen Arek"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    private val selectedMomentIdFlow = _activeSheet.map { (it as? ActiveBottomSheet.MomentDetail)?.momentId }

    val activeMomentComments: StateFlow<List<MomentComment>> = combine(
        selectedMomentIdFlow.flatMapLatest { momentId ->
            if (momentId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeMomentComments(momentId)
                    .catch { emit(emptyList()) }
            }
        },
        blockedUserIds,
    ) { comments, blockedSet ->
        comments.filter { it.authorId !in blockedSet }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    // 6. Destinasi Surabaya (Official destinations catalog)
    val destinationsState: StateFlow<UiState<List<SurabayaDestination>>> = combine(
        repository.observeDestinations(),
        blockedUserIds,
        isAdmin,
    ) { items, blockedSet, admin ->
        val filtered = items.filter { item ->
            item.createdBy !in blockedSet && (!item.isContentQuarantined || admin)
        }
        UiState.Success(filtered) as UiState<List<SurabayaDestination>>
    }
        .catch { e ->
            Log.w(TAG, "observeDestinations error", e)
            emit(UiState.Error(e.localizedMessage ?: "Gagal memuat destinasi Surabaya"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    private val selectedDestinationIdFlow = _activeSheet.map { (it as? ActiveBottomSheet.DestinationDetail)?.destinationId }

    val activeDestinationReviews: StateFlow<List<DestinationReview>> = combine(
        selectedDestinationIdFlow.flatMapLatest { destinationId ->
            if (destinationId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeDestinationReviews(destinationId)
                    .catch { emit(emptyList()) }
            }
        },
        blockedUserIds,
    ) { reviews, blockedSet ->
        reviews.filter { it.authorId !in blockedSet }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    // 7. Promosi Jasa Warga (Community Services Directory)
    val servicesState: StateFlow<UiState<List<CitizenService>>> = combine(
        repository.observeCitizenServices(),
        blockedUserIds,
        isAdmin,
    ) { items, blockedSet, admin ->
        val filtered = items.filter { item ->
            item.providerId !in blockedSet && (!item.isContentQuarantined || admin)
        }
        UiState.Success(filtered) as UiState<List<CitizenService>>
    }
        .catch { e ->
            Log.w(TAG, "observeCitizenServices error", e)
            emit(UiState.Error(e.localizedMessage ?: "Gagal memuat promosi jasa warga"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    // 8. Polling Warga & Suara Rakyat
    val approvedPollsState: StateFlow<UiState<List<CitizenPoll>>> = combine(
        repository.observeApprovedPolls(),
        blockedUserIds,
    ) { polls, blockedSet ->
        val filtered = polls.filter { it.proposerId !in blockedSet }
        UiState.Success(filtered) as UiState<List<CitizenPoll>>
    }
        .catch { e ->
            Log.w(TAG, "observeApprovedPolls error", e)
            emit(UiState.Error(e.localizedMessage ?: "Gagal memuat polling warga"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    val pendingPollsState: StateFlow<UiState<List<CitizenPoll>>> = repository.observePendingPolls()
        .map { list -> UiState.Success(list) as UiState<List<CitizenPoll>> }
        .catch { emit(UiState.Error(it.localizedMessage ?: "Gagal memuat usulan polling")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    val myProposedPollsState: StateFlow<List<CitizenPoll>> = repository.observeMyProposedPolls(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    private val selectedPollIdFlow = _activeSheet.map { (it as? ActiveBottomSheet.PollDetail)?.pollId }

    val activeSelectedPoll: StateFlow<CitizenPoll?> = selectedPollIdFlow.flatMapLatest { pollId ->
        if (pollId.isNullOrBlank()) {
            flowOf(null)
        } else {
            repository.observePollById(pollId)
                .catch { emit(null) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), null)

    val activePollComments: StateFlow<List<PollComment>> = combine(
        selectedPollIdFlow.flatMapLatest { pollId ->
            if (pollId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.observePollComments(pollId)
                    .catch { emit(emptyList()) }
            }
        },
        blockedUserIds,
    ) { comments, blockedSet ->
        comments.filter { it.authorId !in blockedSet }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    // --- TAMBAL BAN (SALAM SATU ASPAL) STATEFLOWS ---

    val tambalBanSpotsState: StateFlow<UiState<List<TambalBanSpot>>> = combine(
        repository.observeTambalBanSpots(),
        _userLat,
        _userLng,
    ) { spots, lat, lng ->
        val mapped = spots.map { spot ->
            val dist = calculateDistanceKm(lat, lng, spot.latitude, spot.longitude)
            spot.copy(distanceKm = dist)
        }.sortedBy { it.distanceKm ?: 999.0 }
        UiState.Success(mapped) as UiState<List<TambalBanSpot>>
    }.catch { emit(UiState.Error(it.localizedMessage ?: "Gagal memuat lokasi tambal ban")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    private val selectedTambalBanIdFlow = _activeSheet.map { (it as? ActiveBottomSheet.TambalBanDetail)?.spotId }

    val activeSelectedTambalBan: StateFlow<TambalBanSpot?> = combine(
        selectedTambalBanIdFlow.flatMapLatest { spotId ->
            if (spotId.isNullOrBlank()) {
                flowOf(null)
            } else {
                repository.observeTambalBanById(spotId).catch { emit(null) }
            }
        },
        _userLat,
        _userLng,
    ) { spot, lat, lng ->
        spot?.let {
            val dist = calculateDistanceKm(lat, lng, it.latitude, it.longitude)
            it.copy(distanceKm = dist)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), null)

    val activeTambalBanComments: StateFlow<List<TambalBanComment>> = combine(
        selectedTambalBanIdFlow.flatMapLatest { spotId ->
            if (spotId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeTambalBanComments(spotId).catch { emit(emptyList()) }
            }
        },
        blockedUserIds,
    ) { comments, blockedSet ->
        comments.filter { it.authorId !in blockedSet }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    // --- BARKAS WARGA (JUAL BELI BARANG BEKAS) STATEFLOWS ---

    val barkasItemsState: StateFlow<UiState<List<BarkasItem>>> = combine(
        repository.observeBarkasItems(),
        blockedUserIds,
        isAdmin,
    ) { items, blockedSet, admin ->
        val filtered = items.filter { item ->
            item.sellerId !in blockedSet && (!item.isContentQuarantined || admin)
        }
        UiState.Success(filtered) as UiState<List<BarkasItem>>
    }.catch { emit(UiState.Error(it.localizedMessage ?: "Gagal memuat daftar barang bekas")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    private val selectedBarkasIdFlow = _activeSheet.map { (it as? ActiveBottomSheet.BarkasDetail)?.barkasId }

    val activeSelectedBarkas: StateFlow<BarkasItem?> = selectedBarkasIdFlow.flatMapLatest { barkasId ->
        if (barkasId.isNullOrBlank()) {
            flowOf(null)
        } else {
            repository.observeBarkasById(barkasId).catch { emit(null) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), null)

    val activeBarkasComments: StateFlow<List<BarkasComment>> = combine(
        selectedBarkasIdFlow.flatMapLatest { barkasId ->
            if (barkasId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeBarkasComments(barkasId).catch { emit(emptyList()) }
            }
        },
        blockedUserIds,
    ) { comments, blockedSet ->
        comments.filter { it.authorId !in blockedSet }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    // --- INFO KOS & KONTRAK STATEFLOWS ---

    val kosListingsState: StateFlow<UiState<List<KosKontrakItem>>> = combine(
        repository.observeKosListings(),
        blockedUserIds,
        isAdmin,
    ) { items, blockedSet, admin ->
        val filtered = items.filter { item ->
            item.ownerId !in blockedSet && (!item.isContentQuarantined || admin)
        }
        UiState.Success(filtered) as UiState<List<KosKontrakItem>>
    }.catch { emit(UiState.Error(it.localizedMessage ?: "Gagal memuat daftar kos & kontrakan")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    private val selectedKosIdFlow = _activeSheet.map { (it as? ActiveBottomSheet.KosDetail)?.kosId }

    val activeSelectedKos: StateFlow<KosKontrakItem?> = selectedKosIdFlow.flatMapLatest { kosId ->
        if (kosId.isNullOrBlank()) {
            flowOf(null)
        } else {
            repository.observeKosById(kosId).catch { emit(null) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), null)

    val activeKosComments: StateFlow<List<KosComment>> = combine(
        selectedKosIdFlow.flatMapLatest { kosId ->
            if (kosId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeKosComments(kosId).catch { emit(emptyList()) }
            }
        },
        blockedUserIds,
    ) { comments, blockedSet ->
        comments.filter { it.authorId !in blockedSet }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    init {
        viewModelScope.launch {
            try {
                val gps = locationHelper.fetchCurrentLocationOrDefault()
                _userLat.value = gps.lat
                _userLng.value = gps.lng
                _isLiveGpsActive.value = gps.isLiveGps
                repository.bootstrapCurrentUserAndSeedIfEmpty(
                    displayNameFallback = initialDisplayName.ifBlank { "Warga Surabaya" },
                    lat = gps.lat,
                    lng = gps.lng,
                    district = gps.nearestDistrict,
                )
                // Lazy maintenance of chat media storage bridge: prune old delivered photos from server
                repository.pruneOldServerChatPhotos()
            } catch (e: Exception) {
                Log.w(TAG, "Initial bootstrap warning", e)
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    fun selectChatSubTab(index: Int) {
        _chatSubTab.value = index.coerceIn(0, 1)
    }

    fun openSheet(sheet: ActiveBottomSheet) {
        _activeSheet.value = sheet
    }

    fun dismissSheet() {
        _activeSheet.value = null
    }

    fun openCitizenProfile(
        userId: String,
        displayName: String = "",
        returnSheet: ActiveBottomSheet? = null,
    ) {
        val cleanUid = userId.trim()
        if (cleanUid.isBlank()) return
        _activeSheet.value = ActiveBottomSheet.CitizenProfile(cleanUid, displayName, returnSheet)
        viewModelScope.launch {
            val fetched = repository.fetchCitizenProfileById(cleanUid)
            if (fetched != null) {
                val updated = LinkedHashMap(_cachedCitizenProfiles.value)
                updated[cleanUid] = fetched
                if (fetched.id.isNotBlank()) updated[fetched.id] = fetched
                if (fetched.userId.isNotBlank()) updated[fetched.userId] = fetched
                _cachedCitizenProfiles.value = updated
            }
        }
    }

    // Radio Suara Surabaya 100 FM
    val radioPlaybackState: StateFlow<RadioPlaybackState> =
        radioPlayer?.playbackState ?: MutableStateFlow(RadioPlaybackState.STOPPED).asStateFlow()

    val radioVolume: StateFlow<Float> =
        radioPlayer?.volume ?: MutableStateFlow(0.85f).asStateFlow()

    val radioErrorMessage: StateFlow<String?> =
        radioPlayer?.errorMessage ?: MutableStateFlow<String?>(null).asStateFlow()

    val radioStationInfo: StateFlow<RadioStationInfo> =
        radioPlayer?.stationInfo ?: MutableStateFlow(RadioStationInfo()).asStateFlow()

    fun playRadio() {
        radioPlayer?.play()
    }

    fun pauseRadio() {
        radioPlayer?.pause()
    }

    fun toggleRadio() {
        radioPlayer?.toggle()
    }

    fun stopRadio() {
        radioPlayer?.stop()
    }

    fun setRadioVolume(volume: Float) {
        radioPlayer?.setVolume(volume)
    }

    fun openRadioPlayer(autoPlay: Boolean = false) {
        openSheet(ActiveBottomSheet.RadioPlayer)
        if (autoPlay && radioPlaybackState.value != RadioPlaybackState.PLAYING) {
            playRadio()
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    private fun notifyMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    private fun currentDisplayName(): String {
        return userAccount.value?.displayName?.takeIf { it.isNotBlank() }
            ?: initialDisplayName.ifBlank { "Warga Surabaya" }
    }

    fun refreshGpsLocation() {
        viewModelScope.launch {
            try {
                val gps = locationHelper.fetchCurrentLocationOrDefault()
                _userLat.value = gps.lat
                _userLng.value = gps.lng
                _isLiveGpsActive.value = gps.isLiveGps
                val acc = userAccount.value
                if (acc != null) {
                    repository.updateProfileAndPrivacy(
                        displayName = acc.displayName,
                        district = gps.nearestDistrict,
                        bio = acc.bio,
                        interests = myNearbyProfile.value?.interests ?: "Kuliner SBY, Kopi, Event Kota",
                        searchRadiusKm = acc.searchRadiusKm,
                        isDiscoverable = acc.isDiscoverable,
                        lat = gps.lat,
                        lng = gps.lng,
                        gender = acc.gender.ifBlank { myNearbyProfile.value?.gender.orEmpty() },
                    )
                }
                notifyMessage("Lokasi GPS diperbarui: Kec. ${gps.nearestDistrict}")
            } catch (e: Exception) {
                notifyMessage("Gagal memperbarui lokasi GPS")
            }
        }
    }

    private fun isCurrentUserFrozen(): Boolean {
        if (userAccount.value?.isBanned == true) {
            notifyMessage("Akun Anda dibekukan oleh Admin karena pelanggaran pedoman konten komunitas.")
            return true
        }
        return false
    }

    private suspend fun verifyPhotosAreSafe(photos: List<PreparedPhoto>): Boolean {
        if (photos.isEmpty()) return true
        _isScanningPhoto.value = true
        return try {
            when (val result = imageSafetyScanner.scanPhotoBatch(photos.map { it.bytes })) {
                ImageSafetyScanResult.Safe -> true
                is ImageSafetyScanResult.BlockedExplicit -> {
                    val idx = result.blockedPhotoIndex.coerceIn(0, photos.lastIndex)
                    val prefix = if (photos.size > 1) {
                        "Foto #${idx + 1} ditolak sistem proteksi AI: "
                    } else {
                        "Foto ditolak sistem proteksi AI: "
                    }
                    notifyMessage("$prefix${result.reasonId}")
                    false
                }
                is ImageSafetyScanResult.ScanError -> {
                    notifyMessage("Verifikasi keamanan foto AI gagal: ${result.message}")
                    false
                }
            }
        } finally {
            _isScanningPhoto.value = false
        }
    }

    fun completeMandatoryProfile(
        displayName: String,
        gender: String,
        district: String,
        avatarUri: Uri?,
        context: Context,
    ) {
        val trimmedName = displayName.trim()
        if (!isValidCitizenDisplayName(trimmedName)) {
            notifyMessage("Mohon isi nama tampilan asli atau panggilan Anda (minimal 2 huruf).")
            return
        }
        val normalizedGender = normalizeCitizenGender(gender)
        if (normalizedGender.isBlank()) {
            notifyMessage("Mohon pilih jenis kelamin (Laki-laki atau Perempuan).")
            return
        }
        val existingAvatar = effectiveCitizenAvatarUrl(userAccount.value, myNearbyProfile.value)
        if (existingAvatar.isBlank() && avatarUri == null) {
            notifyMessage("Wajib memilih foto profil terlebih dahulu sebelum masuk aplikasi.")
            return
        }
        val acc = userAccount.value
        val nearby = myNearbyProfile.value
        saveProfileAndPrivacy(
            displayName = trimmedName,
            district = district.ifBlank { acc?.district ?: "Genteng" },
            bio = acc?.bio?.takeIf { it.isNotBlank() }
                ?: nearby?.bio?.takeIf { it.isNotBlank() }
                ?: "Halo rek! Salam kenal dari ${district.ifBlank { "Surabaya" }}.",
            interests = nearby?.interests?.takeIf { it.isNotBlank() }
                ?: "Kuliner SBY, Kopi, Event Kota",
            searchRadiusKm = acc?.searchRadiusKm ?: 10.0,
            isDiscoverable = acc?.isDiscoverable ?: true,
            avatarUri = avatarUri,
            context = context,
            gender = normalizedGender,
        )
    }

    fun saveProfileAndPrivacy(
        displayName: String,
        district: String,
        bio: String,
        interests: String,
        searchRadiusKm: Double,
        isDiscoverable: Boolean,
        avatarUri: Uri? = null,
        context: Context? = null,
        gender: String = "",
    ) {
        if (_isSubmitting.value || isCurrentUserFrozen()) return
        val trimmedName = displayName.trim()
        if (!isValidCitizenDisplayName(trimmedName)) {
            notifyMessage("Nama tampilan wajib diisi minimal 2 huruf (bukan teks default).")
            return
        }
        val appCtx = context?.applicationContext
        val effectiveGender = normalizeCitizenGender(
            gender.ifBlank { userAccount.value?.gender ?: myNearbyProfile.value?.gender.orEmpty() },
        )
        if (effectiveGender.isBlank()) {
            notifyMessage("Jenis kelamin wajib dipilih (Laki-laki atau Perempuan).")
            return
        }
        val existingAvatar = effectiveCitizenAvatarUrl(userAccount.value, myNearbyProfile.value)
        if (existingAvatar.isBlank() && avatarUri == null) {
            notifyMessage("Foto profil wajib terpasang untuk menyimpan profil warga.")
            return
        }
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val preparedAvatar = if (avatarUri != null && appCtx != null) {
                    val compressed = ImageUploadHelper.preparePhotoFromUri(
                        context = appCtx,
                        uri = avatarUri,
                        prefix = "avatar",
                        maxDimensionPx = 640,
                        initialJpegQuality = 84,
                    )
                    if (compressed == null) {
                        notifyMessage("Foto profil tidak dapat diproses, silakan pilih gambar lain.")
                        return@launch
                    }
                    compressed
                } else {
                    null
                }

                if (preparedAvatar != null && !verifyPhotosAreSafe(listOf(preparedAvatar))) {
                    return@launch
                }

                val (districtLat, districtLng) = if (_isLiveGpsActive.value) {
                    _userLat.value to _userLng.value
                } else {
                    SurabayaDistricts.coordinateFor(district)
                }
                _userLat.value = districtLat
                _userLng.value = districtLng

                var photoSchemaMissing = false
                repository.updateProfileAndPrivacy(
                    displayName = trimmedName,
                    district = district,
                    bio = bio,
                    interests = interests,
                    searchRadiusKm = searchRadiusKm,
                    isDiscoverable = isDiscoverable,
                    lat = districtLat,
                    lng = districtLng,
                    gender = effectiveGender,
                    avatarPhoto = preparedAvatar,
                    onPhotoSchemaMissing = { photoSchemaMissing = true },
                )
                dismissSheet()
                if (preparedAvatar != null && photoSchemaMissing) {
                    notifyMessage("Profil disimpan, namun kolom avatar belum di-import di PocketBase Admin.")
                } else if (preparedAvatar != null) {
                    notifyMessage("Profil warga & foto profil berhasil disimpan!")
                } else {
                    notifyMessage("Pengaturan profil & privasi disimpan")
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                notifyMessage("Gagal menyimpan pengaturan: ${e.localizedMessage ?: "Error"}")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Google Play Store Compliance: Menghapus akun pengguna permanen dari backend PocketBase
     * dan mengosongkan sesi autentikasi lokal.
     */
    fun deleteUserAccount(
        context: Context? = null,
        onSuccess: () -> Unit = {},
    ) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                repository.deleteUserAccount(currentUserId)
                dismissSheet()
                notifyMessage("Akun Warga berhasil dihapus secara permanen.")
                onSuccess()
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                Log.e(TAG, "Gagal menghapus akun: ${e.message}", e)
                notifyMessage("Gagal menghapus akun: ${e.localizedMessage ?: "Terjadi kesalahan"}")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun toggleAdminMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                repository.setAdminMode(enabled)
                notifyMessage(
                    if (enabled) "Mode Admin Surabaya aktif! Anda dapat menyetujui usulan event."
                    else "Mode Admin dinonaktifkan.",
                )
            } catch (e: Exception) {
                notifyMessage("Gagal mengubah Mode Admin")
            }
        }
    }

    fun toggleBookmark(
        targetId: String,
        targetType: String,
        title: String,
        subtitle: String,
        district: String,
    ) {
        viewModelScope.launch {
            try {
                val saved = repository.toggleBookmark(targetId, targetType, title, subtitle, district)
                notifyMessage(if (saved) "Disimpan ke daftar favorit" else "Dihapus dari daftar tersimpan")
            } catch (e: Exception) {
                notifyMessage("Gagal mengubah bookmark")
            }
        }
    }

    fun markNotificationAsRead(id: String) {
        repository.markNotificationAsRead(id)
    }

    fun markAllNotificationsAsRead() {
        repository.markAllNotificationsAsRead(currentUserId)
        notifyMessage("Semua pemberitahuan ditandai sudah dibaca")
    }

    fun sendTestHeadsUpNotification(context: Context, type: AppNotificationType) {
        val now = System.currentTimeMillis()
        val (title, body, targetId) = when (type) {
            AppNotificationType.CHAT -> Triple(
                "Pesan Baru: Cak Sugeng",
                "Rek, sido cangkrukan nang Balai Pemuda sore iki? Tak enteni yo!",
                "public_balai_pemuda"
            )
            AppNotificationType.EVENT -> Triple(
                "Agenda Baru: Festival Rujak Uleg 2026",
                "Pendaftaran stand & pawai warga resmi dibuka di Kya-Kya!",
                "event_rujak_uleg_2026"
            )
            AppNotificationType.INCIDENT -> Triple(
                "Peringatan Lalu Lintas: Jl. Tunjungan",
                "Arus lalu lintas dialihkan sementara karena persiapan parade budaya.",
                "inc_001"
            )
            AppNotificationType.SYSTEM -> Triple(
                "Pemberitahuan Warga Surabaya",
                "Fitur notifikasi penting telah aktif dan siap menampilkan info terkini.",
                ""
            )
        }
        val notif = AppNotification(
            id = "test_${System.currentTimeMillis()}",
            userId = currentUserId,
            type = type,
            title = title,
            message = body,
            targetId = targetId,
            timestamp = now,
            isRead = false,
        )
        repository.addNotification(notif)
        SurabayaNotificationHelper.showHeadsUpNotification(
            context = context,
            title = title,
            message = body,
            type = type,
            targetId = targetId,
        )
        notifyMessage("Notifikasi banner heads-up dikirim ke layar Android")
    }

    fun submitIncident(
        title: String,
        description: String,
        category: String,
        severity: String,
        district: String,
        address: String,
        mapsUrl: String = "",
        photoUris: List<Uri> = emptyList(),
        context: Context? = null,
    ) {
        if (_isSubmitting.value || isCurrentUserFrozen()) return

        val currentIncidents = (incidentsState.value as? UiState.Success)?.data.orEmpty()
        val preCheck = SurabayaSpamProtector.checkIncidentDuplicate(
            userId = currentUserId,
            title = title,
            address = address,
            description = description,
            existingIncidents = currentIncidents,
        )
        if (preCheck is SpamCheckResult.Blocked) {
            notifyMessage(preCheck.reason)
            return
        }

        val appCtx = context?.applicationContext
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val preparedPhotos = buildList {
                    if (appCtx != null) {
                        for ((idx, uri) in photoUris.take(4).withIndex()) {
                            val compressed = ImageUploadHelper.preparePhotoFromUri(
                                context = appCtx,
                                uri = uri,
                                prefix = "kejadian${idx + 1}",
                            )
                            if (compressed != null) {
                                add(compressed)
                            }
                        }
                    }
                }
                if (photoUris.isNotEmpty() && preparedPhotos.isEmpty()) {
                    notifyMessage("Foto gagal diproses, silakan pilih foto lain dari galeri.")
                    return@launch
                }
                if (!verifyPhotosAreSafe(preparedPhotos)) {
                    return@launch
                }

                var photoSchemaMissing = false
                var multiPhotoFallbackUsed = false
                repository.createIncident(
                    authorName = currentDisplayName(),
                    title = title,
                    description = description,
                    category = category,
                    severity = severity,
                    district = district,
                    address = address,
                    mapsUrl = mapsUrl,
                    photos = preparedPhotos,
                    onPhotoSchemaMissing = { photoSchemaMissing = true },
                    onMultiPhotoFallbackUsed = { multiPhotoFallbackUsed = true },
                )
                dismissSheet()
                when {
                    preparedPhotos.isNotEmpty() && photoSchemaMissing -> {
                        notifyMessage("Laporan tayang! Untuk menampilkan foto, import ulang pocketbase_collections.json di Admin.")
                    }
                    multiPhotoFallbackUsed -> {
                        notifyMessage("Laporan tayang dengan 1 foto! Import ulang pocketbase_collections.json di Admin untuk mengaktifkan hingga 4 foto.")
                    }
                    else -> {
                        notifyMessage("Laporan kejadian berhasil dipublikasikan!")
                    }
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: DuplicateContentException) {
                notifyMessage("⚠️ ${e.userFriendlyMessage}")
            } catch (e: SpamRateLimitException) {
                notifyMessage("⚠️ ${e.userFriendlyMessage}")
            } catch (e: Exception) {
                notifyMessage("Gagal mengirim laporan: ${e.localizedMessage ?: "Periksa koneksi internet"}")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun verifyIncident(incident: IncidentReport) {
        viewModelScope.launch {
            try {
                repository.toggleVerifyIncident(incident)
            } catch (e: Exception) {
                notifyMessage("Gagal memverifikasi laporan")
            }
        }
    }

    fun sendIncidentComment(incident: IncidentReport, text: String) {
        if (text.isBlank() || isCurrentUserFrozen()) return
        viewModelScope.launch {
            try {
                repository.addIncidentComment(incident, currentDisplayName(), text)
            } catch (e: Exception) {
                notifyMessage("Gagal mengirim komentar")
            }
        }
    }

    fun submitEventProposal(
        title: String,
        category: String,
        eventDateText: String,
        timeText: String,
        venueName: String,
        district: String,
        address: String,
        priceLabel: String,
        description: String,
        mapsUrl: String = "",
        photoUris: List<Uri> = emptyList(),
        context: Context? = null,
    ) {
        if (_isSubmitting.value || isCurrentUserFrozen()) return

        val currentEvents = (approvedEventsState.value as? UiState.Success)?.data.orEmpty()
        val preCheck = SurabayaSpamProtector.checkEventProposalDuplicate(
            userId = currentUserId,
            title = title,
            venueName = venueName,
            eventDateText = eventDateText,
            existingEvents = currentEvents,
        )
        if (preCheck is SpamCheckResult.Blocked) {
            notifyMessage(preCheck.reason)
            return
        }

        val appCtx = context?.applicationContext
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val preparedPhotos = buildList {
                    if (appCtx != null) {
                        for ((idx, uri) in photoUris.take(4).withIndex()) {
                            val compressed = ImageUploadHelper.preparePhotoFromUri(
                                context = appCtx,
                                uri = uri,
                                prefix = "event${idx + 1}",
                            )
                            if (compressed != null) {
                                add(compressed)
                            }
                        }
                    }
                }
                if (photoUris.isNotEmpty() && preparedPhotos.isEmpty()) {
                    notifyMessage("Poster/foto event gagal diproses, silakan pilih gambar lain.")
                    return@launch
                }
                if (!verifyPhotosAreSafe(preparedPhotos)) {
                    return@launch
                }

                var photoSchemaMissing = false
                var multiPhotoFallbackUsed = false
                repository.proposeEvent(
                    proposerName = currentDisplayName(),
                    title = title,
                    category = category,
                    eventDateText = eventDateText,
                    timeText = timeText,
                    venueName = venueName,
                    district = district,
                    address = address,
                    priceLabel = priceLabel,
                    description = description,
                    mapsUrl = mapsUrl,
                    photos = preparedPhotos,
                    onPhotoSchemaMissing = { photoSchemaMissing = true },
                    onMultiPhotoFallbackUsed = { multiPhotoFallbackUsed = true },
                )
                if (appCtx != null) {
                    EventDraftManager.clearDraft(appCtx)
                }
                dismissSheet()
                when {
                    preparedPhotos.isNotEmpty() && photoSchemaMissing -> {
                        notifyMessage("Usulan event terkirim! Import ulang pocketbase_collections.json di Admin agar foto tampil.")
                    }
                    multiPhotoFallbackUsed -> {
                        notifyMessage("Usulan event terkirim dengan 1 foto! Import ulang pocketbase_collections.json di Admin untuk mengaktifkan hingga 4 foto.")
                    }
                    else -> {
                        notifyMessage("Usulan event dikirim! Menunggu persetujuan Admin.")
                    }
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: DuplicateContentException) {
                notifyMessage("⚠️ ${e.userFriendlyMessage}")
            } catch (e: SpamRateLimitException) {
                notifyMessage("⚠️ ${e.userFriendlyMessage}")
            } catch (e: Exception) {
                notifyMessage("Gagal mengusulkan event: ${e.localizedMessage ?: "Periksa koneksi internet"}")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun saveEventDraft(context: Context, draft: EventDraft) {
        EventDraftManager.saveDraft(context, draft)
        notifyMessage("Draf event berhasil disimpan.")
    }

    fun clearEventDraft(context: Context) {
        EventDraftManager.clearDraft(context)
        notifyMessage("Draf event dibersihkan.")
    }

    fun updateEvent(
        eventId: String,
        title: String,
        category: String,
        eventDateText: String,
        timeText: String,
        venueName: String,
        district: String,
        address: String,
        priceLabel: String,
        description: String,
        mapsUrl: String = "",
        newPhotoUris: List<Uri> = emptyList(),
        context: Context? = null,
    ) {
        if (_isSubmitting.value || isCurrentUserFrozen()) return
        val appCtx = context?.applicationContext
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val preparedPhotos: List<PreparedPhoto> = buildList {
                    if (appCtx != null && newPhotoUris.isNotEmpty()) {
                        _isScanningPhoto.value = true
                        try {
                            for ((idx, uri) in newPhotoUris.take(4).withIndex()) {
                                val compressed = ImageUploadHelper.preparePhotoFromUri(
                                    context = appCtx,
                                    uri = uri,
                                    prefix = "event_edit_${idx + 1}",
                                )
                                if (compressed != null) {
                                    add(compressed)
                                }
                            }
                        } finally {
                            _isScanningPhoto.value = false
                        }
                    }
                }
                if (newPhotoUris.isNotEmpty() && preparedPhotos.isNotEmpty()) {
                    if (!verifyPhotosAreSafe(preparedPhotos)) {
                        return@launch
                    }
                }

                repository.updateEvent(
                    eventId = eventId,
                    title = title,
                    category = category,
                    eventDateText = eventDateText,
                    timeText = timeText,
                    venueName = venueName,
                    district = district,
                    address = address,
                    priceLabel = priceLabel,
                    description = description,
                    mapsUrl = mapsUrl,
                    photos = preparedPhotos,
                )
                dismissSheet()
                notifyMessage("Event Surabaya berhasil diperbarui!")
            } catch (e: Exception) {
                notifyMessage("Gagal memperbarui event: ${e.message}")
            } finally {
                _isSubmitting.value = false
                _isScanningPhoto.value = false
            }
        }
    }

    fun moderateEvent(eventId: String, approve: Boolean, adminNote: String) {
        viewModelScope.launch {
            try {
                repository.moderateEvent(eventId, approve, adminNote)
                dismissSheet()
                notifyMessage(
                    if (approve) "Event disetujui & otomatis disiarkan via DM personal ke seluruh warga!"
                    else "Usulan event telah ditolak.",
                )
            } catch (e: Exception) {
                notifyMessage("Aksi moderasi gagal: pastikan Mode Admin aktif.")
            }
        }
    }

    fun toggleEventInterest(event: SurabayaEvent) {
        if (!SurabayaSpamProtector.canToggleEventInterest(currentUserId, event.id)) {
            return
        }
        val existing = activeInterestJobs[event.id]
        if (existing != null && existing.isActive) {
            return
        }
        val job = viewModelScope.launch {
            _pendingEventInterests.value = _pendingEventInterests.value + event.id
            try {
                repository.toggleEventInterest(event)
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                notifyMessage("Gagal memperbarui status kehadiran: ${e.localizedMessage ?: "Error"}")
            } finally {
                _pendingEventInterests.value = _pendingEventInterests.value - event.id
                activeInterestJobs.remove(event.id)
            }
        }
        activeInterestJobs[event.id] = job
    }

    fun sendEventComment(event: SurabayaEvent, text: String) {
        val clean = text.trim()
        if (clean.isBlank() || isCurrentUserFrozen()) return
        viewModelScope.launch {
            try {
                repository.addEventComment(event.id, currentDisplayName(), clean)
            } catch (e: Exception) {
                notifyMessage("Gagal mengirim komentar event: ${e.message}")
            }
        }
    }

    fun submitCulinarySpot(
        name: String,
        category: String,
        description: String,
        district: String,
        address: String,
        priceRange: String,
        openHour: Int,
        closeHour: Int,
        mapsUrl: String = "",
        gofoodUrl: String = "",
        grabfoodUrl: String = "",
        shopeefoodUrl: String = "",
        photoUris: List<Uri> = emptyList(),
        useCurrentGpsLocation: Boolean = false,
        context: Context? = null,
    ) {
        if (_isSubmitting.value || isCurrentUserFrozen()) return

        val currentSpots = (culinaryState.value as? UiState.Success)?.data.orEmpty()
        val preCheck = SurabayaSpamProtector.checkCulinaryDuplicate(
            userId = currentUserId,
            name = name,
            district = district,
            description = description,
            existingCulinary = currentSpots,
        )
        if (preCheck is SpamCheckResult.Blocked) {
            notifyMessage(preCheck.reason)
            return
        }

        val appCtx = context?.applicationContext
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val preparedPhotos = buildList {
                    if (appCtx != null) {
                        for ((idx, uri) in photoUris.take(4).withIndex()) {
                            val compressed = ImageUploadHelper.preparePhotoFromUri(
                                context = appCtx,
                                uri = uri,
                                prefix = "kuliner${idx + 1}",
                            )
                            if (compressed != null) {
                                add(compressed)
                            }
                        }
                    }
                }
                if (photoUris.isNotEmpty() && preparedPhotos.isEmpty()) {
                    notifyMessage("Foto kuliner gagal diproses, silakan pilih gambar lain.")
                    return@launch
                }
                if (!verifyPhotosAreSafe(preparedPhotos)) {
                    return@launch
                }

                var customSpotLat: Double? = null
                var customSpotLng: Double? = null
                if (useCurrentGpsLocation) {
                    val freshGps = locationHelper.fetchCurrentLocationOrDefault()
                    if (freshGps.isLiveGps) {
                        _userLat.value = freshGps.lat
                        _userLng.value = freshGps.lng
                        _isLiveGpsActive.value = true
                        customSpotLat = freshGps.lat
                        customSpotLng = freshGps.lng
                    } else if (_isLiveGpsActive.value) {
                        customSpotLat = _userLat.value
                        customSpotLng = _userLng.value
                    }
                }

                var photoSchemaMissing = false
                var multiPhotoFallbackUsed = false
                repository.createCulinarySpot(
                    authorName = currentDisplayName(),
                    name = name,
                    category = category,
                    description = description,
                    district = district,
                    address = address,
                    priceRange = priceRange,
                    openHour = openHour,
                    closeHour = closeHour,
                    mapsUrl = mapsUrl,
                    gofoodUrl = gofoodUrl,
                    grabfoodUrl = grabfoodUrl,
                    shopeefoodUrl = shopeefoodUrl,
                    customLat = customSpotLat,
                    customLng = customSpotLng,
                    photos = preparedPhotos,
                    onPhotoSchemaMissing = { photoSchemaMissing = true },
                    onMultiPhotoFallbackUsed = { multiPhotoFallbackUsed = true },
                )
                dismissSheet()
                when {
                    preparedPhotos.isNotEmpty() && photoSchemaMissing -> {
                        notifyMessage("Kuliner tersimpan! Import ulang pocketbase_collections.json di Admin agar foto tampil.")
                    }
                    multiPhotoFallbackUsed -> {
                        notifyMessage("Kuliner tersimpan dengan 1 foto! Import ulang pocketbase_collections.json di Admin untuk mengaktifkan hingga 4 foto.")
                    }
                    else -> {
                        notifyMessage("Rekomendasi kuliner berhasil ditambahkan!")
                    }
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: DuplicateContentException) {
                notifyMessage("⚠️ ${e.userFriendlyMessage}")
            } catch (e: SpamRateLimitException) {
                notifyMessage("⚠️ ${e.userFriendlyMessage}")
            } catch (e: Exception) {
                notifyMessage("Gagal menambah kuliner: ${e.localizedMessage ?: "Periksa koneksi internet"}")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun submitCulinaryReview(spot: CulinarySpot, rating: Int, comment: String) {
        if (comment.trim().length < 2 || isCurrentUserFrozen()) return
        viewModelScope.launch {
            try {
                repository.addCulinaryReview(
                    spot = spot,
                    authorName = currentDisplayName(),
                    rating = rating,
                    comment = comment,
                )
                notifyMessage("Terima kasih atas ulasan kulinernya!")
            } catch (e: Exception) {
                notifyMessage("Gagal mengirim ulasan")
            }
        }
    }

    fun greetNearbyPeer(peer: NearbyProfile) {
        if (isCurrentUserFrozen()) return
        viewModelScope.launch {
            try {
                val room = repository.openOrCreateDirectChat(
                    peerProfile = peer,
                    currentUserName = currentDisplayName(),
                    currentUserDistrict = userAccount.value?.district ?: "Surabaya",
                )
                _chatSubTab.value = 1
                _selectedTab.value = MainTab.CHAT
                _activeSheet.value = ActiveBottomSheet.ChatConversation(room)
            } catch (e: Exception) {
                notifyMessage("Gagal membuka ruang chat: ${e.localizedMessage ?: ""}")
            }
        }
    }

    fun loadSavedUnlockedGroupPins(context: Context) {
        if (currentUserId.isBlank()) return
        val prefs = context.getSharedPreferences(GROUP_PIN_PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString("${KEY_UNLOCKED_GROUP_PINS}_$currentUserId", null)
        val parsed = CommunityGroupPinHelper.parseUnlockedPinMap(raw)
        if (parsed.isNotEmpty()) {
            _unlockedGroupPinHashes.value = parsed
        }
    }

    private fun persistUnlockedGroupPins(context: Context?, updated: Map<String, String>) {
        _unlockedGroupPinHashes.value = updated
        if (context == null || currentUserId.isBlank()) return
        context.getSharedPreferences(GROUP_PIN_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("${KEY_UNLOCKED_GROUP_PINS}_$currentUserId", CommunityGroupPinHelper.serializeUnlockedPinMap(updated))
            .apply()
    }

    fun canAccessRoomWithoutPinPrompt(room: ChatRoom): Boolean {
        if (room.roomType != "PUBLIC" || !room.isPinProtected) return true
        return CommunityGroupPinHelper.hasRoomAccess(
            pinHash = room.pinHash,
            roomId = room.id,
            roomCreatedBy = room.createdBy,
            currentUserId = currentUserId,
            isAdmin = isAdmin.value,
            unlockedPinHashesByRoomId = _unlockedGroupPinHashes.value,
        )
    }

    fun requestOpenChatRoom(room: ChatRoom) {
        if (canAccessRoomWithoutPinPrompt(room)) {
            _pendingPinUnlockRoom.value = null
            _activeSheet.value = ActiveBottomSheet.ChatConversation(room)
        } else {
            _pendingPinUnlockRoom.value = room
        }
    }

    fun dismissPinUnlockDialog() {
        _pendingPinUnlockRoom.value = null
    }

    fun verifyAndUnlockChatRoom(room: ChatRoom, enteredPin: String, context: Context? = null): Boolean {
        if (canAccessRoomWithoutPinPrompt(room)) {
            _pendingPinUnlockRoom.value = null
            _activeSheet.value = ActiveBottomSheet.ChatConversation(room)
            return true
        }
        val isMatch = CommunityGroupPinHelper.verifyPin(
            roomSeed = room.pinSeed,
            expectedPinHash = room.pinHash,
            candidatePin = enteredPin,
        )
        if (isMatch) {
            val nextMap = LinkedHashMap(_unlockedGroupPinHashes.value)
            nextMap[room.id] = room.pinHash.lowercase()
            persistUnlockedGroupPins(context, nextMap)
            _pendingPinUnlockRoom.value = null
            _activeSheet.value = ActiveBottomSheet.ChatConversation(room)
            return true
        }
        return false
    }

    fun openCreatePublicGroup() {
        if (isCurrentUserFrozen()) return
        if (!isAdmin.value) {
            val myCount = publicChatRooms.value.count { it.roomType == "PUBLIC" && it.createdBy == currentUserId }
            if (myCount >= CommunityGroupPinHelper.MAX_GROUPS_PER_CITIZEN) {
                notifyMessage(
                    "Kuota grup Anda penuh ($myCount/${CommunityGroupPinHelper.MAX_GROUPS_PER_CITIZEN}). Hapus grup lama Anda terlebih dahulu (atau minta Admin) untuk membuat grup baru.",
                )
                return
            }
        }
        _activeSheet.value = ActiveBottomSheet.CreatePublicGroup
    }

    fun submitPublicGroupRoom(
        title: String,
        subtitle: String,
        welcomeMessage: String,
        pinCode: String = "",
        context: Context? = null,
    ) {
        if (_isSubmitting.value || isCurrentUserFrozen()) return
        val cleanPin = pinCode.trim()
        if (cleanPin.isNotEmpty() && !CommunityGroupPinHelper.isValidFourDigitPin(cleanPin)) {
            notifyMessage("PIN grup wajib terdiri dari tepat 4 angka (0–9).")
            return
        }
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val createdRoom = repository.createPublicGroupRoom(
                    title = title,
                    subtitle = subtitle,
                    welcomeMessage = welcomeMessage,
                    adminDisplayName = currentDisplayName(),
                    pinCode = cleanPin,
                )
                if (createdRoom.isPinProtected) {
                    val nextMap = LinkedHashMap(_unlockedGroupPinHashes.value)
                    nextMap[createdRoom.id] = createdRoom.pinHash.lowercase()
                    persistUnlockedGroupPins(context, nextMap)
                }
                _chatSubTab.value = 0
                _selectedTab.value = MainTab.CHAT
                _activeSheet.value = ActiveBottomSheet.ChatConversation(createdRoom)
                val modeInfo = if (createdRoom.isPinProtected) " (Komunitas Ber-PIN)" else " (Terbuka)"
                notifyMessage("Grup \"${createdRoom.title}\"$modeInfo berhasil dibuat!")
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                notifyMessage("Gagal membuat grup: ${e.localizedMessage ?: "Periksa koneksi internet"}")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun updatePublicGroupPin(
        room: ChatRoom,
        newPinCode: String,
        context: Context? = null,
    ) {
        if (room.roomType != "PUBLIC") return
        val canManage = isAdmin.value || room.isCreatedBy(currentUserId)
        if (!canManage) {
            notifyMessage("Hanya pembuat grup atau Admin yang dapat mengatur PIN grup ini.")
            return
        }
        val cleanPin = newPinCode.trim()
        if (cleanPin.isNotEmpty() && !CommunityGroupPinHelper.isValidFourDigitPin(cleanPin)) {
            notifyMessage("PIN grup baru wajib tepat 4 angka (0–9).")
            return
        }
        viewModelScope.launch {
            try {
                val updatedRoom = repository.updatePublicGroupPin(room, cleanPin)
                val nextMap = LinkedHashMap(_unlockedGroupPinHashes.value)
                if (updatedRoom.isPinProtected) {
                    nextMap[updatedRoom.id] = updatedRoom.pinHash.lowercase()
                } else {
                    nextMap.remove(updatedRoom.id)
                }
                persistUnlockedGroupPins(context, nextMap)
                val currentSheet = _activeSheet.value
                if (currentSheet is ActiveBottomSheet.ChatConversation && currentSheet.room.id == room.id) {
                    _activeSheet.value = ActiveBottomSheet.ChatConversation(updatedRoom)
                }
                if (updatedRoom.isPinProtected) {
                    notifyMessage("PIN 4 angka untuk grup \"${updatedRoom.title}\" berhasil diperbarui!")
                } else {
                    notifyMessage("Grup \"${updatedRoom.title}\" kini dibuka tanpa PIN.")
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                notifyMessage("Gagal memperbarui PIN grup: ${e.localizedMessage ?: ""}")
            }
        }
    }

    fun deletePublicGroup(room: ChatRoom) {
        val canDelete = (isAdmin.value || room.isCreatedBy(currentUserId)) && room.roomType == "PUBLIC"
        if (!canDelete) {
            notifyMessage("Hanya pembuat grup atau Admin yang dapat menghapus grup ini.")
            return
        }
        viewModelScope.launch {
            try {
                repository.deletePublicGroupRoom(room.id)
                val currentSheet = _activeSheet.value
                if (currentSheet is ActiveBottomSheet.ChatConversation && currentSheet.room.id == room.id) {
                    dismissSheet()
                }
                if (_pendingPinUnlockRoom.value?.id == room.id) {
                    _pendingPinUnlockRoom.value = null
                }
                notifyMessage("Grup \"${room.title}\" telah dihapus.")
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                notifyMessage("Gagal menghapus grup: ${e.localizedMessage ?: ""}")
            }
        }
    }

    fun adminDeletePublicGroup(room: ChatRoom) {
        deletePublicGroup(room)
    }

    fun sendChatMessage(
        room: ChatRoom,
        text: String,
        photoUri: Uri? = null,
        context: Context? = null,
        replyTo: ChatMessage? = null,
    ) {
        if ((text.isBlank() && photoUri == null) || isCurrentUserFrozen()) return
        viewModelScope.launch {
            _isSendingChatMessage.value = true
            try {
                var preparedPhoto: PreparedPhoto? = null
                if (photoUri != null && context != null) {
                    val prepared = ImageUploadHelper.preparePhotoFromUri(
                        context = context,
                        uri = photoUri,
                        prefix = "chat",
                        maxDimensionPx = 1024,
                        initialQuality = 75,
                    )
                    if (prepared != null) {
                        val isSafe = verifyPhotosAreSafe(listOf(prepared))
                        if (!isSafe) return@launch
                        preparedPhoto = prepared
                    }
                }
                val msgId = repository.sendMessage(
                    room = room,
                    senderName = currentDisplayName(),
                    text = text,
                    photo = preparedPhoto,
                    replyTo = replyTo,
                )
                if (msgId.isNotBlank() && preparedPhoto != null && context != null) {
                    // WhatsApp-style: Immediately save outgoing photo to local cache & Android Gallery!
                    ChatMediaManager.saveOutgoingPhoto(context, msgId, preparedPhoto.bytes, preparedPhoto.mimeType)
                }
                setTyping(room, false)
            } catch (e: Exception) {
                notifyMessage("Gagal mengirim pesan: ${e.localizedMessage ?: ""}")
            } finally {
                _isSendingChatMessage.value = false
            }
        }
    }

    /**
     * Store-and-forward synchronizer: Automatically downloads incoming chat photos to local disk cache,
     * registers them into the device's Android Gallery (Pictures/ArekSurabaya), and triggers server pruning
     * for direct chats so server database storage stays permanently clean!
     */
    fun syncAndSaveChatPhotos(context: Context, messages: List<ChatMessage>, room: ChatRoom) {
        viewModelScope.launch(Dispatchers.IO) {
            val photosToDownload = messages.filter { msg ->
                msg.photoUrl.isNotBlank() && !ChatMediaManager.isPhotoSavedLocally(context, msg.id)
            }
            for (msg in photosToDownload) {
                val file = ChatMediaManager.downloadAndSaveIncomingPhoto(context, msg.id, msg.photoUrl)
                if (file != null && msg.senderId != currentUserId) {
                    repository.markChatPhotoDeliveredAndPruneServer(msg.id, isDirect = (room.roomType == "DIRECT"))
                }
            }
        }
    }

    /**
     * Automatically syncs approved event DMs to the current user's direct chat with "Arek Surabaya",
     * and silently downloads/saves the event poster into the Android device's Gallery (Pictures/ArekSurabaya).
     * No user confirmation is requested—ensuring the poster is preserved directly in the citizen's gallery!
     */
    fun syncApprovedEventsToDirectChatAndGallery(context: Context, events: List<SurabayaEvent>) {
        if (events.isEmpty() || currentUserId.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Sync DM records in database
                repository.syncEventDirectMessagesForUser(currentUserId, currentDisplayName(), events)

                // 2. Silently save event posters into device Gallery
                for (event in events) {
                    val drawableId = com.example.data.util.resolveImageDrawable(event.imageKey)
                    ChatMediaManager.saveEventPosterToGallery(
                        context = context,
                        eventId = event.id,
                        photoUrl = event.allPhotos.firstOrNull() ?: event.photoUrl,
                        drawableResId = drawableId,
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "syncApprovedEventsToDirectChatAndGallery warning: ${e.message}")
            }
        }
    }

    fun markMessagesAsRead(room: ChatRoom, messageIds: List<String>) {
        repository.markMessagesAsRead(room, currentUserId, messageIds)
    }

    private var lastTypingReportTime = 0L

    fun setTyping(room: ChatRoom, isTyping: Boolean) {
        val now = System.currentTimeMillis()
        if (isTyping) {
            if (now - lastTypingReportTime > 2500L) {
                lastTypingReportTime = now
                viewModelScope.launch {
                    repository.updateTypingStatus(room, currentDisplayName(), true)
                }
            }
        } else {
            if (lastTypingReportTime != 0L) {
                lastTypingReportTime = 0L
                viewModelScope.launch {
                    repository.updateTypingStatus(room, currentDisplayName(), false)
                }
            }
        }
    }

    fun reportIncidentLink(report: IncidentReport) {
        if (currentUserId in report.linkReportedByUids) {
            notifyMessage("Anda sudah melaporkan tautan pada laporan ini.")
            return
        }
        viewModelScope.launch {
            try {
                val nextCount = repository.reportSuspiciousLink(
                    collection = "incidents",
                    recordId = report.id,
                    currentCount = report.linkReportCount,
                    currentReportedUids = report.linkReportedByUids,
                )
                if (nextCount >= 3) {
                    notifyMessage("Tautan luar otomatis dinonaktifkan demi keamanan warga!")
                } else {
                    notifyMessage("Terima kasih! Laporan tautan mencurigakan dicatat ($nextCount/3 untuk blokir otomatis).")
                }
            } catch (e: Exception) {
                notifyMessage("Gagal melaporkan tautan")
            }
        }
    }

    fun reportEventLink(event: SurabayaEvent) {
        if (currentUserId in event.linkReportedByUids) {
            notifyMessage("Anda sudah melaporkan tautan pada event ini.")
            return
        }
        viewModelScope.launch {
            try {
                val nextCount = repository.reportSuspiciousLink(
                    collection = "events",
                    recordId = event.id,
                    currentCount = event.linkReportCount,
                    currentReportedUids = event.linkReportedByUids,
                )
                if (nextCount >= 3) {
                    notifyMessage("Tautan luar event otomatis dinonaktifkan demi keamanan warga!")
                } else {
                    notifyMessage("Terima kasih! Laporan tautan dicatat ($nextCount/3 untuk blokir otomatis).")
                }
            } catch (e: Exception) {
                notifyMessage("Gagal melaporkan tautan")
            }
        }
    }

    fun reportCulinaryLink(spot: CulinarySpot) {
        if (currentUserId in spot.linkReportedByUids) {
            notifyMessage("Anda sudah melaporkan tautan pada kuliner ini.")
            return
        }
        viewModelScope.launch {
            try {
                val nextCount = repository.reportSuspiciousLink(
                    collection = "culinary",
                    recordId = spot.id,
                    currentCount = spot.linkReportCount,
                    currentReportedUids = spot.linkReportedByUids,
                )
                if (nextCount >= 3) {
                    notifyMessage("Tautan luar kuliner otomatis dinonaktifkan demi keamanan warga!")
                } else {
                    notifyMessage("Terima kasih! Laporan tautan dicatat ($nextCount/3 untuk blokir otomatis).")
                }
            } catch (e: Exception) {
                notifyMessage("Gagal melaporkan tautan")
            }
        }
    }

    fun adminClearExternalLinks(collection: String, recordId: String) {
        viewModelScope.launch {
            try {
                repository.clearAllExternalLinksByAdmin(collection, recordId)
                notifyMessage("Tautan luar mencurigakan telah dihapus bersih oleh Admin!")
            } catch (e: Exception) {
                notifyMessage("Gagal menghapus tautan luar")
            }
        }
    }

    fun reportIncidentContent(report: IncidentReport) {
        if (currentUserId in report.contentReportedByUids) {
            notifyMessage("Anda sudah melaporkan konten/foto pada laporan ini.")
            return
        }
        viewModelScope.launch {
            try {
                val nextCount = repository.reportInappropriateContent(
                    collection = "incidents",
                    recordId = report.id,
                    currentCount = report.contentReportCount,
                    currentReportedUids = report.contentReportedByUids,
                )
                if (nextCount >= 10) {
                    if (!isAdmin.value) dismissSheet()
                    notifyMessage("Konten mencapai 10 laporan warga dan otomatis dikarantina dari feed publik!")
                } else if (nextCount >= 5) {
                    notifyMessage("Laporan diterima ($nextCount/5). Foto otomatis diburamkan demi kenyamanan warga.")
                } else {
                    notifyMessage("Laporan dicatat ($nextCount/5). Foto otomatis diburamkan jika mencapai 5 laporan warga.")
                }
            } catch (e: Exception) {
                notifyMessage("Gagal melaporkan konten")
            }
        }
    }

    fun reportEventContent(event: SurabayaEvent) {
        if (currentUserId in event.contentReportedByUids) {
            notifyMessage("Anda sudah melaporkan konten/foto pada event ini.")
            return
        }
        viewModelScope.launch {
            try {
                val nextCount = repository.reportInappropriateContent(
                    collection = "events",
                    recordId = event.id,
                    currentCount = event.contentReportCount,
                    currentReportedUids = event.contentReportedByUids,
                )
                if (nextCount >= 10) {
                    if (!isAdmin.value) dismissSheet()
                    notifyMessage("Event mencapai 10 laporan warga dan otomatis dikarantina dari feed publik!")
                } else if (nextCount >= 5) {
                    notifyMessage("Laporan diterima ($nextCount/5). Foto event otomatis diburamkan.")
                } else {
                    notifyMessage("Laporan dicatat ($nextCount/5). Foto akan diburamkan jika mencapai 5 laporan warga.")
                }
            } catch (e: Exception) {
                notifyMessage("Gagal melaporkan konten event")
            }
        }
    }

    fun reportCulinaryContent(spot: CulinarySpot) {
        if (currentUserId in spot.contentReportedByUids) {
            notifyMessage("Anda sudah melaporkan konten/foto pada kuliner ini.")
            return
        }
        viewModelScope.launch {
            try {
                val nextCount = repository.reportInappropriateContent(
                    collection = "culinary",
                    recordId = spot.id,
                    currentCount = spot.contentReportCount,
                    currentReportedUids = spot.contentReportedByUids,
                )
                if (nextCount >= 10) {
                    if (!isAdmin.value) dismissSheet()
                    notifyMessage("Kuliner mencapai 10 laporan warga dan otomatis dikarantina dari feed publik!")
                } else if (nextCount >= 5) {
                    notifyMessage("Laporan diterima ($nextCount/5). Foto kuliner otomatis diburamkan.")
                } else {
                    notifyMessage("Laporan dicatat ($nextCount/5). Foto akan diburamkan jika mencapai 5 laporan warga.")
                }
            } catch (e: Exception) {
                notifyMessage("Gagal melaporkan konten kuliner")
            }
        }
    }

    fun toggleMomentLike(moment: UserMoment) {
        viewModelScope.launch {
            try {
                repository.toggleMomentLike(moment)
            } catch (e: Exception) {
                notifyMessage("Gagal menyukai momen")
            }
        }
    }

    fun submitMomentComment(momentId: String, commentText: String) {
        val clean = commentText.trim()
        if (clean.isBlank() || isCurrentUserFrozen()) return
        viewModelScope.launch {
            try {
                repository.addMomentComment(
                    momentId = momentId,
                    text = clean,
                )
                notifyMessage("Komentar terkirim!")
            } catch (e: Exception) {
                notifyMessage("Gagal mengirim komentar")
            }
        }
    }

    fun submitMoment(
        caption: String,
        district: String,
        locationName: String,
        postType: String = POST_TYPE_MOMEN,
        photoUris: List<Uri> = emptyList(),
        context: Context? = null,
    ) {
        if (_isSubmitting.value || isCurrentUserFrozen()) return

        val currentMoments = (momentsState.value as? UiState.Success)?.data.orEmpty()
        val preCheck = SurabayaSpamProtector.checkMomentDuplicate(
            userId = currentUserId,
            caption = caption,
            existingMoments = currentMoments,
        )
        if (preCheck is SpamCheckResult.Blocked) {
            notifyMessage(preCheck.reason)
            return
        }

        val appCtx = context?.applicationContext
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val preparedPhotos = buildList {
                    if (appCtx != null) {
                        for ((idx, uri) in photoUris.take(4).withIndex()) {
                            val compressed = ImageUploadHelper.preparePhotoFromUri(
                                context = appCtx,
                                uri = uri,
                                prefix = "momen${idx + 1}",
                            )
                            if (compressed != null) {
                                add(compressed)
                            }
                        }
                    }
                }
                if (photoUris.isNotEmpty() && preparedPhotos.isEmpty()) {
                    notifyMessage("Foto gagal diproses, silakan pilih foto lain dari galeri.")
                    return@launch
                }
                if (!verifyPhotosAreSafe(preparedPhotos)) {
                    return@launch
                }

                repository.createMoment(
                    authorName = currentDisplayName(),
                    authorDistrict = userAccount.value?.district ?: "Genteng",
                    caption = caption,
                    district = district,
                    locationName = locationName,
                    postType = postType,
                    photos = preparedPhotos,
                )
                dismissSheet()
                notifyMessage(
                    if (postType == POST_TYPE_LENSA_WARGA) "Foto sudut kota berhasil dibagikan ke Lensa Warga!"
                    else "Momen berhasil dibagikan ke warga Surabaya!"
                )
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: DuplicateContentException) {
                notifyMessage("⚠️ ${e.userFriendlyMessage}")
            } catch (e: SpamRateLimitException) {
                notifyMessage("⚠️ ${e.userFriendlyMessage}")
            } catch (e: Exception) {
                notifyMessage("Gagal membagikan momen: ${e.message}")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun submitDestination(
        title: String,
        category: String,
        description: String,
        address: String,
        district: String,
        ticketPrice: String,
        operationalHours: String,
        bestTimeToVisit: String,
        mapsUrl: String = "",
        photoUris: List<Uri> = emptyList(),
        context: Context? = null,
    ) {
        if (!isAdmin.value) {
            notifyMessage("Hanya Admin yang diizinkan menambahkan destinasi baru!")
            return
        }
        if (_isSubmitting.value || isCurrentUserFrozen()) return
        val appCtx = context?.applicationContext
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val preparedPhotos = buildList {
                    if (appCtx != null) {
                        for ((idx, uri) in photoUris.take(4).withIndex()) {
                            val compressed = ImageUploadHelper.preparePhotoFromUri(
                                context = appCtx,
                                uri = uri,
                                prefix = "destinasi${idx + 1}",
                            )
                            if (compressed != null) {
                                add(compressed)
                            }
                        }
                    }
                }
                if (photoUris.isNotEmpty() && preparedPhotos.isEmpty()) {
                    notifyMessage("Foto gagal diproses, silakan pilih foto lain dari galeri.")
                    return@launch
                }
                if (!verifyPhotosAreSafe(preparedPhotos)) {
                    return@launch
                }

                repository.createDestination(
                    title = title,
                    category = category,
                    description = description,
                    address = address,
                    district = district,
                    ticketPrice = ticketPrice,
                    operationalHours = operationalHours,
                    bestTime = bestTimeToVisit,
                    mapsUrl = mapsUrl,
                    photos = preparedPhotos,
                )
                if (appCtx != null) {
                    DestinationDraftManager.clearDraft(appCtx)
                }
                dismissSheet()
                notifyMessage("Destinasi Surabaya berhasil ditambahkan oleh Admin!")
            } catch (e: Exception) {
                notifyMessage("Gagal menambahkan destinasi: ${e.message}")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun saveDestinationDraft(context: Context, draft: DestinationDraft) {
        DestinationDraftManager.saveDraft(context, draft)
        notifyMessage("Draf destinasi berhasil disimpan.")
    }

    fun clearDestinationDraft(context: Context) {
        DestinationDraftManager.clearDraft(context)
        notifyMessage("Draf destinasi dibersihkan.")
    }

    fun updateDestination(
        destinationId: String,
        title: String,
        category: String,
        description: String,
        address: String,
        district: String,
        ticketPrice: String,
        operationalHours: String,
        bestTimeToVisit: String,
        mapsUrl: String = "",
        newPhotoUris: List<Uri> = emptyList(),
        context: Context? = null,
    ) {
        if (!isAdmin.value) {
            notifyMessage("Hanya Admin yang diizinkan mengedit destinasi!")
            return
        }
        if (_isSubmitting.value || isCurrentUserFrozen()) return
        val appCtx = context?.applicationContext
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val preparedPhotos: List<PreparedPhoto> = buildList {
                    if (appCtx != null && newPhotoUris.isNotEmpty()) {
                        _isScanningPhoto.value = true
                        try {
                            for ((idx, uri) in newPhotoUris.take(4).withIndex()) {
                                val compressed = ImageUploadHelper.preparePhotoFromUri(
                                    context = appCtx,
                                    uri = uri,
                                    prefix = "destinasi_edit_${idx + 1}",
                                )
                                if (compressed != null) {
                                    add(compressed)
                                }
                            }
                        } finally {
                            _isScanningPhoto.value = false
                        }
                    }
                }
                if (newPhotoUris.isNotEmpty() && preparedPhotos.isNotEmpty()) {
                    if (!verifyPhotosAreSafe(preparedPhotos)) {
                        return@launch
                    }
                }

                repository.updateDestination(
                    destinationId = destinationId,
                    title = title,
                    category = category,
                    description = description,
                    district = district,
                    address = address,
                    ticketPrice = ticketPrice,
                    operationalHours = operationalHours,
                    bestTime = bestTimeToVisit,
                    mapsUrl = mapsUrl,
                    newPhotos = preparedPhotos,
                )
                dismissSheet()
                notifyMessage("Destinasi Surabaya berhasil diperbarui!")
            } catch (e: Exception) {
                notifyMessage("Gagal memperbarui destinasi: ${e.message}")
            } finally {
                _isSubmitting.value = false
                _isScanningPhoto.value = false
            }
        }
    }

    fun submitDestinationReview(destinationId: String, review: String, rating: Int) {
        val clean = review.trim()
        if (clean.isBlank() || isCurrentUserFrozen()) return
        viewModelScope.launch {
            try {
                repository.addDestinationReview(
                    destinationId = destinationId,
                    rating = rating.coerceIn(1, 5),
                    comment = clean,
                )
                notifyMessage("Ulasan destinasi terkirim!")
            } catch (e: Exception) {
                notifyMessage("Gagal mengirim ulasan destinasi")
            }
        }
    }

    fun reportMomentContent(moment: UserMoment) {
        if (currentUserId in moment.contentReportedByUids) {
            notifyMessage("Anda sudah melaporkan konten/foto pada momen ini.")
            return
        }
        viewModelScope.launch {
            try {
                val nextCount = repository.reportInappropriateContent(
                    collection = "moments",
                    recordId = moment.id,
                    currentCount = moment.contentReportCount,
                    currentReportedUids = moment.contentReportedByUids,
                )
                if (nextCount >= 10) {
                    if (!isAdmin.value) dismissSheet()
                    notifyMessage("Momen mencapai 10 laporan warga dan dikarantina!")
                } else if (nextCount >= 5) {
                    notifyMessage("Laporan diterima ($nextCount/5). Foto otomatis diburamkan.")
                } else {
                    notifyMessage("Laporan dicatat ($nextCount/5). Foto akan diburamkan jika mencapai 5 laporan warga.")
                }
            } catch (e: Exception) {
                notifyMessage("Gagal melaporkan momen")
            }
        }
    }

    fun reportDestinationContent(destination: SurabayaDestination) {
        if (currentUserId in destination.contentReportedByUids) {
            notifyMessage("Anda sudah melaporkan konten/foto pada destinasi ini.")
            return
        }
        viewModelScope.launch {
            try {
                val nextCount = repository.reportInappropriateContent(
                    collection = "destinations",
                    recordId = destination.id,
                    currentCount = destination.contentReportCount,
                    currentReportedUids = destination.contentReportedByUids,
                )
                if (nextCount >= 10) {
                    if (!isAdmin.value) dismissSheet()
                    notifyMessage("Destinasi mencapai 10 laporan warga dan dikarantina!")
                } else if (nextCount >= 5) {
                    notifyMessage("Laporan diterima ($nextCount/5). Foto destinasi otomatis diburamkan.")
                } else {
                    notifyMessage("Laporan dicatat ($nextCount/5). Foto akan diburamkan jika mencapai 5 laporan warga.")
                }
            } catch (e: Exception) {
                notifyMessage("Gagal melaporkan destinasi")
            }
        }
    }

    fun blockCitizen(targetUid: String, targetName: String) {
        val cleanUid = targetUid.trim()
        if (cleanUid.isBlank() || cleanUid == currentUserId) return
        viewModelScope.launch {
            try {
                repository.blockUser(cleanUid, userAccount.value?.blockedUids.orEmpty())
                dismissSheet()
                notifyMessage("${targetName.ifBlank { "Pengguna" }} diblokir. Semua kiriman & pesan darinya disembunyikan.")
            } catch (e: Exception) {
                notifyMessage("Gagal memblokir pengguna")
            }
        }
    }

    fun unblockCitizen(targetUid: String) {
        val cleanUid = targetUid.trim()
        if (cleanUid.isBlank()) return
        viewModelScope.launch {
            try {
                repository.unblockUser(cleanUid, userAccount.value?.blockedUids.orEmpty())
                notifyMessage("Blokir pengguna telah dibuka kembali.")
            } catch (e: Exception) {
                notifyMessage("Gagal membuka blokir pengguna")
            }
        }
    }

    fun adminDeletePost(collection: String, recordId: String) {
        viewModelScope.launch {
            try {
                repository.adminDeletePost(collection, recordId)
                dismissSheet()
                notifyMessage("Kiriman pelanggar berhasil dihapus permanen oleh Admin!")
            } catch (e: Exception) {
                notifyMessage("Gagal menghapus kiriman: pastikan Mode Admin aktif.")
            }
        }
    }

    fun adminRestoreContent(collection: String, recordId: String) {
        viewModelScope.launch {
            try {
                repository.adminRestoreContentReports(collection, recordId)
                notifyMessage("Status blur & laporan konten telah dipulihkan oleh Admin.")
            } catch (e: Exception) {
                notifyMessage("Gagal memulihkan status konten")
            }
        }
    }

    fun adminBanCitizen(targetUid: String, targetName: String) {
        val cleanUid = targetUid.trim()
        if (cleanUid.isBlank() || cleanUid == currentUserId) return
        viewModelScope.launch {
            try {
                repository.adminBanUser(cleanUid)
                dismissSheet()
                notifyMessage("Akun ${targetName.ifBlank { "pelanggar" }} telah dibekukan oleh Admin!")
            } catch (e: Exception) {
                notifyMessage("Gagal membekukan akun pelanggar")
            }
        }
    }

    fun adminUnbanCitizen(targetUid: String, targetName: String) {
        val cleanUid = targetUid.trim()
        if (cleanUid.isBlank()) return
        viewModelScope.launch {
            try {
                repository.adminUnbanUser(cleanUid)
                notifyMessage("Akun ${targetName.ifBlank { "warga" }} berhasil dipulihkan kembali oleh Admin.")
            } catch (e: Exception) {
                notifyMessage("Gagal memulihkan akun warga")
            }
        }
    }

    fun openAdminDashboard() {
        if (!isAdmin.value) {
            notifyMessage("Halaman Pusat Kendali hanya dapat diakses oleh Admin Resmi Surabaya.")
            return
        }
        dismissSheet()
        _selectedTab.value = MainTab.ADMIN_DASHBOARD
    }

    fun approveAllPendingRequests(
        eventsToApprove: List<SurabayaEvent>,
        pollsToApprove: List<CitizenPoll>,
        adminNote: String = "Disetujui melalui Pusat Kendali Admin Surabaya.",
    ) {
        if (!isAdmin.value) return
        val total = eventsToApprove.size + pollsToApprove.size
        if (total == 0) {
            notifyMessage("Tidak ada pengajuan menunggu saat ini.")
            return
        }
        viewModelScope.launch {
            _isSubmitting.value = true
            var approvedCount = 0
            try {
                for (ev in eventsToApprove) {
                    runCatching {
                        repository.moderateEvent(ev.id, approve = true, adminNote = adminNote)
                        approvedCount++
                    }
                }
                for (poll in pollsToApprove) {
                    runCatching {
                        repository.moderatePoll(poll.id, approve = true, adminNote = adminNote)
                        approvedCount++
                    }
                }
                notifyMessage("Berhasil menyetujui $approvedCount pengajuan warga sekaligus!")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun submitCitizenService(
        title: String,
        category: String,
        price: Long,
        priceUnit: String,
        district: String,
        coverageArea: String,
        phoneNumber: String,
        whatsappNumber: String,
        description: String,
        photoUris: List<Uri> = emptyList(),
        context: Context? = null,
    ) {
        if (_isSubmitting.value || isCurrentUserFrozen()) return
        if (title.isBlank()) {
            notifyMessage("Mohon isi nama jasa atau usaha Anda.")
            return
        }
        if (phoneNumber.isBlank() && whatsappNumber.isBlank()) {
            notifyMessage("Mohon sertakan nomor WhatsApp atau telepon agar warga dapat menghubungi Anda.")
            return
        }

        val currentServices = (servicesState.value as? UiState.Success)?.data.orEmpty()
        val preCheck = SurabayaSpamProtector.checkServiceDuplicate(
            userId = currentUserId,
            title = title,
            description = description,
            phoneNumber = phoneNumber,
            whatsappNumber = whatsappNumber,
            existingServices = currentServices,
        )
        if (preCheck is SpamCheckResult.Blocked) {
            notifyMessage(preCheck.reason)
            return
        }

        val appCtx = context?.applicationContext
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val preparedPhotos = buildList {
                    if (appCtx != null) {
                        for ((idx, uri) in photoUris.take(3).withIndex()) {
                            val compressed = ImageUploadHelper.preparePhotoFromUri(
                                context = appCtx,
                                uri = uri,
                                prefix = "jasa${idx + 1}",
                            )
                            if (compressed != null) {
                                add(compressed)
                            }
                        }
                    }
                }
                if (photoUris.isNotEmpty() && preparedPhotos.isEmpty()) {
                    notifyMessage("Foto flyer gagal diproses, silakan coba lagi.")
                    return@launch
                }
                if (!verifyPhotosAreSafe(preparedPhotos)) {
                    return@launch
                }

                repository.createCitizenService(
                    providerName = currentDisplayName(),
                    title = title,
                    category = category,
                    price = price,
                    priceUnit = priceUnit,
                    district = district,
                    coverageArea = coverageArea,
                    phoneNumber = phoneNumber,
                    whatsappNumber = whatsappNumber,
                    description = description,
                    flyerPhotos = preparedPhotos,
                )
                dismissSheet()
                notifyMessage("Promosi jasa berhasil dipasang di Arek Surabaya!")
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: DuplicateContentException) {
                notifyMessage("⚠️ ${e.userFriendlyMessage}")
            } catch (e: SpamRateLimitException) {
                notifyMessage("⚠️ ${e.userFriendlyMessage}")
            } catch (e: Exception) {
                notifyMessage("Gagal memasang promosi jasa: ${e.message}")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun reportServiceContent(service: CitizenService) {
        viewModelScope.launch {
            try {
                repository.reportServiceContent(service.id, currentUserId)
                notifyMessage("Terima kasih. Laporan jasa Anda telah tercatat untuk verifikasi keamanan.")
            } catch (e: Exception) {
                notifyMessage("Gagal mengirim laporan: ${e.message}")
            }
        }
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            try {
                repository.deleteCitizenService(serviceId, currentUserId, isAdmin.value)
                dismissSheet()
                notifyMessage("Promosi jasa berhasil dihapus.")
            } catch (e: Exception) {
                notifyMessage("Gagal menghapus jasa: ${e.message}")
            }
        }
    }

    // --- POLLING WARGA & SUARA RAKYAT OPERATIONS ---

    fun openPollDetail(pollId: String) {
        _activeSheet.value = ActiveBottomSheet.PollDetail(pollId)
    }

    fun openCreatePoll() {
        _activeSheet.value = ActiveBottomSheet.CreatePoll
    }

    fun openAdminPendingPolls() {
        _activeSheet.value = ActiveBottomSheet.AdminPendingPolls
    }

    fun submitPollProposal(
        title: String,
        description: String,
        category: String,
        district: String,
        optionTexts: List<String>,
        onComplete: (Boolean) -> Unit = {},
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                repository.proposePoll(
                    title = title,
                    description = description,
                    category = category,
                    district = district,
                    optionTexts = optionTexts,
                )
                dismissSheet()
                notifyMessage("Usulan polling berhasil diajukan! Menunggu persetujuan moderasi admin agar tampil untuk warga.")
                onComplete(true)
            } catch (e: Exception) {
                notifyMessage("Gagal mengajukan polling: ${e.message}")
                onComplete(false)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun votePoll(pollId: String, optionId: String) {
        viewModelScope.launch {
            try {
                val success = repository.votePoll(pollId, optionId)
                if (success) {
                    notifyMessage("✅ Suara Anda berhasil tercatat! Terima kasih telah berpartisipasi.")
                }
            } catch (e: Exception) {
                notifyMessage("Gagal memberikan suara: ${e.message}")
            }
        }
    }

    fun moderatePoll(pollId: String, approve: Boolean, adminNote: String = "") {
        viewModelScope.launch {
            try {
                repository.moderatePoll(pollId, approve, adminNote)
                notifyMessage(if (approve) "Usulan polling disetujui & tayang ke publik." else "Usulan polling ditolak.")
            } catch (e: Exception) {
                notifyMessage("Gagal memoderasi polling: ${e.message}")
            }
        }
    }

    fun submitPollComment(pollId: String, text: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.addPollComment(pollId, text)
                notifyMessage("Komentar warga berhasil dikirim.")
                onComplete(true)
            } catch (e: Exception) {
                notifyMessage("Gagal mengirim komentar: ${e.message}")
                onComplete(false)
            }
        }
    }

    fun sharePollText(context: Context, poll: CitizenPoll) {
        try {
            PollImageExporter.sharePollAsText(context, poll)
            notifyMessage("Membuka menu bagikan teks polling...")
        } catch (e: Exception) {
            notifyMessage("Gagal membagikan polling: ${e.message}")
        }
    }

    fun exportPollJpg(context: Context, poll: CitizenPoll, shareDirectly: Boolean = true) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                if (shareDirectly) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        PollImageExporter.sharePollAsImage(context, poll)
                    }
                    notifyMessage("Membuka menu bagikan gambar JPG...")
                } else {
                    val saved = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        PollImageExporter.savePollImageToGallery(context, poll)
                    }
                    if (saved) {
                        notifyMessage("✅ Gambar hasil polling (JPG) berhasil disimpan ke Galeri!")
                    } else {
                        notifyMessage("Gagal menyimpan gambar JPG ke galeri.")
                    }
                }
            } catch (e: Exception) {
                notifyMessage("Ekspor gambar gagal: ${e.message}")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun deletePoll(pollId: String) {
        viewModelScope.launch {
            try {
                repository.deletePoll(pollId, currentUserId, isAdmin.value)
                dismissSheet()
                notifyMessage("Polling berhasil dihapus.")
            } catch (e: Exception) {
                notifyMessage("Gagal menghapus polling: ${e.message}")
            }
        }
    }

    // --- TAMBAL BAN (SALAM SATU ASPAL) OPERATIONS ---

    fun openTambalBanDetail(spotId: String) {
        _activeSheet.value = ActiveBottomSheet.TambalBanDetail(spotId)
    }

    fun openCreateMoment(postType: String = POST_TYPE_MOMEN) {
        _activeSheet.value = ActiveBottomSheet.CreateMoment(initialPostType = postType)
    }

    fun openCreateLensaWarga() {
        _activeSheet.value = ActiveBottomSheet.CreateMoment(initialPostType = POST_TYPE_LENSA_WARGA)
    }

    fun openCreateTambalBan() {
        _activeSheet.value = ActiveBottomSheet.CreateTambalBan
    }

    fun submitTambalBanSpot(
        name: String,
        description: String,
        district: String,
        address: String,
        is24Hours: Boolean,
        isTubeless: Boolean,
        forMotor: Boolean,
        forMobil: Boolean,
        canCallOut: Boolean,
        mapsUrl: String = "",
        phoneNumber: String = "",
        whatsappNumber: String = "",
        photoUri: Uri? = null,
        context: Context? = null,
        onComplete: (Boolean) -> Unit = {},
    ) {
        if (_isSubmitting.value || isCurrentUserFrozen()) return

        val appCtx = context?.applicationContext
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val preparedPhoto = if (photoUri != null && appCtx != null) {
                    ImageUploadHelper.preparePhotoFromUri(
                        context = appCtx,
                        uri = photoUri,
                        prefix = "tambalban",
                        maxDimensionPx = 1024,
                        initialJpegQuality = 80,
                    )
                } else null

                if (preparedPhoto != null && !verifyPhotosAreSafe(listOf(preparedPhoto))) {
                    onComplete(false)
                    return@launch
                }

                repository.addTambalBanSpot(
                    name = name,
                    description = description,
                    district = district,
                    address = address,
                    is24Hours = is24Hours,
                    isTubeless = isTubeless,
                    forMotor = forMotor,
                    forMobil = forMobil,
                    canCallOut = canCallOut,
                    mapsUrl = mapsUrl,
                    phoneNumber = phoneNumber,
                    whatsappNumber = whatsappNumber,
                    photo = preparedPhoto,
                )
                if (appCtx != null) {
                    TambalBanDraftManager.clearDraft(appCtx)
                }
                dismissSheet()
                notifyMessage("✅ Lokasi tambal ban berhasil ditambahkan! Salam Satu Aspal, matur nuwun dulur!")
                onComplete(true)
            } catch (e: Exception) {
                notifyMessage("Gagal menambahkan tambal ban: ${e.message}")
                onComplete(false)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun saveTambalBanDraft(context: Context, draft: TambalBanDraft) {
        TambalBanDraftManager.saveDraft(context, draft)
        notifyMessage("Draf tambal ban berhasil disimpan.")
    }

    fun clearTambalBanDraft(context: Context) {
        TambalBanDraftManager.clearDraft(context)
        notifyMessage("Draf tambal ban dibersihkan.")
    }

    fun updateTambalBanSpot(
        spotId: String,
        name: String,
        description: String,
        district: String,
        address: String,
        is24Hours: Boolean,
        isTubeless: Boolean,
        forMotor: Boolean,
        forMobil: Boolean,
        canCallOut: Boolean,
        mapsUrl: String = "",
        phoneNumber: String = "",
        whatsappNumber: String = "",
        photoUri: Uri? = null,
        context: Context? = null,
        onComplete: (Boolean) -> Unit = {},
    ) {
        if (_isSubmitting.value || isCurrentUserFrozen()) return

        val appCtx = context?.applicationContext
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val preparedPhoto = if (photoUri != null && appCtx != null) {
                    ImageUploadHelper.preparePhotoFromUri(
                        context = appCtx,
                        uri = photoUri,
                        prefix = "tambalban_edit",
                        maxDimensionPx = 1024,
                        initialJpegQuality = 80,
                    )
                } else null

                if (preparedPhoto != null && !verifyPhotosAreSafe(listOf(preparedPhoto))) {
                    onComplete(false)
                    return@launch
                }

                repository.updateTambalBanSpot(
                    spotId = spotId,
                    name = name,
                    description = description,
                    district = district,
                    address = address,
                    is24Hours = is24Hours,
                    isTubeless = isTubeless,
                    forMotor = forMotor,
                    forMobil = forMobil,
                    canCallOut = canCallOut,
                    mapsUrl = mapsUrl,
                    phoneNumber = phoneNumber,
                    whatsappNumber = whatsappNumber,
                    photo = preparedPhoto,
                )
                dismissSheet()
                notifyMessage("✅ Lokasi tambal ban berhasil diperbarui! Salam Satu Aspal!")
                onComplete(true)
            } catch (e: Exception) {
                notifyMessage("Gagal memperbarui tambal ban: ${e.message}")
                onComplete(false)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun verifyTambalBan(spotId: String) {
        viewModelScope.launch {
            try {
                val added = repository.verifyTambalBanSpot(spotId)
                notifyMessage(
                    if (added) "✅ Terima kasih telah memvalidasi tambal ban ini! Info sangat berguna bagi sesama pengendara."
                    else "Validasi Anda telah dibatalkan."
                )
            } catch (e: Exception) {
                notifyMessage("Gagal memvalidasi: ${e.message}")
            }
        }
    }

    fun submitTambalBanComment(spotId: String, text: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.addTambalBanComment(spotId, text)
                notifyMessage("Pantauan warga berhasil dikirim.")
                onComplete(true)
            } catch (e: Exception) {
                notifyMessage("Gagal mengirim pantauan: ${e.message}")
                onComplete(false)
            }
        }
    }

    fun deleteTambalBan(spotId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTambalBanSpot(spotId, currentUserId, isAdmin.value)
                dismissSheet()
                notifyMessage("Lokasi tambal ban berhasil dihapus.")
            } catch (e: Exception) {
                notifyMessage("Gagal menghapus tambal ban: ${e.message}")
            }
        }
    }

    // --- BARKAS WARGA (JUAL BELI BARANG BEKAS) OPERATIONS ---

    fun openBarkasDetail(barkasId: String) {
        _activeSheet.value = ActiveBottomSheet.BarkasDetail(barkasId)
    }

    fun openCreateBarkas() {
        if (isCurrentUserFrozen()) return
        _activeSheet.value = ActiveBottomSheet.CreateBarkas
    }

    fun submitBarkasItem(
        title: String,
        category: String,
        itemCondition: String,
        price: Long,
        isNegotiable: Boolean,
        district: String,
        codLocation: String,
        description: String,
        whatsappNumber: String = "",
        photoUris: List<Uri> = emptyList(),
        context: Context? = null,
        onComplete: (Boolean) -> Unit = {},
    ) {
        if (_isSubmitting.value || isCurrentUserFrozen()) return

        val appCtx = context?.applicationContext
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val rateCheck = SurabayaSpamProtector.checkRateLimit(
                    userId = currentUserId,
                    category = "barkas_items",
                    cooldownMs = SurabayaSpamProtector.COOLDOWN_SERVICES_MS,
                )
                if (rateCheck is SpamCheckResult.Blocked) {
                    notifyMessage("⚠️ ${rateCheck.reason}")
                    onComplete(false)
                    return@launch
                }

                val preparedPhotos = if (photoUris.isNotEmpty() && appCtx != null) {
                    photoUris.take(4).mapNotNull { uri ->
                        ImageUploadHelper.preparePhotoFromUri(
                            context = appCtx,
                            uri = uri,
                            prefix = "barkas",
                            maxDimensionPx = 1080,
                            initialJpegQuality = 82,
                        )
                    }
                } else {
                    emptyList()
                }

                if (photoUris.isNotEmpty() && preparedPhotos.isEmpty()) {
                    notifyMessage("Foto barang gagal diproses, silakan coba lagi.")
                    onComplete(false)
                    return@launch
                }

                if (preparedPhotos.isNotEmpty() && !verifyPhotosAreSafe(preparedPhotos)) {
                    onComplete(false)
                    return@launch
                }

                repository.createBarkasItem(
                    sellerName = currentDisplayName(),
                    title = title,
                    category = category,
                    itemCondition = itemCondition,
                    price = price,
                    isNegotiable = isNegotiable,
                    district = district,
                    codLocation = codLocation,
                    description = description,
                    whatsappNumber = whatsappNumber,
                    photos = preparedPhotos,
                )
                SurabayaSpamProtector.recordSubmission(currentUserId, "barkas_items")
                dismissSheet()
                notifyMessage("✅ Barang bekas berhasil dipasang di Barkas Warga Surabaya!")
                onComplete(true)
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: DuplicateContentException) {
                notifyMessage("⚠️ ${e.userFriendlyMessage}")
                onComplete(false)
            } catch (e: SpamRateLimitException) {
                notifyMessage("⚠️ ${e.userFriendlyMessage}")
                onComplete(false)
            } catch (e: Exception) {
                notifyMessage("Gagal memasang barang bekas: ${e.message}")
                onComplete(false)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun toggleBarkasSold(barkasId: String, explicitSold: Boolean? = null) {
        viewModelScope.launch {
            try {
                val nowSold = repository.toggleBarkasSoldStatus(
                    barkasId = barkasId,
                    requesterUid = currentUserId,
                    isAdmin = isAdmin.value,
                    explicitSold = explicitSold,
                )
                notifyMessage(
                    if (nowSold) "✅ Lapak berhasil ditandai TERJUAL!"
                    else "Lapak dibuka kembali dan berstatus TERSEDIA.",
                )
            } catch (e: Exception) {
                notifyMessage("Gagal mengubah status barang: ${e.message}")
            }
        }
    }

    fun submitBarkasComment(barkasId: String, text: String, onComplete: (Boolean) -> Unit = {}) {
        if (isCurrentUserFrozen()) return
        viewModelScope.launch {
            try {
                repository.addBarkasComment(barkasId, text)
                notifyMessage("Komentar berhasil dikirim.")
                onComplete(true)
            } catch (e: Exception) {
                notifyMessage("Gagal mengirim komentar: ${e.message}")
                onComplete(false)
            }
        }
    }

    fun reportBarkasContent(item: BarkasItem) {
        viewModelScope.launch {
            try {
                repository.reportBarkasContent(item.id, currentUserId)
                notifyMessage("Terima kasih. Laporan kiriman Barkas telah diteruskan ke Pusat Kendali Admin.")
            } catch (e: Exception) {
                notifyMessage("Gagal mengirim laporan: ${e.message}")
            }
        }
    }

    fun deleteBarkasItem(barkasId: String) {
        viewModelScope.launch {
            try {
                repository.deleteBarkasItem(barkasId, currentUserId, isAdmin.value)
                dismissSheet()
                notifyMessage("Lapak barang bekas berhasil dihapus.")
            } catch (e: Exception) {
                notifyMessage("Gagal menghapus lapak: ${e.message}")
            }
        }
    }

    fun chatWithBarkasSeller(item: BarkasItem) {
        if (item.sellerId.isBlank() || item.sellerId == currentUserId) return
        val sellerProfile = NearbyProfile(
            id = item.sellerId,
            userId = item.sellerId,
            displayName = item.sellerName.ifBlank { "Penjual Surabaya" },
            district = item.district.ifBlank { "Surabaya" },
        )
        greetNearbyPeer(sellerProfile)
    }

    // --- INFO KOS & KONTRAK (KOS BULANAN & KONTRAKAN TAHUNAN) OPERATIONS ---

    fun openKosDetail(kosId: String) {
        _activeSheet.value = ActiveBottomSheet.KosDetail(kosId)
    }

    fun openCreateKos() {
        if (isCurrentUserFrozen()) return
        _activeSheet.value = ActiveBottomSheet.CreateKos
    }

    fun submitKosListing(
        title: String,
        propertyType: String,
        billingPeriod: String,
        price: Long,
        availableRooms: Int,
        facilities: List<String>,
        district: String,
        address: String,
        mapsUrl: String = "",
        whatsappNumber: String = "",
        description: String,
        photoUris: List<Uri> = emptyList(),
        context: Context? = null,
        onComplete: (Boolean) -> Unit = {},
    ) {
        if (_isSubmitting.value || isCurrentUserFrozen()) return

        val appCtx = context?.applicationContext
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val rateCheck = SurabayaSpamProtector.checkRateLimit(
                    userId = currentUserId,
                    category = "kos_listings",
                    cooldownMs = SurabayaSpamProtector.COOLDOWN_SERVICES_MS,
                )
                if (rateCheck is SpamCheckResult.Blocked) {
                    notifyMessage("⚠️ ${rateCheck.reason}")
                    onComplete(false)
                    return@launch
                }

                val preparedPhotos = if (photoUris.isNotEmpty() && appCtx != null) {
                    photoUris.take(4).mapNotNull { uri ->
                        ImageUploadHelper.preparePhotoFromUri(
                            context = appCtx,
                            uri = uri,
                            prefix = "kos",
                            maxDimensionPx = 1080,
                            initialJpegQuality = 82,
                        )
                    }
                } else {
                    emptyList()
                }

                if (photoUris.isNotEmpty() && preparedPhotos.isEmpty()) {
                    notifyMessage("Foto kos/kontrakan gagal diproses, silakan coba lagi.")
                    onComplete(false)
                    return@launch
                }

                if (preparedPhotos.isNotEmpty() && !verifyPhotosAreSafe(preparedPhotos)) {
                    onComplete(false)
                    return@launch
                }

                repository.createKosListing(
                    ownerName = currentDisplayName(),
                    title = title,
                    propertyType = propertyType,
                    billingPeriod = billingPeriod,
                    price = price,
                    availableRooms = availableRooms,
                    facilities = facilities,
                    district = district,
                    address = address,
                    mapsUrl = mapsUrl,
                    whatsappNumber = whatsappNumber,
                    description = description,
                    photos = preparedPhotos,
                )
                SurabayaSpamProtector.recordSubmission(currentUserId, "kos_listings")
                dismissSheet()
                notifyMessage("✅ Info kos/kontrakan berhasil dipasang untuk warga Surabaya!")
                onComplete(true)
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: DuplicateContentException) {
                notifyMessage("⚠️ ${e.userFriendlyMessage}")
                onComplete(false)
            } catch (e: SpamRateLimitException) {
                notifyMessage("⚠️ ${e.userFriendlyMessage}")
                onComplete(false)
            } catch (e: Exception) {
                notifyMessage("Gagal memasang info kos/kontrakan: ${e.message}")
                onComplete(false)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun toggleKosFull(kosId: String, explicitFull: Boolean? = null) {
        viewModelScope.launch {
            try {
                val nowFull = repository.toggleKosFullStatus(
                    kosId = kosId,
                    requesterUid = currentUserId,
                    isAdmin = isAdmin.value,
                    explicitFull = explicitFull,
                )
                notifyMessage(
                    if (nowFull) "✅ Status berhasil diubah menjadi PENUH / TERSEWA."
                    else "Status dibuka kembali dan berstatus TERSEDIA.",
                )
            } catch (e: Exception) {
                notifyMessage("Gagal mengubah status ketersediaan: ${e.message}")
            }
        }
    }

    fun submitKosComment(kosId: String, text: String, onComplete: (Boolean) -> Unit = {}) {
        if (isCurrentUserFrozen()) return
        viewModelScope.launch {
            try {
                repository.addKosComment(kosId, text)
                notifyMessage("Pertanyaan/komentar berhasil dikirim.")
                onComplete(true)
            } catch (e: Exception) {
                notifyMessage("Gagal mengirim komentar: ${e.message}")
                onComplete(false)
            }
        }
    }

    fun reportKosContent(item: KosKontrakItem) {
        viewModelScope.launch {
            try {
                repository.reportKosContent(item.id, currentUserId)
                notifyMessage("Terima kasih. Laporan info kos/kontrakan telah diteruskan ke Pusat Kendali Admin.")
            } catch (e: Exception) {
                notifyMessage("Gagal mengirim laporan: ${e.message}")
            }
        }
    }

    fun reportKosLink(item: KosKontrakItem) {
        viewModelScope.launch {
            try {
                repository.reportKosLink(item.id, currentUserId)
                notifyMessage("Laporan tautan peta mencurigakan telah dikirim ke sistem keamanan Admin.")
            } catch (e: Exception) {
                notifyMessage("Gagal melaporkan tautan: ${e.message}")
            }
        }
    }

    fun deleteKosListing(kosId: String) {
        viewModelScope.launch {
            try {
                repository.deleteKosListing(kosId, currentUserId, isAdmin.value)
                dismissSheet()
                notifyMessage("Info kos/kontrakan berhasil dihapus.")
            } catch (e: Exception) {
                notifyMessage("Gagal menghapus info kos/kontrakan: ${e.message}")
            }
        }
    }

    fun chatWithKosOwner(item: KosKontrakItem) {
        if (item.ownerId.isBlank() || item.ownerId == currentUserId) return
        val ownerProfile = NearbyProfile(
            id = item.ownerId,
            userId = item.ownerId,
            displayName = item.ownerName.ifBlank { "Pemilik Kos Surabaya" },
            district = item.district.ifBlank { "Surabaya" },
        )
        greetNearbyPeer(ownerProfile)
    }

    fun saveHomeShortcutOrder(orderedIds: List<String>) {
        if (!isAdmin.value) {
            notifyMessage("Hanya Admin resmi yang dapat mengatur tata letak menu cepat.")
            return
        }
        viewModelScope.launch {
            try {
                repository.saveHomeShortcutOrder(orderedIds)
                val isDefault = HomeShortcutIds.normalizeOrder(orderedIds) == HomeShortcutIds.DEFAULT_ORDER
                notifyMessage(
                    if (isDefault) {
                        "Tata letak Menu Cepat dikembalikan ke urutan standar untuk semua warga."
                    } else {
                        "Tata letak Menu Cepat berhasil disimpan dan diterapkan untuk semua warga."
                    },
                )
            } catch (e: Exception) {
                notifyMessage("Gagal menyimpan tata letak menu: ${e.message}")
            }
        }
    }

    private companion object {
        const val TAG = "SurabayaVM"
        const val STOP_TIMEOUT = 5000L
        const val GROUP_PIN_PREFS_NAME = "surabaya_group_pin_prefs"
        const val KEY_UNLOCKED_GROUP_PINS = "unlocked_group_pin_hashes"
    }
}
