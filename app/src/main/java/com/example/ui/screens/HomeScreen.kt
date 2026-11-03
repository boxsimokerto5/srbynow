package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.example.ui.sheets.LocationUsageContext
import com.example.ui.sheets.PermissionDisclosureBottomSheet
import com.example.ui.sheets.PermissionSheetMode
import com.example.ui.sheets.hasAnyLocationPermission
import com.example.ui.sheets.rememberLocationPermissionWithDisclosure
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Emergency
import com.example.data.model.TransitRoute
import com.example.data.seed.SurabayaTransitSeed
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.util.Locale
import com.example.R
import com.example.data.model.CitizenPoll
import com.example.data.model.CitizenService
import com.example.data.model.CulinarySpot
import com.example.data.model.IncidentReport
import com.example.data.model.SurabayaDestination
import com.example.data.model.SurabayaEvent
import com.example.data.model.TambalBanSpot
import com.example.data.model.UiState
import com.example.data.model.UserAccount
import com.example.data.model.UserMoment
import com.example.data.util.SurabayaDistricts
import com.example.data.util.calculateDistanceKm
import com.example.data.util.formatDistanceLabel
import com.example.data.util.formatRelativeTimestamp
import com.example.data.util.resolveImageDrawable
import com.example.ui.components.AboutAppDialog
import com.example.ui.components.HomeShortcutGridSection
import com.example.ui.components.HomeShortcutIds
import com.example.ui.components.HomeShortcutItem
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.openEmergencyDialer112
import com.example.ui.components.rateArekSurabayaOnPlayStore
import com.example.ui.components.shareArekSurabayaApp
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.HeaderMintGradient
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.SoftEmergencyLightGradient
import com.example.ui.theme.SuperAppBorder
import com.example.ui.theme.SuperAppCoral
import com.example.ui.theme.SuperAppCoralBg
import com.example.ui.theme.SuperAppCoralBorder
import com.example.ui.theme.SuperAppGold
import com.example.ui.theme.SuperAppGoldBg
import com.example.ui.theme.SuperAppGoldBorder
import com.example.ui.theme.SuperAppGoldText
import com.example.ui.theme.SuperAppGreen
import com.example.ui.theme.SuperAppGreenContainer
import com.example.ui.theme.SuperAppGreenDeep
import com.example.ui.theme.SuperAppMutedText
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep
import com.example.ui.viewmodel.MainTab

