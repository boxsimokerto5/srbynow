package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Bright & Airy Surabaya Light Palette with Fresh Light-Green Gradients
val ToskaPrimary = Color(0xFF10B981)
val ToskaPrimaryDeep = Color(0xFF059669)
val ToskaPrimaryLight = Color(0xFF34D399)
val ToskaOnPrimary = Color(0xFFFFFFFF)
val ToskaPrimaryContainer = Color(0xFFDDF9EC)
val ToskaOnPrimaryContainer = Color(0xFF064E3B)

val ToskaSecondary = Color(0xFF14B8A6)
val ToskaOnSecondary = Color(0xFFFFFFFF)
val ToskaSecondaryContainer = Color(0xFFE0FBF6)
val ToskaOnSecondaryContainer = Color(0xFF0F524B)

val AmberTertiary = Color(0xFFD97706)
val AmberOnTertiary = Color(0xFFFFFFFF)
val AmberTertiaryContainer = Color(0xFFFFF3DC)
val AmberOnTertiaryContainer = Color(0xFF78350F)

val AlertCrimson = Color(0xFFDC2626)
val AlertContainerLight = Color(0xFFFFEFEF)
val AlertOnContainerLight = Color(0xFF7F1D1D)

// Crisp Bright Surfaces
val SurfaceLight = Color(0xFFF8FCFA)
val CardLight = Color(0xFFFFFFFF)
val SurfaceContainerLight = Color(0xFFEEF9F3)
val OnSurfaceLight = Color(0xFF12231D)
val OnSurfaceVariantLight = Color(0xFF476258)
val OutlineLight = Color(0xFF8AA89C)
val OutlineVariantLight = Color(0xFFD7ECE2)

// Reusable Light-Green Gradient Brushes
val HeaderMintGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFEDFCF5), Color(0xFFFFFFFF)),
)

val MintSoftGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFECFCF4), Color(0xFFCCF5E2)),
)

val MintCardSubtleGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF1FCF6)),
)

val FreshGreenGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF34D399), Color(0xFF10B981)),
)

val SoftChatBubbleGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFE3FBEF), Color(0xFFC5F4DF)),
)

val SoftEmergencyLightGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFFF6F6), Color(0xFFFFE9E9)),
)

// Semantic Status Accents (Light & Fresh)
val OpenGreenBg = Color(0xFFE3FBEF)
val OpenGreenText = Color(0xFF065F46)
val ClosedGrayBg = Color(0xFFF1F5F4)
val ClosedGrayText = Color(0xFF566B63)
val PendingAmberBg = Color(0xFFFFF6E5)
val PendingAmberText = Color(0xFF9A4D06)
val StarGold = Color(0xFFF59E0B)
