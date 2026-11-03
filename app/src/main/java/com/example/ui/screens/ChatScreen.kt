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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ChatRoom
import com.example.data.model.NearbyProfile
import com.example.data.repository.SurabayaRepository
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.data.util.formatRelativeTimestamp
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.components.GoogleMapsSheetHandleHeader
import com.example.ui.components.InitialsAvatar
import com.example.ui.theme.FreshGreenGradient
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.ToskaPrimary
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
    isAdmin: Boolean = false,
    onCreatePublicGroup: () -> Unit = {},
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

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(top = 6.dp, bottom = if (isAdmin && selectedSubTab == 0) 80.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
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
                        .padding(horizontal = 10.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                    if (selectedSubTab == 0 && isAdmin) {
                        Button(
                            onClick = onCreatePublicGroup,
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            modifier = Modifier.testTag("chat_create_group_btn"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GroupAdd,
                                contentDescription = stringResource(R.string.btn_create_public_group),
                                modifier = Modifier.size(17.dp),
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = stringResource(R.string.btn_create_public_group),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    } else {
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
            }

            if (selectedSubTab == 0) {
                item(key = "chat_public_group_role_banner") {
                    if (isAdmin) {
                        Surface(
                            color = ToskaPrimaryDeep,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                                .testTag("chat_group_admin_banner"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = stringResource(R.string.admin_create_group_banner),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = ToskaPrimaryDeep.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                                .testTag("chat_group_admin_notice"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = ToskaPrimaryDeep,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = stringResource(R.string.admin_only_create_group_notice),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ToskaPrimaryDeep,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
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

        if (isAdmin && selectedSubTab == 0) {
            ExtendedFloatingActionButton(
                onClick = onCreatePublicGroup,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.GroupAdd,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp),
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.btn_create_public_group),
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("fab_create_public_group"),
            )
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
            .padding(vertical = 6.dp, horizontal = 8.dp),
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
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, OutlineVariantLight),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("chat_room_card_${room.id}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (isPublic) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MintSoftGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Groups,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(21.dp),
                    )
                }
            } else if (room.isOfficialAccount) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(FreshGreenGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Akun Resmi Arek Surabaya",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            } else {
                InitialsAvatar(
                    name = displayTitle,
                    colorHex = "#10B981",
                    sizeDp = 40,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (room.isOfficialAccount) FontWeight.Bold else FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (room.isOfficialAccount) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Resmi",
                                tint = ToskaPrimary,
                                modifier = Modifier.size(15.dp),
                            )
                            Surface(
                                shape = CircleShape,
                                color = OpenGreenBg,
                            ) {
                                Text(
                                    text = "RESMI",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OpenGreenText,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                )
                            }
                        }
                    }
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
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(
            text = "Mulai Chat Dua Arah Warga Surabaya",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
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
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 2.dp),
        ) {
            if (filtered.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
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
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF3FBF7),
                        border = BorderStroke(1.dp, OutlineVariantLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectCitizen(peer) }
                            .testTag("citizen_chat_item_${peer.id}"),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            InitialsAvatar(
                                name = peer.displayName,
                                colorHex = peer.avatarColorHex,
                                sizeDp = 38,
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

@Composable
fun CreatePublicGroupSheetContent(
    isSubmitting: Boolean = false,
    onSubmit: (title: String, subtitle: String, welcomeMessage: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var welcomeMessage by remember { mutableStateOf("") }
    var selectedTopicPreset by remember { mutableStateOf("") }

    val topicPresets = listOf(
        "Komunitas Warga" to "Diskusi harian & silaturahmi warga Surabaya",
        "Info Kecamatan" to "Kabar lingkungan, RT/RW & pelayanan wilayah",
        "UMKM & Kuliner" to "Promosi usaha warga, bazar & rekomendasi kuliner",
        "Olahraga & Hobi" to "Komunitas gowes, lari pagi, musik & hobi SBY",
        "Lalin & Darurat" to "Pantauan lalu lintas cepat, cuaca & info darurat",
    )

    val isFormValid = title.trim().length >= 3 && subtitle.trim().length >= 4

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag("create_public_group_sheet"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GoogleMapsSheetHandleHeader(
            title = stringResource(R.string.create_group_sheet_title),
            subtitle = stringResource(R.string.create_group_sheet_subtitle),
        )

        Surface(
            shape = CircleShape,
            color = OpenGreenBg,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.AdminPanelSettings,
                    contentDescription = null,
                    tint = OpenGreenText,
                    modifier = Modifier.size(15.dp),
                )
                Text(
                    text = stringResource(R.string.admin_only_badge),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OpenGreenText,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            topicPresets.forEach { (presetLabel, presetSubtitle) ->
                FilterChip(
                    selected = selectedTopicPreset == presetLabel,
                    onClick = {
                        if (!isSubmitting) {
                            selectedTopicPreset = presetLabel
                            if (subtitle.isBlank() || topicPresets.any { it.second == subtitle }) {
                                subtitle = presetSubtitle
                            }
                        }
                    },
                    label = { Text(presetLabel, style = MaterialTheme.typography.labelSmall) },
                    shape = CircleShape,
                )
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            enabled = !isSubmitting,
            label = { Text(stringResource(R.string.create_group_name_label)) },
            placeholder = { Text("Contoh: Grup Warga Gubeng & Sekitarnya") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = ToskaPrimaryDeep,
                    modifier = Modifier.size(20.dp),
                )
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_group_title"),
        )

        OutlinedTextField(
            value = subtitle,
            onValueChange = { subtitle = it },
            enabled = !isSubmitting,
            label = { Text(stringResource(R.string.create_group_subtitle_label)) },
            placeholder = { Text("Contoh: Info kegiatan, kuliner & pantauan wilayah") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_group_subtitle"),
        )

        OutlinedTextField(
            value = welcomeMessage,
            onValueChange = { welcomeMessage = it },
            enabled = !isSubmitting,
            label = { Text(stringResource(R.string.create_group_welcome_label)) },
            placeholder = { Text("Opsional • Pesan sapaan pertama dari Admin di dalam grup…") },
            shape = RoundedCornerShape(16.dp),
            minLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_group_welcome"),
        )

        Button(
            onClick = {
                onSubmit(
                    title.trim(),
                    subtitle.trim(),
                    welcomeMessage.trim(),
                )
            },
            enabled = isFormValid && !isSubmitting,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_create_group_btn"),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.submitting_upload_label))
            } else {
                Icon(
                    imageVector = Icons.Filled.GroupAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_submit_create_group))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
