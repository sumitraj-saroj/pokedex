package com.dexter.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.dexter.app.domain.model.PokemonType
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Modern Material 3 Per-Type Dynamic Theming Engine using HCT (Hue, Chroma, Tone)
 * color space algorithms and dual-type seed blending.
 */

data class Hct(val hue: Float, val chroma: Float, val tone: Float)

fun Color.toHct(): Hct {
    val r = red
    val g = green
    val b = blue

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    val tone = (max + min) / 2f
    val chroma = if (tone == 0f || tone == 1f) 0f else delta / (1f - abs(2f * tone - 1f))

    var hue = when {
        delta == 0f -> 0f
        max == r -> ((g - b) / delta) % 6f
        max == g -> ((b - r) / delta) + 2f
        else -> ((r - g) / delta) + 4f
    } * 60f

    if (hue < 0f) hue += 360f

    return Hct(hue, chroma.coerceIn(0f, 1f), tone.coerceIn(0f, 1f))
}

fun hctToColor(hct: Hct, toneOverride: Float? = null): Color {
    val h = hct.hue
    val c = hct.chroma
    val l = toneOverride ?: hct.tone

    val k = c * (1f - abs(2f * l - 1f))
    val x = k * (1f - abs((h / 60f) % 2f - 1f))
    val m = l - k / 2f

    val (r1, g1, b1) = when {
        h < 60f -> Triple(k, x, 0f)
        h < 120f -> Triple(x, k, 0f)
        h < 180f -> Triple(0f, k, x)
        h < 240f -> Triple(0f, x, k)
        h < 300f -> Triple(x, 0f, k)
        else -> Triple(k, 0f, x)
    }

    val r = (r1 + m).coerceIn(0f, 1f)
    val g = (g1 + m).coerceIn(0f, 1f)
    val b = (b1 + m).coerceIn(0f, 1f)

    return Color(r, g, b, 1f)
}

/**
 * Blends two type seed colors using weighted RGB space blending.
 * Primary type receives 65% weight and secondary type receives 35% weight
 * to produce visually representative, grounded dual-type accent colors
 * without unexpected hue-wheel wraparound artifacts (e.g., Fire + Flying producing magenta).
 */
fun blendTypeSeedColors(primary: Color, secondary: Color?): Color {
    if (secondary == null || primary == secondary) return primary

    val weightPrimary = 0.65f
    val weightSecondary = 0.35f

    val r = (primary.red * weightPrimary + secondary.red * weightSecondary).coerceIn(0f, 1f)
    val g = (primary.green * weightPrimary + secondary.green * weightSecondary).coerceIn(0f, 1f)
    val b = (primary.blue * weightPrimary + secondary.blue * weightSecondary).coerceIn(0f, 1f)

    return Color(r, g, b, 1f)
}

/**
 * Generates a full Material 3 ColorScheme from a seed color.
 */
