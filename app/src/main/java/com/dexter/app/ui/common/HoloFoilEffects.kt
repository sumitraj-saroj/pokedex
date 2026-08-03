package com.dexter.app.ui.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * HoloFoil style presets for different visual aesthetics.
 */
enum class HoloFoilStyle {
    /** Dynamic iridescent spectrum + twinkling star sparkles + radial glare spot (Secret Rare / Cosmic) */
    COSMIC_STARFOIL,

    /** High-intensity multi-gradient prism angle sweep + metallic light glare beam + micro glitter */
    RAINBOW_SECRET,

    /** Classic diagonal holofoil sheen with light sweep */
    CLASSIC_HOLO
}

/**
 * Normalized particle representation for procedural foil sparkles/glitter.
 */
data class SparkleParticle(
    val normX: Float,
    val normY: Float,
    val sizeDp: Float,
    val phaseOffset: Float,
    val isStarShape: Boolean
)

/**
 * Pre-calculated deterministic sparkle particle layout (36 particles distributed over the card surface).
 */
val PrecalculatedSparkleParticles: List<SparkleParticle> = listOf(
    SparkleParticle(0.18f, 0.15f, 5.5f, 0.2f, true),
    SparkleParticle(0.72f, 0.12f, 4.0f, 1.4f, false),
    SparkleParticle(0.85f, 0.25f, 6.0f, 2.1f, true),
    SparkleParticle(0.35f, 0.28f, 3.5f, 0.8f, false),
    SparkleParticle(0.52f, 0.18f, 5.0f, 3.0f, true),
    SparkleParticle(0.12f, 0.38f, 4.5f, 2.5f, false),
    SparkleParticle(0.64f, 0.42f, 7.0f, 1.1f, true),
    SparkleParticle(0.28f, 0.50f, 5.0f, 0.4f, true),
    SparkleParticle(0.80f, 0.55f, 4.0f, 3.7f, false),
    SparkleParticle(0.45f, 0.62f, 6.5f, 1.9f, true),
    SparkleParticle(0.15f, 0.68f, 4.0f, 2.8f, false),
    SparkleParticle(0.75f, 0.72f, 5.5f, 0.6f, true),
    SparkleParticle(0.32f, 0.82f, 4.5f, 3.3f, false),
    SparkleParticle(0.88f, 0.85f, 6.0f, 1.7f, true),
    SparkleParticle(0.55f, 0.88f, 3.8f, 2.3f, false),
    SparkleParticle(0.22f, 0.92f, 5.0f, 0.9f, true),
    SparkleParticle(0.40f, 0.10f, 3.2f, 2.0f, false),
    SparkleParticle(0.92f, 0.38f, 5.2f, 3.5f, true),
    SparkleParticle(0.08f, 0.52f, 4.2f, 1.2f, false),
    SparkleParticle(0.58f, 0.32f, 6.2f, 0.5f, true),
    SparkleParticle(0.25f, 0.75f, 3.6f, 2.7f, false),
    SparkleParticle(0.68f, 0.20f, 4.8f, 3.1f, true),
    SparkleParticle(0.48f, 0.48f, 7.5f, 1.6f, true),
    SparkleParticle(0.82f, 0.40f, 3.5f, 0.3f, false)
)

// Premium Iridescent Spectrum Palettes
private val CosmicSpectrum = listOf(
    Color(0x00FFFFFF),
    Color(0x66FFD700), // Gold
    Color(0x77FF007F), // Neon Pink
    Color(0x777D00FF), // Deep Violet
    Color(0x7700E5FF), // Electric Cyan
    Color(0x7700E676), // Emerald Green
    Color(0x66FFAB00), // Amber Gold
    Color(0x00FFFFFF)
)

private val RainbowSecretSpectrum = listOf(
    Color(0x00FFFFFF),
    Color(0x77FF4081), // Magenta
    Color(0x77E040FB), // Purple
    Color(0x777C4DFF), // Deep Indigo
    Color(0x7740C4FF), // Vivid Blue/Cyan
    Color(0x7764DD17), // Bright Lime
    Color(0x77FFD700), // Gold
    Color(0x77FF6E40), // Coral
    Color(0x00FFFFFF)
)

private val ClassicSpectrum = listOf(
    Color(0x00FFFFFF),
    Color(0x55FFD700), // Gold
    Color(0x5500E5FF), // Cyan
    Color(0x55FF007F), // Pink
    Color(0x55FFD700), // Gold
    Color(0x00FFFFFF)
)

/**
 * Core multi-layered holographic foil rendering engine.
 * Draws realistic iridescent diffraction, specular light glare, diagonal metallic sheen,
 * procedural starfoil sparkles, and 3D border bevel lighting.
 */
