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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.UiState
import com.example.data.model.UserMoment
import com.example.data.util.formatRelativeTimestamp
import com.example.data.util.resolveImageDrawable
import com.example.ui.components.CulinaryPhotoCarousel
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.components.FullscreenZoomablePhotoViewerDialog
import com.example.ui.components.SurabayaDistrictFilterBar
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.ToskaPrimaryDeep

@Composable
fun MomenScreen(
    momentsState: UiState<List<UserMoment>>,
    currentUserId: String,
    isAdmin: Boolean,
    onOpenMomentDetail: (String) -> Unit,
    onOpenCreateMoment: () -> Unit,
    onToggleLike: (UserMoment) -> Unit,
    onReportMoment: (UserMoment) -> Unit,
    onBlockAuthor: (uid: String, name: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedDistrict by rememberSaveable { mutableStateOf("ALL") }
    var viewerTarget by remember { mutableStateOf<Pair<UserMoment, Int>?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // District Filter Bar
            SurabayaDistrictFilterBar(
                selectedDistrict = selectedDistrict,
                onSelect = { selectedDistrict = it },
                testTag = "moment_district_filter_dropdown",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            )

            when (momentsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ToskaPrimaryDeep)
                    }
                }
                is UiState.Error -> {
                    EmptyFeedPlaceholder(
                        icon = Icons.Outlined.PhotoLibrary,
                        title = "Gagal memuat Momen",
                        subtitle = momentsState.message,
                    )
                }
                is UiState.Success -> {
                    val filtered = momentsState.data.filter { moment ->
                        selectedDistrict == "ALL" || moment.district.equals(selectedDistrict, ignoreCase = true)
                    }

                    if (filtered.isEmpty()) {
                        EmptyFeedPlaceholder(
                            icon = Icons.Outlined.PhotoLibrary,
                            title = "Belum ada momen di $selectedDistrict",
                            subtitle = "Jadilah yang pertama mengabadikan potret seru sudut Surabaya!",
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("moments_feed_list"),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(filtered, key = { it.id }) { moment ->
                                MomentFeedCard(
                                    moment = moment,
                                    currentUserId = currentUserId,
                                    isAdmin = isAdmin,
                                    onOpenDetail = { onOpenMomentDetail(moment.id) },
                                    onToggleLike = { onToggleLike(moment) },
                                    onOpenFullscreenPhoto = { pageIdx -> viewerTarget = moment to pageIdx },
                                    onReport = { onReportMoment(moment) },
                                    onBlockAuthor = { onBlockAuthor(moment.authorId, moment.authorName) },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to post new moment
        ExtendedFloatingActionButton(
            onClick = onOpenCreateMoment,
            icon = {
                Icon(
                    imageVector = Icons.Filled.AddAPhoto,
                    contentDescription = null,
                )
            },
            text = { Text(text = "Posting Momen", fontWeight = FontWeight.Bold) },
            containerColor = ToskaPrimaryDeep,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp)
                .testTag("fab_create_moment"),
        )
    }

    // Zoomable Fullscreen Viewer
    viewerTarget?.let { (moment, pageIdx) ->
        FullscreenZoomablePhotoViewerDialog(
            photoUrls = moment.allPhotos,
            fallbackImageKey = moment.imageKey,
            title = moment.caption.ifBlank { "Momen Arek Surabaya" },
            initialPage = pageIdx,
            onDismiss = { viewerTarget = null },
        )
    }
}

@Composable
private fun MomentFeedCard(
    moment: UserMoment,
    currentUserId: String,
    isAdmin: Boolean,
    onOpenDetail: () -> Unit,
    onToggleLike: () -> Unit,
    onOpenFullscreenPhoto: (Int) -> Unit,
    onReport: () -> Unit,
    onBlockAuthor: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isLiked = currentUserId in moment.likedByUids
    val photos = moment.allPhotos

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.7f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("moment_card_${moment.id}"),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Author Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ToskaPrimaryDeep),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = moment.authorName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = moment.authorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = ToskaPrimaryDeep,
                                modifier = Modifier.size(12.dp),
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${moment.district}${if (moment.locationName.isNotBlank()) " • ${moment.locationName}" else ""} • ${formatRelativeTimestamp(moment.createdAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Opsi",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Laporkan Konten / Foto", color = AlertCrimson) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.ReportProblem,
                                    contentDescription = null,
                                    tint = AlertCrimson,
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onReport()
                            },
                        )
                        if (moment.authorId != currentUserId) {
                            DropdownMenuItem(
                                text = { Text("Blokir Pengguna Ini") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Security,
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onBlockAuthor()
                                },
                            )
                        }
                    }
                }
            }

            // 2. Photos Section
            if (photos.isNotEmpty()) {
                CulinaryPhotoCarousel(
                    photoUrls = photos,
                    fallbackImageKey = moment.imageKey,
                    spotName = moment.caption,
                    compactMode = false,
                    isBlurredSensitive = moment.isContentBlurred,
                    sensitiveReportCount = moment.contentReportCount,
                    onPhotoClick = onOpenFullscreenPhoto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                )
            } else if (moment.imageKey.isNotBlank()) {
                val resId = resolveImageDrawable(moment.imageKey)
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(resId)
                        .crossfade(true)
                        .build(),
                    contentDescription = moment.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clickable { onOpenFullscreenPhoto(0) },
                )
            }

            // 3. Actions Row (Heart Like, Comment, Share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleLike,
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isLiked) "Batal suka" else "Sukai",
                            tint = if (isLiked) AlertCrimson else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "${moment.likesCount} suka",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = onOpenDetail,
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubbleOutline,
                            contentDescription = "Lihat komentar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "${moment.commentCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text = "Buka Detail",
                    style = MaterialTheme.typography.labelSmall,
                    color = ToskaPrimaryDeep,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(onClick = onOpenDetail)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }

            // 4. Caption Text
            if (moment.caption.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                        .clickable(onClick = onOpenDetail),
                ) {
                    Text(
                        text = moment.caption,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