fun generateMaterial3ColorScheme(seedColor: Color, isDark: Boolean): ColorScheme {
    val hct = seedColor.toHct()

    val primary = hctToColor(hct, if (isDark) 0.75f else 0.40f)
    val onPrimary = if (isDark) Color.Black else Color.White
    val primaryContainer = hctToColor(hct, if (isDark) 0.25f else 0.88f)
    val onPrimaryContainer = hctToColor(hct, if (isDark) 0.90f else 0.15f)

    val secondaryHct = Hct(hct.hue, (hct.chroma * 0.5f).coerceIn(0.1f, 0.4f), hct.tone)
    val secondary = hctToColor(secondaryHct, if (isDark) 0.70f else 0.45f)
    val onSecondary = if (isDark) Color.Black else Color.White
    val secondaryContainer = hctToColor(secondaryHct, if (isDark) 0.30f else 0.85f)
    val onSecondaryContainer = hctToColor(secondaryHct, if (isDark) 0.90f else 0.20f)

    val tertiaryHct = Hct((hct.hue + 60f) % 360f, (hct.chroma * 0.6f).coerceIn(0.15f, 0.5f), hct.tone)
    val tertiary = hctToColor(tertiaryHct, if (isDark) 0.75f else 0.40f)
    val onTertiary = if (isDark) Color.Black else Color.White
    val tertiaryContainer = hctToColor(tertiaryHct, if (isDark) 0.25f else 0.88f)
    val onTertiaryContainer = hctToColor(tertiaryHct, if (isDark) 0.90f else 0.15f)

    val backgroundHct = Hct(hct.hue, 0.05f, hct.tone)
    val background = hctToColor(backgroundHct, if (isDark) 0.06f else 0.98f)
    val onBackground = hctToColor(backgroundHct, if (isDark) 0.95f else 0.08f)

    val surface = hctToColor(backgroundHct, if (isDark) 0.08f else 0.96f)
    val onSurface = hctToColor(backgroundHct, if (isDark) 0.92f else 0.10f)
    val surfaceVariant = hctToColor(backgroundHct, if (isDark) 0.18f else 0.90f)
    val onSurfaceVariant = hctToColor(backgroundHct, if (isDark) 0.80f else 0.30f)

    val surfaceContainer = hctToColor(backgroundHct, if (isDark) 0.12f else 0.94f)
    val outline = hctToColor(backgroundHct, if (isDark) 0.50f else 0.50f)

    return if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            surfaceContainer = surfaceContainer
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            surfaceContainer = surfaceContainer
        )
    }
}

private data class ColorSchemeCacheKey(
    val primaryType: PokemonType,
    val secondaryType: PokemonType?,
    val isDark: Boolean
)

private val colorSchemeCache = java.util.concurrent.ConcurrentHashMap<ColorSchemeCacheKey, ColorScheme>()

fun getOrGenerateTypeColorScheme(
    primaryType: PokemonType,
    secondaryType: PokemonType? = null,
    isDark: Boolean = false
): ColorScheme {
    val key = ColorSchemeCacheKey(primaryType, secondaryType, isDark)
    return colorSchemeCache.getOrPut(key) {
        val blendedSeed = blendTypeSeedColors(primaryType.seedColor, secondaryType?.seedColor)
        generateMaterial3ColorScheme(blendedSeed, isDark)
    }
}

@Composable
fun rememberTypeColorScheme(
    primaryType: PokemonType,
    secondaryType: PokemonType? = null,
    isDark: Boolean = false
): ColorScheme {
    return remember(primaryType, secondaryType, isDark) {
        getOrGenerateTypeColorScheme(primaryType, secondaryType, isDark)
    }
}

@Composable
fun animateColorSchemeAsState(
    targetColorScheme: ColorScheme,
    animationSpec: AnimationSpec<Color> = tween(durationMillis = 600)
): ColorScheme {
    val primary = animateColorAsState(targetColorScheme.primary, animationSpec, label = "primary").value
    val onPrimary = animateColorAsState(targetColorScheme.onPrimary, animationSpec, label = "onPrimary").value
    val primaryContainer = animateColorAsState(targetColorScheme.primaryContainer, animationSpec, label = "primaryContainer").value
    val onPrimaryContainer = animateColorAsState(targetColorScheme.onPrimaryContainer, animationSpec, label = "onPrimaryContainer").value
    val secondary = animateColorAsState(targetColorScheme.secondary, animationSpec, label = "secondary").value
    val onSecondary = animateColorAsState(targetColorScheme.onSecondary, animationSpec, label = "onSecondary").value
    val secondaryContainer = animateColorAsState(targetColorScheme.secondaryContainer, animationSpec, label = "secondaryContainer").value
    val onSecondaryContainer = animateColorAsState(targetColorScheme.onSecondaryContainer, animationSpec, label = "onSecondaryContainer").value
    val tertiary = animateColorAsState(targetColorScheme.tertiary, animationSpec, label = "tertiary").value
    val onTertiary = animateColorAsState(targetColorScheme.onTertiary, animationSpec, label = "onTertiary").value
    val tertiaryContainer = animateColorAsState(targetColorScheme.tertiaryContainer, animationSpec, label = "tertiaryContainer").value
    val onTertiaryContainer = animateColorAsState(targetColorScheme.onTertiaryContainer, animationSpec, label = "onTertiaryContainer").value
    val background = animateColorAsState(targetColorScheme.background, animationSpec, label = "background").value
    val onBackground = animateColorAsState(targetColorScheme.onBackground, animationSpec, label = "onBackground").value
    val surface = animateColorAsState(targetColorScheme.surface, animationSpec, label = "surface").value
    val onSurface = animateColorAsState(targetColorScheme.onSurface, animationSpec, label = "onSurface").value
    val surfaceVariant = animateColorAsState(targetColorScheme.surfaceVariant, animationSpec, label = "surfaceVariant").value
    val onSurfaceVariant = animateColorAsState(targetColorScheme.onSurfaceVariant, animationSpec, label = "onSurfaceVariant").value
    val outline = animateColorAsState(targetColorScheme.outline, animationSpec, label = "outline").value
    val surfaceContainer = animateColorAsState(targetColorScheme.surfaceContainer, animationSpec, label = "surfaceContainer").value

    return targetColorScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        surfaceContainer = surfaceContainer
    )
}

