package com.example.ui.sheets

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.data.model.TambalBanDraft
import com.example.data.model.TambalBanSpot
import com.example.data.util.TambalBanDraftManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.FormMultiPhotoPickerSection
import com.example.ui.components.SurabayaDistrictDropdown
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.data.location.LocationHelper
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.SoftEmergencyLightGradient
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun CreateTambalBanSheetContent(
    defaultDistrict: String = "Genteng",
    initialSpot: TambalBanSpot? = null,
    isSubmitting: Boolean = false,
    isScanningPhoto: Boolean = false,
    isLiveGpsActive: Boolean = false,
    onRequestGpsRefresh: () -> Unit = {},
    onSaveDraft: ((TambalBanDraft) -> Unit)? = null,
    onClearDraft: (() -> Unit)? = null,
    onSubmit: (
        name: String,
        description: String,
        district: String,
        address: String,
        is24Hours: Boolean,
        isTubeless: Boolean,
        forMotor: Boolean,
        forMobil: Boolean,
        canCallOut: Boolean,
        mapsUrl: String,
        phoneNumber: String,
        whatsappNumber: String,
        photoUri: Uri?,
    ) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isEditMode = initialSpot != null

    val savedDraft = remember(initialSpot) {
        if (initialSpot == null) TambalBanDraftManager.loadDraft(context) else null
    }
    var hasLoadedDraft by remember { mutableStateOf(savedDraft != null) }

    var name by remember { mutableStateOf(initialSpot?.name ?: savedDraft?.name.orEmpty()) }
    var district by remember { mutableStateOf(initialSpot?.district ?: savedDraft?.district ?: defaultDistrict.ifBlank { "Genteng" }) }
    var address by remember { mutableStateOf(initialSpot?.address ?: savedDraft?.address.orEmpty()) }
    var description by remember { mutableStateOf(initialSpot?.description ?: savedDraft?.description.orEmpty()) }
    var is24Hours by remember { mutableStateOf(initialSpot?.is24Hours ?: savedDraft?.is24Hours ?: false) }
    var isTubeless by remember { mutableStateOf(initialSpot?.isTubeless ?: savedDraft?.isTubeless ?: true) }
    var forMotor by remember { mutableStateOf(initialSpot?.forMotor ?: savedDraft?.forMotor ?: true) }
    var forMobil by remember { mutableStateOf(initialSpot?.forMobil ?: savedDraft?.forMobil ?: false) }
    var canCallOut by remember { mutableStateOf(initialSpot?.canCallOut ?: savedDraft?.canCallOut ?: false) }
    var mapsUrl by remember { mutableStateOf(initialSpot?.mapsUrl ?: savedDraft?.mapsUrl.orEmpty()) }
    var phoneNumber by remember { mutableStateOf(initialSpot?.phoneNumber ?: savedDraft?.phoneNumber.orEmpty()) }
    var whatsappNumber by remember { mutableStateOf(initialSpot?.whatsappNumber ?: savedDraft?.whatsappNumber.orEmpty()) }
    var photoUris by remember {
        mutableStateOf<List<Uri>>(
            if (initialSpot == null && savedDraft != null && savedDraft.photoUri.isNotBlank()) {
                listOfNotNull(try { Uri.parse(savedDraft.photoUri) } catch (_: Exception) { null })
            } else emptyList()
        )
    }
    var gpsCoordinatesApplied by remember { mutableStateOf(false) }
    var showDraftSavedNotice by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val applyTambalBanGps = rememberLocationPermissionWithDisclosure(
        usageContext = LocationUsageContext.TAMBAL_BAN,
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

    val isNameValid = name.trim().length >= 4
    val isAddressValid = address.trim().length >= 5
    val hasVehicleType = forMotor || forMobil
    val canSubmit = isNameValid && isAddressValid && hasVehicleType && !isSubmitting && !isScanningPhoto

    val currentDraft = remember(name, description, district, address, mapsUrl, phoneNumber, whatsappNumber, is24Hours, isTubeless, forMotor, forMobil, canCallOut, photoUris) {
        TambalBanDraft(
            name = name.trim(),
            description = description.trim(),
            district = district,
            address = address.trim(),
            mapsUrl = mapsUrl.trim(),
            phoneNumber = phoneNumber.trim(),
            whatsappNumber = whatsappNumber.trim(),
            is24Hours = is24Hours,
            isTubeless = isTubeless,
            forMotor = forMotor,
            forMobil = forMobil,
            canCallOut = canCallOut,
            photoUri = photoUris.firstOrNull()?.toString().orEmpty(),
        )
    }

    val handleAttemptClose: () -> Unit = {
        if (!isEditMode && currentDraft.hasContent()) {
            showUnsavedDialog = true
        } else {
            onClose()
        }
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Simpan Draf Sebelum Keluar?") },
            text = {
                Text("Anda sudah mengisi rincian lokasi tambal ban ini. Simpan sebagai draf agar data tersimpan saat Anda mencari titik Maps atau nomor kontak.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        TambalBanDraftManager.saveDraft(context, currentDraft)
                        onSaveDraft?.invoke(currentDraft)
                        showUnsavedDialog = false
                        onClose()
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
                            TambalBanDraftManager.clearDraft(context)
                            onClearDraft?.invoke()
                            showUnsavedDialog = false
                            onClose()
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
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // --- HEADER & CLOSE BUTTON ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEditMode) "Edit Lokasi Tambal Ban" else "Tambah Tambal Ban",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (isEditMode) "Perbarui informasi titik bengkel/tambal ban, layanan, atau nomor darurat" else "Bantu warga menemukan tambal ban saat darurat",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = handleAttemptClose,
                modifier = Modifier.testTag("btn_close_sheet"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Tutup",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
                            text = "Melanjutkan draf tambal ban tersimpan",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ToskaPrimaryDeep,
                        )
                    }
                    TextButton(
                        onClick = {
                            TambalBanDraftManager.clearDraft(context)
                            onClearDraft?.invoke()
                            hasLoadedDraft = false
                            name = ""
                            district = defaultDistrict.ifBlank { "Genteng" }
                            address = ""
                            description = ""
                            is24Hours = false
                            isTubeless = true
                            forMotor = true
                            forMobil = false
                            canCallOut = false
                            mapsUrl = ""
                            phoneNumber = ""
                            whatsappNumber = ""
                            photoUris = emptyList()
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
                        text = "Draf lokasi tambal ban berhasil disimpan! Anda aman mencari link/alamat di aplikasi lain.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ToskaPrimaryDeep,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        // --- SALAM SATU ASPAL HERO BANNER ---
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = SoftEmergencyLightGradient,
                    shape = RoundedCornerShape(16.dp),
                ),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = ToskaPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "🛵",
                            fontSize = 22.sp,
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.tambal_ban_salam_aspal_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ToskaPrimaryDeep,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.tambal_ban_salam_aspal_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // --- NAMA LOKASI / BENGKEL ---
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Bengkel / Titik Tambal Ban *") },
            placeholder = { Text("Contoh: Tambal Ban & Tubeless Bungkul 24 Jam") },
            isError = name.isNotBlank() && !isNameValid,
            supportingText = {
                if (name.isNotBlank() && !isNameValid) {
                    Text("Nama minimal 4 karakter", color = AlertCrimson)
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_tambal_name"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ToskaPrimary,
                focusedLabelColor = ToskaPrimary,
            ),
        )

        // --- PINPOINT GPS HELPER CARD ---
        Surface(
            onClick = { if (!isSubmitting) applyTambalBanGps() },
            shape = RoundedCornerShape(14.dp),
            color = if (gpsCoordinatesApplied || isLiveGpsActive) OpenGreenBg else MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                1.dp,
                if (gpsCoordinatesApplied || isLiveGpsActive) OpenGreenText.copy(alpha = 0.4f) else OutlineVariantLight,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_tambal_ban_use_gps_card"),
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
                            "Titik GPS Tambal Ban Terdeteksi (Kec. $district)"
                        } else {
                            stringResource(R.string.tambal_ban_use_current_gps_title)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ToskaOnPrimaryContainer,
                    )
                    Text(
                        text = stringResource(R.string.tambal_ban_use_current_gps_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // --- KECAMATAN SURABAYA ---
        SurabayaDistrictDropdown(
            selectedDistrict = district,
            onSelect = { district = it },
            modifier = Modifier.fillMaxWidth(),
        )

        // --- ALAMAT / PATOKAN ---
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Alamat Lengkap / Patokan Jalan *") },
            placeholder = { Text("Contoh: Jl. Raya Darmo No. 45, depan Taman Bungkul samping SPBU") },
            isError = address.isNotBlank() && !isAddressValid,
            supportingText = {
                if (address.isNotBlank() && !isAddressValid) {
                    Text("Alamat minimal 5 karakter", color = AlertCrimson)
                }
            },
            minLines = 2,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_tambal_address"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ToskaPrimary,
                focusedLabelColor = ToskaPrimary,
            ),
        )

        // --- LAYANAN & KAPABILITAS CHIPS ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Layanan & Kemampuan Tambal Ban",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = is24Hours,
                    onClick = { is24Hours = !is24Hours },
                    label = { Text("Buka 24 Jam") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    modifier = Modifier.testTag("chip_24_jam"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                    ),
                )

                FilterChip(
                    selected = isTubeless,
                    onClick = { isTubeless = !isTubeless },
                    label = { Text("Tubeless") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Build,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    modifier = Modifier.testTag("chip_tubeless"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                    ),
                )

                FilterChip(
                    selected = canCallOut,
                    onClick = { canCallOut = !canCallOut },
                    label = { Text("Panggilan") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.PhoneInTalk,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    modifier = Modifier.testTag("chip_panggilan"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                    ),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = forMotor,
                    onClick = { forMotor = !forMotor },
                    label = { Text("Bisa Sepeda Motor") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.TwoWheeler,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    modifier = Modifier.testTag("chip_motor"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                    ),
                )

                FilterChip(
                    selected = forMobil,
                    onClick = { forMobil = !forMobil },
                    label = { Text("Bisa Mobil") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    modifier = Modifier.testTag("chip_mobil"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                    ),
                )
            }

            if (!hasVehicleType) {
                Text(
                    text = "⚠️ Pilih minimal salah satu: Sepeda Motor atau Mobil.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AlertCrimson,
                )
            }
        }

        // --- FOTO LOKASI / BENGKEL ---
        FormMultiPhotoPickerSection(
            selectedPhotoUris = photoUris,
            onPhotosChanged = { photoUris = it },
            maxPhotos = 3,
            title = "Foto Plang / Lokasi Tambal Ban (Opsional)",
            subtitle = if (isEditMode) "Tambahkan foto baru jika ingin mengganti/memperbarui foto bengkel/lokasi" else "Bisa unggah hingga 3 foto plang, gerobak, atau bengkel tambal ban",
        )

        // --- LINK GOOGLE MAPS ---
        OutlinedTextField(
            value = mapsUrl,
            onValueChange = { mapsUrl = it },
            label = { Text("Link Google Maps (Opsional)") },
            placeholder = { Text("https://maps.app.goo.gl/…") },
            supportingText = {
                Text(stringResource(R.string.safe_link_form_notice_maps))
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_tambal_maps"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ToskaPrimary,
                focusedLabelColor = ToskaPrimary,
            ),
        )

        // --- KONTAK DARURAT (WA & TELEPON) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = whatsappNumber,
                onValueChange = { whatsappNumber = it },
                label = { Text("WhatsApp (Opsional)") },
                placeholder = { Text("081234567890") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_tambal_wa"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ToskaPrimary,
                    focusedLabelColor = ToskaPrimary,
                ),
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Telepon (Opsional)") },
                placeholder = { Text("081234567890") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_tambal_phone"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ToskaPrimary,
                    focusedLabelColor = ToskaPrimary,
                ),
            )
        }

        // --- CATATAN TAMBAHAN / KETERANGAN ---
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Catatan / Keterangan Tambahan") },
            placeholder = { Text("Contoh: Sedia tambal tip top bakar, isi angin nitrogen, tambal cacing tubeless, buka sampai subuh") },
            minLines = 3,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_tambal_desc"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ToskaPrimary,
                focusedLabelColor = ToskaPrimary,
            ),
        )

        // --- SAVE DRAFT BUTTON (NEW FORM ONLY) ---
        if (!isEditMode) {
            OutlinedButton(
                onClick = {
                    TambalBanDraftManager.saveDraft(context, currentDraft)
                    onSaveDraft?.invoke(currentDraft)
                    showDraftSavedNotice = true
                },
                enabled = !isSubmitting && currentDraft.hasContent(),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_save_tambal_draft"),
            ) {
                Icon(Icons.Filled.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Sebagai Draf Tambal Ban")
            }
        }

        // --- SUBMIT BUTTON ---
        Button(
            onClick = {
                if (canSubmit) {
                    onSubmit(
                        name.trim(),
                        description.trim(),
                        district.trim(),
                        address.trim(),
                        is24Hours,
                        isTubeless,
                        forMotor,
                        forMobil,
                        canCallOut,
                        mapsUrl.trim(),
                        phoneNumber.trim(),
                        whatsappNumber.trim(),
                        photoUris.firstOrNull(),
                    )
                }
            },
            enabled = canSubmit,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ToskaPrimary,
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_submit_tambal_ban"),
        ) {
            if (isSubmitting || isScanningPhoto) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isScanningPhoto) "Memindai Keamanan Foto..." else "Menyimpan Data...")
            } else {
                Text(
                    text = if (isEditMode) "Simpan Perubahan Tambal Ban" else "Simpan Lokasi Tambal Ban",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
