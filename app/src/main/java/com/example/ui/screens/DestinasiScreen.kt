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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.BookmarkItem
import com.example.data.model.SurabayaDestination
import com.example.data.model.UiState
import com.example.data.util.resolveImageDrawable
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.components.FullscreenZoomablePhotoViewerDialog
import com.example.ui.components.SurabayaDistrictFilterBar
import com.example.ui.components.openGoogleMapsLocation
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.ToskaPrimaryDeep

private val destinationCategories = listOf(
    "ALL" to "Semua Destinasi",
    "SEJARAH" to "Wisata Sejarah",
    "TAMAN" to "Taman Kota",
    "BAHARI" to "Wisata Bahari",
    "RELIGI" to "Religi & Budaya",
    "EDUKASI" to "Museum & Edukasi",
)

@Composable
fun DestinasiScreen(
    destinationsState: UiState<List<SurabayaDestination>>,
    bookmarks: List<BookmarkItem>,
    isAdmin: Boolean,
    onOpenDestinationDetail: (String) -> Unit,
    onOpenCreateDestination: () -> Unit,
    onToggleBookmark: (destination: SurabayaDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var selectedCategory by rememberSaveable { mutableStateOf("ALL") }
    var selectedDistrict by rememberSaveable { mutableStateOf("ALL") }
    var viewingPhotoUrl by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Category horizontal filter chips
            CategoryFilterRow(
                categories = destinationCategories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )

            // District filter
            SurabayaDistrictFilterBar(
                selectedDistrict = selectedDistrict,
                onDistrictSelected = { selectedDistrict = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            )

            // Admin notice banner
            if (!isAdmin) {
                Surface(
                    color = ToskaPrimaryDeep.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.admin_post_destination_notice),
                            style = MaterialTheme.typography.labelSmall,
                            color = ToskaPrimaryDeep,
                        )
                    }
                }
            } else {
                Surface(
                    color = ToskaPrimaryDeep,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mode Kurator Destinasi: Anda dapat menerbitkan destinasi baru",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                }
            }

            when (destinationsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ToskaPrimaryDeep)
                    }
                }
                is UiState.Error -> {
                    EmptyFeedPlaceholder(
                        icon = Icons.Outlined.Place,
                        title = "Gagal memuat Destinasi",
                        subtitle = destinationsState.message,
                    )
                }
                is UiState.Success -> {
                    val filtered = destinationsState.data.filter { dest ->
                        val matchCat = when (selectedCategory) {
                            "ALL" -> true
                            "SEJARAH" -> dest.category.contains("sejarah", ignoreCase = true)
                            "TAMAN" -> dest.category.contains("taman", ignoreCase = true)
                            "BAHARI" -> dest.category.contains("bahari", ignoreCase = true) || dest.category.contains("perahu", ignoreCase = true)
                            "RELIGI" -> dest.category.contains("religi", ignoreCase = true) || dest.category.contains("budaya", ignoreCase = true)
                            "EDUKASI" -> dest.category.contains("museum", ignoreCase = true) || dest.category.contains("edukasi", ignoreCase = true)
                            else -> true
                        }
                        val matchDistrict = selectedDistrict == "ALL" || dest.district.equals(selectedDistrict, ignoreCase = true)
                        matchCat && matchDistrict
                    }

                    if (filtered.isEmpty()) {
                        EmptyFeedPlaceholder(
                            icon = Icons.Outlined.Place,
                            title = "Tidak ada destinasi ditemukan",
                            subtitle = "Coba pilih kategori atau kecamatan lainnya.",
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("destinations_list"),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(filtered, key = { it.id }) { destination ->
                                val isBookmarked = bookmarks.any { it.targetId == destination.id }
                                DestinationCard(
                                    destination = destination,
                                    isBookmarked = isBookmarked,
                                    onOpenDetail = { onOpenDestinationDetail(destination.id) },
                                    onToggleBookmark = { onToggleBookmark(destination) },
                                    onNavigateMaps = {
                                        openGoogleMapsLocation(
                                            context = context,
                                            lat = destination.lat,
                                            lng = destination.lng,
                                            label = destination.title,
                                        )
                                    },
                                    onPhotoClick = { url -> viewingPhotoUrl = url },
                                )
                            }
                        }
                    }
                }
            }
        }

        // ONLY Admin sees the FAB to create destinations!
        if (isAdmin) {
            ExtendedFloatingActionButton(
                onClick = onOpenCreateDestination,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                    )
                },
                text = { Text(text = "Tambah Destinasi", fontWeight = FontWeight.Bold) },
                containerColor = ToskaPrimaryDeep,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 24.dp)
                    .testTag("fab_create_destination"),
            )
        }
    }

    viewingPhotoUrl?.let { url ->
        FullscreenZoomablePhotoViewerDialog(
            photoUrl = url,
            onDismiss = { viewingPhotoUrl = null },
        )
    }
}

@Composable
private fun DestinationCard(
    destination: SurabayaDestination,
    isBookmarked: Boolean,
    onOpenDetail: () -> Unit,
    onToggleBookmark: () -> Unit,
    onNavigateMaps: () -> Unit,
    onPhotoClick: (String) -> Unit,
) {
    val photoModel = destination.firstPhotoUrl.ifBlank { resolveImageDrawable(destination.imageResName) }

    Surface(
        onClick = onOpenDetail,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.7f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("destination_card_${destination.id}"),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = destination.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onPhotoClick(destination.firstPhotoUrl.ifBlank { destination.imageResName }) },
                )

                // Category pill
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    color = ToskaPrimaryDeep.copy(alpha = 0.92f),
                    modifier = Modifier.align(Alignment.TopStart),
                ) {
                    Text(
                        text = destination.category,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }

                // Bookmark icon
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f)),
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Simpan ke favorit",
                        tint = if (isBookmarked) ToskaPrimaryDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }

                // Rating overlay
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f (%d ulasan)", destination.rating, destination.reviewsCount),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = destination.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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
                        text = "${destination.district} • ${destination.address}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = OpenGreenBg,
                    ) {
                        Text(
                            text = destination.ticketPrice,
                            color = OpenGreenText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = destination.operationalHours,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions: Google Maps rute & Detail
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalButton(
                        onClick = onNavigateMaps,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ToskaPrimaryDeep.copy(alpha = 0.12f),
                            contentColor = ToskaPrimaryDeep,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize(),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Directions,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Rute Peta", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onOpenDetail,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize(),
                    ) {
                        Text(text = "Detail Wisata", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
