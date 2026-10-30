package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.CulinarySpot
import com.example.data.model.IncidentReport
import com.example.data.model.SurabayaEvent
import com.example.data.util.LinkSecurityValidator
import com.example.data.util.LinkValidationResult
import com.example.data.util.PendingExternalLink
import com.example.data.util.SafeLinkCategory
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.PendingAmberBg
import com.example.ui.theme.PendingAmberText
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimaryDeep
import kotlin.math.abs

fun openExternalUrlSafely(
    context: Context,
    rawUrl: String,
    expectedCategory: SafeLinkCategory? = null,
): Boolean {
    val cleaned = rawUrl.trim()
    if (cleaned.isBlank()) return false
    val safeUrl = if (expectedCategory != null) {
        val result = LinkSecurityValidator.validateOptionalLink(cleaned, expectedCategory)
        if (result !is LinkValidationResult.Valid || result.normalizedUrl.isBlank()) return false
        result.normalizedUrl
    } else {
        if (!cleaned.startsWith("https://", ignoreCase = true) && !cleaned.startsWith("geo:", ignoreCase = true)) {
            return false
        }
        cleaned
    }
    return try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    } catch (_: Exception) {
        false
    }
}

fun openAutoCulinaryMapsSearch(context: Context, spot: CulinarySpot) {
    val queryText = buildString {
        append(spot.name)
        if (spot.address.isNotBlank()) append(", ${spot.address}")
        if (spot.district.isNotBlank()) append(", Kec. ${spot.district}")
        append(", Surabaya")
    }
    val encodedQuery = Uri.encode(queryText)
    val mapsSearchUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedQuery")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, mapsSearchUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
        openGoogleMapsLocation(context, spot.lat, spot.lng, spot.name)
    }
}

fun openAutoEventMapsSearch(context: Context, event: SurabayaEvent) {
    val queryText = buildString {
        append(event.venueName.ifBlank { event.title })
        if (event.address.isNotBlank()) append(", ${event.address}")
        if (event.district.isNotBlank()) append(", Kec. ${event.district}")
        append(", Surabaya")
    }
    val encodedQuery = Uri.encode(queryText)
    val mapsSearchUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedQuery")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, mapsSearchUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
        openGoogleMapsLocation(context, event.lat, event.lng, event.venueName.ifBlank { event.title })
    }
}

fun openAutoIncidentMapsSearch(context: Context, report: IncidentReport) {
    val queryText = buildString {
        if (report.address.isNotBlank()) {
            append(report.address)
        } else {
            append(report.title)
        }
        if (report.district.isNotBlank()) append(", Kec. ${report.district}")
        append(", Surabaya")
    }
    val encodedQuery = Uri.encode(queryText)
    val mapsSearchUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedQuery")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, mapsSearchUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
        openGoogleMapsLocation(context, report.lat, report.lng, report.title)
    }
}

fun openCulinaryMapsDirection(
    context: Context,
    spot: CulinarySpot,
    onRequestConfirmLink: ((PendingExternalLink) -> Unit)? = null,
) {
    val pending = LinkSecurityValidator.inspectForConfirmation(
        rawUrl = spot.safeMapsUrl,
        label = "Google Maps • ${spot.name}",
        expectedCategory = SafeLinkCategory.GOOGLE_MAPS,
    )
    if (pending != null) {
        if (onRequestConfirmLink != null) {
            onRequestConfirmLink(pending)
            return
        }
        if (openExternalUrlSafely(context, pending.url, SafeLinkCategory.GOOGLE_MAPS)) return
    }
    openAutoCulinaryMapsSearch(context, spot)
}

fun openEventMapsDirection(
    context: Context,
    event: SurabayaEvent,
    onRequestConfirmLink: ((PendingExternalLink) -> Unit)? = null,
) {
    val pending = LinkSecurityValidator.inspectForConfirmation(
        rawUrl = event.safeMapsUrl,
        label = "Google Maps • ${event.venueName.ifBlank { event.title }}",
        expectedCategory = SafeLinkCategory.GOOGLE_MAPS,
    )
    if (pending != null) {
        if (onRequestConfirmLink != null) {
            onRequestConfirmLink(pending)
            return
        }
        if (openExternalUrlSafely(context, pending.url, SafeLinkCategory.GOOGLE_MAPS)) return
    }
    openAutoEventMapsSearch(context, event)
}

