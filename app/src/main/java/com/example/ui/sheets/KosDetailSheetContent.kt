package com.example.ui.sheets

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.KosComment
import com.example.data.model.KosKontrakItem
import com.example.data.util.SafeLinkCategory
import com.example.data.util.formatRelativeTimestamp
import com.example.ui.components.openExternalUrlSafely
import com.example.ui.screens.kosPropertyTypeIcon
import com.example.ui.theme.AlertContainerLight
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.AlertOnContainerLight
import com.example.ui.theme.ClosedGrayText
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep

fun openKosWhatsApp(context: Context, item: KosKontrakItem) {
    val digits = item.whatsappNumber.filter { it.isDigit() }
    if (digits.isBlank()) return
    val normalized = when {
        digits.startsWith("0") -> "62${digits.drop(1)}"
        digits.startsWith("62") -> digits
        else -> "62$digits"
    }
    val msg = buildString {
        append("Assalamualaikum / Halo ${item.ownerName.ifBlank { "Bapak/Ibu Pemilik Kos" }}, ")
        append("saya melihat info di Arek Surabaya:\n")
        append("• Hunian: ${item.title} (${item.propertyTypeLabel})\n")
        append("• Harga Sewa: ${item.priceWithPeriod}\n")
        append("• Lokasi: Kec. ${item.district}")
        if (item.address.isNotBlank()) append(" (${item.address})")
        append("\n\nApakah masih ada kamar/unit kosong dan kapan saya boleh izin survei lokasi langsung?")
    }
    val waUri = Uri.parse("https://wa.me/$normalized?text=${Uri.encode(msg)}")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, waUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
        // Ignore on headless device
    }
}

fun openKosMapsOrAddressSearch(context: Context, item: KosKontrakItem) {
    if (item.hasCustomMapsLink) {
        if (openExternalUrlSafely(context, item.safeMapsUrl, SafeLinkCategory.GOOGLE_MAPS)) {
            return
        }
    }
    val query = buildString {
        if (item.address.isNotBlank()) append("${item.address}, ")
        append("Kec. ${item.district}, Surabaya")
    }
    val searchUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, searchUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
        // Ignore on headless device
    }
}

