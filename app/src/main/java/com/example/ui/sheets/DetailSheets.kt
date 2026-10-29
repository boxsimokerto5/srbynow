package com.example.ui.sheets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.example.data.util.resolveImageDrawable
import com.example.ui.components.openGoogleMapsLocation
import com.example.ui.screens.formatCulinaryCategoryLabel
import com.example.ui.screens.formatEventCategoryLabel
import com.example.ui.screens.formatIncidentCategoryLabel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import com.example.ui.theme.FreshGreenGradient
import com.example.ui.theme.MintSoftGradient
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
    var commentInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(id = resolveImageDrawable(report.imageKey)),
            contentDescription = report.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
                .clip(RoundedCornerShape(20.dp)),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

        Text(
            text = report.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
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

        Text(
            text = report.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Google Maps-style Action Pills Row
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

        HorizontalDivider()

        Text(
            text = "Pantauan & Komentar Warga (${comments.size})",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (comments.isEmpty()) {
            Text(
                text = "Belum ada komentar tambahan. Bagikan pantauan terkini Anda di lokasi ini.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            comments.forEach { cmt ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = cmt.authorName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
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
                        )
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
                value = commentInput,
                onValueChange = { commentInput = it },
                placeholder = { Text("Tulis info situasi terkini…", style = MaterialTheme.typography.bodySmall) },
                shape = CircleShape,
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("incident_comment_input"),
            )
            IconButton(
                onClick = {
                    if (commentInput.isNotBlank()) {
                        onSendComment(commentInput)
                        commentInput = ""
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .testTag("incident_comment_send_btn"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Kirim Komentar",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
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
    var adminNoteInput by remember(event.id) { mutableStateOf(event.adminNote) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(id = resolveImageDrawable(event.imageKey)),
            contentDescription = event.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
                .clip(RoundedCornerShape(20.dp)),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

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

        Text(
            text = event.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

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
            HorizontalDivider()
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
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = { onModerate(false, adminNoteInput.ifBlank { "Belum memenuhi kriteria jadwal kota." }) },
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sheet_reject_event_btn"),
                ) {
                    Text(stringResource(R.string.btn_reject_event))
                }
                Button(
                    onClick = { onModerate(true, adminNoteInput.ifBlank { "Disetujui tayang oleh Admin Surabaya." }) },
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

@Composable
fun CulinaryDetailSheetContent(
    spot: CulinarySpot,
    reviews: List<CulinaryReview>,
    isSaved: Boolean,
    onBookmark: () -> Unit,
    onSubmitReview: (rating: Int, comment: String) -> Unit,
) {
    val context = LocalContext.current
    val isOpen = isCulinaryOpenNow(spot.openHour, spot.closeHour)
    var selectedStars by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(id = resolveImageDrawable(spot.imageKey)),
            contentDescription = spot.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
                .clip(RoundedCornerShape(20.dp)),
        )

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
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            text = "${spot.priceRange} • Jam Buka ${formatOperatingHours(spot.openHour, spot.closeHour)}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = spot.description,
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { openGoogleMapsLocation(context, spot.lat, spot.lng, spot.name) },
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

        HorizontalDivider()

        // Interactive Citizen Rating & Review Box
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Beri Rating & Ulasan Warga", style = MaterialTheme.typography.titleSmall)
                    Row {
                        for (star in 1..5) {
                            Icon(
                                imageVector = if (star <= selectedStars) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Bintang $star",
                                tint = StarGold,
                                modifier = Modifier
                                    .size(26.dp)
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
                        placeholder = { Text("Bagaimana rasa & suasananya?", style = MaterialTheme.typography.bodySmall) },
                        shape = CircleShape,
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kuliner_review_input"),
                    )
                    Button(
                        onClick = {
                            if (reviewText.trim().length >= 2) {
                                onSubmitReview(selectedStars, reviewText)
                                reviewText = ""
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier.testTag("kuliner_review_submit_btn"),
                    ) {
                        Text("Kirim")
                    }
                }
            }
        }

        if (reviews.isNotEmpty()) {
            Text("Ulasan Terbaru (${reviews.size})", style = MaterialTheme.typography.titleSmall)
            reviews.forEach { rev ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(rev.authorName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("★ ${rev.rating}/5", style = MaterialTheme.typography.labelMedium, color = StarGold, fontWeight = FontWeight.Bold)
                        }
                        Text(rev.comment, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
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
