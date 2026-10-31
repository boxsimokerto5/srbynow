package com.example.ui.sheets

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.audio.RadioPlaybackState
import com.example.data.audio.RadioStationInfo
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.StarGold
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep

@Composable
fun RadioPlayerSheetContent(
    playbackState: RadioPlaybackState,
    volume: Float,
    errorMessage: String?,
    stationInfo: RadioStationInfo,
    onTogglePlay: () -> Unit,
    onSetVolume: (Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("radio_player_sheet_content"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(ToskaPrimary, ToskaPrimaryDeep)
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Radio,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.radio_ss_full_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.radio_ss_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("radio_close_btn"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Tutup Radio",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Big Hero Radio Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                ToskaPrimary.copy(alpha = 0.25f),
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Live Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (playbackState == RadioPlaybackState.PLAYING) {
                                AlertCrimson.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (playbackState) {
                                    RadioPlaybackState.PLAYING -> AlertCrimson
                                    RadioPlaybackState.BUFFERING -> StarGold
                                    else -> Color.Gray
                                }
                            ),
                    )
                    Text(
                        text = when (playbackState) {
                            RadioPlaybackState.PLAYING -> "LIVE ON AIR • 100.0 FM"
                            RadioPlaybackState.BUFFERING -> stringResource(R.string.radio_ss_buffering)
                            RadioPlaybackState.PAUSED -> stringResource(R.string.radio_ss_paused)
                            RadioPlaybackState.ERROR -> "KONEKSI TERPUTUS"
                            RadioPlaybackState.STOPPED -> "SIARAN SIAP DIPUTAR"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (playbackState) {
                            RadioPlaybackState.PLAYING -> AlertCrimson
                            RadioPlaybackState.BUFFERING -> ToskaPrimaryDeep
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Animated Equalizer Visualizer Bars
                RadioVisualizerBars(isPlaying = playbackState == RadioPlaybackState.PLAYING)

                Spacer(modifier = Modifier.height(16.dp))

                // Program Title & Info
                Text(
                    text = stationInfo.currentProgram,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stationInfo.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Play / Pause / Buffering FAB
                Box(contentAlignment = Alignment.Center) {
                    if (playbackState == RadioPlaybackState.BUFFERING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(76.dp),
                            color = ToskaPrimary,
                            strokeWidth = 3.dp,
                        )
                    }

                    Button(
                        onClick = onTogglePlay,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ToskaPrimary,
                            contentColor = Color.White,
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .size(64.dp)
                            .testTag("radio_play_pause_btn"),
                    ) {
                        Icon(
                            imageVector = if (playbackState == RadioPlaybackState.PLAYING) {
                                Icons.Filled.Pause
                            } else {
                                Icons.Filled.PlayArrow
                            },
                            contentDescription = if (playbackState == RadioPlaybackState.PLAYING) {
                                stringResource(R.string.radio_ss_pause)
                            } else {
                                stringResource(R.string.radio_ss_play)
                            },
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (playbackState == RadioPlaybackState.PLAYING) {
                        "Sentuh untuk Jeda"
                    } else {
                        "Sentuh untuk Putar Siaran Langsung"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Volume Controls
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(
                        onClick = { onSetVolume(if (volume > 0.05f) 0f else 0.85f) },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = when {
                                volume <= 0.01f -> Icons.AutoMirrored.Filled.VolumeMute
                                volume < 0.5f -> Icons.AutoMirrored.Filled.VolumeDown
                                else -> Icons.AutoMirrored.Filled.VolumeUp
                            },
                            contentDescription = "Volume",
                            tint = ToskaPrimaryDeep,
                        )
                    }

                    Slider(
                        value = volume,
                        onValueChange = { onSetVolume(it) },
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("radio_volume_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = ToskaPrimary,
                            activeTrackColor = ToskaPrimary,
                            inactiveTrackColor = ToskaPrimary.copy(alpha = 0.2f),
                        ),
                    )

                    Text(
                        text = "${(volume * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(36.dp),
                    )
                }

                // Error notice if error state
                if (!errorMessage.isNullOrBlank() || playbackState == RadioPlaybackState.ERROR) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = AlertCrimson.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = AlertCrimson,
                            )
                            Text(
                                text = errorMessage ?: stringResource(R.string.radio_ss_error),
                                style = MaterialTheme.typography.bodySmall,
                                color = AlertCrimson,
                                modifier = Modifier.weight(1f),
                            )
                            Button(
                                onClick = onTogglePlay,
                                colors = ButtonDefaults.buttonColors(containerColor = AlertCrimson),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp),
                            ) {
                                Text("Coba Lagi", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Direct Redaksi & Hotline Suara Surabaya Actions
        Text(
            text = "Layanan Interaktif Suara Surabaya",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Laporkan kemacetan, kecelakaan, atau aduan fasilitas langsung ke gatekeeper on-air SS 100 FM.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // WhatsApp Redaksi Gatekeeper Button
        Button(
            onClick = {
                openWhatsAppGatekeeper(context, stationInfo.hotlineWhatsApp)
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

        Spacer(modifier = Modifier.height(8.dp))

        // Phone Studio On-Air Hotline Button
        OutlinedButton(
            onClick = {
                openPhoneDialer(context, stationInfo.hotlinePhone)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("radio_phone_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ToskaPrimaryDeep,
            ),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ToskaPrimary),
        ) {
            Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.radio_ss_phone),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Share Stream Button
        OutlinedButton(
            onClick = {
                shareRadioStream(context)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("radio_share_btn"),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.radio_ss_share),
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun RadioVisualizerBars(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "radio_wave")

    val h1 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = if (isPlaying) 36f else 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "h1",
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = if (isPlaying) 48f else 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(310, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "h2",
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = if (isPlaying) 40f else 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(530, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "h3",
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = if (isPlaying) 52f else 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "h4",
    )
    val h5 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = if (isPlaying) 44f else 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(460, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "h5",
    )
    val h6 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = if (isPlaying) 32f else 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "h6",
    )
    val h7 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = if (isPlaying) 26f else 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(490, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "h7",
    )

    val heights = listOf(h1, h2, h3, h4, h5, h6, h7)

    Row(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        heights.forEachIndexed { index, heightValue ->
            val barHeight = if (isPlaying) heightValue.dp else 10.dp
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(6.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                ToskaPrimary,
                                if (index % 2 == 0) StarGold else ToskaPrimaryDeep
                            )
                        )
                    ),
            )
        }
    }
}

private fun openWhatsAppGatekeeper(context: Context, phone: String) {
    val message = Uri.encode("Halo Redaksi Suara Surabaya 100 FM, saya warga Surabaya pengguna aplikasi Arek Surabaya ingin menyampaikan pantauan:")
    val url = "https://wa.me/$phone?text=$message"
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(fallbackIntent)
    }
}

private fun openPhoneDialer(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    } catch (_: Exception) {}
}

private fun shareRadioStream(context: Context) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "Yuk dengarkan siaran Radio Suara Surabaya 100.0 FM live streaming langsung dari aplikasi Arek Surabaya!\nhttps://stream.suarasurabaya.net/live"
        )
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Bagikan Siaran Radio SS 100 FM"))
}
