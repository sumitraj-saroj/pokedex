package com.dexter.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.dexter.app.data.repository.AppThemeMode

val LocalDarkTheme = staticCompositionLocalOf { false }
val LocalAppThemeMode = staticCompositionLocalOf { AppThemeMode.SYSTEM }

private val DarkColorScheme = darkColorScheme(
    primary = PokeballRedDarkPrimary,
    onPrimary = androidx.compose.ui.graphics.Color(0xFF690005),
    primaryContainer = PokeballRedContainerDark,
    onPrimaryContainer = OnPokeballRedContainerDark,
    secondary = GreatBallBlueDark,
    onSecondary = androidx.compose.ui.graphics.Color(0xFF003258),
    secondaryContainer = GreatBallBlueContainerDark,
    onSecondaryContainer = OnGreatBallBlueContainerDark,
    tertiary = UltraBallGoldDark,
    onTertiary = androidx.compose.ui.graphics.Color(0xFF412D00),
    background = DarkBackground,
    onBackground = androidx.compose.ui.graphics.Color(0xFFE2E2E9),
    surface = DarkBackground,
    onSurface = androidx.compose.ui.graphics.Color(0xFFE2E2E9),
    surfaceVariant = DarkSurfaceContainerHigh,
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFC4C6D0),
    outline = androidx.compose.ui.graphics.Color(0xFF8E9099),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFF44474F),
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest
)

private val LightColorScheme = lightColorScheme(
    primary = PokeballRedPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PokeballRedContainerLight,
    onPrimaryContainer = OnPokeballRedContainerLight,
    secondary = GreatBallBlueLight,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = GreatBallBlueContainerLight,
    onSecondaryContainer = OnGreatBallBlueContainerLight,
    tertiary = UltraBallGoldLight,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    background = LightBackground,
    onBackground = androidx.compose.ui.graphics.Color(0xFF191C20),
    surface = LightBackground,
    onSurface = androidx.compose.ui.graphics.Color(0xFF191C20),
    surfaceVariant = LightSurfaceContainerHigh,
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF44474F),
    outline = androidx.compose.ui.graphics.Color(0xFF74777F),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFFC4C6D0),
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest
)


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DexterTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    customColorScheme: ColorScheme? = null,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val colorScheme = when {
        customColorScheme != null -> customColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalAppThemeMode provides themeMode
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = MotionScheme.expressive(),
            typography = Typography,
            content = content
        )
    }
}