fun openIncidentMapsDirection(
    context: Context,
    report: IncidentReport,
    onRequestConfirmLink: ((PendingExternalLink) -> Unit)? = null,
) {
    val pending = LinkSecurityValidator.inspectForConfirmation(
        rawUrl = report.safeMapsUrl,
        label = "Google Maps • ${report.title}",
        expectedCategory = SafeLinkCategory.GOOGLE_MAPS,
    )
    if (pending != null) {
        if (onRequestConfirmLink != null) {
            onRequestConfirmLink(pending)
            return
        }
        if (openExternalUrlSafely(context, pending.url, SafeLinkCategory.GOOGLE_MAPS)) return
    }
    openAutoIncidentMapsSearch(context, report)
}

/**
 * Transparent Safe-Exit Confirmation Dialog shown before opening any user-submitted external link.
 * Displays the verified host domain and full URL so citizens are 100% protected from disguised phishing links.
 */
@Composable
fun SafeExternalLinkConfirmationDialog(
    pendingLink: PendingExternalLink,
    isReportedByMe: Boolean = false,
    onReportSuspicious: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onConfirmOpen: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MintSoftGradient),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.GppGood,
                    contentDescription = null,
                    tint = ToskaPrimaryDeep,
                    modifier = Modifier.size(26.dp),
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.safe_exit_dialog_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("safe_external_link_dialog"),
            ) {
                Text(
                    text = stringResource(R.string.safe_exit_dialog_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF3FBF7),
                    border = BorderStroke(1.dp, OutlineVariantLight),
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
                            Surface(
                                shape = CircleShape,
                                color = OpenGreenBg,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.VerifiedUser,
                                        contentDescription = null,
                                        tint = OpenGreenText,
                                        modifier = Modifier.size(13.dp),
                                    )
                                    Text(
                                        text = stringResource(R.string.safe_exit_verified_badge),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = OpenGreenText,
                                    )
                                }
                            }
                        }

                        Text(
                            text = pendingLink.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = ToskaOnPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Text(
                            text = "Domain tujuan: ${pendingLink.host}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ToskaPrimaryDeep,
                        )

                        Text(
                            text = pendingLink.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("safe_external_link_preview_url"),
                        )
                    }
                }

                if (onReportSuspicious != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Tautan terasa janggal?",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Surface(
                            shape = CircleShape,
                            color = if (isReportedByMe) PendingAmberBg else Color(0xFFFFEFEF),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(enabled = !isReportedByMe) {
                                    onReportSuspicious()
                                    onDismiss()
                                }
                                .testTag("dialog_report_suspicious_link_btn"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Flag,
                                    contentDescription = null,
                                    tint = if (isReportedByMe) PendingAmberText else AlertCrimson,
                                    modifier = Modifier.size(13.dp),
                                )
                                Text(
                                    text = stringResource(
                                        if (isReportedByMe) R.string.btn_reported_suspicious_link else R.string.btn_report_suspicious_link,
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isReportedByMe) PendingAmberText else AlertCrimson,
                                )
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = CircleShape,
                modifier = Modifier.testTag("safe_external_link_cancel_btn"),
            ) {
                Text(stringResource(R.string.safe_exit_btn_cancel))
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmOpen,
                shape = CircleShape,
                modifier = Modifier.testTag("safe_external_link_confirm_btn"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.safe_exit_btn_continue))
            }
        },
    )
}

/**
 * Compact security bar shown in Detail Sheets when a post contains user-submitted external links
 * (or when links were auto-quarantined by citizen reports), plus one-tap Admin link removal.
 */
