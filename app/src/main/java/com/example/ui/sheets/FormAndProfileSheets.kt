package com.example.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BookmarkItem
import com.example.data.model.NearbyProfile
import com.example.data.model.UserAccount
import com.example.data.util.SurabayaDistricts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.components.GoogleMapsSheetHandleHeader
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimaryDeep
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateIncidentSheetContent(
    defaultDistrict: String,
    onSubmit: (title: String, desc: String, category: String, severity: String, district: String, address: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("LALU_LINTAS") }
    var severity by remember { mutableStateOf("INFO") }
    var district by remember { mutableStateOf(defaultDistrict) }
    var address by remember { mutableStateOf("") }

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
            subtitle = "Bagikan info jalan, cuaca, atau kondisi fasilitas kota secara akurat.",
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Judul Kejadian") },
            placeholder = { Text("Contoh: Pohon tumbang di Jl. Darmo") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_incident_title"),
        )

        Text("Kategori & Tingkat Urgensi", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                "LALU_LINTAS" to "Lalu Lintas",
                "CUACA_BANJIR" to "Cuaca/Banjir",
                "DARURAT" to "Darurat",
                "FASILITAS" to "Fasilitas",
                "INFO_WARGA" to "Info Warga",
            ).forEach { (key, label) ->
                FilterChip(
                    selected = category == key,
                    onClick = { category = key },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    shape = CircleShape,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("INFO" to "Info Biasa", "PENTING" to "Penting", "DARURAT" to "Darurat").forEach { (key, label) ->
                FilterChip(
                    selected = severity == key,
                    onClick = { severity = key },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    shape = CircleShape,
                )
            }
        }

        DistrictPickerChips(selectedDistrict = district, onSelect = { district = it })

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Nama Jalan / Patokan Lokasi") },
            placeholder = { Text("Contoh: Depan Taman Bungkul arah Selatan") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_incident_address"),
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Kronologi / Detail Situasi") },
            shape = RoundedCornerShape(16.dp),
            minLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_incident_desc"),
        )

        Button(
            onClick = {
                onSubmit(title, description, category, severity, district, address)
            },
            enabled = title.trim().length >= 3 && description.trim().length >= 5 && address.trim().length >= 3,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_incident_btn"),
        ) {
            Text("Publikasikan Laporan Warga")
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProposeEventSheetContent(
    defaultDistrict: String,
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
    ) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("FESTIVAL") }
    var dateText by remember { mutableStateOf("Minggu, 29 Nov 2026") }
    var timeText by remember { mutableStateOf("18.30 - 21.30 WIB") }
    var venue by remember { mutableStateOf("") }
    var district by remember { mutableStateOf(defaultDistrict) }
    var address by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("Gratis") }
    var description by remember { mutableStateOf("") }

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
            subtitle = "Usulan event akan masuk ke antrean moderasi dan tayang publik setelah disetujui Admin.",
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Nama Event / Acara") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_event_title"),
        )

        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                "FESTIVAL" to "Festival",
                "MUSIK_SENI" to "Musik & Seni",
                "KULINER_BAZAR" to "Bazar UMKM",
                "KOMUNITAS" to "Komunitas",
                "OLAHRAGA" to "Olahraga",
            ).forEach { (k, label) ->
                FilterChip(
                    selected = category == k,
                    onClick = { category = k },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    shape = CircleShape,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = dateText,
                onValueChange = { dateText = it },
                label = { Text("Tanggal") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = timeText,
                onValueChange = { timeText = it },
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
                label = { Text("Tiket") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.weight(0.8f),
            )
        }

        DistrictPickerChips(selectedDistrict = district, onSelect = { district = it })

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Alamat Lengkap") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_event_address"),
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Deskripsi Kegiatan") },
            shape = RoundedCornerShape(16.dp),
            minLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_event_desc"),
        )

        Button(
            onClick = {
                onSubmit(title, category, dateText, timeText, venue, district, address, price, description)
            },
            enabled = title.trim().length >= 3 && venue.trim().length >= 2 && address.trim().length >= 3 && description.trim().length >= 5,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_event_proposal_btn"),
        ) {
            Text("Kirim Usulan ke Admin")
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateCulinarySheetContent(
    defaultDistrict: String,
    onSubmit: (
        name: String,
        category: String,
        desc: String,
        district: String,
        address: String,
        priceRange: String,
        openHour: Int,
        closeHour: Int,
    ) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("LEGENDARIS") }
    var district by remember { mutableStateOf(defaultDistrict) }
    var address by remember { mutableStateOf("") }
    var priceRange by remember { mutableStateOf("Rp 18.000 - Rp 32.000") }
    var openHourStr by remember { mutableStateOf("9") }
    var closeHourStr by remember { mutableStateOf("22") }
    var description by remember { mutableStateOf("") }

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
            subtitle = "Tambahkan warung legendaris, penyetan malam, atau kedai kopi favoritmu.",
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Warung / Tempat Makan") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_kuliner_name"),
        )

        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                "RAWON_SOTO" to "Rawon & Soto",
                "PENYETAN_SAMBAL" to "Penyetan",
                "LEGENDARIS" to "Legendaris",
                "KOPI_NONGKRONG" to "Kopi & Kafe",
                "JAJANAN_MALAM" to "Jajanan Malam",
            ).forEach { (k, label) ->
                FilterChip(
                    selected = category == k,
                    onClick = { category = k },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    shape = CircleShape,
                )
            }
        }

        DistrictPickerChips(selectedDistrict = district, onSelect = { district = it })

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Alamat Jalan") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_kuliner_address"),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = priceRange,
                onValueChange = { priceRange = it },
                label = { Text("Kisaran Harga") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.weight(1.3f),
            )
            OutlinedTextField(
                value = openHourStr,
                onValueChange = { openHourStr = it.filter(Char::isDigit).take(2) },
                label = { Text("Buka") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.weight(0.6f),
            )
            OutlinedTextField(
                value = closeHourStr,
                onValueChange = { closeHourStr = it.filter(Char::isDigit).take(2) },
                label = { Text("Tutup") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.weight(0.6f),
            )
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Menu Andalan & Keunggulan") },
            shape = RoundedCornerShape(16.dp),
            minLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_kuliner_desc"),
        )

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
                )
            },
            enabled = name.trim().length >= 2 && address.trim().length >= 3 && description.trim().length >= 5,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_kuliner_btn"),
        ) {
            Text("Simpan Kuliner ke Direktori")
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
    onToggleAdmin: (Boolean) -> Unit,
    onSaveProfile: (name: String, district: String, bio: String, interests: String, radiusKm: Double, discoverable: Boolean) -> Unit,
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
                        modifier = Modifier.testTag("admin_mode_switch"),
                    )
                }
            }

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Nama Tampilan Warga") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio Singkat Sapaan") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = interests,
                onValueChange = { interests = it },
                label = { Text("Minat / Hobi (pisahkan koma)") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        DistrictPickerChips(selectedDistrict = district, onSelect = { district = it })

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
                        modifier = Modifier.testTag("discoverable_switch"),
                    )
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
                )
            },
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_profile_privacy_btn"),
        ) {
            Text("Simpan Pengaturan")
        }

        if (!focusPrivacyOnly) {
            HorizontalDivider()
            OutlinedButton(
                onClick = onSignOut,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DistrictPickerChips(
    selectedDistrict: String,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Kecamatan di Surabaya", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SurabayaDistricts.names().take(8).forEach { name ->
                FilterChip(
                    selected = selectedDistrict.equals(name, ignoreCase = true),
                    onClick = { onSelect(name) },
                    label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                    shape = CircleShape,
                )
            }
        }
    }
}
