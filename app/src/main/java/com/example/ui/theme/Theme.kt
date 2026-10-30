package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IosBlue,
    onPrimary = Color.White,
    primaryContainer = IosBlue.copy(alpha = 0.2f),
    onPrimaryContainer = IosCyan,
    secondary = IosGreen,
    onSecondary = Color.Black,
    secondaryContainer = IosGreen.copy(alpha = 0.2f),
    onSecondaryContainer = IosGreen,
    tertiary = IosPurple,
    background = IosDeepBlack,
    onBackground = Color.White,
    surface = IosSurface,
    onSurface = Color.White,
    surfaceVariant = IosSurfaceElevated,
    onSurfaceVariant = IosGray1,
    outline = IosGlassBorder,
    outlineVariant = IosDivider
)

private val LightColorScheme = lightColorScheme(
    primary = IosBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5F0FF),
    onPrimaryContainer = IosBlue,
    secondary = IosGreen,
    onSecondary = Color.White,
    background = Color(0xFFF2F2F7),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF8E8E93),
    outline = Color(0xFFC6C6C8),
    outlineVariant = Color(0xFFE5E5EA)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to iOS dark space mode for god-level tech look
    dynamicColor: Boolean = false, // Keep consistent iOS palette
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