@Composable
fun PostExternalLinkSecurityStrip(
    hasActiveExternalLinks: Boolean,
    isQuarantined: Boolean,
    linkReportCount: Int,
    isReportedByMe: Boolean,
    isAdmin: Boolean,
    onReportLink: () -> Unit,
    onAdminClearLinks: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (!hasActiveExternalLinks && !isQuarantined) return

    if (isQuarantined) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = PendingAmberBg,
            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
            modifier = modifier
                .fillMaxWidth()
                .testTag("quarantined_link_banner"),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.GppMaybe,
                    contentDescription = null,
                    tint = PendingAmberText,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.quarantined_link_notice),
                    style = MaterialTheme.typography.labelSmall,
                    color = PendingAmberText,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFF3FBF7),
            border = BorderStroke(1.dp, OutlineVariantLight),
            modifier = modifier
                .fillMaxWidth()
                .testTag("verified_link_security_strip"),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = if (linkReportCount > 0) {
                            "Domain resmi • $linkReportCount laporan warga"
                        } else {
                            "Proteksi Link Aktif • Domain resmi terverifikasi"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = ToskaOnPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isAdmin && onAdminClearLinks != null) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFEFEF),
                            border = BorderStroke(0.8.dp, Color(0xFFFFCDD2)),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(onClick = onAdminClearLinks)
                                .testTag("admin_clear_links_btn"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteSweep,
                                    contentDescription = null,
                                    tint = AlertCrimson,
                                    modifier = Modifier.size(13.dp),
                                )
                                Text(
                                    text = "Hapus Link",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertCrimson,
                                )
                            }
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (isReportedByMe) PendingAmberBg else Color.White,
                        border = BorderStroke(1.dp, if (isReportedByMe) Color(0xFFFDE68A) else OutlineVariantLight),
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(enabled = !isReportedByMe, onClick = onReportLink)
                            .testTag("citizen_report_link_btn"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Flag,
                                contentDescription = null,
                                tint = if (isReportedByMe) PendingAmberText else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = stringResource(
                                    if (isReportedByMe) R.string.btn_reported_suspicious_link else R.string.btn_report_suspicious_link,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isReportedByMe) PendingAmberText else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Raw URL preview box for Admin Moderation cards so Admins can inspect the exact URL before approving.
 */
@Composable
fun AdminRawLinkPreviewBadge(
    rawUrl: String,
    reportCount: Int = 0,
    onClearLink: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (rawUrl.isBlank()) return
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (reportCount > 0) PendingAmberBg else Color(0xFFF3FBF7),
        border = BorderStroke(1.dp, if (reportCount > 0) Color(0xFFFDE68A) else OutlineVariantLight),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Link,
                contentDescription = null,
                tint = if (reportCount > 0) PendingAmberText else ToskaPrimaryDeep,
                modifier = Modifier.size(16.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (reportCount > 0) {
                        "Cek Link Pengusul ($reportCount laporan warga):"
                    } else {
                        "Link Maps Pengusul (Terverifikasi HTTPS):"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (reportCount > 0) PendingAmberText else ToskaOnPrimaryContainer,
                )
                Text(
                    text = rawUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (onClearLink != null) {
                FilledTonalButton(
                    onClick = onClearLink,
                    shape = CircleShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFFFEFEF),
                        contentColor = AlertCrimson,
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text("Hapus Link", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Multi-photo picker section (1..4 photos) using Android zero-permission Photo Picker.
 */
@Composable
fun FormMultiPhotoPickerSection(
    selectedPhotoUris: List<Uri>,
    onPhotosChanged: (List<Uri>) -> Unit,
    maxPhotos: Int = 4,
    enabled: Boolean = true,
    title: String = stringResource(R.string.kuliner_multi_photo_title),
    subtitle: String = stringResource(R.string.kuliner_multi_photo_subtitle),
) {
    val multiPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxPhotos),
    ) { uris ->
        if (uris.isNotEmpty()) {
            val combined = (selectedPhotoUris + uris).distinct().take(maxPhotos)
            onPhotosChanged(combined)
        }
    }

    val singlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val combined = (selectedPhotoUris + uri).distinct().take(maxPhotos)
            onPhotosChanged(combined)
        }
    }

    val launchGallery = {
        if (enabled && selectedPhotoUris.size < maxPhotos) {
            val remaining = maxPhotos - selectedPhotoUris.size
            val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            if (remaining >= 2) {
                multiPickerLauncher.launch(request)
            } else {
                singlePickerLauncher.launch(request)
            }
        }
    }

    if (selectedPhotoUris.isEmpty()) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF3FBF7),
            border = BorderStroke(1.dp, OutlineVariantLight),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable(enabled = enabled, onClick = launchGallery)
                .testTag("form_pick_multi_photo_btn"),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MintSoftGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddAPhoto,
                        contentDescription = title,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(21.dp),
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
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = null,
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Foto Dipilih (${selectedPhotoUris.size}/$maxPhotos)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = ToskaOnPrimaryContainer,
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = OpenGreenBg,
                    ) {
                        Text(
                            text = if (selectedPhotoUris.size < maxPhotos) {
                                "Bisa tambah ${maxPhotos - selectedPhotoUris.size} lagi"
                            } else {
                                "Maksimal $maxPhotos foto"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = OpenGreenText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    itemsIndexed(selectedPhotoUris, key = { idx, uri -> "${idx}_$uri" }) { index, uri ->
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, OutlineVariantLight, RoundedCornerShape(14.dp)),
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(uri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto kuliner ${index + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .matchParentSize()
                                    .testTag("form_multi_photo_preview_$index"),
                            )

                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.58f),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(6.dp),
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .minimumInteractiveComponentSize()
                                    .clickable(enabled = enabled) {
                                        val updated = selectedPhotoUris.toMutableList().also { it.removeAt(index) }
                                        onPhotosChanged(updated)
                                    }
                                    .testTag("form_remove_multi_photo_$index"),
                                contentAlignment = Alignment.TopEnd,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xCC93000A)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Hapus foto ${index + 1}",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            }
                        }
                    }

                    if (selectedPhotoUris.size < maxPhotos) {
                        item(key = "add_more_photo_tile") {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, ToskaPrimaryDeep.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(enabled = enabled, onClick = launchGallery)
                                    .testTag("form_add_more_photo_btn"),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = stringResource(R.string.kuliner_add_more_photo),
                                        tint = ToskaPrimaryDeep,
                                        modifier = Modifier.size(24.dp),
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(R.string.kuliner_add_more_photo),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ToskaPrimaryDeep,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(6.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1FAF5))
            .border(1.dp, OutlineVariantLight, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag("ai_photo_safety_notice"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.VerifiedUser,
            contentDescription = null,
            tint = ToskaPrimaryDeep,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = stringResource(R.string.ai_photo_safety_form_notice),
            style = MaterialTheme.typography.labelSmall,
            color = ToskaOnPrimaryContainer,
        )
    }
}

/**
 * Swipeable HorizontalPager carousel for 1..4 photos with Layer-2 Auto-Blur Shield
 * when reported by citizens as inappropriate/sensitive.
 */
@Composable
fun CulinaryPhotoCarousel(
    photoUrls: List<String>,
    fallbackImageKey: String,
    spotName: String,
    modifier: Modifier = Modifier,
    compactMode: Boolean = false,
    isBlurredSensitive: Boolean = false,
    sensitiveReportCount: Int = 0,
    onPhotoClick: (pageIndex: Int) -> Unit,
) {
    val cleanUrls = remember(photoUrls) { photoUrls.filter { it.isNotBlank() } }
    val pageCount = cleanUrls.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount })
    var isManuallyRevealed by remember(photoUrls, isBlurredSensitive) { mutableStateOf(false) }
    val showBlurShield = isBlurredSensitive && !isManuallyRevealed

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            userScrollEnabled = !showBlurShield,
            modifier = Modifier
                .fillMaxSize()
                .then(if (showBlurShield) Modifier.blur(24.dp) else Modifier),
        ) { page ->
            val currentUrl = cleanUrls.getOrNull(page).orEmpty()
            SurabayaRemoteOrFallbackImage(
                imageUrl = currentUrl,
                fallbackImageKey = fallbackImageKey,
                contentDescription = "$spotName - Foto ${page + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = !showBlurShield) { onPhotoClick(page) },
            )
        }

        if (showBlurShield) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xDE0B1F1A))
                    .clickable(enabled = true, onClick = {})
                    .padding(if (compactMode) 6.dp else 12.dp)
                    .testTag("sensitive_photo_blur_shield"),
                contentAlignment = Alignment.Center,
            ) {
                if (compactMode) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = Color(0xFFFDE68A),
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = stringResource(R.string.sensitive_blur_compact_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { isManuallyRevealed = true }
                                .testTag("reveal_sensitive_photo_btn"),
                        ) {
                            Text(
                                text = "Lihat",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFFFDE68A),
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = if (sensitiveReportCount > 0) {
                                    "Foto Disamarkan ($sensitiveReportCount laporan warga)"
                                } else {
                                    stringResource(R.string.sensitive_blur_title)
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = stringResource(R.string.sensitive_blur_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(CircleShape)
                                .clickable { isManuallyRevealed = true }
                                .testTag("reveal_sensitive_photo_btn"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Visibility,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = stringResource(R.string.btn_reveal_sensitive_photo),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }
        } else {
            if (isBlurredSensitive && !compactMode) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .clickable { isManuallyRevealed = false }
                        .testTag("rehide_sensitive_photo_btn"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = stringResource(R.string.btn_rehide_sensitive_photo),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                    }
                }
            }

            if (cleanUrls.size > 1) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.60f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(if (compactMode) 5.dp else 8.dp),
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${cleanUrls.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(
                            horizontal = if (compactMode) 6.dp else 8.dp,
                            vertical = if (compactMode) 1.dp else 3.dp,
                        ),
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (compactMode) 5.dp else 8.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.38f))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    for (i in cleanUrls.indices) {
                        val active = i == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(
                                    width = if (active) (if (compactMode) 10.dp else 14.dp) else (if (compactMode) 5.dp else 6.dp),
                                    height = if (compactMode) 5.dp else 6.dp,
                                )
                                .clip(CircleShape)
                                .background(if (active) Color.White else Color.White.copy(alpha = 0.50f)),
                        )
                    }
                }
            } else if (!compactMode) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.50f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OpenInFull,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = "Perbesar",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen Lightbox Photo Viewer with 2-finger pinch-to-zoom, double-tap zoom, pan,
 * and smooth left/right HorizontalPager swipe across all uploaded culinary photos.
 */
