package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF002029),
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = Color(0xFFBCE9FF),
    secondary = RoseLove,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF5B112B),
    onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary = WarmGold,
    background = DarkSpaceBg,
    onBackground = TextPrimary,
    surface = DarkSpaceCard,
    onSurface = TextPrimary,
    surfaceVariant = DarkSpaceCardElevated,
    onSurfaceVariant = TextSecondary,
    outline = GlowBorder,
    error = AlertRed
)

private val LightColorScheme = darkColorScheme(
    primary = CyberCyanDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0F4FF),
    onPrimaryContainer = Color(0xFF001F28),
    secondary = RoseLove,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD8E4),
    onSecondaryContainer = Color(0xFF3B071A),
    tertiary = WarmGold,
    background = Color(0xFFF7F6FC),
    onBackground = Color(0xFF191724),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191724),
    surfaceVariant = Color(0xFFECEAF7),
    onSurfaceVariant = Color(0xFF4C4765),
    outline = Color(0xFFCCC7E5),
    error = AlertRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to stunning futuristic dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
