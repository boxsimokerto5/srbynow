package com.example.ui.sheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppNotification
import com.example.data.model.AppNotificationType
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRoom
import com.example.data.model.CulinaryReview
import com.example.data.model.CulinarySpot
import com.example.data.model.DestinationReview
import com.example.data.model.IncidentComment
import com.example.data.model.IncidentReport
import com.example.data.model.MomentComment
import com.example.data.model.SurabayaDestination
import com.example.data.model.SurabayaEvent
import com.example.data.model.UserMoment
import com.example.data.util.PendingExternalLink
import com.example.data.util.SafeLinkCategory
import com.example.data.util.formatOperatingHours
import com.example.data.util.formatRelativeTimestamp
import com.example.data.util.isCulinaryOpenNow
import com.example.ui.components.AdminRawLinkPreviewBadge
import com.example.ui.components.CulinaryDeliveryLinksRow
import com.example.ui.components.CulinaryPhotoCarousel
import com.example.ui.components.FullscreenZoomablePhotoViewerDialog
import com.example.ui.components.PostContentSafetyRow
import com.example.ui.components.PostExternalLinkSecurityStrip
import com.example.ui.components.SafeExternalLinkConfirmationDialog
import com.example.ui.components.SurabayaRemoteOrFallbackImage
import com.example.ui.components.openCulinaryMapsDirection
import com.example.ui.components.openEventMapsDirection
import com.example.ui.components.openGoogleMapsLocation
import com.example.ui.components.openExternalUrlSafely
import com.example.ui.components.openIncidentMapsDirection
import com.example.ui.screens.formatCulinaryCategoryLabel
import com.example.ui.screens.formatEventCategoryLabel
import com.example.ui.screens.formatIncidentCategoryLabel
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.FreshGreenGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.PendingAmberBg
import com.example.ui.theme.PendingAmberText
import com.example.ui.theme.SoftChatBubbleGradient
import com.example.ui.theme.StarGold
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimaryDeep
import java.util.Locale

