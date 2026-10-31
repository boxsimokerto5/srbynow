package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.CitizenService
import com.example.data.model.IncidentReport
import com.example.data.model.SurabayaDestination
import com.example.data.model.UiState
import com.example.data.model.UserAccount
import com.example.data.model.UserMoment
import com.example.data.util.formatRelativeTimestamp
import com.example.data.util.resolveImageDrawable
import com.example.ui.components.AboutAppDialog
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.openEmergencyDialer112
import com.example.ui.components.rateArekSurabayaOnPlayStore
import com.example.ui.components.shareArekSurabayaApp
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.HeaderMintGradient
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.SoftEmergencyLightGradient
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep
import com.example.ui.viewmodel.MainTab

data class HomeShortcutItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badge: String? = null,
    val testTag: String,
    val onClick: () -> Unit,
)

@Composable
fun HomeScreen(
    userAccount: UserAccount?,
    isAdmin: Boolean,
    currentDistrict: String,
    incidentsState: UiState<List<IncidentReport>>,
    destinationsState: UiState<List<SurabayaDestination>>,
    momentsState: UiState<List<UserMoment>>,
    servicesState: UiState<List<CitizenService>> = UiState.Loading,
    onNavigateTab: (MainTab) -> Unit,
    onOpenIncidentDetail: (String) -> Unit,
    onOpenDestinationDetail: (String) -> Unit,
    onOpenMomentDetail: (String) -> Unit,
    onOpenServiceDetail: (String) -> Unit = {},
    onOpenCreateService: () -> Unit = {},
    onOpenBookmarks: () -> Unit,
    onOpenProfileAndAdmin: () -> Unit,
    onOpenRadio: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val greetingName = userAccount?.displayName?.ifBlank { "Warga Surabaya" } ?: "Warga Surabaya"
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val shortcuts = listOf(
        HomeShortcutItem(
            title = stringResource(R.string.nav_momen),
            subtitle = "Foto Warga",
            icon = Icons.Filled.PhotoCamera,
            badge = "Baru",
            testTag = "home_shortcut_momen",
            onClick = { onNavigateTab(MainTab.MOMEN) },
        ),
        HomeShortcutItem(
            title = stringResource(R.string.nav_kejadian),
            subtitle = "Lapor & Pantau",
            icon = Icons.Filled.WarningAmber,
            testTag = "home_shortcut_kejadian",
            onClick = { onNavigateTab(MainTab.KEJADIAN) },
        ),
        HomeShortcutItem(
            title = stringResource(R.string.radio_ss_title),
            subtitle = "Streaming Live",
            icon = Icons.Filled.Radio,
            badge = "LIVE",
            testTag = "home_shortcut_radio_ss",
            onClick = onOpenRadio,
        ),
        HomeShortcutItem(
            title = stringResource(R.string.nav_destinasi),
            subtitle = "Wisata Resmi",
            icon = Icons.Filled.Place,
            badge = "Pilihan",
            testTag = "home_shortcut_destinasi",
            onClick = { onNavigateTab(MainTab.DESTINASI) },
        ),
        HomeShortcutItem(
            title = stringResource(R.string.nav_event),
            subtitle = "Agenda Kota",
            icon = Icons.Filled.CalendarMonth,
            testTag = "home_shortcut_event",
            onClick = { onNavigateTab(MainTab.EVENT) },
        ),
        HomeShortcutItem(
            title = stringResource(R.string.nav_kuliner),
            subtitle = "Makan Enak",
            icon = Icons.Filled.Restaurant,
            testTag = "home_shortcut_kuliner",
            onClick = { onNavigateTab(MainTab.KULINER) },
        ),
        HomeShortcutItem(
            title = stringResource(R.string.nav_sekitar),
            subtitle = "Cak & Ning",
            icon = Icons.Filled.People,
            testTag = "home_shortcut_sekitar",
            onClick = { onNavigateTab(MainTab.SEKITAR) },
        ),
        HomeShortcutItem(
            title = stringResource(R.string.nav_chat),
            subtitle = "Ngobrol Warga",
            icon = Icons.Filled.ChatBubble,
            testTag = "home_shortcut_chat",
            onClick = { onNavigateTab(MainTab.CHAT) },
        ),
        HomeShortcutItem(
            title = "Jasa Warga",
            subtitle = "Tukang & Usaha",
            icon = Icons.Filled.Build,
            testTag = "home_shortcut_jasa",
            onClick = { onNavigateTab(MainTab.JASA) },
        ),
        HomeShortcutItem(
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
        contentPadding = PaddingValues(bottom = 96.dp),
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

        // 2. Emergency 112 Card
        item(key = "home_emergency_card") {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SoftEmergencyLightGradient),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AlertCrimson.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Emergency,
                                    contentDescription = null,
                                    tint = AlertCrimson,
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Command Center 112 Surabaya",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertCrimson,
                                )
                                Text(
                                    text = "Bebas pulsa 24 jam untuk darurat medis, laka & damkar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = { openEmergencyDialer112(context) },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AlertCrimson,
                                contentColor = Color.White,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .testTag("home_btn_dial_112")
                                .minimumInteractiveComponentSize(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhoneInTalk,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "112", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2b. Radio Suara Surabaya 100.0 FM Live Banner
        item(key = "home_radio_banner") {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(HeaderMintGradient),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(ToskaPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Radio,
                                    contentDescription = null,
                                    tint = ToskaPrimaryDeep,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = "SS 100.0 FM Live",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ToskaPrimaryDeep,
                                    )
                                    Surface(
                                        color = AlertCrimson,
                                        shape = RoundedCornerShape(4.dp),
                                    ) {
                                        Text(
                                            text = "ON AIR",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                        )
                                    }
                                }
                                Text(
                                    text = "Kelana Kota • Pantauan lalu lintas & berita realtime",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = onOpenRadio,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = ToskaPrimary,
                                contentColor = Color.White,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .testTag("home_btn_listen_radio")
                                .minimumInteractiveComponentSize(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Putar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Shortcut Grid (9 shortcuts in 3 rows of 3)
        item(key = "home_shortcuts_section") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.home_shortcuts_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${shortcuts.size} Layanan Warga",
                        style = MaterialTheme.typography.labelSmall,
                        color = ToskaPrimaryDeep,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Render grid 3 columns
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val rows = shortcuts.chunked(3)
                    for (rowItems in rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            for (item in rowItems) {
                                HomeShortcutButton(
                                    item = item,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            val emptySlots = 3 - rowItems.size
                            repeat(emptySlots) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // 3b. Ruang Promosi Jasa Warga (Featured Community Services Showcase)
        item(key = "home_services_section") {
            Spacer(modifier = Modifier.height(8.dp))
            HomeSectionHeader(
                title = "Ruang Promosi Jasa Warga",
                subtitle = "AC, ledeng, servis & keahlian Arek Surabaya",
                onSeeAllClick = { onNavigateTab(MainTab.JASA) },
            )

            // Anti-Fraud / Anti-DP Quick Alert Banner
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SoftEmergencyLightGradient)
                        .clickable { onNavigateTab(MainTab.JASA) },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = AlertCrimson,
                            modifier = Modifier.size(20.dp),
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

            Spacer(modifier = Modifier.height(8.dp))

            when (servicesState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
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
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                is UiState.Success -> {
                    val list = servicesState.data
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(list.take(6), key = { it.id }) { service ->
                            HomeServiceCard(
                                service = service,
                                onClick = { onOpenServiceDetail(service.id) },
                            )
                        }

                        item(key = "create_service_card") {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = ToskaPrimary.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(130.dp)
                                    .clickable { onOpenCreateService() },
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(ToskaPrimary),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
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

        // 4. Highlight: Kabar Kejadian Terhangat
        item(key = "home_incidents_header") {
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
                            .height(120.dp),
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
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                is UiState.Success -> {
                    val topIncidents = incidentsState.data.take(3)
                    if (topIncidents.isEmpty()) {
                        Text(
                            text = "Situasi Surabaya aman dan kondusif",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            for (incident in topIncidents) {
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
            Spacer(modifier = Modifier.height(12.dp))
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
                            .height(140.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = ToskaPrimaryDeep, modifier = Modifier.size(28.dp))
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = "Gagal memuat destinasi",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                is UiState.Success -> {
                    val list = destinationsState.data
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
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

        // 6. Momen Warga Terkini
        item(key = "home_moments_header") {
            Spacer(modifier = Modifier.height(12.dp))
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
                            .height(120.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = ToskaPrimaryDeep, modifier = Modifier.size(28.dp))
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = "Gagal memuat momen warga",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                is UiState.Success -> {
                    val list = momentsState.data
                    if (list.isEmpty()) {
                        Text(
                            text = "Belum ada momen dibagikan hari ini",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    } else {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
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
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF2FBF6),
                border = BorderStroke(1.dp, ToskaPrimaryDeep.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("home_footer_support_card"),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
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
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
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
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = ToskaOnPrimaryContainer,
                    )
                    if (isAdmin) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ToskaPrimaryDeep,
                        ) {
                            Text(
                                text = "ADMIN",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Halo Cak & Ning $greetingName • $district",
                        style = MaterialTheme.typography.bodySmall,
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
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .testTag("home_btn_profile"),
            ) {
                Icon(
                    imageVector = Icons.Filled.AdminPanelSettings,
                    contentDescription = "Pengaturan Akun & Admin",
                    tint = ToskaPrimaryDeep,
                )
            }
        }
    }
}

@Composable
private fun HomeShortcutButton(
    item: HomeShortcutItem,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = item.onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.6f)),
        shadowElevation = 1.dp,
        modifier = modifier
            .testTag(item.testTag)
            .minimumInteractiveComponentSize(),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MintSoftGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (item.badge != null) {
                Surface(
                    shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 16.dp),
                    color = AlertCrimson,
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Text(
                        text = item.badge,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    )
                }
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = onSeeAllClick)
                .padding(4.dp),
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
                modifier = Modifier.size(16.dp),
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
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_incident_${incident.id}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val photoModel = incident.allPhotos.firstOrNull()?.ifBlank { null } ?: resolveImageDrawable(incident.imageKey)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoModel)
                    .crossfade(true)
                    .build(),
                contentDescription = incident.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (incident.severity == "DARURAT") AlertCrimson.copy(alpha = 0.15f)
                        else ToskaPrimaryDeep.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = incident.category,
                            color = if (incident.severity == "DARURAT") AlertCrimson else ToskaPrimaryDeep,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    Text(
                        text = "• ${incident.district}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = incident.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatRelativeTimestamp(incident.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.7f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .width(220.dp)
            .testTag("home_dest_${destination.id}"),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
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
                    shape = RoundedCornerShape(bottomEnd = 10.dp),
                    color = ToskaPrimaryDeep.copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.TopStart),
                ) {
                    Text(
                        text = destination.category,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(12.dp),
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
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = destination.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${destination.district} • ${destination.ticketPrice}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ToskaPrimaryDeep,
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
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.7f)),
        modifier = Modifier
            .width(140.dp)
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
                    .height(130.dp),
            )
            Column(modifier = Modifier.padding(8.dp)) {
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
                            modifier = Modifier.size(12.dp),
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
            .width(220.dp)
            .clickable { onClick() }
            .testTag("home_service_card_${service.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = service.title,
                style = MaterialTheme.typography.titleSmall,
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

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = ToskaPrimaryDeep,
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(
                    text = service.priceDisplay,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}

