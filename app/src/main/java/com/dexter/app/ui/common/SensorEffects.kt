package com.dexter.app.ui.common

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.PI

data class TiltState(
    val rotationX: Float,
    val rotationY: Float,
    val normX: Float,
    val normY: Float
)

@Composable
fun rememberTiltSensorState(
    maxRotationDegrees: Float = 15f
): TiltState {
    val context = LocalContext.current
    var rawPitch by remember { mutableFloatStateOf(0f) }
    var rawRoll by remember { mutableFloatStateOf(0f) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)

                // pitch is orientation[1], roll is orientation[2]
                val pitchDeg = (orientation[1] * (180f / PI.toFloat()))
                val rollDeg = (orientation[2] * (180f / PI.toFloat()))

                // Standard comfortable holding position is ~45deg pitch
                val adjustedPitch = (pitchDeg + 45f).coerceIn(-maxRotationDegrees, maxRotationDegrees)
                val adjustedRoll = rollDeg.coerceIn(-maxRotationDegrees, maxRotationDegrees)

                rawPitch = adjustedPitch
                rawRoll = adjustedRoll
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (rotationSensor != null) {
            sensorManager?.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    val animatedRotationX by animateFloatAsState(
        targetValue = -rawPitch,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f),
        label = "tilt_rotation_x"
    )

    val animatedRotationY by animateFloatAsState(
        targetValue = rawRoll,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f),
        label = "tilt_rotation_y"
    )

    val normX = (animatedRotationY / maxRotationDegrees).coerceIn(-1f, 1f)
    val normY = (animatedRotationX / maxRotationDegrees).coerceIn(-1f, 1f)

    return TiltState(
        rotationX = animatedRotationX,
        rotationY = animatedRotationY,
        normX = normX,
        normY = normY
    )
}

/**
 * Applies interactive 3D perspective rotation and dynamic gyro-driven holographic foil sheen.
 */
fun Modifier.interactive3DCardEffect(
    tiltState: TiltState,
    enableTouchDrag: Boolean = true
): Modifier = composed {
    val density = LocalDensity.current
    var touchDragX by remember { mutableFloatStateOf(0f) }
    var touchDragY by remember { mutableFloatStateOf(0f) }

    val combinedRotX = (tiltState.rotationX + touchDragY).coerceIn(-25f, 25f)
    val combinedRotY = (tiltState.rotationY + touchDragX).coerceIn(-25f, 25f)

    val normX = ((tiltState.normX + (touchDragX / 25f))).coerceIn(-1f, 1f)
    val normY = ((tiltState.normY + (touchDragY / 25f))).coerceIn(-1f, 1f)

    this
        .pointerInput(enableTouchDrag) {
            if (!enableTouchDrag) return@pointerInput
            detectDragGestures(
                onDragEnd = {
                    touchDragX = 0f
                    touchDragY = 0f
                },
                onDragCancel = {
                    touchDragX = 0f
                    touchDragY = 0f
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    touchDragX = (touchDragX + dragAmount.x * 0.15f).coerceIn(-20f, 20f)
                    touchDragY = (touchDragY - dragAmount.y * 0.15f).coerceIn(-20f, 20f)
                }
            )
        }
        .graphicsLayer {
            rotationX = combinedRotX
            rotationY = combinedRotY
            cameraDistance = 16f * density.density
        }
        .drawWithContent {
            drawContent()

            // Gyroscope/Tilt dynamic metallic rainbow foil sheen
            val width = size.width
            val height = size.height

            val sheenStartX = width * (0.5f + normX * 0.6f)
            val sheenStartY = height * (0.5f + normY * 0.6f)

            val holoSpectrum = listOf(
                Color(0x00FFFFFF),
                Color(0x55FFD700), // Gold
                Color(0x66FF007F), // Pink
                Color(0x6600E5FF), // Cyan
                Color(0x6676FF03), // Lime Green
                Color(0x55FFD700), // Gold
                Color(0x00FFFFFF)
            )

            drawRect(
                brush = Brush.linearGradient(
                    colors = holoSpectrum,
                    start = Offset(sheenStartX - width * 0.6f, sheenStartY - height * 0.6f),
                    end = Offset(sheenStartX + width * 0.6f, sheenStartY + height * 0.6f)
                ),
                blendMode = BlendMode.Screen
            )
        }
}