@Composable
fun FullscreenZoomablePhotoViewerDialog(
    photoUrls: List<String>,
    fallbackImageKey: String,
    title: String,
    initialPage: Int = 0,
    onDismiss: () -> Unit,
) {
    val cleanUrls = remember(photoUrls) { photoUrls.filter { it.isNotBlank() } }
    val pageCount = cleanUrls.size.coerceAtLeast(1)
    val safeInitial = initialPage.coerceIn(0, pageCount - 1)
    val pagerState = rememberPagerState(initialPage = safeInitial, pageCount = { pageCount })

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
                .testTag("fullscreen_photo_viewer"),
        ) {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                ZoomablePhotoPage(
                    imageUrl = cleanUrls.getOrNull(page).orEmpty(),
                    fallbackImageKey = fallbackImageKey,
                    contentDescription = "$title (${page + 1}/$pageCount)",
                    isCurrentPage = pagerState.currentPage == page,
                )
            }

            // Top bar: Title + Page Counter + Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Foto ${pagerState.currentPage + 1} dari $pageCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.80f),
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                        .testTag("fullscreen_photo_close_btn"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.photo_viewer_close_cd),
                        tint = Color.White,
                    )
                }
            }

            // Bottom Hint + Page Dots
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (pageCount > 1) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        for (i in 0 until pageCount) {
                            val selected = i == pagerState.currentPage
                            Box(
                                modifier = Modifier
                                    .size(width = if (selected) 18.dp else 7.dp, height = 7.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) Color.White else Color.White.copy(alpha = 0.45f)),
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.photo_viewer_zoom_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.78f),
                )
            }
        }
    }
}

