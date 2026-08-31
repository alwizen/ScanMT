package com.example.scanmt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color.White,
    primaryContainer = DarkCard,
    onPrimaryContainer = TextPrimary,
    secondary = BluePrimary,
    onSecondary = Color.White,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun ScanMTTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}