package com.example.ui.sheets

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ZoomIn
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
import com.example.data.model.BarkasComment
import com.example.data.model.BarkasItem
import com.example.data.util.formatRelativeTimestamp
import com.example.ui.components.FullscreenZoomablePhotoViewerDialog
import com.example.ui.components.InitialsAvatar
import com.example.ui.screens.barkasCategoryIcon
import com.example.ui.theme.AlertContainerLight
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.AlertOnContainerLight
import com.example.ui.theme.ClosedGrayBg
import com.example.ui.theme.ClosedGrayText
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.PendingAmberBg
import com.example.ui.theme.PendingAmberText
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep

fun openBarkasWhatsApp(context: Context, item: BarkasItem) {
    val digits = item.whatsappNumber.filter { it.isDigit() }
    if (digits.isBlank()) return
    val normalized = when {
        digits.startsWith("0") -> "62${digits.drop(1)}"
        digits.startsWith("62") -> digits
        else -> "62$digits"
    }
    val msg = buildString {
        append("Halo ${item.sellerName.ifBlank { "Dulur Surabaya" }}, saya melihat iklan di Barkas Warga SBY:\n")
        append("• Barang: ${item.title}\n")
        append("• Harga: ${item.formattedPrice} (${item.negotiableLabel})\n")
        append("• Lokasi COD: Kec. ${item.district}")
        if (item.codLocation.isNotBlank()) append(" (${item.codLocation})")
        append("\n\nApakah barangnya masih tersedia untuk cek & COD di Surabaya?")
    }
    val waUri = Uri.parse("https://wa.me/$normalized?text=${Uri.encode(msg)}")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, waUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
        // Ignore on headless device
    }
}

