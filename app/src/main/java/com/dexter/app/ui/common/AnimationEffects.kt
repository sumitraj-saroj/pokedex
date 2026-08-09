package com.dexter.app.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Static Y-offset for floating artwork to prevent continuous recomposition lag.
 */
@Composable
fun rememberBreathingYOffset(
    maxOffsetDp: Dp = 8.dp,
    durationMillis: Int = 2400
): Dp {
    return 0.dp
}

/**
 * Holographic shimmer overlay (disabled continuous loop to optimize frame rate during scroll).
 */
fun Modifier.holographicShimmer(
    enabled: Boolean = true,
    style: HoloFoilStyle = HoloFoilStyle.COSMIC_STARFOIL,
    alphaMultiplier: Float = 1.0f,
    shape: Shape = RoundedCornerShape(20.dp)
): Modifier = this

/**
 * High-performance native clickable micro-interaction for cards and buttons.
 */
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    hapticUtils: HapticUtils? = null,
    onClick: () -> Unit
): Modifier = composed {
    if (!enabled) return@composed this

    this.clickable {
        hapticUtils?.lightClick()
        onClick()
    }
}

/**
 * Instant, crisp bounds transform for shared element transitions without spring bounce lag.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun spatialExpressiveSpring(): BoundsTransform = BoundsTransform { _, _ ->
    spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )
}

/**
 * Scale-bounce animation modifier for toggle buttons (caught, favorite).
 * Provides a satisfying spring overshoot when state changes.
 */
@Composable
fun Modifier.bounceOnStateChange(isActive: Boolean): Modifier = composed {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bounce_toggle_scale"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Press-to-shrink interaction modifier for cards and interactive containers.
 * Gently scales down to 0.96 while pressed, snaps back on release with spring physics.
 */
@Composable
fun Modifier.scaleOnPress(): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press_scale"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        }
}

/**
 * Animated section header that slides in from the left with a fade on first composition.
 */
@Composable
fun AnimatedSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) + slideInHorizontally(
            animationSpec = tween(400),
            initialOffsetX = { -it / 4 }
        ),
        modifier = modifier
    ) {
        Text(
            text = title,
            style = style,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}


