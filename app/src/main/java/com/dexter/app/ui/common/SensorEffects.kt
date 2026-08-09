package com.dexter.app.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

data class TiltState(
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
    val normX: Float = 0f,
    val normY: Float = 0f
)

@Composable
fun rememberTiltSensorState(
    maxRotationDegrees: Float = 15f
): TiltState {
    return TiltState()
}

/**
 * High performance static card modifier. 3D Gyro sensor tracking disabled for 60fps smoothness.
 */
fun Modifier.interactive3DCardEffect(
    tiltState: TiltState,
    enableTouchDrag: Boolean = true,
    style: HoloFoilStyle = HoloFoilStyle.RAINBOW_SECRET,
    alphaMultiplier: Float = 1.0f,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
): Modifier = this

