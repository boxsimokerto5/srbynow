package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.TransitCategory
import com.example.data.model.TransitRoute
import com.example.data.seed.SurabayaTransitSeed
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.HeaderMintGradient
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransitScreen(
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler { onBackToHome() }
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TransitCategory.SEMUA) }
    var selectedRouteForDetail by remember { mutableStateOf<TransitRoute?>(null) }
    var showGuideDialog by remember { mutableStateOf(false) }

    val allRoutes = remember { SurabayaTransitSeed.allRoutes }

    val filteredRoutes = remember(searchQuery, selectedCategory) {
        val query = searchQuery.trim().lowercase()
        allRoutes.filter { route ->
            val matchCategory = selectedCategory == TransitCategory.SEMUA || route.category == selectedCategory
            val matchQuery = if (query.isBlank()) {
                true
            } else {
                route.code.lowercase().contains(query) ||
                    route.name.lowercase().contains(query) ||
                    route.fleetType.lowercase().contains(query) ||
                    route.origin.lowercase().contains(query) ||
                    route.destination.lowercase().contains(query) ||
                    route.keyStops.any { it.lowercase().contains(query) } ||
                    route.transferHubs.any { it.lowercase().contains(query) }
            }
            matchCategory && matchQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("transit_screen_container")
    ) {
        // 1. Top Bar
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToHome,
                    modifier = Modifier.testTag("transit_btn_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali ke Beranda",
                        tint = ToskaPrimaryDeep
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Rute Bis & Feeder Surabaya",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ToskaPrimaryDeep,
                        maxLines = 1,
                    )
                    Text(
                        text = "WiraWiri • Suroboyo Bus • Trans Semanggi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                IconButton(
                    onClick = { showGuideDialog = true },
                    modifier = Modifier.testTag("transit_btn_open_guide")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Panduan & Tarif",
                        tint = ToskaPrimary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // 2. Search Field
            item(key = "transit_search") {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transit_search_input"),
                        placeholder = {
                            Text(
                                "Cari rute (FD01, R1, 2K, Wiyung, Benowo, HiAce…)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Hapus pencarian",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ToskaPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                        )
                    )
                }
            }

            // 3. Category Filter Chips
            item(key = "transit_categories") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(TransitCategory.entries.toTypedArray(), key = { it.name }) { cat ->
                        val count = if (cat == TransitCategory.SEMUA) {
                            allRoutes.size
                        } else {
                            allRoutes.count { it.category == cat }
                        }
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(
                                    text = "${cat.label} ($count)",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ToskaPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) ToskaPrimary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // 4. Integrated 2-Hour Ticket Highlight Banner
            item(key = "transit_ticket_banner") {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(HeaderMintGradient)
                            .clickable { showGuideDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(ToskaPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SyncAlt,
                                    contentDescription = null,
                                    tint = ToskaPrimaryDeep,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Tarif Integrasi 2 Jam: Rp 5.000",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ToskaPrimaryDeep
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = AlertCrimson,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "GRATIS PINDAH",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Pindah antar Feeder WiraWiri & Suroboyo Bus dalam 2 jam tanpa bayar lagi. Ketuk untuk panduan & cara naik →",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 5. Route Count Summary Header
            item(key = "transit_count_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Rute Aktif (${filteredRoutes.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "100% Cashless",
                        style = MaterialTheme.typography.labelSmall,
                        color = ToskaPrimaryDeep,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 6. Routes List
            if (filteredRoutes.isEmpty()) {
                item(key = "transit_empty_state") {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DirectionsBus,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Rute Tidak Ditemukan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Coba gunakan kata kunci lain seperti nomor rute (FD01, R1, 2K) atau nama jalan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredRoutes, key = { it.id }) { route ->
                    TransitRouteCard(
                        route = route,
                        onOpenDetail = { selectedRouteForDetail = route },
                        onShare = { shareRouteInfo(context, route) }
                    )
                }
            }
        }
    }

    // Modal Sheet Detail Rute & Titik Halte Lengkap
    selectedRouteForDetail?.let { route ->
        TransitDetailModalSheet(
            route = route,
            onDismiss = { selectedRouteForDetail = null },
            onShare = { shareRouteInfo(context, route) }
        )
    }

    // Dialog Panduan & Aturan Tiket 2 Jam
    if (showGuideDialog) {
        TransitGuideDialog(onDismiss = { showGuideDialog = false })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransitRouteCard(
    route: TransitRoute,
    onOpenDetail: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedStops by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .animateContentSize()
            .testTag("transit_card_${route.code}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Code Badge + Category & Fleet Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Badge Kode Rute
                    Surface(
                        color = Color(route.badgeColorHex),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = route.code,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Fleet / Armada Type Pill
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            if (route.fleetType.contains("Listrik")) {
                                Icon(
                                    imageVector = Icons.Filled.ElectricBolt,
                                    contentDescription = null,
                                    tint = ToskaPrimaryDeep,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                            Text(
                                text = route.fleetType,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Share Button
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Bagikan Info Rute",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Origin -> Destination Title
            Text(
                text = route.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = route.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expandedStops) 4 else 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Key info chips: Jam Operasional, Interval, Tarif
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null,
                        tint = ToskaPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = route.operatingHours,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Payments,
                        contentDescription = null,
                        tint = ToskaPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Rp 5.000 (Umum)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transfer Hubs preview
            if (route.transferHubs.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    route.transferHubs.forEach { hub ->
                        Surface(
                            color = ToskaPrimary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, ToskaPrimary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AltRoute,
                                    contentDescription = null,
                                    tint = ToskaPrimaryDeep,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = hub,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = ToskaPrimaryDeep,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Actions: Toggle Halte Preview / Buka Detail Lengkap
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { expandedStops = !expandedStops },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (expandedStops) "Sembunyikan Halte" else "Lihat ${route.keyStops.size} Halte Utama",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ToskaPrimaryDeep
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expandedStops) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Button(
                    onClick = onOpenDetail,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Detail Rute", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Quick Halte Stops Expansion
            AnimatedVisibility(visible = expandedStops) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Urutan Titik Halte / Jalur Lintasan:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    route.keyStops.forEachIndexed { index, stop ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == 0 || index == route.keyStops.lastIndex) {
                                            Color(route.badgeColorHex)
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (index == 0 || index == route.keyStops.lastIndex) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stop,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (index == 0 || index == route.keyStops.lastIndex) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransitDetailModalSheet(
    route: TransitRoute,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        modifier = modifier.testTag("transit_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(route.badgeColorHex),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = route.code,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = route.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = route.operator,
                            style = MaterialTheme.typography.bodySmall,
                            color = ToskaPrimaryDeep
                        )
                    }
                }

                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Bagikan Rute",
                        tint = ToskaPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Specs Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Spesifikasi & Fasilitas Armada:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = route.fleetDetails,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Interval: ${route.intervalMinutes}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ToskaPrimaryDeep
                        )
                        Text(
                            text = "Jam: ${route.operatingHours}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ToskaPrimaryDeep
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Description
            Text(
                text = route.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Halte List
            Text(
                text = "Rute Lengkap & Titik Halte (${route.keyStops.size} Titik):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                items(route.keyStops.size) { idx ->
                    val stop = route.keyStops[idx]
                    val isFirst = idx == 0
                    val isLast = idx == route.keyStops.lastIndex

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isFirst || isLast -> Color(route.badgeColorHex)
                                        else -> ToskaPrimary.copy(alpha = 0.2f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isFirst || isLast) {
                                Icon(
                                    imageVector = Icons.Filled.Place,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = ToskaPrimaryDeep
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stop,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isFirst || isLast) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isFirst) {
                                Text(
                                    text = "Titik Keberangkatan Awal",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = ToskaPrimaryDeep
                                )
                            } else if (isLast) {
                                Text(
                                    text = "Titik Pemberhentian Terakhir",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = AlertCrimson
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tutup Button
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tutup", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TransitGuideDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = null,
                    tint = ToskaPrimaryDeep,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Panduan & Tarif Terintegrasi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ToskaPrimaryDeep
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Point 1
                Row(verticalAlignment = Alignment.Top) {
                    Text("1.", fontWeight = FontWeight.Bold, color = ToskaPrimaryDeep)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tarif Terintegrasi 2 Jam: Penumpang hanya membayar Rp 5.000 satu kali saat naik pertama. Pindah antar Feeder WiraWiri dan Suroboyo Bus dalam rentang waktu 2 jam tidak dikenakan biaya tambahan (GRATIS transit).",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Point 2
                Row(verticalAlignment = Alignment.Top) {
                    Text("2.", fontWeight = FontWeight.Bold, color = ToskaPrimaryDeep)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tarif Pelajar & Lansia: Pelajar/Mahasiswa aktif Rp 2.500. Warga Lansia (usia 60 tahun ke atas) dan Veteran Republik Indonesia GRATIS (cukup tunjukkan KTP/kartu identitas ke helper/petugas).",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Point 3
                Row(verticalAlignment = Alignment.Top) {
                    Text("3.", fontWeight = FontWeight.Bold, color = ToskaPrimaryDeep)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Metode Pembayaran (100% Cashless): Mendukung QRIS (Aplikasi GOBIS Surabaya Bus, GoPay, OVO, Dana, ShopeePay, BCA Mobile, dll.) dan Kartu Uang Elektronik (Flazz, e-Money Mandiri, TapCash BNI, BRIZZI).",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Point 4
                Row(verticalAlignment = Alignment.Top) {
                    Text("4.", fontWeight = FontWeight.Bold, color = ToskaPrimaryDeep)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pantau Realtime: Waktu tunggu dan pergerakan armada dapat dipantau langsung melalui aplikasi resmi GOBIS Suroboyo Bus milik Dishub Kota Surabaya.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimary)
            ) {
                Text("Mengerti", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

private fun shareRouteInfo(context: Context, route: TransitRoute) {
    val stopsListText = route.keyStops.take(6).joinToString(" → ")
    val shareText = """
        🚌 Rute Transportasi Surabaya:
        Kode: ${route.code} (${route.name})
        Armada: ${route.fleetType}
        Kategori: ${route.category.label}
        Jam Operasional: ${route.operatingHours}
        Tarif: ${route.fare}
        Integrasi: Bebas pindah armada dalam 2 jam!
        Lintasan Halte Utama: $stopsListText...
        
        Dibagikan lewat aplikasi Arek Surabaya • Bangga Kota Pahlawan
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Rute Transportasi Surabaya ${route.code}")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Bagikan Rute ${route.code}"))
}