@Composable
private fun ZoomablePhotoPage(
    imageUrl: String,
    fallbackImageKey: String,
    contentDescription: String,
    isCurrentPage: Boolean,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(isCurrentPage) {
        if (!isCurrentPage && scale != 1f) {
            scale = 1f
            offset = Offset.Zero
        }
    }

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
                            val newScale = (scale * zoomChange).coerceIn(1f, 4.5f)
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
                            val nextX = offset.x + panChange.x
                            val nextY = offset.y + panChange.y
                            val clampedX = nextX.coerceIn(-maxX, maxX)
                            val clampedY = nextY.coerceIn(-maxY, maxY)
                            val hitHorizontalBoundary = abs(nextX - clampedX) > 0.5f && abs(panChange.x) > abs(panChange.y)

                            offset = Offset(clampedX, clampedY)

                            // If user pans horizontally past the zoomed edge, let HorizontalPager swipe to next/prev photo
                            if (!hitHorizontalBoundary) {
                                event.changes.forEach { change ->
                                    if (change.positionChanged()) {
                                        change.consume()
                                    }
                                }
                            }
                        }
                        // When pressedCount == 1 && scale <= 1.02f, do NOT consume so HorizontalPager swipes freely!
                    } while (event.changes.any { it.pressed })

                    if (scale < 1.02f) {
                        scale = 1f
                        offset = Offset.Zero
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        SurabayaRemoteOrFallbackImage(
            imageUrl = imageUrl,
            fallbackImageKey = fallbackImageKey,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
    }
}

