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
import com.example.data.model.BookmarkItem
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRoom
import com.example.data.model.CulinaryReview
import com.example.data.model.CulinarySpot
import com.example.data.model.DestinationReview
import com.example.data.model.IncidentComment
import com.example.data.model.IncidentReport
import com.example.data.model.MomentComment
import com.example.data.model.NearbyProfile
import com.example.data.model.SurabayaDestination
import com.example.data.model.SurabayaEvent
import com.example.data.model.UiState
import com.example.data.model.UserAccount
import com.example.data.model.UserMoment
import com.example.data.repository.SurabayaRepository
import com.example.data.util.GeminiExplicitImageScanner
import com.example.data.util.ImageSafetyScanResult
import com.example.data.util.ImageSafetyScanner
import com.example.data.util.ImageUploadHelper
import com.example.data.util.PreparedPhoto
import com.example.data.util.SurabayaDistricts
import com.example.data.util.SurabayaNotificationHelper
import com.example.data.util.calculateDistanceKm
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.example.data.model.CitizenService
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
}

sealed interface ActiveBottomSheet {
    data class IncidentDetail(val incidentId: String) : ActiveBottomSheet
    data object CreateIncident : ActiveBottomSheet
    data class EventDetail(val eventId: String) : ActiveBottomSheet
    data object ProposeEvent : ActiveBottomSheet
    data class CulinaryDetail(val culinaryId: String) : ActiveBottomSheet
    data object CreateCulinary : ActiveBottomSheet
    data class MomentDetail(val momentId: String) : ActiveBottomSheet
    data object CreateMoment : ActiveBottomSheet
    data class DestinationDetail(val destinationId: String) : ActiveBottomSheet
    data object CreateDestination : ActiveBottomSheet
    data object GpsPrivacySettings : ActiveBottomSheet
    data object BookmarksList : ActiveBottomSheet
    data object Notifications : ActiveBottomSheet
    data object ProfileAndAdmin : ActiveBottomSheet
    data object StartDirectChat : ActiveBottomSheet
    data class ChatConversation(val room: ChatRoom) : ActiveBottomSheet
    data object RadioPlayer : ActiveBottomSheet
    data class ServiceDetail(val serviceId: String) : ActiveBottomSheet
    data object CreateService : ActiveBottomSheet
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

    // User account & Admin streams
    val userAccount: StateFlow<UserAccount?> = repository.observeUserAccount(currentUserId)
        .catch { e ->
            Log.w(TAG, "observeUserAccount error", e)
            emit(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), null)

    val blockedUserIds: StateFlow<Set<String>> = userAccount
        .map { it?.blockedUids?.toSet().orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptySet())

    val isAdmin: StateFlow<Boolean> = repository.observeAdminRole(currentUserId)
        .catch { e ->
            Log.w(TAG, "observeAdminRole error", e)
            emit(false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), false)

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

