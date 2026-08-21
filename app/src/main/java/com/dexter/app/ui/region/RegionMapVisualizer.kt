package com.dexter.app.ui.region

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.dexter.app.domain.model.region.LocationType
import com.dexter.app.domain.model.region.Region
import com.dexter.app.domain.model.region.RegionLocation
import com.dexter.app.ui.common.rememberHapticUtils

@Composable
fun RegionMapVisualizer(
    region: Region,
    selectedLocationId: String?,
    onLocationSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberHapticUtils()

    val infiniteTransition = rememberInfiniteTransition(label = "map_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val locationMap = remember(region.locations) {
                region.locations.associateBy { it.id }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(region.locations) {
                        detectTapGestures { tapOffset ->
                            val width = size.width
                            val height = size.height

                            var closestLocation: RegionLocation? = null
                            var minDistance = Float.MAX_VALUE
                            val tapThreshold = 48.dp.toPx()

                            for (loc in region.locations) {
                                val nodeX = loc.normalizedX * width
                                val nodeY = loc.normalizedY * height
                                val dist = kotlin.math.hypot(tapOffset.x - nodeX, tapOffset.y - nodeY)
                                if (dist < tapThreshold && dist < minDistance) {
                                    minDistance = dist
                                    closestLocation = loc
                                }
                            }

                            closestLocation?.let {
                                haptics.selectionTick()
                                onLocationSelect(it.id)
                            }
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // 1. Draw subtle background topographic grid
                drawMapGrid(canvasWidth, canvasHeight)

                // 2. Draw road / route connections between locations
                val drawnConnections = mutableSetOf<Pair<String, String>>()
                for (location in region.locations) {
                    val startX = location.normalizedX * canvasWidth
                    val startY = location.normalizedY * canvasHeight

                    for (targetId in location.connectedToIds) {
                        val connectionKey = if (location.id < targetId) location.id to targetId else targetId to location.id
                        if (drawnConnections.add(connectionKey)) {
                            val targetLocation = locationMap[targetId]
                            if (targetLocation != null) {
                                val endX = targetLocation.normalizedX * canvasWidth
                                val endY = targetLocation.normalizedY * canvasHeight

                                val isConnectedToSelected = location.id == selectedLocationId || targetId == selectedLocationId
                                drawRouteConnection(
                                    startX = startX,
                                    startY = startY,
                                    endX = endX,
                                    endY = endY,
                                    isHighlighted = isConnectedToSelected
                                )
                            }
                        }
                    }
                }

                // 3. Draw location nodes & badges
                for (location in region.locations) {
                    val nodeX = location.normalizedX * canvasWidth
                    val nodeY = location.normalizedY * canvasHeight
                    val isSelected = location.id == selectedLocationId

                    drawLocationNode(
                        location = location,
                        centerX = nodeX,
                        centerY = nodeY,
                        isSelected = isSelected,
                        pulseScale = pulseScale,
                        pulseAlpha = pulseAlpha
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawMapGrid(width: Float, height: Float) {
    val gridColor = Color.White.copy(alpha = 0.04f)
    val step = 32.dp.toPx()

    var x = 0f
    while (x <= width) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1.dp.toPx()
        )
        x += step
    }

    var y = 0f
    while (y <= height) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.dp.toPx()
        )
        y += step
    }
}

private fun DrawScope.drawRouteConnection(
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    isHighlighted: Boolean
) {
    val path = Path().apply {
        moveTo(startX, startY)
        val midX = (startX + endX) / 2f
        val midY = (startY + endY) / 2f
        quadraticTo(midX, midY, endX, endY)
    }

    val color = if (isHighlighted) {
        Color(0xFFFFD54F)
    } else {
        Color.White.copy(alpha = 0.22f)
    }

    val strokeWidth = if (isHighlighted) 3.5.dp.toPx() else 2.dp.toPx()

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        )
    )
}

private fun DrawScope.drawLocationNode(
    location: RegionLocation,
    centerX: Float,
    centerY: Float,
    isSelected: Boolean,
    pulseScale: Float,
    pulseAlpha: Float
) {
    val (nodeColor, nodeRadius) = when (location.type) {
        LocationType.LEGENDARY_LAIR -> Color(0xFFFFB300) to 14.dp.toPx()
        LocationType.POKEMON_LEAGUE -> Color(0xFFE53935) to 14.dp.toPx()
        LocationType.CITY -> Color(0xFF42A5F5) to 12.dp.toPx()
        LocationType.TOWN -> Color(0xFF66BB6A) to 10.dp.toPx()
        LocationType.CAVE, LocationType.MOUNTAIN -> Color(0xFF8D6E63) to 11.dp.toPx()
        LocationType.FOREST -> Color(0xFF2E7D32) to 10.dp.toPx()
        LocationType.DUNGEON -> Color(0xFFAB47BC) to 11.dp.toPx()
        LocationType.ROUTE, LocationType.SEA_ROUTE -> Color(0xFF26A69A) to 9.dp.toPx()
    }

    // Outer pulsating ring for selected or legendary/gym nodes
    if (isSelected || location.type == LocationType.LEGENDARY_LAIR || location.gymLeader != null) {
        drawCircle(
            color = if (isSelected) Color(0xFFFFD54F).copy(alpha = pulseAlpha) else nodeColor.copy(alpha = pulseAlpha * 0.7f),
            radius = nodeRadius * (if (isSelected) pulseScale * 1.25f else pulseScale),
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx())
        )
    }

    // Node core circle
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(nodeColor.copy(alpha = 0.9f), nodeColor),
            center = Offset(centerX, centerY),
            radius = nodeRadius
        ),
        radius = if (isSelected) nodeRadius * 1.2f else nodeRadius,
        center = Offset(centerX, centerY)
    )

    // Inner highlight / border
    drawCircle(
        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
        radius = if (isSelected) nodeRadius * 1.2f else nodeRadius,
        center = Offset(centerX, centerY),
        style = Stroke(width = if (isSelected) 2.5.dp.toPx() else 1.5.dp.toPx())
    )

    // Draw Location Name text label below node using native canvas
    val paint = android.graphics.Paint().apply {
        color = if (isSelected) android.graphics.Color.WHITE else android.graphics.Color.LTGRAY
        textSize = if (isSelected) 26f else 22f
        isFakeBoldText = isSelected
        textAlign = android.graphics.Paint.Align.CENTER
        setShadowLayer(4f, 0f, 2f, android.graphics.Color.BLACK)
    }

    drawContext.canvas.nativeCanvas.drawText(
        location.name,
        centerX,
        centerY + nodeRadius + 18.dp.toPx(),
        paint
    )
}
