package com.islam.spendwise.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Teal500 = Color(0xFF00696E)
private val Teal300 = Color(0xFF4EB9BF)
private val Sand100 = Color(0xFFFFFBF2)
private val Ink900 = Color(0xFF1B1C1E)
private val Ink800 = Color(0xFF2A2B2D)

private val LightColors = lightColorScheme(
    primary = Teal500,
    primaryContainer = Color(0xFF9BE4E8),
    secondary = Color(0xFF4E6A6B),
    background = Sand100,
    surface = Color.White,
    onBackground = Ink900,
    onSurface = Ink900
)

private val DarkColors = darkColorScheme(
    primary = Teal300,
    primaryContainer = Color(0xFF004F54),
    secondary = Color(0xFFB1CBCC),
    background = Ink900,
    surface = Ink800,
    onBackground = Color(0xFFE4E2E0),
    onSurface = Color(0xFFE4E2E0)
)

@Composable
fun SpendWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