/**
 * Branded online food delivery action chips (GoFood, GrabFood, ShopeeFood) rendered only when safe links exist.
 * Always shows a SafeExternalLinkConfirmationDialog before leaving the app.
 */
@Composable
fun CulinaryDeliveryLinksRow(
    spot: CulinarySpot,
    compact: Boolean = false,
    isReportedByMe: Boolean = false,
    onReportSuspiciousLink: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var pendingDeliveryLink by remember { mutableStateOf<PendingExternalLink?>(null) }

    val activePending = pendingDeliveryLink
    if (activePending != null) {
        SafeExternalLinkConfirmationDialog(
            pendingLink = activePending,
            isReportedByMe = isReportedByMe,
            onReportSuspicious = onReportSuspiciousLink,
            onDismiss = { pendingDeliveryLink = null },
            onConfirmOpen = {
                val target = activePending
                pendingDeliveryLink = null
                openExternalUrlSafely(context, target.url)
            },
        )
    }

    val gofoodPending = remember(spot.safeGofoodUrl, spot.name) {
        LinkSecurityValidator.inspectForConfirmation(spot.safeGofoodUrl, "GoFood • ${spot.name}", SafeLinkCategory.GOFOOD)
    }
    val grabfoodPending = remember(spot.safeGrabfoodUrl, spot.name) {
        LinkSecurityValidator.inspectForConfirmation(spot.safeGrabfoodUrl, "GrabFood • ${spot.name}", SafeLinkCategory.GRABFOOD)
    }
    val shopeefoodPending = remember(spot.safeShopeefoodUrl, spot.name) {
        LinkSecurityValidator.inspectForConfirmation(spot.safeShopeefoodUrl, "ShopeeFood • ${spot.name}", SafeLinkCategory.SHOPEEFOOD)
    }

    if (gofoodPending == null && grabfoodPending == null && shopeefoodPending == null) return

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (gofoodPending != null) {
            DeliveryPlatformChip(
                label = "GoFood",
                icon = Icons.Filled.DeliveryDining,
                bgColor = Color(0xFFFFEBEE),
                borderColor = Color(0xFFEF9A9A),
                contentColor = Color(0xFFC62828),
                compact = compact,
                testTag = "delivery_gofood_${spot.id}",
                onClick = { pendingDeliveryLink = gofoodPending },
            )
        }
        if (grabfoodPending != null) {
            DeliveryPlatformChip(
                label = "GrabFood",
                icon = Icons.Filled.Storefront,
                bgColor = Color(0xFFE8F5E9),
                borderColor = Color(0xFFA5D6A7),
                contentColor = Color(0xFF00880C),
                compact = compact,
                testTag = "delivery_grabfood_${spot.id}",
                onClick = { pendingDeliveryLink = grabfoodPending },
            )
        }
        if (shopeefoodPending != null) {
            DeliveryPlatformChip(
                label = "ShopeeFood",
                icon = Icons.Filled.ShoppingBag,
                bgColor = Color(0xFFFBE9E7),
                borderColor = Color(0xFFFFAB91),
                contentColor = Color(0xFFD84315),
                compact = compact,
                testTag = "delivery_shopeefood_${spot.id}",
                onClick = { pendingDeliveryLink = shopeefoodPending },
            )
        }
    }
}

