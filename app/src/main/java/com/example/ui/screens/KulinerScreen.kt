package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BookmarkItem
import com.example.data.model.CulinarySpot
import com.example.data.model.UiState
import com.example.data.util.formatOperatingHours
import com.example.data.util.isCulinaryOpenNow
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.CulinaryDeliveryLinksRow
import com.example.ui.components.CulinaryPhotoCarousel
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.components.FullscreenZoomablePhotoViewerDialog
import com.example.ui.theme.ClosedGrayBg
import com.example.ui.theme.ClosedGrayText
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.StarGold
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
    onSelectSpot: (CulinarySpot) -> Unit,
    onToggleBookmark: (CulinarySpot) -> Unit,
    onOpenCreateSheet: () -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("ALL") }
    var onlyOpenNow by rememberSaveable { mutableStateOf(false) }
    var fullscreenViewerTarget by remember { mutableStateOf<Pair<CulinarySpot, Int>?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(top = 10.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("kuliner_feed_list"),
        ) {
            // Compact Search + Open Now Toggle Row
            item(key = "kuliner_search_row") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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

            item(key = "kuliner_categories") {
                CategoryFilterRow(
                    categories = culinaryCategories,
                    selectedKey = selectedCategory,
                    onSelect = { selectedCategory = it },
                    tagPrefix = "kuliner",
                )
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

                    if (filtered.isEmpty()) {
                        item {
                            EmptyFeedPlaceholder(
                                icon = Icons.Outlined.RestaurantMenu,
                                title = "Kuliner tidak ditemukan",
                                subtitle = "Coba ubah kata kunci pencarian atau matikan filter jam buka.",
                            )
                        }
                    } else {
                        items(filtered, key = { it.id }) { spot ->
                            val isSaved = bookmarks.any { it.targetId == spot.id && it.targetType == "KULINER" }
                            CulinaryCompactCard(
                                spot = spot,
                                isSaved = isSaved,
                                onClick = { onSelectSpot(spot) },
                                onPhotoClick = { pageIdx -> fullscreenViewerTarget = spot to pageIdx },
                                onBookmark = { onToggleBookmark(spot) },
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
    isSaved: Boolean,
    onClick: () -> Unit,
    onPhotoClick: (Int) -> Unit,
    onBookmark: () -> Unit,
) {
    val isOpen = isCulinaryOpenNow(spot.openHour, spot.closeHour)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.dp, OutlineVariantLight),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("kuliner_card_${spot.id}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(11.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CulinaryPhotoCarousel(
                photoUrls = spot.allPhotos,
                fallbackImageKey = spot.imageKey,
                spotName = spot.name,
                compactMode = true,
                onPhotoClick = onPhotoClick,
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("kuliner_card_carousel_${spot.id}"),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isOpen) OpenGreenBg else ClosedGrayBg,
                        ) {
                            Text(
                                text = if (isOpen) stringResource(R.string.status_open) else stringResource(R.string.status_closed),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isOpen) OpenGreenText else ClosedGrayText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Text(
                                text = formatCulinaryCategoryLabel(spot.category),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
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
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = spot.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
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
                            text = spot.district,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
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
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
