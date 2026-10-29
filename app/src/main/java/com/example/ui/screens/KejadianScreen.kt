package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BookmarkItem
import com.example.data.model.IncidentReport
import com.example.data.model.UiState
import com.example.data.util.formatRelativeTimestamp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.EmptyFeedPlaceholder
import com.example.ui.components.SurabayaRemoteOrFallbackImage
import com.example.ui.components.openEmergencyDialer112
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.SoftEmergencyLightGradient
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimaryDeep

private val incidentCategories = listOf(
    "ALL" to "Semua Info",
    "LALU_LINTAS" to "Lalu Lintas",
    "CUACA_BANJIR" to "Cuaca & Banjir",
    "DARURAT" to "Darurat",
    "FASILITAS" to "Fasilitas Kota",
    "INFO_WARGA" to "Info Warga",
)

fun formatIncidentCategoryLabel(key: String): String = when (key) {
    "LALU_LINTAS" -> "Lalu Lintas"
    "CUACA_BANJIR" -> "Cuaca & Banjir"
    "DARURAT" -> "Darurat"
    "FASILITAS" -> "Fasilitas"
    else -> "Info Warga"
}

@Composable
fun KejadianScreen(
    incidentsState: UiState<List<IncidentReport>>,
    bookmarks: List<BookmarkItem>,
    currentUserId: String,
    onSelectIncident: (IncidentReport) -> Unit,
    onVerifyIncident: (IncidentReport) -> Unit,
    onToggleBookmark: (IncidentReport) -> Unit,
    onOpenCreateSheet: () -> Unit,
) {
    val context = LocalContext.current
    var selectedCategory by rememberSaveable { mutableStateOf("ALL") }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(top = 10.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("kejadian_feed_list"),
        ) {
            // Compact Command Center 112 Surabaya Banner (Bright Soft Rose-White Gradient)
            item(key = "emergency_112_bar") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SoftEmergencyLightGradient)
                        .border(1.dp, Color(0xFFFFD6D6), RoundedCornerShape(20.dp)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AlertCrimson),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Emergency,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp),
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.emergency_112_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                                Text(
                                    text = stringResource(R.string.emergency_112_subtitle),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f),
                                )
                            }
                        }
                        FilledTonalButton(
                            onClick = { openEmergencyDialer112(context) },
                            shape = CircleShape,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AlertCrimson,
                                contentColor = Color.White,
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("call_112_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhoneInTalk,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "112",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            // Category Filter Chips
            item(key = "incident_categories") {
                CategoryFilterRow(
                    categories = incidentCategories,
                    selectedKey = selectedCategory,
                    onSelect = { selectedCategory = it },
                    tagPrefix = "incident",
                )
            }

            when (incidentsState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        EmptyFeedPlaceholder(
                            icon = Icons.Outlined.Newspaper,
                            title = "Belum dapat memuat kabar kota",
                            subtitle = incidentsState.message,
                        )
                    }
                }
                is UiState.Success -> {
                    val filtered = incidentsState.data.filter {
                        selectedCategory == "ALL" || it.category == selectedCategory
                    }
                    if (filtered.isEmpty()) {
                        item {
                            EmptyFeedPlaceholder(
                                icon = Icons.Outlined.Newspaper,
                                title = "Belum ada laporan di kategori ini",
                                subtitle = "Ketuk tombol Lapor Kejadian di kanan bawah untuk berbagi info warga.",
                            )
                        }
                    } else {
                        items(filtered, key = { it.id }) { report ->
                            val isSaved = bookmarks.any { it.targetId == report.id && it.targetType == "KEJADIAN" }
                            val isVerifiedByMe = currentUserId in report.verifiedByUids
                            IncidentCompactCard(
                                report = report,
                                isSaved = isSaved,
                                isVerifiedByMe = isVerifiedByMe,
                                onClick = { onSelectIncident(report) },
                                onVerify = { onVerifyIncident(report) },
                                onBookmark = { onToggleBookmark(report) },
                            )
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onOpenCreateSheet,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            icon = {
                Icon(
                    imageVector = Icons.Filled.AddAlert,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.btn_report_incident),
                    style = MaterialTheme.typography.labelLarge,
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_report_incident"),
        )
    }
}

@Composable
private fun IncidentCompactCard(
    report: IncidentReport,
    isSaved: Boolean,
    isVerifiedByMe: Boolean,
    onClick: () -> Unit,
    onVerify: () -> Unit,
    onBookmark: () -> Unit,
) {
    val badgeBg = when (report.severity) {
        "DARURAT" -> AlertCrimson
        "PENTING" -> Color(0xFFD97706)
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.dp, OutlineVariantLight),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("incident_card_${report.id}"),
    ) {
        Column {
            // Compact Visual Banner Header (Airy & Bright)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(136.dp),
            ) {
                SurabayaRemoteOrFallbackImage(
                    imageUrl = report.photoUrl,
                    fallbackImageKey = report.imageKey,
                    contentDescription = report.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x14000000), Color.Transparent, Color(0x8009261E)),
                            ),
                        ),
                )

                // Top badges row (Bright frosted white pills)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = CircleShape, color = badgeBg) {
                            Text(
                                text = report.severity,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.92f),
                        ) {
                            Text(
                                text = formatIncidentCategoryLabel(report.category),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ToskaOnPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.90f),
                    ) {
                        Text(
                            text = formatRelativeTimestamp(report.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }

                // Bottom district tag inside image
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFA7F3D0),
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${report.district} • ${report.address}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Dense & proportional body
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = report.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Verify pill button with soft light-green gradient
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .then(
                                    if (isVerifiedByMe) {
                                        Modifier.background(MintSoftGradient, CircleShape)
                                    } else {
                                        Modifier.background(Color(0xFFF1FAF5), CircleShape)
                                    },
                                )
                                .border(1.dp, OutlineVariantLight, CircleShape)
                                .clickable(onClick = onVerify)
                                .testTag("verify_incident_${report.id}"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = null,
                                    tint = ToskaPrimaryDeep,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = "${report.verifiedCount} Warga Validasi",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ToskaOnPrimaryContainer,
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ChatBubbleOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "${report.commentCount}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    IconButton(
                        onClick = onBookmark,
                        modifier = Modifier
                            .size(36.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("bookmark_incident_${report.id}"),
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = stringResource(R.string.btn_bookmarks),
                            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
            }
        }
    }
}
