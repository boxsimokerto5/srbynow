package com.example.ui.sheets

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FormMultiPhotoPickerSection
import com.example.ui.components.SurabayaDistrictDropdown
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.SoftEmergencyLightGradient
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep

@Composable
fun CreateServiceSheetContent(
    defaultDistrict: String,
    isSubmitting: Boolean = false,
    isScanningPhoto: Boolean = false,
    onSubmit: (
        title: String,
        category: String,
        price: Long,
        priceUnit: String,
        district: String,
        coverageArea: String,
        phoneNumber: String,
        whatsappNumber: String,
        description: String,
        photoUris: List<Uri>,
    ) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ELEKTRONIK_AC") }
    var priceInput by remember { mutableStateOf("75000") }
    var priceUnit by remember { mutableStateOf("unit") }
    var district by remember { mutableStateOf(defaultDistrict.ifBlank { "Gubeng" }) }
    var coverageArea by remember { mutableStateOf("Seluruh Surabaya & Sekitarnya") }
    var whatsappNumber by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val categories = listOf(
        "ELEKTRONIK_AC" to "AC & Elektronik",
        "PERBAIKAN_RUMAH" to "Tukang & Renovasi",
        "KEBERSIHAN" to "Kebersihan & Sedot",
        "OTOMOTIF" to "Otomotif & Bengkel",
        "PENDIDIKAN" to "Les & Edukasi",
        "LAINNYA" to "Lainnya",
    )

    val commonUnits = listOf("unit", "jam", "hari", "titik", "kunjungan", "meter", "set")

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .imePadding()
            .navigationBarsPadding()
            .testTag("create_service_sheet_content"),
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Pasang Promosi Jasa Warga",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Tawarkan keahlian atau usaha Anda ke sesama warga Surabaya",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("create_service_close_btn"),
            ) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Tutup")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Anti-DP Safety Rule Notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.35f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftEmergencyLightGradient)
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    tint = AlertCrimson,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Aturan Komunitas: Dilarang meminta uang muka (DP) sebelum pekerjaan selesai. Transaksi dilakukan aman secara COD / setelah hasil kerja dicek pemesan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AlertCrimson,
                    lineHeight = 16.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title Input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Nama Jasa / Usaha (Wajib)") },
            placeholder = { Text("Contoh: Cuci AC Dingin Maknyus Abdulah") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_service_title"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ToskaPrimary,
            ),
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Category Chips
        Text(
            text = "Kategori Jasa",
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
            categories.forEach { (catKey, catLabel) ->
                val isSelected = selectedCategory == catKey
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = catKey },
                    label = { Text(catLabel, style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimary,
                        selectedLabelColor = Color.White,
                    ),
                    modifier = Modifier.testTag("chip_cat_$catKey"),
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Price & Unit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = priceInput,
                onValueChange = { priceInput = it.filter { ch -> ch.isDigit() } },
                label = { Text("Tarif / Biaya (Rp)") },
                placeholder = { Text("75000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("input_service_price"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ToskaPrimary,
                ),
            )

            OutlinedTextField(
                value = priceUnit,
                onValueChange = { priceUnit = it },
                label = { Text("Satuan (per ..)") },
                placeholder = { Text("unit / jam / titik") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_service_unit"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ToskaPrimary,
                ),
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Common Unit Quick Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            commonUnits.forEach { unit ->
                Surface(
                    color = if (priceUnit.equals(unit, ignoreCase = true)) ToskaPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (priceUnit.equals(unit, ignoreCase = true)) ToskaPrimary else Color.Transparent
                    ),
                    modifier = Modifier
                        .clickable { priceUnit = unit }
                        .padding(vertical = 2.dp),
                ) {
                    Text(
                        text = "per $unit",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (priceUnit.equals(unit, ignoreCase = true)) ToskaPrimaryDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Contacts: WhatsApp & Phone
        OutlinedTextField(
            value = whatsappNumber,
            onValueChange = { whatsappNumber = it },
            label = { Text("Nomor WhatsApp (Wajib)") },
            placeholder = { Text("081234567890") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_service_wa"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ToskaPrimary,
            ),
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Nomor Telepon Panggilan (Opsional)") },
            placeholder = { Text("081234567890") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_service_phone"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ToskaPrimary,
            ),
        )

        Spacer(modifier = Modifier.height(14.dp))

        // District & Coverage
        SurabayaDistrictDropdown(
            selectedDistrict = district,
            onSelect = { district = it },
            label = "Kecamatan Tempat Tinggal / Domisili Usaha",
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = coverageArea,
            onValueChange = { coverageArea = it },
            label = { Text("Wilayah Jangkauan Layanan") },
            placeholder = { Text("Contoh: Gubeng, Sukolilo & seluruh Surabaya") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_service_coverage"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ToskaPrimary,
            ),
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Keterangan Lengkap & Garansi (Wajib)") },
            placeholder = { Text("Jelaskan rincian layanan, keahlian, pengalaman, jenis peralatan yang dibawa, serta garansi hasil pengerjaan...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .testTag("input_service_desc"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ToskaPrimary,
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Photo Flyer Upload Section (Multi Photo Picker)
        Text(
            text = "Foto Flyer / Brosur / Bukti Hasil Kerja (Maks. 3 Foto)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Unggah foto brosur promosi atau dokumentasi hasil kerja Anda untuk meyakinkan calon pelanggan.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))

        FormMultiPhotoPickerSection(
            selectedPhotoUris = selectedPhotoUris,
            maxPhotos = 3,
            onPhotosChanged = { selectedPhotoUris = it },
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        val isFormValid = title.isNotBlank() && (whatsappNumber.isNotBlank() || phoneNumber.isNotBlank()) && description.isNotBlank()

        Button(
            onClick = {
                val parsedPrice = priceInput.toLongOrNull() ?: 0L
                onSubmit(
                    title,
                    selectedCategory,
                    parsedPrice,
                    priceUnit.ifBlank { "unit" },
                    district,
                    coverageArea,
                    phoneNumber,
                    whatsappNumber,
                    description,
                    selectedPhotoUris,
                )
            },
            enabled = isFormValid && !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_submit_service"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ToskaPrimary,
                contentColor = Color.White,
            ),
        ) {
            if (isSubmitting || isScanningPhoto) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isScanningPhoto) "Memindai Keamanan Foto..." else "Menyimpan Promosi...")
            } else {
                Text(
                    text = "Pasang Promosi Jasa Sekarang",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