@Composable
fun BarkasDetailSheetContent(
    item: BarkasItem,
    comments: List<BarkasComment>,
    isSaved: Boolean,
    currentUserId: String,
    isAdmin: Boolean,
    onToggleSold: () -> Unit,
    onChatSeller: () -> Unit,
    onToggleBookmark: () -> Unit,
    onSendComment: (String) -> Unit,
    onReport: () -> Unit,
    onDelete: (() -> Unit)?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isOwner = item.isOwnedBy(currentUserId)
    val canManageStatus = isOwner || isAdmin
    var commentDraft by remember { mutableStateOf("") }
    var fullscreenPhotoInitialIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .imePadding()
            .navigationBarsPadding()
            .testTag("barkas_detail_sheet_content"),
    ) {
        // Top Header Badges & Close
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
                            imageVector = barkasCategoryIcon(item.category),
                            contentDescription = null,
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = item.categoryLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ToskaPrimaryDeep,
                        )
                    }
                }
                Surface(
                    color = if (item.isSold) AlertCrimson else OpenGreenBg,
                    shape = CircleShape,
                ) {
                    Text(
                        text = if (item.isSold) {
                            stringResource(R.string.barkas_sold_badge)
                        } else {
                            stringResource(R.string.barkas_available_badge)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (item.isSold) Color.White else OpenGreenText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("barkas_detail_close_btn"),
            ) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Tutup")
            }
        }

        // Prominent Sold Notice Banner
        if (item.isSold) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AlertContainerLight,
                border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("barkas_detail_sold_banner"),
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
                        text = "Barang ini sudah ditandai TERJUAL oleh penjual.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = AlertOnContainerLight,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Photo Gallery
        if (item.allPhotos.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                itemsIndexed(item.allPhotos) { index, url ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .width(if (item.allPhotos.size == 1) 320.dp else 250.dp)
                            .height(200.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { fullscreenPhotoInitialIndex = index }
                            .testTag("barkas_photo_$index"),
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(url)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "${item.title} Foto ${index + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                            // Zoom hint pill on the bottom-end corner
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.6f),
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
                                        imageVector = Icons.Filled.ZoomIn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        text = "Zoom 2 jari",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Title & Price
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = item.formattedPrice,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (item.isSold) ClosedGrayText else ToskaPrimaryDeep,
                textDecoration = if (item.isSold) TextDecoration.LineThrough else TextDecoration.None,
            )
            Surface(shape = RoundedCornerShape(8.dp), color = PendingAmberBg) {
                Text(
                    text = item.negotiableLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = PendingAmberText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            Surface(shape = RoundedCornerShape(8.dp), color = ClosedGrayBg) {
                Text(
                    text = item.conditionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = ClosedGrayText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // COD & Seller Info Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF5FBF8),
            border = BorderStroke(1.dp, OutlineVariantLight),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f, fill = false),
                    ) {
                        InitialsAvatar(
                            name = item.sellerName.ifBlank { "Warga Surabaya" },
                            colorHex = "#10B981",
                            sizeDp = 38,
                        )
                        Column {
                            Text(
                                text = item.sellerName.ifBlank { "Warga Surabaya" },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Penjual Warga • Dipasang ${formatRelativeTimestamp(item.createdAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (!isOwner && !item.isSold) {
                        FilledTonalButton(
                            onClick = onChatSeller,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = ToskaPrimaryDeep.copy(alpha = 0.12f),
                                contentColor = ToskaPrimaryDeep,
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("barkas_seller_card_chat_btn"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Chat",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.6f))
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(17.dp),
                    )
                    Text(
                        text = buildString {
                            append("Lokasi COD: Kec. ${item.district}, Surabaya")
                            if (item.codLocation.isNotBlank()) {
                                append(" — ${item.codLocation}")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Description
        Text(
            text = "Keterangan & Kondisi Barang",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.description.ifBlank { "Silakan hubungi penjual atau tanyakan detail di kolom komentar." },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Owner / Admin Management Panel
        if (canManageStatus) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, OutlineVariantLight),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .background(MintSoftGradient)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = if (isOwner) "Kelola Lapak Anda" else "Kontrol Moderasi Admin",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ToskaOnPrimaryContainer,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = onToggleSold,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (item.isSold) ToskaPrimaryDeep else AlertCrimson,
                                contentColor = Color.White,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("barkas_detail_toggle_sold_btn"),
                        ) {
                            Icon(
                                imageVector = if (item.isSold) Icons.Filled.Refresh else Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (item.isSold) "Buka Kembali Lapak" else "Tandai Sudah Terjual",
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        if (onDelete != null) {
                            OutlinedButton(
                                onClick = onDelete,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("barkas_detail_delete_btn"),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Hapus Lapak",
                                    tint = AlertCrimson,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Buyer Contact Actions (only when viewer is NOT the seller and item is still available)
        if (!isOwner && !item.isSold) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onChatSeller,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ToskaPrimaryDeep,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("barkas_chat_seller_btn"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat Penjual", fontWeight = FontWeight.Bold)
                }

                if (item.whatsappNumber.isNotBlank()) {
                    FilledTonalButton(
                        onClick = { openBarkasWhatsApp(context, item) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = OpenGreenBg,
                            contentColor = OpenGreenText,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("barkas_wa_seller_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhoneInTalk,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp COD", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        } else if (isOwner && !item.isSold) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ToskaPrimaryDeep.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, ToskaPrimaryDeep.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("barkas_owner_chat_info_banner"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Chat,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "Lapak Anda aktif. Tombol \"💬 Chat Penjual\" dan WhatsApp COD otomatis tampil bagi seluruh calon pembeli yang membuka halaman ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ToskaPrimaryDeep,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Secondary Actions: Bookmark & Report UGC + COD Safety Reminder
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onToggleBookmark,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("barkas_bookmark_btn"),
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = ToskaPrimaryDeep,
                    modifier = Modifier.size(17.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isSaved) "Tersimpan" else "Simpan Favorit")
            }

            if (!isOwner) {
                OutlinedButton(
                    onClick = onReport,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("barkas_report_btn"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = null,
                        tint = AlertCrimson,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Laporkan", color = AlertCrimson)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF6FBF8))
                .padding(10.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = ToskaPrimaryDeep,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(R.string.barkas_cod_safety_notice),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = OutlineVariantLight)
        Spacer(modifier = Modifier.height(12.dp))

        // Comments & Q&A Section
        Text(
            text = stringResource(R.string.barkas_comments_title, comments.size),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (comments.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF8FCFA),
                border = BorderStroke(1.dp, OutlineVariantLight),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.barkas_comments_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp),
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                comments.forEach { comment ->
                    val isSellerReply = item.sellerId.isNotBlank() && comment.authorId == item.sellerId
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSellerReply) Color(0xFFECFCF4) else Color(0xFFF8FCFA),
                        border = BorderStroke(
                            1.dp,
                            if (isSellerReply) ToskaPrimary.copy(alpha = 0.4f) else OutlineVariantLight,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("barkas_comment_row_${comment.id}"),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
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
                                    Text(
                                        text = comment.authorName.ifBlank { "Warga Surabaya" },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    if (isSellerReply) {
                                        Surface(
                                            shape = CircleShape,
                                            color = ToskaPrimaryDeep,
                                            modifier = Modifier.testTag("barkas_seller_reply_badge_${comment.id}"),
                                        ) {
                                            Text(
                                                text = "PENJUAL",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                            )
                                        }
                                    } else if (comment.authorDistrict.isNotBlank()) {
                                        Text(
                                            text = "• ${comment.authorDistrict}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
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
                                lineHeight = 18.sp,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Add Comment Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = commentDraft,
                onValueChange = { commentDraft = it },
                placeholder = { Text(stringResource(R.string.barkas_comment_hint)) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ToskaPrimary),
                modifier = Modifier
                    .weight(1f)
                    .testTag("barkas_comment_input"),
            )
            IconButton(
                onClick = {
                    val trimmed = commentDraft.trim()
                    if (trimmed.isNotEmpty()) {
                        onSendComment(trimmed)
                        commentDraft = ""
                    }
                },
                enabled = commentDraft.trim().isNotEmpty(),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (commentDraft.trim().isNotEmpty()) ToskaPrimary else ClosedGrayBg
                    )
                    .testTag("barkas_send_comment_btn"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Kirim Komentar",
                    tint = if (commentDraft.trim().isNotEmpty()) Color.White else ClosedGrayText,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    fullscreenPhotoInitialIndex?.let { initialIndex ->
        FullscreenZoomablePhotoViewerDialog(
            photoUrls = item.allPhotos,
            fallbackImageKey = item.title,
            title = item.title,
            initialPage = initialIndex,
            onDismiss = { fullscreenPhotoInitialIndex = null },
        )
    }
}