fun DrawScope.drawHoloFoilOverlay(
    normX: Float,
    normY: Float,
    animPhase: Float = 0f,
    style: HoloFoilStyle = HoloFoilStyle.COSMIC_STARFOIL,
    alphaMultiplier: Float = 1.0f
) {
    val width = size.width
    val height = size.height
    val minDim = minOf(width, height)
    val maxDim = maxOf(width, height)

    // 1. Calculate dynamic light source position and angle vector
    val lightAngle = atan2(normY, normX) + (animPhase * PI.toFloat() * 2f)
    val tiltMagnitude = hypot(normX, normY).coerceIn(0f, 1f)

    // Specular glare hotspot position
    val glareX = width * (0.5f + normX * 0.42f + cos(animPhase * PI.toFloat() * 2f) * 0.05f)
    val glareY = height * (0.5f + normY * 0.42f + sin(animPhase * PI.toFloat() * 2f) * 0.05f)

    // ---------------------------------------------------------------------------------------------
    // PASS 1: Iridescent Prism Spectrum (ColorDodge / Overlay)
    // ---------------------------------------------------------------------------------------------
    val spectrumColors = when (style) {
        HoloFoilStyle.COSMIC_STARFOIL -> CosmicSpectrum
        HoloFoilStyle.RAINBOW_SECRET -> RainbowSecretSpectrum
        HoloFoilStyle.CLASSIC_HOLO -> ClassicSpectrum
    }

    val specCos = cos(lightAngle)
    val specSin = sin(lightAngle)
    val span = maxDim * 1.4f

    val startOffset = Offset(
        x = width * 0.5f - specCos * span,
        y = height * 0.5f - specSin * span
    )
    val endOffset = Offset(
        x = width * 0.5f + specCos * span,
        y = height * 0.5f + specSin * span
    )

    drawRect(
        brush = Brush.linearGradient(
            colors = spectrumColors,
            start = startOffset,
            end = endOffset
        ),
        blendMode = BlendMode.ColorDodge,
        alpha = 0.52f * alphaMultiplier
    )

    // ---------------------------------------------------------------------------------------------
    // PASS 2: Metallic Specular Beam Sheen (Diagonal Light Sweep)
    // ---------------------------------------------------------------------------------------------
    val sheenSweepOffset = (normX + normY + animPhase * 2f) % 2f - 1f
    val sheenStartX = width * sheenSweepOffset
    val sheenStartY = height * (sheenSweepOffset * 0.5f)

    val specularBeamColors = listOf(
        Color(0x00FFFFFF),
        Color(0x33FFFFFF),
        Color(0xCCFFFDF0), // Concentrated metallic white beam core
        Color(0x99FFD700), // Gold specular halo
        Color(0x33FFFFFF),
        Color(0x00FFFFFF)
    )

    drawRect(
        brush = Brush.linearGradient(
            colors = specularBeamColors,
            start = Offset(sheenStartX - width * 0.3f, sheenStartY - height * 0.3f),
            end = Offset(sheenStartX + width * 0.4f, sheenStartY + height * 0.4f)
        ),
        blendMode = BlendMode.Plus,
        alpha = 0.65f * alphaMultiplier
    )

    // ---------------------------------------------------------------------------------------------
    // PASS 3: Radial Specular Light Source Glare Spot (Overhead Light Reflection)
    // ---------------------------------------------------------------------------------------------
    val glareRadius = maxDim * (0.45f + tiltMagnitude * 0.15f)
    val glareColors = listOf(
        Color(0xAAFFFFFF),
        Color(0x55FFE8A3),
        Color(0x2200E5FF),
        Color(0x00FFFFFF)
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = glareColors,
            center = Offset(glareX, glareY),
            radius = glareRadius
        ),
        radius = glareRadius,
        center = Offset(glareX, glareY),
        blendMode = BlendMode.Screen,
        alpha = 0.60f * alphaMultiplier
    )

    // ---------------------------------------------------------------------------------------------
    // PASS 4: Procedural Starfoil & Glitter Sparkles
    // ---------------------------------------------------------------------------------------------
    if (style == HoloFoilStyle.COSMIC_STARFOIL || style == HoloFoilStyle.RAINBOW_SECRET) {
        val starPath = Path()
        PrecalculatedSparkleParticles.forEach { particle ->
            val px = width * particle.normX
            val py = height * particle.normY

            // Distance to glare spot drives glint sparkle intensity
            val distToGlare = hypot(px - glareX, py - glareY)
            val glareProximity = (1f - distToGlare / (maxDim * 0.65f)).coerceIn(0f, 1f)

            val sparklePhase = sin(animPhase * 4f + particle.phaseOffset + glareProximity * 6f)
            val brightness = (glareProximity * 0.6f + sparklePhase * 0.4f).coerceIn(0f, 1f)

            if (brightness > 0.12f) {
                val sizePx = particle.sizeDp.dp.toPx() * (0.6f + brightness * 0.6f)
                val alpha = (brightness * 0.9f * alphaMultiplier).coerceIn(0f, 1f)

                if (particle.isStarShape) {
                    // Draw 4-point Diamond Starburst
                    starPath.reset()
                    val half = sizePx * 0.5f
                    val quarter = sizePx * 0.18f

                    starPath.moveTo(px, py - half)
                    starPath.quadraticTo(px, py, px + half, py)
                    starPath.quadraticTo(px, py, px, py + half)
                    starPath.quadraticTo(px, py, px - half, py)
                    starPath.quadraticTo(px, py, px, py - half)
                    starPath.close()

                    // Star Outer Gold Glow
                    drawPath(
                        path = starPath,
                        color = Color(0xFFFFD700).copy(alpha = alpha * 0.6f)
                    )
                    // Star Core White Sparkle
                    drawPath(
                        path = starPath,
                        color = Color.White.copy(alpha = alpha)
                    )
                } else {
                    // Glitter Dot
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = sizePx * 0.4f,
                        center = Offset(px, py)
                    )
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // PASS 5: Seamless Edge Integration (No White Border Box)
    // ---------------------------------------------------------------------------------------------
}
