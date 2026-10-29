package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BookmarkItem
import com.example.data.model.SurabayaEvent
import com.example.data.model.UiState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.components.SurabayaRemoteOrFallbackImage
import com.example.ui.theme.FreshGreenGradient
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.PendingAmberBg
import com.example.ui.theme.PendingAmberText
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimaryDeep

private val eventCategories = listOf(
    "ALL" to "Semua Event",
    "FESTIVAL" to "Festival Kota",
    "MUSIK_SENI" to "Musik & Seni",
    "KULINER_BAZAR" to "Bazar UMKM",
    "KOMUNITAS" to "Komunitas",
    "OLAHRAGA" to "Olahraga",
)

fun formatEventCategoryLabel(key: String): String = when (key) {
    "FESTIVAL" -> "Festival"
    "MUSIK_SENI" -> "Musik & Seni"
    "KULINER_BAZAR" -> "Bazar UMKM"
    "KOMUNITAS" -> "Komunitas"
    "OLAHRAGA" -> "Olahraga"
    else -> "Event"
}

@Composable
fun EventScreen(
    approvedEventsState: UiState<List<SurabayaEvent>>,
    pendingEvents: List<SurabayaEvent>,
    myProposedEvents: List<SurabayaEvent>,
    bookmarks: List<BookmarkItem>,
    isAdmin: Boolean,
    currentUserId: String,
    onSelectEvent: (SurabayaEvent) -> Unit,
    onToggleInterest: (SurabayaEvent) -> Unit,
    onToggleBookmark: (SurabayaEvent) -> Unit,
    onQuickApproveEvent: (SurabayaEvent) -> Unit,
    onQuickRejectEvent: (SurabayaEvent) -> Unit,
    onOpenProposeSheet: () -> Unit,
    onOpenAdminSwitchSheet: () -> Unit,
) {
    // 0 = Agenda Kota (Approved), 1 = Usulan Saya, 2 = Moderasi Admin
    var selectedSubTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedCategory by rememberSaveable { mutableStateOf("ALL") }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(top = 10.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("event_feed_list"),
        ) {
            // Segmented Pill Sub-Tabs (Agenda Kota / Usulan Saya / Moderasi Admin)
            item(key = "event_sub_tabs") {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, OutlineVariantLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        EventSubTabPill(
                            label = "Agenda Kota",
                            selected = selectedSubTab == 0,
                            onClick = { selectedSubTab = 0 },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("event_tab_approved"),
                        )
                        EventSubTabPill(
                            label = "Usulan Saya (${myProposedEvents.size})",
                            selected = selectedSubTab == 1,
                            onClick = { selectedSubTab = 1 },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("event_tab_mine"),
                        )
                        EventSubTabPill(
                            label = if (pendingEvents.isNotEmpty()) "Admin (${pendingEvents.size})" else "Admin",
                            selected = selectedSubTab == 2,
                            onClick = { selectedSubTab = 2 },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("event_tab_admin"),
                        )
                    }
                }
            }

            if (selectedSubTab == 0) {
                item(key = "event_category_row") {
                    CategoryFilterRow(
                        categories = eventCategories,
                        selectedKey = selectedCategory,
                        onSelect = { selectedCategory = it },
                        tagPrefix = "event",
                    )
                }

                when (approvedEventsState) {
                    is UiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(36.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is UiState.Error -> {
                        item {
                            EmptyFeedPlaceholder(
                                icon = Icons.Outlined.EventAvailable,
                                title = "Gagal memuat event Surabaya",
                                subtitle = approvedEventsState.message,
                            )
                        }
                    }
                    is UiState.Success -> {
                        val list = approvedEventsState.data.filter {
                            selectedCategory == "ALL" || it.category == selectedCategory
                        }
                        if (list.isEmpty()) {
                            item {
                                EmptyFeedPlaceholder(
                                    icon = Icons.Outlined.EventAvailable,
                                    title = "Belum ada event tayang di kategori ini",
                                    subtitle = if (pendingEvents.isNotEmpty()) {
                                        "Terdapat ${pendingEvents.size} usulan event warga di tab Admin yang menunggu disetujui."
                                    } else {
                                        "Jadilah yang pertama mengusulkan acara komunitas Surabaya!"
                                    },
                                )
                            }
                        } else {
                            items(list, key = { it.id }) { ev ->
                                val isSaved = bookmarks.any { it.targetId == ev.id && it.targetType == "EVENT" }
                                val isInterested = currentUserId in ev.interestedUids
                                ApprovedEventCard(
                                    event = ev,
                                    isSaved = isSaved,
                                    isInterested = isInterested,
                                    onClick = { onSelectEvent(ev) },
                                    onToggleInterest = { onToggleInterest(ev) },
                                    onToggleBookmark = { onToggleBookmark(ev) },
                                )
                            }
                        }
                    }
                }
            } else if (selectedSubTab == 1) {
                if (myProposedEvents.isEmpty()) {
                    item {
                        EmptyFeedPlaceholder(
                            icon = Icons.Outlined.EventAvailable,
                            title = "Anda belum mengusulkan event",
                            subtitle = "Ketuk tombol Usulkan Event di bawah. Setiap usulan akan ditinjau Admin sebelum tampil publik.",
                        )
                    }
                } else {
                    items(myProposedEvents, key = { it.id }) { ev ->
                        MyProposalEventCard(
                            event = ev,
                            onClick = { onSelectEvent(ev) },
                        )
                    }
                }
            } else {
                // Admin Moderation Queue Tab
                item(key = "admin_status_banner") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (isAdmin) {
                                    Modifier.background(MintSoftGradient, RoundedCornerShape(20.dp))
                                } else {
                                    Modifier.background(PendingAmberBg, RoundedCornerShape(20.dp))
                                },
                            )
                            .border(
                                1.dp,
                                if (isAdmin) OutlineVariantLight else Color(0xFFFDE68A),
                                RoundedCornerShape(20.dp),
                            ),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isAdmin) "Mode Admin Aktif" else "Mode Admin Belum Aktif",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAdmin) ToskaOnPrimaryContainer else PendingAmberText,
                                )
                                Text(
                                    text = if (isAdmin) {
                                        "Anda dapat meninjau, menyetujui, atau menolak usulan event warga Surabaya."
                                    } else {
                                        "Aktifkan Mode Admin pada sheet Profil untuk menyetujui usulan event di bawah."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isAdmin) ToskaOnPrimaryContainer else PendingAmberText,
                                )
                            }
                            if (!isAdmin) {
                                Spacer(modifier = Modifier.width(8.dp))
                                FilledTonalButton(
                                    onClick = onOpenAdminSwitchSheet,
                                    shape = CircleShape,
                                    modifier = Modifier.testTag("enable_admin_shortcut_button"),
                                ) {
                                    Text("Aktifkan", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                if (pendingEvents.isEmpty()) {
                    item {
                        EmptyFeedPlaceholder(
                            icon = Icons.Filled.AdminPanelSettings,
                            title = "Antrean moderasi bersih",
                            subtitle = "Semua usulan event warga sudah selesai ditinjau Admin.",
                        )
                    }
                } else {
                    items(pendingEvents, key = { it.id }) { ev ->
                        PendingModerationCard(
                            event = ev,
                            isAdmin = isAdmin,
                            onClick = { onSelectEvent(ev) },
                            onApprove = { onQuickApproveEvent(ev) },
                            onReject = { onQuickRejectEvent(ev) },
                        )
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onOpenProposeSheet,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            icon = {
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.btn_propose_event),
                    style = MaterialTheme.typography.labelLarge,
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_propose_event"),
        )
    }
}

@Composable
private fun EventSubTabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .then(
                if (selected) {
                    Modifier.background(FreshGreenGradient, CircleShape)
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ApprovedEventCard(
    event: SurabayaEvent,
    isSaved: Boolean,
    isInterested: Boolean,
    onClick: () -> Unit,
    onToggleInterest: () -> Unit,
    onToggleBookmark: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.dp, OutlineVariantLight),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("event_card_${event.id}"),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp),
            ) {
                SurabayaRemoteOrFallbackImage(
                    imageUrl = event.photoUrl,
                    fallbackImageKey = event.imageKey,
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(FreshGreenGradient, CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = formatEventCategoryLabel(event.category),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.94f),
                    ) {
                        Text(
                            text = event.priceLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OpenGreenText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "${event.eventDateText} • ${event.timeText}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "${event.venueName}, ${event.district}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .then(
                                if (isInterested) {
                                    Modifier.background(MintSoftGradient, CircleShape)
                                } else {
                                    Modifier.background(Color(0xFFF1FAF5), CircleShape)
                                },
                            )
                            .border(1.dp, OutlineVariantLight, CircleShape)
                            .clickable(onClick = onToggleInterest)
                            .testTag("interest_event_${event.id}"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = if (isInterested) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = ToskaPrimaryDeep,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "${event.interestedCount} Warga Tertarik",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ToskaOnPrimaryContainer,
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier
                            .size(36.dp)
                            .minimumInteractiveComponentSize(),
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = stringResource(R.string.btn_bookmarks),
                            tint = if (isSaved) ToskaPrimaryDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyProposalEventCard(
    event: SurabayaEvent,
    onClick: () -> Unit,
) {
    val (statusLabel, badgeBg, badgeText) = when (event.status) {
        "APPROVED" -> Triple("Disetujui Admin", OpenGreenBg, OpenGreenText)
        "REJECTED" -> Triple("Ditolak Admin", Color(0xFFFFEFEF), Color(0xFF93000A))
        else -> Triple("Menunggu Persetujuan", PendingAmberBg, PendingAmberText)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, OutlineVariantLight),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (event.photoUrl.isNotBlank()) {
                SurabayaRemoteOrFallbackImage(
                    imageUrl = event.photoUrl,
                    fallbackImageKey = event.imageKey,
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(14.dp)),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = CircleShape, color = badgeBg) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = if (event.status == "APPROVED") Icons.Filled.CheckCircle else Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = badgeText,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeText,
                        )
                    }
                }
                Text(
                    text = event.eventDateText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${event.venueName} • ${event.district}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (event.adminNote.isNotBlank()) {
                Text(
                    text = "Catatan Admin: ${event.adminNote}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun PendingModerationCard(
    event: SurabayaEvent,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, OutlineVariantLight),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("pending_event_card_${event.id}"),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            if (event.photoUrl.isNotBlank()) {
                SurabayaRemoteOrFallbackImage(
                    imageUrl = event.photoUrl,
                    fallbackImageKey = event.imageKey,
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(116.dp)
                        .clip(RoundedCornerShape(14.dp)),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(shape = CircleShape, color = PendingAmberBg) {
                    Text(
                        text = "Usulan oleh ${event.proposerName}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PendingAmberText,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    )
                }
                Text(
                    text = event.priceLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${event.eventDateText} (${event.timeText}) • ${event.venueName}, ${event.district}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (isAdmin) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reject_event_${event.id}"),
                    ) {
                        Text(stringResource(R.string.btn_reject_event))
                    }
                    Button(
                        onClick = onApprove,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("approve_event_${event.id}"),
                    ) {
                        Text(stringResource(R.string.btn_approve_event))
                    }
                }
            }
        }
    }
}
