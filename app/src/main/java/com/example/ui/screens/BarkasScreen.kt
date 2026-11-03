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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Weekend
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
import com.example.data.model.BarkasItem
import com.example.data.model.BookmarkItem
import com.example.data.model.UiState
import com.example.data.util.formatRelativeTimestamp
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.ClosedGrayBg
import com.example.ui.theme.ClosedGrayText
import com.example.ui.theme.MintCardSubtleGradient
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.PendingAmberBg
import com.example.ui.theme.PendingAmberText
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep

enum class BarkasStatusFilter {
    AVAILABLE,
    ALL,
    MINE,
}

fun barkasCategoryIcon(category: String): ImageVector = when (category.trim().uppercase()) {
    "ELEKTRONIK" -> Icons.Filled.Devices
    "KENDARAAN" -> Icons.Filled.TwoWheeler
    "PERABOT" -> Icons.Filled.Weekend
    "PAKAIAN_HOBI" -> Icons.Filled.Checkroom
    else -> Icons.Filled.ShoppingBag
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarkasScreen(
    barkasState: UiState<List<BarkasItem>>,
    bookmarks: List<BookmarkItem>,
    currentUserId: String,
    isAdmin: Boolean,
    onOpenBarkasDetail: (String) -> Unit,
    onOpenCreateBarkas: () -> Unit,
    onToggleSold: (BarkasItem) -> Unit,
    onToggleBookmark: (BarkasItem) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBackToHome)

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var statusFilter by remember { mutableStateOf(BarkasStatusFilter.AVAILABLE) }

    val categories = remember {
        listOf(
            "ALL" to "Semua",
            "ELEKTRONIK" to "Elektronik",
            "KENDARAAN" to "Sepeda & Otomotif",
            "PERABOT" to "Perabot & Kos",
            "PAKAIAN_HOBI" to "Pakaian & Hobi",
            "LAINNYA" to "Lainnya",
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("barkas_screen"),
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
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onBackToHome,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("barkas_back_btn"),
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
                            text = stringResource(R.string.barkas_page_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                        Text(
                            text = stringResource(R.string.barkas_page_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Button(
                        onClick = onOpenCreateBarkas,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ToskaPrimary,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("barkas_top_create_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.barkas_btn_post),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            val allItems = (barkasState as? UiState.Success)?.data.orEmpty()
            val availableCount = remember(allItems) { allItems.count { !it.isSold } }
            val myItemsCount = remember(allItems, currentUserId) {
                allItems.count { it.isOwnedBy(currentUserId) }
            }

            val filteredItems = remember(allItems, statusFilter, selectedCategory, searchQuery, currentUserId) {
                val q = searchQuery.trim().lowercase()
                allItems.filter { item ->
                    val matchesStatus = when (statusFilter) {
                        BarkasStatusFilter.AVAILABLE -> !item.isSold
                        BarkasStatusFilter.ALL -> true
                        BarkasStatusFilter.MINE -> item.isOwnedBy(currentUserId)
                    }
                    val matchesCategory = when (selectedCategory) {
                        "ALL" -> true
                        "GRATIS" -> item.price <= 0L
                        else -> item.category.equals(selectedCategory, ignoreCase = true)
                    }
                    val matchesSearch = q.isBlank() ||
                        item.title.lowercase().contains(q) ||
                        item.description.lowercase().contains(q) ||
                        item.district.lowercase().contains(q) ||
                        item.codLocation.lowercase().contains(q) ||
                        item.sellerName.lowercase().contains(q)
                    matchesStatus && matchesCategory && matchesSearch
                }
            }

            val barkasCardCategories = remember(allItems) {
                listOf(
                    CategoryCardItem(
                        key = "ALL",
                        title = "Semua Lapak",
                        subtitle = "Katalog Barkas",
                        icon = Icons.Filled.Storefront,
                        count = allItems.size,
                    ),
                    CategoryCardItem(
                        key = "ELEKTRONIK",
                        title = "Elektronik",
                        subtitle = "HP & Gadget",
                        icon = Icons.Filled.Devices,
                        count = allItems.count { it.category.equals("ELEKTRONIK", ignoreCase = true) },
                        accentColor = Color(0xFF1976D2),
                    ),
                    CategoryCardItem(
                        key = "KENDARAAN",
                        title = "Kendaraan",
                        subtitle = "Sepeda & Motor",
                        icon = Icons.Filled.TwoWheeler,
                        count = allItems.count { it.category.equals("KENDARAAN", ignoreCase = true) },
                        accentColor = Color(0xFFEF6C00),
                    ),
                    CategoryCardItem(
                        key = "PERABOT",
                        title = "Perabot Kos",
                        subtitle = "Meja & Lemari",
                        icon = Icons.Filled.Weekend,
                        count = allItems.count { it.category.equals("PERABOT", ignoreCase = true) },
                        accentColor = Color(0xFF6D4C41),
                    ),
                    CategoryCardItem(
                        key = "PAKAIAN_HOBI",
                        title = "Fashion & Hobi",
                        subtitle = "Pakaian & Musik",
                        icon = Icons.Filled.Checkroom,
                        count = allItems.count { it.category.equals("PAKAIAN_HOBI", ignoreCase = true) },
                        accentColor = Color(0xFF8E24AA),
                    ),
                    CategoryCardItem(
                        key = "GRATIS",
                        title = "Hibah Gratis",
                        subtitle = "Ambil Free COD",
                        icon = Icons.Filled.VolunteerActivism,
                        count = allItems.count { it.price <= 0L },
                        badge = "GRATIS",
                        accentColor = Color(0xFF2E7D32),
                    ),
                    CategoryCardItem(
                        key = "LAINNYA",
                        title = "Lainnya",
                        subtitle = "Serba-serbi",
                        icon = Icons.Filled.ShoppingBag,
                        count = allItems.count { it.category.equals("LAINNYA", ignoreCase = true) },
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
            item(key = "barkas_cod_banner") {
                BarkasCodSafetyBanner()
            }

            item(key = "barkas_filters") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 5.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Status Filter Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        FilterChip(
                            selected = statusFilter == BarkasStatusFilter.AVAILABLE,
                            onClick = { statusFilter = BarkasStatusFilter.AVAILABLE },
                            label = {
                                Text(
                                    text = "Masih Tersedia ($availableCount)",
                                    fontWeight = FontWeight.SemiBold,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ToskaPrimaryDeep,
                                selectedLabelColor = Color.White,
                            ),
                            modifier = Modifier.testTag("barkas_filter_available"),
                        )
                        FilterChip(
                            selected = statusFilter == BarkasStatusFilter.ALL,
                            onClick = { statusFilter = BarkasStatusFilter.ALL },
                            label = {
                                Text(
                                    text = "Semua Barang (${allItems.size})",
                                    fontWeight = FontWeight.SemiBold,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ToskaPrimaryDeep,
                                selectedLabelColor = Color.White,
                            ),
                            modifier = Modifier.testTag("barkas_filter_all"),
                        )
                        FilterChip(
                            selected = statusFilter == BarkasStatusFilter.MINE,
                            onClick = { statusFilter = BarkasStatusFilter.MINE },
                            label = {
                                Text(
                                    text = "Lapak Saya ($myItemsCount)",
                                    fontWeight = FontWeight.SemiBold,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ToskaPrimaryDeep,
                                selectedLabelColor = Color.White,
                            ),
                            modifier = Modifier.testTag("barkas_filter_mine"),
                        )
                    }

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text("Cari sepeda, kulkas kos, elektronik, kecamatan COD…")
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                        },
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
                            .padding(horizontal = 10.dp)
                            .testTag("barkas_search_input"),
                    )

                }
            }

            item(key = "barkas_category_cards") {
                CategoryCardRow(
                    items = barkasCardCategories,
                    selectedKey = selectedCategory,
                    onSelect = { selectedCategory = it },
                    tagPrefix = "barkas_category",
                )
            }

            // Horizontal Featured Barkas Carousel (Just like Destinasi Cards)
            if (filteredItems.isNotEmpty() && selectedCategory == "ALL" && searchQuery.isBlank() && statusFilter == BarkasStatusFilter.AVAILABLE) {
                item(key = "barkas_featured_carousel") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 4.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Lapak Paling Banyak Dilirik",
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
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            items(filteredItems.take(6), key = { "feat_barkas_${it.id}" }) { barkas ->
                                HorizontalBarkasFeaturedCard(
                                    item = barkas,
                                    onClick = { onOpenBarkasDetail(barkas.id) },
                                )
                            }
                        }
                    }
                }
            }

            when (barkasState) {
                is UiState.Loading -> {
                    item(key = "barkas_loading") {
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
                    item(key = "barkas_error") {
                        EmptyFeedPlaceholder(
                            icon = Icons.Filled.Storefront,
                            title = "Gagal memuat Barkas Warga",
                            subtitle = barkasState.message,
                        )
                    }
                }
                is UiState.Success -> {
                    if (filteredItems.isEmpty()) {
                        item(key = "barkas_empty") {
                            EmptyFeedPlaceholder(
                                icon = Icons.Filled.Storefront,
                                title = if (statusFilter == BarkasStatusFilter.MINE) {
                                    "Anda belum memasang barang bekas"
                                } else {
                                    "Belum ada barang bekas yang cocok"
                                },
                                subtitle = if (statusFilter == BarkasStatusFilter.MINE) {
                                    "Ketuk tombol + Jual Barang untuk menawarkan barang layak pakai dengan sistem COD di Surabaya."
                                } else {
                                    "Coba pilih kategori Semua atau pasang barang bekas Anda untuk sesama warga Surabaya."
                                },
                            )
                        }
                    } else {
                        items(filteredItems, key = { it.id }) { item ->
                            val isSaved = bookmarks.any { it.targetId == item.id && it.targetType == "BARKAS" }
                            BarkasItemCard(
                                item = item,
                                isSaved = isSaved,
                                isOwner = item.isOwnedBy(currentUserId),
                                isAdmin = isAdmin,
                                onOpenDetail = { onOpenBarkasDetail(item.id) },
                                onToggleSold = { onToggleSold(item) },
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
            onClick = onOpenCreateBarkas,
            containerColor = ToskaPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("barkas_fab_create"),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.barkas_btn_post),
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun BarkasCodSafetyBanner() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, OutlineVariantLight),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag("barkas_safety_banner"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MintSoftGradient)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = ToskaPrimaryDeep,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.barkas_cod_safety_notice),
                style = MaterialTheme.typography.bodySmall,
                color = ToskaOnPrimaryContainer,
                lineHeight = 17.sp,
            )
        }
    }
}

@Composable
fun BarkasItemCard(
    item: BarkasItem,
    isSaved: Boolean,
    isOwner: Boolean,
    isAdmin: Boolean,
    onOpenDetail: () -> Unit,
    onToggleSold: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenDetail)
            .testTag("barkas_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = 1.dp,
            color = if (item.isSold) ClosedGrayBg else OutlineVariantLight,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.background(MintCardSubtleGradient)) {
            // Photo or Category Visual Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (item.allPhotos.isNotEmpty()) 136.dp else 76.dp)
                    .background(
                        Brush.linearGradient(
                            colors = if (item.isSold) {
                                listOf(Color(0xFFE5ECE9), Color(0xFFF3F6F5))
                            } else {
                                listOf(Color(0xFFE3FBEF), Color(0xFFCCF5E2))
                            }
                        )
                    ),
            ) {
                if (item.allPhotos.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(item.firstPhotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (item.isSold) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.42f)),
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.88f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = barkasCategoryIcon(item.category),
                                contentDescription = null,
                                tint = if (item.isSold) ClosedGrayText else ToskaPrimaryDeep,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Column {
                            Text(
                                text = item.categoryLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isSold) ClosedGrayText else ToskaOnPrimaryContainer,
                            )
                            Text(
                                text = "${item.conditionLabel} • ${item.negotiableLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (item.isSold) ClosedGrayText else ToskaPrimaryDeep,
                            )
                        }
                    }
                }

                // Top Status & Bookmark Overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (item.isSold) {
                        Surface(
                            color = AlertCrimson,
                            shape = CircleShape,
                            modifier = Modifier.testTag("barkas_sold_badge_${item.id}"),
                        ) {
                            Text(
                                text = stringResource(R.string.barkas_sold_badge),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    } else {
                        Surface(
                            color = OpenGreenBg,
                            shape = CircleShape,
                            border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.35f)),
                        ) {
                            Text(
                                text = stringResource(R.string.barkas_available_badge),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OpenGreenText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.92f),
                    ) {
                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("barkas_card_bookmark_${item.id}"),
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = if (isSaved) "Hapus dari favorit" else "Simpan ke favorit",
                                tint = if (isSaved) ToskaPrimaryDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                // Price + Negotiable Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.formattedPrice,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (item.isSold) ClosedGrayText else ToskaPrimaryDeep,
                        textDecoration = if (item.isSold) TextDecoration.LineThrough else TextDecoration.None,
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PendingAmberBg,
                        ) {
                            Text(
                                text = item.negotiableLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = PendingAmberText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ClosedGrayBg,
                        ) {
                            Text(
                                text = item.conditionLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = ClosedGrayText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isSold) ClosedGrayText else MaterialTheme.colorScheme.onSurface,
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
                        text = buildString {
                            append("Kec. ${item.district}")
                            if (item.codLocation.isNotBlank()) {
                                append(" • ${item.codLocation}")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.6f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${item.sellerName.ifBlank { "Warga Surabaya" }} • ${formatRelativeTimestamp(item.createdAt)}",
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
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = "${item.commentCount} Komentar",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ToskaPrimaryDeep,
                        )
                    }
                }

                if (isOwner) {
                    OutlinedButton(
                        onClick = onToggleSold,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (item.isSold) ToskaPrimaryDeep else AlertCrimson.copy(alpha = 0.6f),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("barkas_card_toggle_sold_${item.id}"),
                    ) {
                        Icon(
                            imageVector = if (item.isSold) Icons.Filled.Refresh else Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = if (item.isSold) ToskaPrimaryDeep else AlertCrimson,
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (item.isSold) {
                                stringResource(R.string.barkas_reopen_btn)
                            } else {
                                stringResource(R.string.barkas_mark_sold_btn)
                            },
                            fontWeight = FontWeight.Bold,
                            color = if (item.isSold) ToskaPrimaryDeep else AlertCrimson,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalBarkasFeaturedCard(
    item: BarkasItem,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.75f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .width(186.dp)
            .testTag("barkas_featured_${item.id}"),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(94.dp),
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
                    color = if (item.price <= 0L) Color(0xFF2E7D32) else ToskaPrimaryDeep.copy(alpha = 0.92f),
                    modifier = Modifier.align(Alignment.TopStart),
                ) {
                    Text(
                        text = if (item.price <= 0L) "GRATIS" else item.formattedPrice,
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
                        text = item.conditionLabel,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
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
                        text = "${item.district} • COD: ${item.codLocation.ifBlank { "Surabaya" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ToskaPrimaryDeep,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

