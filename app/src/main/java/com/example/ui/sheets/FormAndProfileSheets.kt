package com.example.ui.sheets

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.TextButton
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.minimumInteractiveComponentSize
import com.example.data.model.BookmarkItem
import com.example.data.model.DestinationDraft
import com.example.data.model.EventDraft
import com.example.data.model.NearbyProfile
import com.example.data.model.SurabayaDestination
import com.example.data.model.SurabayaEvent
import com.example.data.model.UserAccount
import com.example.data.model.UserMoment
import com.example.data.model.POST_TYPE_MOMEN
import com.example.data.model.POST_TYPE_LENSA_WARGA
import com.example.data.model.effectiveCitizenDisplayName
import com.example.data.model.formatCitizenGenderLabel
import com.example.data.model.isValidCitizenDisplayName
import com.example.data.model.normalizeCitizenGender
import com.example.data.pocketbase.PocketBaseClient
import com.example.data.util.DestinationDraftManager
import com.example.data.util.EventDraftManager
import com.example.data.util.LinkSecurityValidator
import com.example.data.util.LinkValidationResult
import com.example.data.util.SafeLinkCategory
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import com.example.ui.components.AboutAppDialog
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.components.FormMultiPhotoPickerSection
import com.example.ui.components.FormPhotoPickerSection
import com.example.ui.components.GoogleMapsSheetHandleHeader
import com.example.ui.components.InitialsAvatar
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.SupportMenuRow
import com.example.ui.components.SurabayaDistrictDropdown
import com.example.ui.components.rateArekSurabayaOnPlayStore
import com.example.ui.components.shareArekSurabayaApp
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import androidx.compose.runtime.rememberCoroutineScope
import com.example.data.location.LocationHelper
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun CreateIncidentSheetContent(
    defaultDistrict: String,
    isSubmitting: Boolean = false,
    isScanningPhoto: Boolean = false,
    isLiveGpsActive: Boolean = false,
    onRequestGpsRefresh: () -> Unit = {},
    onSubmit: (
        title: String,
        desc: String,
        category: String,
        severity: String,
        district: String,
        address: String,
        mapsUrl: String,
        photoUris: List<Uri>,
    ) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("LALU_LINTAS") }
    var severity by remember { mutableStateOf("INFO") }
    var district by remember { mutableStateOf(defaultDistrict) }
    var address by remember { mutableStateOf("") }
    var mapsUrl by remember { mutableStateOf("") }
    var selectedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var gpsCoordinatesApplied by remember { mutableStateOf(false) }

    val applyIncidentGps = rememberLocationPermissionWithDisclosure(
        usageContext = LocationUsageContext.INCIDENT_REPORT,
        onPermissionGranted = {
            onRequestGpsRefresh()
            coroutineScope.launch {
                val gps = LocationHelper(context).fetchCurrentLocationOrDefault()
                if (gps.isLiveGps) {
                    district = gps.nearestDistrict
                    if (mapsUrl.isBlank()) {
                        mapsUrl = String.format(
                            Locale.US,
                            "https://www.google.com/maps/search/?api=1&query=%.6f,%.6f",
                            gps.lat,
                            gps.lng,
                        )
                    }
                    gpsCoordinatesApplied = true
                }
            }
        },
    )

    val mapsValidation = remember(mapsUrl) {
        LinkSecurityValidator.validateOptionalLink(mapsUrl, SafeLinkCategory.GOOGLE_MAPS)
    }

    val isFormValid = title.trim().length >= 3 &&
        description.trim().length >= 3 &&
        address.trim().length >= 2 &&
        mapsValidation is LinkValidationResult.Valid

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GoogleMapsSheetHandleHeader(
            title = "Laporkan Kejadian Surabaya",
            subtitle = "Bagikan info jalan, cuaca, atau kondisi fasilitas kota secara akurat beserta hingga 4 foto & titik Maps.",
        )

        FormMultiPhotoPickerSection(
            selectedPhotoUris = selectedPhotoUris,
            onPhotosChanged = { selectedPhotoUris = it },
            maxPhotos = 4,
            enabled = !isSubmitting,
            title = stringResource(R.string.incident_multi_photo_title),
            subtitle = stringResource(R.string.incident_multi_photo_subtitle),
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            enabled = !isSubmitting,
            label = { Text("Judul Kejadian") },
            placeholder = { Text("Contoh: Pohon tumbang di Jl. Darmo") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_incident_title"),
        )

        Text("Kategori & Tingkat Urgensi", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(
                "LALU_LINTAS" to "Lalu Lintas",
                "CUACA_BANJIR" to "Cuaca/Banjir",
                "DARURAT" to "Darurat",
                "FASILITAS" to "Fasilitas",
                "INFO_WARGA" to "Info Warga",
            ).forEach { (key, label) ->
                FilterChip(
                    selected = category == key,
                    onClick = { if (!isSubmitting) category = key },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    shape = CircleShape,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("INFO" to "Info Biasa", "PENTING" to "Penting", "DARURAT" to "Darurat").forEach { (key, label) ->
                FilterChip(
                    selected = severity == key,
                    onClick = { if (!isSubmitting) severity = key },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    shape = CircleShape,
                )
            }
        }

        Surface(
            onClick = { if (!isSubmitting) applyIncidentGps() },
            shape = RoundedCornerShape(16.dp),
            color = if (gpsCoordinatesApplied || isLiveGpsActive) OpenGreenBg else MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                1.dp,
                if (gpsCoordinatesApplied || isLiveGpsActive) OpenGreenText.copy(alpha = 0.4f) else OutlineVariantLight,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_incident_use_gps_card"),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = null,
                    tint = if (gpsCoordinatesApplied) OpenGreenText else ToskaPrimaryDeep,
                    modifier = Modifier.size(20.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (gpsCoordinatesApplied) {
                            "Titik GPS Kejadian Terdeteksi (Kec. $district)"
                        } else {
                            stringResource(R.string.incident_use_current_gps_title)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ToskaOnPrimaryContainer,
                    )
                    Text(
                        text = stringResource(R.string.incident_use_current_gps_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        DistrictPickerChips(
            selectedDistrict = district,
            enabled = !isSubmitting,
            onSelect = { district = it },
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            enabled = !isSubmitting,
            label = { Text("Nama Jalan / Patokan Lokasi") },
            placeholder = { Text("Contoh: Depan Taman Bungkul arah Selatan") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_incident_address"),
        )

        OutlinedTextField(
            value = mapsUrl,
            onValueChange = { mapsUrl = it },
            enabled = !isSubmitting,
            isError = mapsValidation is LinkValidationResult.Invalid,
            label = { Text(stringResource(R.string.incident_maps_link_label)) },
            placeholder = { Text(stringResource(R.string.kuliner_maps_link_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Map,
                    contentDescription = null,
                    tint = ToskaPrimaryDeep,
                    modifier = Modifier.size(19.dp),
                )
            },
            trailingIcon = {
                when {
                    mapsValidation is LinkValidationResult.Invalid -> {
                        Icon(
                            imageVector = Icons.Filled.GppMaybe,
                            contentDescription = "Link tidak aman",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                    mapsUrl.isNotBlank() && mapsValidation is LinkValidationResult.Valid -> {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = "Domain resmi terverifikasi",
                            tint = OpenGreenText,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
            },
            supportingText = {
                when {
                    mapsValidation is LinkValidationResult.Invalid -> {
                        Text(
                            text = mapsValidation.errorMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    mapsUrl.isNotBlank() && mapsValidation is LinkValidationResult.Valid -> {
                        Text(
                            text = "Domain resmi Google Maps terverifikasi (${mapsValidation.host})",
                            style = MaterialTheme.typography.labelSmall,
                            color = OpenGreenText,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    else -> {
                        Text(
                            text = stringResource(R.string.safe_link_form_notice_maps),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_incident_maps_url"),
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            enabled = !isSubmitting,
            label = { Text("Kronologi / Detail Situasi") },
            shape = RoundedCornerShape(16.dp),
            minLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_incident_desc"),
        )

        if (!isFormValid && !isSubmitting) {
            Text(
                text = stringResource(R.string.form_hint_complete_fields),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Button(
            onClick = {
                onSubmit(title, description, category, severity, district, address, mapsUrl, selectedPhotoUris)
            },
            enabled = isFormValid && !isSubmitting,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_incident_btn"),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(
                        if (isScanningPhoto) R.string.scanning_photo_ai_label else R.string.submitting_upload_label,
                    ),
                )
            } else {
                Text("Publikasikan Laporan Warga")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun ProposeEventSheetContent(
    defaultDistrict: String,
    initialEvent: SurabayaEvent? = null,
    isSubmitting: Boolean = false,
    isScanningPhoto: Boolean = false,
    onSaveDraft: ((EventDraft) -> Unit)? = null,
    onClearDraft: (() -> Unit)? = null,
    onSubmit: (
        title: String,
        category: String,
        dateText: String,
        timeText: String,
        venue: String,
        district: String,
        address: String,
        price: String,
        desc: String,
        mapsUrl: String,
        photoUris: List<Uri>,
    ) -> Unit,
    onClose: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val isEditMode = initialEvent != null

    val savedDraft = remember(initialEvent) {
        if (initialEvent == null) EventDraftManager.loadDraft(context) else null
    }
    var hasLoadedDraft by remember { mutableStateOf(savedDraft != null) }

    var title by remember { mutableStateOf(initialEvent?.title ?: savedDraft?.title.orEmpty()) }
    var category by remember { mutableStateOf(initialEvent?.category ?: savedDraft?.category ?: "FESTIVAL") }
    var dateText by remember { mutableStateOf(initialEvent?.eventDateText ?: savedDraft?.dateText ?: "Minggu, 29 Nov 2026") }
    var timeText by remember { mutableStateOf(initialEvent?.timeText ?: savedDraft?.timeText ?: "18.30 - 21.30 WIB") }
    var venue by remember { mutableStateOf(initialEvent?.venueName ?: savedDraft?.venueName.orEmpty()) }
    var district by remember { mutableStateOf(initialEvent?.district ?: savedDraft?.district ?: defaultDistrict) }
    var address by remember { mutableStateOf(initialEvent?.address ?: savedDraft?.address.orEmpty()) }
    var mapsUrl by remember { mutableStateOf(initialEvent?.mapsUrl ?: savedDraft?.mapsUrl.orEmpty()) }
    var price by remember { mutableStateOf(initialEvent?.priceLabel ?: savedDraft?.priceLabel ?: "Gratis") }
    var description by remember { mutableStateOf(initialEvent?.description ?: savedDraft?.description.orEmpty()) }
    var selectedPhotoUris by remember {
        mutableStateOf<List<Uri>>(
            if (initialEvent == null && savedDraft != null) {
                savedDraft.photoUris.mapNotNull {
                    try { Uri.parse(it) } catch (_: Exception) { null }
                }
            } else emptyList()
        )
    }

    var showDraftSavedNotice by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val mapsValidation = remember(mapsUrl) {
        LinkSecurityValidator.validateOptionalLink(mapsUrl, SafeLinkCategory.GOOGLE_MAPS)
    }

    val isFormValid = title.trim().length >= 3 &&
        venue.trim().length >= 2 &&
        address.trim().length >= 2 &&
        description.trim().length >= 3 &&
        mapsValidation is LinkValidationResult.Valid

    val currentDraft = remember(title, category, dateText, timeText, venue, district, address, price, description, mapsUrl, selectedPhotoUris) {
        EventDraft(
            title = title.trim(),
            category = category,
            dateText = dateText.trim(),
            timeText = timeText.trim(),
            venueName = venue.trim(),
            district = district,
            address = address.trim(),
            priceLabel = price.trim().ifBlank { "Gratis" },
            description = description.trim(),
            mapsUrl = mapsUrl.trim(),
            photoUris = selectedPhotoUris.map { it.toString() },
        )
    }

    val handleAttemptClose: () -> Unit = {
        if (!isEditMode && currentDraft.hasContent()) {
            showUnsavedDialog = true
        } else {
            onClose?.invoke()
        }
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Simpan Draf Sebelum Keluar?") },
            text = {
                Text("Anda sudah mengisi rincian usulan event ini. Simpan sebagai draf agar dapat dilanjutkan saat mencari alamat atau tautan Maps.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        EventDraftManager.saveDraft(context, currentDraft)
                        onSaveDraft?.invoke(currentDraft)
                        showUnsavedDialog = false
                        onClose?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimary),
                ) {
                    Text("Simpan Draf & Keluar")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            EventDraftManager.clearDraft(context)
                            onClearDraft?.invoke()
                            showUnsavedDialog = false
                            onClose?.invoke()
                        }
                    ) {
                        Text("Buang", color = AlertCrimson)
                    }
                    TextButton(onClick = { showUnsavedDialog = false }) {
                        Text("Lanjut Mengisi")
                    }
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEditMode) "Edit Event Surabaya" else "Usulkan Event Surabaya",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (isEditMode) "Perbarui informasi acara, jadwal, lokasi, atau poster kegiatan warga." else "Unggah hingga 4 poster/foto acara beserta titik lokasi Google Maps. Usulan akan ditinjau Admin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (onClose != null) {
                IconButton(onClick = handleAttemptClose, modifier = Modifier.testTag("btn_close_event_form")) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Tutup",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (hasLoadedDraft && !isEditMode) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ToskaPrimary.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = null,
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Melanjutkan draf event tersimpan",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ToskaPrimaryDeep,
                        )
                    }
                    TextButton(
                        onClick = {
                            EventDraftManager.clearDraft(context)
                            onClearDraft?.invoke()
                            hasLoadedDraft = false
                            title = ""
                            category = "FESTIVAL"
                            dateText = "Minggu, 29 Nov 2026"
                            timeText = "18.30 - 21.30 WIB"
                            venue = ""
                            district = defaultDistrict
                            address = ""
                            mapsUrl = ""
                            price = "Gratis"
                            description = ""
                            selectedPhotoUris = emptyList()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text("Bersihkan Draf", color = AlertCrimson, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showDraftSavedNotice) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ToskaPrimary.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Draf usulan event berhasil disimpan! Anda aman mencari link/alamat di aplikasi lain.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ToskaPrimaryDeep,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        FormMultiPhotoPickerSection(
            selectedPhotoUris = selectedPhotoUris,
            onPhotosChanged = { selectedPhotoUris = it.take(4) },
            maxPhotos = 4,
            enabled = !isSubmitting,
            title = stringResource(R.string.event_multi_photo_title),
            subtitle = if (isEditMode) "Tambahkan foto baru untuk mengganti/melengkapi poster acara." else stringResource(R.string.event_multi_photo_subtitle),
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            enabled = !isSubmitting,
            label = { Text("Nama Event / Acara") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_event_title"),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(
                "FESTIVAL" to "Festival",
                "MUSIK_SENI" to "Musik & Seni",
                "KULINER_BAZAR" to "Bazar UMKM",
                "KOMUNITAS" to "Komunitas",
                "OLAHRAGA" to "Olahraga",
            ).forEach { (k, label) ->
                FilterChip(
                    selected = category == k,
                    onClick = { if (!isSubmitting) category = k },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    shape = CircleShape,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = dateText,
                onValueChange = { dateText = it },
                enabled = !isSubmitting,
                label = { Text("Tanggal") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = timeText,
                onValueChange = { timeText = it },
                enabled = !isSubmitting,
                label = { Text("Jam (WIB)") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = venue,
                onValueChange = { venue = it },
                enabled = !isSubmitting,
                label = { Text("Nama Tempat / Venue") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("form_event_venue"),
            )
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                enabled = !isSubmitting,
                label = { Text("Tiket") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.weight(0.8f),
            )
        }

        DistrictPickerChips(
            selectedDistrict = district,
            enabled = !isSubmitting,
            onSelect = { district = it },
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            enabled = !isSubmitting,
            label = { Text("Alamat Lengkap") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_event_address"),
        )

        OutlinedTextField(
            value = mapsUrl,
            onValueChange = { mapsUrl = it },
            enabled = !isSubmitting,
            isError = mapsValidation is LinkValidationResult.Invalid,
            label = { Text(stringResource(R.string.event_maps_link_label)) },
            placeholder = { Text(stringResource(R.string.kuliner_maps_link_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Map,
                    contentDescription = null,
                    tint = ToskaPrimaryDeep,
                    modifier = Modifier.size(19.dp),
                )
            },
            trailingIcon = {
                when {
                    mapsValidation is LinkValidationResult.Invalid -> {
                        Icon(
                            imageVector = Icons.Filled.GppMaybe,
                            contentDescription = "Link tidak aman",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                    mapsUrl.isNotBlank() && mapsValidation is LinkValidationResult.Valid -> {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = "Domain resmi terverifikasi",
                            tint = OpenGreenText,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
            },
            supportingText = {
                when {
                    mapsValidation is LinkValidationResult.Invalid -> {
                        Text(
                            text = mapsValidation.errorMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    mapsUrl.isNotBlank() && mapsValidation is LinkValidationResult.Valid -> {
                        Text(
                            text = "Domain resmi Google Maps terverifikasi (${mapsValidation.host})",
                            style = MaterialTheme.typography.labelSmall,
                            color = OpenGreenText,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    else -> {
                        Text(
                            text = stringResource(R.string.safe_link_form_notice_maps),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_event_maps_url"),
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            enabled = !isSubmitting,
            label = { Text("Deskripsi Kegiatan") },
            shape = RoundedCornerShape(16.dp),
            minLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_event_desc"),
        )

        if (!isFormValid && !isSubmitting) {
            Text(
                text = stringResource(R.string.form_hint_complete_fields),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (!isEditMode) {
            OutlinedButton(
                onClick = {
                    EventDraftManager.saveDraft(context, currentDraft)
                    onSaveDraft?.invoke(currentDraft)
                    showDraftSavedNotice = true
                },
                enabled = !isSubmitting && currentDraft.hasContent(),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_save_event_draft"),
            ) {
                Icon(Icons.Filled.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Sebagai Draf Event")
            }
        }

        Button(
            onClick = {
                onSubmit(
                    title,
                    category,
                    dateText,
                    timeText,
                    venue,
                    district,
                    address,
                    price,
                    description,
                    mapsUrl,
                    selectedPhotoUris,
                )
            },
            enabled = isFormValid && !isSubmitting,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_event_proposal_btn"),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(
                        if (isScanningPhoto) R.string.scanning_photo_ai_label else R.string.submitting_upload_label,
                    ),
                )
            } else {
                Text(if (isEditMode) "Simpan Perubahan Event" else "Kirim Usulan ke Admin")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun CreateCulinarySheetContent(
    defaultDistrict: String,
    isSubmitting: Boolean = false,
    isScanningPhoto: Boolean = false,
    isLiveGpsActive: Boolean = false,
    onRequestGpsRefresh: () -> Unit = {},
    onSubmit: (
        name: String,
        category: String,
        desc: String,
        district: String,
        address: String,
        priceRange: String,
        openHour: Int,
        closeHour: Int,
        mapsUrl: String,
        gofoodUrl: String,
        grabfoodUrl: String,
        shopeefoodUrl: String,
        photoUris: List<Uri>,
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onSubmitWithGps: (
        (
            name: String,
            category: String,
            desc: String,
            district: String,
            address: String,
            priceRange: String,
            openHour: Int,
            closeHour: Int,
            mapsUrl: String,
            gofoodUrl: String,
            grabfoodUrl: String,
            shopeefoodUrl: String,
            photoUris: List<Uri>,
            useCurrentGpsLocation: Boolean,
        ) -> Unit
    )? = null,
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("LEGENDARIS") }
    var district by remember { mutableStateOf(defaultDistrict) }
    var address by remember { mutableStateOf("") }
    var mapsUrl by remember { mutableStateOf("") }
    var priceRange by remember { mutableStateOf("Rp 18.000 - Rp 32.000") }
    var openHourStr by remember { mutableStateOf("9") }
    var closeHourStr by remember { mutableStateOf("22") }
    var description by remember { mutableStateOf("") }
    var gofoodUrl by remember { mutableStateOf("") }
    var grabfoodUrl by remember { mutableStateOf("") }
    var shopeefoodUrl by remember { mutableStateOf("") }
    var showDeliveryInputs by remember { mutableStateOf(false) }
    var selectedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var useCurrentGpsLocation by remember { mutableStateOf(isLiveGpsActive) }

    val requestCulinaryGps = rememberLocationPermissionWithDisclosure(
        usageContext = LocationUsageContext.KULINER,
        onPermissionGranted = {
            useCurrentGpsLocation = true
            onRequestGpsRefresh()
        },
        onPermissionDenied = {
            useCurrentGpsLocation = false
        },
    )

    val toggleUseCurrentGps: (Boolean) -> Unit = { checked ->
        if (!checked) {
            useCurrentGpsLocation = false
        } else {
            requestCulinaryGps()
        }
    }

    val mapsValidation = remember(mapsUrl) {
        LinkSecurityValidator.validateOptionalLink(mapsUrl, SafeLinkCategory.GOOGLE_MAPS)
    }
    val gofoodValidation = remember(gofoodUrl) {
        LinkSecurityValidator.validateOptionalLink(gofoodUrl, SafeLinkCategory.GOFOOD)
    }
    val grabfoodValidation = remember(grabfoodUrl) {
        LinkSecurityValidator.validateOptionalLink(grabfoodUrl, SafeLinkCategory.GRABFOOD)
    }
    val shopeefoodValidation = remember(shopeefoodUrl) {
        LinkSecurityValidator.validateOptionalLink(shopeefoodUrl, SafeLinkCategory.SHOPEEFOOD)
    }

    val isFormValid = name.trim().length >= 2 &&
        address.trim().length >= 2 &&
        description.trim().length >= 3 &&
        mapsValidation is LinkValidationResult.Valid &&
        gofoodValidation is LinkValidationResult.Valid &&
        grabfoodValidation is LinkValidationResult.Valid &&
        shopeefoodValidation is LinkValidationResult.Valid

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GoogleMapsSheetHandleHeader(
            title = "Rekomendasikan Kuliner Surabaya",
            subtitle = "Unggah hingga 4 foto menu/warung, titik Google Maps, serta link pesan online.",
        )

        FormMultiPhotoPickerSection(
            selectedPhotoUris = selectedPhotoUris,
            onPhotosChanged = { selectedPhotoUris = it.take(4) },
            maxPhotos = 4,
            enabled = !isSubmitting,
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            enabled = !isSubmitting,
            label = { Text("Nama Warung / Tempat Makan") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_kuliner_name"),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(
                "RAWON_SOTO" to "Rawon & Soto",
                "PENYETAN_SAMBAL" to "Penyetan",
                "LEGENDARIS" to "Legendaris",
                "KOPI_NONGKRONG" to "Kopi & Kafe",
                "JAJANAN_MALAM" to "Jajanan Malam",
            ).forEach { (k, label) ->
                FilterChip(
                    selected = category == k,
                    onClick = { if (!isSubmitting) category = k },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    shape = CircleShape,
                )
            }
        }

        DistrictPickerChips(
            selectedDistrict = district,
            enabled = !isSubmitting,
            onSelect = { district = it },
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            enabled = !isSubmitting,
            label = { Text("Alamat Jalan") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_kuliner_address"),
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (useCurrentGpsLocation) OpenGreenBg.copy(alpha = 0.65f) else Color(0xFFF6FBF8),
            border = BorderStroke(
                1.dp,
                if (useCurrentGpsLocation) OpenGreenText.copy(alpha = 0.4f) else OutlineVariantLight,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_kuliner_use_gps_card"),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isSubmitting) { toggleUseCurrentGps(!useCurrentGpsLocation) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = null,
                    tint = if (useCurrentGpsLocation) OpenGreenText else ToskaPrimaryDeep,
                    modifier = Modifier.size(20.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.kuliner_use_current_gps_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ToskaOnPrimaryContainer,
                    )
                    Text(
                        text = stringResource(R.string.kuliner_use_current_gps_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = useCurrentGpsLocation,
                    onCheckedChange = { if (!isSubmitting) toggleUseCurrentGps(it) },
                    enabled = !isSubmitting,
                    modifier = Modifier.testTag("form_kuliner_use_gps_switch"),
                )
            }
        }

        OutlinedTextField(
            value = mapsUrl,
            onValueChange = { mapsUrl = it },
            enabled = !isSubmitting,
            isError = mapsValidation is LinkValidationResult.Invalid,
            label = { Text(stringResource(R.string.kuliner_maps_link_label)) },
            placeholder = { Text(stringResource(R.string.kuliner_maps_link_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Map,
                    contentDescription = null,
                    tint = ToskaPrimaryDeep,
                    modifier = Modifier.size(19.dp),
                )
            },
            trailingIcon = {
                when {
                    mapsValidation is LinkValidationResult.Invalid -> {
                        Icon(
                            imageVector = Icons.Filled.GppMaybe,
                            contentDescription = "Link tidak aman",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                    mapsUrl.isNotBlank() && mapsValidation is LinkValidationResult.Valid -> {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = "Domain resmi terverifikasi",
                            tint = OpenGreenText,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
            },
            supportingText = {
                when {
                    mapsValidation is LinkValidationResult.Invalid -> {
                        Text(
                            text = mapsValidation.errorMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    mapsUrl.isNotBlank() && mapsValidation is LinkValidationResult.Valid -> {
                        Text(
                            text = "Domain resmi Google Maps terverifikasi (${mapsValidation.host})",
                            style = MaterialTheme.typography.labelSmall,
                            color = OpenGreenText,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    else -> {
                        Text(
                            text = stringResource(R.string.safe_link_form_notice_maps),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_kuliner_maps_url"),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = priceRange,
                onValueChange = { priceRange = it },
                enabled = !isSubmitting,
                label = { Text("Kisaran Harga") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.weight(1.3f),
            )
            OutlinedTextField(
                value = openHourStr,
                onValueChange = { openHourStr = it.filter(Char::isDigit).take(2) },
                enabled = !isSubmitting,
                label = { Text("Buka") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.weight(0.6f),
            )
            OutlinedTextField(
                value = closeHourStr,
                onValueChange = { closeHourStr = it.filter(Char::isDigit).take(2) },
                enabled = !isSubmitting,
                label = { Text("Tutup") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.weight(0.6f),
            )
        }

        // Expandable Online Food Delivery Links Section (GoFood, GrabFood, ShopeeFood)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF6FBF8),
            border = BorderStroke(1.dp, OutlineVariantLight),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isSubmitting) { showDeliveryInputs = !showDeliveryInputs }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("form_kuliner_toggle_delivery_btn"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeliveryDining,
                            contentDescription = null,
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(20.dp),
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.kuliner_delivery_section_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ToskaOnPrimaryContainer,
                            )
                            Text(
                                text = "Sematkan GoFood, GrabFood, atau ShopeeFood",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Icon(
                        imageVector = if (showDeliveryInputs) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                    )
                }

                AnimatedVisibility(visible = showDeliveryInputs) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.safe_link_form_notice_delivery),
                            style = MaterialTheme.typography.labelSmall,
                            color = ToskaOnPrimaryContainer,
                        )

                        OutlinedTextField(
                            value = gofoodUrl,
                            onValueChange = { gofoodUrl = it },
                            enabled = !isSubmitting,
                            isError = gofoodValidation is LinkValidationResult.Invalid,
                            label = { Text(stringResource(R.string.kuliner_gofood_label)) },
                            placeholder = { Text("https://gofood.link/…") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.DeliveryDining,
                                    contentDescription = null,
                                    tint = Color(0xFFC62828),
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                            trailingIcon = {
                                when {
                                    gofoodValidation is LinkValidationResult.Invalid -> {
                                        Icon(Icons.Filled.GppMaybe, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                    gofoodUrl.isNotBlank() && gofoodValidation is LinkValidationResult.Valid -> {
                                        Icon(Icons.Filled.VerifiedUser, null, tint = OpenGreenText, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            supportingText = if (gofoodValidation is LinkValidationResult.Invalid) {
                                {
                                    Text(
                                        text = gofoodValidation.errorMessage,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            } else if (gofoodUrl.isNotBlank() && gofoodValidation is LinkValidationResult.Valid) {
                                {
                                    Text(
                                        text = "Domain resmi GoFood terverifikasi (${gofoodValidation.host})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OpenGreenText,
                                    )
                                }
                            } else {
                                null
                            },
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_kuliner_gofood_url"),
                        )

                        OutlinedTextField(
                            value = grabfoodUrl,
                            onValueChange = { grabfoodUrl = it },
                            enabled = !isSubmitting,
                            isError = grabfoodValidation is LinkValidationResult.Invalid,
                            label = { Text(stringResource(R.string.kuliner_grabfood_label)) },
                            placeholder = { Text("https://r.grab.com/…") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Storefront,
                                    contentDescription = null,
                                    tint = Color(0xFF00880C),
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                            trailingIcon = {
                                when {
                                    grabfoodValidation is LinkValidationResult.Invalid -> {
                                        Icon(Icons.Filled.GppMaybe, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                    grabfoodUrl.isNotBlank() && grabfoodValidation is LinkValidationResult.Valid -> {
                                        Icon(Icons.Filled.VerifiedUser, null, tint = OpenGreenText, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            supportingText = if (grabfoodValidation is LinkValidationResult.Invalid) {
                                {
                                    Text(
                                        text = grabfoodValidation.errorMessage,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            } else if (grabfoodUrl.isNotBlank() && grabfoodValidation is LinkValidationResult.Valid) {
                                {
                                    Text(
                                        text = "Domain resmi GrabFood terverifikasi (${grabfoodValidation.host})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OpenGreenText,
                                    )
                                }
                            } else {
                                null
                            },
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_kuliner_grabfood_url"),
                        )

                        OutlinedTextField(
                            value = shopeefoodUrl,
                            onValueChange = { shopeefoodUrl = it },
                            enabled = !isSubmitting,
                            isError = shopeefoodValidation is LinkValidationResult.Invalid,
                            label = { Text(stringResource(R.string.kuliner_shopeefood_label)) },
                            placeholder = { Text("https://shopee.co.id/universal-link/now-food/…") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingBag,
                                    contentDescription = null,
                                    tint = Color(0xFFD84315),
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                            trailingIcon = {
                                when {
                                    shopeefoodValidation is LinkValidationResult.Invalid -> {
                                        Icon(Icons.Filled.GppMaybe, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                    shopeefoodUrl.isNotBlank() && shopeefoodValidation is LinkValidationResult.Valid -> {
                                        Icon(Icons.Filled.VerifiedUser, null, tint = OpenGreenText, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            supportingText = if (shopeefoodValidation is LinkValidationResult.Invalid) {
                                {
                                    Text(
                                        text = shopeefoodValidation.errorMessage,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            } else if (shopeefoodUrl.isNotBlank() && shopeefoodValidation is LinkValidationResult.Valid) {
                                {
                                    Text(
                                        text = "Domain resmi ShopeeFood terverifikasi (${shopeefoodValidation.host})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OpenGreenText,
                                    )
                                }
                            } else {
                                null
                            },
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_kuliner_shopeefood_url"),
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            enabled = !isSubmitting,
            label = { Text("Menu Andalan & Keunggulan") },
            shape = RoundedCornerShape(16.dp),
            minLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_kuliner_desc"),
        )

        if (!isFormValid && !isSubmitting) {
            Text(
                text = stringResource(R.string.form_hint_complete_fields),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Button(
            onClick = {
                val parsedOpenHour = openHourStr.toIntOrNull() ?: 8
                val parsedCloseHour = closeHourStr.toIntOrNull() ?: 22
                if (onSubmitWithGps != null) {
                    onSubmitWithGps(
                        name,
                        category,
                        description,
                        district,
                        address,
                        priceRange,
                        parsedOpenHour,
                        parsedCloseHour,
                        mapsUrl,
                        gofoodUrl,
                        grabfoodUrl,
                        shopeefoodUrl,
                        selectedPhotoUris,
                        useCurrentGpsLocation,
                    )
                } else {
                    onSubmit(
                        name,
                        category,
                        description,
                        district,
                        address,
                        priceRange,
                        parsedOpenHour,
                        parsedCloseHour,
                        mapsUrl,
                        gofoodUrl,
                        grabfoodUrl,
                        shopeefoodUrl,
                        selectedPhotoUris,
                    )
                }
            },
            enabled = isFormValid && !isSubmitting,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_kuliner_btn"),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(
                        if (isScanningPhoto) R.string.scanning_photo_ai_label else R.string.submitting_upload_label,
                    ),
                )
            } else {
                Text("Simpan Kuliner ke Direktori")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun BookmarksSheetContent(
    bookmarks: List<BookmarkItem>,
    onOpenBookmark: (BookmarkItem) -> Unit,
    onRemoveBookmark: (BookmarkItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GoogleMapsSheetHandleHeader(
            title = "Daftar Tersimpan Warga",
            subtitle = "Kuliner favorit, kejadian penting, dan event Surabaya pilihanmu.",
        )

        if (bookmarks.isEmpty()) {
            EmptyFeedPlaceholder(
                icon = Icons.Outlined.BookmarkBorder,
                title = stringResource(R.string.empty_bookmarks),
                subtitle = "Ketuk ikon pita simpan pada kartu Kuliner, Kejadian, atau Event.",
            )
        } else {
            bookmarks.forEach { bm ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFF6FBF8),
                    border = BorderStroke(1.dp, OutlineVariantLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenBookmark(bm) },
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${bm.targetType} • Kec. ${bm.district}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = bm.title,
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = bm.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { onRemoveBookmark(bm) }) {
                            Icon(
                                imageVector = Icons.Filled.BookmarkRemove,
                                contentDescription = "Hapus Simpanan",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun ProfileAndPrivacySheetContent(
    userAccount: UserAccount?,
    nearbyProfile: NearbyProfile?,
    userEmail: String,
    isAdmin: Boolean,
    focusPrivacyOnly: Boolean,
    isSubmitting: Boolean = false,
    isScanningPhoto: Boolean = false,
    knownCitizens: List<NearbyProfile> = emptyList(),
    userMoments: List<UserMoment> = emptyList(),
    onOpenMomentDetail: (String) -> Unit = {},
    onOpenCreateMoment: () -> Unit = {},
    onOpenCreateLensaWarga: () -> Unit = onOpenCreateMoment,
    onUnblockUser: (String) -> Unit = {},
    onToggleAdmin: (Boolean) -> Unit,
    onOpenAdminDashboard: () -> Unit = {},
    onSaveProfile: (
        name: String,
        district: String,
        bio: String,
        interests: String,
        radiusKm: Double,
        discoverable: Boolean,
        avatarUri: Uri?,
    ) -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit = {},
    onSaveProfileWithGender: (
        (
            name: String,
            gender: String,
            district: String,
            bio: String,
            interests: String,
            radiusKm: Double,
            discoverable: Boolean,
            avatarUri: Uri?,
        ) -> Unit
    )? = null,
) {
    val isAuthorizedAdminAccount = isAdmin || PocketBaseClient.isOfficialAdminEmail(userEmail)
    val initialEditableName = effectiveCitizenDisplayName(userAccount, nearbyProfile)
        .ifBlank { userAccount?.displayName.orEmpty() }
    var displayName by remember(initialEditableName) { mutableStateOf(initialEditableName) }
    var selectedGender by remember(userAccount?.gender, nearbyProfile?.gender) {
        mutableStateOf(
            userAccount?.normalizedGender?.ifBlank { nearbyProfile?.normalizedGender.orEmpty() }.orEmpty(),
        )
    }
    var district by remember(userAccount?.district) { mutableStateOf(userAccount?.district ?: "Genteng") }
    var bio by remember(userAccount?.bio) { mutableStateOf(userAccount?.bio ?: "Halo rek! Salam kenal.") }
    var interests by remember(nearbyProfile?.interests) {
        mutableStateOf(nearbyProfile?.interests ?: "Kuliner SBY, Kopi, Event Kota")
    }
    var radiusSlider by remember(userAccount?.searchRadiusKm) {
        mutableFloatStateOf((userAccount?.searchRadiusKm ?: 10.0).toFloat())
    }
    var isDiscoverable by remember(userAccount?.isDiscoverable) {
        mutableStateOf(userAccount?.isDiscoverable ?: true)
    }
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var activeProfileTab by remember(focusPrivacyOnly) { mutableIntStateOf(if (focusPrivacyOnly) 2 else 0) }
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showPermissionOverviewSheet by remember { mutableStateOf(false) }
    var showOwnAvatarZoom by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var userConfirmedDeleteCheckbox by remember { mutableStateOf(false) }
    var userToUnblock by remember { mutableStateOf<Pair<String, String>?>(null) }

    val existingAvatarUrl = userAccount?.avatarUrl.orEmpty().ifBlank { nearbyProfile?.avatarUrl.orEmpty() }
    val previewOrExistingAvatarUrl = selectedAvatarUri?.toString()?.ifBlank { existingAvatarUrl } ?: existingAvatarUrl

    if (showOwnAvatarZoom) {
        CitizenAvatarZoomDialog(
            name = displayName,
            subtitle = buildList {
                add("Kec. $district")
                val gLbl = formatCitizenGenderLabel(selectedGender)
                if (gLbl.isNotBlank()) add(gLbl)
            }.joinToString(" • "),
            avatarUrl = previewOrExistingAvatarUrl,
            avatarColorHex = nearbyProfile?.avatarColorHex ?: "#006B5E",
            onDismiss = { showOwnAvatarZoom = false },
        )
    }

    if (userToUnblock != null) {
        val (targetUid, targetName) = userToUnblock!!
        AlertDialog(
            onDismissRequest = { userToUnblock = null },
            icon = {
                Icon(
                    imageVector = Icons.Filled.LockOpen,
                    contentDescription = null,
                    tint = ToskaPrimaryDeep,
                    modifier = Modifier.size(28.dp),
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.dialog_unblock_user_title, targetName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_unblock_user_body, targetName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUnblockUser(targetUid)
                        userToUnblock = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimaryDeep),
                    modifier = Modifier.testTag("dialog_confirm_unblock_btn"),
                ) {
                    Text(stringResource(R.string.btn_confirm_unblock_user))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { userToUnblock = null },
                    modifier = Modifier.testTag("dialog_cancel_unblock_btn"),
                ) {
                    Text(stringResource(R.string.btn_cancel_action))
                }
            },
        )
    }

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            selectedAvatarUri = uri
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        GoogleMapsSheetHandleHeader(
            title = if (focusPrivacyOnly) "Pengaturan Radius GPS & Privasi" else "Profil Warga Surabaya",
            subtitle = userEmail.ifBlank { "Akun Warga Terverifikasi • PocketBase Surabaya" },
        )

        if (!focusPrivacyOnly) {
            InstagramProfileHeaderCard(
                name = displayName,
                district = district,
                bio = bio,
                interests = interests.split(",").map { it.trim() }.filter { it.isNotBlank() },
                avatarUrl = previewOrExistingAvatarUrl,
                avatarColorHex = nearbyProfile?.avatarColorHex ?: "#006B5E",
                totalMoments = userMoments.size,
                totalLikes = userMoments.sumOf { it.likesCount },
                isMyOwnProfile = true,
                genderLabel = formatCitizenGenderLabel(selectedGender),
                onAvatarClick = { showOwnAvatarZoom = true },
                onGreet = {},
                onBlock = {},
                onEditProfile = { activeProfileTab = 2 },
            )

            if (isAuthorizedAdminAccount) {
                Button(
                    onClick = onOpenAdminDashboard,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ToskaPrimaryDeep,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("profile_open_admin_dashboard_btn"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.admin_dashboard_title),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            val personalMoments = remember(userMoments) { userMoments.filter { !it.isLensaWarga } }
            val lensaWargaMoments = remember(userMoments) { userMoments.filter { it.isLensaWarga } }

            // Segment Tabs: Momen Pribadi vs Lensa Warga vs Pengaturan
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilterChip(
                    selected = activeProfileTab == 0,
                    onClick = { activeProfileTab = 0 },
                    label = { Text("Momen (${personalMoments.size})") },
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
                    modifier = Modifier.weight(1f).testTag("tab_my_moments"),
                )
                FilterChip(
                    selected = activeProfileTab == 1,
                    onClick = { activeProfileTab = 1 },
                    label = { Text("Lensa Warga (${lensaWargaMoments.size})") },
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
                    modifier = Modifier.weight(1.1f).testTag("tab_my_lensa_warga"),
                )
                FilterChip(
                    selected = activeProfileTab == 2,
                    onClick = { activeProfileTab = 2 },
                    label = { Text("Pengaturan") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Settings,
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
                    modifier = Modifier.weight(1f).testTag("tab_profile_settings"),
                )
            }
        }

        val personalMoments = remember(userMoments) { userMoments.filter { !it.isLensaWarga } }
        val lensaWargaMoments = remember(userMoments) { userMoments.filter { it.isLensaWarga } }

        if (!focusPrivacyOnly && activeProfileTab == 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Galeri Momen Saya",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                OutlinedButton(
                    onClick = onOpenCreateMoment,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_post_new_moment_from_profile"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Posting Momen", style = MaterialTheme.typography.labelMedium)
                }
            }

            if (personalMoments.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFF7FBF8),
                    border = BorderStroke(1.dp, OutlineVariantLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(ToskaPrimaryDeep.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddPhotoAlternate,
                                contentDescription = null,
                                tint = ToskaPrimaryDeep,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        Text(
                            text = "Belum Ada Momen Pribadi",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Momen santai, swafoto, atau kulineran warga akan tersimpan di sini. Abadikan dan bagikan ceritamu!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Button(
                            onClick = onOpenCreateMoment,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimaryDeep),
                            modifier = Modifier.testTag("btn_create_first_moment"),
                        ) {
                            Text("Posting Momen Sekarang", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                val chunked = personalMoments.chunked(3)
                for (rowMoments in chunked) {
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
                        for (i in 0 until (3 - rowMoments.size)) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        if (!focusPrivacyOnly && activeProfileTab == 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Galeri Lensa Warga Saya",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                OutlinedButton(
                    onClick = onOpenCreateLensaWarga,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_post_new_lensa_warga_from_profile"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Posting Lensa", style = MaterialTheme.typography.labelMedium)
                }
            }

            if (lensaWargaMoments.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFF7FBF8),
                    border = BorderStroke(1.dp, OutlineVariantLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(ToskaPrimaryDeep.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = ToskaPrimaryDeep,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        Text(
                            text = "Belum Ada Foto Lensa Warga",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Abadikan sudut jalanan, ikon bersejarah, atau keramaian kota Surabaya untuk dinikmati bersama warga. Tanpa beban pelaporan darurat!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Button(
                            onClick = onOpenCreateLensaWarga,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimaryDeep),
                            modifier = Modifier.testTag("btn_create_first_lensa_warga"),
                        ) {
                            Text("Posting ke Lensa Warga", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                val chunked = lensaWargaMoments.chunked(3)
                for (rowMoments in chunked) {
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
                        for (i in 0 until (3 - rowMoments.size)) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        if (focusPrivacyOnly || activeProfileTab == 2) {
            if (!focusPrivacyOnly) {
                // Avatar Photo Picker Row
                Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF5FBF7),
                border = BorderStroke(1.dp, OutlineVariantLight),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        InitialsAvatar(
                            name = displayName,
                            colorHex = nearbyProfile?.avatarColorHex ?: "#0F9D76",
                            sizeDp = 58,
                            avatarUrl = existingAvatarUrl,
                        )
                        if (selectedAvatarUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(selectedAvatarUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Pratinjau Foto Profil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                                    .testTag("profile_avatar_preview"),
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Foto Profil Warga",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (selectedAvatarUri != null) {
                                "Foto baru dipilih • Tekan Simpan Pengaturan"
                            } else {
                                "Tampil di pojok atas, Teman Sekitar & ruang chat"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (!isSubmitting) {
                                avatarPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            }
                        },
                        enabled = !isSubmitting,
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("profile_pick_avatar_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddAPhoto,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.avatar_picker_change_btn),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF3FBF7),
                border = BorderStroke(1.dp, OutlineVariantLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_ai_safety_notice"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(R.string.ai_photo_safety_form_notice),
                        style = MaterialTheme.typography.labelSmall,
                        color = ToskaOnPrimaryContainer,
                    )
                }
            }

            // Admin Mode Switch Card (Visible ONLY to authorized official admin accounts)
            if (isAuthorizedAdminAccount) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MintSoftGradient, RoundedCornerShape(20.dp))
                        .border(1.dp, OutlineVariantLight, RoundedCornerShape(20.dp)),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = null,
                                tint = ToskaPrimaryDeep,
                                modifier = Modifier.size(24.dp),
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.admin_mode_label),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ToskaOnPrimaryContainer,
                                )
                                Text(
                                    text = "Khusus Admin Resmi (${PocketBaseClient.PRIMARY_ADMIN_EMAIL}) • Kelola laporan & persetujuan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ToskaOnPrimaryContainer.copy(alpha = 0.85f),
                                )
                            }
                        }
                        Switch(
                            checked = isAdmin,
                            onCheckedChange = onToggleAdmin,
                            enabled = !isSubmitting,
                            modifier = Modifier.testTag("admin_mode_switch"),
                        )
                    }
                }
            }

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                enabled = !isSubmitting,
                label = { Text("Nama Tampilan Warga") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                enabled = !isSubmitting,
                label = { Text("Bio Singkat Sapaan") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = interests,
                onValueChange = { interests = it },
                enabled = !isSubmitting,
                label = { Text("Minat / Hobi (pisahkan koma)") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            DistrictPickerChips(
                selectedDistrict = district,
                enabled = !isSubmitting,
                onSelect = { district = it },
            )

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFF5FBF7),
                border = BorderStroke(1.dp, OutlineVariantLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_gender_selector"),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.mandatory_profile_gender_title),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = ToskaOnPrimaryContainer,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterChip(
                            selected = selectedGender == "LAKI_LAKI",
                            onClick = { if (!isSubmitting) selectedGender = "LAKI_LAKI" },
                            label = { Text(stringResource(R.string.gender_option_male)) },
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
                                .testTag("gender_chip_male"),
                        )

                        FilterChip(
                            selected = selectedGender == "PEREMPUAN",
                            onClick = { if (!isSubmitting) selectedGender = "PEREMPUAN" },
                            label = { Text(stringResource(R.string.gender_option_female)) },
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
                                .testTag("gender_chip_female"),
                        )
                    }
                }
            }
        }

        // GPS Radius & Discoverability Controls
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF5FBF7),
            border = BorderStroke(1.dp, OutlineVariantLight),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Radius Pencarian Teman Sekitar", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "${radiusSlider.roundToInt()} km",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Slider(
                    value = radiusSlider,
                    onValueChange = { radiusSlider = it },
                    enabled = !isSubmitting,
                    valueRange = 1f..35f,
                    modifier = Modifier.testTag("gps_radius_slider"),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tampilkan Profil di Teman Sekitar", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = "Jarak dibulatkan aman, lokasi rumah presisi tidak pernah ditampilkan.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = isDiscoverable,
                        onCheckedChange = { isDiscoverable = it },
                        enabled = !isSubmitting,
                        modifier = Modifier.testTag("discoverable_switch"),
                    )
                }
            }
        }

        val blockedUids = userAccount?.blockedUids.orEmpty()
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (blockedUids.isNotEmpty()) Color(0xFFFFF9F8) else Color(0xFFF7FBF9),
            border = BorderStroke(1.dp, if (blockedUids.isNotEmpty()) Color(0xFFFFCDD2) else OutlineVariantLight),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("blocked_users_card"),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
                                .size(36.dp)
                                .background(
                                    if (blockedUids.isNotEmpty()) Color(0xFFFFEBEE) else ToskaPrimaryDeep.copy(alpha = 0.1f),
                                    CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (blockedUids.isNotEmpty()) Icons.Filled.Block else Icons.Filled.Shield,
                                contentDescription = null,
                                tint = if (blockedUids.isNotEmpty()) AlertCrimson else ToskaPrimaryDeep,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.blocked_users_section_title, blockedUids.size),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(R.string.blocked_users_section_subtitle),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (blockedUids.isNotEmpty()) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    ) {
                        Text(
                            text = if (blockedUids.isNotEmpty()) "${blockedUids.size} Diblokir" else "0 Diblokir",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (blockedUids.isNotEmpty()) AlertCrimson else Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }

                if (blockedUids.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = null,
                                tint = OpenGreenText,
                                modifier = Modifier.size(26.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.blocked_users_empty_title),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = stringResource(R.string.blocked_users_empty_subtitle),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        blockedUids.forEach { uid ->
                            val citizen = knownCitizens.firstOrNull { it.userId == uid || it.id == uid }
                            val resolvedName = citizen?.displayName?.takeIf { it.isNotBlank() }
                                ?: "Warga (${uid.take(6)})"
                            val resolvedDistrict = citizen?.district?.takeIf { it.isNotBlank() } ?: "Surabaya"
                            val avatarColorHex = citizen?.avatarColorHex ?: "#006B5E"
                            val avatarUrl = citizen?.avatarUrl.orEmpty()

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFFFEBEE)),
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("blocked_user_item_$uid"),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            InitialsAvatar(
                                                name = resolvedName,
                                                colorHex = avatarColorHex,
                                                sizeDp = 40,
                                                avatarUrl = avatarUrl,
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = resolvedName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color(0xFFF1F5F9),
                                                ) {
                                                    Text(
                                                        text = "Kec. $resolvedDistrict",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    )
                                                }
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color(0xFFFFEBEE),
                                                ) {
                                                    Text(
                                                        text = "Diblokir",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AlertCrimson,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { userToUnblock = uid to resolvedName },
                                        shape = CircleShape,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = ToskaPrimaryDeep,
                                        ),
                                        border = BorderStroke(1.dp, ToskaPrimaryDeep.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .height(38.dp)
                                            .testTag("unblock_user_btn_$uid"),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.LockOpen,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.btn_unblock_user),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                val roundedRadius = radiusSlider.roundToInt().toDouble()
                if (onSaveProfileWithGender != null) {
                    onSaveProfileWithGender(
                        displayName,
                        selectedGender,
                        district,
                        bio,
                        interests,
                        roundedRadius,
                        isDiscoverable,
                        selectedAvatarUri,
                    )
                } else {
                    onSaveProfile(
                        displayName,
                        district,
                        bio,
                        interests,
                        roundedRadius,
                        isDiscoverable,
                        selectedAvatarUri,
                    )
                }
            },
            enabled = !isSubmitting && (
                focusPrivacyOnly || (
                    isValidCitizenDisplayName(displayName) &&
                        normalizeCitizenGender(selectedGender).isNotBlank() &&
                        (existingAvatarUrl.isNotBlank() || selectedAvatarUri != null)
                )
            ),
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_profile_privacy_btn"),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(
                        if (isScanningPhoto) R.string.scanning_photo_ai_label else R.string.submitting_upload_label,
                    ),
                )
            } else {
                Text("Simpan Pengaturan")
            }
        }

        if (!focusPrivacyOnly) {
            // Section: Tentang & Dukungan Aplikasi
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFF5FBF7),
                border = BorderStroke(1.dp, OutlineVariantLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_app_support_section"),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Tentang & Dukungan Aplikasi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ToskaPrimaryDeep,
                    )

                    SupportMenuRow(
                        icon = Icons.Filled.Info,
                        title = "Tentang Arek Surabaya",
                        subtitle = "Versi 1.0.0 • Misi & informasi aplikasi",
                        testTag = "btn_menu_about",
                        onClick = { showAboutDialog = true },
                    )

                    HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.5f))

                    SupportMenuRow(
                        icon = Icons.Filled.MyLocation,
                        title = "Izin Akses Lokasi & Notifikasi",
                        subtitle = "Penjelasan kegunaan GPS & status izin perangkat",
                        testTag = "btn_menu_permission_disclosure",
                        onClick = { showPermissionOverviewSheet = true },
                    )

                    HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.5f))

                    SupportMenuRow(
                        icon = Icons.Filled.Security,
                        title = "Kebijakan Privasi (Privacy Policy)",
                        subtitle = "Perlindungan data warga & filter AI",
                        testTag = "btn_menu_privacy_policy",
                        onClick = { showPrivacyDialog = true },
                    )

                    HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.5f))

                    SupportMenuRow(
                        icon = Icons.Filled.Share,
                        title = "Bagikan Aplikasi",
                        subtitle = "Ajak dulur & keluarga bergabung",
                        testTag = "btn_menu_share_app",
                        onClick = { shareArekSurabayaApp(context) },
                    )

                    HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.5f))

                    SupportMenuRow(
                        icon = Icons.Filled.Star,
                        title = "Beri Rating di Play Store",
                        subtitle = "Dukung kami dengan ulasan bintang 5",
                        testTag = "btn_menu_rate_app",
                        onClick = { rateArekSurabayaOnPlayStore(context) },
                    )
                }
            }

            HorizontalDivider()
            OutlinedButton(
                onClick = onSignOut,
                enabled = !isSubmitting,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sign_out_button"),
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.btn_sign_out))
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Danger Zone: Hapus Akun & Seluruh Data Warga (Google Play Store Compliance)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFFFF5F5),
                border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("danger_zone_card"),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = AlertCrimson,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(R.string.delete_account_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AlertCrimson,
                        )
                    }

                    Text(
                        text = stringResource(R.string.delete_account_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Data yang dihapus permanen:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "• Identitas profil & foto avatar warga",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "• Momen pribadi, foto lensa warga & komentar pantauan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "• Lapak jual beli barkas, info kos & direktori jasa",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "• Ruang obrolan chat & seluruh sesi masuk lokal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Button(
                        onClick = {
                            userConfirmedDeleteCheckbox = false
                            showDeleteAccountDialog = true
                        },
                        enabled = !isSubmitting,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlertCrimson,
                            contentColor = Color.White,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("delete_account_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.delete_account_btn),
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Text(
                        text = stringResource(R.string.delete_account_support_notice),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isSubmitting) showDeleteAccountDialog = false
            },
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
                        text = stringResource(R.string.delete_account_dialog_desc, displayName),
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
                                .clickable(enabled = !isSubmitting) {
                                    userConfirmedDeleteCheckbox = !userConfirmedDeleteCheckbox
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Checkbox(
                                checked = userConfirmedDeleteCheckbox,
                                onCheckedChange = { userConfirmedDeleteCheckbox = it },
                                enabled = !isSubmitting,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AlertCrimson,
                                ),
                                modifier = Modifier.testTag("delete_account_confirm_checkbox"),
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
                        onDeleteAccount()
                    },
                    enabled = userConfirmedDeleteCheckbox && !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlertCrimson,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier.testTag("confirm_delete_account_btn"),
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Menghapus...")
                    } else {
                        Text(stringResource(R.string.delete_account_dialog_confirm_btn))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    enabled = !isSubmitting,
                    modifier = Modifier.testTag("cancel_delete_account_btn"),
                ) {
                    Text(stringResource(R.string.delete_account_dialog_cancel_btn))
                }
            },
        )
    }

    if (showAboutDialog) {
        AboutAppDialog(onDismiss = { showAboutDialog = false })
    }
    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }
    if (showPermissionOverviewSheet) {
        PermissionDisclosureBottomSheet(
            mode = PermissionSheetMode.Overview,
            onDismiss = { showPermissionOverviewSheet = false },
        )
    }
}

@Composable
private fun DistrictPickerChips(
    selectedDistrict: String,
    enabled: Boolean = true,
    onSelect: (String) -> Unit,
) {
    SurabayaDistrictDropdown(
        selectedDistrict = selectedDistrict,
        onSelect = onSelect,
        enabled = enabled,
    )
}

@Composable
fun CreateMomentSheetContent(
    defaultDistrict: String,
    initialPostType: String = POST_TYPE_MOMEN,
    isSubmitting: Boolean = false,
    isScanningPhoto: Boolean = false,
    onSubmit: (
        caption: String,
        district: String,
        locationName: String,
        postType: String,
        photoUris: List<Uri>,
    ) -> Unit,
) {
    var postType by remember { mutableStateOf(initialPostType) }
    var caption by remember { mutableStateOf("") }
    var district by remember { mutableStateOf(defaultDistrict) }
    var locationName by remember { mutableStateOf("") }
    var selectedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val isLensaWarga = postType == POST_TYPE_LENSA_WARGA
    val isFormValid = if (isLensaWarga) {
        (caption.trim().isNotEmpty() || selectedPhotoUris.isNotEmpty()) && locationName.trim().isNotEmpty()
    } else {
        caption.trim().isNotEmpty() || selectedPhotoUris.isNotEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GoogleMapsSheetHandleHeader(
            title = if (isLensaWarga) "Posting ke Lensa Warga" else "Posting Momen Arek Surabaya",
            subtitle = if (isLensaWarga) {
                "Bagikan foto estetika sudut kota, ikon sejarah, atau dinamika keramaian Surabaya tanpa beban pelaporan darurat."
            } else {
                "Abadikan foto swafoto, nongkrong, kulineran, atau kebersamaan santai warga (hingga 4 foto)."
            },
        )

        // Category Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = !isLensaWarga,
                onClick = { postType = POST_TYPE_MOMEN },
                label = { Text("📸 Momen Pribadi") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chip_type_momen"),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ToskaPrimaryDeep,
                    selectedLabelColor = Color.White,
                ),
            )
            FilterChip(
                selected = isLensaWarga,
                onClick = { postType = POST_TYPE_LENSA_WARGA },
                label = { Text("🏙️ Lensa Warga") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chip_type_lensa_warga"),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ToskaPrimaryDeep,
                    selectedLabelColor = Color.White,
                ),
            )
        }

        FormMultiPhotoPickerSection(
            selectedPhotoUris = selectedPhotoUris,
            onPhotosChanged = { selectedPhotoUris = it },
            maxPhotos = 4,
            enabled = !isSubmitting,
            title = if (isLensaWarga) "Foto Sudut / Ikon Kota (1 - 4 Foto)" else "Foto Momen (1 - 4 Foto)",
            subtitle = if (isLensaWarga) {
                "Unggah foto lanskap, gedung heritage, atau keramaian kota Surabaya."
            } else {
                "Pilih 1 - 4 foto galeri. Dilindungi pemindai AI konten eksplisit."
            },
        )

        OutlinedTextField(
            value = locationName,
            onValueChange = { locationName = it },
            label = {
                Text(
                    if (isLensaWarga) "Nama Titik / Ikon Kota (Wajib) *"
                    else "Nama Lokasi / Tempat (Opsional)"
                )
            },
            placeholder = {
                Text(
                    if (isLensaWarga) "Contoh: Monumen Tugu Pahlawan / Jembatan Suramadu / Alun-Alun SBY"
                    else "Contoh: Jl. Tunjungan / Taman Bungkul"
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("moment_location_input"),
            enabled = !isSubmitting,
        )

        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            label = {
                Text(
                    if (isLensaWarga) "Keterangan Foto / Suasana Kota"
                    else "Cerita / Keterangan Momen"
                )
            },
            placeholder = {
                Text(
                    if (isLensaWarga) "Contoh: Suasana malam minggu ramai bersahabat di depan museum pahlawan..."
                    else "Contoh: Sore syahdu di Tunjungan, lampu kota mulai menyala..."
                )
            },
            minLines = 3,
            maxLines = 6,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("moment_caption_input"),
            enabled = !isSubmitting,
        )

        Column {
            Text(
                text = "Kecamatan Surabaya",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            SurabayaDistrictDropdown(
                selectedDistrict = district,
                onSelect = { district = it },
                enabled = !isSubmitting,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                onSubmit(
                    caption.trim(),
                    district,
                    locationName.trim(),
                    postType,
                    selectedPhotoUris,
                )
            },
            enabled = isFormValid && !isSubmitting,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_submit_moment"),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(
                        if (isScanningPhoto) R.string.scanning_photo_ai_label else R.string.submitting_upload_label,
                    ),
                )
            } else {
                Icon(
                    imageVector = if (isLensaWarga) Icons.Filled.AddAPhoto else Icons.Filled.AddAPhoto,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isLensaWarga) "Publikasikan ke Lensa Warga" else "Publikasikan Momen",
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun CreateDestinationSheetContent(
    defaultDistrict: String,
    initialDestination: SurabayaDestination? = null,
    isSubmitting: Boolean = false,
    isScanningPhoto: Boolean = false,
    onSaveDraft: ((DestinationDraft) -> Unit)? = null,
    onClearDraft: (() -> Unit)? = null,
    onSubmit: (
        title: String,
        category: String,
        description: String,
        address: String,
        district: String,
        ticketPrice: String,
        operationalHours: String,
        bestTimeToVisit: String,
        mapsUrl: String,
        photoUris: List<Uri>,
    ) -> Unit,
    onClose: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val isEditMode = initialDestination != null

    val savedDraft = remember(initialDestination) {
        if (initialDestination == null) DestinationDraftManager.loadDraft(context) else null
    }
    var hasLoadedDraft by remember { mutableStateOf(savedDraft != null) }

    var title by remember { mutableStateOf(initialDestination?.title ?: savedDraft?.title.orEmpty()) }
    var category by remember { mutableStateOf(initialDestination?.category ?: savedDraft?.category ?: "Wisata Sejarah") }
    var description by remember { mutableStateOf(initialDestination?.description ?: savedDraft?.description.orEmpty()) }
    var address by remember { mutableStateOf(initialDestination?.address ?: savedDraft?.address.orEmpty()) }
    var district by remember { mutableStateOf(initialDestination?.district ?: savedDraft?.district ?: defaultDistrict) }
    var ticketPrice by remember { mutableStateOf(initialDestination?.ticketPrice ?: savedDraft?.ticketPrice ?: "Gratis") }
    var operationalHours by remember { mutableStateOf(initialDestination?.operationalHours ?: savedDraft?.operationalHours ?: "08.00 - 17.00 WIB") }
    var bestTimeToVisit by remember { mutableStateOf(initialDestination?.bestTime ?: savedDraft?.bestTimeToVisit ?: "Pagi / Sore Hari") }
    var mapsUrl by remember { mutableStateOf(initialDestination?.mapsUrl ?: savedDraft?.mapsUrl.orEmpty()) }
    var selectedPhotoUris by remember {
        mutableStateOf<List<Uri>>(
            if (initialDestination == null && savedDraft != null) {
                savedDraft.photoUris.mapNotNull {
                    try { Uri.parse(it) } catch (_: Exception) { null }
                }
            } else emptyList()
        )
    }

    var showDraftSavedNotice by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "Wisata Sejarah",
        "Taman Kota",
        "Wisata Bahari",
        "Religi & Budaya",
        "Museum & Edukasi",
    )

    val isFormValid = title.trim().length >= 3 &&
        description.trim().length >= 10 &&
        address.trim().length >= 3

    val currentDraft = remember(title, category, description, address, district, ticketPrice, operationalHours, bestTimeToVisit, mapsUrl, selectedPhotoUris) {
        DestinationDraft(
            title = title.trim(),
            category = category,
            description = description.trim(),
            address = address.trim(),
            district = district,
            ticketPrice = ticketPrice.trim().ifBlank { "Gratis" },
            operationalHours = operationalHours.trim().ifBlank { "24 Jam" },
            bestTimeToVisit = bestTimeToVisit.trim().ifBlank { "Sepanjang Hari" },
            mapsUrl = mapsUrl.trim(),
            photoUris = selectedPhotoUris.map { it.toString() },
        )
    }

    val handleAttemptClose: () -> Unit = {
        if (!isEditMode && currentDraft.hasContent()) {
            showUnsavedDialog = true
        } else {
            onClose?.invoke()
        }
    }

    // Dialog Konfirmasi Pencegah Form Hilang Saat Tergeser / Ditutup
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Simpan Draf Sebelum Keluar?") },
            text = {
                Text("Anda sudah mengisi rincian destinasi ini. Simpan sebagai draf agar dapat dilanjutkan saat mencari alamat atau tautan Maps.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        DestinationDraftManager.saveDraft(context, currentDraft)
                        onSaveDraft?.invoke(currentDraft)
                        showUnsavedDialog = false
                        onClose?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimary),
                ) {
                    Text("Simpan Draf & Keluar")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            DestinationDraftManager.clearDraft(context)
                            onClearDraft?.invoke()
                            showUnsavedDialog = false
                            onClose?.invoke()
                        }
                    ) {
                        Text("Buang", color = AlertCrimson)
                    }
                    TextButton(onClick = { showUnsavedDialog = false }) {
                        Text("Lanjut Mengisi")
                    }
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Header with Close Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEditMode) "Edit Destinasi Surabaya" else "Tambah Destinasi Surabaya",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (isEditMode) "Perbarui informasi destinasi wisata resmi Kota Pahlawan." else "Publikasikan objek wisata, museum, atau cagar budaya resmi Kota Pahlawan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (onClose != null) {
                IconButton(onClick = handleAttemptClose, modifier = Modifier.testTag("btn_close_destination_form")) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Tutup",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Admin Curator Badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ToskaPrimaryDeep,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditMode) "Mode Edit Kurator: Perubahan akan langsung diperbarui di direktori" else "Akses Kurator Admin: Data akan langsung tayang ke direktori resmi",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Banner Melanjutkan Draf Tersimpan (Create Mode)
        if (hasLoadedDraft && !isEditMode) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ToskaPrimary.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = null,
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Melanjutkan draf tersimpan",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ToskaPrimaryDeep,
                        )
                    }
                    TextButton(
                        onClick = {
                            DestinationDraftManager.clearDraft(context)
                            onClearDraft?.invoke()
                            hasLoadedDraft = false
                            title = ""
                            description = ""
                            address = ""
                            mapsUrl = ""
                            selectedPhotoUris = emptyList()
                            district = defaultDistrict
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text("Bersihkan Draf", color = AlertCrimson, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Notifikasi Draft Baru Saja Disimpan
        if (showDraftSavedNotice) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ToskaPrimary.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Draf destinasi berhasil disimpan! Anda aman mencari alamat/link di aplikasi lain.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ToskaPrimaryDeep,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        // Foto Picker
        FormMultiPhotoPickerSection(
            selectedPhotoUris = selectedPhotoUris,
            onPhotosChanged = { selectedPhotoUris = it },
            maxPhotos = 4,
            enabled = !isSubmitting,
            subtitle = if (isEditMode) "Tambahkan foto baru untuk mengganti/melengkapi foto yang ada." else "Foto resmi destinasi (hingga 4 foto).",
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Nama Destinasi") },
            placeholder = { Text("Contoh: Kawasan Kota Lama Surabaya") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_dest_title_input"),
            enabled = !isSubmitting,
        )

        Column {
            Text(
                text = "Kategori Destinasi",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                for (cat in categories) {
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) },
                        enabled = !isSubmitting,
                    )
                }
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Deskripsi & Daya Tarik Wisata") },
            placeholder = { Text("Jelaskan sejarah, fasilitas, keunikan, dan spot foto menarik...") },
            minLines = 3,
            maxLines = 6,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_dest_desc_input"),
            enabled = !isSubmitting,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = ticketPrice,
                onValueChange = { ticketPrice = it },
                label = { Text("Tiket Masuk") },
                placeholder = { Text("Gratis / Rp 10.000") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f),
                enabled = !isSubmitting,
            )

            OutlinedTextField(
                value = operationalHours,
                onValueChange = { operationalHours = it },
                label = { Text("Jam Operasional") },
                placeholder = { Text("24 Jam / 08.00-17.00") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f),
                enabled = !isSubmitting,
            )
        }

        OutlinedTextField(
            value = bestTimeToVisit,
            onValueChange = { bestTimeToVisit = it },
            label = { Text("Waktu Terbaik Berkunjung") },
            placeholder = { Text("Pagi Hari / Menjelang Sunset") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting,
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Alamat Lengkap") },
            placeholder = { Text("Jl. Rajawali No. 12") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting,
        )

        Column {
            Text(
                text = "Kecamatan",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            SurabayaDistrictDropdown(
                selectedDistrict = district,
                onSelect = { district = it },
                enabled = !isSubmitting,
            )
        }

        OutlinedTextField(
            value = mapsUrl,
            onValueChange = { mapsUrl = it },
            label = { Text("Tautan Google Maps (Opsional)") },
            placeholder = { Text("https://maps.app.goo.gl/...") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Tombol Aksi: Simpan Draf & Terbitkan / Simpan Perubahan
        if (isEditMode) {
            Button(
                onClick = {
                    onSubmit(
                        title.trim(),
                        category,
                        description.trim(),
                        address.trim(),
                        district,
                        ticketPrice.trim().ifBlank { "Gratis" },
                        operationalHours.trim().ifBlank { "24 Jam" },
                        bestTimeToVisit.trim().ifBlank { "Sepanjang Hari" },
                        mapsUrl.trim(),
                        selectedPhotoUris,
                    )
                },
                enabled = isFormValid && !isSubmitting,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_submit_destination"),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Menyimpan Perubahan...")
                } else {
                    Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Simpan Perubahan Destinasi", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Tombol Simpan Draf
                OutlinedButton(
                    onClick = {
                        DestinationDraftManager.saveDraft(context, currentDraft)
                        onSaveDraft?.invoke(currentDraft)
                        hasLoadedDraft = true
                        showDraftSavedNotice = true
                    },
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, ToskaPrimary),
                    modifier = Modifier
                        .weight(0.42f)
                        .height(52.dp)
                        .testTag("btn_save_destination_draft"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Simpan Draf",
                        fontWeight = FontWeight.Bold,
                        color = ToskaPrimaryDeep,
                        maxLines = 1,
                    )
                }

                // Tombol Terbitkan Destinasi
                Button(
                    onClick = {
                        onSubmit(
                            title.trim(),
                            category,
                            description.trim(),
                            address.trim(),
                            district,
                            ticketPrice.trim().ifBlank { "Gratis" },
                            operationalHours.trim().ifBlank { "24 Jam" },
                            bestTimeToVisit.trim().ifBlank { "Sepanjang Hari" },
                            mapsUrl.trim(),
                            selectedPhotoUris,
                        )
                    },
                    enabled = isFormValid && !isSubmitting,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimary),
                    modifier = Modifier
                        .weight(0.58f)
                        .height(52.dp)
                        .testTag("btn_submit_destination"),
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Menerbitkan...")
                    } else {
                        Icon(Icons.Filled.Storefront, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Terbitkan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

