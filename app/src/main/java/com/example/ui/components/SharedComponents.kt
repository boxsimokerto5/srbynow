package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.util.SurabayaDistricts
import com.example.data.util.resolveImageDrawable
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.FreshGreenGradient
import com.example.ui.theme.HeaderMintGradient
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimaryDeep

fun openGoogleMapsLocation(context: Context, lat: Double, lng: Double, label: String) {
    val encodedLabel = Uri.encode(label)
    val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($encodedLabel)")
    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(mapIntent)
    } catch (_: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

fun openEmergencyDialer112(context: Context) {
    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(dialIntent)
    } catch (_: Exception) {
        // Ignore on headless device without dialer
    }
}

fun parseAvatarColor(hex: String): Color {
    return try {
        val parsed = Color(android.graphics.Color.parseColor(hex))
        // Keep avatars vibrant & fresh rather than overly dark
        if ((parsed.red + parsed.green + parsed.blue) < 0.85f) {
            Color(0xFF10B981)
        } else {
            parsed
        }
    } catch (_: Exception) {
        Color(0xFF10B981)
    }
}

@Composable
fun SurabayaCompactTopBar(
    district: String,
    isAdmin: Boolean,
    bookmarkCount: Int,
    notificationCount: Int = 0,
    userInitials: String,
    avatarUrl: String = "",
    onOpenBookmarks: () -> Unit,
    onOpenNotifications: () -> Unit = {},
    onOpenProfile: () -> Unit,
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderMintGradient),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo_mascot),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (isAdmin) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MintSoftGradient)
                                        .border(1.dp, OutlineVariantLight, CircleShape)
                                        .padding(horizontal = 7.dp, vertical = 2.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AdminPanelSettings,
                                            contentDescription = null,
                                            tint = ToskaPrimaryDeep,
                                            modifier = Modifier.size(11.dp),
                                        )
                                        Text(
                                            text = "Admin",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ToskaOnPrimaryContainer,
                                        )
                                    }
                                }
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = ToskaPrimaryDeep,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                text = "$district, Surabaya",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // Notification Bell button with Unread Badge
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("top_notifications_button"),
                    ) {
                        Box {
                            Icon(
                                imageVector = if (notificationCount > 0) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                contentDescription = stringResource(R.string.btn_notifications),
                                tint = if (notificationCount > 0) ToskaPrimaryDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (notificationCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(AlertCrimson),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = notificationCount.coerceAtMost(99).toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }

                    // Bookmarks quick button
                    IconButton(
                        onClick = onOpenBookmarks,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("top_bookmarks_button"),
                    ) {
                        Box {
                            Icon(
                                imageVector = if (bookmarkCount > 0) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = stringResource(R.string.btn_bookmarks),
                                tint = ToskaPrimaryDeep,
                            )
                            if (bookmarkCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(15.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = bookmarkCount.coerceAtMost(9).toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }

                    // Profile & Admin Avatar Pill with Light-Green Gradient
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .clickable(onClick = onOpenProfile)
                            .padding(4.dp)
                            .testTag("top_profile_button"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(FreshGreenGradient),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = userInitials,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                            if (avatarUrl.isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(avatarUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = stringResource(R.string.btn_profile),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(CircleShape),
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(thickness = 0.8.dp, color = OutlineVariantLight.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun CategoryFilterRow(
    categories: List<Pair<String, String>>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    tagPrefix: String,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(categories, key = { it.first }) { (key, label) ->
            val isSelected = key == selectedKey
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(FreshGreenGradient)
                        .clickable { onSelect(key) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("${tagPrefix}_chip_${key.lowercase()}"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            } else {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, OutlineVariantLight),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onSelect(key) }
                        .testTag("${tagPrefix}_chip_${key.lowercase()}"),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun GoogleMapsSheetHandleHeader(
    title: String,
    subtitle: String? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun EmptyFeedPlaceholder(
    icon: ImageVector,
    title: String,
    subtitle: String,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.dp, OutlineVariantLight),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MintSoftGradient),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ToskaPrimaryDeep,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun InitialsAvatar(
    name: String,
    colorHex: String,
    sizeDp: Int = 44,
    avatarUrl: String = "",
) {
    val initials = name.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "SB" }

    val base = parseAvatarColor(colorHex)
    val avatarGradient = Brush.linearGradient(
        colors = listOf(
            base.copy(alpha = 0.82f),
            base,
        ),
    )

    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(avatarGradient),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = if (sizeDp >= 44) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
        if (avatarUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape),
            )
        }
    }
}

@Composable
fun SurabayaRemoteOrFallbackImage(
    imageUrl: String,
    fallbackImageKey: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val fallbackPainter = painterResource(id = resolveImageDrawable(fallbackImageKey))
    if (imageUrl.isNotBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            placeholder = fallbackPainter,
            error = fallbackPainter,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
        )
    } else {
        Image(
            painter = fallbackPainter,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
        )
    }
}

@Composable
fun FormPhotoPickerSection(
    selectedPhotoUri: Uri?,
    onPhotoSelected: (Uri?) -> Unit,
    enabled: Boolean = true,
    title: String = stringResource(R.string.photo_picker_add_title),
    subtitle: String = stringResource(R.string.photo_picker_add_subtitle),
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            onPhotoSelected(uri)
        }
    }

    val launchGallery = {
        if (enabled) {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        }
    }

    if (selectedPhotoUri == null) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF3FBF7),
            border = BorderStroke(1.dp, OutlineVariantLight),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable(enabled = enabled, onClick = launchGallery)
                .testTag("form_pick_photo_btn"),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MintSoftGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddAPhoto,
                        contentDescription = title,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ToskaOnPrimaryContainer,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF6FBF8),
            border = BorderStroke(1.dp, OutlineVariantLight),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(152.dp)
                        .clip(RoundedCornerShape(14.dp)),
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(selectedPhotoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .testTag("form_photo_preview"),
                    )
                    Surface(
                        shape = CircleShape,
                        color = OpenGreenBg.copy(alpha = 0.94f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = OpenGreenText,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                text = "Foto Siap Diunggah",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OpenGreenText,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = launchGallery,
                        enabled = enabled,
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("form_change_photo_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.photo_picker_change_btn))
                    }
                    FilledTonalButton(
                        onClick = { onPhotoSelected(null) },
                        enabled = enabled,
                        shape = CircleShape,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFFFEFEF),
                            contentColor = Color(0xFF93000A),
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("form_remove_photo_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = stringResource(R.string.photo_picker_remove_btn),
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.photo_picker_remove_btn))
                    }
                }
            }
        }
    }
}

