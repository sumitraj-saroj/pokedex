package com.dexter.app.ui.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Creates a smooth breathing Y-offset animation for floating artwork.
 */
@Composable
fun rememberBreathingYOffset(
    maxOffsetDp: Dp = 8.dp,
    durationMillis: Int = 2400
): Dp {
    val infiniteTransition = rememberInfiniteTransition(label = "BreathingTransition")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathingYOffset"
    )
    return -maxOffsetDp * progress
}

/**
 * Applies a dynamic holographic metallic shimmer foil overlay across a composable container.
 */
fun Modifier.holographicShimmer(
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "HoloShimmerTransition")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HoloOffset"
    )

    this.drawWithContent {
        drawContent()

        val width = size.width
        val height = size.height
        val startX = width * shimmerOffset
        val startY = height * (shimmerOffset * 0.5f)

        val holographicColors = listOf(
            Color(0x00FFFFFF),
            Color(0x33FFD700), // Gold
            Color(0x44FF007F), // Pink
            Color(0x4400E5FF), // Cyan
            Color(0x449932CC), // Purple
            Color(0x33FFD700), // Gold
            Color(0x00FFFFFF)
        )

        drawRect(
            brush = Brush.linearGradient(
                colors = holographicColors,
                start = Offset(startX, startY),
                end = Offset(startX + width * 0.8f, startY + height * 0.8f)
            ),
            blendMode = BlendMode.Screen
        )
    }
}
