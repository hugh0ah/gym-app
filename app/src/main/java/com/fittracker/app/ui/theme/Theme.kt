package com.fittracker.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val WellnessColorScheme = lightColorScheme(
    primary = AccentCoral,
    onPrimary = CreamSurface,
    primaryContainer = AccentCoralContainer,
    onPrimaryContainer = AccentCoralDark,
    secondary = ElectricBlue,
    onSecondary = CreamSurface,
    secondaryContainer = ElectricBlueContainer,
    onSecondaryContainer = ElectricBlueDark,
    tertiary = WarmFlame,
    onTertiary = TextDark,
    background = CreamBg,
    onBackground = TextDark,
    surface = CreamSurface,
    onSurface = TextDark,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = TextDarkMuted,
    outline = CreamBorder,
    error = ErrorRed,
    onError = CreamSurface
)

@Composable
fun FitTrackerTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = CreamBg.toArgb()
                window.navigationBarColor = CreamBg.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = WellnessColorScheme,
        typography = Typography,
        content = content
    )
}