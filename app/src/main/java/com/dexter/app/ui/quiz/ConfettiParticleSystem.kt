package com.dexter.app.ui.quiz

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var vRot: Float,
    var scale: Float,
    val color: Color,
    val shapeType: Int, // 0: Circle, 1: Square, 2: Pokeball
    var alpha: Float = 1.0f
)

/**
 * Canvas Confetti Particle System.
 * Spawns 60+ animated colored shapes (circles, squares, pokeballs) with physics gravity,
 * velocity, drag, and rotation when [trigger] is activated.
 */
@Composable
fun ConfettiParticleSystem(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 60,
    onFinished: () -> Unit = {}
) {
    var particles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }

    LaunchedEffect(trigger) {
        if (!trigger) {
            particles = emptyList()
            return@LaunchedEffect
        }

        // Initialize 60 particles bursting from center top
        val colorPalette = listOf(
            Color(0xFFEE1515), // Pokeball Red
            Color(0xFFFFD700), // Gold
            Color(0xFF2196F3), // Water Blue
            Color(0xFF4CAF50), // Grass Green
            Color(0xFF9C27B0), // Psychic Purple
            Color(0xFFFF9800), // Fire Orange
            Color(0xFFE91E63), // Pink
            Color.White
        )

        val newParticles = List(particleCount) {
            ConfettiParticle(
                x = 0f, // updated on first frame draw relative to canvas size
                y = 0f,
                vx = (Random.nextFloat() * 1400f - 700f),
                vy = -(Random.nextFloat() * 900f + 500f),
                rotation = Random.nextFloat() * 360f,
                vRot = (Random.nextFloat() * 720f - 360f),
                scale = Random.nextFloat() * 0.7f + 0.6f,
                color = colorPalette.random(),
                shapeType = Random.nextInt(3),
                alpha = 1.0f
            )
        }
        particles = newParticles

        var startTimeNanos = 0L
        var lastTimeNanos = 0L

        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (startTimeNanos == 0L) {
                    startTimeNanos = frameTimeNanos
                    lastTimeNanos = frameTimeNanos
                }

                val dt = (frameTimeNanos - lastTimeNanos) / 1_000_000_000f
                lastTimeNanos = frameTimeNanos
                val elapsedTime = (frameTimeNanos - startTimeNanos) / 1_000_000_000f

                if (elapsedTime > 2.5f) {
                    particles = emptyList()
                    onFinished()
                    return@withFrameNanos
                }

                val gravity = 1800f
                val drag = 0.98f

                val updated = particles.map { p ->
                    val newVx = p.vx * drag
                    val newVy = (p.vy + gravity * dt) * drag
                    val newX = p.x + newVx * dt
                    val newY = p.y + newVy * dt
                    val newRot = p.rotation + p.vRot * dt
                    val newAlpha = if (elapsedTime > 1.8f) {
                        ((2.5f - elapsedTime) / 0.7f).coerceIn(0f, 1f)
                    } else 1.0f

                    p.copy(
                        x = newX,
                        y = newY,
                        vx = newVx,
                        vy = newVy,
                        rotation = newRot,
                        alpha = newAlpha
                    )
                }
                particles = updated
            }

            if (particles.isEmpty()) break
        }
    }

    if (particles.isNotEmpty()) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2f
            val startY = canvasHeight * 0.35f

            particles.forEach { p ->
                // First frame initialization of center origin
                val actualX = if (p.x == 0f) centerX else p.x
                val actualY = if (p.y == 0f) startY else p.y

                translate(left = actualX, top = actualY) {
                    rotate(degrees = p.rotation) {
                        scale(scale = p.scale) {
                            val alphaColor = p.color.copy(alpha = p.alpha)

                            when (p.shapeType) {
                                0 -> {
                                    // Circle particle
                                    drawCircle(
                                        color = alphaColor,
                                        radius = 7.dp.toPx()
                                    )
                                }
                                1 -> {
                                    // Square particle
                                    val sizePx = 14.dp.toPx()
                                    drawRect(
                                        color = alphaColor,
                                        topLeft = Offset(-sizePx / 2f, -sizePx / 2f),
                                        size = Size(sizePx, sizePx)
                                    )
                                }
                                2 -> {
                                    // Mini Pokeball particle
                                    val r = 9.dp.toPx()
                                    // Top red half
                                    drawArc(
                                        color = Color(0xFFEE1515).copy(alpha = p.alpha),
                                        startAngle = 180f,
                                        sweepAngle = 180f,
                                        useCenter = true,
                                        topLeft = Offset(-r, -r),
                                        size = Size(2f * r, 2f * r)
                                    )
                                    // Bottom white half
                                    drawArc(
                                        color = Color.White.copy(alpha = p.alpha),
                                        startAngle = 0f,
                                        sweepAngle = 180f,
                                        useCenter = true,
                                        topLeft = Offset(-r, -r),
                                        size = Size(2f * r, 2f * r)
                                    )
                                    // Black center line & border
                                    drawLine(
                                        color = Color.Black.copy(alpha = p.alpha),
                                        start = Offset(-r, 0f),
                                        end = Offset(r, 0f),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                    drawCircle(
                                        color = Color.Black.copy(alpha = p.alpha),
                                        radius = r,
                                        style = Stroke(width = 1.5.dp.toPx())
                                    )
                                    // Center white button
                                    drawCircle(
                                        color = Color.White.copy(alpha = p.alpha),
                                        radius = 3.dp.toPx()
                                    )
                                    drawCircle(
                                        color = Color.Black.copy(alpha = p.alpha),
                                        radius = 3.dp.toPx(),
                                        style = Stroke(width = 1.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
