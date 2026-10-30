package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.CulinarySpot
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimaryDeep
import kotlin.math.abs

fun openExternalUrlSafely(context: Context, rawUrl: String): Boolean {
    val cleaned = rawUrl.trim()
    if (cleaned.isBlank()) return false
    val normalized = if (
        cleaned.startsWith("http://", ignoreCase = true) ||
        cleaned.startsWith("https://", ignoreCase = true) ||
        cleaned.startsWith("geo:", ignoreCase = true)
    ) {
        cleaned
    } else {
        "https://$cleaned"
    }
    return try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalized)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    } catch (_: Exception) {
        false
    }
}

fun openCulinaryMapsDirection(context: Context, spot: CulinarySpot) {
    if (spot.mapsUrl.isNotBlank()) {
        val opened = openExternalUrlSafely(context, spot.mapsUrl)
        if (opened) return
    }
    val queryText = buildString {
        append(spot.name)
        if (spot.address.isNotBlank()) append(", ${spot.address}")
        if (spot.district.isNotBlank()) append(", Kec. ${spot.district}")
        append(", Surabaya")
    }
    val encodedQuery = Uri.encode(queryText)
    val mapsSearchUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedQuery")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, mapsSearchUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
        openGoogleMapsLocation(context, spot.lat, spot.lng, spot.name)
    }
}

/**
 * Multi-photo picker section (1..4 photos) using Android zero-permission Photo Picker.
 */
@Composable
fun FormMultiPhotoPickerSection(
    selectedPhotoUris: List<Uri>,
    onPhotosChanged: (List<Uri>) -> Unit,
    maxPhotos: Int = 4,
    enabled: Boolean = true,
    title: String = stringResource(R.string.kuliner_multi_photo_title),
    subtitle: String = stringResource(R.string.kuliner_multi_photo_subtitle),
) {
    val multiPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxPhotos),
    ) { uris ->
        if (uris.isNotEmpty()) {
            val combined = (selectedPhotoUris + uris).distinct().take(maxPhotos)
            onPhotosChanged(combined)
        }
    }

    val singlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val combined = (selectedPhotoUris + uri).distinct().take(maxPhotos)
            onPhotosChanged(combined)
        }
    }

    val launchGallery = {
        if (enabled && selectedPhotoUris.size < maxPhotos) {
            val remaining = maxPhotos - selectedPhotoUris.size
            val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            if (remaining >= 2) {
                multiPickerLauncher.launch(request)
            } else {
                singlePickerLauncher.launch(request)
            }
        }
    }

    if (selectedPhotoUris.isEmpty()) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF3FBF7),
            border = BorderStroke(1.dp, OutlineVariantLight),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable(enabled = enabled, onClick = launchGallery)
                .testTag("form_pick_multi_photo_btn"),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MintSoftGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddAPhoto,
                        contentDescription = title,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(21.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ToskaOnPrimaryContainer,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF6FBF8),
            border = BorderStroke(1.dp, OutlineVariantLight),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = null,
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Foto Dipilih (${selectedPhotoUris.size}/$maxPhotos)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = ToskaOnPrimaryContainer,
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = OpenGreenBg,
                    ) {
                        Text(
                            text = if (selectedPhotoUris.size < maxPhotos) {
                                "Bisa tambah ${maxPhotos - selectedPhotoUris.size} lagi"
                            } else {
                                "Maksimal $maxPhotos foto"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = OpenGreenText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    itemsIndexed(selectedPhotoUris, key = { idx, uri -> "${idx}_$uri" }) { index, uri ->
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, OutlineVariantLight, RoundedCornerShape(14.dp)),
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(uri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto kuliner ${index + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .matchParentSize()
                                    .testTag("form_multi_photo_preview_$index"),
                            )

                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.58f),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(6.dp),
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .minimumInteractiveComponentSize()
                                    .clickable(enabled = enabled) {
                                        val updated = selectedPhotoUris.toMutableList().also { it.removeAt(index) }
                                        onPhotosChanged(updated)
                                    }
                                    .testTag("form_remove_multi_photo_$index"),
                                contentAlignment = Alignment.TopEnd,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xCC93000A)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Hapus foto ${index + 1}",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            }
                        }
                    }

                    if (selectedPhotoUris.size < maxPhotos) {
                        item(key = "add_more_photo_tile") {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, ToskaPrimaryDeep.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(enabled = enabled, onClick = launchGallery)
                                    .testTag("form_add_more_photo_btn"),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = stringResource(R.string.kuliner_add_more_photo),
                                        tint = ToskaPrimaryDeep,
                                        modifier = Modifier.size(24.dp),
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(R.string.kuliner_add_more_photo),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ToskaPrimaryDeep,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Swipeable HorizontalPager carousel for 1..4 culinary photos.
 * Tapping any photo triggers [onPhotoClick] with the tapped page index so a fullscreen lightbox opens.
 */
@Composable
fun CulinaryPhotoCarousel(
    photoUrls: List<String>,
    fallbackImageKey: String,
    spotName: String,
    modifier: Modifier = Modifier,
    compactMode: Boolean = false,
    onPhotoClick: (pageIndex: Int) -> Unit,
) {
    val cleanUrls = remember(photoUrls) { photoUrls.filter { it.isNotBlank() } }
    val pageCount = cleanUrls.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount })

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val currentUrl = cleanUrls.getOrNull(page).orEmpty()
            SurabayaRemoteOrFallbackImage(
                imageUrl = currentUrl,
                fallbackImageKey = fallbackImageKey,
                contentDescription = "$spotName - Foto ${page + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onPhotoClick(page) },
            )
        }

        // Top-right counter pill when > 1 photo (or subtle expand icon in detail mode)
        if (cleanUrls.size > 1) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.60f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(if (compactMode) 5.dp else 8.dp),
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${cleanUrls.size}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(
                        horizontal = if (compactMode) 6.dp else 8.dp,
                        vertical = if (compactMode) 1.dp else 3.dp,
                    ),
                )
            }

            // Bottom indicator dots
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (compactMode) 5.dp else 8.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.38f))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (i in cleanUrls.indices) {
                    val active = i == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .size(
                                width = if (active) (if (compactMode) 10.dp else 14.dp) else (if (compactMode) 5.dp else 6.dp),
                                height = if (compactMode) 5.dp else 6.dp,
                            )
                            .clip(CircleShape)
                            .background(if (active) Color.White else Color.White.copy(alpha = 0.50f)),
                    )
                }
            }
        } else if (!compactMode) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.50f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.OpenInFull,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = "Perbesar",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

