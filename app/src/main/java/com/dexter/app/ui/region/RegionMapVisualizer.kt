package com.dexter.app.ui.region

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dexter.app.domain.model.region.LocationType
import com.dexter.app.domain.model.region.MapStyle
import com.dexter.app.domain.model.region.Region
import com.dexter.app.domain.model.region.RegionLocation
import com.dexter.app.ui.common.rememberHapticUtils
import kotlin.math.sin

@Composable
fun RegionMapVisualizer(
    region: Region,
    selectedLocationId: String?,
    mapStyle: MapStyle,
    onLocationSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberHapticUtils()

    val infiniteTransition = rememberInfiniteTransition(label = "map_animations")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val cursorBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_bounce"
    )

    val cardBorderColor = when (mapStyle) {
        MapStyle.PIXEL_RETRO -> Color(0xFF8D6E63)
        MapStyle.ILLUSTRATED_ART -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        MapStyle.TACTICAL_RADAR -> Color(0xFF00E5FF).copy(alpha = 0.5f)
    }

    val cardBgColor = when (mapStyle) {
        MapStyle.PIXEL_RETRO -> Color(0xFF2E68AA) // Deep retro ocean blue
        MapStyle.ILLUSTRATED_ART -> MaterialTheme.colorScheme.surfaceContainerHigh
        MapStyle.TACTICAL_RADAR -> Color(0xFF0D1B2A)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(310.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(2.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp))) {
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
                            val tapThreshold = 52.dp.toPx()

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

                when (mapStyle) {
                    MapStyle.PIXEL_RETRO -> {
                        drawPixelMapBackground(canvasWidth, canvasHeight, region)
                        drawPixelRoutes(canvasWidth, canvasHeight, region, locationMap, selectedLocationId)
                        drawPixelLocationNodes(
                            locations = region.locations,
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight,
                            selectedLocationId = selectedLocationId,
                            pulseScale = pulseScale,
                            pulseAlpha = pulseAlpha,
                            cursorBounce = cursorBounce
                        )
                    }
                    MapStyle.ILLUSTRATED_ART, MapStyle.TACTICAL_RADAR -> {
                        drawTacticalGrid(canvasWidth, canvasHeight)
                        drawTacticalRoutes(canvasWidth, canvasHeight, region, locationMap, selectedLocationId)
                        drawTacticalLocationNodes(
                            locations = region.locations,
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight,
                            selectedLocationId = selectedLocationId,
                            pulseScale = pulseScale,
                            pulseAlpha = pulseAlpha
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 🕹️ 16-BIT PIXEL TOWN MAP DRAWING FUNCTIONS
// -------------------------------------------------------------

private fun DrawScope.drawPixelMapBackground(width: Float, height: Float, region: Region) {
    // 1. Water base
    drawRect(color = Color(0xFF2E68AA))

    // 2. Pixel ocean wave ripples
    val waveColor = Color(0xFF4A90E2).copy(alpha = 0.45f)
    val tileSize = 20.dp.toPx()

    var y = 8.dp.toPx()
    while (y < height) {
        var x = 8.dp.toPx()
        while (x < width) {
            // Little 2x1 pixel wave marks
            drawRect(
                color = waveColor,
                topLeft = Offset(x, y),
                size = Size(8.dp.toPx(), 2.dp.toPx())
            )
            x += tileSize * 2
        }
        y += tileSize
    }

    // 3. Pixel Landmass Islands / Continent Body
    val landColor = Color(0xFF5CB85C) // Vibrant Town Map Grass
    val coastColor = Color(0xFFE6D59A) // Sand Coast
    val mountainColor = Color(0xFF8D6E63) // Rock Highlands
    val forestColor = Color(0xFF2E7D32) // Deep Canopy

    // Procedural Landmass Blobs connecting node clusters
    for (loc in region.locations) {
        val centerX = loc.normalizedX * width
        val centerY = loc.normalizedY * height
        val islandRadius = when (loc.type) {
            LocationType.CITY -> 34.dp.toPx()
            LocationType.TOWN -> 28.dp.toPx()
            LocationType.FOREST -> 32.dp.toPx()
            LocationType.MOUNTAIN, LocationType.CAVE -> 30.dp.toPx()
            LocationType.LEGENDARY_LAIR -> 32.dp.toPx()
            else -> 24.dp.toPx()
        }

        // Coast border
        drawRoundRect(
            color = coastColor,
            topLeft = Offset(centerX - islandRadius - 4.dp.toPx(), centerY - islandRadius - 4.dp.toPx()),
            size = Size((islandRadius + 4.dp.toPx()) * 2, (islandRadius + 4.dp.toPx()) * 2),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )

        // Land fill
        val biomeColor = when (loc.type) {
            LocationType.FOREST -> forestColor
            LocationType.MOUNTAIN, LocationType.CAVE -> mountainColor
            LocationType.LEGENDARY_LAIR -> Color(0xFF7E57C2)
            else -> landColor
        }

        drawRoundRect(
            color = biomeColor,
            topLeft = Offset(centerX - islandRadius, centerY - islandRadius),
            size = Size(islandRadius * 2, islandRadius * 2),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
        )
    }
}

private fun DrawScope.drawPixelRoutes(
    width: Float,
    height: Float,
    region: Region,
    locationMap: Map<String, RegionLocation>,
    selectedLocationId: String?
) {
    val drawn = mutableSetOf<Pair<String, String>>()
    val roadBorderColor = Color(0xFF4E342E)
    val roadFillColor = Color(0xFFE0C068) // 16-bit sand/brick path
    val activeRoadFillColor = Color(0xFFFFD54F)

    for (location in region.locations) {
        val startX = location.normalizedX * width
        val startY = location.normalizedY * height

        for (targetId in location.connectedToIds) {
            val key = if (location.id < targetId) location.id to targetId else targetId to location.id
            if (drawn.add(key)) {
                val target = locationMap[targetId] ?: continue
                val endX = target.normalizedX * width
                val endY = target.normalizedY * height
                val isHighlighted = location.id == selectedLocationId || targetId == selectedLocationId

                val strokeWidth = if (isHighlighted) 7.dp.toPx() else 5.dp.toPx()
                val innerWidth = if (isHighlighted) 4.dp.toPx() else 3.dp.toPx()

                // Outer border line
                drawLine(
                    color = roadBorderColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth
                )

                // Inner road fill
                drawLine(
                    color = if (isHighlighted) activeRoadFillColor else roadFillColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = innerWidth,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                )
            }
        }
    }
}

private fun DrawScope.drawPixelLocationNodes(
    locations: List<RegionLocation>,
    canvasWidth: Float,
    canvasHeight: Float,
    selectedLocationId: String?,
    pulseScale: Float,
    pulseAlpha: Float,
    cursorBounce: Float
) {
    for (location in locations) {
        val x = location.normalizedX * canvasWidth
        val y = location.normalizedY * canvasHeight
        val isSelected = location.id == selectedLocationId

        // Node Pixel Badge
        val (badgeColor, roofColor, sizeDp) = when (location.type) {
            LocationType.CITY -> Triple(Color(0xFF1976D2), Color(0xFFE53935), 18.dp) // Red Pokecenter roof
            LocationType.TOWN -> Triple(Color(0xFF388E3C), Color(0xFF43A047), 15.dp)
            LocationType.POKEMON_LEAGUE -> Triple(Color(0xFFD32F2F), Color(0xFFFFD54F), 22.dp)
            LocationType.LEGENDARY_LAIR -> Triple(Color(0xFFFFA000), Color(0xFFFFD54F), 20.dp)
            LocationType.CAVE, LocationType.MOUNTAIN -> Triple(Color(0xFF5D4037), Color(0xFF8D6E63), 16.dp)
            LocationType.FOREST -> Triple(Color(0xFF1B5E20), Color(0xFF2E7D32), 16.dp)
            LocationType.DUNGEON -> Triple(Color(0xFF7B1FA2), Color(0xFFBA68C8), 16.dp)
            else -> Triple(Color(0xFF00796B), Color(0xFF26A69A), 14.dp)
        }

        val pxSize = sizeDp.toPx()

        // Selected Pulsing Ring
        if (isSelected || location.type == LocationType.LEGENDARY_LAIR) {
            drawCircle(
                color = if (isSelected) Color(0xFFFFD54F).copy(alpha = pulseAlpha) else Color.White.copy(alpha = pulseAlpha * 0.7f),
                radius = (pxSize * 0.9f) * pulseScale,
                center = Offset(x, y),
                style = Stroke(width = 2.5.dp.toPx())
            )
        }

        // 16-Bit Town Map Building / Landmark Icon (Blocky Pixel Roof)
        drawRoundRect(
            color = Color.Black,
            topLeft = Offset(x - pxSize / 2 - 1.5.dp.toPx(), y - pxSize / 2 - 1.5.dp.toPx()),
            size = Size(pxSize + 3.dp.toPx(), pxSize + 3.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        drawRoundRect(
            color = badgeColor,
            topLeft = Offset(x - pxSize / 2, y - pxSize / 2),
            size = Size(pxSize, pxSize),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )

        // Roof highlight
        drawRect(
            color = roofColor,
            topLeft = Offset(x - pxSize / 2 + 2.dp.toPx(), y - pxSize / 2 + 2.dp.toPx()),
            size = Size(pxSize - 4.dp.toPx(), 5.dp.toPx())
        )

        // Center white Pokéball dot
        drawCircle(
            color = Color.White,
            radius = 2.5.dp.toPx(),
            center = Offset(x, y + 2.dp.toPx())
        )

        // Location Label (Crisp Pixel Font Style)
        val paint = android.graphics.Paint().apply {
            color = if (isSelected) android.graphics.Color.WHITE else android.graphics.Color.rgb(230, 240, 255)
            textSize = if (isSelected) 28f else 22f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
            setShadowLayer(5f, 0f, 2f, android.graphics.Color.BLACK)
        }

        drawContext.canvas.nativeCanvas.drawText(
            location.name,
            x,
            y + pxSize / 2 + 16.dp.toPx(),
            paint
        )

        // 🎯 Bouncing Retro Player Cursor above the selected location
        if (isSelected) {
            val cursorY = y - pxSize / 2 - 14.dp.toPx() + cursorBounce

            // Red pointer triangle
            val pointerPath = Path().apply {
                moveTo(x, cursorY + 6.dp.toPx())
                lineTo(x - 6.dp.toPx(), cursorY - 4.dp.toPx())
                lineTo(x + 6.dp.toPx(), cursorY - 4.dp.toPx())
                close()
            }
            drawPath(path = pointerPath, color = Color(0xFFE53935))
            drawPath(path = pointerPath, color = Color.White, style = Stroke(width = 1.5.dp.toPx()))

            // Retro Pokéball icon cursor head
            drawCircle(
                color = Color.White,
                radius = 7.dp.toPx(),
                center = Offset(x, cursorY - 10.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFE53935),
                radius = 6.dp.toPx(),
                center = Offset(x, cursorY - 10.dp.toPx())
            )
            drawCircle(
                color = Color.White,
                radius = 2.5.dp.toPx(),
                center = Offset(x, cursorY - 10.dp.toPx())
            )
        }
    }
}

// -------------------------------------------------------------
// 🌐 TACTICAL RADAR DRAWING FUNCTIONS
// -------------------------------------------------------------

private fun DrawScope.drawTacticalGrid(width: Float, height: Float) {
    val gridColor = Color(0xFF00E5FF).copy(alpha = 0.06f)
    val step = 30.dp.toPx()

    var x = 0f
    while (x <= width) {
        drawLine(color = gridColor, start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 1.dp.toPx())
        x += step
    }

    var y = 0f
    while (y <= height) {
        drawLine(color = gridColor, start = Offset(0f, y), end = Offset(width, y), strokeWidth = 1.dp.toPx())
        y += step
    }
}

private fun DrawScope.drawTacticalRoutes(
    width: Float,
    height: Float,
    region: Region,
    locationMap: Map<String, RegionLocation>,
    selectedLocationId: String?
) {
    val drawn = mutableSetOf<Pair<String, String>>()
    for (location in region.locations) {
        val startX = location.normalizedX * width
        val startY = location.normalizedY * height

        for (targetId in location.connectedToIds) {
            val key = if (location.id < targetId) location.id to targetId else targetId to location.id
            if (drawn.add(key)) {
                val target = locationMap[targetId] ?: continue
                val endX = target.normalizedX * width
                val endY = target.normalizedY * height
                val isHighlighted = location.id == selectedLocationId || targetId == selectedLocationId

                val path = Path().apply {
                    moveTo(startX, startY)
                    val midX = (startX + endX) / 2f
                    val midY = (startY + endY) / 2f
                    quadraticTo(midX, midY, endX, endY)
                }

                val color = if (isHighlighted) Color(0xFFFFD54F) else Color(0xFF00E5FF).copy(alpha = 0.35f)
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
        }
    }
}

private fun DrawScope.drawTacticalLocationNodes(
    locations: List<RegionLocation>,
    canvasWidth: Float,
    canvasHeight: Float,
    selectedLocationId: String?,
    pulseScale: Float,
    pulseAlpha: Float
) {
    for (location in locations) {
        val nodeX = location.normalizedX * canvasWidth
        val nodeY = location.normalizedY * canvasHeight
        val isSelected = location.id == selectedLocationId

        val (nodeColor, nodeRadius) = when (location.type) {
            LocationType.LEGENDARY_LAIR -> Color(0xFFFFB300) to 14.dp.toPx()
            LocationType.POKEMON_LEAGUE -> Color(0xFFE53935) to 14.dp.toPx()
            LocationType.CITY -> Color(0xFF42A5F5) to 12.dp.toPx()
            LocationType.TOWN -> Color(0xFF66BB6A) to 10.dp.toPx()
            LocationType.CAVE, LocationType.MOUNTAIN -> Color(0xFF8D6E63) to 11.dp.toPx()
            LocationType.FOREST -> Color(0xFF2E7D32) to 10.dp.toPx()
            LocationType.DUNGEON -> Color(0xFFAB47BC) to 11.dp.toPx()
            else -> Color(0xFF26A69A) to 9.dp.toPx()
        }

        if (isSelected || location.type == LocationType.LEGENDARY_LAIR) {
            drawCircle(
                color = if (isSelected) Color(0xFFFFD54F).copy(alpha = pulseAlpha) else nodeColor.copy(alpha = pulseAlpha * 0.7f),
                radius = nodeRadius * (if (isSelected) pulseScale * 1.3f else pulseScale),
                center = Offset(nodeX, nodeY),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(nodeColor.copy(alpha = 0.9f), nodeColor),
                center = Offset(nodeX, nodeY),
                radius = nodeRadius
            ),
            radius = if (isSelected) nodeRadius * 1.2f else nodeRadius,
            center = Offset(nodeX, nodeY)
        )

        drawCircle(
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            radius = if (isSelected) nodeRadius * 1.2f else nodeRadius,
            center = Offset(nodeX, nodeY),
            style = Stroke(width = if (isSelected) 2.5.dp.toPx() else 1.5.dp.toPx())
        )

        val paint = android.graphics.Paint().apply {
            color = if (isSelected) android.graphics.Color.WHITE else android.graphics.Color.LTGRAY
            textSize = if (isSelected) 26f else 22f
            isFakeBoldText = isSelected
            textAlign = android.graphics.Paint.Align.CENTER
            setShadowLayer(4f, 0f, 2f, android.graphics.Color.BLACK)
        }

        drawContext.canvas.nativeCanvas.drawText(
            location.name,
            nodeX,
            nodeY + nodeRadius + 18.dp.toPx(),
            paint
        )
    }
}
