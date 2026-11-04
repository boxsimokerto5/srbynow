package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CitizenPoll
import com.example.data.model.UiState
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.HeaderMintGradient
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep

private enum class PollViewFilter {
    ALL,
    VOTED,
    MY_PROPOSALS,
}

private val POLL_CATEGORIES = listOf(
    "ALL" to "Semua Kategori",
    "TRANSPORTASI" to "Transportasi",
    "TATA_KOTA" to "Tata Kota",
    "KULINER_WISATA" to "Kuliner & Wisata",
    "LINGKUNGAN" to "Lingkungan",
    "LAYANAN_PUBLIK" to "Layanan Publik",
    "UMUM" to "Umum",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollingScreen(
    pollsState: UiState<List<CitizenPoll>>,
    pendingPollsState: UiState<List<CitizenPoll>>,
    myProposedPolls: List<CitizenPoll>,
    currentUserId: String,
    isAdmin: Boolean,
    onVote: (pollId: String, optionId: String) -> Unit,
    onOpenPollDetail: (String) -> Unit,
    onOpenCreatePoll: () -> Unit,
    onOpenAdminPending: () -> Unit,
    onShareText: (CitizenPoll) -> Unit,
    onShareJpg: (CitizenPoll) -> Unit,
    onDownloadJpg: (CitizenPoll) -> Unit,
    onDeletePoll: (String) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler { onBackToHome() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var currentTab by remember { mutableStateOf(PollViewFilter.ALL) }
    var pollToShare by remember { mutableStateOf<CitizenPoll?>(null) }

    val pendingCount = (pendingPollsState as? UiState.Success)?.data?.size ?: 0

    // Filter and search computation
    val displayPolls by remember(pollsState, myProposedPolls, currentTab, selectedCategory, searchQuery, currentUserId) {
        derivedStateOf {
            val baseList = when (currentTab) {
                PollViewFilter.ALL -> (pollsState as? UiState.Success)?.data.orEmpty()
                PollViewFilter.VOTED -> (pollsState as? UiState.Success)?.data.orEmpty().filter { it.hasUserVoted(currentUserId) }
                PollViewFilter.MY_PROPOSALS -> myProposedPolls
            }

            baseList.filter { poll ->
                val matchesCategory = selectedCategory == "ALL" || poll.category.equals(selectedCategory, ignoreCase = true)
                val matchesSearch = if (searchQuery.isBlank()) {
                    true
                } else {
                    val q = searchQuery.trim().lowercase()
                    poll.title.lowercase().contains(q) ||
                        poll.description.lowercase().contains(q) ||
                        poll.district.lowercase().contains(q) ||
                        poll.options.any { it.text.lowercase().contains(q) }
                }
                matchesCategory && matchesSearch
            }
        }
    }

    // Modal Dialog for Sharing Options
    if (pollToShare != null) {
        val targetPoll = pollToShare!!
        SharePollOptionsDialog(
            poll = targetPoll,
            onShareText = {
                pollToShare = null
                onShareText(targetPoll)
            },
            onShareJpg = {
                pollToShare = null
                onShareJpg(targetPoll)
            },
            onDownloadJpg = {
                pollToShare = null
                onDownloadJpg(targetPoll)
            },
            onDismiss = { pollToShare = null },
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("polling_screen"),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Spacious Sub-Header Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onBackToHome,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("btn_back_polling"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Beranda",
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.polling_page_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.polling_page_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (isAdmin && pendingCount > 0) {
                        IconButton(
                            onClick = onOpenAdminPending,
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("btn_admin_pending_polls_screen"),
                        ) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = AlertCrimson,
                                        contentColor = Color.White,
                                    ) {
                                        Text("$pendingCount")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Moderasi Usulan Polling",
                                    tint = ToskaPrimaryDeep,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onOpenCreatePoll,
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("btn_header_create_poll"),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ToskaPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Usulkan Polling",
                                    tint = ToskaPrimaryDeep,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // 1. Spacious Hero Banner
                item(key = "poll_hero_banner") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(HeaderMintGradient)
                            .border(1.dp, ToskaPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = ToskaPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(42.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.HowToVote,
                                        contentDescription = null,
                                        tint = ToskaPrimaryDeep,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Aspirasi & Suara Warga Kota",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Transparan, terbuka, dan dapat dibagikan sebagai infografis JPG ke media sosial.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 17.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // 2. Admin Notice Banner (If any pending)
                if (isAdmin && pendingCount > 0) {
                    item(key = "poll_admin_pending_banner") {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = AlertCrimson.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable { onOpenAdminPending() },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = AlertCrimson,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$pendingCount Usulan Polling Menunggu Moderasi",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AlertCrimson,
                                    )
                                }
                                Text(
                                    text = "Periksa →",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertCrimson,
                                )
                            }
                        }
                    }
                }

                // 3. Spacious Search Bar
                item(key = "poll_search_bar") {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("input_search_poll"),
                            placeholder = {
                                Text(
                                    text = "Cari topik polling, transportasi, tata kota, wilayah…",
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = ToskaPrimaryDeep,
                                    modifier = Modifier.size(22.dp),
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Hapus Pencarian",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ToskaPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            ),
                        )
                    }
                }

                // 4. View Tabs (Semua / Sudah Diikuti / Usulan Saya)
                item(key = "poll_view_tabs") {
                    val tabTitles = listOf(
                        PollViewFilter.ALL to "Semua Polling",
                        PollViewFilter.VOTED to "Sudah Diikuti",
                        PollViewFilter.MY_PROPOSALS to "Usulan Saya (${myProposedPolls.size})",
                    )
                    val selectedIndex = tabTitles.indexOfFirst { it.first == currentTab }.coerceAtLeast(0)

                    TabRow(
                        selectedTabIndex = selectedIndex,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = ToskaPrimaryDeep,
                        indicator = { tabPositions ->
                            if (selectedIndex < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                                    height = 3.dp,
                                    color = ToskaPrimary,
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    ) {
                        tabTitles.forEachIndexed { _, (filter, title) ->
                            Tab(
                                selected = currentTab == filter,
                                onClick = { currentTab = filter },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 13.5.sp,
                                        fontWeight = if (currentTab == filter) FontWeight.Bold else FontWeight.Medium,
                                        color = if (currentTab == filter) ToskaPrimaryDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("tab_poll_${filter.name.lowercase()}"),
                            )
                        }
                    }
                }

                // 5. Category Chips (Spacious Horizontal Scroll)
                item(key = "poll_category_chips") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        POLL_CATEGORIES.forEach { (catKey, catLabel) ->
                            val isSelected = selectedCategory == catKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = catKey },
                                label = {
                                    Text(
                                        text = catLabel,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ToskaPrimary.copy(alpha = 0.18f),
                                    selectedLabelColor = ToskaPrimaryDeep,
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) ToskaPrimary else MaterialTheme.colorScheme.outlineVariant,
                                ),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("chip_poll_cat_$catKey"),
                            )
                        }
                    }
                }

                // 6. List Content
                when (pollsState) {
                    is UiState.Loading -> {
                        item(key = "poll_loading") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    color = ToskaPrimaryDeep,
                                    modifier = Modifier.size(36.dp),
                                )
                            }
                        }
                    }
                    is UiState.Error -> {
                        item(key = "poll_error") {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = "Belum dapat memuat polling warga",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = pollsState.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                    is UiState.Success -> {
                        if (displayPolls.isEmpty()) {
                            item(key = "poll_empty") {
                                EmptyPollState(
                                    tab = currentTab,
                                    searchQuery = searchQuery,
                                    onClearFilter = {
                                        searchQuery = ""
                                        selectedCategory = "ALL"
                                    },
                                    onOpenCreatePoll = onOpenCreatePoll,
                                )
                            }
                        } else {
                            items(displayPolls, key = { it.id }) { poll ->
                                PollCardItem(
                                    poll = poll,
                                    currentUserId = currentUserId,
                                    isAdmin = isAdmin,
                                    onVote = { optId -> onVote(poll.id, optId) },
                                    onOpenDetail = { onOpenPollDetail(poll.id) },
                                    onOpenShareMenu = { pollToShare = poll },
                                    onShareText = { onShareText(poll) },
                                    onShareJpg = { onShareJpg(poll) },
                                    onDownloadJpg = { onDownloadJpg(poll) },
                                    onDelete = { onDeletePoll(poll.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onOpenCreatePoll,
            containerColor = ToskaPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_create_poll"),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.HowToVote,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Usulkan Polling",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun EmptyPollState(
    tab: PollViewFilter,
    searchQuery: String,
    onClearFilter: () -> Unit,
    onOpenCreatePoll: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.HowToVote,
                contentDescription = null,
                tint = ToskaPrimaryDeep.copy(alpha = 0.7f),
                modifier = Modifier.size(54.dp),
            )
            Spacer(modifier = Modifier.height(14.dp))
            val title = when {
                searchQuery.isNotBlank() -> "Tidak Ada Polling yang Cocok"
                tab == PollViewFilter.VOTED -> "Belum Pernah Mengikuti Polling"
                tab == PollViewFilter.MY_PROPOSALS -> "Belum Ada Usulan Polling dari Anda"
                else -> "Belum Ada Polling Aktif"
            }
            val subtitle = when {
                searchQuery.isNotBlank() -> "Coba gunakan kata kunci lain atau bersihkan pencarian."
                tab == PollViewFilter.VOTED -> "Ayo telusuri daftar polling dan berikan suara Anda untuk Surabaya!"
                tab == PollViewFilter.MY_PROPOSALS -> "Suarakan ide atau isu warga di kecamatan Anda sekarang."
                else -> "Jadilah yang pertama mengusulkan polling aspirasi warga Kota Pahlawan!"
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(18.dp))

            if (searchQuery.isNotBlank()) {
                OutlinedButton(
                    onClick = onClearFilter,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Bersihkan Filter")
                }
            } else {
                Button(
                    onClick = onOpenCreatePoll,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimary),
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Usulkan Polling Baru")
                }
            }
        }
    }
}

/**
 * Spacious & Modern Polling Card Item (Tampilan Luas, Lega, & Elegan)
 */
@Composable
fun PollCardItem(
    poll: CitizenPoll,
    currentUserId: String,
    isAdmin: Boolean,
    onVote: (optionId: String) -> Unit,
    onOpenDetail: () -> Unit,
    onOpenShareMenu: () -> Unit,
    onShareText: () -> Unit,
    onShareJpg: () -> Unit,
    onDownloadJpg: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val userVotedOptionId = poll.userVotedOptionId(currentUserId)
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Polling Warga?") },
            text = { Text("Kiriman polling \"${poll.title}\" beserta seluruh suaranya akan dihapus permanen.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCrimson),
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            },
        )
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.28f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("poll_card_${poll.id}"),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category, District, and Vote Count Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false),
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ToskaPrimary.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = poll.category.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ToskaPrimaryDeep,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Kec. ${poll.district}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ToskaPrimary.copy(alpha = 0.12f),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.HowToVote,
                            contentDescription = null,
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
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
            }

            // Moderation Status Badge if pending or rejected
            if (poll.status != "APPROVED") {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (poll.status == "PENDING") Color(0xFFFFF3CD) else AlertCrimson.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = if (poll.status == "PENDING") "Menunggu Moderasi Admin" else "Ditolak: ${poll.adminNote.ifBlank { "Tidak sesuai ketentuan" }}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (poll.status == "PENDING") Color(0xFF856404) else AlertCrimson,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title - Large, Clear, and Well-Spaced
            Text(
                text = poll.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDetail() },
            )

            if (poll.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = poll.description,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onOpenDetail() },
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Options List with Direct Voting (Spacious & Modern Option Tiles)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                poll.options.forEach { opt ->
                    val isChosenByMe = opt.id == userVotedOptionId
                    val percentage = poll.percentageForOption(opt.id)
                    val animatedProgress by animateFloatAsState(
                        targetValue = percentage / 100f,
                        animationSpec = tween(durationMillis = 500),
                        label = "poll_card_prog_${opt.id}",
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isChosenByMe) {
                            ToskaPrimary.copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        },
                        border = BorderStroke(
                            width = if (isChosenByMe) 2.dp else 1.dp,
                            color = if (isChosenByMe) {
                                ToskaPrimary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                            },
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVote(opt.id) }
                            .testTag("btn_card_vote_opt_${opt.id}"),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(
                                        imageVector = if (isChosenByMe) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = if (isChosenByMe) "Pilihan Anda" else "Pilih Opsi",
                                        tint = if (isChosenByMe) ToskaPrimaryDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp),
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = opt.text,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.5.sp,
                                            lineHeight = 20.sp,
                                        ),
                                        fontWeight = if (isChosenByMe) FontWeight.Bold else FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.widthIn(min = 52.dp),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    Text(
                                        text = "%.0f%%".format(percentage),
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChosenByMe) ToskaPrimaryDeep else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        softWrap = false,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(${opt.voteCount})",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        softWrap = false,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Thicker, cleaner progress bar (8dp)
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isChosenByMe) ToskaPrimary else ToskaPrimary.copy(alpha = 0.75f),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtle divider before actions
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.8.dp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Actions Row: Comments pill and Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Comments Button as a spacious pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier
                        .clickable { onOpenDetail() }
                        .testTag("btn_card_comments_${poll.id}"),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Comment,
                            contentDescription = "Komentar",
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${poll.commentCount} Komentar",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                // Sharing & Action Buttons with generous touch targets (40dp+)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Share Text button (WhatsApp / Chats)
                    IconButton(
                        onClick = onShareText,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("btn_card_share_text_${poll.id}"),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ToskaPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(34.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Bagikan Polling",
                                    tint = ToskaPrimaryDeep,
                                    modifier = Modifier.size(19.dp),
                                )
                            }
                        }
                    }

                    // Direct Share JPG button
                    IconButton(
                        onClick = onShareJpg,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("btn_card_share_jpg_${poll.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Bagikan Gambar Infografis JPG",
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    // Direct Download JPG to Gallery
                    IconButton(
                        onClick = onDownloadJpg,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("btn_card_download_jpg_${poll.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Unduh JPG ke Galeri",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    // Delete button if author or admin
                    if (isAdmin || poll.proposerId == currentUserId) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_card_delete_${poll.id}"),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Polling",
                                tint = AlertCrimson.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Share Options Dialog: ensures clicking Share gives clear, working options
 */
@Composable
private fun SharePollOptionsDialog(
    poll: CitizenPoll,
    onShareText: () -> Unit,
    onShareJpg: () -> Unit,
    onDownloadJpg: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = ToskaPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Bagikan Polling Warga",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Kec. ${poll.district} • ${poll.totalVotes} Suara",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "\"${poll.title}\"",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Option 1: Share Text (WhatsApp / Chats)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShareText() },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bagikan Teks & Ringkasan",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Kirim teks pertanyaan & hasil suara ke WhatsApp, Telegram, dll",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Option 2: Share Infographic JPG
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ToskaPrimary.copy(alpha = 0.12f)),
                    border = BorderStroke(1.5.dp, ToskaPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShareJpg() },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bagikan Gambar Infografis (JPG)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ToskaPrimaryDeep,
                            )
                            Text(
                                text = "Poster hasil polling beresolusi tinggi untuk WhatsApp Story & medsos",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Option 3: Download JPG to Gallery
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDownloadJpg() },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Simpan JPG ke Galeri Foto",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Unduh gambar infografis langsung ke folder Pictures perangkat",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = ToskaPrimaryDeep, fontWeight = FontWeight.Bold)
            }
        },
    )
}
