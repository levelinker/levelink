package com.zeroorhunderd.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CrimsonRed,
    onPrimary = Color.White,
    secondary = CyberGold,
    onSecondary = Color.Black,
    background = CyberBlack,
    onBackground = Color.White,
    surface = CyberSurface,
    onSurface = Color.White
)

@Composable
fun ZeroOrHunderdTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
