package com.example.ui.sheets

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.TambalBanComment
import com.example.data.model.TambalBanSpot
import com.example.data.util.LinkSecurityValidator
import com.example.data.util.SafeLinkCategory
import com.example.data.util.formatRelativeTimestamp
import com.example.ui.components.openExternalUrlSafely
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.SoftChatBubbleGradient
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep

@Composable
fun TambalBanDetailSheetContent(
    spot: TambalBanSpot,
    comments: List<TambalBanComment>,
    currentUserId: String = "",
    isAdmin: Boolean = false,
    onVerify: () -> Unit,
    onSendComment: (String) -> Unit,
    onDeleteSpot: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var commentInput by remember(spot.id) { mutableStateOf("") }
    var pendingScrollToBottom by remember(spot.id) { mutableStateOf(false) }
    var previousCommentCount by remember(spot.id) { mutableIntStateOf(comments.size) }

    val commentListState = rememberLazyListState()

    // Smooth collapsible header height (max 120dp down to 0dp when keyboard is active or comments scroll)
    val maxImageHeight = if (spot.photoUrl.isNotBlank()) 130.dp else 0.dp
    val minImageHeight = 0.dp
    val maxCollapsePx = with(density) { (maxImageHeight - minImageHeight).toPx() }
    var collapseProgressPx by remember(spot.id) { mutableFloatStateOf(0f) }

    LaunchedEffect(spot.id, comments.size) {
        if (comments.isNotEmpty() && (pendingScrollToBottom || (comments.size > previousCommentCount && previousCommentCount > 0))) {
            commentListState.animateScrollToItem(0)
            pendingScrollToBottom = false
        }
        previousCommentCount = comments.size
    }

    // Scroll isolation connection: allows comments to scroll smoothly without dismissing bottom sheet
    val commentNestedScrollConnection = remember(spot.id, commentListState, maxCollapsePx) {
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
                return Offset(0f, available.y)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return Velocity(0f, available.y)
            }
        }
    }

    val submitCommentAction = {
        val trimmed = commentInput.trim()
        if (trimmed.length >= 3) {
            pendingScrollToBottom = true
            onSendComment(trimmed)
            commentInput = ""
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    val isImeVisible = WindowInsets.ime.getBottom(density) > 0

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        val hasBoundedHeight = maxHeight != Dp.Unspecified && maxHeight < 2000.dp
        val availableHeight = if (hasBoundedHeight) maxHeight else 680.dp
        val isKeyboardOpen = isImeVisible || (hasBoundedHeight && maxHeight < 500.dp)
        val sheetMaxHeight = if (isKeyboardOpen) availableHeight else (availableHeight * 0.92f).coerceIn(440.dp, 800.dp)

        val currentImageHeight = (maxImageHeight - with(density) { collapseProgressPx.toDp() })
            .coerceIn(minImageHeight, maxImageHeight)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = sheetMaxHeight)
                .padding(horizontal = 18.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // ================= ZONA 1: POSTINGAN ATAS (STICKY / TETAP TERLIHAT) =================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(if (isKeyboardOpen) 4.dp else 8.dp),
            ) {
                // Header bar: Title, Distance badge & Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ToskaPrimary.copy(alpha = 0.12f),
                        ) {
                            Text(
                                text = "Salam Satu Aspal 🛵",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ToskaPrimaryDeep,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }

                        spot.formattedDistance?.let { distStr ->
                            Surface(
                                shape = CircleShape,
                                color = OpenGreenBg,
                            ) {
                                Text(
                                    text = "📍 $distStr",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OpenGreenText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp).testTag("btn_close_tambal_detail"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                // Optional Photo (collapses cleanly when comments scroll)
                if (spot.photoUrl.isNotBlank() && !isKeyboardOpen) {
                    AnimatedVisibility(
                        visible = currentImageHeight > 20.dp,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(spot.photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = spot.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(currentImageHeight)
                                .clip(RoundedCornerShape(14.dp)),
                        )
                    }
                }

                // Spot Name
                Text(
                    text = spot.name,
                    style = if (isKeyboardOpen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (isKeyboardOpen) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                )

                // Address & District Card with Maps direction button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                                tint = ToskaPrimary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "${spot.address}, Kec. ${spot.district}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledTonalButton(
                            onClick = {
                                if (spot.mapsUrl.isNotBlank()) {
                                    openExternalUrlSafely(context, spot.mapsUrl, SafeLinkCategory.GOOGLE_MAPS)
                                } else {
                                    val query = Uri.encode("${spot.name}, ${spot.address}, Kec. ${spot.district}, Surabaya")
                                    val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$query"))
                                    mapsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    try {
                                        context.startActivity(mapsIntent)
                                    } catch (_: Exception) {}
                                }
                            },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_tambal_open_maps"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Directions,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rute Maps", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                // Capability chips row (24h, tubeless, vehicle types, panggilan)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (spot.is24Hours) {
                        Surface(
                            shape = CircleShape,
                            color = OpenGreenBg,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccessTime,
                                    contentDescription = null,
                                    tint = OpenGreenText,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    text = "24 Jam",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OpenGreenText,
                                )
                            }
                        }
                    }

                    if (spot.isTubeless) {
                        Surface(
                            shape = CircleShape,
                            color = ToskaPrimary.copy(alpha = 0.12f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Build,
                                    contentDescription = null,
                                    tint = ToskaPrimaryDeep,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    text = "Tubeless",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ToskaPrimaryDeep,
                                )
                            }
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            text = spot.vehicleSupportText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }

                    if (spot.canCallOut) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF3E8FF),
                        ) {
                            Text(
                                text = "Bisa Panggilan",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6B21A8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }

                // Description (if available)
                if (spot.description.isNotBlank() && !isKeyboardOpen) {
                    Text(
                        text = spot.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Action Bar: Bantu Validasi, Kontak WA/Telepon, Hapus
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val isVerifiedByMe = spot.isVerifiedBy(currentUserId)
                    Button(
                        onClick = onVerify,
                        shape = CircleShape,
                        colors = if (isVerifiedByMe) {
                            ButtonDefaults.buttonColors(
                                containerColor = OpenGreenBg,
                                contentColor = OpenGreenText,
                            )
                        } else {
                            ButtonDefaults.buttonColors(
                                containerColor = ToskaPrimary,
                                contentColor = Color.White,
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_tambal_verify"),
                    ) {
                        Icon(
                            imageVector = if (isVerifiedByMe) Icons.Filled.DoneAll else Icons.Filled.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isVerifiedByMe) {
                                "Tervalidasi (${spot.verifiedCount})"
                            } else {
                                "Bantu Validasi (${spot.verifiedCount})"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    // WhatsApp button
                    if (spot.whatsappNumber.isNotBlank()) {
                        FilledTonalButton(
                            onClick = {
                                val cleanNum = spot.whatsappNumber.replace(Regex("[^0-9]"), "")
                                val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanNum?text=Halo%20dulur,%20apakah%20tambal%20ban%20masih%20buka?"))
                                waIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                try { context.startActivity(waIntent) } catch (_: Exception) {}
                            },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("btn_tambal_wa"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Chat,
                                contentDescription = "WhatsApp",
                                tint = OpenGreenText,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WA", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    // Phone button
                    if (spot.phoneNumber.isNotBlank()) {
                        FilledTonalButton(
                            onClick = {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${spot.phoneNumber}"))
                                dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                try { context.startActivity(dialIntent) } catch (_: Exception) {}
                            },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("btn_tambal_phone"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "Telepon",
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    // Delete button (for author or admin)
                    if (onDeleteSpot != null && (spot.authorId == currentUserId || isAdmin)) {
                        IconButton(
                            onClick = onDeleteSpot,
                            modifier = Modifier.testTag("btn_delete_tambal_ban"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Hapus Tambal Ban",
                                tint = AlertCrimson,
                            )
                        }
                    }
                }

                HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.8f))

                // Comments header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.tambal_ban_comments_title, comments.size),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (comments.size >= 2) {
                        Surface(
                            shape = CircleShape,
                            color = ToskaPrimary.copy(alpha = 0.12f),
                        ) {
                            Text(
                                text = stringResource(R.string.tambal_ban_comments_scroll_hint),
                                style = MaterialTheme.typography.labelSmall,
                                color = ToskaPrimaryDeep,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }

            // ================= ZONA 2: KOMENTAR & PANTAUAN WARGA (SCROLL ISOLATION) =================
            val shouldFillCommentArea = !isKeyboardOpen && (comments.size >= 2 || collapseProgressPx > 0.5f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = shouldFillCommentArea)
                    .heightIn(min = if (comments.isEmpty()) 52.dp else 70.dp)
                    .nestedScroll(commentNestedScrollConnection),
            ) {
                if (comments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Belum ada info kondisi terkini. Tulis pantauan pertama Anda di bawah untuk membantu dulur di jalan!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                } else {
                    LazyColumn(
                        state = commentListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tambal_comments_list"),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp),
                    ) {
                        items(comments, key = { it.id }) { comment ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "${comment.authorName} • Kec. ${comment.authorDistrict}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ToskaPrimaryDeep,
                                        )
                                        Text(
                                            text = formatRelativeTimestamp(comment.createdAt),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Text(
                                        text = comment.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ================= ZONA 3: INPUT KOMENTAR / PANTAUAN DI BAWAH =================
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        placeholder = {
                            Text(
                                stringResource(R.string.tambal_ban_comment_input_hint),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { submitCommentAction() }),
                        singleLine = true,
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_tambal_comment"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ToskaPrimary,
                            unfocusedBorderColor = OutlineVariantLight,
                        ),
                    )

                    val isSendEnabled = commentInput.trim().length >= 3
                    IconButton(
                        onClick = submitCommentAction,
                        enabled = isSendEnabled,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (isSendEnabled) ToskaPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape,
                            )
                            .testTag("btn_send_tambal_comment"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Kirim Pantauan",
                            tint = if (isSendEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}
