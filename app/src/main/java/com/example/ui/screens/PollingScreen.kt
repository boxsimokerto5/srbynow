package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("polling_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.polling_page_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.polling_page_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackToHome,
                        modifier = Modifier.testTag("btn_back_polling"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Beranda",
                        )
                    }
                },
                actions = {
                    if (isAdmin && pendingCount > 0) {
                        IconButton(
                            onClick = onOpenAdminPending,
                            modifier = Modifier.testTag("btn_admin_pending_polls_screen"),
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
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onOpenCreatePoll,
                        modifier = Modifier.testTag("btn_header_create_poll"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Usulkan Polling",
                            tint = ToskaPrimaryDeep,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenCreatePoll,
                containerColor = ToskaPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_create_poll"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.HowToVote,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Usulkan Polling",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            // 1. Hero / Header Banner
            item(key = "poll_hero_banner") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(HeaderMintGradient)
                        .padding(16.dp),
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = ToskaPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.HowToVote,
                                        contentDescription = null,
                                        tint = ToskaPrimaryDeep,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Aspirasi & Suara Warga Kota",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "Transparan, terbuka, dan dapat dibagikan sebagai infografis JPG",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            // 2. Admin Notice Banner (If any pending)
            if (isAdmin && pendingCount > 0) {
                item(key = "poll_admin_pending_banner") {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AlertCrimson.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onOpenAdminPending() },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = AlertCrimson,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$pendingCount Usulan Polling Menunggu Moderasi",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertCrimson,
                                )
                            }
                            Text(
                                text = "Periksa →",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AlertCrimson,
                            )
                        }
                    }
                }
            }

            // 3. Search Bar
            item(key = "poll_search_bar") {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_poll"),
                        placeholder = {
                            Text(
                                text = "Cari topik polling, transportasi, taman, wilayah…",
                                fontSize = 12.5.sp,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Hapus Pencarian",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ToskaPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
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
                                color = ToskaPrimary,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    tabTitles.forEachIndexed { index, (filter, title) ->
                        Tab(
                            selected = currentTab == filter,
                            onClick = { currentTab = filter },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (currentTab == filter) FontWeight.Bold else FontWeight.Medium,
                                    color = if (currentTab == filter) ToskaPrimaryDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            modifier = Modifier.testTag("tab_poll_${filter.name.lowercase()}"),
                        )
                    }
                }
            }

            // 5. Category Chips (Horizontal Scroll)
            item(key = "poll_category_chips") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ToskaPrimary.copy(alpha = 0.15f),
                                selectedLabelColor = ToskaPrimaryDeep,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) ToskaPrimary else MaterialTheme.colorScheme.outlineVariant,
                            ),
                            modifier = Modifier.testTag("chip_poll_cat_$catKey"),
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
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    item(key = "poll_error") {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "Belum dapat memuat polling warga",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = pollsState.message,
                                    style = MaterialTheme.typography.bodySmall,
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
                                onShareText = { onShareText(poll) },
                                onShareJpg = { onShareJpg(poll) },
                                onDownloadJpg = { onDownloadJpg(poll) },
                                onDelete = { onDeletePoll(poll.id) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.HowToVote,
                contentDescription = null,
                tint = ToskaPrimaryDeep.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (searchQuery.isNotBlank()) {
                OutlinedButton(
                    onClick = onClearFilter,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("Bersihkan Filter")
                }
            } else {
                Button(
                    onClick = onOpenCreatePoll,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimary),
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Usulkan Polling Baru")
                }
            }
        }
    }
}

@Composable
fun PollCardItem(
    poll: CitizenPoll,
    currentUserId: String,
    isAdmin: Boolean,
    onVote: (optionId: String) -> Unit,
    onOpenDetail: () -> Unit,
    onShareText: () -> Unit,
    onShareJpg: () -> Unit,
    onDownloadJpg: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val userVotedOptionId = poll.userVotedOptionId(currentUserId)
    val hasVoted = userVotedOptionId != null
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
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
                androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            },
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("poll_card_${poll.id}"),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Category, District, and Vote Count Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ToskaPrimary.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = poll.category.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ToskaPrimaryDeep,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kec. ${poll.district}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    )
                }
            }

            // Moderation Status Badge if pending or rejected
            if (poll.status != "APPROVED") {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (poll.status == "PENDING") Color(0xFFFFF3CD) else AlertCrimson.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = if (poll.status == "PENDING") "Menunggu Moderasi Admin" else "Ditolak: ${poll.adminNote.ifBlank { "Tidak sesuai ketentuan" }}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (poll.status == "PENDING") Color(0xFF856404) else AlertCrimson,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = poll.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable { onOpenDetail() },
            )

            if (poll.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = poll.description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Options List with Direct Voting
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
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
                        shape = RoundedCornerShape(10.dp),
                        color = if (isChosenByMe) ToskaPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(
                            width = if (isChosenByMe) 1.5.dp else 1.dp,
                            color = if (isChosenByMe) ToskaPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVote(opt.id) }
                            .testTag("btn_card_vote_opt_${opt.id}"),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
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
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = opt.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isChosenByMe) FontWeight.Bold else FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "%.0f%%".format(percentage),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChosenByMe) ToskaPrimaryDeep else MaterialTheme.colorScheme.onSurface,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(${opt.voteCount})",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (isChosenByMe) ToskaPrimary else ToskaPrimary.copy(alpha = 0.7f),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions Row: Comments, Share Text, Share JPG, Download JPG
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Comments Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onOpenDetail() }
                        .testTag("btn_card_comments_${poll.id}")
                        .padding(vertical = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = "Komentar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${poll.commentCount} Komentar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Sharing & Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // Share Text
                    IconButton(
                        onClick = onShareText,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_card_share_text_${poll.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Bagikan Teks",
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    // Share JPG
                    IconButton(
                        onClick = onShareJpg,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_card_share_jpg_${poll.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Bagikan Gambar JPG",
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    // Download JPG to Gallery
                    IconButton(
                        onClick = onDownloadJpg,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_card_download_jpg_${poll.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Unduh JPG ke Galeri",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    // Delete button if author or admin
                    if (isAdmin || poll.proposerId == currentUserId) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_card_delete_${poll.id}"),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Polling",
                                tint = AlertCrimson.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
