package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.TambalBanSpot
import com.example.data.model.UiState
import com.example.data.util.SafeLinkCategory
import com.example.ui.components.openExternalUrlSafely
import com.example.ui.theme.HeaderMintGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.SoftEmergencyLightGradient
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep

private enum class TambalBanFilter {
    ALL,
    NEARBY_ONLY,
    HOURS_24,
    TUBELESS,
    MOTOR,
    MOBIL,
    CALL_OUT,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambalBanScreen(
    spotsState: UiState<List<TambalBanSpot>>,
    currentUserId: String = "",
    onBackToHome: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onCreateSpot: () -> Unit,
    onRefreshLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler { onBackToHome() }

    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(TambalBanFilter.ALL) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.tambal_ban_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Text(
                            text = "Salam Satu Aspal • Solusi Ban Bocor",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackToHome,
                        modifier = Modifier.testTag("btn_back_tambal_ban"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Beranda",
                            tint = Color.White,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onRefreshLocation,
                        modifier = Modifier.testTag("btn_refresh_tambal_gps"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = "Perbarui Lokasi GPS",
                            tint = Color.White,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ToskaPrimary,
                    titleContentColor = Color.White,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateSpot,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                    )
                },
                text = {
                    Text(
                        text = "Tambah Lokasi",
                        fontWeight = FontWeight.Bold,
                    )
                },
                containerColor = ToskaPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_create_tambal_ban"),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (spotsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = ToskaPrimary)
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = spotsState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                is UiState.Success -> {
                    val allSpots = spotsState.data
                    val filteredSpots = remember(allSpots, searchQuery, selectedFilter) {
                        allSpots.filter { spot ->
                            val matchSearch = searchQuery.isBlank() ||
                                spot.name.contains(searchQuery, ignoreCase = true) ||
                                spot.address.contains(searchQuery, ignoreCase = true) ||
                                spot.district.contains(searchQuery, ignoreCase = true) ||
                                spot.description.contains(searchQuery, ignoreCase = true)

                            val matchFilter = when (selectedFilter) {
                                TambalBanFilter.ALL -> true
                                TambalBanFilter.NEARBY_ONLY -> (spot.distanceKm ?: 999.0) <= 5.0
                                TambalBanFilter.HOURS_24 -> spot.is24Hours
                                TambalBanFilter.TUBELESS -> spot.isTubeless
                                TambalBanFilter.MOTOR -> spot.forMotor
                                TambalBanFilter.MOBIL -> spot.forMobil
                                TambalBanFilter.CALL_OUT -> spot.canCallOut
                            }

                            matchSearch && matchFilter
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("tambal_ban_list"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // --- 1. SALAM SATU ASPAL CALLOUT CARD ---
                        item(key = "salam_satu_aspal_banner") {
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
                                        modifier = Modifier.size(46.dp),
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
                                            text = "Bantu sesama pemotor & driver Surabaya. Laporkan titik tambal ban & saling bantu saat ban bocor di jalanan!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }

                        // --- 2. SEARCH BAR ---
                        item(key = "search_tambal_ban") {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        stringResource(R.string.tambal_ban_search_hint),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = null,
                                        tint = ToskaPrimary,
                                    )
                                },
                                singleLine = true,
                                shape = CircleShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_search_tambal_ban"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ToskaPrimary,
                                    unfocusedBorderColor = OutlineVariantLight,
                                ),
                            )
                        }

                        // --- 3. FILTER CHIPS ---
                        item(key = "filter_chips_tambal_ban") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                FilterChip(
                                    selected = selectedFilter == TambalBanFilter.ALL,
                                    onClick = { selectedFilter = TambalBanFilter.ALL },
                                    label = { Text("Semua (${allSpots.size})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ToskaPrimary,
                                        selectedLabelColor = Color.White,
                                    ),
                                )

                                FilterChip(
                                    selected = selectedFilter == TambalBanFilter.NEARBY_ONLY,
                                    onClick = { selectedFilter = TambalBanFilter.NEARBY_ONLY },
                                    label = { Text("Dekat (< 5 km)") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.MyLocation,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ToskaPrimary,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White,
                                    ),
                                )

                                FilterChip(
                                    selected = selectedFilter == TambalBanFilter.HOURS_24,
                                    onClick = { selectedFilter = TambalBanFilter.HOURS_24 },
                                    label = { Text("24 Jam") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ToskaPrimary,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White,
                                    ),
                                )

                                FilterChip(
                                    selected = selectedFilter == TambalBanFilter.TUBELESS,
                                    onClick = { selectedFilter = TambalBanFilter.TUBELESS },
                                    label = { Text("Tubeless") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Build,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ToskaPrimary,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White,
                                    ),
                                )

                                FilterChip(
                                    selected = selectedFilter == TambalBanFilter.MOTOR,
                                    onClick = { selectedFilter = TambalBanFilter.MOTOR },
                                    label = { Text("Motor") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.TwoWheeler,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ToskaPrimary,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White,
                                    ),
                                )

                                FilterChip(
                                    selected = selectedFilter == TambalBanFilter.MOBIL,
                                    onClick = { selectedFilter = TambalBanFilter.MOBIL },
                                    label = { Text("Mobil") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.DirectionsCar,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ToskaPrimary,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White,
                                    ),
                                )

                                FilterChip(
                                    selected = selectedFilter == TambalBanFilter.CALL_OUT,
                                    onClick = { selectedFilter = TambalBanFilter.CALL_OUT },
                                    label = { Text("Panggilan") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.PhoneInTalk,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ToskaPrimary,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White,
                                    ),
                                )
                            }
                        }

                        // --- 4. LIST OF CARDS ---
                        if (filteredSpots.isEmpty()) {
                            item(key = "empty_tambal_ban") {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(text = "🔧", fontSize = 36.sp)
                                        Text(
                                            text = "Belum Menemukan Tambal Ban",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            text = "Coba ubah kata kunci pencarian atau jadilah yang pertama mengusulkan titik tambal ban di area ini!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredSpots, key = { it.id }) { spot ->
                                TambalBanCardItem(
                                    spot = spot,
                                    currentUserId = currentUserId,
                                    onClick = { onOpenDetail(spot.id) },
                                    onOpenMaps = {
                                        if (spot.mapsUrl.isNotBlank()) {
                                            openExternalUrlSafely(context, spot.mapsUrl, SafeLinkCategory.GOOGLE_MAPS)
                                        } else {
                                            val query = Uri.encode("${spot.name}, ${spot.address}, Kec. ${spot.district}, Surabaya")
                                            val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$query"))
                                            mapsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            try { context.startActivity(mapsIntent) } catch (_: Exception) {}
                                        }
                                    },
                                    onOpenWhatsApp = {
                                        if (spot.whatsappNumber.isNotBlank()) {
                                            val cleanNum = spot.whatsappNumber.replace(Regex("[^0-9]"), "")
                                            val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanNum?text=Halo%20dulur,%20apakah%20tambal%20ban%20masih%20buka?"))
                                            waIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            try { context.startActivity(waIntent) } catch (_: Exception) {}
                                        }
                                    },
                                )
                            }
                        }

                        // Bottom space so FAB doesn't obscure last item
                        item(key = "bottom_spacer") {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TambalBanCardItem(
    spot: TambalBanSpot,
    currentUserId: String = "",
    onClick: () -> Unit,
    onOpenMaps: () -> Unit,
    onOpenWhatsApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("tambal_card_${spot.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, OutlineVariantLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Badges row: Distance, 24 Hours, Tubeless, Verified
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    spot.formattedDistance?.let { distStr ->
                        Surface(
                            shape = CircleShape,
                            color = OpenGreenBg,
                        ) {
                            Text(
                                text = "📍 $distStr",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OpenGreenText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }

                    if (spot.is24Hours) {
                        Surface(
                            shape = CircleShape,
                            color = ToskaPrimary.copy(alpha = 0.12f),
                        ) {
                            Text(
                                text = "24 Jam",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ToskaPrimaryDeep,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }

                    if (spot.isTubeless) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Text(
                                text = "Tubeless",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }

                if (spot.verifiedCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = null,
                            tint = ToskaPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "${spot.verifiedCount} validasi",
                            style = MaterialTheme.typography.labelSmall,
                            color = ToskaPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Spot Title & Vehicle Support
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = spot.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = ToskaPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "${spot.address}, Kec. ${spot.district}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Description / Support snippet
            if (spot.description.isNotBlank()) {
                Text(
                    text = spot.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Quick Actions: Rute Maps, WA, Detail & Pantauan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalButton(
                    onClick = onOpenMaps,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_maps_${spot.id}"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Directions,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rute Maps", style = MaterialTheme.typography.labelMedium)
                }

                if (spot.whatsappNumber.isNotBlank()) {
                    OutlinedButton(
                        onClick = onOpenWhatsApp,
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_wa_${spot.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Chat,
                            contentDescription = "WhatsApp",
                            tint = OpenGreenText,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WA", style = MaterialTheme.typography.labelMedium, color = OpenGreenText)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = if (spot.commentCount > 0) "${spot.commentCount} pantauan ›" else "Pantauan ›",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ToskaPrimary,
                )
            }
        }
    }
}