@Composable
fun HomeScreen(
    userAccount: UserAccount?,
    isAdmin: Boolean,
    currentDistrict: String,
    userLat: Double = SurabayaDistricts.DEFAULT_LAT,
    userLng: Double = SurabayaDistricts.DEFAULT_LNG,
    isLiveGpsActive: Boolean = false,
    onRefreshGps: () -> Unit = {},
    incidentsState: UiState<List<IncidentReport>>,
    destinationsState: UiState<List<SurabayaDestination>>,
    culinaryState: UiState<List<CulinarySpot>> = UiState.Loading,
    eventsState: UiState<List<SurabayaEvent>> = UiState.Loading,
    momentsState: UiState<List<UserMoment>>,
    servicesState: UiState<List<CitizenService>> = UiState.Loading,
    pollsState: UiState<List<CitizenPoll>> = UiState.Loading,
    tambalBanSpotsState: UiState<List<TambalBanSpot>> = UiState.Loading,
    pendingEventsCount: Int = 0,
    pendingPollsCount: Int = 0,
    flaggedContentCount: Int = 0,
    onNavigateTab: (MainTab) -> Unit,
    onOpenIncidentDetail: (String) -> Unit,
    onOpenDestinationDetail: (String) -> Unit,
    onOpenCulinaryDetail: (String) -> Unit = {},
    onOpenEventDetail: (String) -> Unit = {},
    onOpenMomentDetail: (String) -> Unit,
    onOpenServiceDetail: (String) -> Unit = {},
    onOpenCreateService: () -> Unit = {},
    onOpenPollDetail: (String) -> Unit = {},
    onOpenCreatePoll: () -> Unit = {},
    onOpenAdminPendingPolls: () -> Unit = {},
    onOpenAdminDashboard: () -> Unit = {},
    onOpenTambalBanDetail: (String) -> Unit = {},
    onOpenCreateTambalBan: () -> Unit = {},
    onOpenBookmarks: () -> Unit,
    onOpenProfileAndAdmin: () -> Unit,
    onOpenRadio: () -> Unit = {},
    shortcutOrderIds: List<String> = HomeShortcutIds.DEFAULT_ORDER,
    onSaveShortcutOrder: (List<String>) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val greetingName = userAccount?.displayName?.ifBlank { "Warga Surabaya" } ?: "Warga Surabaya"
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showPermissionOverviewSheet by remember { mutableStateOf(false) }

    val requestOrRefreshGps = rememberLocationPermissionWithDisclosure(
        usageContext = LocationUsageContext.GENERAL_NEARBY,
        onPermissionGranted = onRefreshGps,
    )

    LaunchedEffect(isLiveGpsActive) {
        if (!isLiveGpsActive && hasAnyLocationPermission(context)) {
            onRefreshGps()
        }
    }

    val shortcuts = listOf(
        HomeShortcutItem(
            id = HomeShortcutIds.KULINER,
            title = stringResource(R.string.nav_kuliner),
            subtitle = "GPS Terdekat",
            icon = Icons.Filled.Restaurant,
            badge = "Terdekat",
            testTag = "home_shortcut_kuliner",
            onClick = { onNavigateTab(MainTab.KULINER) },
        ),
        HomeShortcutItem(
            id = HomeShortcutIds.POLLING,
            title = "Polling Warga",
            subtitle = "Suara Rakyat",
            icon = Icons.Filled.HowToVote,
            badge = "Baru",
            testTag = "home_shortcut_polling",
            onClick = { onNavigateTab(MainTab.POLLING) },
        ),
        HomeShortcutItem(
            id = HomeShortcutIds.MOMEN,
            title = stringResource(R.string.nav_momen),
            subtitle = "Foto Warga",
            icon = Icons.Filled.PhotoCamera,
            badge = "Baru",
            testTag = "home_shortcut_momen",
            onClick = { onNavigateTab(MainTab.MOMEN) },
        ),
        HomeShortcutItem(
            id = HomeShortcutIds.LENSA_WARGA,
            title = stringResource(R.string.lensa_warga_title),
            subtitle = "Potret Kota",
            icon = Icons.Filled.CameraAlt,
            badge = "Estetika",
            testTag = "home_shortcut_lensa_warga",
            onClick = { onNavigateTab(MainTab.MOMEN) },
        ),
        HomeShortcutItem(
            id = HomeShortcutIds.RADIO_SS,
            title = stringResource(R.string.radio_ss_title),
            subtitle = "Streaming Live",
            icon = Icons.Filled.Radio,
            badge = "LIVE",
            testTag = "home_shortcut_radio_ss",
            onClick = onOpenRadio,
        ),
        HomeShortcutItem(
            id = HomeShortcutIds.KEJADIAN,
            title = stringResource(R.string.nav_kejadian),
            subtitle = "Lapor & Pantau",
            icon = Icons.Filled.WarningAmber,
            testTag = "home_shortcut_kejadian",
            onClick = { onNavigateTab(MainTab.KEJADIAN) },
        ),
        HomeShortcutItem(
            id = HomeShortcutIds.DESTINASI,
            title = stringResource(R.string.nav_destinasi),
            subtitle = "Wisata Resmi",
            icon = Icons.Filled.Place,
            badge = "Pilihan",
            testTag = "home_shortcut_destinasi",
            onClick = { onNavigateTab(MainTab.DESTINASI) },
        ),
        HomeShortcutItem(
            id = HomeShortcutIds.EVENT,
            title = stringResource(R.string.nav_event),
            subtitle = "Agenda Kota",
            icon = Icons.Filled.CalendarMonth,
            testTag = "home_shortcut_event",
            onClick = { onNavigateTab(MainTab.EVENT) },
        ),
        HomeShortcutItem(
            id = HomeShortcutIds.SEKITAR,
            title = stringResource(R.string.nav_sekitar),
            subtitle = "Cak & Ning",
            icon = Icons.Filled.People,
            testTag = "home_shortcut_sekitar",
            onClick = { onNavigateTab(MainTab.SEKITAR) },
        ),
        HomeShortcutItem(
            id = HomeShortcutIds.CHAT,
            title = stringResource(R.string.nav_chat),
            subtitle = "Ngobrol Warga",
            icon = Icons.Filled.ChatBubble,
            testTag = "home_shortcut_chat",
            onClick = { onNavigateTab(MainTab.CHAT) },
        ),
        HomeShortcutItem(
            id = HomeShortcutIds.JASA,
            title = "Jasa Warga",
            subtitle = "Tukang & Usaha",
            icon = Icons.Filled.Build,
            testTag = "home_shortcut_jasa",
            onClick = { onNavigateTab(MainTab.JASA) },
        ),
        HomeShortcutItem(
            id = HomeShortcutIds.BARKAS,
            title = "Barkas Warga",
            subtitle = "Jual Beli Bekas",
            icon = Icons.Filled.Storefront,
            badge = "COD SBY",
            testTag = "home_shortcut_barkas",
            onClick = { onNavigateTab(MainTab.BARKAS) },
        ),
        HomeShortcutItem(
            id = HomeShortcutIds.KOS,
            title = "Info Kos & Kontrak",
            subtitle = "Bulanan & Tahunan",
            icon = Icons.Filled.HomeWork,
            badge = "Surabaya",
            testTag = "home_shortcut_kos",
            onClick = { onNavigateTab(MainTab.KOS_KONTRAK) },
        ),
        HomeShortcutItem(
            id = HomeShortcutIds.TAMBAL_BAN,
            title = "Tambal Ban",
            subtitle = "Salam 1 Aspal",
            icon = Icons.Filled.TwoWheeler,
            badge = "24 Jam",
            testTag = "home_shortcut_tambal_ban",
            onClick = { onNavigateTab(MainTab.TAMBAL_BAN) },
        ),
        HomeShortcutItem(
            id = HomeShortcutIds.TRANSIT,
            title = "Rute Bis & Feeder",
            subtitle = "WiraWiri & Bus",
            icon = Icons.Filled.DirectionsBus,
            badge = "22 Rute",
            testTag = "home_shortcut_transit",
            onClick = { onNavigateTab(MainTab.TRANSIT) },
        ),
        HomeShortcutItem(
            id = HomeShortcutIds.BOOKMARKS,
            title = stringResource(R.string.btn_bookmarks),
            subtitle = "Favorit Saya",
            icon = Icons.Filled.Bookmark,
            testTag = "home_shortcut_bookmarks",
            onClick = onOpenBookmarks,
        ),
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(bottom = 84.dp),
    ) {
        // 1. Hero Header
        item(key = "home_header") {
            HomeHeaderSection(
                greetingName = greetingName,
                district = currentDistrict,
                isAdmin = isAdmin,
                onOpenProfile = onOpenProfileAndAdmin,
            )
        }

        // 2. Side-by-Side Quick Action Bar: Command Center 112 + SS 100.0 FM Live
        item(key = "home_quick_action_banners") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Emergency 112 Compact Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.28f)),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SoftEmergencyLightGradient),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Emergency,
                                    contentDescription = null,
                                    tint = AlertCrimson,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = "Darurat 112",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AlertCrimson,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(
                                text = "Medis, Laka & Damkar",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilledTonalButton(
                            onClick = { openEmergencyDialer112(context) },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AlertCrimson,
                                contentColor = Color.White,
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .testTag("home_btn_dial_112")
                                .minimumInteractiveComponentSize(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhoneInTalk,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "112", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // SS 100.0 FM Live Compact Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(HeaderMintGradient),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Radio,
                                    contentDescription = null,
                                    tint = ToskaPrimaryDeep,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = "SS 100 FM",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ToskaPrimaryDeep,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(
                                text = "LIVE • Kelana Kota",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilledTonalButton(
                            onClick = onOpenRadio,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = ToskaPrimary,
                                contentColor = Color.White,
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .testTag("home_btn_listen_radio")
                                .minimumInteractiveComponentSize(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text = "Putar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Shortcut Grid (2x4 horizontal swipeable carousel + Admin drag-and-drop layout arrangement)
        item(key = "home_shortcuts_section") {
            HomeShortcutGridSection(
                shortcuts = shortcuts,
                shortcutOrderIds = shortcutOrderIds,
                isAdmin = isAdmin,
                onSaveShortcutOrder = onSaveShortcutOrder,
            )
        }

        // 3a. Resto yummy di dekatmu (Gojek-Style Pastel Card Container)
        item(key = "home_culinary_gojek_container") {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SuperAppCoralBg,
                border = BorderStroke(1.dp, SuperAppCoralBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .testTag("home_culinary_container"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                ) {
                    // Header inside card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(BorderStroke(1.dp, SuperAppCoralBorder), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Restaurant,
                                    contentDescription = null,
                                    tint = SuperAppCoral,
                                    modifier = Modifier.size(17.dp),
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.home_culinary_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1F2937),
                                )
                                Text(
                                    text = "Rekomendasi enak terdekat dari lokasimu",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                    color = SuperAppMutedText,
                                )
                            }
                        }

                        // Circular Arrow Button like Gojek
                        Surface(
                            onClick = { onNavigateTab(MainTab.KULINER) },
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, SuperAppCoralBorder),
                            shadowElevation = 0.5.dp,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("home_culinary_see_all_arrow"),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Lihat Semua Kuliner",
                                    tint = Color(0xFF1F2937),
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // GPS Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            onClick = requestOrRefreshGps,
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(
                                1.dp,
                                if (isLiveGpsActive) SuperAppGreenDeep.copy(alpha = 0.35f) else SuperAppCoralBorder,
                            ),
                            modifier = Modifier.testTag("home_culinary_gps_chip"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = if (isLiveGpsActive) Icons.Filled.GpsFixed else Icons.Filled.NearMe,
                                    contentDescription = null,
                                    tint = if (isLiveGpsActive) SuperAppGreenDeep else SuperAppCoral,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    text = if (isLiveGpsActive) {
                                        "GPS Aktif • Urut dari posisi Anda"
                                    } else {
                                        "Aktifkan GPS untuk akurasi terdekat"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isLiveGpsActive) SuperAppGreenDeep else Color(0xFF374151),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal Cards
                    when (culinaryState) {
                        is UiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = SuperAppCoral, modifier = Modifier.size(28.dp))
                            }
                        }
                        is UiState.Error -> {
                            Text(
                                text = "Gagal memuat kuliner",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                        is UiState.Success -> {
                            val list = remember(culinaryState.data, userLat, userLng) {
                                culinaryState.data.sortedWith(
                                    compareBy<CulinarySpot> { calculateDistanceKm(userLat, userLng, it.lat, it.lng) }
                                        .thenByDescending { it.ratingAvg }
                                )
                            }
                            if (list.isEmpty()) {
                                Text(
                                    text = "Belum ada kuliner terdaftar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            } else {
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("home_culinary_lazy_row"),
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    itemsIndexed(list, key = { _, spot -> spot.id }) { index, spot ->
                                        val distanceKm = calculateDistanceKm(userLat, userLng, spot.lat, spot.lng)
                                        HomeCulinaryCard(
                                            spot = spot,
                                            distanceKm = distanceKm,
                                            isNearestTopSpot = index == 0,
                                            onClick = { onOpenCulinaryDetail(spot.id) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3b. Ruang Promosi Jasa Warga (Featured Community Services Showcase)
        item(key = "home_services_section") {
            Spacer(modifier = Modifier.height(4.dp))
            HomeSectionHeader(
                title = "Ruang Promosi Jasa Warga",
                subtitle = "AC, ledeng, servis & keahlian Arek Surabaya",
                onSeeAllClick = { onNavigateTab(MainTab.JASA) },
            )

            // Anti-Fraud / Anti-DP Quick Alert Banner
            Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftEmergencyLightGradient)
                        .clickable { onNavigateTab(MainTab.JASA) },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = AlertCrimson,
                            modifier = Modifier.size(17.dp),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tips Aman: Dilarang Transfer DP Terlebih Dahulu!",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AlertCrimson,
                            )
                            Text(
                                text = "Bayar hanya setelah pekerjaan selesai & Anda telah memeriksa hasilnya (COD).",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            when (servicesState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = ToskaPrimaryDeep, modifier = Modifier.size(24.dp))
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = "Belum ada data promosi jasa warga",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp),
                    )
                }
                is UiState.Success -> {
                    val list = servicesState.data
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(list.take(6), key = { it.id }) { service ->
                            HomeServiceCard(
                                service = service,
                                onClick = { onOpenServiceDetail(service.id) },
                            )
                        }

                        item(key = "create_service_card") {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = ToskaPrimary.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .width(142.dp)
                                    .height(106.dp)
                                    .clickable { onOpenCreateService() },
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(ToskaPrimary),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(17.dp),
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                        text = "Pasang Jasa Anda",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ToskaPrimaryDeep,
                                        textAlign = TextAlign.Center,
                                    )
                                    Text(
                                        text = "Gratis untuk Warga",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3c. Polling Warga & Suara Kota
        item(key = "home_polls_section") {
            Spacer(modifier = Modifier.height(4.dp))
            HomeSectionHeader(
                title = "Polling Warga & Suara Kota",
                subtitle = "Beri suara & tentukan kebijakan kota Surabaya",
                onSeeAllClick = { onNavigateTab(MainTab.POLLING) },
            )

            if (isAdmin && pendingPollsCount > 0) {
                Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(HeaderMintGradient)
                            .clickable { onOpenAdminPendingPolls() },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Security,
                                    contentDescription = null,
                                    tint = ToskaPrimaryDeep,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$pendingPollsCount Usulan Polling Menunggu Moderasi",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ToskaPrimaryDeep,
                                )
                            }
                            Text(
                                text = "Periksa →",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ToskaPrimaryDeep,
                            )
                        }
                    }
                }
            }

            when (pollsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = ToskaPrimaryDeep, modifier = Modifier.size(26.dp))
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = "Belum dapat memuat polling warga",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                is UiState.Success -> {
                    val polls = pollsState.data
                    if (polls.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "Belum Ada Polling Aktif",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ayo ajukan topik atau aspirasi pembangunan yang ingin kamu tanyakan ke warga Surabaya!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = onOpenCreatePoll,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimary),
                                ) {
                                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Buat Usulan Polling", fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(polls, key = { it.id }) { poll ->
                                HomePollCard(
                                    poll = poll,
                                    onClick = { onOpenPollDetail(poll.id) },
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3d. Tambal Ban Terdekat (Salam Satu Aspal)
        item(key = "home_tambal_ban_section") {
            Spacer(modifier = Modifier.height(4.dp))
            HomeSectionHeader(
                title = "Tambal Ban Terdekat",
                subtitle = "Salam Satu Aspal • Solusi saat ban bocor di Surabaya",
                onSeeAllClick = { onNavigateTab(MainTab.TAMBAL_BAN) },
            )

            when (tambalBanSpotsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = ToskaPrimaryDeep, modifier = Modifier.size(24.dp))
                    }
                }
                is UiState.Error -> {}
                is UiState.Success -> {
                    val spots = tambalBanSpotsState.data
                    if (spots.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(spots.take(6), key = { it.id }) { spot ->
                                HomeTambalBanCard(
                                    spot = spot,
                                    onClick = { onOpenTambalBanDetail(spot.id) },
                                )
                            }

                            item(key = "create_tambal_ban_card") {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = ToskaPrimary.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .width(142.dp)
                                        .height(100.dp)
                                        .clickable { onOpenCreateTambalBan() },
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(ToskaPrimary),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Tambah Tambal Ban",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ToskaPrimaryDeep,
                                            textAlign = TextAlign.Center,
                                        )
                                        Text(
                                            text = "Bantu Sesama",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3e. Rute Bis & Feeder Surabaya
        item(key = "home_transit_section") {
            Spacer(modifier = Modifier.height(4.dp))
            HomeSectionHeader(
                title = "Rute Bis & Feeder Surabaya",
                subtitle = "WiraWiri • Suroboyo Bus • Trans Semanggi",
                onSeeAllClick = { onNavigateTab(MainTab.TRANSIT) },
            )

            val previewRoutes = remember {
                SurabayaTransitSeed.allRoutes.filter {
                    it.code in listOf("FD01", "FD06", "R1 / R2", "2K / K2L", "FD02", "R7 / R8")
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(previewRoutes, key = { it.id }) { route ->
                    HomeTransitPreviewCard(
                        route = route,
                        onClick = { onNavigateTab(MainTab.TRANSIT) },
                    )
                }

                item(key = "see_all_transit_card") {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ToskaPrimary.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .width(142.dp)
                            .height(112.dp)
                            .clickable { onNavigateTab(MainTab.TRANSIT) },
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(ToskaPrimary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DirectionsBus,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp),
                                )
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = "Semua Rute (22)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ToskaPrimaryDeep,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = "Cek Tarif & Halte",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }

        // 4. Highlight: Kabar Kejadian Terhangat
        item(key = "home_incidents_header") {
            Spacer(modifier = Modifier.height(4.dp))
            HomeSectionHeader(
                title = stringResource(R.string.home_highlights_title),
                subtitle = "Update situasi kota dari warga & petugas",
                onSeeAllClick = { onNavigateTab(MainTab.KEJADIAN) },
            )
        }

        item(key = "home_incidents_list") {
            when (incidentsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = ToskaPrimaryDeep, modifier = Modifier.size(28.dp))
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = "Belum ada laporan terkini",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
                is UiState.Success -> {
                    val topIncidents = incidentsState.data.take(6)
                    if (topIncidents.isEmpty()) {
                        Text(
                            text = "Situasi Surabaya aman dan kondusif",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    } else {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(topIncidents, key = { it.id }) { incident ->
                                HomeIncidentCard(
                                    incident = incident,
                                    onClick = { onOpenIncidentDetail(incident.id) },
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Destinasi Ikonik Surabaya
        item(key = "home_destinations_header") {
            Spacer(modifier = Modifier.height(4.dp))
            HomeSectionHeader(
                title = stringResource(R.string.home_destinations_title),
                subtitle = "Wisata sejarah, taman kota & edukasi resmi Surabaya",
                onSeeAllClick = { onNavigateTab(MainTab.DESTINASI) },
            )
        }

        item(key = "home_destinations_carousel") {
            when (destinationsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = ToskaPrimaryDeep, modifier = Modifier.size(28.dp))
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = "Gagal memuat destinasi",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 10.dp),
                    )
                }
                is UiState.Success -> {
                    val list = destinationsState.data
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(list, key = { it.id }) { dest ->
                            HomeDestinationCard(
                                destination = dest,
                                onClick = { onOpenDestinationDetail(dest.id) },
                            )
                        }
                    }
                }
            }
        }

        // 5c. Agenda & Event Surabaya
        item(key = "home_events_header") {
            Spacer(modifier = Modifier.height(4.dp))
            HomeSectionHeader(
                title = stringResource(R.string.home_events_title),
                subtitle = "Festival budaya, bazar UMKM, musik & kegiatan kota",
                onSeeAllClick = { onNavigateTab(MainTab.EVENT) },
            )
        }

        item(key = "home_events_carousel") {
            when (eventsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = ToskaPrimaryDeep, modifier = Modifier.size(28.dp))
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = "Gagal memuat event",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 10.dp),
                    )
                }
                is UiState.Success -> {
                    val list = eventsState.data
                    if (list.isEmpty()) {
                        Text(
                            text = "Belum ada event terjadwal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp),
                        )
                    } else {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(list, key = { it.id }) { event ->
                                HomeEventCard(
                                    event = event,
                                    onClick = { onOpenEventDetail(event.id) },
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Momen Warga Terkini
        item(key = "home_moments_header") {
            Spacer(modifier = Modifier.height(4.dp))
            HomeSectionHeader(
                title = stringResource(R.string.home_moments_title),
                subtitle = "Potret keseharian warga di sudut-sudut kota",
                onSeeAllClick = { onNavigateTab(MainTab.MOMEN) },
            )
        }

        item(key = "home_moments_carousel") {
            when (momentsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = ToskaPrimaryDeep, modifier = Modifier.size(28.dp))
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = "Gagal memuat momen warga",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 10.dp),
                    )
                }
                is UiState.Success -> {
                    val list = momentsState.data
                    if (list.isEmpty()) {
                        Text(
                            text = "Belum ada momen dibagikan hari ini",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp),
                        )
                    } else {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(list, key = { it.id }) { moment ->
                                HomeMomentCard(
                                    moment = moment,
                                    onClick = { onOpenMomentDetail(moment.id) },
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Community Support & Footer Banner
        item(key = "home_community_footer") {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF2FBF6),
                border = BorderStroke(1.dp, ToskaPrimaryDeep.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .testTag("home_footer_support_card"),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ToskaPrimaryDeep),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Column {
                            Text(
                                text = "Arek Surabaya • Bangga Kota Pahlawan",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ToskaPrimaryDeep,
                            )
                            Text(
                                text = "Bantu aplikasi ini berkembang untuk warga",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = { rateArekSurabayaOnPlayStore(context) },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimaryDeep),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_btn_rate_playstore"),
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Beri Rating", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { shareArekSurabayaApp(context) },
                            shape = CircleShape,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_btn_share_app"),
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bagikan", fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Tentang Aplikasi",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ToskaPrimaryDeep,
                            modifier = Modifier
                                .clickable { showAboutDialog = true }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .testTag("home_footer_link_about"),
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                        Text(
                            text = "Kebijakan Privasi",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ToskaPrimaryDeep,
                            modifier = Modifier
                                .clickable { showPrivacyDialog = true }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .testTag("home_footer_link_privacy"),
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                        Text(
                            text = stringResource(R.string.perm_footer_link_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ToskaPrimaryDeep,
                            modifier = Modifier
                                .clickable { showPermissionOverviewSheet = true }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .testTag("home_footer_link_permissions"),
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                        Text(
                            text = "v1.0.0",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        AboutAppDialog(onDismiss = { showAboutDialog = false })
    }
    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }
    if (showPermissionOverviewSheet) {
        PermissionDisclosureBottomSheet(
            mode = PermissionSheetMode.Overview,
            onDismiss = { showPermissionOverviewSheet = false },
            onConfirmLocationRequest = onRefreshGps,
        )
    }
}

@Composable
private fun HomeHeaderSection(
    greetingName: String,
    district: String,
    isAdmin: Boolean,
    onOpenProfile: () -> Unit,
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderMintGradient)
            .padding(top = 8.dp, start = 12.dp, end = 12.dp, bottom = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Surabaya Hari Ini",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = ToskaOnPrimaryContainer,
                    )
                    if (isAdmin) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ToskaPrimaryDeep,
                        ) {
                            Text(
                                text = "ADMIN",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Halo Cak & Ning $greetingName • $district",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            IconButton(
                onClick = onOpenProfile,
                modifier = Modifier
                    .size(38.dp)
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .testTag("home_btn_profile"),
            ) {
                Icon(
                    imageVector = Icons.Filled.AdminPanelSettings,
                    contentDescription = "Pengaturan Akun & Admin",
                    tint = ToskaPrimaryDeep,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    }
}



@Composable
private fun HomeSectionHeader(
    title: String,
    subtitle: String,
    onSeeAllClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = onSeeAllClick)
                .padding(horizontal = 4.dp, vertical = 2.dp),
        ) {
            Text(
                text = "Semua",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = ToskaPrimaryDeep,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = ToskaPrimaryDeep,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun HomeIncidentCard(
    incident: IncidentReport,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.7f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .width(188.dp)
            .testTag("home_incident_${incident.id}"),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
            ) {
                val photoModel = incident.allPhotos.firstOrNull()?.ifBlank { null }
                    ?: resolveImageDrawable(incident.imageKey)
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = incident.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    color = if (incident.severity == "DARURAT") AlertCrimson else ToskaPrimaryDeep.copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.TopStart),
                ) {
                    Text(
                        text = incident.category,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(5.dp),
                ) {
                    Text(
                        text = formatRelativeTimestamp(incident.createdAt),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    )
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = incident.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(1.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = "Kec. ${incident.district}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ToskaPrimaryDeep,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeDestinationCard(
    destination: SurabayaDestination,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.7f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .width(188.dp)
            .testTag("home_dest_${destination.id}"),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
            ) {
                val photoModel = destination.allPhotos.firstOrNull()?.ifBlank { null } ?: resolveImageDrawable(destination.imageKey)
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = destination.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    color = ToskaPrimaryDeep.copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.TopStart),
                ) {
                    Text(
                        text = destination.category,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(5.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(11.dp),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", destination.ratingAvg),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = destination.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${destination.district} • ${destination.ticketPrice}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ToskaPrimaryDeep,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun HomeCulinaryCard(
    spot: CulinarySpot,
    distanceKm: Double = 0.0,
    isNearestTopSpot: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isNearestTopSpot) 1.5.dp else 1.dp,
            color = if (isNearestTopSpot) ToskaPrimary.copy(alpha = 0.6f) else OutlineVariantLight.copy(alpha = 0.7f),
        ),
        shadowElevation = 2.dp,
        modifier = Modifier
            .width(188.dp)
            .testTag("home_culinary_${spot.id}"),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
            ) {
                val photoModel = spot.allPhotos.firstOrNull()?.ifBlank { null } ?: resolveImageDrawable(spot.imageKey)
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = spot.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    color = if (isNearestTopSpot) OpenGreenText.copy(alpha = 0.94f) else ToskaPrimaryDeep.copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.TopStart),
                ) {
                    val catLabel = if (isNearestTopSpot) {
                        stringResource(R.string.kuliner_nearest_rank_badge, 1)
                    } else {
                        when (spot.category) {
                            "RAWON_SOTO" -> "Rawon & Soto"
                            "PENYETAN_SAMBAL" -> "Penyetan"
                            "KOPI_NONGKRONG" -> "Kopi & Warkop"
                            "JAJANAN_MALAM" -> "Jajanan Malam"
                            else -> "Legendaris"
                        }
                    }
                    Text(
                        text = catLabel,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(bottomStart = 8.dp),
                    color = Color.Black.copy(alpha = 0.72f),
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Text(
                        text = "📍 ${formatDistanceLabel(distanceKm)}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("home_culinary_distance_${spot.id}"),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(5.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(11.dp),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f", spot.ratingAvg),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = spot.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${spot.district} • ${formatDistanceLabel(distanceKm)} • ${spot.priceRange}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ToskaPrimaryDeep,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun HomeEventCard(
    event: SurabayaEvent,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.7f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .width(188.dp)
            .testTag("home_event_${event.id}"),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
            ) {
                val photoModel = event.allPhotos.firstOrNull()?.ifBlank { null } ?: resolveImageDrawable(event.imageKey)
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    color = Color(0xFF1E88E5).copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.TopStart),
                ) {
                    val catLabel = when (event.category) {
                        "FESTIVAL" -> "Festival"
                        "MUSIK_SENI" -> "Musik & Seni"
                        "KULINER_BAZAR" -> "Bazar UMKM"
                        "OLAHRAGA" -> "Olahraga"
                        else -> "Komunitas"
                    }
                    Text(
                        text = catLabel,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(5.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp),
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = event.eventDateText.ifBlank { "Segera" },
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${event.venueName.ifBlank { event.district }} • ${event.priceLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF1E88E5),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun HomeMomentCard(
    moment: UserMoment,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.7f)),
        modifier = Modifier
            .width(126.dp)
            .testTag("home_moment_${moment.id}"),
    ) {
        Column {
            val photoModel = moment.allPhotos.firstOrNull()?.ifBlank { null } ?: resolveImageDrawable(moment.imageKey)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoModel)
                    .crossfade(true)
                    .build(),
                contentDescription = moment.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp),
            )
            Column(modifier = Modifier.padding(7.dp)) {
                Text(
                    text = moment.authorName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = moment.district,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = AlertCrimson,
                            modifier = Modifier.size(11.dp),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${moment.likesCount}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AlertCrimson,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeServiceCard(
    service: CitizenService,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .width(196.dp)
            .clickable { onClick() }
            .testTag("home_service_card_${service.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.padding(9.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = ToskaPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = formatServiceCategory(service.category),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontWeight = FontWeight.Bold,
                        color = ToskaPrimaryDeep,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = ToskaPrimary,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = "Warga",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontWeight = FontWeight.Bold,
                        color = ToskaPrimary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = service.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = "${service.providerName} • ${service.district}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = ToskaPrimaryDeep,
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(
                    text = service.priceDisplay,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
fun HomePollCard(
    poll: CitizenPoll,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .width(240.dp)
            .clickable(onClick = onClick)
            .testTag("home_poll_card_${poll.id}"),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ToskaPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f, fill = false),
                ) {
                    Text(
                        text = poll.category.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ToskaPrimaryDeep,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.HowToVote,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${poll.totalVotes} Suara",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ToskaPrimaryDeep,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = poll.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(4.dp))

            poll.options.take(2).forEach { opt ->
                val pct = poll.percentageForOption(opt.id)
                Column(modifier = Modifier.padding(vertical = 1.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = opt.text,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "%.0f%%".format(pct),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ToskaPrimaryDeep,
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { pct / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = ToskaPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "💬 ${poll.commentCount} Komentar",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Ikut Polling →",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ToskaPrimaryDeep,
                )
            }
        }
    }
}

@Composable
private fun HomeTambalBanCard(
    spot: TambalBanSpot,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineVariantLight),
        shadowElevation = 1.dp,
        modifier = modifier
            .width(196.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("home_tambal_card_${spot.id}"),
    ) {
        Column(
            modifier = Modifier.padding(9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                spot.formattedDistance?.let { distStr ->
                    Surface(shape = CircleShape, color = OpenGreenBg) {
                        Text(
                            text = "📍 $distStr",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OpenGreenText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                        )
                    }
                }
                if (spot.is24Hours) {
                    Surface(shape = CircleShape, color = ToskaPrimary.copy(alpha = 0.12f)) {
                        Text(
                            text = "24 Jam",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ToskaPrimaryDeep,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                        )
                    }
                }
            }

            Text(
                text = spot.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = ToskaPrimary,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = "Kec. ${spot.district}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = spot.vehicleSupportText + if (spot.isTubeless) " • Tubeless" else "",
                style = MaterialTheme.typography.labelSmall,
                color = ToskaPrimaryDeep,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun HomeTransitPreviewCard(
    route: TransitRoute,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
            .width(196.dp)
            .height(112.dp)
            .clickable { onClick() }
            .testTag("home_transit_card_${route.code}"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(9.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    color = Color(route.badgeColorHex),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = route.code,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                }

                Text(
                    text = route.fare.split("•").firstOrNull()?.trim() ?: "Rp 5.000",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ToskaPrimaryDeep,
                )
            }

            Column {
                Text(
                    text = route.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = route.fleetType,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = route.operatingHours,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Lihat Rute →",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Bold,
                    color = ToskaPrimary,
                )
            }
        }
    }
}