@Composable
private fun DeliveryPlatformChip(
    label: String,
    icon: ImageVector,
    bgColor: Color,
    borderColor: Color,
    contentColor: Color,
    compact: Boolean,
    testTag: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .then(if (!compact) Modifier.minimumInteractiveComponentSize() else Modifier)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 8.dp else 11.dp,
                vertical = if (compact) 3.dp else 6.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(if (compact) 12.dp else 15.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
        }
    }
}

/**
 * Layer 3 & Layer 4 UGC Safety Row:
 * - Citizen 1-tap "Lapor Asusila" (1 report = auto-blur, 3 reports = auto-quarantine)
 * - Citizen "Blokir Penulis" (Google Play UGC compliance)
 * - Admin 1-click "Hapus Permanen", "Pulihkan Blur", and "Bekukan Akun"
 */
@Composable
fun PostContentSafetyRow(
    authorId: String,
    authorName: String,
    currentUserId: String,
    contentReportCount: Int,
    isContentReportedByMe: Boolean,
    isAdmin: Boolean,
    onReportContent: () -> Unit,
    onBlockAuthor: (() -> Unit)? = null,
    onAdminDeletePost: (() -> Unit)? = null,
    onAdminRestoreBlur: (() -> Unit)? = null,
    onAdminBanAuthor: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var showReportDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBanDialog by remember { mutableStateOf(false) }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            shape = RoundedCornerShape(22.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "Laporkan Konten Asusila / Tidak Pantas?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "1 laporan warga akan langsung menyamarkan (blur) foto kiriman ini, dan 3 laporan warga otomatis menyembunyikan kiriman dari feed publik sembari menunggu tindakan Admin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("report_content_confirm_dialog"),
                )
            },
            dismissButton = {
                OutlinedButton(onClick = { showReportDialog = false }, shape = CircleShape) {
                    Text(stringResource(R.string.safe_exit_btn_cancel))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReportDialog = false
                        onReportContent()
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCrimson),
                    modifier = Modifier.testTag("confirm_report_content_btn"),
                ) {
                    Text("Kirim Laporan")
                }
            },
        )
    }

    if (showBlockDialog && onBlockAuthor != null) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            shape = RoundedCornerShape(22.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "Blokir ${authorName.ifBlank { "Pengguna Ini" }}?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "Semua laporan kejadian, event, kuliner, komentar, profil sekitar, dan pesan chat dari pengguna ini akan langsung disembunyikan dari layar Anda. Anda dapat membuka blokir kapan saja di menu Profil.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("block_user_confirm_dialog"),
                )
            },
            dismissButton = {
                OutlinedButton(onClick = { showBlockDialog = false }, shape = CircleShape) {
                    Text(stringResource(R.string.safe_exit_btn_cancel))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBlockDialog = false
                        onBlockAuthor()
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCrimson),
                    modifier = Modifier.testTag("confirm_block_user_btn"),
                ) {
                    Text("Blokir Sekarang")
                }
            },
        )
    }

    if (showDeleteDialog && onAdminDeletePost != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(22.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "Hapus Permanen Kiriman Ini?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "Kiriman beserta seluruh fotonya akan dihapus permanen dari server PocketBase.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }, shape = CircleShape) {
                    Text(stringResource(R.string.safe_exit_btn_cancel))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onAdminDeletePost()
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCrimson),
                    modifier = Modifier.testTag("confirm_admin_delete_btn"),
                ) {
                    Text("Hapus Permanen")
                }
            },
        )
    }

    if (showBanDialog && onAdminBanAuthor != null) {
        AlertDialog(
            onDismissRequest = { showBanDialog = false },
            shape = RoundedCornerShape(22.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "Bekukan Akun ${authorName.ifBlank { "Pelanggar" }}?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "Akun pelanggar tidak dapat lagi mengunggah foto/kiriman, disembunyikan dari Teman Sekitar, dan seluruh kirimannya dibersihkan dari feed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            dismissButton = {
                OutlinedButton(onClick = { showBanDialog = false }, shape = CircleShape) {
                    Text(stringResource(R.string.safe_exit_btn_cancel))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBanDialog = false
                        onAdminBanAuthor()
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCrimson),
                    modifier = Modifier.testTag("confirm_admin_ban_btn"),
                ) {
                    Text("Bekukan Pelanggar")
                }
            },
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (isAdmin && contentReportCount >= 3) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFEBEE),
                border = BorderStroke(1.dp, Color(0xFFEF9A9A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_quarantined_post_banner"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.ReportProblem,
                        contentDescription = null,
                        tint = AlertCrimson,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(R.string.quarantined_content_admin_badge),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AlertCrimson,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Citizen Report Asusila Pill
            Surface(
                shape = CircleShape,
                color = if (isContentReportedByMe) PendingAmberBg else Color(0xFFFFF8F6),
                border = BorderStroke(1.dp, if (isContentReportedByMe) Color(0xFFFDE68A) else Color(0xFFFFCDD2)),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = !isContentReportedByMe) { showReportDialog = true }
                    .testTag("citizen_report_content_btn"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.ReportProblem,
                        contentDescription = null,
                        tint = if (isContentReportedByMe) PendingAmberText else AlertCrimson,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = if (isContentReportedByMe) {
                            "${stringResource(R.string.btn_reported_inappropriate_content)} ($contentReportCount/3)"
                        } else if (contentReportCount > 0) {
                            "${stringResource(R.string.btn_report_inappropriate_content)} ($contentReportCount/3)"
                        } else {
                            stringResource(R.string.btn_report_inappropriate_content)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isContentReportedByMe) PendingAmberText else AlertCrimson,
                    )
                }
            }

            // Block Author Pill (when not own post)
            if (authorId.isNotBlank() && authorId != currentUserId && onBlockAuthor != null) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF4F6F5),
                    border = BorderStroke(1.dp, OutlineVariantLight),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { showBlockDialog = true }
                        .testTag("block_author_btn"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Block,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = stringResource(R.string.btn_block_author),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Admin 1-Click Delete Post
            if (isAdmin && onAdminDeletePost != null) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFEBEE),
                    border = BorderStroke(1.dp, Color(0xFFEF9A9A)),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { showDeleteDialog = true }
                        .testTag("admin_delete_post_btn"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteForever,
                            contentDescription = null,
                            tint = AlertCrimson,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = stringResource(R.string.btn_admin_delete_post),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AlertCrimson,
                        )
                    }
                }
            }

            // Admin Restore False-Positive Blur
            if (isAdmin && contentReportCount > 0 && onAdminRestoreBlur != null) {
                Surface(
                    shape = CircleShape,
                    color = OpenGreenBg,
                    border = BorderStroke(1.dp, OutlineVariantLight),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onAdminRestoreBlur)
                        .testTag("admin_restore_blur_btn"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Restore,
                            contentDescription = null,
                            tint = OpenGreenText,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = stringResource(R.string.btn_admin_restore_blur),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OpenGreenText,
                        )
                    }
                }
            }

            // Admin Freeze Offending Account
            if (isAdmin && authorId.isNotBlank() && authorId != currentUserId && onAdminBanAuthor != null) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF3E0007),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { showBanDialog = true }
                        .testTag("admin_ban_author_btn"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Gavel,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = stringResource(R.string.btn_admin_ban_author),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}
