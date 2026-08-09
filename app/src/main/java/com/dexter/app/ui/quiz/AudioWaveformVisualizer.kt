package com.dexter.app.ui.quiz

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Live Canvas audio soundwave frequency visualizer.
 * Renders animated vertical bars driven by smooth sine-wave combinations when [isPlaying] is active.
 */
@Composable
fun AudioWaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    barCount: Int = 20,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SoundwaveTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SoundwavePhase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val widthPx = size.width
        val heightPx = size.height
        val centerY = heightPx / 2f

        val totalSpacingRatio = 0.4f
        val barSlotWidth = widthPx / barCount
        val barWidth = barSlotWidth * (1f - totalSpacingRatio)
        val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

        for (i in 0 until barCount) {
            val xOffset = i * barSlotWidth + (barSlotWidth - barWidth) / 2f

            val wave = if (isPlaying) {
                val sin1 = sin(phase * 2f * PI + i * 0.45f).toFloat()
                val cos2 = cos(phase * 4f * PI - i * 0.35f).toFloat()
                val sin3 = sin(phase * 6f * PI + i * 0.2f).toFloat()
                val combined = (sin1 * 0.45f + cos2 * 0.35f + sin3 * 0.2f + 1f) / 2f
                combined.coerceIn(0.15f, 1.0f)
            } else {
                val idleWave = sin(phase * 2f * PI + i * 0.3f).toFloat()
                (0.12f + 0.05f * idleWave).coerceIn(0.08f, 0.2f)
            }

            val currentBarHeight = heightPx * wave
            val topY = centerY - currentBarHeight / 2f

            val brush = Brush.verticalGradient(
                colors = listOf(primaryColor, secondaryColor),
                startY = topY,
                endY = topY + currentBarHeight
            )

            drawRoundRect(
                brush = brush,
                topLeft = Offset(xOffset, topY),
                size = Size(barWidth, currentBarHeight),
                cornerRadius = cornerRadius
            )
        }
    }
}
