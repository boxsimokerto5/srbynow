package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.location.LocationHelper
import com.example.data.model.BookmarkItem
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRoom
import com.example.data.model.CulinaryReview
import com.example.data.model.CulinarySpot
import com.example.data.model.IncidentComment
import com.example.data.model.IncidentReport
import com.example.data.model.NearbyProfile
import com.example.data.model.SurabayaEvent
import com.example.data.model.UiState
import com.example.data.model.UserAccount
import com.example.data.repository.SurabayaRepository
import com.example.data.util.SurabayaDistricts
import com.example.data.util.calculateDistanceKm
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
import kotlinx.coroutines.launch

enum class MainTab {
    KEJADIAN,
    EVENT,
    KULINER,
    SEKITAR,
    CHAT,
}

sealed interface ActiveBottomSheet {
    data class IncidentDetail(val incidentId: String) : ActiveBottomSheet
    data object CreateIncident : ActiveBottomSheet
    data class EventDetail(val eventId: String) : ActiveBottomSheet
    data object ProposeEvent : ActiveBottomSheet
    data class CulinaryDetail(val culinaryId: String) : ActiveBottomSheet
    data object CreateCulinary : ActiveBottomSheet
    data object GpsPrivacySettings : ActiveBottomSheet
    data object BookmarksList : ActiveBottomSheet
    data object ProfileAndAdmin : ActiveBottomSheet
    data object StartDirectChat : ActiveBottomSheet
    data class ChatConversation(val room: ChatRoom) : ActiveBottomSheet
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
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(MainTab.KEJADIAN)
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

    // User account & Admin streams
    val userAccount: StateFlow<UserAccount?> = repository.observeUserAccount(currentUserId)
        .catch { e ->
            Log.w(TAG, "observeUserAccount error", e)
            emit(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), null)

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