/**
 * Compact Dropdown selector for all 31 official Kecamatan of Kota Surabaya.
 * Keeps forms and registration screens clean and concise while offering complete coverage.
 */
@Composable
fun SurabayaDistrictDropdown(
    selectedDistrict: String,
    onSelect: (String) -> Unit,
    enabled: Boolean = true,
    label: String = stringResource(R.string.district_dropdown_label),
    testTag: String = "surabaya_district_dropdown",
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val allDistricts = remember { SurabayaDistricts.all }
    val activeItem = remember(selectedDistrict, allDistricts) {
        SurabayaDistricts.findDistrict(selectedDistrict)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (enabled) Color.White else Color(0xFFF4F7F5),
                border = BorderStroke(
                    width = if (expanded) 1.5.dp else 1.dp,
                    color = if (expanded) ToskaPrimaryDeep else OutlineVariantLight,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = enabled) { expanded = !expanded }
                    .testTag(testTag),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(19.dp),
                        )
                        Text(
                            text = "Kec. ${activeItem?.name ?: selectedDistrict.ifBlank { "Genteng" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (activeItem != null) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEEF9F3),
                            ) {
                                Text(
                                    text = activeItem.region.removePrefix("Surabaya "),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = ToskaPrimaryDeep,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .heightIn(max = 310.dp)
                    .background(Color.White),
            ) {
                allDistricts.forEach { dist ->
                    val isSelected = dist.name.equals(selectedDistrict, ignoreCase = true)
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Kec. ${dist.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ToskaPrimaryDeep else MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = dist.region.removePrefix("Surabaya "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = ToskaPrimaryDeep,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        } else {
                            null
                        },
                        onClick = {
                            onSelect(dist.name)
                            expanded = false
                        },
                        modifier = Modifier.testTag("district_option_${dist.name.lowercase().replace(' ', '_')}"),
                    )
                }
            }
        }
    }
}

