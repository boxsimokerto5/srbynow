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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.POST_TYPE_LENSA_WARGA
import com.example.data.model.POST_TYPE_MOMEN
import com.example.data.model.UiState
import com.example.data.model.UserMoment
import com.example.data.util.formatRelativeTimestamp
import com.example.data.util.resolveImageDrawable
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import com.example.ui.components.CulinaryPhotoCarousel
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.components.FullscreenZoomablePhotoViewerDialog
import com.example.ui.components.SurabayaDistrictFilterBar
import com.example.ui.components.SurabayaVideoPlayer
import com.example.ui.components.TikTokReelsViewerDialog
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
    onOpenCreateLensaWarga: () -> Unit = onOpenCreateMoment,
    onToggleLike: (UserMoment) -> Unit,
    onReportMoment: (UserMoment) -> Unit,
    onBlockAuthor: (uid: String, name: String) -> Unit,
    onOpenAuthorProfile: (authorId: String, authorName: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    var selectedDistrict by rememberSaveable { mutableStateOf("ALL") }
    var selectedCategory by rememberSaveable { mutableStateOf("ALL") } // "ALL", "VIDEO", "MOMEN", "LENSA_WARGA", "KULINER", "KOMUNITAS"
    var viewerTarget by remember { mutableStateOf<Pair<UserMoment, Int>?>(null) }
    var reelsTarget by remember { mutableStateOf<UserMoment?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // District Filter Bar
            SurabayaDistrictFilterBar(
                selectedDistrict = selectedDistrict,
                onSelect = { selectedDistrict = it },
                testTag = "moment_district_filter_dropdown",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )

            // Category Chips (Horizontal Scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = selectedCategory == "ALL",
                    onClick = { selectedCategory = "ALL" },
                    label = { Text("🌟 Semua") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimaryDeep,
                        selectedLabelColor = Color.White,
                    ),
                )
                FilterChip(
                    selected = selectedCategory == "VIDEO",
                    onClick = { selectedCategory = "VIDEO" },
                    label = { Text("🎬 Video Reels") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD81B60),
                        selectedLabelColor = Color.White,
                    ),
                )
                FilterChip(
                    selected = selectedCategory == "MOMEN",
                    onClick = { selectedCategory = "MOMEN" },
                    label = { Text("📸 Momen Arek") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimaryDeep,
                        selectedLabelColor = Color.White,
                    ),
                )
                FilterChip(
                    selected = selectedCategory == "LENSA_WARGA",
                    onClick = { selectedCategory = "LENSA_WARGA" },
                    label = { Text("🏙️ Lensa Warga") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimaryDeep,
                        selectedLabelColor = Color.White,
                    ),
                )
                FilterChip(
                    selected = selectedCategory == "KULINER",
                    onClick = { selectedCategory = "KULINER" },
                    label = { Text("🍜 Kuliner") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimaryDeep,
                        selectedLabelColor = Color.White,
                    ),
                )
                FilterChip(
                    selected = selectedCategory == "KOMUNITAS",
                    onClick = { selectedCategory = "KOMUNITAS" },
                    label = { Text("👥 Komunitas") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimaryDeep,
                        selectedLabelColor = Color.White,
                    ),
                )
            }

            val allMoments = (momentsState as? UiState.Success)?.data.orEmpty()

            if (selectedCategory == "LENSA_WARGA") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ToskaPrimaryDeep.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, ToskaPrimaryDeep.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("🏙️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Lensa Warga Surabaya",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ToskaPrimaryDeep,
                            )
                            Text(
                                text = "Galeri foto & video estetika landmark Surabaya tanpa perlu tindak lanjut penanganan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else if (selectedCategory == "VIDEO") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFD81B60).copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color(0xFFD81B60).copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("🎬", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Reels & Video Arek Surabaya",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD81B60),
                            )
                            Text(
                                text = "Video vertikal sudut kota berdurasi hingga 60 detik. Didukung kompresi otomatis & S3 storage super hemat.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

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
                        val matchDistrict = selectedDistrict == "ALL" || moment.district.equals(selectedDistrict, ignoreCase = true)
                        val matchCategory = when (selectedCategory) {
                            "VIDEO" -> moment.isVideo
                            "LENSA_WARGA" -> moment.isLensaWarga
                            "MOMEN" -> !moment.isLensaWarga
                            "KULINER" -> {
                                val c = moment.caption.lowercase()
                                c.contains("kuliner") || c.contains("makan") || c.contains("warung") || c.contains("kopi")
                            }
                            "KOMUNITAS" -> {
                                val c = moment.caption.lowercase()
                                c.contains("komunitas") || c.contains("arek") || c.contains("bareng") || c.contains("acara")
                            }
                            else -> true
                        }
                        matchDistrict && matchCategory
                    }

                    if (filtered.isEmpty()) {
                        EmptyFeedPlaceholder(
                            icon = when (selectedCategory) {
                                "VIDEO" -> Icons.Filled.Videocam
                                "LENSA_WARGA" -> Icons.Filled.CameraAlt
                                else -> Icons.Outlined.PhotoLibrary
                            },
                            title = when (selectedCategory) {
                                "VIDEO" -> "Belum ada Video Reels di $selectedDistrict"
                                "LENSA_WARGA" -> "Belum ada potret Lensa Warga di $selectedDistrict"
                                else -> "Belum ada momen di $selectedDistrict"
                            },
                            subtitle = when (selectedCategory) {
                                "VIDEO" -> "Jadilah yang pertama mengunggah video reels estetik kota Surabaya!"
                                "LENSA_WARGA" -> "Jadilah yang pertama mengabadikan sudut estetik kota Surabaya!"
                                else -> "Jadilah yang pertama mengabadikan potret seru sudut Surabaya!"
                            },
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("moments_feed_list"),
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 84.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp),
                        ) {
                            if (filtered.isNotEmpty() && selectedCategory == "ALL" && selectedDistrict == "ALL") {
                                item(key = "moments_featured_carousel") {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 2.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                text = "Sorotan Foto & Video Populer",
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
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            items(filtered.take(6), key = { "feat_mom_${it.id}" }) { mom ->
                                                HorizontalMomentFeaturedCard(
                                                    moment = mom,
                                                    onClick = {
                                                        if (mom.isVideo) {
                                                            reelsTarget = mom
                                                        } else {
                                                            onOpenMomentDetail(mom.id)
                                                        }
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            items(filtered, key = { it.id }) { moment ->
                                MomentFeedCard(
                                    moment = moment,
                                    currentUserId = currentUserId,
                                    isAdmin = isAdmin,
                                    onOpenDetail = { onOpenMomentDetail(moment.id) },
                                    onToggleLike = { onToggleLike(moment) },
                                    onOpenFullscreenPhoto = { pageIdx -> viewerTarget = moment to pageIdx },
                                    onOpenFullscreenReels = { reelsTarget = moment },
                                    onReport = { onReportMoment(moment) },
                                    onBlockAuthor = { onBlockAuthor(moment.authorId, moment.authorName) },
                                    onOpenAuthorProfile = { onOpenAuthorProfile(moment.authorId, moment.authorName) },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to post new moment, video reels, or lensa warga
        ExtendedFloatingActionButton(
            onClick = {
                if (selectedCategory == "LENSA_WARGA") {
                    onOpenCreateLensaWarga()
                } else {
                    onOpenCreateMoment()
                }
            },
            icon = {
                Icon(
                    imageVector = when (selectedCategory) {
                        "VIDEO" -> Icons.Filled.Videocam
                        "LENSA_WARGA" -> Icons.Filled.CameraAlt
                        else -> Icons.Filled.AddAPhoto
                    },
                    contentDescription = null,
                )
            },
            text = {
                Text(
                    text = when (selectedCategory) {
                        "VIDEO" -> "Posting Video Reels"
                        "LENSA_WARGA" -> "Posting Lensa Warga"
                        else -> "Posting Momen"
                    },
                    fontWeight = FontWeight.Bold,
                )
            },
            containerColor = if (selectedCategory == "VIDEO") Color(0xFFD81B60) else ToskaPrimaryDeep,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp)
                .testTag("fab_create_moment"),
        )
    }

    // Zoomable Fullscreen Photo Viewer
    viewerTarget?.let { (moment, pageIdx) ->
        FullscreenZoomablePhotoViewerDialog(
            photoUrls = moment.allPhotos,
            fallbackImageKey = moment.imageKey,
            title = moment.caption.ifBlank { "Momen Arek Surabaya" },
            initialPage = pageIdx,
            onDismiss = { viewerTarget = null },
        )
    }

    // Fullscreen TikTok / Reels Video Viewer
    reelsTarget?.let { rMoment ->
        val isLiked = currentUserId in rMoment.likedByUids
        TikTokReelsViewerDialog(
            moment = rMoment,
            isLiked = isLiked,
            onToggleLike = { onToggleLike(rMoment) },
            onOpenComments = {
                reelsTarget = null
                onOpenMomentDetail(rMoment.id)
            },
            onDismiss = { reelsTarget = null },
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
    onOpenFullscreenReels: () -> Unit = {},
    onReport: () -> Unit,
    onBlockAuthor: () -> Unit,
    onOpenAuthorProfile: () -> Unit = {},
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isLiked = currentUserId in moment.likedByUids
    val photos = moment.allPhotos

    Surface(
        shape = RoundedCornerShape(16.dp),
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
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenAuthorProfile)
                        .padding(vertical = 1.dp)
                        .testTag("moment_author_header_${moment.id}"),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF006B5E),
                                        Color(0xFF26A69A),
                                        Color(0xFF80CBC4),
                                        Color(0xFF006B5E),
                                    )
                                )
                            )
                            .padding(2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(ToskaPrimaryDeep),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = moment.authorName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = moment.authorName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "• Lihat Profil",
                                style = MaterialTheme.typography.labelSmall,
                                color = ToskaPrimaryDeep,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
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

                        if (moment.isVideo) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFD81B60).copy(alpha = 0.12f),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        tint = Color(0xFFD81B60),
                                        modifier = Modifier.size(11.dp),
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (moment.isLensaWarga) "Lensa Video • S3 H.264" else "Video Reels • S3 H.264",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFD81B60),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                    )
                                }
                            }
                        } else if (moment.isLensaWarga) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ToskaPrimaryDeep.copy(alpha = 0.12f),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CameraAlt,
                                        contentDescription = null,
                                        tint = ToskaPrimaryDeep,
                                        modifier = Modifier.size(11.dp),
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Lensa Warga • Potret Kota",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ToskaPrimaryDeep,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                    )
                                }
                            }
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

            // 2. Media Section (Video or Photos)
            if (moment.isVideo && moment.videoUrl.isNotBlank()) {
                SurabayaVideoPlayer(
                    videoUrl = moment.videoUrl,
                    thumbnailUrl = moment.videoThumbnailUrl,
                    durationSec = moment.videoDurationSec,
                    onExpandFullscreen = onOpenFullscreenReels,
                    onDoubleTapLike = onToggleLike,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp),
                )
            } else if (photos.isNotEmpty()) {
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
                        .height(196.dp),
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
                        .height(196.dp)
                        .clickable { onOpenFullscreenPhoto(0) },
                )
            }

            // 3. Actions Row (Heart Like, Comment, Share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
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

                    Spacer(modifier = Modifier.width(8.dp))

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
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }

            // 4. Caption Text
            if (moment.caption.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                        .clickable(onClick = onOpenDetail),
                ) {
                    Text(
                        text = moment.caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun HorizontalMomentFeaturedCard(
    moment: UserMoment,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        color = Color.White,
        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.75f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .width(172.dp)
            .testTag("moment_featured_${moment.id}"),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(94.dp),
            ) {
                val photoModel = moment.allPhotos.firstOrNull()?.ifBlank { null }
                    ?: resolveImageDrawable(moment.imageKey)
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = moment.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 10.dp),
                    color = when {
                        moment.isVideo -> Color(0xFFD81B60)
                        moment.isLensaWarga -> ToskaPrimaryDeep
                        else -> Color(0xFFE91E63)
                    },
                    modifier = Modifier.align(Alignment.TopStart),
                ) {
                    Text(
                        text = when {
                            moment.isVideo -> "🎬 Reels"
                            moment.isLensaWarga -> "Lensa Kota"
                            else -> "Momen Warga"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                }
                if (moment.isVideo) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Video",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(5.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(11.dp),
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${moment.likesCount}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(9.dp)) {
                Text(
                    text = moment.caption.ifBlank { "Momen indah di Surabaya" },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${moment.authorName} • ${moment.district}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