@Composable
fun IncidentDetailSheetContent(
    report: IncidentReport,
    comments: List<IncidentComment>,
    isSaved: Boolean,
    isVerifiedByMe: Boolean,
    currentUserId: String = "",
    isAdmin: Boolean = false,
    onVerify: () -> Unit,
    onBookmark: () -> Unit,
    onSendComment: (String) -> Unit,
    onReportLink: () -> Unit = {},
    onAdminClearLinks: () -> Unit = {},
    onReportContent: () -> Unit = {},
    onBlockAuthor: (() -> Unit)? = null,
    onAdminDeletePost: (() -> Unit)? = null,
    onAdminRestoreBlur: (() -> Unit)? = null,
    onAdminBanAuthor: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var commentInput by remember(report.id) { mutableStateOf("") }
    var isDescriptionExpanded by remember(report.id) { mutableStateOf(false) }
    var hasDescriptionOverflow by remember(report.id, report.description) { mutableStateOf(false) }
    var pendingScrollToBottom by remember(report.id) { mutableStateOf(false) }
    var previousCommentCount by remember(report.id) { mutableIntStateOf(comments.size) }
    var fullscreenInitialPage by remember(report.id) { mutableStateOf<Int?>(null) }
    var pendingExternalLink by remember(report.id) { mutableStateOf<PendingExternalLink?>(null) }

    val activePendingLink = pendingExternalLink
    if (activePendingLink != null) {
        SafeExternalLinkConfirmationDialog(
            pendingLink = activePendingLink,
            isReportedByMe = currentUserId in report.linkReportedByUids,
            onReportSuspicious = onReportLink,
            onDismiss = { pendingExternalLink = null },
            onConfirmOpen = {
                val target = activePendingLink
                pendingExternalLink = null
                openExternalUrlSafely(context, target.url, SafeLinkCategory.GOOGLE_MAPS)
            },
        )
    }

    val activeFullscreenPage = fullscreenInitialPage
    if (activeFullscreenPage != null) {
        FullscreenZoomablePhotoViewerDialog(
            photoUrls = report.allPhotos,
            fallbackImageKey = report.imageKey,
            title = report.title,
            initialPage = activeFullscreenPage,
            onDismiss = { fullscreenInitialPage = null },
        )
    }

    val commentListState = rememberLazyListState()

    // Gaya 2: Slimmer base photo height (140.dp) that smoothly shrinks to 84.dp as comments scroll
    val maxImageHeight = 140.dp
    val minImageHeight = 84.dp
    val maxCollapsePx = with(density) { (maxImageHeight - minImageHeight).toPx() }
    var collapseProgressPx by remember(report.id) { mutableFloatStateOf(0f) }

    LaunchedEffect(report.id, comments.size) {
        if (comments.isNotEmpty() && (pendingScrollToBottom || (comments.size > previousCommentCount && previousCommentCount > 0))) {
            commentListState.animateScrollToItem(comments.lastIndex)
            pendingScrollToBottom = false
        }
        previousCommentCount = comments.size
    }

    val commentNestedScrollConnection = remember(report.id, commentListState, maxCollapsePx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0f && (commentListState.canScrollForward || collapseProgressPx < maxCollapsePx)) {
                    val previous = collapseProgressPx
                    val updated = (previous - available.y).coerceIn(0f, maxCollapsePx)
                    val consumedY = previous - updated
                    if (consumedY != 0f) {
                        collapseProgressPx = updated
                        return Offset(0f, consumedY)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available.y > 0f && collapseProgressPx > 0f) {
                    val updated = (collapseProgressPx - available.y).coerceIn(0f, maxCollapsePx)
                    collapseProgressPx = updated
                }
                // Consume remaining vertical scroll so swiping comments up/down never drags the sheet closed
                return Offset(0f, available.y)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return Velocity(0f, available.y)
            }
        }
    }

    val submitCommentAction = {
        val trimmed = commentInput.trim()
        if (trimmed.isNotEmpty()) {
            pendingScrollToBottom = true
            onSendComment(trimmed)
            commentInput = ""
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    val isImeVisible = WindowInsets.ime.getBottom(density) > 0

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        val hasBoundedHeight = maxHeight != Dp.Unspecified && maxHeight < 2000.dp
        val availableHeight = if (hasBoundedHeight) maxHeight else 680.dp
        val isKeyboardOpen = isImeVisible || (hasBoundedHeight && maxHeight < 500.dp)
        val sheetMaxHeight = if (isKeyboardOpen) {
            availableHeight
        } else {
            (availableHeight * 0.90f).coerceIn(420.dp, 760.dp)
        }

        val currentImageHeight = (maxImageHeight - with(density) { collapseProgressPx.toDp() })
            .coerceIn(minImageHeight, maxImageHeight)
        val collapseFraction = if (maxCollapsePx > 0f) (collapseProgressPx / maxCollapsePx).coerceIn(0f, 1f) else 0f
        val imageCornerRadius = 20.dp - (4.dp * collapseFraction)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = sheetMaxHeight)
                .padding(horizontal = 18.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // ================= ZONA 1: ATAS (TETAP DI POSISI, FOTO ADAPTIF GAYA 2) =================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(if (isKeyboardOpen) 6.dp else 10.dp),
            ) {
                AnimatedVisibility(
                    visible = !isKeyboardOpen,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    CulinaryPhotoCarousel(
                        photoUrls = report.allPhotos,
                        fallbackImageKey = report.imageKey,
                        spotName = report.title,
                        compactMode = false,
                        isBlurredSensitive = report.isContentBlurred,
                        sensitiveReportCount = report.contentReportCount,
                        onPhotoClick = { pageIdx -> fullscreenInitialPage = pageIdx },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(currentImageHeight)
                            .clip(RoundedCornerShape(imageCornerRadius))
                            .testTag("sheet_incident_image"),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            text = formatIncidentCategoryLabel(report.category),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(
                            text = "Dilaporkan ${report.authorName} • ${formatRelativeTimestamp(report.createdAt)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }

                Text(
                    text = report.title,
                    style = if (isKeyboardOpen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (isKeyboardOpen) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                )

                AnimatedVisibility(
                    visible = !isKeyboardOpen,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = "${report.address}, Kec. ${report.district}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                FilledTonalButton(
                                    onClick = {
                                        openIncidentMapsDirection(context, report) { pendingExternalLink = it }
                                    },
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("sheet_incident_maps_btn"),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Directions,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Maps", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        PostExternalLinkSecurityStrip(
                            hasActiveExternalLinks = report.safeMapsUrl.isNotBlank(),
                            isQuarantined = report.isLinkQuarantined,
                            linkReportCount = report.linkReportCount,
                            isReportedByMe = currentUserId in report.linkReportedByUids,
                            isAdmin = isAdmin,
                            onReportLink = onReportLink,
                            onAdminClearLinks = onAdminClearLinks,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = report.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = when {
                            isKeyboardOpen -> 1
                            isDescriptionExpanded -> 6
                            else -> 2
                        },
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { layoutResult ->
                            if (!isDescriptionExpanded && !isKeyboardOpen) {
                                hasDescriptionOverflow = layoutResult.hasVisualOverflow
                            }
                        },
                    )
                    if (!isKeyboardOpen && (hasDescriptionOverflow || isDescriptionExpanded)) {
                        Text(
                            text = stringResource(
                                if (isDescriptionExpanded) R.string.incident_desc_show_less else R.string.incident_desc_read_more,
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                                .padding(vertical = 2.dp),
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !isKeyboardOpen,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = onVerify,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isVerifiedByMe) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sheet_verify_incident_btn"),
                        ) {
                            Icon(imageVector = Icons.Filled.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isVerifiedByMe) "Tervalidasi (${report.verifiedCount})" else "Validasi (${report.verifiedCount})",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }

                        OutlinedButton(
                            onClick = onBookmark,
                            shape = CircleShape,
                            modifier = Modifier.testTag("sheet_bookmark_incident_btn"),
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(if (isSaved) "Tersimpan" else "Simpan")
                        }
                    }
                }

                HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.8f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.incident_comments_title, comments.size),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (comments.size >= 2) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                        ) {
                            Text(
                                text = stringResource(R.string.incident_comments_scroll_hint),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }

            // ================= ZONA 2: TENGAH (SCROLL KOMENTAR MANDIRI NAIK-TURUN) =================
            val shouldFillCommentArea = !isKeyboardOpen && (comments.size >= 3 || collapseProgressPx > 0.5f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = shouldFillCommentArea)
                    .heightIn(min = if (comments.isEmpty()) 52.dp else 68.dp)
                    .nestedScroll(commentNestedScrollConnection),
            ) {
                LazyColumn(
                    state = commentListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("incident_comments_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp),
                ) {
                    item(key = "incident_safety_controls") {
                        PostContentSafetyRow(
                            authorId = report.authorId,
                            authorName = report.authorName,
                            currentUserId = currentUserId,
                            contentReportCount = report.contentReportCount,
                            isContentReportedByMe = currentUserId in report.contentReportedByUids,
                            isAdmin = isAdmin,
                            onReportContent = onReportContent,
                            onBlockAuthor = onBlockAuthor,
                            onAdminDeletePost = onAdminDeletePost,
                            onAdminRestoreBlur = onAdminRestoreBlur,
                            onAdminBanAuthor = onAdminBanAuthor,
                        )
                    }
                    if (comments.isEmpty()) {
                        item(key = "empty_comments_placeholder") {
                            Text(
                                text = stringResource(R.string.incident_comments_empty),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 6.dp),
                            )
                        }
                    } else {
                        items(
                            items = comments,
                            key = { cmt -> cmt.id.ifBlank { "${cmt.authorName}_${cmt.createdAt}_${cmt.text.hashCode()}" } },
                        ) { cmt ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(0.5.dp, OutlineVariantLight.copy(alpha = 0.65f)),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = cmt.authorName,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = formatRelativeTimestamp(cmt.createdAt),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Text(
                                        text = cmt.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ================= ZONA 3: BAWAH (KOLOM INPUT TETAP MENEMPEL DI BAWAH) =================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.6f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.incident_comment_input_hint),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        shape = CircleShape,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { submitCommentAction() }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("incident_comment_input"),
                    )
                    IconButton(
                        onClick = submitCommentAction,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("incident_comment_send_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.incident_comment_send_cd),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventDetailSheetContent(
    event: SurabayaEvent,
    isAdmin: Boolean,
    isSaved: Boolean,
    isInterested: Boolean,
    isInterestPending: Boolean = false,
    currentUserId: String = "",
    onToggleInterest: () -> Unit,
    onToggleBookmark: () -> Unit,
    onModerate: (approve: Boolean, note: String) -> Unit,
    onReportLink: () -> Unit = {},
    onAdminClearLinks: () -> Unit = {},
    onReportContent: () -> Unit = {},
    onBlockAuthor: (() -> Unit)? = null,
    onAdminDeletePost: (() -> Unit)? = null,
    onAdminRestoreBlur: (() -> Unit)? = null,
    onAdminBanAuthor: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var adminNoteInput by remember(event.id) { mutableStateOf(event.adminNote) }
    var isDescriptionExpanded by remember(event.id) { mutableStateOf(false) }
    var hasDescriptionOverflow by remember(event.id, event.description) { mutableStateOf(false) }
    var fullscreenInitialPage by remember(event.id) { mutableStateOf<Int?>(null) }
    var pendingExternalLink by remember(event.id) { mutableStateOf<PendingExternalLink?>(null) }

    val activePendingLink = pendingExternalLink
    if (activePendingLink != null) {
        SafeExternalLinkConfirmationDialog(
            pendingLink = activePendingLink,
            isReportedByMe = currentUserId in event.linkReportedByUids,
            onReportSuspicious = onReportLink,
            onDismiss = { pendingExternalLink = null },
            onConfirmOpen = {
                val target = activePendingLink
                pendingExternalLink = null
                openExternalUrlSafely(context, target.url, SafeLinkCategory.GOOGLE_MAPS)
            },
        )
    }

    val activeFullscreenPage = fullscreenInitialPage
    if (activeFullscreenPage != null) {
        FullscreenZoomablePhotoViewerDialog(
            photoUrls = event.allPhotos,
            fallbackImageKey = event.imageKey,
            title = event.title,
            initialPage = activeFullscreenPage,
            onDismiss = { fullscreenInitialPage = null },
        )
    }

    val scrollState = rememberScrollState()

    // Gaya 2: Base photo height (140.dp) that smoothly shrinks to 84.dp as content scrolls
    val maxImageHeight = 140.dp
    val minImageHeight = 84.dp
    val maxCollapsePx = with(density) { (maxImageHeight - minImageHeight).toPx() }
    var collapseProgressPx by remember(event.id) { mutableFloatStateOf(0f) }

    val eventNestedScrollConnection = remember(event.id, scrollState, maxCollapsePx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0f && (scrollState.canScrollForward || collapseProgressPx < maxCollapsePx)) {
                    val previous = collapseProgressPx
                    val updated = (previous - available.y).coerceIn(0f, maxCollapsePx)
                    val consumedY = previous - updated
                    if (consumedY != 0f) {
                        collapseProgressPx = updated
                        return Offset(0f, consumedY)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available.y > 0f && collapseProgressPx > 0f) {
                    val updated = (collapseProgressPx - available.y).coerceIn(0f, maxCollapsePx)
                    collapseProgressPx = updated
                }
                return Offset(0f, available.y)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return Velocity(0f, available.y)
            }
        }
    }

    val isImeVisible = WindowInsets.ime.getBottom(density) > 0

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        val hasBoundedHeight = maxHeight != Dp.Unspecified && maxHeight < 2000.dp
        val availableHeight = if (hasBoundedHeight) maxHeight else 680.dp
        val isKeyboardOpen = isImeVisible || (hasBoundedHeight && maxHeight < 500.dp)
        val sheetMaxHeight = if (isKeyboardOpen) {
            availableHeight
        } else {
            (availableHeight * 0.90f).coerceIn(420.dp, 760.dp)
        }

        val currentImageHeight = (maxImageHeight - with(density) { collapseProgressPx.toDp() })
            .coerceIn(minImageHeight, maxImageHeight)
        val collapseFraction = if (maxCollapsePx > 0f) (collapseProgressPx / maxCollapsePx).coerceIn(0f, 1f) else 0f
        val imageCornerRadius = 20.dp - (4.dp * collapseFraction)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = sheetMaxHeight)
                .padding(horizontal = 18.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // ================= ZONA 1: ATAS (TETAP DI POSISI, FOTO ADAPTIF GAYA 2) =================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(if (isKeyboardOpen) 6.dp else 10.dp),
            ) {
                AnimatedVisibility(
                    visible = !isKeyboardOpen,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    CulinaryPhotoCarousel(
                        photoUrls = event.allPhotos,
                        fallbackImageKey = event.imageKey,
                        spotName = event.title,
                        compactMode = false,
                        isBlurredSensitive = event.isContentBlurred,
                        sensitiveReportCount = event.contentReportCount,
                        onPhotoClick = { pageIdx -> fullscreenInitialPage = pageIdx },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(currentImageHeight)
                            .clip(RoundedCornerShape(imageCornerRadius))
                            .testTag("sheet_event_image"),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            text = formatEventCategoryLabel(event.category),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                    val (statusLabel, bg, fg) = when (event.status) {
                        "APPROVED" -> Triple("Disetujui Admin", OpenGreenBg, OpenGreenText)
                        "REJECTED" -> Triple("Ditolak", Color(0xFFFFDAD6), Color(0xFF93000A))
                        else -> Triple("Menunggu Persetujuan Admin", PendingAmberBg, PendingAmberText)
                    }
                    Surface(shape = CircleShape, color = bg) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = fg,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }

                Text(
                    text = event.title,
                    style = if (isKeyboardOpen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (isKeyboardOpen) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // ================= ZONA 2: TENGAH (SCROLL DETAIL, VENUE, & ADMIN PANEL) =================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .nestedScroll(eventNestedScrollConnection),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .testTag("event_details_scroll"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(Icons.Filled.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text("${event.eventDateText} • ${event.timeText}", style = MaterialTheme.typography.labelLarge)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(event.venueName, style = MaterialTheme.typography.titleSmall)
                                Text("${event.address}, Kec. ${event.district}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledTonalButton(
                                onClick = {
                                    openEventMapsDirection(context, event) { pendingExternalLink = it }
                                },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("sheet_event_maps_btn"),
                            ) {
                                Icon(Icons.Filled.Directions, null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Maps", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Text(
                            text = "Tiket: ${event.priceLabel} • Pengusul: ${event.proposerName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                PostExternalLinkSecurityStrip(
                    hasActiveExternalLinks = event.safeMapsUrl.isNotBlank(),
                    isQuarantined = event.isLinkQuarantined,
                    linkReportCount = event.linkReportCount,
                    isReportedByMe = currentUserId in event.linkReportedByUids,
                    isAdmin = isAdmin,
                    onReportLink = onReportLink,
                    onAdminClearLinks = onAdminClearLinks,
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = when {
                            isKeyboardOpen -> 2
                            isDescriptionExpanded -> 12
                            else -> 3
                        },
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { layoutResult ->
                            if (!isDescriptionExpanded && !isKeyboardOpen) {
                                hasDescriptionOverflow = layoutResult.hasVisualOverflow
                            }
                        },
                    )
                    if (!isKeyboardOpen && (hasDescriptionOverflow || isDescriptionExpanded)) {
                        Text(
                            text = stringResource(
                                if (isDescriptionExpanded) R.string.incident_desc_show_less else R.string.incident_desc_read_more,
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                                .padding(vertical = 2.dp),
                        )
                    }
                }

                if (event.status == "APPROVED") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = onToggleInterest,
                            enabled = !isInterestPending,
                            shape = CircleShape,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Filled.Favorite, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isInterestPending) "Menyimpan…"
                                else if (isInterested) "Ikut Hadir (${event.interestedCount})"
                                else "Tertarik (${event.interestedCount})"
                            )
                        }
                        OutlinedButton(onClick = onToggleBookmark, shape = CircleShape) {
                            Icon(if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(if (isSaved) "Tersimpan" else "Simpan")
                        }
                    }
                }

                PostContentSafetyRow(
                    authorId = event.proposerId,
                    authorName = event.proposerName,
                    currentUserId = currentUserId,
                    contentReportCount = event.contentReportCount,
                    isContentReportedByMe = currentUserId in event.contentReportedByUids,
                    isAdmin = isAdmin,
                    onReportContent = onReportContent,
                    onBlockAuthor = onBlockAuthor,
                    onAdminDeletePost = onAdminDeletePost,
                    onAdminRestoreBlur = onAdminRestoreBlur,
                    onAdminBanAuthor = onAdminBanAuthor,
                )

                if (isAdmin && event.status != "APPROVED") {
                    HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.8f))
                    Text(
                        text = "Panel Persetujuan Admin Surabaya",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    AdminRawLinkPreviewBadge(
                        rawUrl = event.safeMapsUrl,
                        reportCount = event.linkReportCount,
                        onClearLink = onAdminClearLinks,
                    )
                    OutlinedTextField(
                        value = adminNoteInput,
                        onValueChange = { adminNoteInput = it },
                        label = { Text("Catatan Admin (opsional)") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("event_admin_note_input"),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                onModerate(false, adminNoteInput.ifBlank { "Belum memenuhi kriteria jadwal kota." })
                            },
                            shape = CircleShape,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sheet_reject_event_btn"),
                        ) {
                            Text(stringResource(R.string.btn_reject_event))
                        }
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                onModerate(true, adminNoteInput.ifBlank { "Disetujui tayang oleh Admin Surabaya." })
                            },
                            shape = CircleShape,
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("sheet_approve_event_btn"),
                        ) {
                            Text(stringResource(R.string.btn_approve_event))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
}

@Composable
fun CulinaryDetailSheetContent(
    spot: CulinarySpot,
    reviews: List<CulinaryReview>,
    isSaved: Boolean,
    currentUserId: String = "",
    isAdmin: Boolean = false,
    onBookmark: () -> Unit,
    onSubmitReview: (rating: Int, comment: String) -> Unit,
    onReportLink: () -> Unit = {},
    onAdminClearLinks: () -> Unit = {},
    onReportContent: () -> Unit = {},
    onBlockAuthor: (() -> Unit)? = null,
    onAdminDeletePost: (() -> Unit)? = null,
    onAdminRestoreBlur: (() -> Unit)? = null,
    onAdminBanAuthor: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val isOpen = isCulinaryOpenNow(spot.openHour, spot.closeHour)
    var selectedStars by remember(spot.id) { mutableIntStateOf(5) }
    var reviewText by remember(spot.id) { mutableStateOf("") }
    var isDescriptionExpanded by remember(spot.id) { mutableStateOf(false) }
    var hasDescriptionOverflow by remember(spot.id, spot.description) { mutableStateOf(false) }
    var pendingScrollToBottom by remember(spot.id) { mutableStateOf(false) }
    var previousReviewCount by remember(spot.id) { mutableIntStateOf(reviews.size) }
    var fullscreenInitialPage by remember(spot.id) { mutableStateOf<Int?>(null) }
    var pendingExternalLink by remember(spot.id) { mutableStateOf<PendingExternalLink?>(null) }

    val activePendingLink = pendingExternalLink
    if (activePendingLink != null) {
        SafeExternalLinkConfirmationDialog(
            pendingLink = activePendingLink,
            isReportedByMe = currentUserId in spot.linkReportedByUids,
            onReportSuspicious = onReportLink,
            onDismiss = { pendingExternalLink = null },
            onConfirmOpen = {
                val target = activePendingLink
                pendingExternalLink = null
                openExternalUrlSafely(context, target.url, SafeLinkCategory.GOOGLE_MAPS)
            },
        )
    }

    val reviewListState = rememberLazyListState()

    // Gaya 2: Base photo height (140.dp) that smoothly shrinks to 84.dp as reviews scroll
    val maxImageHeight = 140.dp
    val minImageHeight = 84.dp
    val maxCollapsePx = with(density) { (maxImageHeight - minImageHeight).toPx() }
    var collapseProgressPx by remember(spot.id) { mutableFloatStateOf(0f) }

    LaunchedEffect(spot.id, reviews.size) {
        if (reviews.isNotEmpty() && (pendingScrollToBottom || (reviews.size > previousReviewCount && previousReviewCount > 0))) {
            reviewListState.animateScrollToItem(reviews.lastIndex)
            pendingScrollToBottom = false
        }
        previousReviewCount = reviews.size
    }

    val culinaryNestedScrollConnection = remember(spot.id, reviewListState, maxCollapsePx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0f && (reviewListState.canScrollForward || collapseProgressPx < maxCollapsePx)) {
                    val previous = collapseProgressPx
                    val updated = (previous - available.y).coerceIn(0f, maxCollapsePx)
                    val consumedY = previous - updated
                    if (consumedY != 0f) {
                        collapseProgressPx = updated
                        return Offset(0f, consumedY)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available.y > 0f && collapseProgressPx > 0f) {
                    val updated = (collapseProgressPx - available.y).coerceIn(0f, maxCollapsePx)
                    collapseProgressPx = updated
                }
                // Consume remaining vertical scroll so swiping reviews up/down never accidentally drags the sheet closed
                return Offset(0f, available.y)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return Velocity(0f, available.y)
            }
        }
    }

    val submitReviewAction = {
        val trimmed = reviewText.trim()
        if (trimmed.length >= 2) {
            pendingScrollToBottom = true
            onSubmitReview(selectedStars, trimmed)
            reviewText = ""
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    val isImeVisible = WindowInsets.ime.getBottom(density) > 0

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        val hasBoundedHeight = maxHeight != Dp.Unspecified && maxHeight < 2000.dp
        val availableHeight = if (hasBoundedHeight) maxHeight else 680.dp
        val isKeyboardOpen = isImeVisible || (hasBoundedHeight && maxHeight < 500.dp)
        val sheetMaxHeight = if (isKeyboardOpen) {
            availableHeight
        } else {
            (availableHeight * 0.90f).coerceIn(420.dp, 760.dp)
        }

        val currentImageHeight = (maxImageHeight - with(density) { collapseProgressPx.toDp() })
            .coerceIn(minImageHeight, maxImageHeight)
        val collapseFraction = if (maxCollapsePx > 0f) (collapseProgressPx / maxCollapsePx).coerceIn(0f, 1f) else 0f
        val imageCornerRadius = 20.dp - (4.dp * collapseFraction)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = sheetMaxHeight)
                .padding(horizontal = 18.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // ================= ZONA 1: ATAS (TETAP DI POSISI, FOTO ADAPTIF GAYA 2) =================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(if (isKeyboardOpen) 6.dp else 10.dp),
            ) {
                AnimatedVisibility(
                    visible = !isKeyboardOpen,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    CulinaryPhotoCarousel(
                        photoUrls = spot.allPhotos,
                        fallbackImageKey = spot.imageKey,
                        spotName = spot.name,
                        compactMode = false,
                        isBlurredSensitive = spot.isContentBlurred,
                        sensitiveReportCount = spot.contentReportCount,
                        onPhotoClick = { clickedIdx -> fullscreenInitialPage = clickedIdx },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(currentImageHeight)
                            .clip(RoundedCornerShape(imageCornerRadius))
                            .testTag("sheet_kuliner_image"),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = CircleShape, color = if (isOpen) OpenGreenBg else Color(0xFFECEFF1)) {
                            Text(
                                text = if (isOpen) "Sedang Buka" else "Sedang Tutup",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isOpen) OpenGreenText else Color(0xFF546E7A),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text(
                                text = formatCulinaryCategoryLabel(spot.category),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, null, tint = StarGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${String.format(Locale.US, "%.1f", spot.ratingAvg)} (${spot.reviewCount} ulasan)",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                Text(
                    text = spot.name,
                    style = if (isKeyboardOpen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (isKeyboardOpen) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                )

                AnimatedVisibility(
                    visible = !isKeyboardOpen,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "${spot.priceRange} • Jam Buka ${formatOperatingHours(spot.openHour, spot.closeHour)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        if (spot.address.isNotBlank() || spot.district.isNotBlank()) {
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
                                    text = listOfNotNull(
                                        spot.address.takeIf { it.isNotBlank() },
                                        spot.district.takeIf { it.isNotBlank() }?.let { "Kec. $it" },
                                    ).joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = {
                                    openCulinaryMapsDirection(context, spot) { pendingExternalLink = it }
                                },
                                shape = CircleShape,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sheet_kuliner_maps_btn"),
                            ) {
                                Icon(Icons.Filled.Directions, null, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.btn_open_maps))
                            }
                            OutlinedButton(
                                onClick = onBookmark,
                                shape = CircleShape,
                                modifier = Modifier.testTag("sheet_kuliner_bookmark_btn"),
                            ) {
                                Icon(if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, null, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(if (isSaved) "Tersimpan" else "Simpan")
                            }
                        }

                        CulinaryDeliveryLinksRow(
                            spot = spot,
                            compact = false,
                            isReportedByMe = currentUserId in spot.linkReportedByUids,
                            onReportSuspiciousLink = onReportLink,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        PostExternalLinkSecurityStrip(
                            hasActiveExternalLinks = spot.safeMapsUrl.isNotBlank() ||
                                spot.safeGofoodUrl.isNotBlank() ||
                                spot.safeGrabfoodUrl.isNotBlank() ||
                                spot.safeShopeefoodUrl.isNotBlank(),
                            isQuarantined = spot.isLinkQuarantined,
                            linkReportCount = spot.linkReportCount,
                            isReportedByMe = currentUserId in spot.linkReportedByUids,
                            isAdmin = isAdmin,
                            onReportLink = onReportLink,
                            onAdminClearLinks = onAdminClearLinks,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = spot.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = when {
                            isKeyboardOpen -> 1
                            isDescriptionExpanded -> 6
                            else -> 2
                        },
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { layoutResult ->
                            if (!isDescriptionExpanded && !isKeyboardOpen) {
                                hasDescriptionOverflow = layoutResult.hasVisualOverflow
                            }
                        },
                    )
                    if (!isKeyboardOpen && (hasDescriptionOverflow || isDescriptionExpanded)) {
                        Text(
                            text = stringResource(
                                if (isDescriptionExpanded) R.string.incident_desc_show_less else R.string.incident_desc_read_more,
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                                .padding(vertical = 2.dp),
                        )
                    }
                }

                HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.8f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.kuliner_reviews_title, reviews.size),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (reviews.size >= 2) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                        ) {
                            Text(
                                text = stringResource(R.string.kuliner_reviews_scroll_hint),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }

            // ================= ZONA 2: TENGAH (SCROLL ULASAN WARGA MANDIRI NAIK-TURUN) =================
            val shouldFillReviewArea = !isKeyboardOpen && (reviews.size >= 2 || collapseProgressPx > 0.5f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = shouldFillReviewArea)
                    .heightIn(min = if (reviews.isEmpty()) 52.dp else 68.dp)
                    .nestedScroll(culinaryNestedScrollConnection),
            ) {
                LazyColumn(
                    state = reviewListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("kuliner_reviews_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp),
                ) {
                    item(key = "culinary_safety_controls") {
                        PostContentSafetyRow(
                            authorId = spot.createdBy,
                            authorName = spot.authorName,
                            currentUserId = currentUserId,
                            contentReportCount = spot.contentReportCount,
                            isContentReportedByMe = currentUserId in spot.contentReportedByUids,
                            isAdmin = isAdmin,
                            onReportContent = onReportContent,
                            onBlockAuthor = onBlockAuthor,
                            onAdminDeletePost = onAdminDeletePost,
                            onAdminRestoreBlur = onAdminRestoreBlur,
                            onAdminBanAuthor = onAdminBanAuthor,
                        )
                    }
                    if (reviews.isEmpty()) {
                        item(key = "empty_reviews_placeholder") {
                            Text(
                                text = stringResource(R.string.kuliner_reviews_empty),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 6.dp),
                            )
                        }
                    } else {
                        items(
                            items = reviews,
                            key = { rev -> rev.id.ifBlank { "${rev.authorName}_${rev.createdAt}_${rev.comment.hashCode()}" } },
                        ) { rev ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(0.5.dp, OutlineVariantLight.copy(alpha = 0.65f)),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = rev.authorName,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = "★ ${rev.rating}/5",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = StarGold,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                    Text(
                                        text = rev.comment,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ================= ZONA 3: BAWAH (INTERACTIVE CITIZEN RATING & REVIEW TETAP MENEMPEL) =================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.6f))
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Beri Rating & Ulasan", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Row {
                                for (star in 1..5) {
                                    Icon(
                                        imageVector = if (star <= selectedStars) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                        contentDescription = "Bintang $star",
                                        tint = StarGold,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clickable { selectedStars = star },
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = reviewText,
                                onValueChange = { reviewText = it },
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.kuliner_review_input_hint),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                },
                                shape = CircleShape,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = { submitReviewAction() }),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("kuliner_review_input"),
                            )
                            Button(
                                onClick = submitReviewAction,
                                shape = CircleShape,
                                modifier = Modifier.testTag("kuliner_review_submit_btn"),
                            ) {
                                Text(stringResource(R.string.kuliner_review_send_label))
                            }
                        }
                    }
                }
            }
        }
    }

    val openPage = fullscreenInitialPage
    if (openPage != null) {
        FullscreenZoomablePhotoViewerDialog(
            photoUrls = spot.allPhotos,
            fallbackImageKey = spot.imageKey,
            title = spot.name,
            initialPage = openPage,
            onDismiss = { fullscreenInitialPage = null },
        )
    }
}

