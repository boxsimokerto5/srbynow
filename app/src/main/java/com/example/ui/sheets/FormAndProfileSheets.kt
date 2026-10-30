package com.example.ui.sheets

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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.BookmarkItem
import com.example.data.model.NearbyProfile
import com.example.data.model.UserAccount
import com.example.data.util.LinkSecurityValidator
import com.example.data.util.LinkValidationResult
import com.example.data.util.SafeLinkCategory
import com.example.data.util.SurabayaDistricts
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.components.FormMultiPhotoPickerSection
import com.example.ui.components.FormPhotoPickerSection
import com.example.ui.components.GoogleMapsSheetHandleHeader
import com.example.ui.components.InitialsAvatar
import com.example.ui.components.SurabayaDistrictDropdown
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimaryDeep
import kotlin.math.roundToInt

@Composable
fun CreateIncidentSheetContent(
    defaultDistrict: String,
    isSubmitting: Boolean = false,
    isScanningPhoto: Boolean = false,
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
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("LALU_LINTAS") }
    var severity by remember { mutableStateOf("INFO") }
    var district by remember { mutableStateOf(defaultDistrict) }
    var address by remember { mutableStateOf("") }
    var mapsUrl by remember { mutableStateOf("") }
    var selectedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

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
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
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
    isSubmitting: Boolean = false,
    isScanningPhoto: Boolean = false,
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
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("FESTIVAL") }
    var dateText by remember { mutableStateOf("Minggu, 29 Nov 2026") }
    var timeText by remember { mutableStateOf("18.30 - 21.30 WIB") }
    var venue by remember { mutableStateOf("") }
    var district by remember { mutableStateOf(defaultDistrict) }
    var address by remember { mutableStateOf("") }
    var mapsUrl by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("Gratis") }
    var description by remember { mutableStateOf("") }
    var selectedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val mapsValidation = remember(mapsUrl) {
        LinkSecurityValidator.validateOptionalLink(mapsUrl, SafeLinkCategory.GOOGLE_MAPS)
    }

    val isFormValid = title.trim().length >= 3 &&
        venue.trim().length >= 2 &&
        address.trim().length >= 2 &&
        description.trim().length >= 3 &&
        mapsValidation is LinkValidationResult.Valid

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GoogleMapsSheetHandleHeader(
            title = "Usulkan Event Surabaya",
            subtitle = "Unggah hingga 4 poster/foto acara beserta titik lokasi Google Maps. Usulan akan ditinjau Admin.",
        )

        FormMultiPhotoPickerSection(
            selectedPhotoUris = selectedPhotoUris,
            onPhotosChanged = { selectedPhotoUris = it.take(4) },
            maxPhotos = 4,
            enabled = !isSubmitting,
            title = stringResource(R.string.event_multi_photo_title),
            subtitle = stringResource(R.string.event_multi_photo_subtitle),
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
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
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
                Text("Kirim Usulan ke Admin")
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
    ) -> Unit,
) {
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
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
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
                onSubmit(
                    name,
                    category,
                    description,
                    district,
                    address,
                    priceRange,
                    openHourStr.toIntOrNull() ?: 8,
                    closeHourStr.toIntOrNull() ?: 22,
                    mapsUrl,
                    gofoodUrl,
                    grabfoodUrl,
                    shopeefoodUrl,
                    selectedPhotoUris,
                )
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
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
    onUnblockUser: (String) -> Unit = {},
    onToggleAdmin: (Boolean) -> Unit,
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
) {
    var displayName by remember(userAccount?.displayName) { mutableStateOf(userAccount?.displayName ?: "Warga Surabaya") }
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

    val existingAvatarUrl = userAccount?.avatarUrl.orEmpty().ifBlank { nearbyProfile?.avatarUrl.orEmpty() }
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
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GoogleMapsSheetHandleHeader(
            title = if (focusPrivacyOnly) "Pengaturan Radius GPS & Privasi" else "Profil Warga & Mode Admin",
            subtitle = userEmail.ifBlank { "Akun Warga Terverifikasi • PocketBase Surabaya" },
        )

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

            // Admin Mode Switch Card (Soft Light-Green Gradient)
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
                                text = "Izinkan meninjau & menyetujui usulan Event Surabaya",
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
        }

        DistrictPickerChips(
            selectedDistrict = district,
            enabled = !isSubmitting,
            onSelect = { district = it },
        )

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
        if (blockedUids.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFFFF8F6),
                border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("blocked_users_card"),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Block,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = stringResource(R.string.blocked_users_section_title, blockedUids.size),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = stringResource(R.string.blocked_users_section_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    blockedUids.forEach { uid ->
                        val resolvedName = knownCitizens
                            .firstOrNull { it.userId == uid || it.id == uid }
                            ?.displayName
                            ?.takeIf { it.isNotBlank() }
                            ?: "Warga (${uid.take(6)})"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = resolvedName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedButton(
                                onClick = { onUnblockUser(uid) },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("unblock_user_btn_$uid"),
                            ) {
                                Text(
                                    text = stringResource(R.string.btn_unblock_user),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                onSaveProfile(
                    displayName,
                    district,
                    bio,
                    interests,
                    radiusSlider.roundToInt().toDouble(),
                    isDiscoverable,
                    selectedAvatarUri,
                )
            },
            enabled = displayName.trim().length >= 2 && !isSubmitting,
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
        }

        Spacer(modifier = Modifier.height(10.dp))
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
