package com.example.ui.sheets

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.HowToVote
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SurabayaDistrictDropdown
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep

@Composable
fun CreatePollSheetContent(
    isSubmitting: Boolean,
    onSubmit: (title: String, description: String, category: String, district: String, options: List<String>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("TRANSPORTASI") }
    var selectedDistrict by remember { mutableStateOf("Genteng") }

    val options = remember {
        mutableStateListOf(
            "Sangat Setuju & Perlu Diterapkan Segera",
            "Perlu Evaluasi & Kajian Lebih Matang",
            "Tidak Setuju / Belum Prioritas",
        )
    }

    var validationError by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        "TRANSPORTASI" to "Transportasi",
        "TATA_KOTA" to "Tata Kota",
        "LINGKUNGAN" to "Lingkungan",
        "KULINER_WISATA" to "Wisata & Kuliner",
        "LAYANAN_PUBLIK" to "Layanan Publik",
        "UMUM" to "Umum",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = ToskaPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(42.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.HowToVote,
                            contentDescription = null,
                            tint = ToskaPrimaryDeep,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Usulkan Polling Warga",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Suarakan aspirasi & dengar pendapat Arek Surabaya",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_create_poll_button")) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup formulir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Info Banner
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ToskaPrimary.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = ToskaPrimaryDeep,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Usulan polling warga akan ditinjau tim moderasi sebelum diterbitkan resmi ke publik guna menjaga ketertiban, mencegah hoax, dan memastikan aspirasi berkualitas.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Judul Polling
        Text(
            text = "Judul Polling / Topik Aspirasi *",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                validationError = null
            },
            placeholder = { Text("Contoh: Usulan Penataan Kantong Parkir Jl. Tunjungan") },
            singleLine = false,
            maxLines = 3,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_poll_title"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ToskaPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Kategori Chip Selector
        Text(
            text = "Kategori Topik *",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
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
                    label = { Text(catLabel, fontSize = 12.sp) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimary,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Wilayah Kecamatan Dropdown
        Text(
            text = "Kecamatan Relevan *",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        SurabayaDistrictDropdown(
            selectedDistrict = selectedDistrict,
            onSelect = { selectedDistrict = it },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Deskripsi / Latar Belakang
        Text(
            text = "Penjelasan & Latar Belakang Masalah *",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
                validationError = null
            },
            placeholder = { Text("Jelaskan mengapa isu ini penting disurvei kepada warga Surabaya, konteks di lapangan, serta dampak yang diharapkan...") },
            minLines = 3,
            maxLines = 6,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_poll_description"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ToskaPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Opsi Jawaban Dinamis (2 - 5)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Pilihan Opsi Suara (${options.size}/5) *",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (options.size < 5) {
                OutlinedButton(
                    onClick = {
                        options.add("Opsi Baru ${options.size + 1}")
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ToskaPrimaryDeep),
                    border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("btn_add_poll_option"),
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Opsi", fontSize = 12.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        options.forEachIndexed { index, optText ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = ToskaPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(28.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ToskaPrimaryDeep,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = optText,
                    onValueChange = { newVal ->
                        options[index] = newVal
                        validationError = null
                    },
                    singleLine = true,
                    placeholder = { Text("Opsi ${index + 1}") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_poll_option_$index"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ToskaPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
                if (options.size > 2) {
                    IconButton(
                        onClick = {
                            options.removeAt(index)
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus opsi",
                            tint = AlertCrimson.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        if (validationError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = validationError.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = AlertCrimson,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Submit
        Button(
            onClick = {
                val cleanTitle = title.trim()
                val cleanDesc = description.trim()
                val validOptions = options.map { it.trim() }.filter { it.isNotBlank() }

                if (cleanTitle.length < 6) {
                    validationError = "Judul usulan polling minimal 6 karakter."
                    return@Button
                }
                if (cleanDesc.length < 15) {
                    validationError = "Penjelasan usulan minimal 15 karakter agar warga memahami konteks."
                    return@Button
                }
                if (validOptions.size < 2) {
                    validationError = "Mohon isi minimal 2 pilihan opsi suara yang jelas."
                    return@Button
                }

                onSubmit(cleanTitle, cleanDesc, selectedCategory, selectedDistrict, validOptions)
            },
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_poll_proposal_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimary),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mengirim Usulan...")
            } else {
                Icon(
                    imageVector = Icons.Default.HowToVote,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ajukan Usulan Polling Warga",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
