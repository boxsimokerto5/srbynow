package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.BookmarkBorder
import com.example.ui.components.CategoryCardItem
import com.example.ui.components.CategoryCardRow
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.BookmarkItem
import com.example.data.model.KosKontrakItem
import com.example.data.model.UiState
import com.example.data.util.formatRelativeTimestamp
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.components.SurabayaDistrictFilterBar
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.ClosedGrayBg
import com.example.ui.theme.ClosedGrayText
import com.example.ui.theme.MintCardSubtleGradient
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep

enum class KosAvailabilityFilter {
    AVAILABLE,
    ALL,
    MINE,
}

fun kosPropertyTypeIcon(propertyType: String): ImageVector = when (propertyType.trim().uppercase()) {
    "KOS_PUTRA" -> Icons.Filled.Male
    "KOS_PUTRI" -> Icons.Filled.Female
    "KOS_CAMPUR" -> Icons.Filled.Groups
    "KONTRAKAN" -> Icons.Filled.HomeWork
    else -> Icons.Filled.Apartment
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KosKontrakScreen(
    kosState: UiState<List<KosKontrakItem>>,
    bookmarks: List<BookmarkItem>,
    currentUserId: String,
    isAdmin: Boolean,
    onOpenKosDetail: (String) -> Unit,
    onOpenCreateKos: () -> Unit,
    onToggleFull: (KosKontrakItem) -> Unit,
    onToggleBookmark: (KosKontrakItem) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBackToHome)

    var searchQuery by remember { mutableStateOf("") }
    var selectedPropertyType by remember { mutableStateOf("ALL") }
    var selectedBillingPeriod by remember { mutableStateOf("ALL") }
    var selectedDistrict by remember { mutableStateOf("ALL") }
    var availabilityFilter by remember { mutableStateOf(KosAvailabilityFilter.AVAILABLE) }

    val propertyTypes = remember {
        listOf(
            "ALL" to "Semua Hunian",
            "KOS_PUTRA" to "Kos Putra",
            "KOS_PUTRI" to "Kos Putri",
            "KOS_CAMPUR" to "Pasutri / Campur",
            "KONTRAKAN" to "Kontrakan Rumah",
        )
    }

    val billingPeriods = remember {
        listOf(
            "ALL" to "Semua Durasi",
            "BULANAN" to "Sewa Bulanan",
            "TAHUNAN" to "Sewa Tahunan",
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("kos_kontrak_screen"),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Compact Sub-Header Bar (Mepet ke TopBar Surabaya)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onBackToHome,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("kos_back_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Beranda",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.kos_page_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                        Text(
                            text = stringResource(R.string.kos_page_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Button(
                        onClick = onOpenCreateKos,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ToskaPrimary,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("kos_top_create_btn"),
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.kos_btn_post),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            val allItems = (kosState as? UiState.Success)?.data.orEmpty()
            val availableCount = remember(allItems) { allItems.count { !it.isFull } }
            val myItemsCount = remember(allItems, currentUserId) { allItems.count { it.isOwnedBy(currentUserId) } }

            val filteredItems = remember(
                allItems,
                availabilityFilter,
                selectedPropertyType,
                selectedBillingPeriod,
                selectedDistrict,
                searchQuery,
                currentUserId,
            ) {
                val q = searchQuery.trim().lowercase()
                allItems.filter { item ->
                    val matchesStatus = when (availabilityFilter) {
                        KosAvailabilityFilter.AVAILABLE -> !item.isFull
                        KosAvailabilityFilter.ALL -> true
                        KosAvailabilityFilter.MINE -> item.isOwnedBy(currentUserId)
                    }
                    val matchesType = when (selectedPropertyType) {
                        "ALL" -> true
                        "DEKAT_KAMPUS" -> {
                            val text = "${item.title} ${item.description} ${item.address}".lowercase()
                            text.contains("its") || text.contains("unair") || text.contains("unesa") || text.contains("kampus") || text.contains("ubaya") || text.contains("upn")
                        }
                        else -> item.propertyType.equals(selectedPropertyType, ignoreCase = true)
                    }
                    val matchesPeriod = selectedBillingPeriod == "ALL" ||
                        item.billingPeriod.equals(selectedBillingPeriod, ignoreCase = true)
                    val matchesDistrict = selectedDistrict == "ALL" ||
                        selectedDistrict.isBlank() ||
                        item.district.equals(selectedDistrict, ignoreCase = true)
                    val matchesSearch = q.isBlank() ||
                        item.title.lowercase().contains(q) ||
                        item.description.lowercase().contains(q) ||
                        item.address.lowercase().contains(q) ||
                        item.district.lowercase().contains(q) ||
                        item.ownerName.lowercase().contains(q) ||
                        item.facilities.any { it.lowercase().contains(q) }
                    matchesStatus && matchesType && matchesPeriod && matchesDistrict && matchesSearch
                }
            }

            val kosCardCategories = remember(allItems) {
                listOf(
                    CategoryCardItem(
                        key = "ALL",
                        title = "Semua Hunian",
                        subtitle = "Kos & Rumah",
                        icon = Icons.Filled.Apartment,
                        count = allItems.size,
                    ),
                    CategoryCardItem(
                        key = "KOS_PUTRI",
                        title = "Kos Putri",
                        subtitle = "Aman & Nyaman",
                        icon = Icons.Filled.Female,
                        count = allItems.count { it.propertyType.equals("KOS_PUTRI", ignoreCase = true) },
                        accentColor = Color(0xFFE91E63),
                    ),
                    CategoryCardItem(
                        key = "KOS_PUTRA",
                        title = "Kos Putra",
                        subtitle = "Bebas Jam Malam",
                        icon = Icons.Filled.Male,
                        count = allItems.count { it.propertyType.equals("KOS_PUTRA", ignoreCase = true) },
                        accentColor = Color(0xFF1976D2),
                    ),
                    CategoryCardItem(
                        key = "KOS_CAMPUR",
                        title = "Campur / Pasutri",
                        subtitle = "Pasutri & Karyawan",
                        icon = Icons.Filled.Groups,
                        count = allItems.count { it.propertyType.equals("KOS_CAMPUR", ignoreCase = true) },
                        accentColor = Color(0xFF7B1FA2),
                    ),
                    CategoryCardItem(
                        key = "KONTRAKAN",
                        title = "Kontrakan",
                        subtitle = "Rumah & Paviliun",
                        icon = Icons.Filled.HomeWork,
                        count = allItems.count { it.propertyType.equals("KONTRAKAN", ignoreCase = true) },
                        accentColor = Color(0xFFE65100),
                    ),
                    CategoryCardItem(
                        key = "DEKAT_KAMPUS",
                        title = "Dekat Kampus",
                        subtitle = "ITS • UNAIR • UNESA",
                        icon = Icons.Filled.School,
                        count = allItems.count {
                            val t = "${it.title} ${it.description} ${it.address}".lowercase()
                            t.contains("its") || t.contains("unair") || t.contains("unesa") || t.contains("kampus") || t.contains("ubaya") || t.contains("upn")
                        },
                        badge = "KAMPUS",
                        accentColor = ToskaPrimaryDeep,
                    ),
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
            ) {
            item(key = "kos_safety_banner") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, OutlineVariantLight),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MintSoftGradient)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(R.string.kos_safety_notice),
                            style = MaterialTheme.typography.bodySmall,
                            color = ToskaOnPrimaryContainer,
                            lineHeight = 17.sp,
                        )
                    }
                }
            }

            item(key = "kos_filters") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = availabilityFilter == KosAvailabilityFilter.AVAILABLE,
                            onClick = { availabilityFilter = KosAvailabilityFilter.AVAILABLE },
                            label = { Text("Masih Tersedia ($availableCount)", fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ToskaPrimaryDeep,
                                selectedLabelColor = Color.White,
                            ),
                            modifier = Modifier.testTag("kos_filter_available"),
                        )
                        FilterChip(
                            selected = availabilityFilter == KosAvailabilityFilter.ALL,
                            onClick = { availabilityFilter = KosAvailabilityFilter.ALL },
                            label = { Text("Semua Info (${allItems.size})", fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ToskaPrimaryDeep,
                                selectedLabelColor = Color.White,
                            ),
                            modifier = Modifier.testTag("kos_filter_all"),
                        )
                        FilterChip(
                            selected = availabilityFilter == KosAvailabilityFilter.MINE,
                            onClick = { availabilityFilter = KosAvailabilityFilter.MINE },
                            label = { Text("Iklan Saya ($myItemsCount)", fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ToskaPrimaryDeep,
                                selectedLabelColor = Color.White,
                            ),
                            modifier = Modifier.testTag("kos_filter_mine"),
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        billingPeriods.forEach { (key, label) ->
                            FilterChip(
                                selected = selectedBillingPeriod == key,
                                onClick = { selectedBillingPeriod = key },
                                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ToskaPrimary,
                                    selectedLabelColor = Color.White,
                                ),
                                modifier = Modifier.testTag("kos_period_$key"),
                            )
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari dekat Unair/ITS, AC, parkir mobil, nama jalan…") },
                        leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = if (searchQuery.isNotBlank()) {
                            {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Filled.Clear, contentDescription = "Hapus pencarian")
                                }
                            }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ToskaPrimary,
                            unfocusedBorderColor = OutlineVariantLight,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .testTag("kos_search_input"),
                    )

                    SurabayaDistrictFilterBar(
                        selectedDistrict = selectedDistrict,
                        onSelect = { selectedDistrict = it },
                        testTag = "kos_district_filter",
                        modifier = Modifier.padding(horizontal = 14.dp),
                    )

                }
            }

            item(key = "kos_category_cards") {
                CategoryCardRow(
                    items = kosCardCategories,
                    selectedKey = selectedPropertyType,
                    onSelect = { selectedPropertyType = it },
                    tagPrefix = "kos_type_filter",
                )
            }

            // Horizontal Featured Kos Carousel (Like Destinasi Cards)
            if (filteredItems.isNotEmpty() && selectedPropertyType == "ALL" && selectedBillingPeriod == "ALL" && searchQuery.isBlank() && availabilityFilter == KosAvailabilityFilter.AVAILABLE) {
                item(key = "kos_featured_carousel") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 4.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Hunian Paling Diminati",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Geser ke samping",
                                style = MaterialTheme.typography.labelSmall,
                                color = ToskaPrimaryDeep,
                            )
                        }
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            items(filteredItems.take(6), key = { "feat_kos_${it.id}" }) { kos ->
                                HorizontalKosFeaturedCard(
                                    item = kos,
                                    onClick = { onOpenKosDetail(kos.id) },
                                )
                            }
                        }
                    }
                }
            }

            when (kosState) {
                is UiState.Loading -> {
                    item(key = "kos_loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = ToskaPrimary)
                        }
                    }
                }
                is UiState.Error -> {
                    item(key = "kos_error") {
                        EmptyFeedPlaceholder(
                            icon = Icons.Filled.HomeWork,
                            title = "Gagal memuat Info Kos & Kontrak",
                            subtitle = kosState.message,
                        )
                    }
                }
                is UiState.Success -> {
                    if (filteredItems.isEmpty()) {
                        item(key = "kos_empty") {
                            EmptyFeedPlaceholder(
                                icon = Icons.Filled.HomeWork,
                                title = if (availabilityFilter == KosAvailabilityFilter.MINE) {
                                    "Anda belum memasang info kos/kontrakan"
                                } else {
                                    "Belum ada kos atau kontrakan yang sesuai filter"
                                },
                                subtitle = if (availabilityFilter == KosAvailabilityFilter.MINE) {
                                    "Ketuk + Pasang Kos untuk menawarkan kos bulanan atau kontrakan tahunan Anda kepada warga Surabaya."
                                } else {
                                    "Coba pilih semua kecamatan/durasi atau pasang info kos kosong di sekitar Anda."
                                },
                            )
                        }
                    } else {
                        items(filteredItems, key = { it.id }) { item ->
                            val isSaved = bookmarks.any { it.targetId == item.id && it.targetType == "KOS_KONTRAK" }
                            KosListingCard(
                                item = item,
                                isSaved = isSaved,
                                isOwner = item.isOwnedBy(currentUserId),
                                isAdmin = isAdmin,
                                onOpenDetail = { onOpenKosDetail(item.id) },
                                onToggleFull = { onToggleFull(item) },
                                onToggleBookmark = { onToggleBookmark(item) },
                            )
                        }
                    }
                }
            }
        }
    }

        // Floating Action Button mepet & rapat tepat di atas footer ala KejadianScreen
        FloatingActionButton(
            onClick = onOpenCreateKos,
            containerColor = ToskaPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("kos_fab_create"),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.kos_btn_post),
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun KosListingCard(
    item: KosKontrakItem,
    isSaved: Boolean,
    isOwner: Boolean,
    isAdmin: Boolean,
    onOpenDetail: () -> Unit,
    onToggleFull: () -> Unit,
    onToggleBookmark: () -> Unit,
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onOpenDetail)
            .testTag("kos_card_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (item.isFull) ClosedGrayBg else OutlineVariantLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFE5F7F0), Color(0xFFD2EFE4)),
                        ),
                    ),
            ) {
                if (item.firstPhotoUrl.isNotBlank() && !item.isContentBlurred) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(item.firstPhotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = kosPropertyTypeIcon(item.propertyType),
                            contentDescription = null,
                            tint = ToskaPrimaryDeep.copy(alpha = 0.68f),
                            modifier = Modifier.size(44.dp),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (item.isContentBlurred) "Foto Disamarkan Sementara" else item.propertyTypeLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ToskaPrimaryDeep,
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Surface(
                        color = if (item.isFull) AlertCrimson else OpenGreenBg,
                        shape = CircleShape,
                        modifier = Modifier.testTag("kos_full_badge_${item.id}"),
                    ) {
                        Text(
                            text = item.availabilityLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (item.isFull) Color.White else OpenGreenText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.92f),
                        shape = CircleShape,
                    ) {
                        Text(
                            text = item.billingBadgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ToskaPrimaryDeep,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .minimumInteractiveComponentSize()
                        .testTag("kos_card_bookmark_${item.id}"),
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isSaved) "Hapus dari favorit" else "Simpan ke favorit",
                        tint = if (isSaved) ToskaPrimaryDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MintCardSubtleGradient)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.priceWithPeriod,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (item.isFull) ClosedGrayText else ToskaPrimaryDeep,
                        textDecoration = if (item.isFull) TextDecoration.LineThrough else TextDecoration.None,
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ToskaPrimary.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = item.propertyTypeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ToskaPrimaryDeep,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = "Kec. ${item.district} • ${item.address}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (item.hasCustomMapsLink) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = OpenGreenBg,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Map,
                                    contentDescription = null,
                                    tint = OpenGreenText,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    text = "Peta",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OpenGreenText,
                                )
                            }
                        }
                    }
                }

                if (item.facilities.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        item.facilities.take(5).forEach { fac ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF2FAF6),
                                border = BorderStroke(1.dp, OutlineVariantLight),
                            ) {
                                Text(
                                    text = fac,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.6f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${item.ownerName.ifBlank { "Pemilik Kos" }} • ${formatRelativeTimestamp(item.createdAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "${item.commentCount} tanya",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (isOwner || isAdmin) {
                    OutlinedButton(
                        onClick = onToggleFull,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (item.isFull) ToskaPrimaryDeep else AlertCrimson),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("kos_card_toggle_full_${item.id}"),
                    ) {
                        Icon(
                            imageVector = if (item.isFull) Icons.Filled.Refresh else Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = if (item.isFull) ToskaPrimaryDeep else AlertCrimson,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (item.isFull) {
                                stringResource(R.string.kos_reopen_btn)
                            } else {
                                stringResource(R.string.kos_mark_full_btn)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isFull) ToskaPrimaryDeep else AlertCrimson,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalKosFeaturedCard(
    item: KosKontrakItem,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.75f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .width(220.dp)
            .testTag("kos_featured_${item.id}"),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp),
            ) {
                val photoModel = item.firstPhotoUrl.ifBlank { null } ?: R.drawable.app_logo_mascot
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 10.dp),
                    color = ToskaPrimaryDeep.copy(alpha = 0.92f),
                    modifier = Modifier.align(Alignment.TopStart),
                ) {
                    Text(
                        text = item.priceWithPeriod,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                ) {
                    Text(
                        text = item.propertyTypeLabel,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = "Kec. ${item.district}",
                        style = MaterialTheme.typography.bodySmall,
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

