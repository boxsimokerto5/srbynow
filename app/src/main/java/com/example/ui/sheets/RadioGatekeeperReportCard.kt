package com.example.ui.sheets

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.location.LocationHelper
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.PendingAmberBg
import com.example.ui.theme.PendingAmberText
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private const val RADIO_REPORTER_PREFS = "arek_surabaya_radio_ss_reporter_prefs"
private const val KEY_SS_NAME = "reporter_name"
private const val KEY_SS_ADDRESS = "reporter_address"
private const val KEY_SS_BIRTH_YEAR = "reporter_birth_year"
private const val KEY_SS_HAS_REPORTED_BEFORE = "has_reported_before"

private val radioReportCategories = listOf(
    "Lalu Lintas / Macet",
    "Kecelakaan",
    "Cuaca / Banjir",
    "Fasilitas / Pohon",
    "Kehilangan / Info Warga",
)

fun appendTranscribedVoiceText(existing: String, spokenRaw: String): String {
    val cleanSpoken = spokenRaw.trim()
    if (cleanSpoken.isEmpty()) return existing
    val capitalized = cleanSpoken.replaceFirstChar { ch ->
        if (ch.isLowerCase()) ch.titlecase(Locale("id", "ID")) else ch.toString()
    }
    val trimmedExisting = existing.trimEnd()
    return if (trimmedExisting.isEmpty()) {
        capitalized
    } else {
        val separator = if (trimmedExisting.endsWith(".") || trimmedExisting.endsWith("!") || trimmedExisting.endsWith("?")) {
            " "
        } else {
            ". "
        }
        "$trimmedExisting$separator$capitalized"
    }
}

fun buildSuaraSurabayaWhatsAppMessage(
    isFirstTimeReporter: Boolean,
    reporterName: String,
    reporterAddress: String,
    birthYear: String,
    category: String,
    incidentLocation: String,
    reportBody: String,
    gpsMapsUrl: String = "",
    timestampWib: String = SimpleDateFormat("HH:mm", Locale("id", "ID")).format(Date()) + " WIB",
): String {
    val cleanName = reporterName.trim().ifBlank { "Warga Surabaya" }
    val cleanAddress = reporterAddress.trim().ifBlank { "Surabaya" }
    val cleanBirthYear = birthYear.trim()
    val cleanLoc = incidentLocation.trim()
    val cleanBody = reportBody.trim()

    return buildString {
        appendLine("Halo Gatekeeper Radio Suara Surabaya 100 FM,")
        appendLine()
        if (isFirstTimeReporter) {
            appendLine("*DATA PELAPOR (Pertama Kali Menghubungi)*")
            appendLine("• Nama: $cleanName")
            appendLine("• Alamat: $cleanAddress")
            appendLine("• Tahun Kelahiran: ${cleanBirthYear.ifBlank { "-" }}")
        } else {
            appendLine("*DATA PELAPOR*")
            appendLine("• Nama: $cleanName")
            appendLine("• Domisili: $cleanAddress")
            if (cleanBirthYear.isNotBlank()) {
                appendLine("• Tahun Kelahiran: $cleanBirthYear")
            }
        }
        appendLine()
        appendLine("*LAPORAN PANTAUAN WARGA*")
        appendLine("• Kategori: $category")
        if (cleanLoc.isNotBlank()) {
            appendLine("• Lokasi & Arah: $cleanLoc")
        }
        appendLine("• Waktu Pantauan: $timestampWib")
        appendLine("• Detail Laporan:")
        appendLine(cleanBody.ifBlank { "(Menyampaikan pantauan situasi terkini)" })
        if (gpsMapsUrl.isNotBlank()) {
            appendLine()
            appendLine("• Titik GPS Lokasi: $gpsMapsUrl")
        }
        appendLine()
        append("_(Dikirim melalui aplikasi warga Arek Surabaya)_")
    }
}

