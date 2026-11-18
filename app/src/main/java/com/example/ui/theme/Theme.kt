package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val NexusDarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = OnCyan,
    primaryContainer = CyanContainer,
    onPrimaryContainer = TextPrimary,
    secondary = VioletAccent,
    onSecondary = OnViolet,
    secondaryContainer = VioletContainer,
    onSecondaryContainer = TextPrimary,
    tertiary = EmeraldSuccess,
    onTertiary = OnCyan,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = SlateSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = ObsidianBg,
    surfaceContainerLow = SlateSurface,
    surfaceContainer = CardSurface,
    surfaceContainerHigh = ElevatedSurface,
    surfaceContainerHighest = BorderSubtle,
    outline = BorderSubtle,
    outlineVariant = ColorTokens.OutlineVariant,
    error = CoralError,
    onError = ObsidianBg,
    errorContainer = ErrorContainerDark,
    onErrorContainer = TextPrimary
)

private object ColorTokens {
    val OutlineVariant = androidx.compose.ui.graphics.Color(0xFF1D2638)
}

val NexusShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun NexusAiTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(LocalContext.current)
    } else {
        NexusDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = NexusShapes,
        content = content
    )
}
