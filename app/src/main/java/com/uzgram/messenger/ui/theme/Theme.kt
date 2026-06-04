package com.uzgram.messenger.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// UzGram Brand Colors
val UzBlue = Color(0xFF2AABEE)
val UzBlueDeep = Color(0xFF229ED9)
val UzBlueLite = Color(0xFFE8F5FC)
val UzDarkBg = Color(0xFF0E0E0E)
val UzDarkSurface = Color(0xFF1C1C1C)
val UzDarkSurface2 = Color(0xFF262626)
val UzSuccess = Color(0xFF4CAF50)
val UzError = Color(0xFFFF3B30)
val UzWarning = Color(0xFFFF9500)

private val DarkColorScheme = darkColorScheme(
    primary = UzBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF004D6E),
    onPrimaryContainer = UzBlueLite,
    secondary = Color(0xFF2AABEE).copy(alpha = 0.7f),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1A3A4A),
    onSecondaryContainer = Color(0xFFBBDEF0),
    tertiary = UzSuccess,
    onTertiary = Color.White,
    background = UzDarkBg,
    onBackground = Color.White,
    surface = UzDarkSurface,
    onSurface = Color.White,
    surfaceVariant = UzDarkSurface2,
    onSurfaceVariant = Color(0xFFAAAAAA),
    error = UzError,
    onError = Color.White,
    outline = Color(0xFF2C2C2C),
    outlineVariant = Color(0xFF1F1F1F),
    inverseSurface = Color(0xFFF0F0F0),
    inverseOnSurface = Color(0xFF1A1A1A),
    inversePrimary = UzBlueDeep
)

private val LightColorScheme = lightColorScheme(
    primary = UzBlue,
    onPrimary = Color.White,
    primaryContainer = UzBlueLite,
    onPrimaryContainer = Color(0xFF003547),
    secondary = UzBlueDeep,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0EEFA),
    onSecondaryContainer = Color(0xFF002536),
    tertiary = UzSuccess,
    onTertiary = Color.White,
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF111111),
    surface = Color.White,
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF666666),
    error = UzError,
    onError = Color.White,
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFEEEEEE),
    inverseSurface = UzDarkSurface,
    inverseOnSurface = Color.White,
    inversePrimary = UzBlueLite
)

@Composable
fun UzGramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = UzGramTypography,
        shapes = UzGramShapes,
        content = content
    )
}
