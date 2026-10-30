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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRoom
import com.example.data.model.CulinaryReview
import com.example.data.model.CulinarySpot
import com.example.data.model.IncidentComment
import com.example.data.model.IncidentReport
import com.example.data.model.SurabayaEvent
import com.example.data.util.formatOperatingHours
import com.example.data.util.formatRelativeTimestamp
import com.example.data.util.isCulinaryOpenNow
import com.example.ui.components.CulinaryDeliveryLinksRow
import com.example.ui.components.CulinaryPhotoCarousel
import com.example.ui.components.FullscreenZoomablePhotoViewerDialog
import com.example.ui.components.SurabayaRemoteOrFallbackImage
import com.example.ui.components.openCulinaryMapsDirection
import com.example.ui.components.openGoogleMapsLocation
import com.example.ui.screens.formatCulinaryCategoryLabel
import com.example.ui.screens.formatEventCategoryLabel
import com.example.ui.screens.formatIncidentCategoryLabel
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
    onVerify: () -> Unit,
    onBookmark: () -> Unit,
    onSendComment: (String) -> Unit,
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
                    SurabayaRemoteOrFallbackImage(
                        imageUrl = report.photoUrl,
                        fallbackImageKey = report.imageKey,
                        contentDescription = report.title,
                        contentScale = ContentScale.Crop,
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
                                onClick = { openGoogleMapsLocation(context, report.lat, report.lng, report.title) },
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
    onToggleInterest: () -> Unit,
    onToggleBookmark: () -> Unit,
    onModerate: (approve: Boolean, note: String) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var adminNoteInput by remember(event.id) { mutableStateOf(event.adminNote) }
    var isDescriptionExpanded by remember(event.id) { mutableStateOf(false) }
    var hasDescriptionOverflow by remember(event.id, event.description) { mutableStateOf(false) }

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
                    SurabayaRemoteOrFallbackImage(
                        imageUrl = event.photoUrl,
                        fallbackImageKey = event.imageKey,
                        contentDescription = event.title,
                        contentScale = ContentScale.Crop,
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
                                onClick = { openGoogleMapsLocation(context, event.lat, event.lng, event.venueName) },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
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
                            shape = CircleShape,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Filled.Favorite, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isInterested) "Ikut Hadir (${event.interestedCount})" else "Tertarik (${event.interestedCount})")
                        }
                        OutlinedButton(onClick = onToggleBookmark, shape = CircleShape) {
                            Icon(if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(if (isSaved) "Tersimpan" else "Simpan")
                        }
                    }
                }

                if (isAdmin && event.status != "APPROVED") {
                    HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.8f))
                    Text(
                        text = "Panel Persetujuan Admin Surabaya",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
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
    onBookmark: () -> Unit,
    onSubmitReview: (rating: Int, comment: String) -> Unit,
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
                                onClick = { openCulinaryMapsDirection(context, spot) },
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
                            modifier = Modifier.fillMaxWidth(),
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
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val displayTitle = room.displayTitleFor(currentUserId)

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
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleLarge,
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
