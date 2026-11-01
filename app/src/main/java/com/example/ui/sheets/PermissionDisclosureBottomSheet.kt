package com.example.ui.sheets

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.theme.FreshGreenGradient
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.PendingAmberBg
import com.example.ui.theme.PendingAmberText
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep

private const val PERMISSION_PREFS_NAME = "arek_surabaya_permission_disclosure_prefs"
private const val KEY_NOTIF_DISCLOSURE_SHOWN = "notif_disclosure_shown_v1"

enum class LocationUsageContext(val badgeLabel: String) {
    GENERAL_NEARBY("Fitur Terdekat Beranda • Kuliner & Tambal Ban"),
    KULINER("Urutan Jarak Kuliner & Warung Terdekat"),
    TAMBAL_BAN("Pencarian & Titik Darurat Tambal Ban Terdekat"),
    INCIDENT_REPORT("Titik Koordinat Akurat Laporan Kejadian Warga"),
    NEARBY_CITIZENS("Estimasi Jarak Kecamatan Teman Sekitar"),
}

sealed interface PermissionSheetMode {
    data class Location(
        val usageContext: LocationUsageContext = LocationUsageContext.GENERAL_NEARBY,
        val showSettingsShortcut: Boolean = false,
    ) : PermissionSheetMode

    data class Notification(
        val showSettingsShortcut: Boolean = false,
    ) : PermissionSheetMode

    data object Overview : PermissionSheetMode
}

fun hasAnyLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

fun hasSeenInitialNotificationDisclosure(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PERMISSION_PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_NOTIF_DISCLOSURE_SHOWN, false)
}

fun markInitialNotificationDisclosureSeen(context: Context) {
    context.getSharedPreferences(PERMISSION_PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_NOTIF_DISCLOSURE_SHOWN, true)
        .apply()
}

fun openAppSystemPermissionSettings(context: Context) {
    try {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // Ignore if Settings activity is unavailable in test environment
    }
}

private tailrec fun Context.findHostActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findHostActivity()
    else -> null
}

/**
 * Reusable controller that shows a slide-up Material 3 Bottom Sheet explaining why location
 * permission is needed BEFORE triggering the Android runtime permission prompt.
 * If permission is already granted, [onPermissionGranted] executes immediately.
 */
@Composable
fun rememberLocationPermissionWithDisclosure(
    usageContext: LocationUsageContext,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit = {},
): () -> Unit {
    val context = LocalContext.current
    var activeSheetMode by remember { mutableStateOf<PermissionSheetMode.Location?>(null) }
    var launchTimestampMs by remember { mutableLongStateOf(0L) }

    val systemPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
            val elapsed = SystemClock.elapsedRealtime() - launchTimestampMs
            val hostActivity = context.findHostActivity()
            val canShowRationale = hostActivity != null && (
                ActivityCompat.shouldShowRequestPermissionRationale(
                    hostActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    hostActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
                )
            if (hostActivity != null && elapsed in 1..280 && !canShowRationale) {
                activeSheetMode = PermissionSheetMode.Location(
                    usageContext = usageContext,
                    showSettingsShortcut = true,
                )
            }
        }
    }

    val currentSheet = activeSheetMode
    if (currentSheet != null) {
        PermissionDisclosureBottomSheet(
            mode = currentSheet,
            onDismiss = {
                activeSheetMode = null
                onPermissionDenied()
            },
            onConfirmLocationRequest = {
                activeSheetMode = null
                launchTimestampMs = SystemClock.elapsedRealtime()
                systemPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            },
        )
    }

    return remember(context, usageContext, onPermissionGranted) {
        {
            if (hasAnyLocationPermission(context)) {
                onPermissionGranted()
            } else {
                activeSheetMode = PermissionSheetMode.Location(
                    usageContext = usageContext,
                    showSettingsShortcut = false,
                )
            }
        }
    }
}

/**
 * Reusable controller that shows a slide-up Material 3 Bottom Sheet explaining why notification
 * permission is needed BEFORE triggering Android 13+ POST_NOTIFICATIONS runtime prompt.
 */