    // 1. Kejadian Surabaya
    val incidentsState: StateFlow<UiState<List<IncidentReport>>> = repository.observeIncidents()
        .map<List<IncidentReport>, UiState<List<IncidentReport>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "observeIncidents error", e)
            emit(UiState.Error(e.localizedMessage ?: "Gagal memuat kejadian Surabaya"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    private val selectedIncidentIdFlow = _activeSheet.map { (it as? ActiveBottomSheet.IncidentDetail)?.incidentId }

    val activeIncidentComments: StateFlow<List<IncidentComment>> = selectedIncidentIdFlow
        .flatMapLatest { incidentId ->
            if (incidentId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeIncidentComments(incidentId)
                    .catch { emit(emptyList()) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    // 2. Event Surabaya & Moderation
    val approvedEventsState: StateFlow<UiState<List<SurabayaEvent>>> = repository.observeApprovedEvents()
        .map<List<SurabayaEvent>, UiState<List<SurabayaEvent>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "observeApprovedEvents error", e)
            emit(UiState.Error(e.localizedMessage ?: "Gagal memuat agenda event"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    val pendingEvents: StateFlow<List<SurabayaEvent>> = repository.observePendingEvents()
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
    val culinaryState: StateFlow<UiState<List<CulinarySpot>>> = repository.observeCulinarySpots()
        .map<List<CulinarySpot>, UiState<List<CulinarySpot>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "observeCulinarySpots error", e)
            emit(UiState.Error(e.localizedMessage ?: "Gagal memuat daftar kuliner"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    private val selectedCulinaryIdFlow = _activeSheet.map { (it as? ActiveBottomSheet.CulinaryDetail)?.culinaryId }

    val activeCulinaryReviews: StateFlow<List<CulinaryReview>> = selectedCulinaryIdFlow
        .flatMapLatest { culinaryId ->
            if (culinaryId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeCulinaryReviews(culinaryId)
                    .catch { emit(emptyList()) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    // 4. Teman Sekitar (Nearby Peers sorted & filtered by radius)
    val nearbyPeersState: StateFlow<UiState<List<NearbyPeerItem>>> = combine(
        repository.observeDiscoverableProfiles(),
        _userLat,
        _userLng,
        userAccount,
    ) { profiles, lat, lng, account ->
        val radiusKm = account?.searchRadiusKm ?: 15.0
        val peers = profiles
            .filter { it.id != currentUserId }
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

    val allDiscoverableCitizens: StateFlow<List<NearbyProfile>> = repository.observeDiscoverableProfiles()
        .map { list -> list.filter { it.id != currentUserId }.sortedBy { it.displayName.lowercase() } }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    // 5. Chat Rooms & Messages
    val publicChatRooms: StateFlow<List<ChatRoom>> = repository.observePublicChatRooms()
        .catch { e ->
            Log.w(TAG, "observePublicChatRooms error", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    val directChatRooms: StateFlow<List<ChatRoom>> = repository.observeDirectChatRooms(currentUserId)
        .catch { e ->
            Log.w(TAG, "observeDirectChatRooms error", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    private val activeChatRoomFlow = _activeSheet.map { (it as? ActiveBottomSheet.ChatConversation)?.room }

    val activeChatMessages: StateFlow<List<ChatMessage>> = activeChatRoomFlow
        .flatMapLatest { room ->
            if (room == null) {
                flowOf(emptyList())
            } else {
                repository.observeMessages(room, currentUserId)
                    .catch { emit(emptyList()) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

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

    fun saveProfileAndPrivacy(
        displayName: String,
        district: String,
        bio: String,
        interests: String,
        searchRadiusKm: Double,
        isDiscoverable: Boolean,
    ) {
        viewModelScope.launch {
            try {
                val (districtLat, districtLng) = if (_isLiveGpsActive.value) {
                    _userLat.value to _userLng.value
                } else {
                    SurabayaDistricts.coordinateFor(district)
                }
                _userLat.value = districtLat
                _userLng.value = districtLng
                repository.updateProfileAndPrivacy(
                    displayName = displayName,
                    district = district,
                    bio = bio,
                    interests = interests,
                    searchRadiusKm = searchRadiusKm,
                    isDiscoverable = isDiscoverable,
                    lat = districtLat,
                    lng = districtLng,
                )
                notifyMessage("Pengaturan profil & privasi disimpan")
                dismissSheet()
            } catch (e: Exception) {
                notifyMessage("Gagal menyimpan pengaturan: ${e.localizedMessage ?: "Error"}")
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

    fun submitIncident(
        title: String,
        description: String,
        category: String,
        severity: String,
        district: String,
        address: String,
    ) {
        viewModelScope.launch {
            try {
                repository.createIncident(
                    authorName = currentDisplayName(),
                    title = title,
                    description = description,
                    category = category,
                    severity = severity,
                    district = district,
                    address = address,
                )
                dismissSheet()
                notifyMessage("Laporan kejadian berhasil dipublikasikan")
            } catch (e: Exception) {
                notifyMessage("Gagal mengirim laporan: ${e.localizedMessage ?: ""}")
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
        if (text.isBlank()) return
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
    ) {
        viewModelScope.launch {
            try {
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
                )
                dismissSheet()
                notifyMessage("Usulan event dikirim! Menunggu persetujuan Admin.")
            } catch (e: Exception) {
                notifyMessage("Gagal mengusulkan event: ${e.localizedMessage ?: ""}")
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
    ) {
        viewModelScope.launch {
            try {
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
                )
                dismissSheet()
                notifyMessage("Rekomendasi kuliner berhasil ditambahkan!")
            } catch (e: Exception) {
                notifyMessage("Gagal menambah kuliner")
            }
        }
    }

    fun submitCulinaryReview(spot: CulinarySpot, rating: Int, comment: String) {
        if (comment.trim().length < 2) return
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
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                repository.sendMessage(room, currentDisplayName(), text)
            } catch (e: Exception) {
                notifyMessage("Gagal mengirim pesan")
            }
        }
    }

    private companion object {
        const val TAG = "SurabayaVM"
        const val STOP_TIMEOUT = 5000L
    }
}