    // 3. Kuliner Surabaya
    val culinaryState: StateFlow<UiState<List<CulinarySpot>>> = combine(
        repository.observeCulinarySpots(),
        blockedUserIds,
        isAdmin,
    ) { items, blockedSet, admin ->
        val filtered = items.filter { item ->
            item.createdBy !in blockedSet && (!item.isContentQuarantined || admin)
        }
        UiState.Success(filtered) as UiState<List<CulinarySpot>>
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

    val myNearbyProfile: StateFlow<NearbyProfile?> = repository.observeDiscoverableProfiles()
        .map { list -> list.firstOrNull { it.id == currentUserId } }
        .catch { emit(null) }
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

    val directChatRooms: StateFlow<List<ChatRoom>> = combine(
        repository.observeDirectChatRooms(currentUserId),
        blockedUserIds,
    ) { rooms, blockedSet ->
        rooms.filter { room ->
            val otherId = room.otherParticipantIdFor(currentUserId)
            otherId == null || otherId !in blockedSet
        }
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

    fun saveProfileAndPrivacy(
        displayName: String,
        district: String,
        bio: String,
        interests: String,
        searchRadiusKm: Double,
        isDiscoverable: Boolean,
        avatarUri: Uri? = null,
        context: Context? = null,
    ) {
        if (_isSubmitting.value || isCurrentUserFrozen()) return
        val appCtx = context?.applicationContext
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
                    displayName = displayName,
                    district = district,
                    bio = bio,
                    interests = interests,
                    searchRadiusKm = searchRadiusKm,
                    isDiscoverable = isDiscoverable,
                    lat = districtLat,
                    lng = districtLng,
                    avatarPhoto = preparedAvatar,
                    onPhotoSchemaMissing = { photoSchemaMissing = true },
                )
                dismissSheet()
                if (preparedAvatar != null && photoSchemaMissing) {
                    notifyMessage("Profil disimpan, namun kolom avatar belum di-import di PocketBase Admin.")
                } else if (preparedAvatar != null) {
                    notifyMessage("Foto profil & pengaturan berhasil diperbarui!")
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
            } catch (e: Exception) {
                notifyMessage("Gagal mengusulkan event: ${e.localizedMessage ?: "Periksa koneksi internet"}")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun moderateEvent(eventId: String, approve: Boolean, adminNote: String) {
        viewModelScope.launch {
            try {
                repository.moderateEvent(eventId, approve, adminNote)
                dismissSheet()
                notifyMessage(
                    if (approve) "Event disetujui & tayang di halaman Event Surabaya!"
                    else "Usulan event telah ditolak.",
                )
            } catch (e: Exception) {
                notifyMessage("Aksi moderasi gagal: pastikan Mode Admin aktif.")
            }
        }
    }

    fun toggleEventInterest(event: SurabayaEvent) {
        viewModelScope.launch {
            try {
                repository.toggleEventInterest(event)
            } catch (e: Exception) {
                notifyMessage("Gagal memperbarui minat event")
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
        context: Context? = null,
    ) {
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

    fun sendChatMessage(room: ChatRoom, text: String) {
        if (text.isBlank() || isCurrentUserFrozen()) return
        viewModelScope.launch {
            try {
                repository.sendMessage(room, currentDisplayName(), text)
            } catch (e: Exception) {
                notifyMessage("Gagal mengirim pesan")
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
                if (nextCount >= 3) {
                    if (!isAdmin.value) dismissSheet()
                    notifyMessage("Konten mencapai 3 laporan warga dan otomatis dikarantina dari feed publik!")
                } else {
                    notifyMessage("Laporan diterima ($nextCount/3). Foto otomatis diburamkan demi kenyamanan warga.")
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
                if (nextCount >= 3) {
                    if (!isAdmin.value) dismissSheet()
                    notifyMessage("Event mencapai 3 laporan warga dan otomatis dikarantina dari feed publik!")
                } else {
                    notifyMessage("Laporan diterima ($nextCount/3). Foto event otomatis diburamkan.")
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
                if (nextCount >= 3) {
                    if (!isAdmin.value) dismissSheet()
                    notifyMessage("Kuliner mencapai 3 laporan warga dan otomatis dikarantina dari feed publik!")
                } else {
                    notifyMessage("Laporan diterima ($nextCount/3). Foto kuliner otomatis diburamkan.")
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
        photoUris: List<Uri> = emptyList(),
        context: Context? = null,
    ) {
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
                    photos = preparedPhotos,
                )
                dismissSheet()
                notifyMessage("Momen berhasil dibagikan ke warga Surabaya!")
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
                dismissSheet()
                notifyMessage("Destinasi Surabaya berhasil ditambahkan oleh Admin!")
            } catch (e: Exception) {
                notifyMessage("Gagal menambahkan destinasi: ${e.message}")
            } finally {
                _isSubmitting.value = false
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
                if (nextCount >= 3) {
                    if (!isAdmin.value) dismissSheet()
                    notifyMessage("Momen mencapai 3 laporan warga dan dikarantina!")
                } else {
                    notifyMessage("Laporan diterima ($nextCount/3). Foto otomatis diburamkan.")
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
                if (nextCount >= 3) {
                    if (!isAdmin.value) dismissSheet()
                    notifyMessage("Destinasi mencapai 3 laporan warga dan dikarantina!")
                } else {
                    notifyMessage("Laporan diterima ($nextCount/3).")
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

    private companion object {
        const val TAG = "SurabayaVM"
        const val STOP_TIMEOUT = 5000L
    }
}
