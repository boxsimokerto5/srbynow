package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ChatRoom
import com.example.data.model.CitizenPoll
import com.example.data.model.IncidentReport
import com.example.data.model.NearbyProfile
import com.example.data.model.SurabayaEvent
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.PendingAmberBg
import com.example.ui.theme.PendingAmberText
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep

@Composable
fun AdminDashboardScreen(
    adminEmail: String,
    currentAdminUid: String,
    isAdmin: Boolean,
    isSubmitting: Boolean = false,
    pendingEvents: List<SurabayaEvent>,
    pendingPolls: List<CitizenPoll>,
    flaggedItems: List<AdminFlaggedItem>,
    incidents: List<IncidentReport>,
    publicChatRooms: List<ChatRoom>,
    bannedProfiles: List<NearbyProfile>,
    onBackToHome: () -> Unit,
    onApproveAllPending: (List<SurabayaEvent>, List<CitizenPoll>) -> Unit,
    onModerateEvent: (eventId: String, approve: Boolean, note: String) -> Unit,
    onModeratePoll: (pollId: String, approve: Boolean, note: String) -> Unit,
    onOpenEventDetail: (String) -> Unit,
    onOpenPollDetail: (String) -> Unit,
    onOpenFlaggedItem: (AdminFlaggedItem) -> Unit,
    onRestoreFlaggedItem: (collection: String, recordId: String) -> Unit,
    onClearFlaggedLinks: (collection: String, recordId: String) -> Unit,
    onDeletePostPermanently: (collection: String, recordId: String) -> Unit,
    onBanAuthor: (uid: String, name: String) -> Unit,
    onUnbanAuthor: (uid: String, name: String) -> Unit,
    onOpenIncidentDetail: (String) -> Unit,
    onToggleVerifyIncident: (IncidentReport) -> Unit,
    onOpenCreatePublicGroup: () -> Unit,
    onOpenCreateDestination: () -> Unit,
    onOpenPublicRoom: (ChatRoom) -> Unit,
    onDeletePublicRoom: (ChatRoom) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBackToHome)

    if (!isAdmin) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("admin_dashboard_restricted"),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.AdminPanelSettings,
                    contentDescription = null,
                    tint = AlertCrimson,
                    modifier = Modifier.size(48.dp),
                )
                Text(
                    text = "Akses Khusus Admin Resmi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Halaman Pusat Kendali ini hanya tersedia untuk akun Admin Resmi Arek Surabaya.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onBackToHome,
                    shape = CircleShape,
                    modifier = Modifier.height(48.dp),
                ) {
                    Text("Kembali ke Beranda")
                }
            }
        }
        return
    }

    val totalPendingApprovals = pendingEvents.size + pendingPolls.size
    var selectedSection by rememberSaveable {
        mutableIntStateOf(
            when {
                totalPendingApprovals > 0 -> 0
                flaggedItems.isNotEmpty() -> 1
                else -> 0
            },
        )
    }
    var incidentFilter by rememberSaveable { mutableStateOf("SEMUA") }

    val filteredIncidents = remember(incidents, incidentFilter) {
        when (incidentFilter) {
            "DARURAT" -> incidents.filter { it.severity == "DARURAT" || it.severity == "TINGGI" }
            "BELUM_VALIDASI" -> incidents.filter { it.verifiedCount == 0 }
            else -> incidents
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 1. Official Admin Header Card
        item(key = "admin_header") {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(MintSoftGradient),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            IconButton(
                                onClick = onBackToHome,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.85f))
                                    .testTag("admin_dashboard_back_btn"),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali ke Beranda",
                                    tint = ToskaPrimaryDeep,
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.admin_dashboard_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ToskaOnPrimaryContainer,
                                )
                                Text(
                                    text = adminEmail.ifBlank { "eccko.w4@gmail.com" },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ToskaPrimaryDeep,
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = ToskaPrimaryDeep,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = "ADMIN UTAMA",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                )
                            }
                        }
                    }

                    // 4 Live KPI Summary Tiles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AdminKpiStatTile(
                            label = "Menunggu",
                            count = totalPendingApprovals,
                            highlight = totalPendingApprovals > 0,
                            highlightColor = PendingAmberText,
                            onClick = { selectedSection = 0 },
                            modifier = Modifier.weight(1f),
                        )
                        AdminKpiStatTile(
                            label = "Laporan UGC",
                            count = flaggedItems.size,
                            highlight = flaggedItems.isNotEmpty(),
                            highlightColor = AlertCrimson,
                            onClick = { selectedSection = 1 },
                            modifier = Modifier.weight(1f),
                        )
                        AdminKpiStatTile(
                            label = "Kejadian",
                            count = incidents.size,
                            highlight = false,
                            highlightColor = ToskaPrimaryDeep,
                            onClick = { selectedSection = 2 },
                            modifier = Modifier.weight(1f),
                        )
                        AdminKpiStatTile(
                            label = "Dibekukan",
                            count = bannedProfiles.size,
                            highlight = bannedProfiles.isNotEmpty(),
                            highlightColor = AlertCrimson,
                            onClick = { selectedSection = 3 },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        // 2. Section Navigation Chips
        item(key = "admin_tabs") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AdminSectionTabChip(
                    selected = selectedSection == 0,
                    label = "${stringResource(R.string.admin_tab_approvals_label)} ($totalPendingApprovals)",
                    icon = Icons.Filled.CheckCircle,
                    testTag = "admin_tab_approvals",
                    onClick = { selectedSection = 0 },
                )
                AdminSectionTabChip(
                    selected = selectedSection == 1,
                    label = "${stringResource(R.string.admin_tab_reports_label)} (${flaggedItems.size})",
                    icon = Icons.Filled.GppMaybe,
                    testTag = "admin_tab_reports",
                    onClick = { selectedSection = 1 },
                )
                AdminSectionTabChip(
                    selected = selectedSection == 2,
                    label = "${stringResource(R.string.admin_tab_incidents_label)} (${incidents.size})",
                    icon = Icons.Filled.WarningAmber,
                    testTag = "admin_tab_incidents",
                    onClick = { selectedSection = 2 },
                )
                AdminSectionTabChip(
                    selected = selectedSection == 3,
                    label = stringResource(R.string.admin_tab_community_label),
                    icon = Icons.Filled.Groups,
                    testTag = "admin_tab_community",
                    onClick = { selectedSection = 3 },
                )
            }
        }

        // SECTION 0: PERSETUJUAN (PENDING EVENTS & PENDING POLLS)
        if (selectedSection == 0) {
            if (totalPendingApprovals > 0) {
                item(key = "admin_bulk_approve_bar") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = OpenGreenBg,
                        border = BorderStroke(1.dp, OpenGreenText.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$totalPendingApprovals Pengajuan Menunggu Persetujuan",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ToskaOnPrimaryContainer,
                                )
                                Text(
                                    text = "${pendingEvents.size} usulan event • ${pendingPolls.size} usulan polling warga",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OpenGreenText,
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { onApproveAllPending(pendingEvents, pendingPolls) },
                                enabled = !isSubmitting,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimaryDeep),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("admin_approve_all_btn"),
                            ) {
                                Icon(Icons.Filled.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Setujui Semua", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (totalPendingApprovals == 0) {
                item(key = "admin_empty_approvals") {
                    AdminEmptySectionCard(
                        title = "Semua Pengajuan Sudah Ditangani",
                        subtitle = "Tidak ada usulan acara atau polling warga yang menunggu persetujuan saat ini.",
                    )
                }
            } else {
                items(pendingEvents, key = { "pe_${it.id}" }) { ev ->
                    AdminPendingEventApprovalCard(
                        event = ev,
                        onOpenDetail = { onOpenEventDetail(ev.id) },
                        onApprove = { note -> onModerateEvent(ev.id, true, note) },
                        onReject = { note -> onModerateEvent(ev.id, false, note) },
                    )
                }
                items(pendingPolls, key = { "pp_${it.id}" }) { poll ->
                    AdminPendingPollApprovalCard(
                        poll = poll,
                        onOpenDetail = { onOpenPollDetail(poll.id) },
                        onApprove = { note -> onModeratePoll(poll.id, true, note) },
                        onReject = { note -> onModeratePoll(poll.id, false, note) },
                    )
                }
            }
        }

        // SECTION 1: LAPORAN KONTEN & KEAMANAN UGC
        if (selectedSection == 1) {
            if (flaggedItems.isEmpty()) {
                item(key = "admin_empty_reports") {
                    AdminEmptySectionCard(
                        title = "Feed Warga Bersih & Aman",
                        subtitle = "Belum ada kiriman foto, teks, atau tautan luar yang dilaporkan warga.",
                    )
                }
            } else {
                items(flaggedItems, key = { "flag_${it.collection}_${it.id}" }) { flagged ->
                    AdminFlaggedContentCard(
                        item = flagged,
                        currentAdminUid = currentAdminUid,
                        onOpenItem = { onOpenFlaggedItem(flagged) },
                        onRestoreSafe = { onRestoreFlaggedItem(flagged.collection, flagged.id) },
                        onClearLinks = { onClearFlaggedLinks(flagged.collection, flagged.id) },
                        onDeletePermanently = { onDeletePostPermanently(flagged.collection, flagged.id) },
                        onBanAuthor = { onBanAuthor(flagged.authorId, flagged.authorName) },
                    )
                }
            }
        }

        // SECTION 2: PANTAUAN LAPORAN KEJADIAN KOTA
        if (selectedSection == 2) {
            item(key = "admin_incident_filters") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        "SEMUA" to "Semua (${incidents.size})",
                        "DARURAT" to "Darurat / Tinggi",
                        "BELUM_VALIDASI" to "Belum Tervalidasi",
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = incidentFilter == key,
                            onClick = { incidentFilter = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            shape = CircleShape,
                        )
                    }
                }
            }

            if (filteredIncidents.isEmpty()) {
                item(key = "admin_empty_incidents") {
                    AdminEmptySectionCard(
                        title = "Tidak Ada Laporan Kejadian pada Filter Ini",
                        subtitle = "Pilih filter Semua untuk melihat seluruh pantauan warga Surabaya.",
                    )
                }
            } else {
                items(filteredIncidents, key = { "inc_${it.id}" }) { inc ->
                    AdminIncidentManagementCard(
                        incident = inc,
                        currentAdminUid = currentAdminUid,
                        onOpenDetail = { onOpenIncidentDetail(inc.id) },
                        onToggleVerify = { onToggleVerifyIncident(inc) },
                        onDeleteIncident = { onDeletePostPermanently("incidents", inc.id) },
                    )
                }
            }
        }

        // SECTION 3: KELOLA KOMUNITAS, GRUP PUBLIK & AKUN DIBEKUKAN
        if (selectedSection == 3) {
            item(key = "admin_official_actions") {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, OutlineVariantLight),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Aksi Resmi Admin Surabaya",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = onOpenCreatePublicGroup,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("admin_action_create_public_group"),
                            ) {
                                Icon(Icons.Filled.Forum, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Buat Grup Publik", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onOpenCreateDestination,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("admin_action_create_destination"),
                            ) {
                                Icon(Icons.Filled.AddLocationAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Tambah Wisata", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item(key = "admin_banned_header") {
                Text(
                    text = "Akun Warga yang Dibekukan (${bannedProfiles.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (bannedProfiles.isEmpty()) {
                item(key = "admin_no_banned") {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF6FBF8),
                        border = BorderStroke(1.dp, OutlineVariantLight),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Tidak ada akun warga yang sedang dibekukan saat ini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp),
                        )
                    }
                }
            } else {
                items(bannedProfiles, key = { "ban_${it.userId.ifBlank { it.id }}" }) { banned ->
                    AdminBannedCitizenRow(
                        profile = banned,
                        onUnban = { onUnbanAuthor(banned.userId.ifBlank { banned.id }, banned.displayName) },
                    )
                }
            }

            item(key = "admin_groups_header") {
                Text(
                    text = "Daftar Ruang Publik SBY (${publicChatRooms.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            items(publicChatRooms, key = { "grp_${it.id}" }) { room ->
                AdminPublicGroupManageRow(
                    room = room,
                    onOpenRoom = { onOpenPublicRoom(room) },
                    onDeleteRoom = { onDeletePublicRoom(room) },
                )
            }
        }

        item(key = "admin_bottom_spacer") {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun AdminKpiStatTile(
    label: String,
    count: Int,
    highlight: Boolean,
    highlightColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.92f),
        border = BorderStroke(
            1.dp,
            if (highlight) highlightColor.copy(alpha = 0.4f) else OutlineVariantLight,
        ),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (highlight) highlightColor else ToskaPrimaryDeep,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun AdminSectionTabChip(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ToskaPrimaryDeep,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White,
        ),
        shape = CircleShape,
        modifier = Modifier.testTag(testTag),
    )
}

@Composable
private fun AdminEmptySectionCard(
    title: String,
    subtitle: String,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF6FBF8),
        border = BorderStroke(1.dp, OutlineVariantLight),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(ToskaPrimary, ToskaPrimaryDeep))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