/**
 * Compact single-line Dropdown Filter Bar for feed screens (e.g., Kejadian) with "ALL" + all 31 Kecamatan of Surabaya.
 */
@Composable
fun SurabayaDistrictFilterBar(
    selectedDistrict: String,
    onSelect: (String) -> Unit,
    testTag: String = "surabaya_district_filter_dropdown",
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val allDistricts = remember { SurabayaDistricts.all }
    val activeItem = remember(selectedDistrict, allDistricts) {
        if (selectedDistrict == "ALL" || selectedDistrict.isBlank()) null else SurabayaDistricts.findDistrict(selectedDistrict)
    }
    val isAll = selectedDistrict == "ALL" || selectedDistrict.isBlank()

    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            shape = CircleShape,
            color = if (isAll) Color.White else Color(0xFFEEF9F3),
            border = BorderStroke(
                width = if (expanded || !isAll) 1.2.dp else 1.dp,
                color = if (expanded || !isAll) ToskaPrimaryDeep else OutlineVariantLight,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .clickable { expanded = !expanded }
                .testTag(testTag),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(17.dp),
                    )
                    Text(
                        text = if (isAll) {
                            stringResource(R.string.incident_filter_all_districts)
                        } else {
                            "Filter: Kec. ${activeItem?.name ?: selectedDistrict}"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isAll) FontWeight.Medium else FontWeight.Bold,
                        color = if (isAll) MaterialTheme.colorScheme.onSurface else ToskaOnPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (activeItem != null) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.90f),
                        ) {
                            Text(
                                text = activeItem.region.removePrefix("Surabaya "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ToskaPrimaryDeep,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp),
                            )
                        }
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = stringResource(R.string.district_dropdown_label),
                    tint = if (isAll) MaterialTheme.colorScheme.onSurfaceVariant else ToskaPrimaryDeep,
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 320.dp)
                .background(Color.White),
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.incident_filter_all_districts),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
                        color = if (isAll) ToskaPrimaryDeep else MaterialTheme.colorScheme.onSurface,
                    )
                },
                leadingIcon = if (isAll) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                } else {
                    null
                },
                onClick = {
                    onSelect("ALL")
                    expanded = false
                },
                modifier = Modifier.testTag("district_filter_option_all"),
            )
            HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.6f))
            allDistricts.forEach { dist ->
                val isSelected = !isAll && dist.name.equals(selectedDistrict, ignoreCase = true)
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Kec. ${dist.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ToskaPrimaryDeep else MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = dist.region.removePrefix("Surabaya "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = ToskaPrimaryDeep,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        onSelect(dist.name)
                        expanded = false
                    },
                    modifier = Modifier.testTag("district_filter_option_${dist.name.lowercase().replace(' ', '_')}"),
                )
            }
        }
    }
}

/**
 * Utility functions for sharing the app and rating it on the Play Store.
 */
