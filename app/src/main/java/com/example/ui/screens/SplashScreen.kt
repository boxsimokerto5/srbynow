package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.StarGold
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep
import com.example.ui.theme.ToskaPrimaryLight
import kotlinx.coroutines.delay

/**
 * Splash Screen modern & berkarakter untuk "Arek Surabaya".
 * Menampilkan ikon maskot khas Surabaya dengan animasi entrance halus,
 * halo pernapasan bercahaya, badge identitas Kota Pahlawan, serta indikator loading mulus.
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    splashDurationMillis: Long = 2200L,
    onSplashFinished: () -> Unit,
) {
    var animationStarted by remember { mutableStateOf(false) }
    var currentHintIndex by remember { mutableIntStateOf(0) }

    val loadingHints = remember {
        listOf(
            "Menghubungkan arek-arek Suroboyo…",
            "Memuat kabar & event Kota Pahlawan…",
            "Satu Nyali Wani!",
        )
    }

    // Trigger animations immediately on enter
    LaunchedEffect(Unit) {
        animationStarted = true
        // Cycle through dynamic loading hints
        delay(700L)
        currentHintIndex = 1
        delay(700L)
        currentHintIndex = 2
        delay(splashDurationMillis - 1400L)
        onSplashFinished()
    }

    // Entrance scale animation with spring bounce
    val logoScale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 250f,
        ),
        label = "logo_scale",
    )

    // Entrance alpha animation
    val contentAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(
            durationMillis = 650,
            easing = FastOutSlowInEasing,
        ),
        label = "content_alpha",
    )

    // Subtle pulsing ambient halo behind the logo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_pulse",
    )

    val shimmerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_rotation",
    )

    // Vibrant deep Surabaya gradient background
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("splash_screen_container")
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF042138), // Deep Midnight Teal
                        Color(0xFF063F5E), // Surabaya Navy-Teal
                        Color(0xFF0A556B), // Deep Green-Teal
                        Color(0xFF064E3B), // Signature Deep Toska
                    ),
                ),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSplashFinished,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Optional subtle city background silhouette overlay
        Image(
            painter = painterResource(id = R.drawable.img_login_sky_sby_1793238473815),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(3.dp),
            alpha = 0.12f,
        )

        // Main Center Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Center Branding Section (Logo + Names)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Logo & Mascot Container with Glowing Aura
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(190.dp)
                        .scale(logoScale),
                ) {
                    // Outer Ambient Glowing Ring
                    Box(
                        modifier = Modifier
                            .size(175.dp * pulseGlow)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        ToskaPrimaryLight.copy(alpha = 0.35f),
                                        ToskaPrimary.copy(alpha = 0.18f),
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                    )

                    // Secondary Accent Halo Ring (Subtle Gold/Teal)
                    Box(
                        modifier = Modifier
                            .size(148.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        ToskaPrimaryLight,
                                        StarGold,
                                        ToskaPrimary,
                                        Color(0xFF6EE7B7),
                                        ToskaPrimaryLight,
                                    ),
                                ),
                                shape = CircleShape,
                            ),
                    )

                    // Main Elevated Icon Card
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = Color.White,
                        shadowElevation = 14.dp,
                        tonalElevation = 6.dp,
                        modifier = Modifier
                            .size(126.dp)
                            .testTag("splash_screen_logo_card"),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo_mascot),
                                contentDescription = stringResource(R.string.app_name),
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .testTag("splash_screen_logo"),
                            )
                        }
                    }
                }

            Spacer(modifier = Modifier.height(28.dp))

            // App Title & Badges with Animated Visibility
            AnimatedVisibility(
                visible = animationStarted,
                enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = 0.75f),
                ),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // City Identity Pill Badge
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.14f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.28f),
                        ),
                        modifier = Modifier.testTag("splash_city_badge"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationCity,
                                contentDescription = null,
                                tint = StarGold,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "KOTA PAHLAWAN • SURABAYA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = Color(0xFFF0FDF4),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Main App Name
                    Text(
                        text = stringResource(R.string.app_name),
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        color = Color.White,
                        letterSpacing = (-0.5).sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("splash_app_title"),
                    )

                    // Tagline
                    Text(
                        text = "Media Sosial & Informasi Warga Suroboyo",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFD1FAE5),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .testTag("splash_app_tagline"),
                    )
                }
            }
        }

            // Bottom Progress & Status Section
            AnimatedVisibility(
                visible = animationStarted,
                enter = fadeIn(tween(700, delayMillis = 400)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Sleek Modern Glowing Progress Bar
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .testTag("splash_progress_indicator"),
                        color = ToskaPrimaryLight,
                        trackColor = Color.White.copy(alpha = 0.18f),
                        strokeCap = StrokeCap.Round,
                    )

                    // Dynamic Cycling Status Hint
                    Text(
                        text = loadingHints[currentHintIndex],
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("splash_hint_text"),
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Version & Motto Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Versi 1.0",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.55f),
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.55f),
                        )
                        Text(
                            text = "Satu Nyali Wani!",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StarGold,
                        )
                    }
                }
            }
        }
    }
}
