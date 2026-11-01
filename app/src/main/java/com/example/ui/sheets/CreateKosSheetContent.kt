package com.example.ui.sheets

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import com.example.data.model.KOS_STANDARD_FACILITIES
import com.example.data.util.LinkSecurityValidator
import com.example.data.util.LinkValidationResult
import com.example.data.util.SafeLinkCategory
import com.example.ui.components.FormMultiPhotoPickerSection
import com.example.ui.components.SurabayaDistrictDropdown
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep

@Composable
fun CreateKosSheetContent(
    defaultDistrict: String,
    isSubmitting: Boolean = false,
    isScanningPhoto: Boolean = false,
    onSubmit: (
        title: String,
        propertyType: String,
        billingPeriod: String,
        price: Long,
        availableRooms: Int,
        facilities: List<String>,
        district: String,
        address: String,
        mapsUrl: String,
        whatsappNumber: String,
        description: String,
        photoUris: List<Uri>,
    ) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by remember { mutableStateOf("") }
    var selectedPropertyType by remember { mutableStateOf("KOS_PUTRA") }
    var selectedBillingPeriod by remember { mutableStateOf("BULANAN") }
    var priceInput by remember { mutableStateOf("") }
    var roomsInput by remember { mutableStateOf("1") }
    var selectedFacilities by remember { mutableStateOf(setOf("KM Dalam", "WiFi", "Parkir Motor")) }
    var district by remember { mutableStateOf(defaultDistrict.ifBlank { "Gubeng" }) }
    var address by remember { mutableStateOf("") }
    var mapsUrl by remember { mutableStateOf("") }
    var whatsappNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val propertyTypes = remember {
        listOf(
            "KOS_PUTRA" to "Kos Putra",
            "KOS_PUTRI" to "Kos Putri",
            "KOS_CAMPUR" to "Kos Pasutri / Campur",
            "KONTRAKAN" to "Kontrakan Rumah",
        )
    }

    val billingPeriods = remember {
        listOf(
            "BULANAN" to "Per Bulan",
            "TAHUNAN" to "Per Tahun",
        )
    }

    val mapsValidation = remember(mapsUrl) {
        LinkSecurityValidator.validateOptionalLink(mapsUrl, SafeLinkCategory.GOOGLE_MAPS)
    }
    val mapsErrorText = (mapsValidation as? LinkValidationResult.Invalid)?.errorMessage

    val parsedPrice = priceInput.toLongOrNull() ?: 0L
    val parsedRooms = (roomsInput.toIntOrNull() ?: 1).coerceIn(1, 100)
    val canSubmit = !isSubmitting &&
        !isScanningPhoto &&
        title.trim().length >= 4 &&
        parsedPrice > 0L &&
        address.trim().length >= 4 &&
        description.trim().length >= 5 &&
        mapsErrorText == null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .imePadding()
            .navigationBarsPadding()
            .testTag("create_kos_sheet_content"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pasang Info Kos & Kontrakan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Tawarkan kos bulanan atau rumah kontrakan tahunan di Surabaya",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("create_kos_close_btn"),
            ) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Tutup")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, OutlineVariantLight),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MintSoftGradient)
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    tint = ToskaPrimaryDeep,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.kos_safety_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = ToskaOnPrimaryContainer,
                    lineHeight = 16.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Foto Kamar / Bangunan Asli (Maks. 4 Foto)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Unggah foto kamar, kamar mandi, dan area parkir agar pencari kos nyaman.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))

        FormMultiPhotoPickerSection(
            selectedPhotoUris = selectedPhotoUris,
            maxPhotos = 4,
            enabled = !isSubmitting,
            onPhotosChanged = { selectedPhotoUris = it },
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Nama Kos / Kontrakan (Wajib)") },
            placeholder = { Text("Contoh: Kos Putri Asri Dekat Kampus B Unair") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ToskaPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("kos_input_title"),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Tipe Hunian",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            propertyTypes.forEach { (key, label) ->
                FilterChip(
                    selected = selectedPropertyType == key,
                    onClick = {
                        selectedPropertyType = key
                        if (key == "KONTRAKAN" && priceInput.isBlank()) {
                            selectedBillingPeriod = "TAHUNAN"
                        }
                    },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimary,
                        selectedLabelColor = Color.White,
                    ),
                    modifier = Modifier.testTag("kos_type_$key"),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Hitungan Sewa (Per Bulan / Per Tahun)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            billingPeriods.forEach { (key, label) ->
                FilterChip(
                    selected = selectedBillingPeriod == key,
                    onClick = { selectedBillingPeriod = key },
                    label = { Text(label, fontWeight = FontWeight.SemiBold) },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimaryDeep,
                        selectedLabelColor = Color.White,
                    ),
                    modifier = Modifier.testTag("kos_billing_$key"),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = priceInput,
                onValueChange = { priceInput = it.filter { ch -> ch.isDigit() } },
                label = {
                    Text(
                        if (selectedBillingPeriod == "TAHUNAN") "Harga / Tahun (Rp)"
                        else "Harga / Bulan (Rp)",
                    )
                },
                placeholder = {
                    Text(if (selectedBillingPeriod == "TAHUNAN") "18000000" else "950000")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ToskaPrimary),
                modifier = Modifier
                    .weight(1.6f)
                    .testTag("kos_input_price"),
            )

            OutlinedTextField(
                value = roomsInput,
                onValueChange = { roomsInput = it.filter { ch -> ch.isDigit() }.take(3) },
                label = { Text("Sisa Kamar/Unit") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ToskaPrimary),
                modifier = Modifier
                    .weight(1f)
                    .testTag("kos_input_rooms"),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Fasilitas Utama",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            KOS_STANDARD_FACILITIES.withIndex().chunked(3).forEach { rowChunk ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowChunk.forEach { (idx, fac) ->
                        val active = fac in selectedFacilities
                        FilterChip(
                            selected = active,
                            onClick = {
                                selectedFacilities = if (active) selectedFacilities - fac else selectedFacilities + fac
                            },
                            label = { Text(fac, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ToskaPrimary.copy(alpha = 0.16f),
                                selectedLabelColor = ToskaPrimaryDeep,
                            ),
                            modifier = Modifier.testTag("kos_fac_$idx"),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SurabayaDistrictDropdown(
            selectedDistrict = district,
            onSelect = { district = it },
            label = "Kecamatan Lokasi Kos / Kontrakan",
            testTag = "kos_district_dropdown",
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Alamat Jalan & Patokan Lokasi (Wajib)") },
            placeholder = { Text("Contoh: Jl. Gebang Lor Gg. 2, 3 menit dari Bundaran ITS") },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ToskaPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("kos_input_address"),
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = mapsUrl,
            onValueChange = { mapsUrl = it },
            label = { Text("Link Google Maps (Opsional)") },
            placeholder = { Text("https://maps.app.goo.gl/... atau kosongkan") },
            leadingIcon = { Icon(imageVector = Icons.Filled.Map, contentDescription = null) },
            isError = mapsErrorText != null,
            supportingText = {
                if (mapsErrorText != null) {
                    Text(
                        text = mapsErrorText,
                        color = AlertCrimson,
                        modifier = Modifier.testTag("kos_maps_error_text"),
                    )
                } else {
                    Text("Opsional — hanya link resmi Google Maps yang diizinkan demi keamanan warga.")
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ToskaPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("kos_input_maps_url"),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = whatsappNumber,
            onValueChange = { whatsappNumber = it },
            label = { Text("Nomor WhatsApp Pemilik / Penjaga Kos") },
            placeholder = { Text("Contoh: 081234567890") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ToskaPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("kos_input_whatsapp"),
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Deskripsi Lengkap & Peraturan Kos (Wajib)") },
            placeholder = { Text("Jelaskan ukuran kamar, daya listrik, jam bertamu, bebas banjir, dll.") },
            minLines = 3,
            maxLines = 6,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ToskaPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("kos_input_description"),
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                onSubmit(
                    title.trim(),
                    selectedPropertyType,
                    selectedBillingPeriod,
                    parsedPrice,
                    parsedRooms,
                    selectedFacilities.toList(),
                    district,
                    address.trim(),
                    mapsUrl.trim(),
                    whatsappNumber.trim(),
                    description.trim(),
                    selectedPhotoUris,
                )
            },
            enabled = canSubmit,
            colors = ButtonDefaults.buttonColors(
                containerColor = ToskaPrimary,
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("kos_submit_btn"),
        ) {
            if (isSubmitting || isScanningPhoto) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isScanningPhoto) "Memindai Keamanan Foto…" else "Memasang Info Kos…")
            } else {
                Text(
                    text = "Tayangkan Info Kos / Kontrakan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
