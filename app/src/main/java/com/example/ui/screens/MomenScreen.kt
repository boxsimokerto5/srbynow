package com.example.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import com.example.ui.components.shareMomentReel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import android.net.Uri
import android.util.Log
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.UiState
import com.example.data.model.UserMoment
import com.example.data.util.resolveImageDrawable
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.ToskaPrimaryDeep
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    // Shared sound state across all reels (persisted when switching videos)
    var isGlobalMuted by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("instagram_reels_screen"),
    ) {
        when (momentsState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
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
                val moments = momentsState.data
                if (moments.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(54.dp),
                            )
                            Text(
                                text = "Belum Ada Video Reels / Momen",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Jadilah yang pertama mengunggah video reels atau potret seru sudut kota Surabaya!",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Surface(
                                onClick = onOpenCreateMoment,
                                shape = RoundedCornerShape(20.dp),
                                color = ToskaPrimaryDeep,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Posting Video / Momen", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Full-screen Instagram Reels Vertical Pager
                    val pagerState = rememberPagerState(pageCount = { moments.size })

                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("reels_vertical_pager"),
                    ) { pageIndex ->
                        val moment = moments[pageIndex]
                        val isCurrentPage = pagerState.currentPage == pageIndex

                        InstagramReelPageItem(
                            moment = moment,
                            isCurrentPage = isCurrentPage,
                            isGlobalMuted = isGlobalMuted,
                            onToggleMute = { isGlobalMuted = !isGlobalMuted },
                            isLiked = currentUserId in moment.likedByUids,
                            onToggleLike = { onToggleLike(moment) },
                            onOpenComments = { onOpenMomentDetail(moment.id) },
                            onOpenAuthorProfile = { onOpenAuthorProfile(moment.authorId, moment.authorName) },
                            onReport = { onReportMoment(moment) },
                            onBlockAuthor = { onBlockAuthor(moment.authorId, moment.authorName) },
                        )
                    }

                    // Top Bar Overlay (Translucent, Minimalist, Status-Bar Aware)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .align(Alignment.TopCenter),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Reels Arek",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.shadow(8.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ToskaPrimaryDeep.copy(alpha = 0.85f),
                            ) {
                                Text(
                                    text = "Surabaya",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                )
                            }
                        }

                        // Sleek Floating Camera / Post Button
                        Surface(
                            onClick = onOpenCreateMoment,
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = "Posting Reels",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Posting",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun InstagramReelPageItem(
    moment: UserMoment,
    isCurrentPage: Boolean,
    isGlobalMuted: Boolean,
    onToggleMute: () -> Unit,
    isLiked: Boolean,
    onToggleLike: () -> Unit,
    onOpenComments: () -> Unit,
    onOpenAuthorProfile: () -> Unit,
    onReport: () -> Unit,
    onBlockAuthor: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var isUserPaused by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableFloatStateOf(0f) }
    var showPlayPauseIndicator by remember { mutableStateOf(false) }
    var isHeartVisible by remember { mutableStateOf(false) }
    var isCaptionExpanded by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val heartScale = remember { Animatable(0f) }

    // ExoPlayer for Video Moments
    val isVideo = moment.isVideo && (moment.videoUrl.isNotBlank() || moment.mediaType.equals("VIDEO", ignoreCase = true))

    // Resolve URL safely: redirect stale Google 403 URLs or empty to bundled reliable asset
    val targetVideoUri = remember(moment.videoUrl) {
        val clean = moment.videoUrl.trim()
        if (clean.isBlank() || clean.contains("commondatastorage.googleapis.com")) {
            Uri.parse("asset:///sample_reels_video.mp4")
        } else {
            Uri.parse(clean)
        }
    }

    val exoPlayer = remember(targetVideoUri) {
        if (isVideo) {
            val httpFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (Linux; Android) ArekSurabaya/1.0")
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(10000)
                .setReadTimeoutMs(10000)
            val dataSourceFactory = DefaultDataSource.Factory(context, httpFactory)
            val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
            ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build().apply {
                    setMediaItem(MediaItem.fromUri(targetVideoUri))
                    repeatMode = Player.REPEAT_MODE_ALL
                    volume = if (isGlobalMuted) 0f else 1f
                    playWhenReady = isCurrentPage && !isUserPaused
                    prepare()
                }
        } else {
            null
        }
    }

    // Auto-play when swiped into view, auto-pause when swiped away!
    LaunchedEffect(isCurrentPage, isUserPaused, isVideo, exoPlayer) {
        if (isVideo && exoPlayer != null) {
            if (isCurrentPage && !isUserPaused) {
                exoPlayer.play()
            } else {
                exoPlayer.pause()
                if (!isCurrentPage) {
                    exoPlayer.seekTo(0)
                    isUserPaused = false
                }
            }
        }
    }

    // Update volume when global mute changes
    LaunchedEffect(isGlobalMuted) {
        exoPlayer?.volume = if (isGlobalMuted) 0f else 1f
    }

    DisposableEffect(exoPlayer) {
        if (exoPlayer == null) {
            onDispose {}
        } else {
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    isBuffering = state == Player.STATE_BUFFERING
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.w("InstagramReelPageItem", "Playback error for $targetVideoUri: ${error.message}, falling back to bundled asset")
                    runCatching {
                        exoPlayer.setMediaItem(MediaItem.fromUri("asset:///sample_reels_video.mp4"))
                        exoPlayer.prepare()
                        exoPlayer.play()
                    }
                }
            }
            exoPlayer.addListener(listener)

            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> exoPlayer.playWhenReady = false
                    Lifecycle.Event.ON_RESUME -> {
                        if (isCurrentPage && !isUserPaused) {
                            exoPlayer.playWhenReady = true
                        }
                    }
                    Lifecycle.Event.ON_DESTROY -> exoPlayer.release()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                exoPlayer.removeListener(listener)
                exoPlayer.release()
            }
        }
    }

    // Progress updates
    LaunchedEffect(isCurrentPage, isVideo, isUserPaused) {
        while (isCurrentPage && isVideo && exoPlayer != null && !isUserPaused) {
            val duration = exoPlayer.duration
            val current = exoPlayer.currentPosition
            if (duration > 0) {
                playbackProgress = (current.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
            }
            delay(200)
        }
    }

    fun triggerPlayPause() {
        if (isVideo && exoPlayer != null) {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                isUserPaused = true
            } else {
                exoPlayer.play()
                isUserPaused = false
            }
            showPlayPauseIndicator = true
            coroutineScope.launch {
                delay(750)
                showPlayPauseIndicator = false
            }
        }
    }

    fun triggerDoubleTapLike() {
        if (!isLiked) {
            onToggleLike()
        }
        coroutineScope.launch {
            isHeartVisible = true
            heartScale.snapTo(0.2f)
            heartScale.animateTo(
                targetValue = 1.3f,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
            )
            heartScale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 150),
            )
            delay(450)
            isHeartVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { triggerPlayPause() },
                    onDoubleTap = { triggerDoubleTapLike() },
                )
            },
    ) {
        // 1. FULL-SCREEN MEDIA BACKDROP (Always present: Photo / Thumbnail / Poster)
        // Guarantees zero black screen while video buffers or loads!
        val posterModel = remember(moment) {
            moment.allPhotos.firstOrNull()?.ifBlank { null }
                ?: resolveImageDrawable(moment.imageKey)
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(posterModel)
                .crossfade(true)
                .build(),
            contentDescription = moment.caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // 1b. Full-Screen Video (ExoPlayer) OR Photo Carousel
        if (isVideo && exoPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                    }
                },
                update = { playerView ->
                    if (playerView.player != exoPlayer) {
                        playerView.player = exoPlayer
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        } else if (!isVideo && moment.allPhotos.size > 1) {
            // Photo Carousel for multi-photo moments
            val photos = moment.allPhotos
            val photoPagerState = rememberPagerState(pageCount = { photos.size })
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = photoPagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { pIdx ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photos[pIdx])
                            .crossfade(true)
                            .build(),
                        contentDescription = moment.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                // Photo Indicator Dots
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 64.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    repeat(photos.size) { dotIdx ->
                        Box(
                            modifier = Modifier
                                .size(if (photoPagerState.currentPage == dotIdx) 7.dp else 5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (photoPagerState.currentPage == dotIdx) Color.White else Color.White.copy(alpha = 0.4f),
                                ),
                        )
                    }
                }
            }
        }

        // 2. Subtle Top & Bottom Gradient Shadows (for perfect text readability)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f)),
                    ),
                ),
        )

        // 3. Center Play/Pause Animated Indicator
        AnimatedVisibility(
            visible = showPlayPauseIndicator || (isVideo && isUserPaused),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isUserPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp),
                )
            }
        }

        // 4. Buffering Spinner
        if (isBuffering && isVideo) {
            CircularProgressIndicator(
                color = ToskaPrimaryDeep,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(46.dp)
                    .align(Alignment.Center),
            )
        }

        // 5. Double-Tap Animated Pop Heart
        if (isHeartVisible) {
            Box(
                modifier = Modifier.align(Alignment.Center),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = AlertCrimson,
                    modifier = Modifier
                        .size(96.dp)
                        .scale(heartScale.value),
                )
            }
        }

        // 6. RIGHT SIDE ACTION BAR (Instagram Style: Like, Comment, Repost, Send, Options)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Heart Like
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onToggleLike,
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Suka",
                        tint = if (isLiked) AlertCrimson else Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Text(
                    text = "${moment.likesCount}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.shadow(4.dp),
                )
            }

            // Comment
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onOpenComments,
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChatBubbleOutline,
                        contentDescription = "Komentar",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Text(
                    text = "${moment.commentCount}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.shadow(4.dp),
                )
            }

            // Repost / Share Curved Arrows (matching Instagram)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { shareMomentReel(context, moment) },
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Repeat,
                        contentDescription = "Bagikan",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Text(
                    text = "${(moment.likesCount / 4).coerceAtLeast(1)}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.shadow(4.dp),
                )
            }

            // Direct Share / Send
            IconButton(
                onClick = { shareMomentReel(context, moment) },
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Bagikan",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }

            // Save / Bookmark
            IconButton(
                onClick = { isSaved = !isSaved },
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "Simpan",
                    tint = if (isSaved) Color(0xFFFFD54F) else Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }

            // 3-Dots More Options (Report / Block)
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Opsi",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp),
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Laporkan Konten", color = AlertCrimson) },
                        leadingIcon = {
                            Icon(Icons.Filled.ReportProblem, contentDescription = null, tint = AlertCrimson)
                        },
                        onClick = {
                            menuExpanded = false
                            onReport()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Blokir Akun Kreator") },
                        leadingIcon = {
                            Icon(Icons.Filled.Security, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onBlockAuthor()
                        },
                    )
                }
            }
        }

        // 7. BOTTOM-RIGHT CORNER: Floating Mute Toggle (matching Instagram in screenshot!)
        if (isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 10.dp),
            ) {
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape),
                ) {
                    Icon(
                        imageVector = if (isGlobalMuted) Icons.Filled.VolumeMute else Icons.Filled.VolumeUp,
                        contentDescription = "Suara",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
        }

        // 8. BOTTOM-LEFT OVERLAY (Author, Caption, Music Ticker, District)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 14.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            // Author row: Avatar + Name + Verified Badge + "Profil" Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenAuthorProfile)
                    .padding(vertical = 2.dp),
            ) {
                // Circular Avatar with Story Gradient Ring
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
                                ),
                            ),
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
                            fontSize = 14.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = moment.authorName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .shadow(4.dp),
                )

                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = "Verified",
                    tint = Color(0xFF29B6F6),
                    modifier = Modifier.size(15.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color.White.copy(alpha = 0.4f)),
                ) {
                    Text(
                        text = "Profil",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    )
                }
            }

            // Location Tag
            if (moment.locationName.isNotBlank() || moment.district.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.45f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "📍 ${moment.district}${if (moment.locationName.isNotBlank()) " • ${moment.locationName}" else ""}",
                            color = Color(0xFFFFD54F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            // Caption Text (Expandable with "... selengkapnya" matching Instagram!)
            if (moment.caption.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .clickable { isCaptionExpanded = !isCaptionExpanded }
                        .padding(top = 1.dp),
                ) {
                    Text(
                        text = moment.caption,
                        color = Color.White,
                        fontSize = 13.sp,
                        maxLines = if (isCaptionExpanded) 8 else 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp,
                        modifier = Modifier.shadow(4.dp),
                    )
                    if (moment.caption.length > 50) {
                        Text(
                            text = if (isCaptionExpanded) "lebih sedikit" else "... selengkapnya",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 1.dp),
                        )
                    }
                }
            }

            // Music / Audio Tag (matching screenshot: "🎵 reels.surabaya • Audio asli")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(13.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${moment.authorName.lowercase().replace(" ", ".")} • Audio asli Surabaya",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .shadow(4.dp),
                )
            }
        }

        // 9. Playback Progress Indicator at Very Bottom Edge
        if (isVideo) {
            LinearProgressIndicator(
                progress = { playbackProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .align(Alignment.BottomCenter),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.25f),
            )
        }
    }
}