@Composable
fun RadioGatekeeperReportSection(
    whatsappHotline: String,
    defaultReporterName: String = "",
    defaultDistrict: String = "Genteng",
    isLiveGpsActive: Boolean = false,
    onRequestGpsRefresh: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember(context) {
        context.getSharedPreferences(RADIO_REPORTER_PREFS, Context.MODE_PRIVATE)
    }

    val initialSavedName = remember(prefs, defaultReporterName) {
        prefs.getString(KEY_SS_NAME, null)?.takeIf { it.isNotBlank() }
            ?: defaultReporterName.trim()
    }
    val initialSavedAddress = remember(prefs, defaultDistrict) {
        prefs.getString(KEY_SS_ADDRESS, null)?.takeIf { it.isNotBlank() }
            ?: if (defaultDistrict.isNotBlank()) "Kec. $defaultDistrict, Surabaya" else "Surabaya"
    }
    val initialBirthYear = remember(prefs) {
        prefs.getString(KEY_SS_BIRTH_YEAR, "").orEmpty()
    }
    val initialHasReportedBefore = remember(prefs) {
        prefs.getBoolean(KEY_SS_HAS_REPORTED_BEFORE, false)
    }

    var isFirstTimeReporter by rememberSaveable {
        mutableStateOf(!initialHasReportedBefore && initialBirthYear.isBlank())
    }
    var reporterName by rememberSaveable { mutableStateOf(initialSavedName) }
    var reporterAddress by rememberSaveable { mutableStateOf(initialSavedAddress) }
    var birthYear by rememberSaveable { mutableStateOf(initialBirthYear) }
    var showIdentityEditor by rememberSaveable { mutableStateOf(false) }

    var selectedCategory by rememberSaveable { mutableStateOf(radioReportCategories.first()) }
    var incidentLocation by rememberSaveable { mutableStateOf("") }
    var reportBody by rememberSaveable { mutableStateOf("") }
    var gpsMapsUrl by rememberSaveable { mutableStateOf("") }
    var showMessagePreview by rememberSaveable { mutableStateOf(false) }

    var voiceFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    val voicePromptText = stringResource(R.string.radio_voice_prompt)
    val voiceUnavailableText = stringResource(R.string.radio_voice_unavailable)

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull().orEmpty()
            if (spoken.isNotBlank()) {
                reportBody = appendTranscribedVoiceText(reportBody, spoken)
                validationMessage = null
                voiceFeedbackMessage = "Suara berhasil diubah menjadi teks. Silakan tinjau sebelum dikirim."
            }
        }
    }

    val startVoiceDictation: () -> Unit = {
        voiceFeedbackMessage = null
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_PROMPT, voicePromptText)
        }
        try {
            speechRecognizerLauncher.launch(speechIntent)
        } catch (_: ActivityNotFoundException) {
            voiceFeedbackMessage = voiceUnavailableText
        } catch (_: Exception) {
            voiceFeedbackMessage = voiceUnavailableText
        }
    }

    val attachGpsLocation = rememberLocationPermissionWithDisclosure(
        usageContext = LocationUsageContext.INCIDENT_REPORT,
        onPermissionGranted = {
            onRequestGpsRefresh()
            coroutineScope.launch {
                val gps = LocationHelper(context).fetchCurrentLocationOrDefault()
                if (gps.isLiveGps) {
                    if (incidentLocation.isBlank()) {
                        incidentLocation = "Sekitar Kec. ${gps.nearestDistrict}, Surabaya"
                    }
                    gpsMapsUrl = String.format(
                        Locale.US,
                        "https://www.google.com/maps/search/?api=1&query=%.6f,%.6f",
                        gps.lat,
                        gps.lng,
                    )
                }
            }
        },
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineVariantLight),
        modifier = modifier
            .fillMaxWidth()
            .testTag("radio_gatekeeper_form_card"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Header + Official Number Badge 0855-3010-055
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.radio_gatekeeper_form_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Gatekeeper On-Air • Siap Kirim ke WhatsApp Resmi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = OpenGreenBg,
                    border = BorderStroke(1.dp, OpenGreenText.copy(alpha = 0.3f)),
                ) {
                    Text(
                        text = "0855-3010-055",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = OpenGreenText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            // First-Time Reporter vs Returning Reporter Mode Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = isFirstTimeReporter,
                    onClick = {
                        isFirstTimeReporter = true
                        if (birthYear.isBlank()) showIdentityEditor = true
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.radio_first_time_label),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimary,
                        selectedLabelColor = Color.White,
                    ),
                    shape = CircleShape,
                    modifier = Modifier.testTag("radio_first_time_chip"),
                )
                FilterChip(
                    selected = !isFirstTimeReporter,
                    onClick = {
                        isFirstTimeReporter = false
                        validationMessage = null
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.radio_returning_label),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToskaPrimaryDeep,
                        selectedLabelColor = Color.White,
                    ),
                    shape = CircleShape,
                    modifier = Modifier.testTag("radio_returning_chip"),
                )
            }

            // Reporter Identity Summary / Inline Expandable Editor
            val isIdentityComplete = reporterName.trim().length >= 2 &&
                reporterAddress.trim().length >= 3 &&
                (!isFirstTimeReporter || birthYear.trim().length == 4)

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isIdentityComplete) OpenGreenBg.copy(alpha = 0.55f) else PendingAmberBg,
                border = BorderStroke(
                    1.dp,
                    if (isIdentityComplete) OpenGreenText.copy(alpha = 0.25f) else PendingAmberText.copy(alpha = 0.3f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { showIdentityEditor = !showIdentityEditor }
                    .testTag("radio_toggle_identity_btn"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = if (isIdentityComplete) Icons.Filled.CheckCircle else Icons.Filled.Person,
                        contentDescription = null,
                        tint = if (isIdentityComplete) OpenGreenText else PendingAmberText,
                        modifier = Modifier.size(18.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        val summaryTitle = buildString {
                            append(reporterName.ifBlank { "Isi Nama Pelapor" })
                            append(" • ")
                            append(reporterAddress.ifBlank { "Isi Alamat" })
                            if (isFirstTimeReporter) {
                                append(" • Thn: ")
                                append(birthYear.ifBlank { "Wajib Diisi" })
                            }
                        }
                        Text(
                            text = summaryTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isIdentityComplete) ToskaOnPrimaryContainer else PendingAmberText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (isFirstTimeReporter && birthYear.trim().length != 4) {
                                "Ketuk untuk melengkapi Nama, Alamat & Tahun Kelahiran (wajib pelapor pertama)"
                            } else {
                                "Data pelapor tersimpan otomatis • Ketuk untuk ubah identitas"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(
                        imageVector = if (showIdentityEditor) Icons.Filled.ExpandLess else Icons.Filled.Edit,
                        contentDescription = "Ubah Data Pelapor",
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            AnimatedVisibility(visible = showIdentityEditor) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.radio_gatekeeper_rule_notice),
                        style = MaterialTheme.typography.labelSmall,
                        color = ToskaPrimaryDeep,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = reporterName,
                            onValueChange = {
                                reporterName = it
                                validationMessage = null
                            },
                            label = { Text("Nama Lengkap *") },
                            placeholder = { Text("Cak Budi") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            modifier = Modifier
                                .weight(1.35f)
                                .testTag("radio_input_reporter_name"),
                        )
                        OutlinedTextField(
                            value = birthYear,
                            onValueChange = { input ->
                                birthYear = input.filter { it.isDigit() }.take(4)
                                validationMessage = null
                            },
                            label = { Text(if (isFirstTimeReporter) "Thn Lahir *" else "Thn Lahir") },
                            placeholder = { Text("1992") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(0.95f)
                                .testTag("radio_input_birth_year"),
                        )
                    }
                    OutlinedTextField(
                        value = reporterAddress,
                        onValueChange = {
                            reporterAddress = it
                            validationMessage = null
                        },
                        label = { Text("Alamat / Domisili Lengkap *") },
                        placeholder = { Text("Contoh: Jl. Ketabang Kali No. 10, Kec. Genteng, Surabaya") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("radio_input_reporter_address"),
                    )
                }
            }

            // Category Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                radioReportCategories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ToskaPrimary.copy(alpha = 0.16f),
                            selectedLabelColor = ToskaPrimaryDeep,
                        ),
                    )
                }
            }

            // Incident Location + 1-Tap GPS Pinpoint Button
            OutlinedTextField(
                value = incidentLocation,
                onValueChange = { incidentLocation = it },
                label = { Text("Lokasi Kejadian & Arah Lajur") },
                placeholder = { Text("Contoh: Jl. A. Yani arah Waru depan Royal Plaza") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(19.dp),
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = attachGpsLocation,
                        modifier = Modifier.testTag("radio_use_gps_btn"),
                    ) {
                        Icon(
                            imageVector = if (gpsMapsUrl.isNotBlank() || isLiveGpsActive) {
                                Icons.Filled.GpsFixed
                            } else {
                                Icons.Filled.MyLocation
                            },
                            contentDescription = "Lampirkan Titik GPS Saat Ini",
                            tint = if (gpsMapsUrl.isNotBlank()) OpenGreenText else ToskaPrimaryDeep,
                        )
                    }
                },
                supportingText = if (gpsMapsUrl.isNotBlank()) {
                    {
                        Text(
                            text = "Titik koordinat Google Maps siap disertakan ke laporan WA",
                            style = MaterialTheme.typography.labelSmall,
                            color = OpenGreenText,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                } else {
                    null
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ToskaPrimary,
                    focusedLabelColor = ToskaPrimaryDeep,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("radio_input_location"),
            )

            // Report Body + Voice-to-Text (Bahasa Indonesia) Action Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Isi Laporan / Pantauan *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Surface(
                    onClick = startVoiceDictation,
                    shape = CircleShape,
                    color = ToskaPrimary,
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("radio_voice_to_text_btn"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Dikte Suara ke Teks",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = stringResource(R.string.radio_voice_btn_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }

            OutlinedTextField(
                value = reportBody,
                onValueChange = {
                    reportBody = it
                    validationMessage = null
                },
                placeholder = {
                    Text(
                        text = "Ketik laporan atau tekan tombol 'Dikte Suara' di atas untuk bicara langsung menjadi teks…",
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                trailingIcon = if (reportBody.isNotBlank()) {
                    {
                        IconButton(
                            onClick = { reportBody = "" },
                            modifier = Modifier.testTag("radio_clear_report_btn"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Bersihkan Teks Laporan",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    null
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ToskaPrimary,
                    focusedLabelColor = ToskaPrimaryDeep,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("radio_input_report_body"),
            )

            if (!voiceFeedbackMessage.isNullOrBlank()) {
                Text(
                    text = voiceFeedbackMessage.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = ToskaPrimaryDeep,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (!validationMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AlertCrimson.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("radio_form_validation_banner"),
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = AlertCrimson,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = validationMessage.orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = AlertCrimson,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Collapsible Live Preview of Formatted WhatsApp Message
            val formattedPreview = remember(
                isFirstTimeReporter,
                reporterName,
                reporterAddress,
                birthYear,
                selectedCategory,
                incidentLocation,
                reportBody,
                gpsMapsUrl,
            ) {
                buildSuaraSurabayaWhatsAppMessage(
                    isFirstTimeReporter = isFirstTimeReporter,
                    reporterName = reporterName,
                    reporterAddress = reporterAddress,
                    birthYear = birthYear,
                    category = selectedCategory,
                    incidentLocation = incidentLocation,
                    reportBody = reportBody,
                    gpsMapsUrl = gpsMapsUrl,
                )
            }

            AnimatedVisibility(visible = showMessagePreview) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MintSoftGradient)
                        .padding(12.dp)
                        .testTag("radio_whatsapp_preview_box"),
                ) {
                    Text(
                        text = formattedPreview,
                        style = MaterialTheme.typography.bodySmall,
                        color = ToskaOnPrimaryContainer,
                    )
                }
            }

            // Primary Submit to Official WhatsApp Suara Surabaya (0855-3010-055)
            Button(
                onClick = {
                    val trimmedName = reporterName.trim()
                    val trimmedAddr = reporterAddress.trim()
                    val trimmedYear = birthYear.trim()
                    val trimmedBody = reportBody.trim()

                    when {
                        trimmedName.length < 2 || trimmedAddr.length < 3 -> {
                            showIdentityEditor = true
                            validationMessage = "Mohon lengkapi Nama dan Alamat domisili pelapor terlebih dahulu."
                        }
                        isFirstTimeReporter && trimmedYear.length != 4 -> {
                            showIdentityEditor = true
                            validationMessage = "Sesuai ketentuan WA Suara Surabaya, mohon isi Tahun Kelahiran (4 angka, misal 1992) untuk pelapor pertama kali."
                        }
                        trimmedBody.length < 4 -> {
                            validationMessage = "Mohon isi detail laporan atau gunakan tombol 'Dikte Suara' terlebih dahulu."
                        }
                        else -> {
                            validationMessage = null
                            prefs.edit()
                                .putString(KEY_SS_NAME, trimmedName)
                                .putString(KEY_SS_ADDRESS, trimmedAddr)
                                .putString(KEY_SS_BIRTH_YEAR, trimmedYear)
                                .putBoolean(KEY_SS_HAS_REPORTED_BEFORE, true)
                                .apply()

                            val finalMessage = buildSuaraSurabayaWhatsAppMessage(
                                isFirstTimeReporter = isFirstTimeReporter,
                                reporterName = trimmedName,
                                reporterAddress = trimmedAddr,
                                birthYear = trimmedYear,
                                category = selectedCategory,
                                incidentLocation = incidentLocation,
                                reportBody = trimmedBody,
                                gpsMapsUrl = gpsMapsUrl,
                            )
                            openOfficialSuaraSurabayaWhatsApp(
                                context = context,
                                rawPhone = whatsappHotline,
                                formattedMessage = finalMessage,
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("radio_whatsapp_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366),
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.radio_ss_whatsapp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            TextButton(
                onClick = { showMessagePreview = !showMessagePreview },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("radio_toggle_preview_btn"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Visibility,
                    contentDescription = null,
                    tint = ToskaPrimaryDeep,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showMessagePreview) "Sembunyikan Pratinjau Pesan WA" else "Lihat Pratinjau Format Pesan WA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = ToskaPrimaryDeep,
                )
                Icon(
                    imageVector = if (showMessagePreview) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = ToskaPrimaryDeep,
                    modifier = Modifier.size(16.dp),
                )
            }

            HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.5f))

            Text(
                text = stringResource(R.string.radio_safety_driving_notice),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

fun openOfficialSuaraSurabayaWhatsApp(
    context: Context,
    rawPhone: String,
    formattedMessage: String,
) {
    val digitsOnly = rawPhone.filter { it.isDigit() }.let { digits ->
        when {
            digits.startsWith("0") -> "62" + digits.drop(1)
            digits.isBlank() -> "628553010055"
            else -> digits
        }
    }
    val encodedText = Uri.encode(formattedMessage)
    val url = "https://wa.me/$digitsOnly?text=$encodedText"
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // Ignore if browser/WhatsApp handler is absent in unit test environment
    }
}
