package com.example

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.location.LocationHelper
import com.example.data.model.UiState
import com.example.data.model.isCitizenProfileComplete
import com.example.data.pocketbase.PocketBaseClient
import com.example.data.repository.SurabayaRepository
import com.example.data.util.GeminiExplicitImageScanner
import com.example.data.util.calculateDistanceKm
import com.example.data.util.configureAppCheck
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.MandatoryProfileSetupScreen
import com.example.ui.auth.OAuthRedirectBus
import com.example.ui.auth.performSignOut
import com.example.ui.components.SurabayaCompactTopBar
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CreatePublicGroupSheetContent
import com.example.ui.screens.DestinasiScreen
import com.example.ui.screens.EventScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.collectAdminFlaggedItems
import com.example.ui.screens.JasaWargaScreen
import com.example.ui.screens.BarkasScreen
import com.example.ui.screens.KosKontrakScreen
import com.example.ui.screens.KejadianScreen
import com.example.ui.screens.KulinerScreen
import com.example.ui.screens.MomenScreen
import com.example.ui.screens.PollingScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StartDirectChatSheetContent
import com.example.ui.screens.TambalBanScreen
import com.example.ui.screens.TemanSekitarScreen
import com.example.ui.screens.TransitScreen
import com.example.ui.screens.UnlockGroupPinDialog
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.data.model.AppNotificationType
import com.example.data.util.ChatMediaManager
import com.example.data.util.SurabayaNotificationHelper
import com.example.data.audio.RadioPlaybackState
import com.example.data.audio.SurabayaRadioPlayer
import com.example.ui.sheets.AdminPendingPollsSheetContent
import com.example.ui.sheets.BarkasDetailSheetContent
import com.example.ui.sheets.BookmarksSheetContent
import com.example.ui.sheets.ChatConversationSheetContent
import com.example.ui.sheets.CitizenProfileSheetContent
import com.example.ui.sheets.CreateBarkasSheetContent
import com.example.ui.sheets.CreateCulinarySheetContent
import com.example.ui.sheets.CreateDestinationSheetContent
import com.example.ui.sheets.CreateIncidentSheetContent
import com.example.ui.sheets.CreateKosSheetContent
import com.example.ui.sheets.CreateMomentSheetContent
import com.example.ui.sheets.CreatePollSheetContent
import com.example.ui.sheets.CreateServiceSheetContent
import com.example.ui.sheets.CreateTambalBanSheetContent
import com.example.ui.sheets.CulinaryDetailSheetContent
import com.example.ui.sheets.DestinationDetailSheetContent
import com.example.ui.sheets.EventDetailSheetContent
import com.example.ui.sheets.IncidentDetailSheetContent
import com.example.ui.sheets.KosDetailSheetContent
import com.example.ui.sheets.MomentDetailSheetContent
import com.example.ui.sheets.NotificationsSheetContent
import com.example.ui.sheets.PermissionDisclosureBottomSheet
import com.example.ui.sheets.PermissionSheetMode
import com.example.ui.sheets.PollDetailSheetContent
import com.example.ui.sheets.ProfileAndPrivacySheetContent
import com.example.ui.sheets.ProposeEventSheetContent
import com.example.ui.sheets.RadioPlayerSheetContent
import com.example.ui.sheets.ServiceDetailSheetContent
import com.example.ui.sheets.hasNotificationPermission
import com.example.ui.sheets.hasSeenInitialNotificationDisclosure
import com.example.ui.sheets.markInitialNotificationDisclosureSeen
import kotlinx.coroutines.delay
import com.example.ui.sheets.TambalBanDetailSheetContent
import com.example.ui.theme.SurabayaHariIniTheme
import com.example.ui.viewmodel.ActiveBottomSheet
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.SurabayaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        OAuthRedirectBus.handleIncomingIntent(intent)
        SurabayaNotificationHelper.createNotificationChannels(this)
        SurabayaNotificationHelper.handleIncomingIntent(intent)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                AndroidColor.WHITE,
                AndroidColor.WHITE,
            ),
        )
        setContent {
            SurabayaHariIniTheme {
                AppNavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        OAuthRedirectBus.handleIncomingIntent(intent)
        SurabayaNotificationHelper.handleIncomingIntent(intent)
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val pbClient = remember(context) { PocketBaseClient.getInstance(context) }
    val session by pbClient.sessionFlow.collectAsStateWithLifecycle()
    val activeSession = session
    var isSplashVisible by rememberSaveable { mutableStateOf(true) }

    Crossfade(
        targetState = isSplashVisible,
        label = "splash_crossfade",
    ) { showSplash ->
        if (showSplash) {
            SplashScreen(
                onSplashFinished = { isSplashVisible = false }
            )
        } else {
            if (activeSession == null) {
                AuthScreen(onAuthSuccess = {})
            } else {
                SurabayaMainHub(
                    currentUserId = activeSession.userId,
                    displayName = activeSession.displayName.ifBlank { "Warga Surabaya" },
                    email = activeSession.email,
                )
            }
        }
    }
}

private data class NavDestinationItem(
    val tab: MainTab,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
    val gameIconRes: Int? = null,
    val customLabel: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurabayaMainHub(
    currentUserId: String,
    displayName: String,
    email: String,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val viewModel: SurabayaViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                ChatMediaManager.init(app)
                val pbClient = PocketBaseClient.getInstance(app)
                SurabayaViewModel(
                    repository = SurabayaRepository(pbClient, app),
                    locationHelper = LocationHelper(app),
                    currentUserId = currentUserId,
                    initialDisplayName = displayName,
                    currentUserEmail = email,
                    imageSafetyScanner = GeminiExplicitImageScanner(app),
                    radioPlayer = SurabayaRadioPlayer.getInstance(app),
                )
            }
        },
    )

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val chatSubTab by viewModel.chatSubTab.collectAsStateWithLifecycle()
    val activeSheet by viewModel.activeSheet.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val radioPlaybackState by viewModel.radioPlaybackState.collectAsStateWithLifecycle()
    val radioVolume by viewModel.radioVolume.collectAsStateWithLifecycle()
    val radioErrorMessage by viewModel.radioErrorMessage.collectAsStateWithLifecycle()
    val radioStationInfo by viewModel.radioStationInfo.collectAsStateWithLifecycle()

    val userAccount by viewModel.userAccount.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotifCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val pendingNotifPayload by SurabayaNotificationHelper.pendingNotificationPayload.collectAsStateWithLifecycle()

    val isLiveGps by viewModel.isLiveGpsActive.collectAsStateWithLifecycle()
    val userLat by viewModel.userLat.collectAsStateWithLifecycle()
    val userLng by viewModel.userLng.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val isScanningPhoto by viewModel.isScanningPhoto.collectAsStateWithLifecycle()
    val isProfileCheckReady by viewModel.isProfileCheckReady.collectAsStateWithLifecycle()
    val myNearbyProfile by viewModel.myNearbyProfile.collectAsStateWithLifecycle()
    val allDiscoverableCitizens by viewModel.allDiscoverableCitizens.collectAsStateWithLifecycle()
    val cachedCitizenProfiles by viewModel.cachedCitizenProfiles.collectAsStateWithLifecycle()
    val combinedKnownCitizens = remember(allDiscoverableCitizens, cachedCitizenProfiles) {
        (allDiscoverableCitizens + cachedCitizenProfiles.values).distinctBy { it.userId.ifBlank { it.id } }
    }
    val isProfileComplete = isCitizenProfileComplete(userAccount, myNearbyProfile)

    val incidentsState by viewModel.incidentsState.collectAsStateWithLifecycle()
    val incidentComments by viewModel.activeIncidentComments.collectAsStateWithLifecycle()
    val eventComments by viewModel.activeEventComments.collectAsStateWithLifecycle()

    val approvedEventsState by viewModel.approvedEventsState.collectAsStateWithLifecycle()
    val pendingEvents by viewModel.pendingEvents.collectAsStateWithLifecycle()
    val myProposedEvents by viewModel.myProposedEvents.collectAsStateWithLifecycle()
    val pendingEventInterests by viewModel.pendingEventInterests.collectAsStateWithLifecycle()

    val culinaryState by viewModel.culinaryState.collectAsStateWithLifecycle()
    val culinaryReviews by viewModel.activeCulinaryReviews.collectAsStateWithLifecycle()

    val momentsState by viewModel.momentsState.collectAsStateWithLifecycle()
    val momentComments by viewModel.activeMomentComments.collectAsStateWithLifecycle()

    val destinationsState by viewModel.destinationsState.collectAsStateWithLifecycle()
    val destinationReviews by viewModel.activeDestinationReviews.collectAsStateWithLifecycle()

    val nearbyPeersState by viewModel.nearbyPeersState.collectAsStateWithLifecycle()
    val publicChatRooms by viewModel.publicChatRooms.collectAsStateWithLifecycle()
    val directChatRooms by viewModel.directChatRooms.collectAsStateWithLifecycle()
    val activeChatMessages by viewModel.activeChatMessages.collectAsStateWithLifecycle()
    val isSendingChatMessage by viewModel.isSendingChatMessage.collectAsStateWithLifecycle()
    LaunchedEffect(directChatRooms, currentUserId) {
        for (room in directChatRooms) {
            val peerUid = room.otherParticipantIdFor(currentUserId)
                ?: room.roomKey.removePrefix("direct_").split("_").firstOrNull { it.isNotBlank() && it != currentUserId }
            if (peerUid != null) {
                viewModel.preloadCitizenProfile(peerUid)
            }
        }
    }
    val servicesState by viewModel.servicesState.collectAsStateWithLifecycle()
    val approvedPollsState by viewModel.approvedPollsState.collectAsStateWithLifecycle()
    val pendingPollsState by viewModel.pendingPollsState.collectAsStateWithLifecycle()
    val activeSelectedPoll by viewModel.activeSelectedPoll.collectAsStateWithLifecycle()
    val activePollComments by viewModel.activePollComments.collectAsStateWithLifecycle()
    val myProposedPolls by viewModel.myProposedPollsState.collectAsStateWithLifecycle()
    val tambalBanSpotsState by viewModel.tambalBanSpotsState.collectAsStateWithLifecycle()
    val activeSelectedTambalBan by viewModel.activeSelectedTambalBan.collectAsStateWithLifecycle()
    val activeTambalBanComments by viewModel.activeTambalBanComments.collectAsStateWithLifecycle()
    val barkasItemsState by viewModel.barkasItemsState.collectAsStateWithLifecycle()
    val activeSelectedBarkas by viewModel.activeSelectedBarkas.collectAsStateWithLifecycle()
    val activeBarkasComments by viewModel.activeBarkasComments.collectAsStateWithLifecycle()
    val kosListingsState by viewModel.kosListingsState.collectAsStateWithLifecycle()
    val activeSelectedKos by viewModel.activeSelectedKos.collectAsStateWithLifecycle()
    val activeKosComments by viewModel.activeKosComments.collectAsStateWithLifecycle()
    val bannedProfiles by viewModel.bannedProfiles.collectAsStateWithLifecycle()
    val homeShortcutOrder by viewModel.homeShortcutOrder.collectAsStateWithLifecycle()
    val homeBannerConfig by viewModel.homeBannerConfig.collectAsStateWithLifecycle()
    val unlockedGroupPinHashes by viewModel.unlockedGroupPinHashes.collectAsStateWithLifecycle()
    val pendingPinUnlockRoom by viewModel.pendingPinUnlockRoom.collectAsStateWithLifecycle()
    val myActivePublicGroupCount by viewModel.myActivePublicGroupCount.collectAsStateWithLifecycle()

    val pendingPollsList = (pendingPollsState as? UiState.Success)?.data.orEmpty()
    val flaggedItems = remember(
        incidentsState,
        approvedEventsState,
        pendingEvents,
        culinaryState,
        momentsState,
        destinationsState,
        servicesState,
        barkasItemsState,
        kosListingsState,
    ) {
        collectAdminFlaggedItems(
            incidents = (incidentsState as? UiState.Success)?.data.orEmpty(),
            approvedEvents = (approvedEventsState as? UiState.Success)?.data.orEmpty(),
            pendingEvents = pendingEvents,
            culinarySpots = (culinaryState as? UiState.Success)?.data.orEmpty(),
            moments = (momentsState as? UiState.Success)?.data.orEmpty(),
            destinations = (destinationsState as? UiState.Success)?.data.orEmpty(),
            services = (servicesState as? UiState.Success)?.data.orEmpty(),
            barkasItems = (barkasItemsState as? UiState.Success)?.data.orEmpty(),
            kosListings = (kosListingsState as? UiState.Success)?.data.orEmpty(),
        )
    }
    val adminAttentionCount = pendingEvents.size + pendingPollsList.size + flaggedItems.size

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            viewModel.loadSavedUnlockedGroupPins(context)
        }
    }

    LaunchedEffect(approvedEventsState) {
        val events = (approvedEventsState as? com.example.data.model.UiState.Success)?.data
        if (!events.isNullOrEmpty()) {
            viewModel.syncApprovedEventsToDirectChatAndGallery(context, events)
        }
    }

    val pinTargetRoom = pendingPinUnlockRoom
    if (pinTargetRoom != null) {
        UnlockGroupPinDialog(
            room = pinTargetRoom,
            onDismiss = { viewModel.dismissPinUnlockDialog() },
            onVerifyPin = { enteredPin ->
                viewModel.verifyAndUnlockChatRoom(pinTargetRoom, enteredPin, context)
            },
        )
    }

    // Android 13+ Notification Permission Slide-Up Disclosure (only after mandatory profile is complete)
    var showInitialNotifDisclosure by remember { mutableStateOf(false) }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notifPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                Log.d("MainActivity", "POST_NOTIFICATIONS granted: $isGranted")
            },
        )
        LaunchedEffect(isProfileComplete) {
            if (isProfileComplete && !hasNotificationPermission(context) && !hasSeenInitialNotificationDisclosure(context)) {
                delay(650)
                if (activeSheet == null && !hasNotificationPermission(context)) {
                    showInitialNotifDisclosure = true
                }
            }
        }
        if (isProfileComplete && showInitialNotifDisclosure && activeSheet == null) {
            PermissionDisclosureBottomSheet(
                mode = PermissionSheetMode.Notification(showSettingsShortcut = false),
                onDismiss = {
                    markInitialNotificationDisclosureSeen(context)
                    showInitialNotifDisclosure = false
                },
                onConfirmNotificationRequest = {
                    markInitialNotificationDisclosureSeen(context)
                    showInitialNotifDisclosure = false
                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                },
            )
        }
    }

    // Deep-link intent observer from Heads-up System Notification tap
    LaunchedEffect(pendingNotifPayload) {
        val payload = pendingNotifPayload ?: return@LaunchedEffect
        SurabayaNotificationHelper.consumePendingPayload()
        when (payload.type) {
            AppNotificationType.CHAT -> {
                viewModel.selectTab(MainTab.CHAT)
                val targetRoom = (publicChatRooms + directChatRooms).firstOrNull { it.id == payload.targetId || it.roomKey == payload.targetId }
                if (targetRoom != null) {
                    viewModel.requestOpenChatRoom(targetRoom)
                }
            }
            AppNotificationType.EVENT -> {
                viewModel.selectTab(MainTab.EVENT)
                if (payload.targetId.isNotBlank()) {
                    viewModel.openSheet(ActiveBottomSheet.EventDetail(payload.targetId))
                }
            }
            AppNotificationType.INCIDENT -> {
                viewModel.selectTab(MainTab.KEJADIAN)
                if (payload.targetId.isNotBlank()) {
                    viewModel.openSheet(ActiveBottomSheet.IncidentDetail(payload.targetId))
                }
            }
            AppNotificationType.SYSTEM -> {
                viewModel.openSheet(ActiveBottomSheet.Notifications)
            }
        }
    }

    LaunchedEffect(snackbarMsg) {
        val msg = snackbarMsg
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    if (!isProfileComplete && !isProfileCheckReady) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("profile_check_loading"),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(30.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.mandatory_profile_loading_check),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    if (!isProfileComplete) {
        MandatoryProfileSetupScreen(
            userAccount = userAccount,
            nearbyProfile = myNearbyProfile,
            userEmail = email,
            isSubmitting = isSubmitting,
            isScanningPhoto = isScanningPhoto,
            snackbarHostState = snackbarHostState,
            onSubmitProfile = { fullName, gender, district, avatarUri ->
                viewModel.completeMandatoryProfile(
                    displayName = fullName,
                    gender = gender,
                    district = district,
                    avatarUri = avatarUri,
                    context = context,
                )
            },
            onSignOut = {
                performSignOut(
                    context = context,
                    onSignOutComplete = {},
                    scope = scope,
                )
            },
        )
        return
    }

    // Back button returns to previous sheet (e.g. Chat) if citizen profile has a returnSheet
    val citizenReturnSheet = (activeSheet as? ActiveBottomSheet.CitizenProfile)?.returnSheet
    BackHandler(enabled = citizenReturnSheet != null) {
        if (citizenReturnSheet != null) {
            viewModel.openSheet(citizenReturnSheet)
        }
    }

    // Back button returns to Home tab if on another tab
    BackHandler(enabled = activeSheet == null && selectedTab != MainTab.HOME) {
        viewModel.selectTab(MainTab.HOME)
    }

    val destinations = remember {
        listOf(
            NavDestinationItem(
                tab = MainTab.HOME,
                labelRes = R.string.nav_home,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                testTag = "nav_tab_home",
                gameIconRes = R.drawable.ic_game_sword,
                customLabel = "Beranda",
            ),
            NavDestinationItem(
                tab = MainTab.MOMEN,
                labelRes = R.string.nav_momen,
                selectedIcon = Icons.Filled.PhotoCamera,
                unselectedIcon = Icons.Outlined.PhotoCamera,
                testTag = "nav_tab_momen",
                gameIconRes = R.drawable.ic_game_lantern,
                customLabel = "Momen",
            ),
            NavDestinationItem(
                tab = MainTab.KEJADIAN,
                labelRes = R.string.nav_kejadian,
                selectedIcon = Icons.Filled.Newspaper,
                unselectedIcon = Icons.Outlined.Newspaper,
                testTag = "nav_tab_kejadian",
                gameIconRes = R.drawable.ic_game_grimoire,
                customLabel = "Kejarian",
            ),
            NavDestinationItem(
                tab = MainTab.SEKITAR,
                labelRes = R.string.nav_sekitar,
                selectedIcon = Icons.Filled.NearMe,
                unselectedIcon = Icons.Outlined.NearMe,
                testTag = "nav_tab_sekitar",
                gameIconRes = R.drawable.ic_game_party_citizens,
                customLabel = "Sekitar",
            ),
            NavDestinationItem(
                tab = MainTab.CHAT,
                labelRes = R.string.nav_chat,
                selectedIcon = Icons.Filled.ChatBubble,
                unselectedIcon = Icons.Outlined.ChatBubbleOutline,
                testTag = "nav_tab_chat",
                gameIconRes = R.drawable.ic_game_scroll,
                customLabel = "Chat",
            ),
        )
    }

    val currentDistrict = userAccount?.district ?: "Genteng"
    val effectiveName = userAccount?.displayName?.ifBlank { displayName } ?: displayName
    val currentAvatarUrl = userAccount?.avatarUrl.orEmpty().ifBlank { myNearbyProfile?.avatarUrl.orEmpty() }
    val userInitials = effectiveName.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "SB" }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideTablet = maxWidth >= 680.dp

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                SurabayaCompactTopBar(
                    district = currentDistrict,
                    isAdmin = isAdmin,
                    bookmarkCount = bookmarks.size,
                    notificationCount = unreadNotifCount,
                    adminAttentionCount = adminAttentionCount,
                    userInitials = userInitials,
                    avatarUrl = currentAvatarUrl,
                    onOpenBookmarks = { viewModel.openSheet(ActiveBottomSheet.BookmarksList) },
                    onOpenNotifications = { viewModel.openSheet(ActiveBottomSheet.Notifications) },
                    onOpenAdminDashboard = { viewModel.openAdminDashboard() },
                    onOpenProfile = { viewModel.openSheet(ActiveBottomSheet.ProfileAndAdmin) },
                    onOpenSearch = { viewModel.selectTab(MainTab.KULINER) },
                )
            },
            bottomBar = {
                if (!isWideTablet) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (radioPlaybackState == RadioPlaybackState.PLAYING ||
                            radioPlaybackState == RadioPlaybackState.BUFFERING ||
                            radioPlaybackState == RadioPlaybackState.PAUSED
                        ) {
                            SurabayaRadioMiniPlayer(
                                playbackState = radioPlaybackState,
                                stationInfo = radioStationInfo,
                                onToggle = { viewModel.toggleRadio() },
                                onClose = { viewModel.stopRadio() },
                                onOpenFull = { viewModel.openRadioPlayer(autoPlay = false) },
                            )
                        }

                        Surface(
                            color = androidx.compose.ui.graphics.Color(0xFFFFF0F3),
                            border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFFFD1DC)),
                        ) {
                            NavigationBar(
                                windowInsets = WindowInsets.navigationBars,
                                containerColor = androidx.compose.ui.graphics.Color(0xFFFFF0F3),
                                tonalElevation = 0.dp,
                            ) {
                                destinations.forEach { dest ->
                                    val selected = selectedTab == dest.tab
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { viewModel.selectTab(dest.tab) },
                                        icon = {
                                            if (dest.gameIconRes != null) {
                                                Image(
                                                    painter = painterResource(id = dest.gameIconRes),
                                                    contentDescription = dest.customLabel ?: stringResource(dest.labelRes),
                                                    modifier = Modifier.size(28.dp),
                                                    contentScale = ContentScale.Fit,
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                    contentDescription = dest.customLabel ?: stringResource(dest.labelRes),
                                                )
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = dest.customLabel ?: stringResource(dest.labelRes),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                                color = if (selected) androidx.compose.ui.graphics.Color(0xFF111827) else androidx.compose.ui.graphics.Color(0xFF6B7280),
                                                maxLines = 1,
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = androidx.compose.ui.graphics.Color(0xFFFFDDE4),
                                            selectedIconColor = androidx.compose.ui.graphics.Color(0xFF111827),
                                            selectedTextColor = androidx.compose.ui.graphics.Color(0xFF111827),
                                            unselectedIconColor = androidx.compose.ui.graphics.Color(0xFF6B7280),
                                            unselectedTextColor = androidx.compose.ui.graphics.Color(0xFF6B7280),
                                        ),
                                        modifier = Modifier.testTag(dest.testTag),
                                    )
                                }
                            }
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (isWideTablet) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight(),
                    ) {
                        destinations.forEach { dest ->
                            val selected = selectedTab == dest.tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(dest.tab) },
                                icon = {
                                    if (dest.gameIconRes != null) {
                                        Image(
                                            painter = painterResource(id = dest.gameIconRes),
                                            contentDescription = dest.customLabel ?: stringResource(dest.labelRes),
                                            modifier = Modifier.size(28.dp),
                                            contentScale = ContentScale.Fit,
                                        )
                                    } else {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.customLabel ?: stringResource(dest.labelRes),
                                        )
                                    }
                                },
                                label = { Text(dest.customLabel ?: stringResource(dest.labelRes), style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag(dest.testTag),
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    Crossfade(targetState = selectedTab, label = "surabaya_tab_crossfade") { tab ->
                        when (tab) {
                            MainTab.HOME -> HomeScreen(
                                userAccount = userAccount,
                                isAdmin = isAdmin,
                                currentDistrict = currentDistrict,
                                userLat = userLat,
                                userLng = userLng,
                                isLiveGpsActive = isLiveGps,
                                onRefreshGps = { viewModel.refreshGpsLocation() },
                                incidentsState = incidentsState,
                                destinationsState = destinationsState,
                                culinaryState = culinaryState,
                                eventsState = approvedEventsState,
                                momentsState = momentsState,
                                servicesState = servicesState,
                                pollsState = approvedPollsState,
                                tambalBanSpotsState = tambalBanSpotsState,
                                pendingEventsCount = pendingEvents.size,
                                pendingPollsCount = pendingPollsList.size,
                                flaggedContentCount = flaggedItems.size,
                                onNavigateTab = { viewModel.selectTab(it) },
                                onOpenIncidentDetail = { viewModel.openSheet(ActiveBottomSheet.IncidentDetail(it)) },
                                onOpenDestinationDetail = { viewModel.openSheet(ActiveBottomSheet.DestinationDetail(it)) },
                                onOpenCulinaryDetail = { viewModel.openSheet(ActiveBottomSheet.CulinaryDetail(it)) },
                                onOpenEventDetail = { viewModel.openSheet(ActiveBottomSheet.EventDetail(it)) },
                                onOpenMomentDetail = { viewModel.openSheet(ActiveBottomSheet.MomentDetail(it)) },
                                onOpenServiceDetail = { viewModel.openSheet(ActiveBottomSheet.ServiceDetail(it)) },
                                onOpenCreateService = { viewModel.openSheet(ActiveBottomSheet.CreateService) },
                                onOpenPollDetail = { viewModel.openPollDetail(it) },
                                onOpenCreatePoll = { viewModel.openCreatePoll() },
                                onOpenAdminPendingPolls = { viewModel.openAdminPendingPolls() },
                                onOpenAdminDashboard = { viewModel.openAdminDashboard() },
                                onOpenTambalBanDetail = { viewModel.openTambalBanDetail(it) },
                                onOpenCreateTambalBan = { viewModel.openCreateTambalBan() },
                                onOpenBookmarks = { viewModel.openSheet(ActiveBottomSheet.BookmarksList) },
                                onOpenProfileAndAdmin = { viewModel.openSheet(ActiveBottomSheet.ProfileAndAdmin) },
                                onOpenRadio = { viewModel.openRadioPlayer(autoPlay = true) },
                                shortcutOrderIds = homeShortcutOrder,
                                onSaveShortcutOrder = { viewModel.saveHomeShortcutOrder(it) },
                                homeBannerConfig = homeBannerConfig,
                                onSaveHomeBannerConfig = { viewModel.saveHomeBannerConfig(it) },
                                onBannerClick = { banner ->
                                    when (banner.targetType) {
                                        "EVENT" -> viewModel.selectTab(MainTab.EVENT)
                                        "KULINER" -> viewModel.selectTab(MainTab.KULINER)
                                        "DESTINASI" -> viewModel.selectTab(MainTab.DESTINASI)
                                        "POLLING" -> viewModel.selectTab(MainTab.POLLING)
                                        "SEKITAR" -> viewModel.selectTab(MainTab.SEKITAR)
                                        "RADIO" -> viewModel.openRadioPlayer(autoPlay = true)
                                        "WEB" -> {
                                            if (banner.targetPayload.isNotBlank()) {
                                                runCatching {
                                                    val intent = android.content.Intent(
                                                        android.content.Intent.ACTION_VIEW,
                                                        android.net.Uri.parse(banner.targetPayload),
                                                    )
                                                    context.startActivity(intent)
                                                }
                                            }
                                        }
                                        else -> Unit
                                    }
                                },
                                onOpenSearch = { viewModel.selectTab(MainTab.KULINER) },
                            )
                            MainTab.MOMEN -> MomenScreen(
                                momentsState = momentsState,
                                currentUserId = currentUserId,
                                isAdmin = isAdmin,
                                onOpenMomentDetail = { viewModel.openSheet(ActiveBottomSheet.MomentDetail(it)) },
                                onOpenCreateMoment = { viewModel.openCreateMoment() },
                                onOpenCreateLensaWarga = { viewModel.openCreateLensaWarga() },
                                onToggleLike = { moment -> viewModel.toggleMomentLike(moment) },
                                onReportMoment = { viewModel.reportMomentContent(it) },
                                onBlockAuthor = { uid, name -> viewModel.blockCitizen(uid, name) },
                                onOpenAuthorProfile = { uid, name -> viewModel.openCitizenProfile(uid, name) },
                            )
                            MainTab.DESTINASI -> DestinasiScreen(
                                destinationsState = destinationsState,
                                bookmarks = bookmarks,
                                isAdmin = isAdmin,
                                onOpenDestinationDetail = { viewModel.openSheet(ActiveBottomSheet.DestinationDetail(it)) },
                                onOpenCreateDestination = { viewModel.openSheet(ActiveBottomSheet.CreateDestination) },
                                onToggleBookmark = {
                                    viewModel.toggleBookmark(it.id, "DESTINASI", it.title, "${it.district} • ${it.ticketPrice}", it.district)
                                },
                            )
                            MainTab.KEJADIAN -> KejadianScreen(
                                incidentsState = incidentsState,
                                bookmarks = bookmarks,
                                currentUserId = currentUserId,
                                onSelectIncident = { viewModel.openSheet(ActiveBottomSheet.IncidentDetail(it.id)) },
                                onVerifyIncident = { viewModel.verifyIncident(it) },
                                onToggleBookmark = {
                                    viewModel.toggleBookmark(it.id, "KEJADIAN", it.title, it.address, it.district)
                                },
                                onOpenCreateSheet = { viewModel.openSheet(ActiveBottomSheet.CreateIncident) },
                            )
                            MainTab.EVENT -> EventScreen(
                                approvedEventsState = approvedEventsState,
                                pendingEvents = pendingEvents,
                                myProposedEvents = myProposedEvents,
                                bookmarks = bookmarks,
                                isAdmin = isAdmin,
                                currentUserId = currentUserId,
                                onSelectEvent = { viewModel.openSheet(ActiveBottomSheet.EventDetail(it.id)) },
                                onToggleInterest = { viewModel.toggleEventInterest(it) },
                                onToggleBookmark = {
                                    viewModel.toggleBookmark(it.id, "EVENT", it.title, "${it.eventDateText} • ${it.venueName}", it.district)
                                },
                                onQuickApproveEvent = { viewModel.moderateEvent(it.id, true, "Disetujui Admin Surabaya.") },
                                onQuickRejectEvent = { viewModel.moderateEvent(it.id, false, "Usulan belum disetujui.") },
                                onOpenProposeSheet = { viewModel.openSheet(ActiveBottomSheet.ProposeEvent) },
                                onOpenAdminSwitchSheet = { viewModel.openSheet(ActiveBottomSheet.ProfileAndAdmin) },
                                onAdminClearEventLinks = { viewModel.adminClearExternalLinks("events", it.id) },
                            )
                            MainTab.KULINER -> KulinerScreen(
                                culinaryState = culinaryState,
                                bookmarks = bookmarks,
                                currentUserId = currentUserId,
                                userLat = userLat,
                                userLng = userLng,
                                isLiveGpsActive = isLiveGps,
                                onRefreshGps = { viewModel.refreshGpsLocation() },
                                onSelectSpot = { viewModel.openSheet(ActiveBottomSheet.CulinaryDetail(it.id)) },
                                onToggleBookmark = {
                                    viewModel.toggleBookmark(it.id, "KULINER", it.name, it.priceRange, it.district)
                                },
                                onOpenCreateSheet = { viewModel.openSheet(ActiveBottomSheet.CreateCulinary) },
                                onReportSpotLink = { viewModel.reportCulinaryLink(it) },
                            )
                            MainTab.SEKITAR -> TemanSekitarScreen(
                                nearbyPeersState = nearbyPeersState,
                                userAccount = userAccount,
                                isLiveGpsActive = isLiveGps,
                                onRefreshGps = { viewModel.refreshGpsLocation() },
                                onOpenPrivacySheet = { viewModel.openSheet(ActiveBottomSheet.GpsPrivacySettings) },
                                onGreetPeer = { viewModel.greetNearbyPeer(it) },
                                onBlockPeer = { peer ->
                                    viewModel.blockCitizen(peer.userId.ifBlank { peer.id }, peer.displayName)
                                },
                                onOpenCitizenProfile = { peer ->
                                    viewModel.openCitizenProfile(peer.userId.ifBlank { peer.id }, peer.displayName)
                                },
                            )
                            MainTab.CHAT -> ChatScreen(
                                publicRooms = publicChatRooms,
                                directRooms = directChatRooms,
                                currentUserId = currentUserId,
                                selectedSubTab = chatSubTab,
                                onSelectSubTab = { viewModel.selectChatSubTab(it) },
                                onStartNewDirectChat = { viewModel.openSheet(ActiveBottomSheet.StartDirectChat) },
                                onOpenRoom = { viewModel.requestOpenChatRoom(it) },
                                isAdmin = isAdmin,
                                unlockedGroupPinHashes = unlockedGroupPinHashes,
                                onCreatePublicGroup = { viewModel.openCreatePublicGroup() },
                                knownCitizens = combinedKnownCitizens,
                            )
                            MainTab.JASA -> JasaWargaScreen(
                                servicesState = servicesState,
                                currentUserId = currentUserId,
                                isAdmin = isAdmin,
                                onOpenServiceDetail = { viewModel.openSheet(ActiveBottomSheet.ServiceDetail(it)) },
                                onOpenCreateService = { viewModel.openSheet(ActiveBottomSheet.CreateService) },
                                onBookmarkService = {
                                    viewModel.toggleBookmark(it.id, "JASA", it.title, "${it.district} • ${it.priceDisplay}", it.district)
                                },
                                onReportService = { viewModel.reportServiceContent(it) },
                                onBackToHome = { viewModel.selectTab(MainTab.HOME) },
                            )
                            MainTab.POLLING -> PollingScreen(
                                pollsState = approvedPollsState,
                                pendingPollsState = pendingPollsState,
                                myProposedPolls = myProposedPolls,
                                currentUserId = currentUserId,
                                isAdmin = isAdmin,
                                onVote = { pollId, optId -> viewModel.votePoll(pollId, optId) },
                                onOpenPollDetail = { viewModel.openPollDetail(it) },
                                onOpenCreatePoll = { viewModel.openCreatePoll() },
                                onOpenAdminPending = { viewModel.openAdminPendingPolls() },
                                onShareText = { p -> viewModel.sharePollText(context, p) },
                                onShareJpg = { p -> viewModel.exportPollJpg(context, p, shareDirectly = true) },
                                onDownloadJpg = { p -> viewModel.exportPollJpg(context, p, shareDirectly = false) },
                                onDeletePoll = { pollId -> viewModel.deletePoll(pollId) },
                                onBackToHome = { viewModel.selectTab(MainTab.HOME) },
                            )
                            MainTab.TAMBAL_BAN -> TambalBanScreen(
                                spotsState = tambalBanSpotsState,
                                currentUserId = currentUserId,
                                isLiveGpsActive = isLiveGps,
                                onBackToHome = { viewModel.selectTab(MainTab.HOME) },
                                onOpenDetail = { viewModel.openTambalBanDetail(it) },
                                onCreateSpot = { viewModel.openCreateTambalBan() },
                                onRefreshLocation = { viewModel.refreshGpsLocation() },
                            )
                            MainTab.TRANSIT -> TransitScreen(
                                onBackToHome = { viewModel.selectTab(MainTab.HOME) },
                            )
                            MainTab.BARKAS -> BarkasScreen(
                                barkasState = barkasItemsState,
                                bookmarks = bookmarks,
                                currentUserId = currentUserId,
                                isAdmin = isAdmin,
                                onOpenBarkasDetail = { viewModel.openBarkasDetail(it) },
                                onOpenCreateBarkas = { viewModel.openCreateBarkas() },
                                onToggleSold = { item -> viewModel.toggleBarkasSold(item.id) },
                                onToggleBookmark = { item ->
                                    viewModel.toggleBookmark(
                                        targetId = item.id,
                                        targetType = "BARKAS",
                                        title = item.title,
                                        subtitle = "${item.formattedPrice} • Kec. ${item.district}",
                                        district = item.district,
                                    )
                                },
                                onBackToHome = { viewModel.selectTab(MainTab.HOME) },
                            )
                            MainTab.KOS_KONTRAK -> KosKontrakScreen(
                                kosState = kosListingsState,
                                bookmarks = bookmarks,
                                currentUserId = currentUserId,
                                isAdmin = isAdmin,
                                onOpenKosDetail = { viewModel.openKosDetail(it) },
                                onOpenCreateKos = { viewModel.openCreateKos() },
                                onToggleFull = { item -> viewModel.toggleKosFull(item.id) },
                                onToggleBookmark = { item ->
                                    viewModel.toggleBookmark(
                                        targetId = item.id,
                                        targetType = "KOS_KONTRAK",
                                        title = item.title,
                                        subtitle = "${item.priceWithPeriod} • Kec. ${item.district}",
                                        district = item.district,
                                    )
                                },
                                onBackToHome = { viewModel.selectTab(MainTab.HOME) },
                            )
                            MainTab.ADMIN_DASHBOARD -> AdminDashboardScreen(
                                adminEmail = email,
                                currentAdminUid = currentUserId,
                                isAdmin = isAdmin,
                                isSubmitting = isSubmitting,
                                pendingEvents = pendingEvents,
                                pendingPolls = pendingPollsList,
                                flaggedItems = flaggedItems,
                                incidents = (incidentsState as? UiState.Success)?.data.orEmpty(),
                                publicChatRooms = publicChatRooms,
                                bannedProfiles = bannedProfiles,
                                onBackToHome = { viewModel.selectTab(MainTab.HOME) },
                                onApproveAllPending = { evs, polls ->
                                    viewModel.approveAllPendingRequests(evs, polls)
                                },
                                onModerateEvent = { id, approve, note ->
                                    viewModel.moderateEvent(id, approve, note)
                                },
                                onModeratePoll = { id, approve, note ->
                                    viewModel.moderatePoll(id, approve, note)
                                },
                                onOpenEventDetail = { viewModel.openSheet(ActiveBottomSheet.EventDetail(it)) },
                                onOpenPollDetail = { viewModel.openPollDetail(it) },
                                onOpenFlaggedItem = { flagged ->
                                    when (flagged.collection) {
                                        "incidents" -> viewModel.openSheet(ActiveBottomSheet.IncidentDetail(flagged.id))
                                        "events" -> viewModel.openSheet(ActiveBottomSheet.EventDetail(flagged.id))
                                        "culinary" -> viewModel.openSheet(ActiveBottomSheet.CulinaryDetail(flagged.id))
                                        "moments" -> viewModel.openSheet(ActiveBottomSheet.MomentDetail(flagged.id))
                                        "destinations" -> viewModel.openSheet(ActiveBottomSheet.DestinationDetail(flagged.id))
                                        "services" -> viewModel.openSheet(ActiveBottomSheet.ServiceDetail(flagged.id))
                                        "barkas_items" -> viewModel.openBarkasDetail(flagged.id)
                                        "kos_listings" -> viewModel.openKosDetail(flagged.id)
                                    }
                                },
                                onRestoreFlaggedItem = { col, id -> viewModel.adminRestoreContent(col, id) },
                                onClearFlaggedLinks = { col, id -> viewModel.adminClearExternalLinks(col, id) },
                                onDeletePostPermanently = { col, id -> viewModel.adminDeletePost(col, id) },
                                onBanAuthor = { uid, name -> viewModel.adminBanCitizen(uid, name) },
                                onUnbanAuthor = { uid, name -> viewModel.adminUnbanCitizen(uid, name) },
                                onOpenIncidentDetail = { viewModel.openSheet(ActiveBottomSheet.IncidentDetail(it)) },
                                onToggleVerifyIncident = { viewModel.verifyIncident(it) },
                                onOpenCreatePublicGroup = { viewModel.openCreatePublicGroup() },
                                onOpenCreateDestination = { viewModel.openSheet(ActiveBottomSheet.CreateDestination) },
                                onOpenPublicRoom = { viewModel.openSheet(ActiveBottomSheet.ChatConversation(it)) },
                                onDeletePublicRoom = { viewModel.adminDeletePublicGroup(it) },
                            )
                        }
                    }
                }
            }
        }
    }

    // Google Maps-style Slide-Up Bottom Sheet Host for all Pop-ups, Forms, and Details
    val sheetTarget = activeSheet
    if (sheetTarget != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                val returnSheet = (sheetTarget as? ActiveBottomSheet.CitizenProfile)?.returnSheet
                if (returnSheet != null) {
                    viewModel.openSheet(returnSheet)
                } else {
                    viewModel.dismissSheet()
                }
            },
            sheetState = sheetState,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = androidx.compose.ui.graphics.Color.White,
            tonalElevation = 0.dp,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    width = 42.dp,
                    height = 5.dp,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            },
        ) {
            when (sheetTarget) {
                is ActiveBottomSheet.IncidentDetail -> {
                    val found = (incidentsState as? UiState.Success)?.data?.firstOrNull { it.id == sheetTarget.incidentId }
                    if (found != null) {
                        val isSaved = bookmarks.any { it.targetId == found.id && it.targetType == "KEJADIAN" }
                        IncidentDetailSheetContent(
                            report = found,
                            comments = incidentComments,
                            isSaved = isSaved,
                            isVerifiedByMe = currentUserId in found.verifiedByUids,
                            currentUserId = currentUserId,
                            isAdmin = isAdmin,
                            onVerify = { viewModel.verifyIncident(found) },
                            onBookmark = { viewModel.toggleBookmark(found.id, "KEJADIAN", found.title, found.address, found.district) },
                            onSendComment = { txt -> viewModel.sendIncidentComment(found, txt) },
                            onReportLink = { viewModel.reportIncidentLink(found) },
                            onAdminClearLinks = { viewModel.adminClearExternalLinks("incidents", found.id) },
                            onReportContent = { viewModel.reportIncidentContent(found) },
                            onBlockAuthor = if (found.authorId.isNotBlank() && found.authorId != currentUserId) {
                                { viewModel.blockCitizen(found.authorId, found.authorName) }
                            } else {
                                null
                            },
                            onAdminDeletePost = if (isAdmin) {
                                { viewModel.adminDeletePost("incidents", found.id) }
                            } else {
                                null
                            },
                            onAdminRestoreBlur = if (isAdmin) {
                                { viewModel.adminRestoreContent("incidents", found.id) }
                            } else {
                                null
                            },
                            onAdminBanAuthor = if (isAdmin && found.authorId.isNotBlank() && found.authorId != currentUserId) {
                                { viewModel.adminBanCitizen(found.authorId, found.authorName) }
                            } else {
                                null
                            },
                        )
                    }
                }
                ActiveBottomSheet.CreateIncident -> {
                    CreateIncidentSheetContent(
                        defaultDistrict = currentDistrict,
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
                        isLiveGpsActive = isLiveGps,
                        onRequestGpsRefresh = { viewModel.refreshGpsLocation() },
                        onSubmit = { title, desc, cat, sev, dist, addr, mapsUrl, photoUris ->
                            viewModel.submitIncident(
                                title = title,
                                description = desc,
                                category = cat,
                                severity = sev,
                                district = dist,
                                address = addr,
                                mapsUrl = mapsUrl,
                                photoUris = photoUris,
                                context = context,
                            )
                        },
                    )
                }
                is ActiveBottomSheet.EventDetail -> {
                    val allKnownEvents = ((approvedEventsState as? UiState.Success)?.data.orEmpty() + pendingEvents + myProposedEvents)
                        .distinctBy { it.id }
                    val found = allKnownEvents.firstOrNull { it.id == sheetTarget.eventId }
                    if (found != null) {
                        val isSaved = bookmarks.any { it.targetId == found.id && it.targetType == "EVENT" }
                        EventDetailSheetContent(
                            event = found,
                            comments = eventComments,
                            isAdmin = isAdmin,
                            isSaved = isSaved,
                            isInterested = currentUserId in found.interestedUids,
                            isInterestPending = found.id in pendingEventInterests,
                            currentUserId = currentUserId,
                            onToggleInterest = { viewModel.toggleEventInterest(found) },
                            onToggleBookmark = {
                                viewModel.toggleBookmark(found.id, "EVENT", found.title, "${found.eventDateText} • ${found.venueName}", found.district)
                            },
                            onModerate = { approve, note -> viewModel.moderateEvent(found.id, approve, note) },
                            onSendComment = { text -> viewModel.sendEventComment(found, text) },
                            onOpenCitizenProfile = { authorId, authorName ->
                                viewModel.openSheet(
                                    ActiveBottomSheet.CitizenProfile(
                                        userId = authorId,
                                        initialDisplayName = authorName,
                                        returnSheet = sheetTarget,
                                    )
                                )
                            },
                            onReportLink = { viewModel.reportEventLink(found) },
                            onAdminClearLinks = { viewModel.adminClearExternalLinks("events", found.id) },
                            onReportContent = { viewModel.reportEventContent(found) },
                            onEditEvent = if ((found.proposerId.isNotBlank() && found.proposerId == currentUserId) || isAdmin) {
                                { viewModel.openSheet(ActiveBottomSheet.EditEvent(found)) }
                            } else {
                                null
                            },
                            onBlockAuthor = if (found.proposerId.isNotBlank() && found.proposerId != currentUserId) {
                                { viewModel.blockCitizen(found.proposerId, found.proposerName) }
                            } else {
                                null
                            },
                            onAdminDeletePost = if (isAdmin) {
                                { viewModel.adminDeletePost("events", found.id) }
                            } else {
                                null
                            },
                            onAdminRestoreBlur = if (isAdmin) {
                                { viewModel.adminRestoreContent("events", found.id) }
                            } else {
                                null
                            },
                            onAdminBanAuthor = if (isAdmin && found.proposerId.isNotBlank() && found.proposerId != currentUserId) {
                                { viewModel.adminBanCitizen(found.proposerId, found.proposerName) }
                            } else {
                                null
                            },
                        )
                    }
                }
                ActiveBottomSheet.ProposeEvent -> {
                    ProposeEventSheetContent(
                        defaultDistrict = currentDistrict,
                        initialEvent = null,
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
                        onSaveDraft = { draft -> viewModel.saveEventDraft(context, draft) },
                        onClearDraft = { viewModel.clearEventDraft(context) },
                        onSubmit = { title, cat, dateTxt, timeTxt, venue, dist, addr, price, desc, mapsUrl, photoUris ->
                            viewModel.submitEventProposal(
                                title = title,
                                category = cat,
                                eventDateText = dateTxt,
                                timeText = timeTxt,
                                venueName = venue,
                                district = dist,
                                address = addr,
                                priceLabel = price,
                                description = desc,
                                mapsUrl = mapsUrl,
                                photoUris = photoUris,
                                context = context,
                            )
                        },
                        onClose = { viewModel.dismissSheet() },
                    )
                }
                is ActiveBottomSheet.EditEvent -> {
                    ProposeEventSheetContent(
                        defaultDistrict = sheetTarget.event.district,
                        initialEvent = sheetTarget.event,
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
                        onSubmit = { title, cat, dateTxt, timeTxt, venue, dist, addr, price, desc, mapsUrl, photoUris ->
                            viewModel.updateEvent(
                                eventId = sheetTarget.event.id,
                                title = title,
                                category = cat,
                                eventDateText = dateTxt,
                                timeText = timeTxt,
                                venueName = venue,
                                district = dist,
                                address = addr,
                                priceLabel = price,
                                description = desc,
                                mapsUrl = mapsUrl,
                                newPhotoUris = photoUris,
                                context = context,
                            )
                        },
                        onClose = { viewModel.dismissSheet() },
                    )
                }
                is ActiveBottomSheet.CulinaryDetail -> {
                    val found = (culinaryState as? UiState.Success)?.data?.firstOrNull { it.id == sheetTarget.culinaryId }
                    if (found != null) {
                        val isSaved = bookmarks.any { it.targetId == found.id && it.targetType == "KULINER" }
                        CulinaryDetailSheetContent(
                            spot = found,
                            reviews = culinaryReviews,
                            isSaved = isSaved,
                            currentUserId = currentUserId,
                            isAdmin = isAdmin,
                            userLat = userLat,
                            userLng = userLng,
                            onBookmark = { viewModel.toggleBookmark(found.id, "KULINER", found.name, found.priceRange, found.district) },
                            onSubmitReview = { stars, comment -> viewModel.submitCulinaryReview(found, stars, comment) },
                            onReportLink = { viewModel.reportCulinaryLink(found) },
                            onAdminClearLinks = { viewModel.adminClearExternalLinks("culinary", found.id) },
                            onReportContent = { viewModel.reportCulinaryContent(found) },
                            onBlockAuthor = if (found.createdBy.isNotBlank() && found.createdBy != currentUserId) {
                                { viewModel.blockCitizen(found.createdBy, found.authorName) }
                            } else {
                                null
                            },
                            onAdminDeletePost = if (isAdmin) {
                                { viewModel.adminDeletePost("culinary", found.id) }
                            } else {
                                null
                            },
                            onAdminRestoreBlur = if (isAdmin) {
                                { viewModel.adminRestoreContent("culinary", found.id) }
                            } else {
                                null
                            },
                            onAdminBanAuthor = if (isAdmin && found.createdBy.isNotBlank() && found.createdBy != currentUserId) {
                                { viewModel.adminBanCitizen(found.createdBy, found.authorName) }
                            } else {
                                null
                            },
                        )
                    }
                }
                ActiveBottomSheet.CreateCulinary -> {
                    CreateCulinarySheetContent(
                        defaultDistrict = currentDistrict,
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
                        isLiveGpsActive = isLiveGps,
                        onRequestGpsRefresh = { viewModel.refreshGpsLocation() },
                        onSubmitWithGps = { name, cat, desc, dist, addr, price, openH, closeH, mapsUrl, gofoodUrl, grabfoodUrl, shopeefoodUrl, photoUris, useCurrentGps ->
                            viewModel.submitCulinarySpot(
                                name = name,
                                category = cat,
                                description = desc,
                                district = dist,
                                address = addr,
                                priceRange = price,
                                openHour = openH,
                                closeHour = closeH,
                                mapsUrl = mapsUrl,
                                gofoodUrl = gofoodUrl,
                                grabfoodUrl = grabfoodUrl,
                                shopeefoodUrl = shopeefoodUrl,
                                photoUris = photoUris,
                                useCurrentGpsLocation = useCurrentGps,
                                context = context,
                            )
                        },
                    )
                }
                ActiveBottomSheet.GpsPrivacySettings -> {
                    ProfileAndPrivacySheetContent(
                        userAccount = userAccount,
                        nearbyProfile = myNearbyProfile,
                        userEmail = email,
                        isAdmin = isAdmin,
                        focusPrivacyOnly = true,
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
                        knownCitizens = allDiscoverableCitizens,
                        onUnblockUser = { uid -> viewModel.unblockCitizen(uid) },
                        onToggleAdmin = { viewModel.toggleAdminMode(it) },
                        onSaveProfile = { name, dist, bio, ints, rad, disc, avatarUri ->
                            viewModel.saveProfileAndPrivacy(name, dist, bio, ints, rad, disc, avatarUri, context)
                        },
                        onSaveProfileWithGender = { name, gender, dist, bio, ints, rad, disc, avatarUri ->
                            viewModel.saveProfileAndPrivacy(name, dist, bio, ints, rad, disc, avatarUri, context, gender)
                        },
                        onSignOut = {},
                    )
                }
                ActiveBottomSheet.ProfileAndAdmin -> {
                    ProfileAndPrivacySheetContent(
                        userAccount = userAccount,
                        nearbyProfile = myNearbyProfile,
                        userEmail = email,
                        isAdmin = isAdmin,
                        focusPrivacyOnly = false,
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
                        knownCitizens = allDiscoverableCitizens,
                        userMoments = (momentsState as? UiState.Success)?.data?.filter { it.authorId == currentUserId }.orEmpty(),
                        onOpenMomentDetail = { viewModel.openSheet(ActiveBottomSheet.MomentDetail(it)) },
                        onOpenCreateMoment = { viewModel.openCreateMoment() },
                        onOpenCreateLensaWarga = { viewModel.openCreateLensaWarga() },
                        onUnblockUser = { uid -> viewModel.unblockCitizen(uid) },
                        onToggleAdmin = { viewModel.toggleAdminMode(it) },
                        onOpenAdminDashboard = { viewModel.openAdminDashboard() },
                        onSaveProfile = { name, dist, bio, ints, rad, disc, avatarUri ->
                            viewModel.saveProfileAndPrivacy(name, dist, bio, ints, rad, disc, avatarUri, context)
                        },
                        onSaveProfileWithGender = { name, gender, dist, bio, ints, rad, disc, avatarUri ->
                            viewModel.saveProfileAndPrivacy(name, dist, bio, ints, rad, disc, avatarUri, context, gender)
                        },
                        onSignOut = {
                            viewModel.dismissSheet()
                            performSignOut(
                                context = context,
                                onSignOutComplete = {},
                                scope = scope,
                            )
                        },
                        onDeleteAccount = {
                            viewModel.deleteUserAccount(context)
                        },
                    )
                }
                is ActiveBottomSheet.CitizenProfile -> {
                    val targetUid = sheetTarget.userId
                    val peerItems = (nearbyPeersState as? UiState.Success)?.data.orEmpty()
                    val matchedPeerItem = peerItems.firstOrNull {
                        it.profile.userId == targetUid || it.profile.id == targetUid
                    }
                    val foundProfile = matchedPeerItem?.profile
                        ?: allDiscoverableCitizens.firstOrNull { it.userId == targetUid || it.id == targetUid }
                        ?: cachedCitizenProfiles[targetUid]
                    val estimatedDistKm = when {
                        targetUid == currentUserId -> null
                        matchedPeerItem != null -> matchedPeerItem.distanceKm
                        foundProfile != null -> calculateDistanceKm(userLat, userLng, foundProfile.lat, foundProfile.lng)
                        else -> null
                    }
                    val citizenMoments = (momentsState as? UiState.Success)?.data?.filter { it.authorId == targetUid }.orEmpty()
                    CitizenProfileSheetContent(
                        citizenUserId = targetUid,
                        initialName = sheetTarget.initialDisplayName,
                        citizenProfile = foundProfile,
                        citizenMoments = citizenMoments,
                        currentUserId = currentUserId,
                        isAdmin = isAdmin,
                        estimatedDistanceKm = estimatedDistKm,
                        onOpenMomentDetail = { viewModel.openSheet(ActiveBottomSheet.MomentDetail(it)) },
                        onGreetCitizen = { profile -> viewModel.greetNearbyPeer(profile) },
                        onBlockCitizen = { uid, name -> viewModel.blockCitizen(uid, name) },
                        onEditMyProfile = { viewModel.openSheet(ActiveBottomSheet.ProfileAndAdmin) },
                        onDeleteAccount = { viewModel.deleteUserAccount(context) },
                        onClose = {
                            if (sheetTarget.returnSheet != null) {
                                viewModel.openSheet(sheetTarget.returnSheet)
                            } else {
                                viewModel.dismissSheet()
                            }
                        },
                    )
                }
                ActiveBottomSheet.BookmarksList -> {
                    BookmarksSheetContent(
                        bookmarks = bookmarks,
                        onOpenBookmark = { bm ->
                            when (bm.targetType) {
                                "KULINER" -> {
                                    viewModel.selectTab(MainTab.KULINER)
                                    viewModel.openSheet(ActiveBottomSheet.CulinaryDetail(bm.targetId))
                                }
                                "KEJADIAN" -> {
                                    viewModel.selectTab(MainTab.KEJADIAN)
                                    viewModel.openSheet(ActiveBottomSheet.IncidentDetail(bm.targetId))
                                }
                                "EVENT" -> {
                                    viewModel.selectTab(MainTab.EVENT)
                                    viewModel.openSheet(ActiveBottomSheet.EventDetail(bm.targetId))
                                }
                                "DESTINASI" -> {
                                    viewModel.selectTab(MainTab.DESTINASI)
                                    viewModel.openSheet(ActiveBottomSheet.DestinationDetail(bm.targetId))
                                }
                                "JASA" -> {
                                    viewModel.selectTab(MainTab.JASA)
                                    viewModel.openSheet(ActiveBottomSheet.ServiceDetail(bm.targetId))
                                }
                                "BARKAS" -> {
                                    viewModel.selectTab(MainTab.BARKAS)
                                    viewModel.openBarkasDetail(bm.targetId)
                                }
                                "KOS_KONTRAK" -> {
                                    viewModel.selectTab(MainTab.KOS_KONTRAK)
                                    viewModel.openKosDetail(bm.targetId)
                                }
                            }
                        },
                        onRemoveBookmark = { bm ->
                            viewModel.toggleBookmark(bm.targetId, bm.targetType, bm.title, bm.subtitle, bm.district)
                        },
                    )
                }
                ActiveBottomSheet.Notifications -> {
                    NotificationsSheetContent(
                        notifications = notifications,
                        onOpenNotification = { notif ->
                            viewModel.markNotificationAsRead(notif.id)
                            viewModel.dismissSheet()
                            when (notif.type) {
                                AppNotificationType.CHAT -> {
                                    viewModel.selectTab(MainTab.CHAT)
                                    val foundRoom = (publicChatRooms + directChatRooms).firstOrNull { it.id == notif.targetId || it.roomKey == notif.targetId }
                                    if (foundRoom != null) {
                                        viewModel.requestOpenChatRoom(foundRoom)
                                    }
                                }
                                AppNotificationType.EVENT -> {
                                    viewModel.selectTab(MainTab.EVENT)
                                    if (notif.targetId.isNotBlank()) {
                                        viewModel.openSheet(ActiveBottomSheet.EventDetail(notif.targetId))
                                    }
                                }
                                AppNotificationType.INCIDENT -> {
                                    viewModel.selectTab(MainTab.KEJADIAN)
                                    if (notif.targetId.isNotBlank()) {
                                        viewModel.openSheet(ActiveBottomSheet.IncidentDetail(notif.targetId))
                                    }
                                }
                                AppNotificationType.SYSTEM -> {}
                            }
                        },
                        onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
                        onTestNotification = { type -> viewModel.sendTestHeadsUpNotification(context, type) },
                        onClose = { viewModel.dismissSheet() },
                    )
                }
                is ActiveBottomSheet.MomentDetail -> {
                    val found = (momentsState as? UiState.Success)?.data?.firstOrNull { it.id == sheetTarget.momentId }
                    if (found != null) {
                        MomentDetailSheetContent(
                            moment = found,
                            comments = momentComments,
                            currentUserId = currentUserId,
                            isAdmin = isAdmin,
                            onToggleLike = {
                                viewModel.toggleMomentLike(found)
                            },
                            onAddComment = { viewModel.submitMomentComment(found.id, it) },
                            onReport = { viewModel.reportMomentContent(found) },
                            onBlockAuthor = { viewModel.blockCitizen(found.authorId, found.authorName) },
                            onAdminDelete = if (isAdmin) {
                                { viewModel.adminDeletePost("moments", found.id) }
                            } else null,
                            onOpenAuthorProfile = { uid, name ->
                                viewModel.openCitizenProfile(uid, name)
                            },
                            onClose = { viewModel.dismissSheet() },
                        )
                    }
                }
                is ActiveBottomSheet.CreateMoment -> {
                    CreateMomentSheetContent(
                        defaultDistrict = currentDistrict,
                        initialPostType = sheetTarget.initialPostType,
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
                        onSubmit = { caption, dist, loc, postType, photos ->
                            viewModel.submitMoment(
                                caption = caption,
                                district = dist,
                                locationName = loc,
                                postType = postType,
                                photoUris = photos,
                                context = context,
                            )
                        },
                    )
                }
                is ActiveBottomSheet.DestinationDetail -> {
                    val found = (destinationsState as? UiState.Success)?.data?.firstOrNull { it.id == sheetTarget.destinationId }
                    if (found != null) {
                        val isSaved = bookmarks.any { it.targetId == found.id && it.targetType == "DESTINASI" }
                        DestinationDetailSheetContent(
                            destination = found,
                            reviews = destinationReviews,
                            isBookmarked = isSaved,
                            isAdmin = isAdmin,
                            onToggleBookmark = {
                                viewModel.toggleBookmark(found.id, "DESTINASI", found.title, "${found.district} • ${found.ticketPrice}", found.district)
                            },
                            onAddReview = { rev, rating -> viewModel.submitDestinationReview(found.id, rev, rating) },
                            onReport = { viewModel.reportDestinationContent(found) },
                            onEditDestination = if (isAdmin) {
                                { viewModel.openSheet(ActiveBottomSheet.EditDestination(found)) }
                            } else null,
                            onAdminDelete = if (isAdmin) {
                                { viewModel.adminDeletePost("destinations", found.id) }
                            } else null,
                            onClose = { viewModel.dismissSheet() },
                        )
                    }
                }
                ActiveBottomSheet.CreateDestination -> {
                    CreateDestinationSheetContent(
                        defaultDistrict = currentDistrict,
                        initialDestination = null,
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
                        onSaveDraft = { draft -> viewModel.saveDestinationDraft(context, draft) },
                        onClearDraft = { viewModel.clearDestinationDraft(context) },
                        onSubmit = { title, cat, desc, addr, dist, price, hours, bestTime, mapsUrl, photos ->
                            viewModel.submitDestination(
                                title = title,
                                category = cat,
                                description = desc,
                                address = addr,
                                district = dist,
                                ticketPrice = price,
                                operationalHours = hours,
                                bestTimeToVisit = bestTime,
                                mapsUrl = mapsUrl,
                                photoUris = photos,
                                context = context,
                            )
                        },
                        onClose = { viewModel.dismissSheet() },
                    )
                }
                is ActiveBottomSheet.EditDestination -> {
                    CreateDestinationSheetContent(
                        defaultDistrict = sheetTarget.destination.district,
                        initialDestination = sheetTarget.destination,
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
                        onSubmit = { title, cat, desc, addr, dist, price, hours, bestTime, mapsUrl, photos ->
                            viewModel.updateDestination(
                                destinationId = sheetTarget.destination.id,
                                title = title,
                                category = cat,
                                description = desc,
                                address = addr,
                                district = dist,
                                ticketPrice = price,
                                operationalHours = hours,
                                bestTimeToVisit = bestTime,
                                mapsUrl = mapsUrl,
                                newPhotoUris = photos,
                                context = context,
                            )
                        },
                        onClose = { viewModel.dismissSheet() },
                    )
                }
                ActiveBottomSheet.StartDirectChat -> {
                    StartDirectChatSheetContent(
                        citizens = allDiscoverableCitizens,
                        currentUserId = currentUserId,
                        onSelectCitizen = { peer -> viewModel.greetNearbyPeer(peer) },
                    )
                }
                ActiveBottomSheet.CreatePublicGroup -> {
                    CreatePublicGroupSheetContent(
                        isSubmitting = isSubmitting,
                        isAdmin = isAdmin,
                        myActiveGroupCount = myActivePublicGroupCount,
                        onSubmitWithPin = { title, subtitle, welcomeMsg, pinCode ->
                            viewModel.submitPublicGroupRoom(
                                title = title,
                                subtitle = subtitle,
                                welcomeMessage = welcomeMsg,
                                pinCode = pinCode,
                                context = context,
                            )
                        },
                    )
                }
                is ActiveBottomSheet.ChatConversation -> {
                    val liveRoom = (publicChatRooms + directChatRooms)
                        .firstOrNull { it.id == sheetTarget.room.id } ?: sheetTarget.room
                    val peerUid = liveRoom.otherParticipantIdFor(currentUserId)
                        ?: if (liveRoom.roomType == "DIRECT" && liveRoom.roomKey.startsWith("direct_")) {
                            val parts = liveRoom.roomKey.removePrefix("direct_").split("_")
                            parts.firstOrNull { it.isNotBlank() && it != currentUserId }
                        } else null
                    val peerProfile = if (peerUid != null) {
                        val peerItems = (nearbyPeersState as? UiState.Success)?.data.orEmpty()
                        peerItems.firstOrNull { it.profile.userId == peerUid || it.profile.id == peerUid }?.profile
                            ?: allDiscoverableCitizens.firstOrNull { it.userId == peerUid || it.id == peerUid }
                            ?: cachedCitizenProfiles[peerUid]
                    } else null

                    LaunchedEffect(peerUid) {
                        if (peerUid != null) {
                            viewModel.preloadCitizenProfile(peerUid)
                        }
                    }
                    val canManageGroup = liveRoom.roomType == "PUBLIC" && (isAdmin || liveRoom.isCreatedBy(currentUserId))
                    ChatConversationSheetContent(
                        room = liveRoom,
                        messages = activeChatMessages,
                        currentUserId = currentUserId,
                        isAdmin = isAdmin,
                        isSending = isSendingChatMessage || isScanningPhoto,
                        peerProfile = peerProfile,
                        knownCitizens = combinedKnownCitizens,
                        onOpenPeerProfile = if (peerUid != null) {
                            {
                                val targetName = peerProfile?.displayName?.ifBlank { liveRoom.displayTitleFor(currentUserId) }
                                    ?: liveRoom.displayTitleFor(currentUserId)
                                viewModel.openCitizenProfile(
                                    userId = peerUid,
                                    displayName = targetName,
                                    returnSheet = ActiveBottomSheet.ChatConversation(liveRoom),
                                )
                            }
                        } else null,
                        onOpenSenderProfile = { senderUid, senderName ->
                            viewModel.openCitizenProfile(
                                userId = senderUid,
                                displayName = senderName,
                                returnSheet = ActiveBottomSheet.ChatConversation(liveRoom),
                            )
                        },
                        onSendMessage = { txt, photoUri, replyMsg ->
                            viewModel.sendChatMessage(liveRoom, txt, photoUri, context, replyMsg)
                        },
                        onBlockPeer = if (peerUid != null) {
                            { viewModel.blockCitizen(peerUid, liveRoom.displayTitleFor(currentUserId)) }
                        } else {
                            null
                        },
                        onAdminDeleteGroup = if (canManageGroup) {
                            { viewModel.deletePublicGroup(liveRoom) }
                        } else {
                            null
                        },
                        onUpdateGroupPin = if (canManageGroup) {
                            { newPin -> viewModel.updatePublicGroupPin(liveRoom, newPin, context) }
                        } else {
                            null
                        },
                        onMarkMessagesRead = { ids ->
                            viewModel.markMessagesAsRead(liveRoom, ids)
                        },
                        onTyping = { isTyping ->
                            viewModel.setTyping(liveRoom, isTyping)
                        },
                        onSyncPhotos = { msgs ->
                            viewModel.syncAndSaveChatPhotos(context, msgs, liveRoom)
                        },
                        onDeleteMessage = { msgId ->
                            viewModel.deleteChatMessage(liveRoom, msgId)
                        },
                    )
                }
                ActiveBottomSheet.RadioPlayer -> {
                    RadioPlayerSheetContent(
                        playbackState = radioPlaybackState,
                        volume = radioVolume,
                        errorMessage = radioErrorMessage,
                        stationInfo = radioStationInfo,
                        onTogglePlay = { viewModel.toggleRadio() },
                        onSetVolume = { viewModel.setRadioVolume(it) },
                        onClose = { viewModel.dismissSheet() },
                        defaultReporterName = effectiveName,
                        defaultDistrict = currentDistrict,
                        isLiveGpsActive = isLiveGps,
                        onRequestGpsRefresh = { viewModel.refreshGpsLocation() },
                    )
                }
                is ActiveBottomSheet.ServiceDetail -> {
                    val found = (servicesState as? UiState.Success)?.data?.firstOrNull { it.id == sheetTarget.serviceId }
                    if (found != null) {
                        val isSaved = bookmarks.any { it.targetId == found.id && it.targetType == "JASA" }
                        ServiceDetailSheetContent(
                            service = found,
                            isSaved = isSaved,
                            currentUserId = currentUserId,
                            isAdmin = isAdmin,
                            onBookmark = {
                                viewModel.toggleBookmark(found.id, "JASA", found.title, "${found.district} • ${found.priceDisplay}", found.district)
                            },
                            onReport = { viewModel.reportServiceContent(found) },
                            onDelete = if (found.providerId == currentUserId || isAdmin) {
                                { viewModel.deleteService(found.id) }
                            } else {
                                null
                            },
                            onClose = { viewModel.dismissSheet() },
                        )
                    }
                }
                ActiveBottomSheet.CreateService -> {
                    CreateServiceSheetContent(
                        defaultDistrict = currentDistrict,
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
                        onSubmit = { title, category, price, priceUnit, district, coverageArea, phoneNumber, whatsappNumber, description, photoUris ->
                            viewModel.submitCitizenService(
                                title = title,
                                category = category,
                                price = price,
                                priceUnit = priceUnit,
                                district = district,
                                coverageArea = coverageArea,
                                phoneNumber = phoneNumber,
                                whatsappNumber = whatsappNumber,
                                description = description,
                                photoUris = photoUris,
                                context = context,
                            )
                        },
                        onClose = { viewModel.dismissSheet() },
                    )
                }
                is ActiveBottomSheet.PollDetail -> {
                    val activePoll = activeSelectedPoll
                    if (activePoll != null) {
                        PollDetailSheetContent(
                            poll = activePoll,
                            comments = activePollComments,
                            currentUserId = currentUserId,
                            isAdmin = isAdmin,
                            onVote = { pollId, optId -> viewModel.votePoll(pollId, optId) },
                            onAddComment = { text -> viewModel.submitPollComment(activePoll.id, text) },
                            onShareText = { p -> viewModel.sharePollText(context, p) },
                            onShareJpg = { p -> viewModel.exportPollJpg(context, p, shareDirectly = true) },
                            onDownloadJpg = { p -> viewModel.exportPollJpg(context, p, shareDirectly = false) },
                            onDelete = if (activePoll.proposerId == currentUserId || isAdmin) {
                                { viewModel.deletePoll(activePoll.id) }
                            } else {
                                null
                            },
                            onDismiss = { viewModel.dismissSheet() },
                        )
                    }
                }
                ActiveBottomSheet.CreatePoll -> {
                    CreatePollSheetContent(
                        isSubmitting = isSubmitting,
                        onSubmit = { title, description, category, district, options ->
                            viewModel.submitPollProposal(title, description, category, district, options)
                        },
                        onDismiss = { viewModel.dismissSheet() },
                    )
                }
                ActiveBottomSheet.AdminPendingPolls -> {
                    val pendingList = (pendingPollsState as? UiState.Success)?.data ?: emptyList()
                    AdminPendingPollsSheetContent(
                        pendingPolls = pendingList,
                        onApprove = { viewModel.moderatePoll(it, true) },
                        onReject = { viewModel.moderatePoll(it, false) },
                        onDismiss = { viewModel.dismissSheet() },
                    )
                }
                is ActiveBottomSheet.TambalBanDetail -> {
                    val activeTambal = activeSelectedTambalBan
                    if (activeTambal != null) {
                        TambalBanDetailSheetContent(
                            spot = activeTambal,
                            comments = activeTambalBanComments,
                            currentUserId = currentUserId,
                            isAdmin = isAdmin,
                            onVerify = { viewModel.verifyTambalBan(activeTambal.id) },
                            onSendComment = { text -> viewModel.submitTambalBanComment(activeTambal.id, text) },
                            onEditSpot = if ((activeTambal.authorId.isNotBlank() && activeTambal.authorId == currentUserId) || isAdmin) {
                                { viewModel.openSheet(ActiveBottomSheet.EditTambalBan(activeTambal)) }
                            } else {
                                null
                            },
                            onDeleteSpot = { viewModel.deleteTambalBan(activeTambal.id) },
                            onClose = { viewModel.dismissSheet() },
                        )
                    }
                }
                ActiveBottomSheet.CreateTambalBan -> {
                    CreateTambalBanSheetContent(
                        defaultDistrict = currentDistrict,
                        initialSpot = null,
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
                        isLiveGpsActive = isLiveGps,
                        onRequestGpsRefresh = { viewModel.refreshGpsLocation() },
                        onSaveDraft = { draft -> viewModel.saveTambalBanDraft(context, draft) },
                        onClearDraft = { viewModel.clearTambalBanDraft(context) },
                        onSubmit = { name, description, district, address, is24Hours, isTubeless, forMotor, forMobil, canCallOut, mapsUrl, phoneNumber, whatsappNumber, photoUri ->
                            viewModel.submitTambalBanSpot(
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
                                photoUri = photoUri,
                                context = context,
                            )
                        },
                        onClose = { viewModel.dismissSheet() },
                    )
                }
                is ActiveBottomSheet.EditTambalBan -> {
                    CreateTambalBanSheetContent(
                        defaultDistrict = sheetTarget.spot.district,
                        initialSpot = sheetTarget.spot,
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
                        isLiveGpsActive = isLiveGps,
                        onRequestGpsRefresh = { viewModel.refreshGpsLocation() },
                        onSubmit = { name, description, district, address, is24Hours, isTubeless, forMotor, forMobil, canCallOut, mapsUrl, phoneNumber, whatsappNumber, photoUri ->
                            viewModel.updateTambalBanSpot(
                                spotId = sheetTarget.spot.id,
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
                                photoUri = photoUri,
                                context = context,
                            )
                        },
                        onClose = { viewModel.dismissSheet() },
                    )
                }
                is ActiveBottomSheet.BarkasDetail -> {
                    val found = activeSelectedBarkas
                        ?: (barkasItemsState as? UiState.Success)?.data?.firstOrNull { it.id == sheetTarget.barkasId }
                    if (found != null) {
                        val isSaved = bookmarks.any { it.targetId == found.id && it.targetType == "BARKAS" }
                        BarkasDetailSheetContent(
                            item = found,
                            comments = activeBarkasComments,
                            isSaved = isSaved,
                            currentUserId = currentUserId,
                            isAdmin = isAdmin,
                            onToggleSold = { viewModel.toggleBarkasSold(found.id) },
                            onChatSeller = { viewModel.chatWithBarkasSeller(found) },
                            onToggleBookmark = {
                                viewModel.toggleBookmark(
                                    targetId = found.id,
                                    targetType = "BARKAS",
                                    title = found.title,
                                    subtitle = "${found.formattedPrice} • Kec. ${found.district}",
                                    district = found.district,
                                )
                            },
                            onSendComment = { text -> viewModel.submitBarkasComment(found.id, text) },
                            onReport = { viewModel.reportBarkasContent(found) },
                            onDelete = if (found.isOwnedBy(currentUserId) || isAdmin) {
                                { viewModel.deleteBarkasItem(found.id) }
                            } else {
                                null
                            },
                            onClose = { viewModel.dismissSheet() },
                        )
                    }
                }
                ActiveBottomSheet.CreateBarkas -> {
                    CreateBarkasSheetContent(
                        defaultDistrict = currentDistrict,
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
                        onSubmit = { title, cat, cond, price, nego, dist, codLoc, desc, wa, photos ->
                            viewModel.submitBarkasItem(
                                title = title,
                                category = cat,
                                itemCondition = cond,
                                price = price,
                                isNegotiable = nego,
                                district = dist,
                                codLocation = codLoc,
                                description = desc,
                                whatsappNumber = wa,
                                photoUris = photos,
                                context = context,
                            )
                        },
                        onClose = { viewModel.dismissSheet() },
                    )
                }
                is ActiveBottomSheet.KosDetail -> {
                    val found = activeSelectedKos
                        ?: (kosListingsState as? UiState.Success)?.data?.firstOrNull { it.id == sheetTarget.kosId }
                    if (found != null) {
                        val isSaved = bookmarks.any { it.targetId == found.id && it.targetType == "KOS_KONTRAK" }
                        KosDetailSheetContent(
                            item = found,
                            comments = activeKosComments,
                            isSaved = isSaved,
                            currentUserId = currentUserId,
                            isAdmin = isAdmin,
                            onToggleFull = { viewModel.toggleKosFull(found.id) },
                            onChatOwner = { viewModel.chatWithKosOwner(found) },
                            onToggleBookmark = {
                                viewModel.toggleBookmark(
                                    targetId = found.id,
                                    targetType = "KOS_KONTRAK",
                                    title = found.title,
                                    subtitle = "${found.priceWithPeriod} • Kec. ${found.district}",
                                    district = found.district,
                                )
                            },
                            onSendComment = { text -> viewModel.submitKosComment(found.id, text) },
                            onReportContent = { viewModel.reportKosContent(found) },
                            onReportLink = { viewModel.reportKosLink(found) },
                            onDelete = if (found.isOwnedBy(currentUserId) || isAdmin) {
                                { viewModel.deleteKosListing(found.id) }
                            } else {
                                null
                            },
                            onClose = { viewModel.dismissSheet() },
                        )
                    }
                }
                ActiveBottomSheet.CreateKos -> {
                    CreateKosSheetContent(
                        defaultDistrict = currentDistrict,
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
                        onSubmit = { title, propType, billing, price, rooms, facs, dist, addr, mapsUrl, wa, desc, photos ->
                            viewModel.submitKosListing(
                                title = title,
                                propertyType = propType,
                                billingPeriod = billing,
                                price = price,
                                availableRooms = rooms,
                                facilities = facs,
                                district = dist,
                                address = addr,
                                mapsUrl = mapsUrl,
                                whatsappNumber = wa,
                                description = desc,
                                photoUris = photos,
                                context = context,
                            )
                        },
                        onClose = { viewModel.dismissSheet() },
                    )
                }
            }
        }
    }
}

