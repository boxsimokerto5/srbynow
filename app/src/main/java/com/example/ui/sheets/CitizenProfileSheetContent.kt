package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.NearbyProfile
import com.example.data.model.UserMoment
import com.example.data.util.formatEstimatedDistanceLabel
import com.example.ui.components.GoogleMapsSheetHandleHeader
import com.example.ui.components.InitialsAvatar
import com.example.ui.components.SurabayaRemoteOrFallbackImage
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.ToskaPrimaryDeep
import kotlin.math.roundToInt

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
    estimatedDistanceKm: Double? = null,
    onOpenMomentDetail: (String) -> Unit,
    onGreetCitizen: (NearbyProfile) -> Unit,
    onBlockCitizen: (uid: String, name: String) -> Unit,
    onEditMyProfile: () -> Unit,
    onDeleteAccount: (() -> Unit)? = null,
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
    val resolvedGenderLabel = citizenProfile?.genderLabel.orEmpty()
    val resolvedAvatarUrl = citizenProfile?.avatarUrl.orEmpty()
    val resolvedAvatarColorHex = citizenProfile?.avatarColorHex ?: "#006B5E"
    val estimatedDistanceText = if (!isMyOwnProfile && estimatedDistanceKm != null) {
        stringResource(R.string.nearby_estimated_distance_badge, formatEstimatedDistanceLabel(estimatedDistanceKm))
    } else {
        ""
    }

    val totalMoments = citizenMoments.size
    val totalLikes = citizenMoments.sumOf { it.likesCount }

    val personalMoments = remember(citizenMoments) { citizenMoments.filter { !it.isLensaWarga } }
    val lensaWargaMoments = remember(citizenMoments) { citizenMoments.filter { it.isLensaWarga } }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val displayedMoments = if (selectedCategoryIndex == 0) personalMoments else lensaWargaMoments

    var showBlockConfirmDialog by remember { mutableStateOf(false) }
    var showAvatarZoomDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var userConfirmedDeleteCheckbox by remember { mutableStateOf(false) }

    if (showAvatarZoomDialog) {
        val subtitleParts = buildList {
            add("Kec. $resolvedDistrict")
            if (resolvedGenderLabel.isNotBlank()) add(resolvedGenderLabel)
            if (estimatedDistanceText.isNotBlank()) add(estimatedDistanceText)
        }
        CitizenAvatarZoomDialog(
            name = resolvedName,
            subtitle = subtitleParts.joinToString(" • "),
            avatarUrl = resolvedAvatarUrl,
            avatarColorHex = resolvedAvatarColorHex,
            onDismiss = { showAvatarZoomDialog = false },
        )
    }

    if (showDeleteAccountDialog && onDeleteAccount != null) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.DeleteForever,
                    contentDescription = null,
                    tint = AlertCrimson,
                    modifier = Modifier.size(36.dp),
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.delete_account_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.delete_account_dialog_desc, resolvedName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF0F0),
                        border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    userConfirmedDeleteCheckbox = !userConfirmedDeleteCheckbox
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Checkbox(
                                checked = userConfirmedDeleteCheckbox,
                                onCheckedChange = { userConfirmedDeleteCheckbox = it },
                                colors = CheckboxDefaults.colors(checkedColor = AlertCrimson),
                                modifier = Modifier.testTag("dialog_delete_checkbox"),
                            )
                            Text(
                                text = stringResource(R.string.delete_account_confirm_checkbox),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccount()
                        onClose()
                    },
                    enabled = userConfirmedDeleteCheckbox,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlertCrimson,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier.testTag("dialog_confirm_delete_btn"),
                ) {
                    Text(stringResource(R.string.delete_account_dialog_confirm_btn))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    modifier = Modifier.testTag("dialog_cancel_delete_btn"),
                ) {
                    Text(stringResource(R.string.delete_account_dialog_cancel_btn))
                }
            },
        )
    }

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
                subtitle = buildString {
                    append("Komunitas Arek Suroboyo • @$resolvedDistrict")
                    if (estimatedDistanceText.isNotBlank()) {
                        append(" • $estimatedDistanceText")
                    }
                },
            )
        }

        // 2. Instagram-style Profile Header Card
        item {
            InstagramProfileHeaderCard(
                name = resolvedName,
                district = resolvedDistrict,
                bio = resolvedBio,
                interests = resolvedInterests,
                avatarUrl = resolvedAvatarUrl,
                avatarColorHex = resolvedAvatarColorHex,
                totalMoments = totalMoments,
                totalLikes = totalLikes,
                isMyOwnProfile = isMyOwnProfile,
                genderLabel = resolvedGenderLabel,
                estimatedDistanceText = estimatedDistanceText,
                onAvatarClick = { showAvatarZoomDialog = true },
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
                onDeleteAccount = if (onDeleteAccount != null) { { showDeleteAccountDialog = true } } else null,
            )
        }

        // 3. Category Tabs: Momen vs Lensa Warga
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = selectedCategoryIndex == 0,
                        onClick = { selectedCategoryIndex = 0 },
                        label = { Text("📸 Momen (${personalMoments.size})") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.GridOn,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ToskaPrimaryDeep,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("tab_citizen_moments"),
                    )

                    FilterChip(
                        selected = selectedCategoryIndex == 1,
                        onClick = { selectedCategoryIndex = 1 },
                        label = { Text("🏙️ Lensa Warga (${lensaWargaMoments.size})") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ToskaPrimaryDeep,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.1f).testTag("tab_citizen_lensa_warga"),
                    )
                }

                if (displayedMoments.isNotEmpty()) {
                    Text(
                        text = if (selectedCategoryIndex == 0) "Momen gaya hidup & cerita warga • Ketuk foto untuk detail"
                        else "Potret sudut kota & keramaian Surabaya • Ketuk foto untuk detail",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // 4. Instagram 3-Column Square Grid
        if (displayedMoments.isEmpty()) {
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
                                imageVector = if (selectedCategoryIndex == 0) Icons.Outlined.PhotoLibrary else Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = ToskaPrimaryDeep,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        Text(
                            text = if (selectedCategoryIndex == 0) {
                                if (isMyOwnProfile) "Kamu belum membagikan Momen Pribadi" else "Belum ada momen pribadi yang dibagikan"
                            } else {
                                if (isMyOwnProfile) "Kamu belum membagikan foto Lensa Warga" else "Belum ada foto Lensa Warga yang dibagikan"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = if (selectedCategoryIndex == 0) {
                                if (isMyOwnProfile) "Abadikan momen jalan-jalan atau kulineran santai dan bagikan ke warga!"
                                else "Warga ini belum mengunggah potret momen pribadi santai di Surabaya."
                            } else {
                                if (isMyOwnProfile) "Abadikan sudut kota favorit, jalanan, atau keramaian Surabaya ke Lensa Warga!"
                                else "Warga ini belum mengunggah potret sudut estetis atau dinamika kota ke Lensa Warga."
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
            val chunkedMoments = displayedMoments.chunked(3)
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
    onDeleteAccount: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    genderLabel: String = "",
    estimatedDistanceText: String = "",
    onAvatarClick: (() -> Unit)? = null,
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
                // Avatar with modern Toska-Green Instagram-like ring (tap to open 2-finger zoom viewer)
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .then(
                            if (onAvatarClick != null) {
                                Modifier
                                    .clip(CircleShape)
                                    .clickable(onClick = onAvatarClick)
                                    .testTag("citizen_profile_avatar_btn")
                            } else {
                                Modifier
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
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

                    if (onAvatarClick != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(ToskaPrimaryDeep)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ZoomIn,
                                contentDescription = stringResource(R.string.profile_tap_photo_to_zoom),
                                tint = Color.White,
                                modifier = Modifier.size(13.dp),
                            )
                        }
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

            // Row 2: Name, District, Estimated Distance & Gender Badges
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    if (onAvatarClick != null) {
                        Text(
                            text = "• ${stringResource(R.string.profile_tap_photo_to_zoom)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
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

                    if (estimatedDistanceText.isNotBlank()) {
                        Surface(
                            shape = CircleShape,
                            color = OpenGreenBg,
                            border = BorderStroke(1.dp, OpenGreenText.copy(alpha = 0.35f)),
                            modifier = Modifier.testTag("citizen_profile_distance_badge"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.NearMe,
                                    contentDescription = null,
                                    tint = OpenGreenText,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    text = estimatedDistanceText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OpenGreenText,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    if (genderLabel.isNotBlank()) {
                        val isFemale = genderLabel.equals("Perempuan", ignoreCase = true)
                        val badgeBg = if (isFemale) Color(0xFFFCE4EC) else Color(0xFFE3F2FD)
                        val badgeFg = if (isFemale) Color(0xFFAD1457) else Color(0xFF1565C0)
                        Surface(
                            shape = CircleShape,
                            color = badgeBg,
                            modifier = Modifier.testTag("citizen_profile_gender_badge"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Icon(
                                    imageVector = if (isFemale) Icons.Filled.Female else Icons.Filled.Male,
                                    contentDescription = null,
                                    tint = badgeFg,
                                    modifier = Modifier.size(13.dp),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onEditProfile,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
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

                    if (onDeleteAccount != null) {
                        OutlinedButton(
                            onClick = onDeleteAccount,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCrimson),
                            border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("profile_delete_account_btn"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteForever,
                                contentDescription = "Hapus Akun Warga",
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
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

/**
 * Dialog layar penuh untuk melihat foto profil warga dengan dukungan
 * cubit 2 jari (pinch-to-zoom 1x–5x), geser (pan), dan ketuk 2x (double-tap zoom).
 */
@Composable
fun CitizenAvatarZoomDialog(
    name: String,
    subtitle: String = "",
    avatarUrl: String,
    avatarColorHex: String = "#006B5E",
    onDismiss: () -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var imageLoadError by remember(avatarUrl) { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF5070B0E))
                .testTag("citizen_photo_zoom_dialog"),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { containerSize = it }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1.05f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    scale = 2.5f
                                    offset = Offset.Zero
                                }
                            },
                        )
                    }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            do {
                                val event = awaitPointerEvent()
                                val pressedCount = event.changes.count { it.pressed }
                                if (pressedCount >= 2) {
                                    val zoomChange = event.calculateZoom()
                                    val panChange = event.calculatePan()
                                    val newScale = (scale * zoomChange).coerceIn(1f, 5f)
                                    val maxX = ((containerSize.width * (newScale - 1f)) / 2f).coerceAtLeast(0f)
                                    val maxY = ((containerSize.height * (newScale - 1f)) / 2f).coerceAtLeast(0f)
                                    scale = newScale
                                    offset = if (newScale <= 1.01f) {
                                        Offset.Zero
                                    } else {
                                        Offset(
                                            x = (offset.x + panChange.x).coerceIn(-maxX, maxX),
                                            y = (offset.y + panChange.y).coerceIn(-maxY, maxY),
                                        )
                                    }
                                    event.changes.forEach { change ->
                                        if (change.positionChanged()) {
                                            change.consume()
                                        }
                                    }
                                } else if (pressedCount == 1 && scale > 1.02f) {
                                    val panChange = event.calculatePan()
                                    val maxX = ((containerSize.width * (scale - 1f)) / 2f).coerceAtLeast(0f)
                                    val maxY = ((containerSize.height * (scale - 1f)) / 2f).coerceAtLeast(0f)
                                    offset = Offset(
                                        x = (offset.x + panChange.x).coerceIn(-maxX, maxX),
                                        y = (offset.y + panChange.y).coerceIn(-maxY, maxY),
                                    )
                                    event.changes.forEach { change ->
                                        if (change.positionChanged()) {
                                            change.consume()
                                        }
                                    }
                                }
                            } while (event.changes.any { it.pressed })

                            if (scale < 1.02f) {
                                scale = 1f
                                offset = Offset.Zero
                            }
                        }
                    }
                    .testTag("citizen_photo_zoom_canvas"),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (avatarUrl.isNotBlank() && !imageLoadError) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto Profil $name",
                            contentScale = ContentScale.Fit,
                            onError = { imageLoadError = true },
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp)),
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(236.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.sweepGradient(
                                            listOf(
                                                Color(0xFF006B5E),
                                                Color(0xFF26A69A),
                                                Color(0xFF80CBC4),
                                                Color(0xFF006B5E),
                                            ),
                                        ),
                                        CircleShape,
                                    )
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                InitialsAvatar(
                                    name = name,
                                    colorHex = avatarColorHex,
                                    sizeDp = 224,
                                    avatarUrl = "",
                                )
                            }
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            // Top Bar: Citizen Name + Zoom Level + Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.50f))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.82f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.16f),
                        modifier = Modifier
                            .then(
                                if (scale > 1.05f) {
                                    Modifier
                                        .clip(CircleShape)
                                        .clickable {
                                            scale = 1f
                                            offset = Offset.Zero
                                        }
                                } else {
                                    Modifier
                                },
                            )
                            .testTag("citizen_photo_zoom_level_badge"),
                    ) {
                        Text(
                            text = "${(scale * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .testTag("citizen_photo_zoom_close_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.photo_viewer_close_cd),
                            tint = Color.White,
                        )
                    }
                }
            }

            // Bottom Hint Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.50f))
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.ZoomIn,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.profile_photo_zoom_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

