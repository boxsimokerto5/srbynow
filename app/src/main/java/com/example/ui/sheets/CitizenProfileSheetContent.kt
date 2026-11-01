package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.NearbyProfile
import com.example.data.model.UserMoment
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.components.GoogleMapsSheetHandleHeader
import com.example.ui.components.InitialsAvatar
import com.example.ui.components.SurabayaRemoteOrFallbackImage
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.ToskaPrimaryDeep

/**
 * Bottom Sheet ala profil Instagram untuk warga Surabaya.
 * Menampilkan avatar, bio, minat, statistik postingan, serta galeri grid
 * momen yang telah diposting oleh warga terkait.
 */
@Composable
fun CitizenProfileSheetContent(
    citizenUserId: String,
    initialName: String,
    citizenProfile: NearbyProfile?,
    citizenMoments: List<UserMoment>,
    currentUserId: String,
    isAdmin: Boolean,
    onOpenMomentDetail: (String) -> Unit,
    onGreetCitizen: (NearbyProfile) -> Unit,
    onBlockCitizen: (uid: String, name: String) -> Unit,
    onEditMyProfile: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMyOwnProfile = citizenUserId == currentUserId
    val resolvedName = citizenProfile?.displayName?.ifBlank { initialName }
        ?: initialName.ifBlank { "Warga Surabaya" }
    val resolvedDistrict = citizenProfile?.district?.ifBlank { "Genteng" } ?: "Genteng"
    val resolvedBio = citizenProfile?.bio?.ifBlank { "Halo rek! Salam kenal sesama warga Surabaya." }
        ?: "Halo rek! Salam kenal sesama warga Surabaya."
    val resolvedInterests = citizenProfile?.interests.orEmpty()
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val totalMoments = citizenMoments.size
    val totalLikes = citizenMoments.sumOf { it.likesCount }

    var showBlockConfirmDialog by remember { mutableStateOf(false) }

    if (showBlockConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBlockConfirmDialog = false },
            title = { Text(stringResource(R.string.dialog_block_user_title, resolvedName)) },
            text = { Text(stringResource(R.string.dialog_block_user_body, resolvedName)) },
            confirmButton = {
                Button(
                    onClick = {
                        showBlockConfirmDialog = false
                        onBlockCitizen(citizenUserId, resolvedName)
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_block_citizen_btn"),
                ) {
                    Text(stringResource(R.string.btn_confirm_block_user))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirmDialog = false }) {
                    Text(stringResource(R.string.btn_cancel_action))
                }
            },
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .testTag("citizen_profile_sheet"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 1. Google Maps style header handle
        item {
            GoogleMapsSheetHandleHeader(
                title = if (isMyOwnProfile) "Profil Saya" else "Profil Warga",
                subtitle = "Komunitas Arek Suroboyo • @$resolvedDistrict",
            )
        }

        // 2. Instagram-style Profile Header Card
        item {
            InstagramProfileHeaderCard(
                name = resolvedName,
                district = resolvedDistrict,
                bio = resolvedBio,
                interests = resolvedInterests,
                avatarUrl = citizenProfile?.avatarUrl.orEmpty(),
                avatarColorHex = citizenProfile?.avatarColorHex ?: "#006B5E",
                totalMoments = totalMoments,
                totalLikes = totalLikes,
                isMyOwnProfile = isMyOwnProfile,
                onGreet = {
                    val p = citizenProfile ?: NearbyProfile(
                        userId = citizenUserId,
                        displayName = resolvedName,
                        district = resolvedDistrict,
                    )
                    onGreetCitizen(p)
                },
                onBlock = { showBlockConfirmDialog = true },
                onEditProfile = onEditMyProfile,
            )
        }

        // 3. Grid Section Header (Instagram Gallery Style)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.GridOn,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = if (isMyOwnProfile) "Momen Saya" else "Momen Warga",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Surface(
                        shape = CircleShape,
                        color = ToskaPrimaryDeep.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = "$totalMoments",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ToskaPrimaryDeep,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }

                if (totalMoments > 0) {
                    Text(
                        text = "Ketuk foto untuk detail",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // 4. Instagram 3-Column Square Grid
        if (citizenMoments.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFF7FBF8),
                    border = BorderStroke(1.dp, OutlineVariantLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(ToskaPrimaryDeep.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoLibrary,
                                contentDescription = null,
                                tint = ToskaPrimaryDeep,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        Text(
                            text = if (isMyOwnProfile) "Kamu belum membagikan Momen" else "Belum ada momen yang dibagikan",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = if (isMyOwnProfile) {
                                "Abadikan momen jalan-jalan, kulineran, atau potret kota Surabaya dan bagikan ke warga!"
                            } else {
                                "Warga ini belum mengunggah potret momen di Surabaya."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        } else {
            // Chunk moments into rows of 3 for smooth Instagram-style grid layout
            val chunkedMoments = citizenMoments.chunked(3)
            items(chunkedMoments) { rowMoments ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    for (moment in rowMoments) {
                        InstagramGridThumbnail(
                            moment = moment,
                            onClick = { onOpenMomentDetail(moment.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Fill remaining slots in row to maintain 1:1 square ratio
                    for (i in 0 until (3 - rowMoments.size)) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Kartu Header profil bergaya Instagram lengkap dengan ring avatar,
 * statistik (Momen, Suka, Wilayah), bio, dan tombol aksi.
 */
@Composable
fun InstagramProfileHeaderCard(
    name: String,
    district: String,
    bio: String,
    interests: List<String>,
    avatarUrl: String,
    avatarColorHex: String,
    totalMoments: Int,
    totalLikes: Int,
    isMyOwnProfile: Boolean,
    onGreet: () -> Unit,
    onBlock: () -> Unit,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.dp, OutlineVariantLight),
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("instagram_profile_header"),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Row 1: Avatar with Instagram gradient ring + Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Avatar with modern Toska-Green Instagram-like ring
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFF006B5E),
                                    Color(0xFF26A69A),
                                    Color(0xFF80CBC4),
                                    Color(0xFF006B5E),
                                )
                            ),
                            CircleShape
                        )
                        .padding(3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        InitialsAvatar(
                            name = name,
                            colorHex = avatarColorHex,
                            sizeDp = 66,
                            avatarUrl = avatarUrl,
                        )
                    }
                }

                // Stats Columns: Momen, Suka, Wilayah
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfileStatItem(
                        count = "$totalMoments",
                        label = "Momen",
                        testTag = "stat_moments_count",
                    )
                    ProfileStatItem(
                        count = "$totalLikes",
                        label = "Suka ❤️",
                        testTag = "stat_likes_count",
                    )
                    ProfileStatItem(
                        count = district.take(8),
                        label = "Kecamatan",
                        testTag = "stat_district_label",
                    )
                }
            }

            // Row 2: Name & District Badge
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Warga Terverifikasi",
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(16.dp),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "Kec. $district, Surabaya",
                        style = MaterialTheme.typography.labelMedium,
                        color = ToskaPrimaryDeep,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // Row 3: Bio Text
            if (bio.isNotBlank()) {
                Text(
                    text = bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Row 4: Interests Chips (e.g. #Kuliner #Kopi #Event)
            if (interests.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    interests.take(4).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F5E9),
                            border = BorderStroke(1.dp, Color(0xFFC8E6C9)),
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
            }

            // Row 5: Action Buttons
            if (isMyOwnProfile) {
                OutlinedButton(
                    onClick = onEditProfile,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("profile_edit_btn"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profil & Pengaturan", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onGreet,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimaryDeep),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("greet_citizen_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kirim Pesan", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onBlock,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCrimson),
                        border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("block_citizen_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Block,
                            contentDescription = "Blokir",
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatItem(
    count: String,
    label: String,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.testTag(testTag),
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Thumbnail foto 1:1 kotak ala Instagram Grid dengan badge suka & indikator foto ganda.
 */
@Composable
fun InstagramGridThumbnail(
    moment: UserMoment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val photoUrl = moment.allPhotos.firstOrNull().orEmpty()
    val isMultiPhoto = moment.allPhotos.size > 1

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE0F2F1))
            .clickable(onClick = onClick)
            .testTag("moment_thumbnail_${moment.id}"),
    ) {
        SurabayaRemoteOrFallbackImage(
            imageUrl = photoUrl,
            fallbackImageKey = moment.imageKey,
            contentDescription = moment.caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Gradient dark scrim at the bottom for readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                    )
                ),
        )

        // Like counter badge at bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = "${moment.likesCount}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }

        // Multi-photo indicator in top-right corner
        if (isMultiPhoto) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Collections,
                    contentDescription = "Multi Foto",
                    tint = Color.White,
                    modifier = Modifier.size(11.dp),
                )
            }
        }
    }
}