/**
 * Encapsulates Material 3 ColorScheme alongside ambient radial background glow brush
 * and high-contrast text color for elemental types.
 */
data class PokemonTypeColorScheme(
    val colorScheme: ColorScheme,
    val ambientGradient: androidx.compose.ui.graphics.Brush,
    val contrastTextColor: Color
)

/**
 * Creates a rich ambient radial/mesh gradient combining primary type, secondary type,
 * and background surface tones into a dynamic glow.
 */
fun createAmbientTypeGradient(
    primaryType: PokemonType,
    secondaryType: PokemonType? = null,
    isDark: Boolean = false,
    colorScheme: ColorScheme
): androidx.compose.ui.graphics.Brush {
    val primarySeed = primaryType.seedColor
    val secondarySeed = secondaryType?.seedColor ?: primarySeed
    val surfaceColor = colorScheme.surface

    val centerColor = primarySeed.copy(alpha = if (isDark) 0.35f else 0.28f)
    val midColor = secondarySeed.copy(alpha = if (isDark) 0.20f else 0.14f)
    val outerColor = surfaceColor

    return androidx.compose.ui.graphics.Brush.radialGradient(
        colors = listOf(centerColor, midColor, outerColor),
        center = androidx.compose.ui.geometry.Offset.Unspecified,
        radius = Float.POSITIVE_INFINITY
    )
}

/**
 * Computes WCAG 2.1 compliant contrasting text color (dark charcoal or crisp white)
 * based on relative luminance of the provided background color.
 */
fun getContrastingTextColor(backgroundColor: Color): Color {
    val r = backgroundColor.red
    val g = backgroundColor.green
    val b = backgroundColor.blue
    val luminance = 0.2126f * r + 0.7152f * g + 0.0722f * b
    return if (luminance > 0.45f) Color(0xFF101318) else Color(0xFFFFFFFF)
}

/**
 * Retrieves cached or generates a full [PokemonTypeColorScheme] containing Material 3 ColorScheme,
 * radial ambient background brush, and contrasting text colors.
 */
fun getOrGeneratePokemonTypeColorScheme(
    primaryType: PokemonType,
    secondaryType: PokemonType? = null,
    isDark: Boolean = false
): PokemonTypeColorScheme {
    val colorScheme = getOrGenerateTypeColorScheme(primaryType, secondaryType, isDark)
    val ambientGradient = createAmbientTypeGradient(primaryType, secondaryType, isDark, colorScheme)
    val contrastTextColor = getContrastingTextColor(primaryType.seedColor)

    return PokemonTypeColorScheme(
        colorScheme = colorScheme,
        ambientGradient = ambientGradient,
        contrastTextColor = contrastTextColor
    )
}

@Composable
fun rememberPokemonTypeColorScheme(
    primaryType: PokemonType,
    secondaryType: PokemonType? = null,
    isDark: Boolean = false
): PokemonTypeColorScheme {
    return remember(primaryType, secondaryType, isDark) {
        getOrGeneratePokemonTypeColorScheme(primaryType, secondaryType, isDark)
    }
}
