package com.example.ui.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.pocketbase.PocketBaseClient
import com.example.ui.theme.OutlineVariantLight
import java.net.URLEncoder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OAuthCallbackEvent(
    val code: String?,
    val state: String?,
    val error: String?,
)

object OAuthRedirectBus {
    private val _pendingCallback = MutableStateFlow<OAuthCallbackEvent?>(null)
    val pendingCallback: StateFlow<OAuthCallbackEvent?> = _pendingCallback.asStateFlow()

    fun handleIncomingIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "areksurabaya" && data.host == "oauth2redirect") {
            _pendingCallback.value = OAuthCallbackEvent(
                code = data.getQueryParameter("code"),
                state = data.getQueryParameter("state"),
                error = data.getQueryParameter("error_description") ?: data.getQueryParameter("error"),
            )
        }
    }

    fun consume() {
        _pendingCallback.value = null
    }
}

suspend fun startPocketBaseSocialOAuth2(
    context: Context,
    pbClient: PocketBaseClient,
    providerKey: String,
    providerLabel: String,
    onInfo: (String) -> Unit,
    onError: (String) -> Unit,
) {
    val providers = try {
        pbClient.fetchOAuth2Providers()
    } catch (e: Exception) {
        onError("Gagal memeriksa penyedia login sosial di PocketBase: ${e.localizedMessage ?: "Cek koneksi"}")
        return
    }

    val matched = providers.firstOrNull { p ->
        p.name.equals(providerKey, ignoreCase = true) ||
            (providerKey == "instagram" && p.name.contains("instagram", ignoreCase = true))
    }

    if (matched == null || matched.authUrl.isBlank()) {
        onError(
            "Login $providerLabel belum diaktifkan di PocketBase Admin. Buka Settings → Auth providers → aktifkan $providerLabel (Redirect URL: ${PocketBaseClient.OAUTH2_REDIRECT_URI}).",
        )
        return
    }

    val redirectUri = PocketBaseClient.OAUTH2_REDIRECT_URI
    pbClient.savePendingOAuth2Flow(
        providerName = matched.name,
        codeVerifier = matched.codeVerifier,
        state = matched.state,
        redirectUrl = redirectUri,
    )

    val fullAuthUrl = buildString {
        append(matched.authUrl)
        if (!matched.authUrl.endsWith("=") && !matched.authUrl.contains("redirect_uri=")) {
            append(if (matched.authUrl.contains("?")) "&redirect_uri=" else "?redirect_uri=")
        }
        append(URLEncoder.encode(redirectUri, "UTF-8"))
    }

    try {
        onInfo("Membuka halaman login $providerLabel…")
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fullAuthUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(browserIntent)
    } catch (_: Exception) {
        onError("Tidak dapat membuka browser untuk login $providerLabel.")
    }
}

/**
 * Compact Social Login Section with small brand icons (Google, Facebook, Instagram)
 * while maintaining >= 48dp accessible touch targets.
 */
@Composable
fun CompactSocialLoginSection(
    enabled: Boolean,
    onSelectProvider: (providerKey: String, providerLabel: String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = OutlineVariantLight,
            )
            Text(
                text = stringResource(R.string.social_auth_divider),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = OutlineVariantLight,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompactSocialCircleButton(
                contentDescription = stringResource(R.string.social_login_google_cd),
                enabled = enabled,
                testTag = "social_login_google_btn",
                onClick = { onSelectProvider("google", "Google") },
            ) {
                GoogleSmallBrandIcon()
            }

            CompactSocialCircleButton(
                contentDescription = stringResource(R.string.social_login_facebook_cd),
                enabled = enabled,
                testTag = "social_login_facebook_btn",
                onClick = { onSelectProvider("facebook", "Facebook") },
            ) {
                FacebookSmallBrandIcon()
            }

            CompactSocialCircleButton(
                contentDescription = stringResource(R.string.social_login_instagram_cd),
                enabled = enabled,
                testTag = "social_login_instagram_btn",
                onClick = { onSelectProvider("instagram", "Instagram") },
            ) {
                InstagramSmallBrandIcon()
            }
        }
    }
}

@Composable
private fun CompactSocialCircleButton(
    contentDescription: String,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit,
    iconContent: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .clickable(
                enabled = enabled,
                onClickLabel = contentDescription,
                onClick = onClick,
            )
            .padding(4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFF9FCFA),
            border = BorderStroke(1.dp, OutlineVariantLight),
            shadowElevation = 1.dp,
            modifier = Modifier.size(38.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                iconContent()
            }
        }
    }
}

@Composable
private fun GoogleSmallBrandIcon() {
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = size.minDimension * 0.20f
        val inset = stroke / 2f
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(inset, inset)

        // Red top arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 215f,
            sweepAngle = 105f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt),
        )
        // Yellow left arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 140f,
            sweepAngle = 78f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt),
        )
        // Green bottom arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 42f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt),
        )
        // Blue right arc + crossbar
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -10f,
            sweepAngle = 56f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt),
        )
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(size.width * 0.50f, size.height * 0.50f),
            end = Offset(size.width - inset * 0.4f, size.height * 0.50f),
            strokeWidth = stroke * 0.92f,
            cap = StrokeCap.Butt,
        )
    }
}

@Composable
private fun FacebookSmallBrandIcon() {
    Canvas(modifier = Modifier.size(18.dp)) {
        drawCircle(
            color = Color(0xFF1877F2),
            radius = size.minDimension / 2f,
        )
        val w = size.width
        val h = size.height
        val fPath = Path().apply {
            moveTo(w * 0.57f, h * 0.88f)
            lineTo(w * 0.57f, h * 0.54f)
            lineTo(w * 0.69f, h * 0.54f)
            lineTo(w * 0.71f, h * 0.41f)
            lineTo(w * 0.57f, h * 0.41f)
            lineTo(w * 0.57f, h * 0.33f)
            cubicTo(w * 0.57f, h * 0.28f, w * 0.60f, h * 0.25f, w * 0.66f, h * 0.25f)
            lineTo(w * 0.72f, h * 0.25f)
            lineTo(w * 0.72f, h * 0.14f)
            lineTo(w * 0.62f, h * 0.14f)
            cubicTo(w * 0.49f, h * 0.14f, w * 0.43f, h * 0.21f, w * 0.43f, h * 0.33f)
            lineTo(w * 0.43f, h * 0.41f)
            lineTo(w * 0.33f, h * 0.41f)
            lineTo(w * 0.33f, h * 0.54f)
            lineTo(w * 0.43f, h * 0.54f)
            lineTo(w * 0.43f, h * 0.88f)
            close()
        }
        drawPath(path = fPath, color = Color.White)
    }
}

@Composable
private fun InstagramSmallBrandIcon() {
    Canvas(modifier = Modifier.size(18.dp)) {
        val igBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFEDA75),
                Color(0xFFFA7E1E),
                Color(0xFFD62976),
                Color(0xFF962FBF),
            ),
            start = Offset(0f, size.height),
            end = Offset(size.width, 0f),
        )
        val stroke = size.minDimension * 0.11f
        val inset = stroke / 2f
        drawRoundRect(
            brush = igBrush,
            topLeft = Offset(inset, inset),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(size.minDimension * 0.28f, size.minDimension * 0.28f),
            style = Stroke(width = stroke),
        )
        drawCircle(
            brush = igBrush,
            radius = size.minDimension * 0.22f,
            center = center,
            style = Stroke(width = stroke),
        )
        drawCircle(
            color = Color(0xFFD62976),
            radius = size.minDimension * 0.065f,
            center = Offset(size.width * 0.74f, size.height * 0.26f),
        )
    }
}
