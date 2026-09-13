package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TacticalCyan,
    onPrimary = DarkBackground,
    secondary = TacticalAmber,
    onSecondary = DarkBackground,
    tertiary = AfterburnerOrange,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = LightOnDark,
    surface = DarkSurface,
    onSurface = LightOnDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = MutedSlate,
    error = CrimsonWarning,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
