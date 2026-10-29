package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = ToskaPrimary,
    onPrimary = ToskaOnPrimary,
    primaryContainer = ToskaPrimaryContainer,
    onPrimaryContainer = ToskaOnPrimaryContainer,
    secondary = ToskaSecondary,
    onSecondary = ToskaOnSecondary,
    secondaryContainer = ToskaSecondaryContainer,
    onSecondaryContainer = ToskaOnSecondaryContainer,
    tertiary = AmberTertiary,
    onTertiary = AmberOnTertiary,
    tertiaryContainer = AmberTertiaryContainer,
    onTertiaryContainer = AmberOnTertiaryContainer,
    error = AlertCrimson,
    errorContainer = AlertContainerLight,
    onErrorContainer = AlertOnContainerLight,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = CardLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceContainerLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainerLowest = CardLight,
    surfaceContainerLow = SurfaceLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = CardLight,
    surfaceContainerHighest = SurfaceContainerLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
)

val SurabayaShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun SurabayaHariIniTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Always use bright LightColorScheme across all screens & sheets as requested
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = SurabayaShapes,
        content = content,
    )
}