fun shareArekSurabayaApp(context: Context) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Aplikasi Arek Surabaya")
        putExtra(
            Intent.EXTRA_TEXT,
            "Ayo gabung di aplikasi Arek Surabaya! Pantau info kejadian kota, agenda event & festival, kuliner legendaris, direktori wisata, dan kenalan sesama warga Surabaya: https://play.google.com/store/apps/details?id=${context.packageName}",
        )
    }
    context.startActivity(Intent.createChooser(sendIntent, "Bagikan Aplikasi Arek Surabaya").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

fun rateArekSurabayaOnPlayStore(context: Context) {
    val packageName = context.packageName
    val marketUri = Uri.parse("market://details?id=$packageName")
    val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
    }
    try {
        context.startActivity(marketIntent)
    } catch (_: Exception) {
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

/**
 * Reusable menu row item for app support/settings.
 */
@Composable
fun SupportMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .minimumInteractiveComponentSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ToskaPrimaryDeep.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * Dialog displaying information about the Arek Surabaya application.
 */
@Composable
fun AboutAppDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo_mascot),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
                Column {
                    Text(
                        text = "Tentang Arek Surabaya",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Versi 1.0.0 • Komunitas Kota Pahlawan",
                        style = MaterialTheme.typography.labelSmall,
                        color = ToskaPrimaryDeep,
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Arek Surabaya adalah wadah digital terpadu warga Kota Pahlawan untuk saling berbagi informasi kejadian kota terkini, agenda event & festival, rekomendasi kuliner legendaris, direktori wisata kota, mencari teman sekitar berbasis radius GPS, serta obrolan komunitas warga.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0F9F5),
                    border = BorderStroke(1.dp, OutlineVariantLight),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "🏛️ Fitur Utama Aplikasi:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ToskaPrimaryDeep,
                        )
                        Text(text = "• Info Kejadian & Warga (Anti Hoaks & Peta)", style = MaterialTheme.typography.bodySmall)
                        Text(text = "• Agenda Event & Festival Surabaya", style = MaterialTheme.typography.bodySmall)
                        Text(text = "• Direktori Kuliner Legendaris & Modern", style = MaterialTheme.typography.bodySmall)
                        Text(text = "• Teman Sekitar Aman (Jarak Presisi Terlindungi)", style = MaterialTheme.typography.bodySmall)
                        Text(text = "• Momen & Obrolan Real-time Warga", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text(
                    text = "Dikembangkan dengan semangat gotong royong untuk memajukan solidaritas warga Surabaya.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimaryDeep),
            ) {
                Text("Tutup")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onDismiss()
                    shareArekSurabayaApp(context)
                },
                shape = CircleShape,
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Bagikan")
            }
        },
        shape = RoundedCornerShape(20.dp),
    )
}

/**
 * Dialog displaying the transparent Privacy Policy.
 */
@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column {
                    Text(
                        text = "Kebijakan Privasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Perlindungan Data & Keamanan Warga",
                        style = MaterialTheme.typography.labelSmall,
                        color = ToskaPrimaryDeep,
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PrivacyPolicyClause(
                    title = "1. Lokasi & Radius GPS",
                    description = "Lokasi GPS Anda hanya digunakan untuk menghitung jarak perkiraan antarwarga di menu Teman Sekitar dan menyortir info terdekat. Lokasi presisi rumah Anda TIDAK PERNAH disimpan permanen atau diumbar ke publik.",
                )
                PrivacyPolicyClause(
                    title = "2. Kendali Visibilitas Profil",
                    description = "Anda memiliki kendali penuh untuk menyembunyikan profil Anda kapan saja dari menu Teman Sekitar melalui tombol 'Tampilkan Profil' di pengaturan akun.",
                )
                PrivacyPolicyClause(
                    title = "3. Pemindaian Konten AI & Moderasi",
                    description = "Setiap foto yang diunggah dipindai oleh modul AI on-device untuk menyaring konten eksplisit sebelum terbit. Postingan yang dilaporkan warga akan otomatis disamarkan (blur) untuk menjaga kenyamanan komunitas.",
                )
                PrivacyPolicyClause(
                    title = "4. Fitur Blokir & Kebebasan Pengguna",
                    description = "Anda bebas memblokir warga yang mengganggu dan melaporkan konten bermasalah kapan saja. Pengguna yang diblokir tidak dapat berinteraksi dengan Anda.",
                )
                PrivacyPolicyClause(
                    title = "5. Akun & Penghapusan Data",
                    description = "Anda dapat keluar dari sesi atau mengajukan permohonan hapus akun melalui tim pengelola kapan saja.",
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimaryDeep),
            ) {
                Text("Saya Mengerti")
            }
        },
        shape = RoundedCornerShape(20.dp),
    )
}

@Composable
private fun PrivacyPolicyClause(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = ToskaPrimaryDeep,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