@Composable
fun ChatConversationSheetContent(
    room: ChatRoom,
    messages: List<ChatMessage>,
    currentUserId: String,
    onSendMessage: (String) -> Unit,
    onBlockPeer: (() -> Unit)? = null,
) {
    var inputText by remember { mutableStateOf("") }
    var showBlockPeerDialog by remember(room.id) { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val displayTitle = room.displayTitleFor(currentUserId)

    if (showBlockPeerDialog && onBlockPeer != null) {
        AlertDialog(
            onDismissRequest = { showBlockPeerDialog = false },
            title = { Text(stringResource(R.string.dialog_block_user_title, displayTitle)) },
            text = { Text(stringResource(R.string.dialog_block_user_body, displayTitle)) },
            confirmButton = {
                Button(
                    onClick = {
                        showBlockPeerDialog = false
                        onBlockPeer()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCrimson),
                    modifier = Modifier.testTag("confirm_chat_block_peer_btn"),
                ) {
                    Text(stringResource(R.string.btn_confirm_block_user))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockPeerDialog = false }) {
                    Text(stringResource(R.string.btn_cancel_action))
                }
            },
        )
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = OpenGreenBg,
                    ) {
                        Text(
                            text = "Sinkron 2 Arah",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OpenGreenText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                    Text(
                        text = "Room ID: ${room.roomKey.ifBlank { room.id }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("chat_sheet_room_id"),
                    )
                }
            }

            if (room.roomType == "DIRECT" && onBlockPeer != null) {
                OutlinedButton(
                    onClick = { showBlockPeerDialog = true },
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("chat_block_peer_btn"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Block,
                        contentDescription = null,
                        tint = AlertCrimson,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.btn_block_chat_peer),
                        style = MaterialTheme.typography.labelMedium,
                        color = AlertCrimson,
                    )
                }
            }
        }

        HorizontalDivider()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp, max = 380.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 6.dp),
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Mulai sapaan pertama dengan $displayTitle! 👋",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(messages, key = { it.id }) { msg ->
                    val isMine = msg.senderId == currentUserId
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
                    ) {
                        if (!isMine) {
                            Text(
                                text = msg.senderName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            )
                        }
                        val bubbleShape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isMine) 18.dp else 4.dp,
                            bottomEnd = if (isMine) 4.dp else 18.dp,
                        )
                        if (isMine) {
                            Box(
                                modifier = Modifier
                                    .clip(bubbleShape)
                                    .background(SoftChatBubbleGradient, bubbleShape)
                                    .border(1.dp, OutlineVariantLight, bubbleShape),
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ToskaOnPrimaryContainer,
                                    )
                                    Text(
                                        text = formatRelativeTimestamp(msg.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ToskaPrimaryDeep,
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = bubbleShape,
                                color = Color(0xFFF6FBF8),
                                border = BorderStroke(1.dp, OutlineVariantLight),
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = formatRelativeTimestamp(msg.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.chat_input_hint),
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                shape = CircleShape,
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_message_input"),
            )
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(FreshGreenGradient, CircleShape)
                    .testTag("chat_message_send_button"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Kirim Pesan",
                    tint = Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun MomentDetailSheetContent(
    moment: UserMoment,
    comments: List<MomentComment>,
    currentUserId: String,
    isAdmin: Boolean,
    onToggleLike: () -> Unit,
    onAddComment: (String) -> Unit,
    onReport: () -> Unit,
    onBlockAuthor: () -> Unit,
    onAdminDelete: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var commentInput by remember { mutableStateOf("") }
    var viewingPhotoIndex by remember { mutableStateOf<Int?>(null) }
    val isLiked = currentUserId in moment.likedByUids
    val photos = moment.allPhotos

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        // Sheet Drag Handle & Close
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 36.dp, height = 4.dp)
                .clip(CircleShape)
                .background(OutlineVariantLight),
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Author row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
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
                            )
                            Text(
                                text = "${moment.district}${if (moment.locationName.isNotBlank()) " • ${moment.locationName}" else ""} • ${formatRelativeTimestamp(moment.timestamp)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onReport) {
                            Icon(
                                imageVector = Icons.Filled.Block,
                                contentDescription = "Lapor",
                                tint = AlertCrimson,
                            )
                        }
                        if (isAdmin && onAdminDelete != null) {
                            IconButton(onClick = onAdminDelete) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Hapus Admin",
                                    tint = AlertCrimson,
                                )
                            }
                        }
                    }
                }
            }

            // Photos
            item {
                if (photos.isNotEmpty()) {
                    CulinaryPhotoCarousel(
                        photoUrls = photos,
                        fallbackImageKey = moment.imageKey,
                        spotName = moment.caption.ifBlank { "Momen Arek Surabaya" },
                        isBlurredSensitive = moment.isContentBlurred,
                        sensitiveReportCount = moment.contentReportCount,
                        onPhotoClick = { viewingPhotoIndex = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    )
                } else if (moment.imageKey.isNotBlank()) {
                    SurabayaRemoteOrFallbackImage(
                        imageUrl = "",
                        fallbackImageKey = moment.imageKey,
                        contentDescription = moment.caption,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewingPhotoIndex = 0 },
                    )
                }
            }

            // Caption
            if (moment.caption.isNotBlank()) {
                item {
                    Text(
                        text = moment.caption,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Likes row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onToggleLike) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.Favorite,
                            contentDescription = "Suka",
                            tint = if (isLiked) AlertCrimson else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "${moment.likesCount} warga menyukai ini",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            item {
                HorizontalDivider(color = OutlineVariantLight)
            }

            // Comments header
            item {
                Text(
                    text = "Komentar Warga (${comments.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ToskaPrimaryDeep,
                )
            }

            if (comments.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada komentar. Jadilah yang pertama menyapa!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            } else {
                items(comments, key = { it.id }) { comment ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = comment.authorName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = formatRelativeTimestamp(comment.timestamp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = comment.comment,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }

        // Comment Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = commentInput,
                onValueChange = { commentInput = it },
                placeholder = {
                    Text(
                        text = "Tulis tanggapan atau sapa...",
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                shape = CircleShape,
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("moment_comment_input"),
            )
            IconButton(
                onClick = {
                    if (commentInput.isNotBlank()) {
                        onAddComment(commentInput)
                        commentInput = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(FreshGreenGradient, CircleShape)
                    .testTag("moment_comment_send_btn"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Kirim",
                    tint = Color.White,
                )
            }
        }
    }

    viewingPhotoIndex?.let { pageIdx ->
        FullscreenZoomablePhotoViewerDialog(
            photoUrls = photos,
            fallbackImageKey = moment.imageKey,
            title = moment.caption.ifBlank { "Momen Arek Surabaya" },
            initialPage = pageIdx,
            onDismiss = { viewingPhotoIndex = null },
        )
    }
}

@Composable
fun DestinationDetailSheetContent(
    destination: SurabayaDestination,
    reviews: List<DestinationReview>,
    isBookmarked: Boolean,
    isAdmin: Boolean,
    onToggleBookmark: () -> Unit,
    onAddReview: (review: String, rating: Int) -> Unit,
    onReport: () -> Unit,
    onAdminDelete: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var reviewInput by remember { mutableStateOf("") }
    var ratingInput by remember { mutableIntStateOf(5) }
    var viewingPhotoIndex by remember { mutableStateOf<Int?>(null) }
    val photos = destination.allPhotos

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 36.dp, height = 4.dp)
                .clip(CircleShape)
                .background(OutlineVariantLight),
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header Image / Carousel
            item {
                if (photos.isNotEmpty()) {
                    CulinaryPhotoCarousel(
                        photoUrls = photos,
                        fallbackImageKey = destination.imageKey,
                        spotName = destination.title,
                        isBlurredSensitive = destination.isContentBlurred,
                        sensitiveReportCount = destination.contentReportCount,
                        onPhotoClick = { viewingPhotoIndex = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    )
                } else if (destination.imageKey.isNotBlank()) {
                    SurabayaRemoteOrFallbackImage(
                        imageUrl = "",
                        fallbackImageKey = destination.imageKey,
                        contentDescription = destination.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewingPhotoIndex = 0 },
                    )
                }
            }

            // Title & Bookmark
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ToskaPrimaryDeep,
                        ) {
                            Text(
                                text = destination.category,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = destination.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = ToskaPrimaryDeep,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${destination.district} • ${destination.address}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onToggleBookmark) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Simpan",
                                tint = ToskaPrimaryDeep,
                            )
                        }
                        if (isAdmin && onAdminDelete != null) {
                            IconButton(onClick = onAdminDelete) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Hapus Destinasi",
                                    tint = AlertCrimson,
                                )
                            }
                        }
                    }
                }
            }

            // Key info pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = OpenGreenBg,
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "Tiket Masuk", style = MaterialTheme.typography.labelSmall, color = OpenGreenText)
                            Text(text = destination.ticketPrice, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = OpenGreenText)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ToskaPrimaryDeep.copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "Jam Buka", style = MaterialTheme.typography.labelSmall, color = ToskaPrimaryDeep)
                            Text(text = destination.operationalHours, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ToskaPrimaryDeep)
                        }
                    }
                }
            }

            // Description
            item {
                Column {
                    Text(
                        text = "Tentang Destinasi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ToskaPrimaryDeep,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = destination.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (destination.bestTimeToVisit.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "💡 Waktu Kunjungan Terbaik: ${destination.bestTimeToVisit}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Google Maps Navigation Button
            item {
                FilledTonalButton(
                    onClick = {
                        openGoogleMapsLocation(
                            context = context,
                            lat = destination.lat,
                            lng = destination.lng,
                            label = destination.title,
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ToskaPrimaryDeep,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dest_btn_maps_nav"),
                ) {
                    Icon(imageVector = Icons.Filled.Directions, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Buka Rute di Google Maps", fontWeight = FontWeight.Bold)
                }
            }

            item {
                HorizontalDivider(color = OutlineVariantLight)
            }

            // Reviews & Rating Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Ulasan & Penilaian Warga",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ToskaPrimaryDeep,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f (%d ulasan)", destination.rating, destination.reviewsCount),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // Add Review Input Box
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Beri Bintang & Ulasan:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            for (star in 1..5) {
                                IconButton(
                                    onClick = { ratingInput = star },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        imageVector = if (star <= ratingInput) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                        contentDescription = "$star Bintang",
                                        tint = if (star <= ratingInput) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = reviewInput,
                            onValueChange = { reviewInput = it },
                            placeholder = { Text("Ceritakan pengalaman berkunjung Anda...") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dest_review_input"),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FilledTonalButton(
                            onClick = {
                                if (reviewInput.isNotBlank()) {
                                    onAddReview(reviewInput, ratingInput)
                                    reviewInput = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text("Kirim Ulasan")
                        }
                    }
                }
            }

            if (reviews.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada ulasan dari warga. Berikan penilaian pertama Anda!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(reviews, key = { it.id }) { rev ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = rev.authorName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    for (i in 1..rev.rating) {
                                        Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = rev.review, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatRelativeTimestamp(rev.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    viewingPhotoIndex?.let { pageIdx ->
        FullscreenZoomablePhotoViewerDialog(
            photoUrls = photos,
            fallbackImageKey = destination.imageKey,
            title = destination.title,
            initialPage = pageIdx,
            onDismiss = { viewingPhotoIndex = null },
        )
    }
}

private enum class NotifFilter(val label: String) {
    ALL("Semua"),
    CHAT("Pesan Chat"),
    EVENT("Event Baru"),
    INCIDENT("Kejadian Kritis"),
}

@Composable
fun NotificationsSheetContent(
    notifications: List<AppNotification>,
    onOpenNotification: (AppNotification) -> Unit,
    onMarkAllRead: () -> Unit,
    onTestNotification: (AppNotificationType) -> Unit,
    onClose: () -> Unit,
) {
    var selectedFilter by remember { mutableStateOf(NotifFilter.ALL) }

    val filteredNotifications = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            NotifFilter.ALL -> notifications
            NotifFilter.CHAT -> notifications.filter { it.type == AppNotificationType.CHAT }
            NotifFilter.EVENT -> notifications.filter { it.type == AppNotificationType.EVENT }
            NotifFilter.INCIDENT -> notifications.filter { it.type == AppNotificationType.INCIDENT }
        }
    }

    val unreadCount = remember(notifications) { notifications.count { !it.isRead } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 680.dp)
            .padding(horizontal = 18.dp, vertical = 6.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // --- Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(com.example.ui.theme.MintSoftGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = null,
                        tint = com.example.ui.theme.ToskaPrimaryDeep,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.notifications_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (unreadCount > 0) {
                        Text(
                            text = "$unreadCount belum dibaca",
                            style = MaterialTheme.typography.labelSmall,
                            color = com.example.ui.theme.ToskaPrimaryDeep,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            if (unreadCount > 0) {
                TextButton(
                    onClick = onMarkAllRead,
                    modifier = Modifier.testTag("btn_mark_all_read"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.DoneAll,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = com.example.ui.theme.ToskaPrimaryDeep,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.btn_mark_all_read),
                        style = MaterialTheme.typography.labelSmall,
                        color = com.example.ui.theme.ToskaPrimaryDeep,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // --- Filter Chips ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NotifFilter.values().forEach { filter ->
                val isSelected = selectedFilter == filter
                val countForFilter = remember(notifications, filter) {
                    when (filter) {
                        NotifFilter.ALL -> notifications.size
                        NotifFilter.CHAT -> notifications.count { it.type == AppNotificationType.CHAT }
                        NotifFilter.EVENT -> notifications.count { it.type == AppNotificationType.EVENT }
                        NotifFilter.INCIDENT -> notifications.count { it.type == AppNotificationType.INCIDENT }
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) com.example.ui.theme.ToskaPrimaryDeep else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) com.example.ui.theme.ToskaPrimaryDeep else com.example.ui.theme.OutlineVariantLight,
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedFilter = filter },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = filter.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        )
                        if (countForFilter > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color.White.copy(alpha = 0.25f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 1.dp),
                            ) {
                                Text(
                                    text = countForFilter.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Notifications List ---
        if (filteredNotifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp),
                    )
                    Text(
                        text = stringResource(R.string.notifications_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("notifications_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(
                    items = filteredNotifications,
                    key = { it.id.ifBlank { "${it.title}_${it.timestamp}" } },
                ) { notif ->
                    val (iconVector, iconBg, iconTint, typeTag) = when (notif.type) {
                        AppNotificationType.CHAT -> Quadruple(
                            Icons.Filled.ChatBubble,
                            com.example.ui.theme.MintSoftGradient,
                            com.example.ui.theme.ToskaPrimaryDeep,
                            "Chat",
                        )
                        AppNotificationType.EVENT -> Quadruple(
                            Icons.Filled.CalendarMonth,
                            androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFFECB3), Color(0xFFFFE082))),
                            Color(0xFFE65100),
                            "Event",
                        )
                        AppNotificationType.INCIDENT -> Quadruple(
                            Icons.Filled.WarningAmber,
                            androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2))),
                            com.example.ui.theme.AlertCrimson,
                            "Kejadian",
                        )
                        AppNotificationType.SYSTEM -> Quadruple(
                            Icons.Filled.Notifications,
                            androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))),
                            Color(0xFF0284C7),
                            "Info",
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (notif.isRead) Color.White else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (notif.isRead) com.example.ui.theme.OutlineVariantLight else com.example.ui.theme.ToskaPrimaryDeep.copy(alpha = 0.35f),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onOpenNotification(notif) },
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(iconBg),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = iconTint.copy(alpha = 0.12f),
                                    ) {
                                        Text(
                                            text = typeTag,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = iconTint,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        )
                                    }
                                    Text(
                                        text = formatRelativeTimestamp(notif.timestamp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                Text(
                                    text = notif.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (notif.isRead) FontWeight.Medium else FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )

                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            if (!notif.isRead) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(com.example.ui.theme.AlertCrimson),
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Heads-Up Simulation Panel (Allows testing instant Android top banner) ---
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(0.5.dp, com.example.ui.theme.OutlineVariantLight),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = null,
                        tint = com.example.ui.theme.ToskaPrimaryDeep,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(R.string.btn_test_notification),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = "Ketuk untuk mengirim notifikasi banner melayang (Heads-Up) di atas layar Android:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalButton(
                        onClick = { onTestNotification(AppNotificationType.CHAT) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    ) {
                        Text("💬 Chat", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    FilledTonalButton(
                        onClick = { onTestNotification(AppNotificationType.EVENT) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    ) {
                        Text("🎉 Event", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    FilledTonalButton(
                        onClick = { onTestNotification(AppNotificationType.INCIDENT) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    ) {
                        Text("⚠️ Kejadian", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)