/**
 * Fullscreen Lightbox Photo Viewer with 2-finger pinch-to-zoom, double-tap zoom, pan,
 * and smooth left/right HorizontalPager swipe across all uploaded culinary photos.
 */
@Composable
fun FullscreenZoomablePhotoViewerDialog(
    photoUrls: List<String>,
    fallbackImageKey: String,
    title: String,
    initialPage: Int = 0,
    onDismiss: () -> Unit,
) {
    val cleanUrls = remember(photoUrls) { photoUrls.filter { it.isNotBlank() } }
    val pageCount = cleanUrls.size.coerceAtLeast(1)
    val safeInitial = initialPage.coerceIn(0, pageCount - 1)
    val pagerState = rememberPagerState(initialPage = safeInitial, pageCount = { pageCount })

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF5070B0E))
                .testTag("fullscreen_photo_viewer"),
        ) {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                ZoomablePhotoPage(
                    imageUrl = cleanUrls.getOrNull(page).orEmpty(),
                    fallbackImageKey = fallbackImageKey,
                    contentDescription = "$title (${page + 1}/$pageCount)",
                    isCurrentPage = pagerState.currentPage == page,
                )
            }

            // Top bar: Title + Page Counter + Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Foto ${pagerState.currentPage + 1} dari $pageCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.80f),
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                        .testTag("fullscreen_photo_close_btn"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.photo_viewer_close_cd),
                        tint = Color.White,
                    )
                }
            }

            // Bottom Hint + Page Dots
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (pageCount > 1) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        for (i in 0 until pageCount) {
                            val selected = i == pagerState.currentPage
                            Box(
                                modifier = Modifier
                                    .size(width = if (selected) 18.dp else 7.dp, height = 7.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) Color.White else Color.White.copy(alpha = 0.45f)),
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.photo_viewer_zoom_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.78f),
                )
            }
        }
    }
}

