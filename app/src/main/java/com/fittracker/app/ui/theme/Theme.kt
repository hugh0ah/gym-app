package com.fittracker.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EliteDarkColorScheme = darkColorScheme(
    primary = NeonMint,
    onPrimary = Color(0xFF04140B),
    primaryContainer = Color(0xFF00381B),
    onPrimaryContainer = NeonMintLight,
    secondary = ElectricCyan,
    onSecondary = Color(0xFF001B26),
    secondaryContainer = Color(0xFF00344D),
    onSecondaryContainer = ElectricCyanLight,
    tertiary = AthleticOrange,
    onTertiary = Color(0xFF2E1000),
    background = DarkBg,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextMuted,
    surfaceContainer = DarkSurfaceElevated,
    surfaceContainerHigh = DarkSurfaceElevated,
    outline = DarkCardBorder,
    outlineVariant = DarkCardBorderSubtle,
    error = AlertRed,
    onError = Color.White
)

@Composable
fun FitTrackerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DarkBg.toArgb()
                window.navigationBarColor = DarkBg.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = EliteDarkColorScheme,
        typography = Typography,
        content = content
    )
}
