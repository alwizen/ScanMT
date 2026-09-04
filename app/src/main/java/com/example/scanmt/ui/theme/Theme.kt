package com.example.scanmt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary = NeutralAccent,
    onPrimary = Color.White,
    primaryContainer = WhiteCard,
    onPrimaryContainer = TextPrimary,
    secondary = NeutralAccent,
    onSecondary = Color.White,
    background = WhitePrimary,
    onBackground = TextPrimary,
    surface = WhiteSurface,
    onSurface = TextPrimary,
    surfaceVariant = WhiteCard,
    onSurfaceVariant = TextSecondary,
    outline = WhiteBorder,
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