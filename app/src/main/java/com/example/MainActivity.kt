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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
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
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.location.LocationHelper
import com.example.data.model.UiState
import com.example.data.pocketbase.PocketBaseClient
import com.example.data.repository.SurabayaRepository
import com.example.data.util.GeminiExplicitImageScanner
import com.example.data.util.configureAppCheck
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.OAuthRedirectBus
import com.example.ui.auth.performSignOut
import com.example.ui.components.SurabayaCompactTopBar
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DestinasiScreen
import com.example.ui.screens.EventScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JasaWargaScreen
import com.example.ui.screens.KejadianScreen
import com.example.ui.screens.KulinerScreen
import com.example.ui.screens.MomenScreen
import com.example.ui.screens.PollingScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StartDirectChatSheetContent
import com.example.ui.screens.TambalBanScreen
import com.example.ui.screens.TemanSekitarScreen
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.data.model.AppNotificationType
import com.example.data.util.SurabayaNotificationHelper
import com.example.data.audio.RadioPlaybackState
import com.example.data.audio.SurabayaRadioPlayer
import com.example.ui.sheets.AdminPendingPollsSheetContent
import com.example.ui.sheets.BookmarksSheetContent
import com.example.ui.sheets.ChatConversationSheetContent
import com.example.ui.sheets.CreateCulinarySheetContent
import com.example.ui.sheets.CreateDestinationSheetContent
import com.example.ui.sheets.CreateIncidentSheetContent
import com.example.ui.sheets.CreateMomentSheetContent
import com.example.ui.sheets.CreatePollSheetContent
import com.example.ui.sheets.CreateServiceSheetContent
import com.example.ui.sheets.CreateTambalBanSheetContent
import com.example.ui.sheets.CulinaryDetailSheetContent
import com.example.ui.sheets.DestinationDetailSheetContent
import com.example.ui.sheets.EventDetailSheetContent
import com.example.ui.sheets.IncidentDetailSheetContent
import com.example.ui.sheets.MomentDetailSheetContent
import com.example.ui.sheets.NotificationsSheetContent
import com.example.ui.sheets.PollDetailSheetContent
import com.example.ui.sheets.ProfileAndPrivacySheetContent
import com.example.ui.sheets.ProposeEventSheetContent
import com.example.ui.sheets.RadioPlayerSheetContent
import com.example.ui.sheets.ServiceDetailSheetContent
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
                val pbClient = PocketBaseClient.getInstance(app)
                SurabayaViewModel(
                    repository = SurabayaRepository(pbClient),
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
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val isScanningPhoto by viewModel.isScanningPhoto.collectAsStateWithLifecycle()
    val myNearbyProfile by viewModel.myNearbyProfile.collectAsStateWithLifecycle()
    val allDiscoverableCitizens by viewModel.allDiscoverableCitizens.collectAsStateWithLifecycle()

    val incidentsState by viewModel.incidentsState.collectAsStateWithLifecycle()
    val incidentComments by viewModel.activeIncidentComments.collectAsStateWithLifecycle()

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
    val servicesState by viewModel.servicesState.collectAsStateWithLifecycle()
    val approvedPollsState by viewModel.approvedPollsState.collectAsStateWithLifecycle()
    val pendingPollsState by viewModel.pendingPollsState.collectAsStateWithLifecycle()
    val activeSelectedPoll by viewModel.activeSelectedPoll.collectAsStateWithLifecycle()
    val activePollComments by viewModel.activePollComments.collectAsStateWithLifecycle()
    val myProposedPolls by viewModel.myProposedPollsState.collectAsStateWithLifecycle()
    val tambalBanSpotsState by viewModel.tambalBanSpotsState.collectAsStateWithLifecycle()
    val activeSelectedTambalBan by viewModel.activeSelectedTambalBan.collectAsStateWithLifecycle()
    val activeTambalBanComments by viewModel.activeTambalBanComments.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Android 13+ Notification Runtime Permission Request
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notifPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                Log.d("MainActivity", "POST_NOTIFICATIONS granted: $isGranted")
            },
        )
        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
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
                    viewModel.openSheet(ActiveBottomSheet.ChatConversation(targetRoom))
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

    // Back button returns to Home tab if on another tab
    BackHandler(enabled = activeSheet == null && selectedTab != MainTab.HOME) {
        viewModel.selectTab(MainTab.HOME)
    }

    val destinations = remember {
        listOf(
            NavDestinationItem(MainTab.HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home, "nav_tab_home"),
            NavDestinationItem(MainTab.MOMEN, R.string.nav_momen, Icons.Filled.PhotoCamera, Icons.Outlined.PhotoCamera, "nav_tab_momen"),
            NavDestinationItem(MainTab.KEJADIAN, R.string.nav_kejadian, Icons.Filled.Newspaper, Icons.Outlined.Newspaper, "nav_tab_kejadian"),
            NavDestinationItem(MainTab.DESTINASI, R.string.nav_destinasi, Icons.Filled.Place, Icons.Outlined.Place, "nav_tab_destinasi"),
            NavDestinationItem(MainTab.CHAT, R.string.nav_chat, Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline, "nav_tab_chat"),
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
                    userInitials = userInitials,
                    avatarUrl = currentAvatarUrl,
                    onOpenBookmarks = { viewModel.openSheet(ActiveBottomSheet.BookmarksList) },
                    onOpenNotifications = { viewModel.openSheet(ActiveBottomSheet.Notifications) },
                    onOpenProfile = { viewModel.openSheet(ActiveBottomSheet.ProfileAndAdmin) },
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

                        NavigationBar(
                            windowInsets = WindowInsets.navigationBars,
                            containerColor = androidx.compose.ui.graphics.Color.White,
                            tonalElevation = 0.dp,
                        ) {
                            destinations.forEach { dest ->
                                val selected = selectedTab == dest.tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(dest.tab) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = stringResource(dest.labelRes),
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(dest.labelRes),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedIconColor = com.example.ui.theme.ToskaPrimaryDeep,
                                        selectedTextColor = com.example.ui.theme.ToskaPrimaryDeep,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                    modifier = Modifier.testTag(dest.testTag),
                                )
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
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes),
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes), style = MaterialTheme.typography.labelSmall) },
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
                                incidentsState = incidentsState,
                                destinationsState = destinationsState,
                                momentsState = momentsState,
                                servicesState = servicesState,
                                pollsState = approvedPollsState,
                                tambalBanSpotsState = tambalBanSpotsState,
                                pendingPollsCount = (pendingPollsState as? UiState.Success)?.data?.size ?: 0,
                                onNavigateTab = { viewModel.selectTab(it) },
                                onOpenIncidentDetail = { viewModel.openSheet(ActiveBottomSheet.IncidentDetail(it)) },
                                onOpenDestinationDetail = { viewModel.openSheet(ActiveBottomSheet.DestinationDetail(it)) },
                                onOpenMomentDetail = { viewModel.openSheet(ActiveBottomSheet.MomentDetail(it)) },
                                onOpenServiceDetail = { viewModel.openSheet(ActiveBottomSheet.ServiceDetail(it)) },
                                onOpenCreateService = { viewModel.openSheet(ActiveBottomSheet.CreateService) },
                                onOpenPollDetail = { viewModel.openPollDetail(it) },
                                onOpenCreatePoll = { viewModel.openCreatePoll() },
                                onOpenAdminPendingPolls = { viewModel.openAdminPendingPolls() },
                                onOpenTambalBanDetail = { viewModel.openTambalBanDetail(it) },
                                onOpenCreateTambalBan = { viewModel.openCreateTambalBan() },
                                onOpenBookmarks = { viewModel.openSheet(ActiveBottomSheet.BookmarksList) },
                                onOpenProfileAndAdmin = { viewModel.openSheet(ActiveBottomSheet.ProfileAndAdmin) },
                                onOpenRadio = { viewModel.openRadioPlayer(autoPlay = true) },
                            )
                            MainTab.MOMEN -> MomenScreen(
                                momentsState = momentsState,
                                currentUserId = currentUserId,
                                isAdmin = isAdmin,
                                onOpenMomentDetail = { viewModel.openSheet(ActiveBottomSheet.MomentDetail(it)) },
                                onOpenCreateMoment = { viewModel.openSheet(ActiveBottomSheet.CreateMoment) },
                                onToggleLike = { moment -> viewModel.toggleMomentLike(moment) },
                                onReportMoment = { viewModel.reportMomentContent(it) },
                                onBlockAuthor = { uid, name -> viewModel.blockCitizen(uid, name) },
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
                            )
                            MainTab.CHAT -> ChatScreen(
                                publicRooms = publicChatRooms,
                                directRooms = directChatRooms,
                                currentUserId = currentUserId,
                                selectedSubTab = chatSubTab,
                                onSelectSubTab = { viewModel.selectChatSubTab(it) },
                                onStartNewDirectChat = { viewModel.openSheet(ActiveBottomSheet.StartDirectChat) },
                                onOpenRoom = { viewModel.openSheet(ActiveBottomSheet.ChatConversation(it)) },
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
                                onBackToHome = { viewModel.selectTab(MainTab.HOME) },
                                onOpenDetail = { viewModel.openTambalBanDetail(it) },
                                onCreateSpot = { viewModel.openCreateTambalBan() },
                                onRefreshLocation = { viewModel.refreshGpsLocation() },
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
            onDismissRequest = { viewModel.dismissSheet() },
            sheetState = sheetState,
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
                            onReportLink = { viewModel.reportEventLink(found) },
                            onAdminClearLinks = { viewModel.adminClearExternalLinks("events", found.id) },
                            onReportContent = { viewModel.reportEventContent(found) },
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
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
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
                        onSubmit = { name, cat, desc, dist, addr, price, openH, closeH, mapsUrl, gofoodUrl, grabfoodUrl, shopeefoodUrl, photoUris ->
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
                        onUnblockUser = { uid -> viewModel.unblockCitizen(uid) },
                        onToggleAdmin = { viewModel.toggleAdminMode(it) },
                        onSaveProfile = { name, dist, bio, ints, rad, disc, avatarUri ->
                            viewModel.saveProfileAndPrivacy(name, dist, bio, ints, rad, disc, avatarUri, context)
                        },
                        onSignOut = {
                            viewModel.dismissSheet()
                            performSignOut(
                                context = context,
                                onSignOutComplete = {},
                                scope = scope,
                            )
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
                                        viewModel.openSheet(ActiveBottomSheet.ChatConversation(foundRoom))
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
                            onClose = { viewModel.dismissSheet() },
                        )
                    }
                }
                ActiveBottomSheet.CreateMoment -> {
                    CreateMomentSheetContent(
                        defaultDistrict = currentDistrict,
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
                        onSubmit = { caption, dist, loc, photos ->
                            viewModel.submitMoment(
                                caption = caption,
                                district = dist,
                                locationName = loc,
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
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
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
                    )
                }
                ActiveBottomSheet.StartDirectChat -> {
                    StartDirectChatSheetContent(
                        citizens = allDiscoverableCitizens,
                        currentUserId = currentUserId,
                        onSelectCitizen = { peer -> viewModel.greetNearbyPeer(peer) },
                    )
                }
                is ActiveBottomSheet.ChatConversation -> {
                    val liveRoom = (publicChatRooms + directChatRooms)
                        .firstOrNull { it.id == sheetTarget.room.id } ?: sheetTarget.room
                    val peerUid = liveRoom.otherParticipantIdFor(currentUserId)
                    ChatConversationSheetContent(
                        room = liveRoom,
                        messages = activeChatMessages,
                        currentUserId = currentUserId,
                        onSendMessage = { txt -> viewModel.sendChatMessage(liveRoom, txt) },
                        onBlockPeer = if (peerUid != null) {
                            { viewModel.blockCitizen(peerUid, liveRoom.displayTitleFor(currentUserId)) }
                        } else {
                            null
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
                            onDeleteSpot = { viewModel.deleteTambalBan(activeTambal.id) },
                            onClose = { viewModel.dismissSheet() },
                        )
                    }
                }
                ActiveBottomSheet.CreateTambalBan -> {
                    CreateTambalBanSheetContent(
                        defaultDistrict = currentDistrict,
                        isSubmitting = isSubmitting,
                        isScanningPhoto = isScanningPhoto,
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
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Radio,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
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