@Composable
fun KosDetailSheetContent(
    item: KosKontrakItem,
    comments: List<KosComment>,
    isSaved: Boolean,
    currentUserId: String,
    isAdmin: Boolean,
    onToggleFull: () -> Unit,
    onChatOwner: () -> Unit,
    onToggleBookmark: () -> Unit,
    onSendComment: (String) -> Unit,
    onReportContent: () -> Unit,
    onReportLink: () -> Unit,
    onDelete: (() -> Unit)?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isOwner = item.isOwnedBy(currentUserId)
    val canManageStatus = isOwner || isAdmin
    var commentDraft by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .imePadding()
            .navigationBarsPadding()
            .testTag("kos_detail_sheet_content"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    color = ToskaPrimary.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = kosPropertyTypeIcon(item.propertyType),
                            contentDescription = null,
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = item.propertyTypeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ToskaPrimaryDeep,
                        )
                    }
                }
                Surface(
                    color = if (item.isFull) AlertCrimson else OpenGreenBg,
                    shape = CircleShape,
                ) {
                    Text(
                        text = item.availabilityLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (item.isFull) Color.White else OpenGreenText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("kos_detail_close_btn"),
            ) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Tutup")
            }
        }

        if (item.isFull) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AlertContainerLight,
                border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("kos_detail_full_banner"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = AlertCrimson,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Kamar kos / kontrakan ini sedang ditandai PENUH / TERSEWA oleh pemilik.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = AlertOnContainerLight,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (item.allPhotos.isNotEmpty() && !item.isContentBlurred) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(item.allPhotos) { photoUrl ->
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(268.dp)
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = item.priceWithPeriod,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (item.isFull) ClosedGrayText else ToskaPrimaryDeep,
                textDecoration = if (item.isFull) TextDecoration.LineThrough else TextDecoration.None,
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFEEF9F3),
            ) {
                Text(
                    text = item.billingBadgeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ToskaPrimaryDeep,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Location & Optional Google Maps Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6FBF9)),
            border = BorderStroke(1.dp, OutlineVariantLight),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(18.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kecamatan ${item.district}, Surabaya",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = item.address.ifBlank { "Hubungi pemilik untuk patokan detail lokasi" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { openKosMapsOrAddressSearch(context, item) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ToskaPrimary.copy(alpha = 0.14f),
                        contentColor = ToskaPrimaryDeep,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("kos_open_maps_btn"),
                ) {
                    Icon(imageVector = Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (item.hasCustomMapsLink) {
                            "Buka Titik Google Maps Pemilik"
                        } else {
                            "Cari Alamat di Google Maps"
                        },
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (item.hasAnyExternalLink && !isOwner) {
                    TextButton(
                        onClick = onReportLink,
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("kos_report_link_btn"),
                    ) {
                        Icon(imageVector = Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Laporkan Link Peta Mencurigakan", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        if (item.facilities.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Fasilitas Tersedia",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item.facilities.chunked(3).forEach { rowFacilities ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        rowFacilities.forEach { fac ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEEF9F3),
                                border = BorderStroke(1.dp, OutlineVariantLight),
                            ) {
                                Text(
                                    text = fac,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ToskaPrimaryDeep,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Keterangan & Peraturan",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Direct Contact Row (Chat Pemilik & Hubungi WA)
        if (!isOwner && !item.isFull) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onChatOwner,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ToskaPrimaryDeep,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("kos_chat_owner_btn"),
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Outlined.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat Pemilik", fontWeight = FontWeight.Bold)
                }

                if (item.whatsappNumber.isNotBlank()) {
                    Button(
                        onClick = { openKosWhatsApp(context, item) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ToskaPrimary,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("kos_wa_owner_btn"),
                    ) {
                        Icon(imageVector = Icons.Filled.PhoneInTalk, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hubungi WA", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (canManageStatus) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onToggleFull,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.isFull) ToskaPrimaryDeep else AlertCrimson,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("kos_detail_toggle_full_btn"),
                ) {
                    Icon(
                        imageVector = if (item.isFull) Icons.Filled.Refresh else Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (item.isFull) {
                            stringResource(R.string.kos_reopen_btn)
                        } else {
                            stringResource(R.string.kos_mark_full_btn)
                        },
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (onDelete != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, AlertCrimson),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("kos_detail_delete_btn"),
                    ) {
                        Icon(imageVector = Icons.Filled.DeleteOutline, contentDescription = "Hapus", tint = AlertCrimson)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = onToggleBookmark,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("kos_bookmark_btn"),
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isSaved) "Tersimpan" else "Simpan Kos")
            }

            if (!isOwner) {
                TextButton(
                    onClick = onReportContent,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("kos_report_btn"),
                ) {
                    Icon(imageVector = Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Laporkan Iklan")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, OutlineVariantLight),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MintSoftGradient)
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(imageVector = Icons.Filled.Security, contentDescription = null, tint = ToskaPrimaryDeep, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.kos_safety_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = ToskaOnPrimaryContainer,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = OutlineVariantLight)
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "${stringResource(R.string.kos_comments_title)} (${comments.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (comments.isEmpty()) {
            Text(
                text = stringResource(R.string.kos_comments_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                comments.forEach { comment ->
                    val isOwnerReply = item.ownerId.isNotBlank() && comment.authorId == item.ownerId
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isOwnerReply) Color(0xFFEEF9F3) else Color(0xFFF8FAF9),
                        border = BorderStroke(1.dp, OutlineVariantLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("kos_comment_item_${comment.id}"),
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(ToskaPrimary.copy(alpha = 0.16f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = comment.authorName.trim().firstOrNull()?.uppercase() ?: "W",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ToskaPrimaryDeep,
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = comment.authorName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    if (isOwnerReply) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = ToskaPrimaryDeep,
                                            modifier = Modifier.testTag("kos_owner_reply_badge_${comment.id}"),
                                        ) {
                                            Text(
                                                text = "PEMILIK",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            )
                                        }
                                    }
                                    Text(
                                        text = "• ${formatRelativeTimestamp(comment.createdAt)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
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

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = commentDraft,
                onValueChange = { commentDraft = it },
                placeholder = { Text(stringResource(R.string.kos_comment_hint)) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ToskaPrimary),
                modifier = Modifier
                    .weight(1f)
                    .testTag("kos_comment_input"),
            )
            IconButton(
                onClick = {
                    val trimmed = commentDraft.trim()
                    if (trimmed.length >= 2) {
                        onSendComment(trimmed)
                        commentDraft = ""
                    }
                },
                enabled = commentDraft.trim().length >= 2,
                modifier = Modifier
                    .background(
                        if (commentDraft.trim().length >= 2) ToskaPrimary else Color(0xFFE0E6E3),
                        CircleShape,
                    )
                    .minimumInteractiveComponentSize()
                    .testTag("kos_send_comment_btn"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Kirim pertanyaan",
                    tint = Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}
