package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ChatRoom
import com.example.data.model.NearbyProfile
import com.example.data.repository.SurabayaRepository
import com.example.data.util.CommunityGroupPinHelper
import com.example.data.util.formatRelativeTimestamp
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.components.GoogleMapsSheetHandleHeader
import com.example.ui.components.InitialsAvatar
import com.example.ui.theme.AlertCrimson
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
    unlockedGroupPinHashes: Map<String, String> = emptyMap(),
    onCreatePublicGroup: () -> Unit = {},
    knownCitizens: List<NearbyProfile> = emptyList(),
) {
    var publicSearchQuery by rememberSaveable { mutableStateOf("") }
    var directSearchQuery by rememberSaveable { mutableStateOf("") }

    val activeSearchQuery = if (selectedSubTab == 0) publicSearchQuery else directSearchQuery

    val myActiveGroupCount = remember(publicRooms, currentUserId) {
        if (currentUserId.isBlank()) 0
        else publicRooms.count { it.roomType == "PUBLIC" && it.createdBy == currentUserId }
    }
    val isCitizenQuotaFull = !isAdmin && myActiveGroupCount >= CommunityGroupPinHelper.MAX_GROUPS_PER_CITIZEN

    val baseRooms = if (selectedSubTab == 0) publicRooms else directRooms
    val activeRooms = remember(baseRooms, activeSearchQuery, currentUserId, isAdmin, unlockedGroupPinHashes) {
        if (activeSearchQuery.isBlank()) {
            baseRooms
        } else {
            val q = activeSearchQuery.trim().lowercase()
            baseRooms.filter { room ->
                val canReadPreview = CommunityGroupPinHelper.hasRoomAccess(
                    pinHash = room.pinHash,
                    roomId = room.id,
                    roomCreatedBy = room.createdBy,
                    currentUserId = currentUserId,
                    isAdmin = isAdmin,
                    unlockedPinHashesByRoomId = unlockedGroupPinHashes,
                )
                room.displayTitleFor(currentUserId).lowercase().contains(q) ||
                    room.subtitle.lowercase().contains(q) ||
                    (canReadPreview && room.lastMessage.lowercase().contains(q)) ||
                    room.creatorName.lowercase().contains(q) ||
                    room.roomKey.lowercase().contains(q) ||
                    room.id.lowercase().contains(q)
            }
        }
    }

    val focusManager = LocalFocusManager.current
    val currentOnSelectSubTab by rememberUpdatedState(onSelectSubTab)

    val citizenByUid = remember(knownCitizens) {
        buildMap {
            for (c in knownCitizens) {
                if (c.userId.isNotBlank()) put(c.userId, c)
                if (c.id.isNotBlank()) put(c.id, c)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Fixed top header outside LazyColumn so tab switching never loses clicks during list recomposition
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
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
                            onClick = {
                                if (selectedSubTab != 0) {
                                    currentOnSelectSubTab(0)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_tab_public"),
                        )
                        ChatModeTabPill(
                            label = "Pesan Pribadi (${directRooms.size})",
                            selected = selectedSubTab == 1,
                            onClick = {
                                if (selectedSubTab != 1) {
                                    currentOnSelectSubTab(1)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_tab_direct"),
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    OutlinedTextField(
                        value = activeSearchQuery,
                        onValueChange = {
                            if (selectedSubTab == 0) {
                                publicSearchQuery = it
                            } else {
                                directSearchQuery = it
                            }
                        },
                        placeholder = {
                            Text(
                                text = if (selectedSubTab == 0) "Cari grup publik / komunitas…" else "Cari teman atau ID room…",
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
                        trailingIcon = {
                            if (activeSearchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        if (selectedSubTab == 0) {
                                            publicSearchQuery = ""
                                        } else {
                                            directSearchQuery = ""
                                        }
                                    },
                                    modifier = Modifier.size(24.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Hapus pencarian",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
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
                    if (selectedSubTab == 0) {
                        Button(
                            onClick = onCreatePublicGroup,
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                            modifier = Modifier.testTag("chat_create_group_btn"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GroupAdd,
                                contentDescription = stringResource(R.string.btn_create_public_group),
                                modifier = Modifier.size(17.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
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

            LazyColumn(
                contentPadding = PaddingValues(top = 2.dp, bottom = if (selectedSubTab == 0) 80.dp else 20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("chat_rooms_list"),
            ) {
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
                                color = if (isCitizenQuotaFull) Color(0xFFFFF8E1) else ToskaPrimaryDeep.copy(alpha = 0.08f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isCitizenQuotaFull) Color(0xFFFFD54F) else ToskaPrimaryDeep.copy(alpha = 0.16f),
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp)
                                    .testTag("chat_group_citizen_quota_banner"),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = if (isCitizenQuotaFull) Icons.Filled.Lock else Icons.Filled.Info,
                                        contentDescription = null,
                                        tint = if (isCitizenQuotaFull) Color(0xFFB45309) else ToskaPrimaryDeep,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isCitizenQuotaFull) {
                                                stringResource(
                                                    R.string.group_quota_full_hint,
                                                    myActiveGroupCount,
                                                    CommunityGroupPinHelper.MAX_GROUPS_PER_CITIZEN,
                                                )
                                            } else {
                                                stringResource(R.string.admin_only_create_group_notice)
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isCitizenQuotaFull) Color(0xFF92400E) else ToskaPrimaryDeep,
                                        )
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isCitizenQuotaFull) Color(0xFFFDE68A) else OpenGreenBg,
                                    ) {
                                        Text(
                                            text = "$myActiveGroupCount/${CommunityGroupPinHelper.MAX_GROUPS_PER_CITIZEN}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCitizenQuotaFull) Color(0xFF92400E) else OpenGreenText,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (activeRooms.isEmpty()) {
                    item(key = if (selectedSubTab == 0) "empty_public_rooms" else "empty_direct_rooms") {
                        val isSearching = activeSearchQuery.isNotBlank()
                        EmptyFeedPlaceholder(
                            icon = if (selectedSubTab == 0) Icons.Filled.Forum else Icons.AutoMirrored.Filled.Chat,
                            title = if (selectedSubTab == 0) {
                                if (isSearching) "Grup Tidak Ditemukan" else "Belum Ada Grup Publik"
                            } else {
                                if (isSearching) "Obrolan Tidak Ditemukan" else "Belum ada obrolan pribadi"
                            },
                            subtitle = if (selectedSubTab == 0) {
                                if (isSearching) {
                                    "Tidak ada grup atau komunitas yang cocok dengan \"$activeSearchQuery\"."
                                } else {
                                    "Ruang diskusi terbuka atau komunitas ber-PIN bagi seluruh warga Kota Pahlawan."
                                }
                            } else {
                                if (isSearching) {
                                    "Tidak ada kontak obrolan yang cocok dengan \"$activeSearchQuery\"."
                                } else {
                                    "Tekan tombol Chat Warga di atas atau tombol Sapa pada menu Sekitar untuk memulai percakapan dua arah!"
                                }
                            },
                        )
                    }
                } else {
                    items(
                        items = activeRooms,
                        key = { it.id },
                    ) { room ->
                        val isUnlocked = CommunityGroupPinHelper.hasRoomAccess(
                            pinHash = room.pinHash,
                            roomId = room.id,
                            roomCreatedBy = room.createdBy,
                            currentUserId = currentUserId,
                            isAdmin = isAdmin,
                            unlockedPinHashesByRoomId = unlockedGroupPinHashes,
                        )
                        val peerUid = if (room.roomType == "DIRECT") {
                            room.otherParticipantIdFor(currentUserId)
                                ?: room.roomKey.removePrefix("direct_").split("_").firstOrNull { it.isNotBlank() && it != currentUserId }
                        } else null
                        val peerProfile = peerUid?.let { citizenByUid[it] }

                        ChatRoomRowCard(
                            room = room,
                            currentUserId = currentUserId,
                            isAdmin = isAdmin,
                            isPinUnlocked = isUnlocked,
                            peerProfile = peerProfile,
                            onClick = { onOpenRoom(room) },
                        )
                    }
                }
            }
        }

        if (selectedSubTab == 0) {
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
                        text = if (isAdmin) {
                            stringResource(R.string.btn_create_public_group)
                        } else {
                            "${stringResource(R.string.btn_create_public_group)} ($myActiveGroupCount/${CommunityGroupPinHelper.MAX_GROUPS_PER_CITIZEN})"
                        },
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
            .clickable(
                role = androidx.compose.ui.semantics.Role.Tab,
                onClick = onClick,
            )
            .then(
                if (selected) {
                    Modifier.background(FreshGreenGradient, CircleShape)
                } else {
                    Modifier.background(Color.Transparent)
                },
            )
            .padding(vertical = 9.dp, horizontal = 10.dp),
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
    isAdmin: Boolean,
    isPinUnlocked: Boolean,
    peerProfile: NearbyProfile? = null,
    onClick: () -> Unit,
) {
    val isPublic = room.roomType == "PUBLIC"
    val isPinProtected = room.isPinProtected
    val isMine = isPublic && room.isCreatedBy(currentUserId)
    val displayTitle = room.displayTitleFor(currentUserId)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(
            1.dp,
            if (isPinProtected && !isPinUnlocked) Color(0xFFFCD34D) else OutlineVariantLight,
        ),
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
                        .then(
                            if (isPinProtected && !isPinUnlocked) {
                                Modifier.background(Color(0xFFFEF3C7))
                            } else {
                                Modifier.background(MintSoftGradient)
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = when {
                            isPinProtected && !isPinUnlocked -> Icons.Filled.Lock
                            isPinProtected && isPinUnlocked -> Icons.Filled.LockOpen
                            else -> Icons.Filled.Groups
                        },
                        contentDescription = null,
                        tint = if (isPinProtected && !isPinUnlocked) Color(0xFFB45309) else ToskaPrimaryDeep,
                        modifier = Modifier.size(20.dp),
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
                    colorHex = peerProfile?.avatarColorHex ?: "#10B981",
                    sizeDp = 40,
                    avatarUrl = peerProfile?.avatarUrl.orEmpty(),
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
                            fontWeight = if (room.isOfficialAccount || isMine) FontWeight.Bold else FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (isPinProtected) {
                            val badgeBg = when {
                                isAdmin && !isMine -> Color(0xFFE0F2FE)
                                isPinUnlocked -> OpenGreenBg
                                else -> Color(0xFFFEF3C7)
                            }
                            val badgeFg = when {
                                isAdmin && !isMine -> Color(0xFF0369A1)
                                isPinUnlocked -> OpenGreenText
                                else -> Color(0xFFB45309)
                            }
                            val badgeText = when {
                                isAdmin && !isMine -> stringResource(R.string.group_badge_admin_bypass)
                                isPinUnlocked -> stringResource(R.string.group_badge_unlocked_pin)
                                else -> stringResource(R.string.group_badge_locked_pin)
                            }
                            Surface(
                                shape = CircleShape,
                                color = badgeBg,
                                modifier = Modifier.testTag("chat_room_pin_badge_${room.id}"),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Icon(
                                        imageVector = if (isPinUnlocked) Icons.Filled.LockOpen else Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = badgeFg,
                                        modifier = Modifier.size(11.dp),
                                    )
                                    Text(
                                        text = badgeText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeFg,
                                    )
                                }
                            }
                        }
                        if (isMine) {
                            Surface(
                                shape = CircleShape,
                                color = OpenGreenBg,
                                modifier = Modifier.testTag("chat_room_mine_badge_${room.id}"),
                            ) {
                                Text(
                                    text = stringResource(R.string.group_badge_my_group),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OpenGreenText,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                )
                            }
                        } else if (room.isOfficialAccount) {
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
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatRelativeTimestamp(room.updatedAt ?: room.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (room.subtitle.isNotBlank()) {
                    val subtitleWithCreator = if (isPublic && room.creatorName.isNotBlank()) {
                        "${room.subtitle} • Oleh ${room.creatorName}"
                    } else {
                        room.subtitle
                    }
                    Text(
                        text = subtitleWithCreator,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                val previewMessage = if (isPinProtected && !isPinUnlocked) {
                    stringResource(R.string.group_masked_last_message)
                } else if (room.lastSenderName.isNotBlank()) {
                    "${room.lastSenderName}: ${room.lastMessage}"
                } else {
                    room.lastMessage.ifBlank { "Ketuk untuk membuka percakapan" }
                }
                Text(
                    text = previewMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPinProtected && !isPinUnlocked) {
                        Color(0xFF92400E)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (isPinProtected && !isPinUnlocked) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("chat_room_preview_${room.id}"),
                )
            }
        }
    }
}

@Composable
fun UnlockGroupPinDialog(
    room: ChatRoom,
    onDismiss: () -> Unit,
    onVerifyPin: (String) -> Boolean,
) {
    var pinInput by remember(room.id) { mutableStateOf("") }
    var showError by remember(room.id) { mutableStateOf(false) }
    var showDigits by remember(room.id) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Color(0xFFB45309),
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.dialog_unlock_pin_title),
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.dialog_unlock_pin_subtitle, room.title),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { raw ->
                        pinInput = CommunityGroupPinHelper.sanitizePinDigits(raw)
                        if (showError) showError = false
                    },
                    label = { Text(stringResource(R.string.group_pin_input_label)) },
                    placeholder = { Text("••••") },
                    singleLine = true,
                    isError = showError,
                    visualTransformation = if (showDigits) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        IconButton(onClick = { showDigits = !showDigits }) {
                            Icon(
                                imageVector = if (showDigits) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showDigits) "Sembunyikan PIN" else "Tampilkan PIN",
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unlock_group_pin_input"),
                )

                if (showError) {
                    Text(
                        text = stringResource(R.string.dialog_unlock_pin_error),
                        style = MaterialTheme.typography.labelSmall,
                        color = AlertCrimson,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("unlock_group_pin_error"),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ok = onVerifyPin(pinInput)
                    if (!ok) {
                        showError = true
                    }
                },
                enabled = CommunityGroupPinHelper.isValidFourDigitPin(pinInput),
                shape = CircleShape,
                modifier = Modifier.testTag("confirm_unlock_group_pin_btn"),
            ) {
                Icon(Icons.Filled.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.btn_unlock_group_pin))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel_action))
            }
        },
    )
}

@Composable
fun ManageGroupPinDialog(
    room: ChatRoom,
    onDismiss: () -> Unit,
    onSavePin: (String) -> Unit,
) {
    var newPin by remember(room.id) { mutableStateOf("") }
    var showDigits by remember(room.id) { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = ToskaPrimaryDeep,
                modifier = Modifier.size(28.dp),
            )
        },
        title = {
            Text(
                text = stringResource(R.string.dialog_manage_pin_title, room.title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.dialog_manage_pin_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = newPin,
                    onValueChange = { newPin = CommunityGroupPinHelper.sanitizePinDigits(it) },
                    label = { Text(stringResource(R.string.group_pin_input_label)) },
                    placeholder = { Text(stringResource(R.string.group_pin_input_placeholder)) },
                    singleLine = true,
                    visualTransformation = if (showDigits) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        IconButton(onClick = { showDigits = !showDigits }) {
                            Icon(
                                imageVector = if (showDigits) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showDigits) "Sembunyikan PIN" else "Tampilkan PIN",
                            )
                        }
                    },
                    supportingText = {
                        Text("${newPin.length}/${CommunityGroupPinHelper.PIN_LENGTH} angka")
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manage_group_pin_input"),
                )

                if (room.isPinProtected) {
                    OutlinedButton(
                        onClick = { onSavePin("") },
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("remove_group_pin_btn"),
                    ) {
                        Icon(Icons.Filled.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.btn_remove_group_pin))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSavePin(newPin) },
                enabled = CommunityGroupPinHelper.isValidFourDigitPin(newPin),
                shape = CircleShape,
                modifier = Modifier.testTag("save_manage_group_pin_btn"),
            ) {
                Text(stringResource(R.string.btn_save_new_pin))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel_action))
            }
        },
    )
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
    isAdmin: Boolean = false,
    myActiveGroupCount: Int = 0,
    onSubmitWithPin: (title: String, subtitle: String, welcomeMessage: String, pinCode: String) -> Unit = { _, _, _, _ -> },
    onSubmit: ((title: String, subtitle: String, welcomeMessage: String) -> Unit)? = null,
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var welcomeMessage by remember { mutableStateOf("") }
    var selectedTopicPreset by remember { mutableStateOf("") }
    var usePinProtection by remember { mutableStateOf(false) }
    var pinCode by remember { mutableStateOf("") }
    var showPinDigits by remember { mutableStateOf(true) }

    val topicPresets = listOf(
        "Komunitas Warga" to "Diskusi harian & silaturahmi warga Surabaya",
        "Info Kecamatan" to "Kabar lingkungan, RT/RW & pelayanan wilayah",
        "UMKM & Kuliner" to "Promosi usaha warga, bazar & rekomendasi kuliner",
        "Olahraga & Hobi" to "Komunitas gowes, lari pagi, musik & hobi SBY",
        "Lalin & Darurat" to "Pantauan lalu lintas cepat, cuaca & info darurat",
    )

    val isQuotaFull = !isAdmin && myActiveGroupCount >= CommunityGroupPinHelper.MAX_GROUPS_PER_CITIZEN
    val isPinValid = !usePinProtection || CommunityGroupPinHelper.isValidFourDigitPin(pinCode)
    val isFormValid = title.trim().length >= 3 && subtitle.trim().length >= 4 && isPinValid && !isQuotaFull

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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
                        imageVector = if (isAdmin) Icons.Filled.AdminPanelSettings else Icons.Filled.Groups,
                        contentDescription = null,
                        tint = OpenGreenText,
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = if (isAdmin) {
                            "Admin Resmi • Tanpa Batas Grup"
                        } else {
                            stringResource(
                                R.string.group_quota_citizen_badge,
                                myActiveGroupCount,
                                CommunityGroupPinHelper.MAX_GROUPS_PER_CITIZEN,
                            )
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OpenGreenText,
                    )
                }
            }
        }

        // Privacy Mode Selector: Terbuka (Tanpa PIN) vs Komunitas Ber-PIN (4 Angka)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF6FBF8),
            border = BorderStroke(1.dp, OutlineVariantLight),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Pengaturan Privasi Ruang / Grup",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = ToskaPrimaryDeep,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = !usePinProtection,
                        onClick = {
                            if (!isSubmitting) {
                                usePinProtection = false
                                pinCode = ""
                            }
                        },
                        label = {
                            Text(
                                text = stringResource(R.string.group_privacy_mode_open),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ToskaPrimaryDeep,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("group_mode_open_chip"),
                    )
                    FilterChip(
                        selected = usePinProtection,
                        onClick = {
                            if (!isSubmitting) {
                                usePinProtection = true
                            }
                        },
                        label = {
                            Text(
                                text = stringResource(R.string.group_privacy_mode_pin),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFB45309),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("group_mode_pin_chip"),
                    )
                }

                if (usePinProtection) {
                    OutlinedTextField(
                        value = pinCode,
                        onValueChange = { pinCode = CommunityGroupPinHelper.sanitizePinDigits(it) },
                        enabled = !isSubmitting,
                        label = { Text(stringResource(R.string.group_pin_input_label)) },
                        placeholder = { Text(stringResource(R.string.group_pin_input_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(19.dp),
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPinDigits = !showPinDigits }) {
                                Icon(
                                    imageVector = if (showPinDigits) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showPinDigits) "Sembunyikan PIN" else "Tampilkan PIN",
                                )
                            }
                        },
                        visualTransformation = if (showPinDigits) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        supportingText = {
                            Text("${stringResource(R.string.group_pin_helper_text)} (${pinCode.length}/4)")
                        },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_group_pin_input"),
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("group_pin_moderation_notice"),
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = stringResource(R.string.group_pin_moderation_notice),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF92400E),
                            )
                        }
                    }
                }
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
            placeholder = { Text("Opsional • Pesan sapaan pertama di dalam grup…") },
            shape = RoundedCornerShape(16.dp),
            minLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_group_welcome"),
        )

        Button(
            onClick = {
                val effectivePin = if (usePinProtection) pinCode.trim() else ""
                if (onSubmit != null && effectivePin.isEmpty()) {
                    onSubmit(title.trim(), subtitle.trim(), welcomeMessage.trim())
                } else {
                    onSubmitWithPin(
                        title.trim(),
                        subtitle.trim(),
                        welcomeMessage.trim(),
                        effectivePin,
                    )
                }
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
                    imageVector = if (usePinProtection) Icons.Filled.Lock else Icons.Filled.GroupAdd,
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
