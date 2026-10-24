package com.personal.investmenttracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    secondary = GoldAccent,
    onSecondary = TealDark,
    background = CanvasLight,
    surface = CardLight,
    surfaceVariant = Color(0xFFE7EFED),
    onBackground = TealDark,
    onSurface = TealDark,
    onSurfaceVariant = Color(0xFF4B5C59),
)

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = TealDark,
    primaryContainer = Color(0xFF134E4A),
    secondary = GoldAccent,
    background = CanvasDark,
    surface = CardDark,
    surfaceVariant = Color(0xFF1E2E2C),
    onBackground = Color(0xFFE2E8E7),
    onSurface = Color(0xFFE2E8E7),
)

@Composable
fun MyInvestmentTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
