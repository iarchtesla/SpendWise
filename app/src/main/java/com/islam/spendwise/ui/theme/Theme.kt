package com.islam.spendwise.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light: deep teal primary on warm sand, premium financial feel.
private val LightColors = lightColorScheme(
    primary = Color(0xFF00696E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF9BE4E8),
    onPrimaryContainer = Color(0xFF002022),
    secondary = Color(0xFF4E6A6B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFE8E9),
    onSecondaryContainer = Color(0xFF051F20),
    tertiary = Color(0xFF745B00),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDF9E),
    onTertiaryContainer = Color(0xFF251A00),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFBF9F7),
    onBackground = Color(0xFF1B1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1C1E),
    surfaceVariant = Color(0xFFE3E9E9),
    onSurfaceVariant = Color(0xFF3F4949),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F3F1),
    surfaceContainer = Color(0xFFEFEEEC),
    surfaceContainerHigh = Color(0xFFEAE8E6),
    surfaceContainerHighest = Color(0xFFE4E2E0),
    outline = Color(0xFF6F7979),
    outlineVariant = Color(0xFFBEC8C8)
)

// Dark: soft teal on deep ink, high-luminance-teal accents, zero pure-black surfaces.
private val DarkColors = darkColorScheme(
    primary = Color(0xFF4EB9BF),
    onPrimary = Color(0xFF00363B),
    primaryContainer = Color(0xFF004F54),
    onPrimaryContainer = Color(0xFF9BE4E8),
    secondary = Color(0xFFB1CBCC),
    onSecondary = Color(0xFF1D3536),
    secondaryContainer = Color(0xFF354B4C),
    onSecondaryContainer = Color(0xFFCFE8E9),
    tertiary = Color(0xFFE3C16D),
    onTertiary = Color(0xFF3F2E00),
    tertiaryContainer = Color(0xFF584400),
    onTertiaryContainer = Color(0xFFFFDF9E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111414),
    onBackground = Color(0xFFE2E4E4),
    surface = Color(0xFF1A1D1D),
    onSurface = Color(0xFFE2E4E4),
    surfaceVariant = Color(0xFF3F4949),
    onSurfaceVariant = Color(0xFFBEC8C8),
    surfaceContainerLowest = Color(0xFF0C0F0F),
    surfaceContainerLow = Color(0xFF1A1D1D),
    surfaceContainer = Color(0xFF1E2121),
    surfaceContainerHigh = Color(0xFF282B2B),
    surfaceContainerHighest = Color(0xFF333536),
    outline = Color(0xFF899393),
    outlineVariant = Color(0xFF2F3A3A)
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
