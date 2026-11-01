package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            onRefreshGps()
        }
    }

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
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("teman_sekitar_list"),
    ) {
        // Compact GPS Radar & Privacy Control Header Card (Soft Light-Green Gradient)
        item(key = "gps_radar_header") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MintSoftGradient)
                    .border(1.dp, OutlineVariantLight, RoundedCornerShape(22.dp)),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(FreshGreenGradient, CircleShape),
                            ) {
                                Icon(
                                    imageVector = if (isLiveGpsActive) Icons.Filled.GpsFixed else Icons.Filled.MyLocation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(20.dp),
                                )
                            }
                            Column {
                                Text(
                                    text = if (isLiveGpsActive) "GPS Aktif • Kec. $district" else "Area Kec. $district",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ToskaOnPrimaryContainer,
                                )
                                Text(
                                    text = "Radius pantauan $radiusKm km • ${if (discoverable) "Profil Terlihat" else "Mode Privat"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ToskaOnPrimaryContainer.copy(alpha = 0.85f),
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                    ),
                                )
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("update_gps_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.btn_update_gps),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }

                        FilledTonalButton(
                            onClick = onOpenPrivacySheet,
                            shape = CircleShape,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color.White,
                                contentColor = ToskaOnPrimaryContainer,
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("privacy_radius_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = null,
                                tint = ToskaPrimaryDeep,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.btn_privacy_radius),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
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
                    .padding(horizontal = 14.dp)
                    .horizontalScroll(rememberScrollState())
                    .testTag("teman_sekitar_gender_filter_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, OutlineVariantLight),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .testTag("nearby_peer_card_${peer.id}"),
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
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp)
                        .testTag("peer_profile_click_${peer.id}"),
                ) {
                    // Tapping the photo opens the 2-finger pinch-to-zoom dialog directly
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onOpenPhotoZoom)
                            .testTag("peer_avatar_photo_${peer.id}"),
                        contentAlignment = Alignment.Center,
                    ) {
                        InitialsAvatar(
                            name = peer.displayName,
                            colorHex = peer.avatarColorHex,
                            sizeDp = 48,
                            avatarUrl = peer.avatarUrl,
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(ToskaPrimaryDeep)
                                .border(1.5.dp, Color.White, CircleShape),
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

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = peer.displayName,
                                style = MaterialTheme.typography.titleMedium,
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
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        Icon(
                                            imageVector = if (isFemale) Icons.Filled.Female else Icons.Filled.Male,
                                            contentDescription = null,
                                            tint = badgeFg,
                                            modifier = Modifier.size(11.dp),
                                        )
                                        Text(
                                            text = genderLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = badgeFg,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = OpenGreenBg,
                                border = BorderStroke(1.dp, OpenGreenText.copy(alpha = 0.30f)),
                                modifier = Modifier.testTag("peer_distance_badge_${peer.id}"),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.NearMe,
                                        contentDescription = null,
                                        tint = OpenGreenText,
                                        modifier = Modifier.size(11.dp),
                                    )
                                    Text(
                                        text = estimatedDistanceBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OpenGreenText,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = ToskaPrimaryDeep,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    text = peer.district,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ToskaPrimaryDeep,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Button(
                        onClick = onGreet,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("greet_peer_${peer.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.btn_greet_chat),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }

                    IconButton(
                        onClick = { showBlockDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("block_peer_${peer.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Block,
                            contentDescription = "Blokir ${peer.displayName}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            if (peer.bio.isNotBlank()) {
                Text(
                    text = peer.bio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    tags.forEach { hobby ->
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Text(
                                text = hobby,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

