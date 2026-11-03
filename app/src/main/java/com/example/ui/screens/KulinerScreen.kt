package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.example.ui.sheets.LocationUsageContext
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.BookmarkItem
import com.example.data.model.CulinarySpot
import com.example.data.model.UiState
import com.example.data.util.SurabayaDistricts
import com.example.data.util.calculateDistanceKm
import com.example.data.util.formatDistanceLabel
import com.example.data.util.formatOperatingHours
import com.example.data.util.isCulinaryOpenNow
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.CulinaryDeliveryLinksRow
import com.example.ui.components.CulinaryPhotoCarousel
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.components.FullscreenZoomablePhotoViewerDialog
import com.example.ui.theme.ClosedGrayBg
import com.example.ui.theme.ClosedGrayText
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.StarGold
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep
import java.util.Locale

private val culinaryCategories = listOf(
    "ALL" to "Semua Kuliner",
    "RAWON_SOTO" to "Rawon & Soto",
    "PENYETAN_SAMBAL" to "Penyetan & Sambal",
    "LEGENDARIS" to "Legendaris",
    "KOPI_NONGKRONG" to "Kopi & Nongkrong",
    "JAJANAN_MALAM" to "Jajanan Malam",
)

fun formatCulinaryCategoryLabel(key: String): String = when (key) {
    "RAWON_SOTO" -> "Rawon & Soto"
    "PENYETAN_SAMBAL" -> "Penyetan"
    "KOPI_NONGKRONG" -> "Kopi & Kafe"
    "JAJANAN_MALAM" -> "Kuliner Malam"
    else -> "Legendaris"
}

