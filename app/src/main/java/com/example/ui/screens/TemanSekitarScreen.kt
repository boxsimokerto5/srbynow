package com.example.ui.screens

import androidx.compose.foundation.clickable
import com.example.ui.sheets.LocationUsageContext
import com.example.ui.sheets.rememberLocationPermissionWithDisclosure
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.NearbyProfile
import com.example.data.model.UiState
import com.example.data.model.UserAccount
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.data.util.formatEstimatedDistanceLabel
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.components.InitialsAvatar
import com.example.ui.sheets.CitizenAvatarZoomDialog
import com.example.ui.theme.FreshGreenGradient
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimaryDeep
import com.example.ui.viewmodel.NearbyPeerItem

@Composable
fun TemanSekitarScreen(
    nearbyPeersState: UiState<List<NearbyPeerItem>>,
    userAccount: UserAccount?,
    isLiveGpsActive: Boolean,
    onRefreshGps: () -> Unit,
    onOpenPrivacySheet: () -> Unit,
    onGreetPeer: (NearbyProfile) -> Unit,
    onBlockPeer: (NearbyProfile) -> Unit = {},
    onOpenCitizenProfile: (NearbyProfile) -> Unit = {},
) {
    val requestOrRefreshGps = rememberLocationPermissionWithDisclosure(
        usageContext = LocationUsageContext.NEARBY_CITIZENS,
        onPermissionGranted = onRefreshGps,
    )

    val radiusKm = (userAccount?.searchRadiusKm ?: 10.0).toInt()
    val district = userAccount?.district ?: "Genteng"
    val discoverable = userAccount?.isDiscoverable ?: true

    var selectedGenderFilter by rememberSaveable { mutableStateOf("ALL") }
    var zoomPhotoPeerItem by remember { mutableStateOf<NearbyPeerItem?>(null) }

    val activeZoomItem = zoomPhotoPeerItem
    if (activeZoomItem != null) {
        val zp = activeZoomItem.profile
        val estText = stringResource(
            R.string.nearby_estimated_distance_badge,
            formatEstimatedDistanceLabel(activeZoomItem.distanceKm),
        )
        val subtitleParts = buildList {
            add("Kec. ${zp.district}")
            if (zp.genderLabel.isNotBlank()) add(zp.genderLabel)
            add(estText)
        }
        CitizenAvatarZoomDialog(
            name = zp.displayName.ifBlank { "Warga Surabaya" },
            subtitle = subtitleParts.joinToString(" • "),
            avatarUrl = zp.avatarUrl,
            avatarColorHex = zp.avatarColorHex,
            onDismiss = { zoomPhotoPeerItem = null },
        )
    }

    val allSuccessPeers = (nearbyPeersState as? UiState.Success)?.data.orEmpty()
    val femaleCount = remember(allSuccessPeers) {
        allSuccessPeers.count { it.profile.normalizedGender == "PEREMPUAN" }
    }
    val maleCount = remember(allSuccessPeers) {
        allSuccessPeers.count { it.profile.normalizedGender == "LAKI_LAKI" }
    }

    LazyColumn(
        contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("teman_sekitar_list"),
    ) {
        // Slim GPS Radar & Privacy Control Header Bar
        item(key = "gps_radar_header") {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, OutlineVariantLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MintSoftGradient),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(FreshGreenGradient),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (isLiveGpsActive) Icons.Filled.GpsFixed else Icons.Filled.MyLocation,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(17.dp),
                            )
                        }
                        Column {
                            Text(
                                text = if (isLiveGpsActive) "GPS Aktif • Kec. $district" else "Area Kec. $district",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ToskaOnPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "Radius $radiusKm km • ${if (discoverable) "Profil Terlihat" else "Mode Privat"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ToskaOnPrimaryContainer.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable(onClick = requestOrRefreshGps)
                                .testTag("update_gps_button"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MyLocation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp),
                                )
                                Text(
                                    text = "GPS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, OutlineVariantLight),
                            modifier = Modifier
                                .clickable(onClick = onOpenPrivacySheet)
                                .testTag("privacy_radius_button"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = null,
                                    tint = ToskaPrimaryDeep,
                                    modifier = Modifier.size(13.dp),
                                )
                                Text(
                                    text = "Radius",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ToskaOnPrimaryContainer,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Gender Filter Chips Row (Semua, Perempuan saja, Laki-laki saja)
        item(key = "gender_filter_bar") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .horizontalScroll(rememberScrollState())
                    .testTag("teman_sekitar_gender_filter_row"),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val allLabel = if (nearbyPeersState is UiState.Success) {
                    "${stringResource(R.string.nearby_filter_gender_all)} (${allSuccessPeers.size})"
                } else {
                    stringResource(R.string.nearby_filter_gender_all)
                }
                val femaleLabel = if (nearbyPeersState is UiState.Success) {
                    "${stringResource(R.string.nearby_filter_gender_female)} ($femaleCount)"
                } else {
                    stringResource(R.string.nearby_filter_gender_female)
                }
                val maleLabel = if (nearbyPeersState is UiState.Success) {
                    "${stringResource(R.string.nearby_filter_gender_male)} ($maleCount)"
                } else {
                    stringResource(R.string.nearby_filter_gender_male)
                }

                FilterChip(
                    selected = selectedGenderFilter == "ALL",
                    onClick = { selectedGenderFilter = "ALL" },
                    label = { Text(allLabel, fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Groups,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimaryDeep,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("filter_gender_all"),
                )

                FilterChip(
                    selected = selectedGenderFilter == "PEREMPUAN",
                    onClick = { selectedGenderFilter = "PEREMPUAN" },
                    label = { Text(femaleLabel, fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Female,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFAD1457),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("filter_gender_female"),
                )

                FilterChip(
                    selected = selectedGenderFilter == "LAKI_LAKI",
                    onClick = { selectedGenderFilter = "LAKI_LAKI" },
                    label = { Text(maleLabel, fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Male,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF1565C0),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("filter_gender_male"),
                )
            }
        }

        when (nearbyPeersState) {
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
                        icon = Icons.Outlined.Group,
                        title = "Gagal mencari warga sekitar",
                        subtitle = nearbyPeersState.message,
                    )
                }
            }
            is UiState.Success -> {
                val peers = nearbyPeersState.data
                val filteredPeers = when (selectedGenderFilter) {
                    "PEREMPUAN" -> peers.filter { it.profile.normalizedGender == "PEREMPUAN" }
                    "LAKI_LAKI" -> peers.filter { it.profile.normalizedGender == "LAKI_LAKI" }
                    else -> peers
                }
                if (peers.isEmpty()) {
                    item {
                        EmptyFeedPlaceholder(
                            icon = Icons.Outlined.Group,
                            title = "Belum ada warga dalam radius $radiusKm km",
                            subtitle = "Perbesar jangkauan kilometer di menu Radius & Privasi untuk menemukan lebih banyak teman Surabaya.",
                        )
                    }
                } else if (filteredPeers.isEmpty()) {
                    item {
                        val emptyTitle = if (selectedGenderFilter == "PEREMPUAN") {
                            stringResource(R.string.nearby_empty_female_title, radiusKm)
                        } else {
                            stringResource(R.string.nearby_empty_male_title, radiusKm)
                        }
                        EmptyFeedPlaceholder(
                            icon = Icons.Outlined.Group,
                            title = emptyTitle,
                            subtitle = stringResource(R.string.nearby_empty_gender_subtitle),
                        )
                    }
                } else {
                    items(filteredPeers, key = { it.profile.id }) { item ->
                        NearbyPeerCard(
                            peerItem = item,
                            onGreet = { onGreetPeer(item.profile) },
                            onBlock = { onBlockPeer(item.profile) },
                            onOpenProfile = { onOpenCitizenProfile(item.profile) },
                            onOpenPhotoZoom = { zoomPhotoPeerItem = item },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NearbyPeerCard(
    peerItem: NearbyPeerItem,
    onGreet: () -> Unit,
    onBlock: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenPhotoZoom: () -> Unit = {},
) {
    val peer = peerItem.profile
    val tags = peer.interests.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(3)
    val estimatedDistanceBadge = stringResource(
        R.string.nearby_estimated_distance_badge,
        formatEstimatedDistanceLabel(peerItem.distanceKm),
    )
    val genderLabel = peer.genderLabel
    var showBlockDialog by remember(peer.id) { mutableStateOf(false) }

    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text(stringResource(R.string.dialog_block_user_title, peer.displayName)) },
            text = { Text(stringResource(R.string.dialog_block_user_body, peer.displayName)) },
            confirmButton = {
                Button(
                    onClick = {
                        showBlockDialog = false
                        onBlock()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_block_peer_${peer.id}"),
                ) {
                    Text(stringResource(R.string.btn_confirm_block_user))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) {
                    Text(stringResource(R.string.btn_cancel_action))
                }
            },
        )
    }

    Surface(
        onClick = onOpenProfile,
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nearby_peer_card_${peer.id}"),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Foto Avatar Rounded Square (Gaya Ramping ala MiChat) dengan zoom hint
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenPhotoZoom)
                        .testTag("peer_avatar_photo_${peer.id}"),
                    contentAlignment = Alignment.Center,
                ) {
                    InitialsAvatar(
                        name = peer.displayName,
                        colorHex = peer.avatarColorHex,
                        sizeDp = 52,
                        avatarUrl = peer.avatarUrl,
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ZoomIn,
                            contentDescription = stringResource(R.string.profile_tap_photo_to_zoom),
                            tint = Color.White,
                            modifier = Modifier.size(10.dp),
                        )
                    }
                }

                // Kolom Informasi Utama: Nama + Gender, Jarak + Lokasi Lengkap, dan Bio / Hobi
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 1.dp)
                        .testTag("peer_profile_click_${peer.id}"),
                    verticalArrangement = Arrangement.spacedBy(2.5.dp),
                ) {
                    // Baris 1: Nama Warga + Badge Gender
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = peer.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )

                        if (genderLabel.isNotBlank()) {
                            val isFemale = peer.normalizedGender == "PEREMPUAN"
                            val badgeBg = if (isFemale) Color(0xFFFCE4EC) else Color(0xFFE3F2FD)
                            val badgeFg = if (isFemale) Color(0xFFAD1457) else Color(0xFF1565C0)
                            Surface(
                                shape = CircleShape,
                                color = badgeBg,
                                modifier = Modifier.testTag("peer_gender_badge_${peer.id}"),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Icon(
                                        imageVector = if (isFemale) Icons.Filled.Female else Icons.Filled.Male,
                                        contentDescription = null,
                                        tint = badgeFg,
                                        modifier = Modifier.size(10.dp),
                                    )
                                    Text(
                                        text = genderLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = badgeFg,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }

                    // Baris 2: Estimasi Jarak GPS & Kecamatan (Lengkap tanpa terpotong!)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.testTag("peer_distance_badge_${peer.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NearMe,
                            contentDescription = null,
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(11.dp),
                        )
                        Text(
                            text = estimatedDistanceBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = ToskaPrimaryDeep,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(11.dp),
                        )
                        Text(
                            text = "Kec. ${peer.district}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // Baris 3: Bio Warga & Minat / Hobi (Tetap tercantum semua tanpa terlewat)
                    val statusText = when {
                        peer.bio.isNotBlank() && tags.isNotEmpty() -> "${peer.bio} • ${tags.joinToString(", ")}"
                        peer.bio.isNotBlank() -> peer.bio
                        tags.isNotEmpty() -> tags.joinToString(" • ")
                        else -> "Warga aktif Arek Surabaya"
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Sisi Kanan: Status Tag Pill Abu-abu (ala MiChat) + Tombol Sapa & Opsi Blokir
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val rightTag = tags.firstOrNull() ?: if (peer.bio.isNotBlank()) "Warga" else "Aktif"
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F3F4),
                    ) {
                        Text(
                            text = rightTag,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF5F6368),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable(onClick = onGreet)
                                .testTag("greet_peer_${peer.id}"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp),
                                )
                                Text(
                                    text = stringResource(R.string.btn_greet_chat),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }

                        IconButton(
                            onClick = { showBlockDialog = true },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("block_peer_${peer.id}"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Block,
                                contentDescription = "Blokir ${peer.displayName}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }
                }
            }

            // Garis Pembatas Tipis & Rapi antar item (indentasi setelah avatar ala MiChat)
            HorizontalDivider(
                color = OutlineVariantLight.copy(alpha = 0.5f),
                thickness = 0.7.dp,
                modifier = Modifier.padding(start = 78.dp, end = 12.dp),
            )
        }
    }
}

