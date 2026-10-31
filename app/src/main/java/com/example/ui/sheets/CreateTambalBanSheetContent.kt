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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.SoftEmergencyLightGradient
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep

@Composable
fun CreateTambalBanSheetContent(
    defaultDistrict: String = "Genteng",
    isSubmitting: Boolean = false,
    isScanningPhoto: Boolean = false,
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
    var name by remember { mutableStateOf("") }
    var district by remember { mutableStateOf(defaultDistrict.ifBlank { "Genteng" }) }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var is24Hours by remember { mutableStateOf(false) }
    var isTubeless by remember { mutableStateOf(true) }
    var forMotor by remember { mutableStateOf(true) }
    var forMobil by remember { mutableStateOf(false) }
    var canCallOut by remember { mutableStateOf(false) }
    var mapsUrl by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var whatsappNumber by remember { mutableStateOf("") }
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val isNameValid = name.trim().length >= 4
    val isAddressValid = address.trim().length >= 5
    val hasVehicleType = forMotor || forMobil
    val canSubmit = isNameValid && isAddressValid && hasVehicleType && !isSubmitting && !isScanningPhoto

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
                    text = "Tambah Tambal Ban",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Bantu warga menemukan tambal ban saat darurat",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("btn_close_sheet"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Tutup",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
            maxPhotos = 1,
            title = "Foto Plang / Lokasi Tambal Ban (Opsional)",
            subtitle = "Membantu dulur mengenali lokasi dan gerobak/bengkel tambal ban",
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
                    text = "Simpan Lokasi Tambal Ban",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
