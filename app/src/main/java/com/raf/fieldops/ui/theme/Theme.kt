package com.raf.fieldops.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.raf.fieldops.util.ThemePreference

val LocalIsDarkResolved = staticCompositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary = BtIndigoLight,
    onPrimary = Color.White,
    primaryContainer = BtIndigoDark,
    onPrimaryContainer = Color.White,
    secondary = BtMagenta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3D0025),
    onSecondaryContainer = Color(0xFFFFD9E8),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF1A0044),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF280001)
)

private val LightColorScheme = lightColorScheme(
    primary = BtIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = BtIndigoDark,
    secondary = BtMagenta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFDE7F3),
    onSecondaryContainer = Color(0xFF6B0F3B),
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun FieldOpsTheme(
    themePreference: ThemePreference = ThemePreference.System,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()

    val darkTheme = when (themePreference) {
        ThemePreference.System -> systemDark
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalIsDarkResolved provides darkTheme,
        LocalSpacing provides FieldOpsSpacing(),
        LocalElevation provides FieldOpsElevation()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FieldOpsTypography,
            shapes = FieldOpsShapes,
            content = content
        )
    }
}
