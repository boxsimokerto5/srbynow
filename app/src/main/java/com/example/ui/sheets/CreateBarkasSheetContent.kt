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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.components.FormMultiPhotoPickerSection
import com.example.ui.components.SurabayaDistrictDropdown
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep

@Composable
fun CreateBarkasSheetContent(
    defaultDistrict: String,
    isSubmitting: Boolean = false,
    isScanningPhoto: Boolean = false,
    onSubmit: (
        title: String,
        category: String,
        itemCondition: String,
        price: Long,
        isNegotiable: Boolean,
        district: String,
        codLocation: String,
        description: String,
        whatsappNumber: String,
        photoUris: List<Uri>,
    ) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ELEKTRONIK") }
    var selectedCondition by remember { mutableStateOf("BEKAS_TERAWAT") }
    var priceInput by remember { mutableStateOf("") }
    var isNegotiable by remember { mutableStateOf(true) }
    var district by remember { mutableStateOf(defaultDistrict.ifBlank { "Genteng" }) }
    var codLocation by remember { mutableStateOf("") }
    var whatsappNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val categories = remember {
        listOf(
            "ELEKTRONIK" to "Elektronik & Gadget",
            "KENDARAAN" to "Sepeda & Otomotif",
            "PERABOT" to "Perabot & Kos",
            "PAKAIAN_HOBI" to "Pakaian & Hobi",
            "LAINNYA" to "Lainnya",
        )
    }

    val conditions = remember {
        listOf(
            "SEPERTI_BARU" to "Seperti Baru",
            "BEKAS_TERAWAT" to "Bekas Terawat",
            "LAYAK_PAKAI" to "Layak Pakai",
            "MINUS_SERVIS" to "Ada Minus / Bahan",
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .imePadding()
            .navigationBarsPadding()
            .testTag("create_barkas_sheet_content"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Jual Barang Bekas Warga",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Pasang gratis • Transaksi langsung COD sesama warga Surabaya",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("create_barkas_close_btn"),
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
                    text = stringResource(R.string.barkas_cod_safety_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = ToskaOnPrimaryContainer,
                    lineHeight = 16.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Foto Barang Asli (Maks. 4 Foto)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Unggah foto kondisi nyata barang agar pembeli yakin sebelum janjian COD.",
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
            label = { Text("Nama Barang Bekas (Wajib)") },
            placeholder = { Text("Contoh: Kipas Angin Cosmos Berdiri Normal") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ToskaPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("barkas_input_title"),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Kategori Barang",
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
            categories.forEach { (key, label) ->
                FilterChip(
                    selected = selectedCategory == key,
                    onClick = { selectedCategory = key },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimary,
                        selectedLabelColor = Color.White,
                    ),
                    modifier = Modifier.testTag("barkas_cat_$key"),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Kondisi Barang",
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
            conditions.forEach { (key, label) ->
                FilterChip(
                    selected = selectedCondition == key,
                    onClick = { selectedCondition = key },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimaryDeep,
                        selectedLabelColor = Color.White,
                    ),
                    modifier = Modifier.testTag("barkas_cond_$key"),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = priceInput,
            onValueChange = { priceInput = it.filter { ch -> ch.isDigit() } },
            label = { Text("Harga Jual (Rp) — isi 0 jika Hibah Gratis") },
            placeholder = { Text("Contoh: 250000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ToskaPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("barkas_input_price"),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFF3FBF7),
            border = BorderStroke(1.dp, OutlineVariantLight),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isNegotiable) "Harga Bisa Nego Sopan" else "Harga Pas / Nett",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Tentukan apakah calon pembeli boleh menawar harga",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = isNegotiable,
                    onCheckedChange = { isNegotiable = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = ToskaPrimary),
                    modifier = Modifier.testTag("barkas_switch_nego"),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SurabayaDistrictDropdown(
            selectedDistrict = district,
            onSelect = { district = it },
            label = "Kecamatan Lokasi COD Surabaya",
            testTag = "barkas_district_dropdown",
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = codLocation,
            onValueChange = { codLocation = it },
            label = { Text("Titik / Patokan COD (Wajib)") },
            placeholder = { Text("Contoh: Indomaret Raya Darmo / Taman Bungkul") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ToskaPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("barkas_input_cod_location"),
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = whatsappNumber,
            onValueChange = { whatsappNumber = it },
            label = { Text("Nomor WhatsApp COD (Opsional)") },
            placeholder = { Text("Contoh: 081234567890 (kosongkan jika hanya via Chat App)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ToskaPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("barkas_input_whatsapp"),
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Deskripsi Kondisi, Kelengkapan & Minus Jujur (Wajib)") },
            placeholder = {
                Text("Jelaskan pemakaian, kelengkapan dus/kabel, dan minus jika ada secara jujur agar berkah…")
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ToskaPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .testTag("barkas_input_description"),
        )

        Spacer(modifier = Modifier.height(20.dp))

        val isFormValid = title.trim().length >= 3 &&
            codLocation.trim().isNotEmpty() &&
            description.trim().length >= 5

        Button(
            onClick = {
                val parsedPrice = priceInput.toLongOrNull() ?: 0L
                onSubmit(
                    title.trim(),
                    selectedCategory,
                    selectedCondition,
                    parsedPrice,
                    isNegotiable,
                    district,
                    codLocation.trim(),
                    description.trim(),
                    whatsappNumber.trim(),
                    selectedPhotoUris,
                )
            },
            enabled = isFormValid && !isSubmitting,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ToskaPrimary,
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("barkas_submit_btn"),
        ) {
            if (isSubmitting || isScanningPhoto) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isScanningPhoto) "Memindai Keamanan Foto…" else "Memasang Iklan…")
            } else {
                Text(
                    text = "Pasang Iklan Barang Bekas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
