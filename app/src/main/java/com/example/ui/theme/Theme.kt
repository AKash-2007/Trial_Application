package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyanLight,
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF004D63),
    onPrimaryContainer = Color(0xFFBCE9FF),
    secondary = SecurityIndigoLight,
    onSecondary = Color(0xFF241C70),
    secondaryContainer = Color(0xFF3B3588),
    onSecondaryContainer = Color(0xFFE2DFFF),
    tertiary = SafeEmeraldLight,
    onTertiary = Color(0xFF003822),
    tertiaryContainer = Color(0xFF005234),
    onTertiaryContainer = Color(0xFF86F8BF),
    error = ThreatCrimsonLight,
    onError = Color(0xFF60000D),
    errorContainer = Color(0xFF8C0018),
    onErrorContainer = Color(0xFFFFDAD9),
    background = DarkBackground,
    onBackground = Color(0xFFE2E8F0),
    surface = DarkSurface,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val LightColorScheme = lightColorScheme(
    primary = CyberCyanDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7E7FF),
    onPrimaryContainer = Color(0xFF001E29),
    secondary = SecurityIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2DFFF),
    onSecondaryContainer = Color(0xFF130962),
    tertiary = SafeEmerald,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF86F8BF),
    onTertiaryContainer = Color(0xFF002113),
    error = ThreatCrimson,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD9),
    onErrorContainer = Color(0xFF410006),
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent cyber theme
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
