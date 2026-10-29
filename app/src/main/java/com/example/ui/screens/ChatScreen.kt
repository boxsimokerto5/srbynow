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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ChatRoom
import com.example.data.model.NearbyProfile
import com.example.data.repository.SurabayaRepository
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.data.util.formatRelativeTimestamp
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.components.InitialsAvatar
import com.example.ui.theme.FreshGreenGradient
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.ToskaPrimaryDeep

@Composable
fun ChatScreen(
    publicRooms: List<ChatRoom>,
    directRooms: List<ChatRoom>,
    currentUserId: String,
    selectedSubTab: Int,
    onSelectSubTab: (Int) -> Unit,
    onStartNewDirectChat: () -> Unit,
    onOpenRoom: (ChatRoom) -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val baseRooms = if (selectedSubTab == 0) publicRooms else directRooms
    val activeRooms = remember(baseRooms, searchQuery, currentUserId) {
        if (searchQuery.isBlank()) {
            baseRooms
        } else {
            val q = searchQuery.trim().lowercase()
            baseRooms.filter { room ->
                room.displayTitleFor(currentUserId).lowercase().contains(q) ||
                    room.subtitle.lowercase().contains(q) ||
                    room.lastMessage.lowercase().contains(q) ||
                    room.roomKey.lowercase().contains(q) ||
                    room.id.lowercase().contains(q)
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("chat_rooms_list"),
    ) {
        item(key = "chat_sub_tabs") {
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ChatModeTabPill(
                        label = "Ruang Publik SBY (${publicRooms.size})",
                        selected = selectedSubTab == 0,
                        onClick = { onSelectSubTab(0) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_tab_public"),
                    )
                    ChatModeTabPill(
                        label = "Pesan Pribadi (${directRooms.size})",
                        selected = selectedSubTab == 1,
                        onClick = { onSelectSubTab(1) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_tab_direct"),
                    )
                }
            }
        }

        item(key = "chat_search_and_new_bar") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Cari teman, ruang, atau ID room…",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    shape = CircleShape,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = OutlineVariantLight,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_search_input"),
                )
                FilledTonalButton(
                    onClick = onStartNewDirectChat,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    modifier = Modifier.testTag("chat_start_direct_btn"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonAddAlt1,
                        contentDescription = "Mulai Chat Warga",
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Chat Warga", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        if (activeRooms.isEmpty()) {
            item {
                EmptyFeedPlaceholder(
                    icon = if (selectedSubTab == 0) Icons.Filled.Forum else Icons.AutoMirrored.Filled.Chat,
                    title = if (selectedSubTab == 0) "Memuat Ruang Publik Surabaya" else "Belum ada obrolan pribadi",
                    subtitle = if (selectedSubTab == 0) {
                        "Ruang diskusi terbuka dua arah bagi seluruh warga Kota Pahlawan."
                    } else {
                        "Tekan tombol Chat Warga di atas atau tombol Sapa pada menu Sekitar untuk memulai percakapan dua arah!"
                    },
                )
            }
        } else {
            items(activeRooms, key = { it.id }) { room ->
                ChatRoomRowCard(
                    room = room,
                    currentUserId = currentUserId,
                    onClick = { onOpenRoom(room) },
                )
            }
        }
    }
}

@Composable
private fun ChatModeTabPill(
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
            .padding(vertical = 8.dp, horizontal = 10.dp),
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
private fun ChatRoomRowCard(
    room: ChatRoom,
    currentUserId: String,
    onClick: () -> Unit,
) {
    val isPublic = room.roomType == "PUBLIC"
    val displayTitle = room.displayTitleFor(currentUserId)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, OutlineVariantLight),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("chat_room_card_${room.id}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (isPublic) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MintSoftGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Groups,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(24.dp),
                    )
                }
            } else {
                InitialsAvatar(
                    name = displayTitle,
                    colorHex = "#10B981",
                    sizeDp = 46,
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
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = formatRelativeTimestamp(room.updatedAt ?: room.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (room.subtitle.isNotBlank()) {
                    Text(
                        text = room.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = if (room.lastSenderName.isNotBlank()) {
                        "${room.lastSenderName}: ${room.lastMessage}"
                    } else {
                        room.lastMessage.ifBlank { "Ketuk untuk membuka percakapan" }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun StartDirectChatSheetContent(
    citizens: List<NearbyProfile>,
    currentUserId: String,
    onSelectCitizen: (NearbyProfile) -> Unit,
) {
    var filterQuery by remember { mutableStateOf("") }
    val filtered = remember(citizens, filterQuery) {
        if (filterQuery.isBlank()) {
            citizens
        } else {
            val q = filterQuery.trim().lowercase()
            citizens.filter {
                it.displayName.lowercase().contains(q) ||
                    it.district.lowercase().contains(q) ||
                    it.interests.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Mulai Chat Dua Arah Warga Surabaya",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = "Pilih warga untuk terhubung pada Room ID yang sama secara otomatis & real-time.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = filterQuery,
            onValueChange = { filterQuery = it },
            placeholder = { Text("Cari nama atau kecamatan warga…", style = MaterialTheme.typography.bodySmall) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            shape = CircleShape,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("start_chat_citizen_search"),
        )

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 380.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            if (filtered.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Belum ada warga lain yang cocok dengan pencarian.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(filtered, key = { it.id }) { peer ->
                    val sharedRoomId = SurabayaRepository.buildDirectRoomId(
                        currentUserId,
                        peer.id.ifBlank { peer.userId },
                    )
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFF3FBF7),
                        border = BorderStroke(1.dp, OutlineVariantLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onSelectCitizen(peer) }
                            .testTag("citizen_chat_item_${peer.id}"),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            InitialsAvatar(
                                name = peer.displayName,
                                colorHex = peer.avatarColorHex,
                                sizeDp = 42,
                                avatarUrl = peer.avatarUrl,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = peer.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "Kec. ${peer.district} • ID: ${sharedRoomId.take(22)}…",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Button(
                                onClick = { onSelectCitizen(peer) },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            ) {
                                Text("Chat", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