@Composable
fun KulinerScreen(
    culinaryState: UiState<List<CulinarySpot>>,
    bookmarks: List<BookmarkItem>,
    currentUserId: String = "",
    userLat: Double = SurabayaDistricts.DEFAULT_LAT,
    userLng: Double = SurabayaDistricts.DEFAULT_LNG,
    isLiveGpsActive: Boolean = false,
    onRefreshGps: () -> Unit = {},
    onSelectSpot: (CulinarySpot) -> Unit,
    onToggleBookmark: (CulinarySpot) -> Unit,
    onOpenCreateSheet: () -> Unit,
    onReportSpotLink: (CulinarySpot) -> Unit = {},
) {
    val context = LocalContext.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("ALL") }
    var onlyOpenNow by rememberSaveable { mutableStateOf(false) }
    var sortOption by rememberSaveable { mutableStateOf("DISTANCE") } // Default urut terdekat GPS!
    var fullscreenViewerTarget by remember { mutableStateOf<Pair<CulinarySpot, Int>?>(null) }

    val requestOrRefreshGps = rememberLocationPermissionWithDisclosure(
        usageContext = LocationUsageContext.KULINER,
        onPermissionGranted = onRefreshGps,
    )

    LaunchedEffect(isLiveGpsActive) {
        if (!isLiveGpsActive && hasAnyLocationPermission(context)) {
            onRefreshGps()
        }
    }

    val sortedSpots = remember(culinaryState, searchQuery, selectedCategory, onlyOpenNow, sortOption, userLat, userLng) {
        if (culinaryState !is UiState.Success) {
            emptyList()
        } else {
            val q = searchQuery.trim().lowercase(Locale.ROOT)
            val filtered = culinaryState.data.filter { spot ->
                val catMatch = selectedCategory == "ALL" || spot.category == selectedCategory
                val queryMatch = q.isEmpty() ||
                    spot.name.lowercase(Locale.ROOT).contains(q) ||
                    spot.district.lowercase(Locale.ROOT).contains(q) ||
                    spot.description.lowercase(Locale.ROOT).contains(q)
                val openMatch = !onlyOpenNow || isCulinaryOpenNow(spot.openHour, spot.closeHour)
                catMatch && queryMatch && openMatch
            }
            when (sortOption) {
                "RATING" -> filtered.sortedWith(
                    compareByDescending<CulinarySpot> { it.ratingAvg }
                        .thenBy { calculateDistanceKm(userLat, userLng, it.lat, it.lng) }
                )
                else -> filtered.sortedWith(
                    compareBy<CulinarySpot> { calculateDistanceKm(userLat, userLng, it.lat, it.lng) }
                        .thenByDescending { it.ratingAvg }
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("kuliner_feed_list"),
        ) {
            // Compact Search + Open Now Toggle Row
            item(key = "kuliner_search_row") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search_culinary_hint),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        singleLine = true,
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = OutlineVariantLight,
                        ),
                        textStyle = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kuliner_search_input"),
                    )

                    FilterChip(
                        selected = onlyOpenNow,
                        onClick = { onlyOpenNow = !onlyOpenNow },
                        label = {
                            Text(
                                text = "Buka",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                            )
                        },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier.testTag("kuliner_open_now_chip"),
                    )
                }
            }

            // Categories row
            item(key = "kuliner_categories") {
                CategoryFilterRow(
                    categories = culinaryCategories,
                    selectedKey = selectedCategory,
                    onSelect = { selectedCategory = it },
                    tagPrefix = "kuliner",
                )
            }

            // Urutkan GPS Terdekat (Default) & Rating Toggle Bar
            item(key = "kuliner_sort_bar") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterChip(
                            selected = sortOption == "DISTANCE",
                            onClick = { sortOption = "DISTANCE" },
                            label = {
                                Text(
                                    text = "📍 Terdekat (GPS)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (sortOption == "DISTANCE") FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier.testTag("sort_kuliner_distance"),
                        )
                        FilterChip(
                            selected = sortOption == "RATING",
                            onClick = { sortOption = "RATING" },
                            label = {
                                Text(
                                    text = "⭐ Rating",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (sortOption == "RATING") FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier.testTag("sort_kuliner_rating"),
                        )
                    }

                    // GPS Status / Refresh Location Pill
                    Surface(
                        onClick = requestOrRefreshGps,
                        shape = CircleShape,
                        color = if (isLiveGpsActive) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isLiveGpsActive) Color(0xFF81C784) else OutlineVariantLight),
                        modifier = Modifier.testTag("kuliner_gps_status"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = if (isLiveGpsActive) Icons.Filled.GpsFixed else Icons.Filled.NearMe,
                                contentDescription = null,
                                tint = if (isLiveGpsActive) Color(0xFF2E7D32) else ToskaPrimaryDeep,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                text = stringResource(
                                    if (isLiveGpsActive) R.string.kuliner_gps_active_pill else R.string.kuliner_gps_enable_pill,
                                ),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = if (isLiveGpsActive) Color(0xFF1B5E20) else ToskaPrimaryDeep,
                            )
                        }
                    }
                }
            }

            if (!isLiveGpsActive) {
                item(key = "kuliner_gps_prompt_banner") {
                    Surface(
                        onClick = requestOrRefreshGps,
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, OutlineVariantLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MintSoftGradient)
                            .testTag("kuliner_gps_prompt_banner"),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MyLocation,
                                contentDescription = null,
                                tint = ToskaPrimaryDeep,
                                modifier = Modifier.size(17.dp),
                            )
                            Text(
                                text = stringResource(R.string.kuliner_gps_banner_inactive),
                                style = MaterialTheme.typography.labelSmall,
                                color = ToskaOnPrimaryContainer,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            when (culinaryState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        EmptyFeedPlaceholder(
                            icon = Icons.Outlined.RestaurantMenu,
                            title = "Gagal memuat kuliner Surabaya",
                            subtitle = culinaryState.message,
                        )
                    }
                }
                is UiState.Success -> {
                    if (sortedSpots.isEmpty()) {
                        item {
                            EmptyFeedPlaceholder(
                                icon = Icons.Outlined.RestaurantMenu,
                                title = "Kuliner tidak ditemukan",
                                subtitle = "Coba ubah kata kunci pencarian atau matikan filter jam buka.",
                            )
                        }
                    } else {
                        itemsIndexed(sortedSpots, key = { _, spot -> spot.id }) { index, spot ->
                            val isSaved = bookmarks.any { it.targetId == spot.id && it.targetType == "KULINER" }
                            val distanceKm = calculateDistanceKm(userLat, userLng, spot.lat, spot.lng)
                            CulinaryCompactCard(
                                spot = spot,
                                distanceKm = distanceKm,
                                isNearestTopSpot = sortOption == "DISTANCE" && index == 0,
                                isSaved = isSaved,
                                isReportedByMe = currentUserId in spot.linkReportedByUids,
                                onClick = { onSelectSpot(spot) },
                                onPhotoClick = { pageIdx -> fullscreenViewerTarget = spot to pageIdx },
                                onBookmark = { onToggleBookmark(spot) },
                                onReportLink = { onReportSpotLink(spot) },
                            )
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onOpenCreateSheet,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            icon = {
                Icon(
                    imageVector = Icons.Filled.AddBusiness,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.btn_add_culinary),
                    style = MaterialTheme.typography.labelLarge,
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_culinary"),
        )
    }

    val activeViewer = fullscreenViewerTarget
    if (activeViewer != null) {
        val (viewerSpot, startPage) = activeViewer
        FullscreenZoomablePhotoViewerDialog(
            photoUrls = viewerSpot.allPhotos,
            fallbackImageKey = viewerSpot.imageKey,
            title = viewerSpot.name,
            initialPage = startPage,
            onDismiss = { fullscreenViewerTarget = null },
        )
    }
}

@Composable
private fun CulinaryCompactCard(
    spot: CulinarySpot,
    distanceKm: Double = 0.0,
    isNearestTopSpot: Boolean = false,
    isSaved: Boolean,
    isReportedByMe: Boolean,
    onClick: () -> Unit,
    onPhotoClick: (Int) -> Unit,
    onBookmark: () -> Unit,
    onReportLink: () -> Unit,
) {
    val isOpen = isCulinaryOpenNow(spot.openHour, spot.closeHour)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(
            width = if (isNearestTopSpot) 1.5.dp else 1.dp,
            color = if (isNearestTopSpot) ToskaPrimary.copy(alpha = 0.55f) else OutlineVariantLight,
        ),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("kuliner_card_${spot.id}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CulinaryPhotoCarousel(
                photoUrls = spot.allPhotos,
                fallbackImageKey = spot.imageKey,
                spotName = spot.name,
                compactMode = true,
                isBlurredSensitive = spot.isContentBlurred,
                sensitiveReportCount = spot.contentReportCount,
                onPhotoClick = onPhotoClick,
                modifier = Modifier
                    .size(86.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("kuliner_card_carousel_${spot.id}"),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            onClick = onClick,
                            shape = CircleShape,
                            color = ToskaPrimary.copy(alpha = 0.14f),
                            modifier = Modifier.testTag("kuliner_distance_tag_${spot.id}"),
                        ) {
                            Text(
                                text = "📍 ${formatDistanceLabel(distanceKm)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ToskaPrimaryDeep,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (isOpen) OpenGreenBg else ClosedGrayBg,
                        ) {
                            Text(
                                text = if (isOpen) stringResource(R.string.status_open) else stringResource(R.string.status_closed),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isOpen) OpenGreenText else ClosedGrayText,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                        if (isNearestTopSpot) {
                            Surface(
                                shape = CircleShape,
                                color = ToskaPrimaryDeep,
                            ) {
                                Text(
                                    text = "#1",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onBookmark,
                        modifier = Modifier
                            .size(30.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("bookmark_kuliner_${spot.id}"),
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = stringResource(R.string.btn_bookmarks),
                            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Text(
                    text = spot.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = spot.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = StarGold,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f", spot.ratingAvg),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "(${spot.reviewCount})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = "${spot.district} • ${formatCulinaryCategoryLabel(spot.category)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Text(
                    text = "${spot.priceRange} • ${formatOperatingHours(spot.openHour, spot.closeHour)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = ToskaPrimaryDeep,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                CulinaryDeliveryLinksRow(
                    spot = spot,
                    compact = true,
                    isReportedByMe = isReportedByMe,
                    onReportSuspiciousLink = onReportLink,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