@Composable
fun rememberNotificationPermissionWithDisclosure(
    onPermissionGranted: () -> Unit = {},
    onPermissionDenied: () -> Unit = {},
): () -> Unit {
    val context = LocalContext.current
    var activeSheetMode by remember { mutableStateOf<PermissionSheetMode.Notification?>(null) }
    var launchTimestampMs by remember { mutableLongStateOf(0L) }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val elapsed = SystemClock.elapsedRealtime() - launchTimestampMs
                val hostActivity = context.findHostActivity()
                val canShowRationale = hostActivity != null &&
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        hostActivity,
                        Manifest.permission.POST_NOTIFICATIONS,
                    )
                if (hostActivity != null && elapsed in 1..280 && !canShowRationale) {
                    activeSheetMode = PermissionSheetMode.Notification(showSettingsShortcut = true)
                }
            }
        }
    }

    val currentSheet = activeSheetMode
    if (currentSheet != null) {
        PermissionDisclosureBottomSheet(
            mode = currentSheet,
            onDismiss = {
                markInitialNotificationDisclosureSeen(context)
                activeSheetMode = null
                onPermissionDenied()
            },
            onConfirmNotificationRequest = {
                markInitialNotificationDisclosureSeen(context)
                activeSheetMode = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launchTimestampMs = SystemClock.elapsedRealtime()
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    onPermissionGranted()
                }
            },
        )
    }

    return remember(context, onPermissionGranted) {
        {
            if (hasNotificationPermission(context)) {
                onPermissionGranted()
            } else {
                activeSheetMode = PermissionSheetMode.Notification(showSettingsShortcut = false)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionDisclosureBottomSheet(
    mode: PermissionSheetMode,
    onDismiss: () -> Unit,
    onConfirmLocationRequest: () -> Unit = {},
    onConfirmNotificationRequest: () -> Unit = {},
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var locationGranted by remember { mutableStateOf(hasAnyLocationPermission(context)) }
    var notificationGranted by remember { mutableStateOf(hasNotificationPermission(context)) }

    val internalLocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        locationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted) {
            onConfirmLocationRequest()
        }
    }

    val internalNotifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        notificationGranted = granted
        markInitialNotificationDisclosureSeen(context)
        if (granted) {
            onConfirmNotificationRequest()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White,
        tonalElevation = 0.dp,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                width = 42.dp,
                height = 5.dp,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        },
        modifier = Modifier.testTag("permission_disclosure_sheet"),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                when (mode) {
                    is PermissionSheetMode.Location -> {
                        LocationDisclosureContent(
                            usageContext = mode.usageContext,
                            showSettingsShortcut = mode.showSettingsShortcut,
                            onConfirm = onConfirmLocationRequest,
                            onOpenSettings = {
                                onDismiss()
                                openAppSystemPermissionSettings(context)
                            },
                            onLater = onDismiss,
                        )
                    }

                    is PermissionSheetMode.Notification -> {
                        NotificationDisclosureContent(
                            showSettingsShortcut = mode.showSettingsShortcut,
                            onConfirm = onConfirmNotificationRequest,
                            onOpenSettings = {
                                onDismiss()
                                openAppSystemPermissionSettings(context)
                            },
                            onLater = onDismiss,
                        )
                    }

                    PermissionSheetMode.Overview -> {
                        OverviewDisclosureContent(
                            isLocationGranted = locationGranted,
                            isNotificationGranted = notificationGranted,
                            onEnableLocation = {
                                internalLocLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                    ),
                                )
                            },
                            onEnableNotification = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    internalNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            onOpenSettings = {
                                onDismiss()
                                openAppSystemPermissionSettings(context)
                            },
                            onClose = onDismiss,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun LocationDisclosureContent(
    usageContext: LocationUsageContext,
    showSettingsShortcut: Boolean,
    onConfirm: () -> Unit,
    onOpenSettings: () -> Unit,
    onLater: () -> Unit,
) {
    // Header Row with Fresh Gradient Icon + Contextual Badge
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(FreshGreenGradient, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.perm_sheet_location_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Surface(
                shape = CircleShape,
                color = OpenGreenBg,
                border = BorderStroke(1.dp, OpenGreenText.copy(alpha = 0.25f)),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        imageVector = when (usageContext) {
                            LocationUsageContext.TAMBAL_BAN -> Icons.Filled.Build
                            LocationUsageContext.KULINER -> Icons.Filled.Restaurant
                            LocationUsageContext.INCIDENT_REPORT -> Icons.Filled.ReportProblem
                            LocationUsageContext.NEARBY_CITIZENS -> Icons.Filled.People
                            LocationUsageContext.GENERAL_NEARBY -> Icons.Filled.GpsFixed
                        },
                        contentDescription = null,
                        tint = OpenGreenText,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = usageContext.badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OpenGreenText,
                    )
                }
            }
        }
    }

    Text(
        text = stringResource(R.string.perm_sheet_location_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    // 3 Clear Feature Usage Items
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineVariantLight),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PermissionFeatureRow(
                icon = Icons.Filled.Build,
                title = stringResource(R.string.perm_loc_point_nearby_title),
                description = stringResource(R.string.perm_loc_point_nearby_desc),
                highlighted = usageContext == LocationUsageContext.TAMBAL_BAN ||
                    usageContext == LocationUsageContext.KULINER ||
                    usageContext == LocationUsageContext.GENERAL_NEARBY,
            )
            HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.6f))
            PermissionFeatureRow(
                icon = Icons.Filled.LocationOn,
                title = stringResource(R.string.perm_loc_point_incident_title),
                description = stringResource(R.string.perm_loc_point_incident_desc),
                highlighted = usageContext == LocationUsageContext.INCIDENT_REPORT,
            )
            HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.6f))
            PermissionFeatureRow(
                icon = Icons.Filled.People,
                title = stringResource(R.string.perm_loc_point_social_title),
                description = stringResource(R.string.perm_loc_point_social_desc),
                highlighted = usageContext == LocationUsageContext.NEARBY_CITIZENS,
            )
        }
    }

    // Data Minimization & No Background Tracking Guarantee Banner
    PrivacyGuaranteeBanner(
        text = stringResource(R.string.perm_loc_privacy_guarantee),
    )

    if (showSettingsShortcut) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = PendingAmberBg,
            border = BorderStroke(1.dp, PendingAmberText.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.perm_settings_blocked_hint),
                style = MaterialTheme.typography.labelMedium,
                color = PendingAmberText,
                modifier = Modifier.padding(12.dp),
            )
        }

        Button(
            onClick = onOpenSettings,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = ToskaPrimaryDeep,
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_permission_open_settings"),
        ) {
            Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.perm_btn_open_settings),
                fontWeight = FontWeight.Bold,
            )
        }
    } else {
        Button(
            onClick = onConfirm,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = ToskaPrimary,
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_permission_sheet_continue"),
        ) {
            Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.perm_btn_allow_location),
                fontWeight = FontWeight.Bold,
            )
        }
    }

    OutlinedButton(
        onClick = onLater,
        shape = CircleShape,
        border = BorderStroke(1.dp, OutlineVariantLight),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("btn_permission_sheet_later"),
    ) {
        Text(
            text = stringResource(R.string.perm_btn_later),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun NotificationDisclosureContent(
    showSettingsShortcut: Boolean,
    onConfirm: () -> Unit,
    onOpenSettings: () -> Unit,
    onLater: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(FreshGreenGradient, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.perm_sheet_notification_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Info Chat Warga • Kejadian Sekitar • Radio 100 FM",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = ToskaPrimaryDeep,
            )
        }
    }

    Text(
        text = stringResource(R.string.perm_sheet_notification_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineVariantLight),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PermissionFeatureRow(
                icon = Icons.Filled.ChatBubble,
                title = stringResource(R.string.perm_notif_point_chat_title),
                description = stringResource(R.string.perm_notif_point_chat_desc),
                highlighted = true,
            )
            HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.6f))
            PermissionFeatureRow(
                icon = Icons.Filled.ReportProblem,
                title = stringResource(R.string.perm_notif_point_incident_title),
                description = stringResource(R.string.perm_notif_point_incident_desc),
                highlighted = true,
            )
            HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.6f))
            PermissionFeatureRow(
                icon = Icons.Filled.Radio,
                title = stringResource(R.string.perm_notif_point_radio_title),
                description = stringResource(R.string.perm_notif_point_radio_desc),
                highlighted = false,
            )
        }
    }

    PrivacyGuaranteeBanner(
        text = stringResource(R.string.perm_notif_privacy_guarantee),
    )

    if (showSettingsShortcut) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = PendingAmberBg,
            border = BorderStroke(1.dp, PendingAmberText.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.perm_settings_blocked_hint),
                style = MaterialTheme.typography.labelMedium,
                color = PendingAmberText,
                modifier = Modifier.padding(12.dp),
            )
        }

        Button(
            onClick = onOpenSettings,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = ToskaPrimaryDeep,
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_permission_open_settings"),
        ) {
            Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.perm_btn_open_settings),
                fontWeight = FontWeight.Bold,
            )
        }
    } else {
        Button(
            onClick = onConfirm,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = ToskaPrimary,
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_permission_sheet_continue"),
        ) {
            Icon(Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.perm_btn_allow_notification),
                fontWeight = FontWeight.Bold,
            )
        }
    }

    OutlinedButton(
        onClick = onLater,
        shape = CircleShape,
        border = BorderStroke(1.dp, OutlineVariantLight),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("btn_permission_sheet_later"),
    ) {
        Text(
            text = stringResource(R.string.perm_btn_later),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun OverviewDisclosureContent(
    isLocationGranted: Boolean,
    isNotificationGranted: Boolean,
    onEnableLocation: () -> Unit,
    onEnableNotification: () -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(FreshGreenGradient, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(23.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.perm_sheet_overview_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.perm_sheet_overview_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    // Card 1: Location Permission Overview
    OverviewPermissionCard(
        icon = Icons.Filled.MyLocation,
        title = stringResource(R.string.perm_sheet_location_title),
        isGranted = isLocationGranted,
        bullets = listOf(
            "Menghitung jarak lokasi Tambal Ban, Kuliner, & Destinasi terdekat.",
            "Menandai kecamatan & titik akurat pada Laporan Kejadian warga.",
            "Estimasi jarak kecamatan di Teman Sekitar (mendukung Mode Privat).",
            "Hanya diakses saat digunakan (tidak pernah melacak di latar belakang).",
        ),
        actionLabel = "Aktifkan GPS",
        actionTestTag = "btn_overview_enable_location",
        onEnable = onEnableLocation,
    )

    // Card 2: Notification Permission Overview
    OverviewPermissionCard(
        icon = Icons.Filled.NotificationsActive,
        title = stringResource(R.string.perm_sheet_notification_title),
        isGranted = isNotificationGranted,
        bullets = listOf(
            "Pemberitahuan balasan pesan pribadi & Ruang Publik SBY.",
            "Peringatan laporan kejadian penting/darurat & status usulan event.",
            "Kontrol putar/jeda siaran Radio Suara Surabaya 100 FM di latar depan.",
        ),
        actionLabel = "Aktifkan Notifikasi",
        actionTestTag = "btn_overview_enable_notification",
        onEnable = onEnableNotification,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedButton(
            onClick = onOpenSettings,
            shape = CircleShape,
            border = BorderStroke(1.dp, OutlineVariantLight),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("btn_permission_open_settings"),
        ) {
            Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Pengaturan HP", style = MaterialTheme.typography.labelLarge)
        }

        Button(
            onClick = onClose,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = ToskaPrimary,
                contentColor = Color.White,
            ),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("btn_permission_sheet_close"),
        ) {
            Text(stringResource(R.string.perm_btn_close), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OverviewPermissionCard(
    icon: ImageVector,
    title: String,
    isGranted: Boolean,
    bullets: List<String>,
    actionLabel: String,
    actionTestTag: String,
    onEnable: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isGranted) OpenGreenText.copy(alpha = 0.3f) else OutlineVariantLight,
        ),
        modifier = Modifier.fillMaxWidth(),
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (isGranted) OpenGreenBg else PendingAmberBg,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (isGranted) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = OpenGreenText,
                                modifier = Modifier.size(13.dp),
                            )
                        }
                        Text(
                            text = stringResource(
                                if (isGranted) R.string.perm_status_granted else R.string.perm_status_not_granted,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isGranted) OpenGreenText else PendingAmberText,
                        )
                    }
                }
            }

            bullets.forEach { line ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = ToskaPrimaryDeep,
                    )
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (!isGranted) {
                Button(
                    onClick = onEnable,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ToskaPrimary,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(top = 4.dp)
                        .testTag(actionTestTag),
                ) {
                    Text(actionLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PermissionFeatureRow(
    icon: ImageVector,
    title: String,
    description: String,
    highlighted: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    if (highlighted) OpenGreenBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (highlighted) OpenGreenText else ToskaPrimaryDeep,
                modifier = Modifier.size(19.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ToskaOnPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PrivacyGuaranteeBanner(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MintSoftGradient),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = ToskaPrimaryDeep,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(18.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = ToskaOnPrimaryContainer,
            )
        }
    }
}
