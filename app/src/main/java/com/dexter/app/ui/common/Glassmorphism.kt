package com.dexter.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable glassmorphic modifier providing a frosted glass container layout
 * with smooth translucent surface background, customizable shape, and crisp inner gradient border.
 */
fun Modifier.glassmorphicContainer(
    backgroundColor: Color = Color.White.copy(alpha = 0.12f),
    borderColor: Color = Color.Unspecified,
    borderWidth: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(20.dp),
    shadowElevation: Dp = 0.dp
): Modifier {
    val borderBrush = if (borderColor != Color.Unspecified) {
        Brush.verticalGradient(
            colors = listOf(
                borderColor.copy(alpha = (borderColor.alpha * 0.8f).coerceIn(0.15f, 0.7f)),
                borderColor.copy(alpha = (borderColor.alpha * 0.2f).coerceIn(0.03f, 0.25f))
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.25f),
                Color.White.copy(alpha = 0.05f)
            )
        )
    }

    return this
        .then(if (shadowElevation > 0.dp) Modifier.shadow(shadowElevation, shape) else Modifier)
        .clip(shape)
        .background(backgroundColor, shape)
        .border(borderWidth, borderBrush, shape)
}
