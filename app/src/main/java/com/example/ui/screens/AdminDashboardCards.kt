package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ChatRoom
import com.example.data.model.CitizenPoll
import com.example.data.model.IncidentReport
import com.example.data.model.NearbyProfile
import com.example.data.model.SurabayaEvent
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.PendingAmberBg
import com.example.ui.theme.PendingAmberText
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep

@Composable
fun AdminPendingEventApprovalCard(
    event: SurabayaEvent,
    onOpenDetail: () -> Unit,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
) {
    var adminNote by remember(event.id) { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, PendingAmberText.copy(alpha = 0.28f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_pending_event_card_${event.id}"),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = PendingAmberBg,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = PendingAmberText,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "Usulan Event • Kec. ${event.district}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PendingAmberText,
                        )
                    }
                }
                Text(
                    text = "Oleh: ${event.proposerName.ifBlank { "Warga" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable { onOpenDetail() },
            )

            Text(
                text = "${event.eventDateText} • ${event.timeText} • ${event.venueName} (${event.priceLabel})",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = ToskaPrimaryDeep,
            )

            if (event.description.isNotBlank()) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            OutlinedTextField(
                value = adminNote,
                onValueChange = { adminNote = it },
                placeholder = { Text("Catatan Admin opsional (misal: Terverifikasi & layak tayang)", style = MaterialTheme.typography.bodySmall) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_event_note_${event.id}"),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        onApprove(adminNote.trim().ifBlank { "Disetujui Admin Resmi Surabaya." })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .testTag("admin_approve_event_${event.id}"),
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Setujui", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        onReject(adminNote.trim().ifBlank { "Belum memenuhi kriteria publikasi agenda kota." })
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCrimson),
                    border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(0.95f)
                        .height(48.dp)
                        .testTag("admin_reject_event_${event.id}"),
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Tolak", fontWeight = FontWeight.SemiBold)
                }

                FilledTonalButton(
                    onClick = onOpenDetail,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.height(48.dp),
                ) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = "Lihat Detail Event", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun AdminPendingPollApprovalCard(
    poll: CitizenPoll,
    onOpenDetail: () -> Unit,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
) {
    var adminNote by remember(poll.id) { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_pending_poll_card_${poll.id}"),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = CircleShape, color = OpenGreenBg) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.HowToVote,
                            contentDescription = null,
                            tint = OpenGreenText,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "Usulan Polling • Kec. ${poll.district}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OpenGreenText,
                        )
                    }
                }
                Text(
                    text = "Oleh: ${poll.proposerName.ifBlank { "Warga" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = poll.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onOpenDetail() },
            )

            if (poll.description.isNotBlank()) {
                Text(
                    text = poll.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (poll.options.isNotEmpty()) {
                Text(
                    text = "Opsi Suara: " + poll.options.joinToString(" • ") { it.text },
                    style = MaterialTheme.typography.labelSmall,
                    color = ToskaPrimaryDeep,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            OutlinedTextField(
                value = adminNote,
                onValueChange = { adminNote = it },
                placeholder = { Text("Catatan Admin opsional…", style = MaterialTheme.typography.bodySmall) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        onApprove(adminNote.trim().ifBlank { "Disetujui untuk voting warga Surabaya." })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ToskaPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .testTag("admin_approve_poll_${poll.id}"),
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Tayangkan", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        onReject(adminNote.trim().ifBlank { "Topik polling belum memenuhi pedoman komunitas." })
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCrimson),
                    border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(0.95f)
                        .height(48.dp)
                        .testTag("admin_reject_poll_${poll.id}"),
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Tolak", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun AdminFlaggedContentCard(
    item: AdminFlaggedItem,
    currentAdminUid: String,
    onOpenItem: () -> Unit,
    onRestoreSafe: () -> Unit,
    onClearLinks: () -> Unit,
    onDeletePermanently: () -> Unit,
    onBanAuthor: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (item.isQuarantined) AlertCrimson.copy(alpha = 0.45f) else PendingAmberText.copy(alpha = 0.35f),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_flagged_card_${item.collection}_${item.id}"),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (item.isQuarantined) AlertCrimson.copy(alpha = 0.14f) else PendingAmberBg,
                    ) {
                        Text(
                            text = if (item.isQuarantined) {
                                "KARANTINA (${item.contentReportCount}/3)"
                            } else if (item.contentReportCount > 0) {
                                "AUTO-BLUR (${item.contentReportCount}/3)"
                            } else {
                                "LINK DILAPORKAN (${item.linkReportCount}/3)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (item.isQuarantined) AlertCrimson else PendingAmberText,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        )
                    }
                    Text(
                        text = "${item.sourceLabel} • Kec. ${item.district}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ToskaPrimaryDeep,
                    )
                }
            }

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onOpenItem() },
            )

            if (item.snippet.isNotBlank()) {
                Text(
                    text = item.snippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = "Pengirim: ${item.authorName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(color = OutlineVariantLight.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = onRestoreSafe,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = OpenGreenBg,
                        contentColor = OpenGreenText,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("admin_restore_${item.id}"),
                ) {
                    Icon(Icons.Filled.Restore, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Pulihkan Aman", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDeletePermanently,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCrimson),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("admin_delete_${item.id}"),
                ) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Hapus Kiriman", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (item.hasExternalLink || item.linkReportCount > 0) {
                    OutlinedButton(
                        onClick = onClearLinks,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("admin_clear_links_${item.id}"),
                    ) {
                        Icon(Icons.Filled.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Bersihkan Link", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (item.authorId.isNotBlank() && item.authorId != currentAdminUid) {
                    OutlinedButton(
                        onClick = onBanAuthor,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCrimson),
                        border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("admin_ban_author_${item.id}"),
                    ) {
                        Icon(Icons.Filled.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Bekukan Pelanggar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminIncidentManagementCard(
    incident: IncidentReport,
    currentAdminUid: String,
    onOpenDetail: () -> Unit,
    onToggleVerify: () -> Unit,
    onDeleteIncident: () -> Unit,
) {
    val isVerifiedByAdmin = currentAdminUid in incident.verifiedByUids
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineVariantLight),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_incident_card_${incident.id}"),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${incident.severity} • ${incident.category} • Kec. ${incident.district}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (incident.severity == "DARURAT" || incident.severity == "TINGGI") AlertCrimson else ToskaPrimaryDeep,
                )
                Text(
                    text = "${incident.verifiedCount} Validasi Warga",
                    style = MaterialTheme.typography.labelSmall,
                    color = OpenGreenText,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Text(
                text = incident.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onOpenDetail() },
            )
            Text(
                text = "${incident.address} • Pelapor: ${incident.authorName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = onToggleVerify,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isVerifiedByAdmin) OpenGreenBg else ToskaPrimary.copy(alpha = 0.14f),
                        contentColor = if (isVerifiedByAdmin) OpenGreenText else ToskaPrimaryDeep,
                    ),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .testTag("admin_verify_incident_${incident.id}"),
                ) {
                    Icon(
                        imageVector = if (isVerifiedByAdmin) Icons.Filled.CheckCircle else Icons.Filled.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = if (isVerifiedByAdmin) "Tervalidasi Admin" else "Validasi Resmi",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }

                OutlinedButton(
                    onClick = onDeleteIncident,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCrimson),
                    border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(48.dp)
                        .testTag("admin_delete_incident_${incident.id}"),
                ) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Hapus", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun AdminBannedCitizenRow(
    profile: NearbyProfile,
    onUnban: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFFF8F6),
        border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_banned_user_${profile.userId.ifBlank { profile.id }}"),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.displayName.ifBlank { "Warga Dibekukan" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ToskaOnPrimaryContainer,
                )
                Text(
                    text = "Kec. ${profile.district} • Status: Akun Dibekukan",
                    style = MaterialTheme.typography.labelSmall,
                    color = AlertCrimson,
                )
            }
            OutlinedButton(
                onClick = onUnban,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("admin_unban_btn_${profile.userId.ifBlank { profile.id }}"),
            ) {
                Icon(Icons.Filled.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text("Pulihkan", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminPublicGroupManageRow(
    room: ChatRoom,
    onOpenRoom: () -> Unit,
    onDeleteRoom: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineVariantLight),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenRoom() },
            ) {
                Text(
                    text = room.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = room.subtitle.ifBlank { "Ruang Publik SBY" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OutlinedButton(
                onClick = onDeleteRoom,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCrimson),
                border = BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.4f)),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("admin_delete_group_${room.id}"),
            ) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Hapus Grup", modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Hapus", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
