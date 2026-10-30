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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.example.data.util.OnDeviceLiteRtImageScanner
import com.example.data.util.configureAppCheck
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.OAuthRedirectBus
import com.example.ui.auth.performSignOut
import com.example.ui.components.SurabayaCompactTopBar
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.EventScreen
import com.example.ui.screens.KejadianScreen
import com.example.ui.screens.KulinerScreen
import com.example.ui.screens.StartDirectChatSheetContent
import com.example.ui.screens.TemanSekitarScreen
import com.example.ui.sheets.BookmarksSheetContent
import com.example.ui.sheets.ChatConversationSheetContent
import com.example.ui.sheets.CreateCulinarySheetContent
import com.example.ui.sheets.CreateIncidentSheetContent
import com.example.ui.sheets.CulinaryDetailSheetContent
import com.example.ui.sheets.EventDetailSheetContent
import com.example.ui.sheets.IncidentDetailSheetContent
import com.example.ui.sheets.ProfileAndPrivacySheetContent
import com.example.ui.sheets.ProposeEventSheetContent
import com.example.ui.theme.SurabayaHariIniTheme
import com.example.ui.viewmodel.ActiveBottomSheet
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.SurabayaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        OAuthRedirectBus.handleIncomingIntent(intent)
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
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val pbClient = remember(context) { PocketBaseClient.getInstance(context) }
    val session by pbClient.sessionFlow.collectAsStateWithLifecycle()
    val activeSession = session

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
                    imageSafetyScanner = OnDeviceLiteRtImageScanner(app),
                )
            }
        },
    )

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val chatSubTab by viewModel.chatSubTab.collectAsStateWithLifecycle()
    val activeSheet by viewModel.activeSheet.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val userAccount by viewModel.userAccount.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
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

    val culinaryState by viewModel.culinaryState.collectAsStateWithLifecycle()
    val culinaryReviews by viewModel.activeCulinaryReviews.collectAsStateWithLifecycle()

    val nearbyPeersState by viewModel.nearbyPeersState.collectAsStateWithLifecycle()
    val publicChatRooms by viewModel.publicChatRooms.collectAsStateWithLifecycle()
    val directChatRooms by viewModel.directChatRooms.collectAsStateWithLifecycle()
    val activeChatMessages by viewModel.activeChatMessages.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        val msg = snackbarMsg
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Back button returns to Kejadian home tab if on another tab
    BackHandler(enabled = activeSheet == null && selectedTab != MainTab.KEJADIAN) {
        viewModel.selectTab(MainTab.KEJADIAN)
    }

    val destinations = remember {
        listOf(
            NavDestinationItem(MainTab.KEJADIAN, R.string.nav_kejadian, Icons.Filled.Newspaper, Icons.Outlined.Newspaper, "nav_tab_kejadian"),
            NavDestinationItem(MainTab.EVENT, R.string.nav_event, Icons.Filled.Event, Icons.Outlined.Event, "nav_tab_event"),
            NavDestinationItem(MainTab.KULINER, R.string.nav_kuliner, Icons.Filled.Restaurant, Icons.Outlined.Restaurant, "nav_tab_kuliner"),
            NavDestinationItem(MainTab.SEKITAR, R.string.nav_sekitar, Icons.Filled.NearMe, Icons.Outlined.NearMe, "nav_tab_sekitar"),
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
                    userInitials = userInitials,
                    avatarUrl = currentAvatarUrl,
                    onOpenBookmarks = { viewModel.openSheet(ActiveBottomSheet.BookmarksList) },
                    onOpenProfile = { viewModel.openSheet(ActiveBottomSheet.ProfileAndAdmin) },
                )
            },
            bottomBar = {
                if (!isWideTablet) {
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
                            }
                        },
                        onRemoveBookmark = { bm ->
                            viewModel.toggleBookmark(bm.targetId, bm.targetType, bm.title, bm.subtitle, bm.district)
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
            }
        }
    }
}