@Composable
fun SurabayaRadioMiniPlayer(
    playbackState: RadioPlaybackState,
    stationInfo: com.example.data.audio.RadioStationInfo,
    onToggle: () -> Unit,
    onClose: () -> Unit,
    onOpenFull: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenFull() }
            .testTag("radio_mini_player_bar"),
        color = com.example.ui.theme.ToskaPrimaryDeep,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Radio,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(17.dp),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "100.0 FM • Suara Surabaya",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White,
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (playbackState == RadioPlaybackState.PLAYING) {
                                        com.example.ui.theme.AlertCrimson
                                    } else {
                                        com.example.ui.theme.StarGold
                                    }
                                ),
                        )
                    }
                    Text(
                        text = when (playbackState) {
                            RadioPlaybackState.PLAYING -> "Sedang Mengudara (Live)"
                            RadioPlaybackState.BUFFERING -> "Menghubungkan ke Siaran SS 100 FM…"
                            RadioPlaybackState.PAUSED -> "Siaran Dijeda"
                            else -> stationInfo.currentProgram
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("radio_mini_play_pause_btn"),
                ) {
                    Icon(
                        imageVector = if (playbackState == RadioPlaybackState.PLAYING) {
                            Icons.Filled.Pause
                        } else {
                            Icons.Filled.PlayArrow
                        },
                        contentDescription = if (playbackState == RadioPlaybackState.PLAYING) "Jeda" else "Putar",
                        tint = androidx.compose.ui.graphics.Color.White,
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("radio_mini_close_btn"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Tutup Radio",
                        tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}