@Composable
private fun ZoomablePhotoPage(
    imageUrl: String,
    fallbackImageKey: String,
    contentDescription: String,
    isCurrentPage: Boolean,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(isCurrentPage) {
        if (!isCurrentPage && scale != 1f) {
            scale = 1f
            offset = Offset.Zero
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1.05f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                            offset = Offset.Zero
                        }
                    },
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val pressedCount = event.changes.count { it.pressed }
                        if (pressedCount >= 2) {
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()
                            val newScale = (scale * zoomChange).coerceIn(1f, 4.5f)
                            val maxX = ((containerSize.width * (newScale - 1f)) / 2f).coerceAtLeast(0f)
                            val maxY = ((containerSize.height * (newScale - 1f)) / 2f).coerceAtLeast(0f)
                            scale = newScale
                            offset = if (newScale <= 1.01f) {
                                Offset.Zero
                            } else {
                                Offset(
                                    x = (offset.x + panChange.x).coerceIn(-maxX, maxX),
                                    y = (offset.y + panChange.y).coerceIn(-maxY, maxY),
                                )
                            }
                            event.changes.forEach { change ->
                                if (change.positionChanged()) {
                                    change.consume()
                                }
                            }
                        } else if (pressedCount == 1 && scale > 1.02f) {
                            val panChange = event.calculatePan()
                            val maxX = ((containerSize.width * (scale - 1f)) / 2f).coerceAtLeast(0f)
                            val maxY = ((containerSize.height * (scale - 1f)) / 2f).coerceAtLeast(0f)
                            val nextX = offset.x + panChange.x
                            val nextY = offset.y + panChange.y
                            val clampedX = nextX.coerceIn(-maxX, maxX)
                            val clampedY = nextY.coerceIn(-maxY, maxY)
                            val hitHorizontalBoundary = abs(nextX - clampedX) > 0.5f && abs(panChange.x) > abs(panChange.y)

                            offset = Offset(clampedX, clampedY)

                            // If user pans horizontally past the zoomed edge, let HorizontalPager swipe to next/prev photo
                            if (!hitHorizontalBoundary) {
                                event.changes.forEach { change ->
                                    if (change.positionChanged()) {
                                        change.consume()
                                    }
                                }
                            }
                        }
                        // When pressedCount == 1 && scale <= 1.02f, do NOT consume so HorizontalPager swipes freely!
                    } while (event.changes.any { it.pressed })

                    if (scale < 1.02f) {
                        scale = 1f
                        offset = Offset.Zero
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        SurabayaRemoteOrFallbackImage(
            imageUrl = imageUrl,
            fallbackImageKey = fallbackImageKey,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
    }
}

/**
 * Branded online food delivery action chips (GoFood, GrabFood, ShopeeFood) rendered only when links exist.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CulinaryDeliveryLinksRow(
    spot: CulinarySpot,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val hasAny = spot.gofoodUrl.isNotBlank() || spot.grabfoodUrl.isNotBlank() || spot.shopeefoodUrl.isNotBlank()
    if (!hasAny) return

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (spot.gofoodUrl.isNotBlank()) {
            DeliveryPlatformChip(
                label = "GoFood",
                icon = Icons.Filled.DeliveryDining,
                bgColor = Color(0xFFFFEBEE),
                borderColor = Color(0xFFEF9A9A),
                contentColor = Color(0xFFC62828),
                compact = compact,
                testTag = "delivery_gofood_${spot.id}",
                onClick = { openExternalUrlSafely(context, spot.gofoodUrl) },
            )
        }
        if (spot.grabfoodUrl.isNotBlank()) {
            DeliveryPlatformChip(
                label = "GrabFood",
                icon = Icons.Filled.Storefront,
                bgColor = Color(0xFFE8F5E9),
                borderColor = Color(0xFFA5D6A7),
                contentColor = Color(0xFF00880C),
                compact = compact,
                testTag = "delivery_grabfood_${spot.id}",
                onClick = { openExternalUrlSafely(context, spot.grabfoodUrl) },
            )
        }
        if (spot.shopeefoodUrl.isNotBlank()) {
            DeliveryPlatformChip(
                label = "ShopeeFood",
                icon = Icons.Filled.ShoppingBag,
                bgColor = Color(0xFFFBE9E7),
                borderColor = Color(0xFFFFAB91),
                contentColor = Color(0xFFD84315),
                compact = compact,
                testTag = "delivery_shopeefood_${spot.id}",
                onClick = { openExternalUrlSafely(context, spot.shopeefoodUrl) },
            )
        }
    }
}

@Composable
private fun DeliveryPlatformChip(
    label: String,
    icon: ImageVector,
    bgColor: Color,
    borderColor: Color,
    contentColor: Color,
    compact: Boolean,
    testTag: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .then(if (!compact) Modifier.minimumInteractiveComponentSize() else Modifier)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 8.dp else 11.dp,
                vertical = if (compact) 3.dp else 6.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(if (compact) 12.dp else 15.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
        }
    }
}
