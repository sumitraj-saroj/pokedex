package com.dexter.app.ui.region

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import com.dexter.app.domain.model.region.LocationType
import com.dexter.app.domain.model.region.MapStyle
import com.dexter.app.domain.model.region.Region
import com.dexter.app.domain.model.region.RegionLocation
import com.dexter.app.ui.common.rememberHapticUtils

@Composable
fun RegionMapVisualizer(
    region: Region,
    selectedLocationId: String?,
    mapStyle: MapStyle,
    onLocationSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberHapticUtils()
    val infiniteTransition = rememberInfiniteTransition(label = "gba_map_animations")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val cursorBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_bounce"
    )

    val cardBorderColor = when (mapStyle) {
        MapStyle.PIXEL_RETRO -> Color(0xFFC09848) // Retro GBA golden cart border
        MapStyle.ILLUSTRATED_ART -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        MapStyle.TACTICAL_RADAR -> Color(0xFF00E5FF).copy(alpha = 0.5f)
    }

    val cardBgColor = when (mapStyle) {
        MapStyle.PIXEL_RETRO -> Color(0xFFC5D2F8) // Authentic GBA Periwinkle Ocean
        MapStyle.ILLUSTRATED_ART -> MaterialTheme.colorScheme.surfaceContainerHigh
        MapStyle.TACTICAL_RADAR -> Color(0xFF0D1B2A)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(2.5.dp, cardBorderColor),
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

                when (mapStyle) {
                    MapStyle.PIXEL_RETRO -> {
                        // 🕹️ AUTHENTIC GBA TOWN MAP RENDERER
                        drawGbaOceanScanlines(canvasWidth, canvasHeight)
                        drawGbaLandmass(canvasWidth, canvasHeight)
                        drawGbaRoutesAndBridges(canvasWidth, canvasHeight, locationMap)
                        drawGbaNodes(
                            locations = region.locations,
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight,
                            selectedLocationId = selectedLocationId,
                            pulseScale = pulseScale,
                            pulseAlpha = pulseAlpha,
                            cursorBounce = cursorBounce
                        )
                        drawGbaTownMapBadge(canvasWidth, canvasHeight)
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
// 🎮 EXACT GBA FIRE-RED / LEAF-GREEN TOWN MAP RENDERING ENGINE
// -------------------------------------------------------------

private fun DrawScope.drawGbaOceanScanlines(width: Float, height: Float) {
    // 1. Periwinkle base
    drawRect(color = Color(0xFFC5D2F8))

    // 2. Subtle horizontal scanline stripes across the sea
    val scanlineColor = Color(0xFFB4C2F0)
    val step = 3.5.dp.toPx()
    var y = 0f
    while (y < height) {
        drawLine(
            color = scanlineColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.2.dp.toPx()
        )
        y += step * 2
    }
}

private fun DrawScope.drawGbaLandmass(width: Float, height: Float) {
    // GBA 3-tier elevation colors
    val baseGreen = Color(0xFF58B627) // Layer 1 (Darker Green Base)
    val midGreen = Color(0xFF78CF38)  // Layer 2 (Vibrant Green Middle)
    val peakGreen = Color(0xFF98E84A) // Layer 3 (Lime Yellow-Green High Elevation)
    val sandColor = Color(0xFFE8C676) // Sand / Pathway Foundation

    // --- MAIN CONTINENT LAYER 1: Base Green ---
    val basePath = Path().apply {
        moveTo(0.04f * width, 0.05f * height)
        lineTo(0.48f * width, 0.04f * height)
        cubicTo(0.68f * width, 0.06f * height, 0.72f * width, 0.16f * height, 0.88f * width, 0.22f * height)
        cubicTo(0.92f * width, 0.35f * height, 0.90f * width, 0.55f * height, 0.86f * width, 0.76f * height)
        cubicTo(0.82f * width, 0.86f * height, 0.65f * width, 0.90f * height, 0.52f * width, 0.88f * height)
        lineTo(0.50f * width, 0.82f * height)
        cubicTo(0.58f * width, 0.70f * height, 0.62f * width, 0.52f * height, 0.50f * width, 0.52f * height)
        cubicTo(0.38f * width, 0.52f * height, 0.34f * width, 0.68f * height, 0.28f * width, 0.78f * height)
        cubicTo(0.24f * width, 0.84f * height, 0.14f * width, 0.84f * height, 0.12f * width, 0.70f * height)
        lineTo(0.04f * width, 0.60f * height)
        close()
    }
    drawPath(basePath, baseGreen)

    // --- MAIN CONTINENT LAYER 2: Middle Elevation ---
    val midPath = Path().apply {
        moveTo(0.06f * width, 0.08f * height)
        lineTo(0.42f * width, 0.07f * height)
        cubicTo(0.58f * width, 0.10f * height, 0.64f * width, 0.18f * height, 0.78f * width, 0.24f * height)
        cubicTo(0.82f * width, 0.36f * height, 0.80f * width, 0.52f * height, 0.76f * width, 0.70f * height)
        lineTo(0.60f * width, 0.76f * height)
        cubicTo(0.52f * width, 0.64f * height, 0.52f * width, 0.44f * height, 0.42f * width, 0.44f * height)
        cubicTo(0.32f * width, 0.44f * height, 0.26f * width, 0.56f * height, 0.22f * width, 0.66f * height)
        lineTo(0.14f * width, 0.62f * height)
        lineTo(0.06f * width, 0.45f * height)
        close()
    }
    drawPath(midPath, midGreen)

    // --- MAIN CONTINENT LAYER 3: Mountain Heights / Plateau (Top Left) ---
    val peakPath = Path().apply {
        moveTo(0.08f * width, 0.12f * height)
        cubicTo(0.18f * width, 0.12f * height, 0.24f * width, 0.20f * height, 0.22f * width, 0.38f * height)
        cubicTo(0.18f * width, 0.46f * height, 0.10f * width, 0.44f * height, 0.08f * width, 0.32f * height)
        close()
    }
    drawPath(peakPath, peakGreen)

    // Concentric elevation rings at Mt. Moon / Indigo Plateau
    drawCircle(
        color = peakGreen,
        radius = 24.dp.toPx(),
        center = Offset(0.14f * width, 0.30f * height)
    )
    drawCircle(
        color = Color(0xFFAEF45A),
        radius = 14.dp.toPx(),
        center = Offset(0.14f * width, 0.30f * height)
    )

    // --- CINNABAR ISLAND (Bottom Left) ---
    drawRoundRect(
        color = baseGreen,
        topLeft = Offset(0.18f * width, 0.85f * height),
        size = Size(0.10f * width, 0.10f * height),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
    )
    drawRoundRect(
        color = midGreen,
        topLeft = Offset(0.20f * width, 0.87f * height),
        size = Size(0.06f * width, 0.06f * height),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )

    // --- ROUTE FOUNDATION SAND PADS ---
    val routeThickness = 16.dp.toPx()

    // Route 1-2 Vertical Sand
    drawRect(
        color = sandColor,
        topLeft = Offset(0.22f * width - routeThickness / 2 - 2.dp.toPx(), 0.35f * height),
        size = Size(routeThickness + 4.dp.toPx(), 0.40f * height)
    )
    // Route 3-4 Horizontal Sand (Pewter -> Cerulean)
    drawRect(
        color = sandColor,
        topLeft = Offset(0.22f * width, 0.30f * height - routeThickness / 2 - 2.dp.toPx()),
        size = Size(0.44f * width, routeThickness + 4.dp.toPx())
    )
    // Route 5-6 Vertical Sand (Cerulean -> Saffron -> Vermilion)
    drawRect(
        color = sandColor,
        topLeft = Offset(0.65f * width - routeThickness / 2 - 2.dp.toPx(), 0.18f * height),
        size = Size(routeThickness + 4.dp.toPx(), 0.46f * height)
    )
    // Route 7-8 Horizontal Sand (Celadon -> Saffron -> Lavender)
    drawRect(
        color = sandColor,
        topLeft = Offset(0.50f * width, 0.46f * height - routeThickness / 2 - 2.dp.toPx()),
        size = Size(0.34f * width, routeThickness + 4.dp.toPx())
    )
    // Route 9-10 Sand (Cerulean East -> Lavender)
    drawRect(
        color = sandColor,
        topLeft = Offset(0.65f * width, 0.30f * height - routeThickness / 2 - 2.dp.toPx()),
        size = Size(0.18f * width, routeThickness + 4.dp.toPx())
    )
    drawRect(
        color = sandColor,
        topLeft = Offset(0.82f * width - routeThickness / 2 - 2.dp.toPx(), 0.30f * height),
        size = Size(routeThickness + 4.dp.toPx(), 0.45f * height)
    )
    // Route 15 Sand (Fuchsia East)
    drawRect(
        color = sandColor,
        topLeft = Offset(0.56f * width, 0.80f * height - routeThickness / 2 - 2.dp.toPx()),
        size = Size(0.27f * width, routeThickness + 4.dp.toPx())
    )
}

private fun DrawScope.drawGbaRoutesAndBridges(
    width: Float,
    height: Float,
    locationMap: Map<String, RegionLocation>
) {
    val routeThickness = 14.dp.toPx()
    val orangeRoute = Color(0xFFE59E10)
    val orangeRouteBorder = Color(0xFFC77F08)
    val seaRouteColor = Color(0xFF7EAEE0)
    val bridgeWoodColor = Color(0xFFDCD0A8)
    val bridgeWoodBorder = Color(0xFF9E8D62)

    // 1. Sea Routes (Fuchsia -> Seafoam -> Cinnabar -> Pallet)
    // Horizontal Sea Route 20 (Fuchsia to Cinnabar)
    drawRect(
        color = seaRouteColor,
        topLeft = Offset(0.22f * width, 0.90f * height - routeThickness / 2),
        size = Size(0.34f * width, routeThickness)
    )
    // Vertical Sea Route 19 (Fuchsia South)
    drawRect(
        color = seaRouteColor,
        topLeft = Offset(0.56f * width - routeThickness / 2, 0.80f * height),
        size = Size(routeThickness, 0.10f * height)
    )
    // Vertical Sea Route 21 (Cinnabar to Pallet)
    drawRect(
        color = seaRouteColor,
        topLeft = Offset(0.22f * width - routeThickness / 2, 0.74f * height),
        size = Size(routeThickness, 0.16f * height)
    )

    // 2. Cycling Road (Route 16, 17, 18 Bridge: Celadon -> Bay -> Fuchsia)
    // Top Bridge arm (Celadon West)
    drawRect(
        color = bridgeWoodBorder,
        topLeft = Offset(0.34f * width, 0.46f * height - routeThickness / 2 - 1.dp.toPx()),
        size = Size(0.18f * width, routeThickness + 2.dp.toPx())
    )
    drawRect(
        color = bridgeWoodColor,
        topLeft = Offset(0.34f * width, 0.46f * height - routeThickness / 2),
        size = Size(0.18f * width, routeThickness)
    )
    // Long Vertical Bridge Over Bay
    drawRect(
        color = bridgeWoodBorder,
        topLeft = Offset(0.34f * width - routeThickness / 2 - 1.dp.toPx(), 0.46f * height),
        size = Size(routeThickness + 2.dp.toPx(), 0.34f * height)
    )
    drawRect(
        color = bridgeWoodColor,
        topLeft = Offset(0.34f * width - routeThickness / 2, 0.46f * height),
        size = Size(routeThickness, 0.34f * height)
    )
    // Bottom Bridge arm (Fuchsia West)
    drawRect(
        color = bridgeWoodBorder,
        topLeft = Offset(0.34f * width, 0.80f * height - routeThickness / 2 - 1.dp.toPx()),
        size = Size(0.22f * width, routeThickness + 2.dp.toPx())
    )
    drawRect(
        color = bridgeWoodColor,
        topLeft = Offset(0.34f * width, 0.80f * height - routeThickness / 2),
        size = Size(0.22f * width, routeThickness)
    )

    // Draw horizontal slats on the wooden bridge
    var by = 0.46f * height
    while (by <= 0.80f * height) {
        drawLine(
            color = bridgeWoodBorder,
            start = Offset(0.34f * width - routeThickness / 2, by),
            end = Offset(0.34f * width + routeThickness / 2, by),
            strokeWidth = 1.dp.toPx()
        )
        by += 4.dp.toPx()
    }

    // 3. Iconic Orange Land Routes
    // Route 1 (Pallet -> Viridian)
    drawGbaOrangeSegment(0.22f * width, 0.74f * height, 0.22f * width, 0.58f * height, routeThickness, orangeRoute, orangeRouteBorder)
    // Route 2 (Viridian -> Pewter)
    drawGbaOrangeSegment(0.22f * width, 0.58f * height, 0.22f * width, 0.35f * height, routeThickness, orangeRoute, orangeRouteBorder)
    // Route 22 & Victory Road (Viridian -> Indigo Plateau)
    drawGbaOrangeSegment(0.22f * width, 0.58f * height, 0.14f * width, 0.58f * height, routeThickness, orangeRoute, orangeRouteBorder)
    drawGbaOrangeSegment(0.14f * width, 0.58f * height, 0.14f * width, 0.30f * height, routeThickness, orangeRoute, orangeRouteBorder)
    // Route 3 & 4 (Pewter -> Cerulean)
    drawGbaOrangeSegment(0.22f * width, 0.30f * height, 0.65f * width, 0.30f * height, routeThickness, orangeRoute, orangeRouteBorder)
    // Route 24 & 25 (Cerulean North & Bill's Cottage)
    drawGbaOrangeSegment(0.65f * width, 0.30f * height, 0.65f * width, 0.18f * height, routeThickness, orangeRoute, orangeRouteBorder)
    drawGbaOrangeSegment(0.65f * width, 0.18f * height, 0.76f * width, 0.18f * height, routeThickness, orangeRoute, orangeRouteBorder)
    // Route 5 & 6 (Cerulean -> Saffron -> Vermilion)
    drawGbaOrangeSegment(0.65f * width, 0.30f * height, 0.65f * width, 0.63f * height, routeThickness, orangeRoute, orangeRouteBorder)
    // Route 7 & 8 (Celadon -> Saffron -> Lavender)
    drawGbaOrangeSegment(0.52f * width, 0.46f * height, 0.82f * width, 0.46f * height, routeThickness, orangeRoute, orangeRouteBorder)
    // Route 9 & 10 (Cerulean -> Power Plant -> Lavender)
    drawGbaOrangeSegment(0.65f * width, 0.30f * height, 0.82f * width, 0.30f * height, routeThickness, orangeRoute, orangeRouteBorder)
    drawGbaOrangeSegment(0.82f * width, 0.30f * height, 0.82f * width, 0.46f * height, routeThickness, orangeRoute, orangeRouteBorder)
    // Route 11 (Vermilion East)
    drawGbaOrangeSegment(0.65f * width, 0.63f * height, 0.82f * width, 0.63f * height, routeThickness, orangeRoute, orangeRouteBorder)
    // Route 12, 13, 14, 15 (Lavender -> Fuchsia)
    drawGbaOrangeSegment(0.82f * width, 0.46f * height, 0.82f * width, 0.72f * height, routeThickness, orangeRoute, orangeRouteBorder)
    drawGbaOrangeSegment(0.82f * width, 0.72f * height, 0.56f * width, 0.80f * height, routeThickness, orangeRoute, orangeRouteBorder)
    drawGbaOrangeSegment(0.56f * width, 0.80f * height, 0.56f * width, 0.88f * height, routeThickness, orangeRoute, orangeRouteBorder)

    // Intermediate Route Rivets / Dots
    drawGbaRouteDot(0.14f * width, 0.45f * height)
    drawGbaRouteDot(0.22f * width, 0.46f * height)
    drawGbaRouteDot(0.76f * width, 0.18f * height)
    drawGbaRouteDot(0.82f * width, 0.36f * height)
    drawGbaRouteDot(0.82f * width, 0.63f * height)
}

private fun DrawScope.drawGbaOrangeSegment(
    x1: Float, y1: Float, x2: Float, y2: Float,
    thickness: Float,
    fillColor: Color,
    borderColor: Color
) {
    // Border
    drawLine(
        color = borderColor,
        start = Offset(x1, y1),
        end = Offset(x2, y2),
        strokeWidth = thickness + 2.dp.toPx()
    )
    // Fill
    drawLine(
        color = fillColor,
        start = Offset(x1, y1),
        end = Offset(x2, y2),
        strokeWidth = thickness
    )
}

private fun DrawScope.drawGbaRouteDot(x: Float, y: Float) {
    drawCircle(
        color = Color(0xFF333333),
        radius = 3.5.dp.toPx(),
        center = Offset(x, y)
    )
    drawCircle(
        color = Color(0xFFE0E0E0),
        radius = 2.5.dp.toPx(),
        center = Offset(x, y)
    )
}

private fun DrawScope.drawGbaNodes(
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

        when (location.type) {
            LocationType.CAVE, LocationType.FOREST, LocationType.DUNGEON -> {
                // 🟦 BLUE SQUARE DUNGEON TILE
                val tileSize = 11.dp.toPx()

                if (isSelected) {
                    drawRect(
                        color = Color(0xFFFFD54F).copy(alpha = pulseAlpha),
                        topLeft = Offset(x - tileSize / 2 - 3.dp.toPx(), y - tileSize / 2 - 3.dp.toPx()),
                        size = Size(tileSize + 6.dp.toPx(), tileSize + 6.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // White outer rim
                drawRect(
                    color = Color.White,
                    topLeft = Offset(x - tileSize / 2 - 1.5.dp.toPx(), y - tileSize / 2 - 1.5.dp.toPx()),
                    size = Size(tileSize + 3.dp.toPx(), tileSize + 3.dp.toPx())
                )
                // Blue square center
                drawRect(
                    color = Color(0xFF4A90E2),
                    topLeft = Offset(x - tileSize / 2, y - tileSize / 2),
                    size = Size(tileSize, tileSize)
                )
                // Inner white cross dot
                drawRect(
                    color = Color.White,
                    topLeft = Offset(x - 1.5.dp.toPx(), y - 1.5.dp.toPx()),
                    size = Size(3.dp.toPx(), 3.dp.toPx())
                )
            }
            else -> {
                // 🔴 ICONIC RED 3D ORB IN SILVER FRAME (Towns, Cities, League, Legendaries)
                val frameSize = 16.dp.toPx()
                val orbRadius = 6.dp.toPx()

                // Pulsing highlight ring on active node
                if (isSelected) {
                    drawCircle(
                        color = Color(0xFFFFD54F).copy(alpha = pulseAlpha),
                        radius = (orbRadius + 6.dp.toPx()) * pulseScale,
                        center = Offset(x, y),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }

                // Outer square frame with silver metal corners
                drawRect(
                    color = Color(0xFF1E1E1E),
                    topLeft = Offset(x - frameSize / 2, y - frameSize / 2),
                    size = Size(frameSize, frameSize)
                )
                // 4 Silver Corner Brackets
                val cSize = 3.dp.toPx()
                val cornerColor = Color(0xFFEAEAEA)
                drawRect(cornerColor, Offset(x - frameSize / 2, y - frameSize / 2), Size(cSize, cSize))
                drawRect(cornerColor, Offset(x + frameSize / 2 - cSize, y - frameSize / 2), Size(cSize, cSize))
                drawRect(cornerColor, Offset(x - frameSize / 2, y + frameSize / 2 - cSize), Size(cSize, cSize))
                drawRect(cornerColor, Offset(x + frameSize / 2 - cSize, y + frameSize / 2 - cSize), Size(cSize, cSize))

                // 3D Glossy Red Marble Orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF5252), // Highlight
                            Color(0xFFD32F2F), // Mid tone
                            Color(0xFF6B0000)  // Deep shadow
                        ),
                        center = Offset(x - 2.dp.toPx(), y - 2.dp.toPx()),
                        radius = orbRadius
                    ),
                    radius = orbRadius,
                    center = Offset(x, y)
                )

                // White specular reflection dot (Top-Left)
                drawCircle(
                    color = Color.White,
                    radius = 1.8.dp.toPx(),
                    center = Offset(x - 2.2.dp.toPx(), y - 2.2.dp.toPx())
                )
            }
        }

        // 🎯 Bouncing Retro Player Cursor above the selected location
        if (isSelected) {
            val cursorY = y - 18.dp.toPx() + cursorBounce

            // Red pointer triangle
            val pointerPath = Path().apply {
                moveTo(x, cursorY + 6.dp.toPx())
                lineTo(x - 5.dp.toPx(), cursorY - 3.dp.toPx())
                lineTo(x + 5.dp.toPx(), cursorY - 3.dp.toPx())
                close()
            }
            drawPath(path = pointerPath, color = Color(0xFFE53935))
            drawPath(path = pointerPath, color = Color.White, style = Stroke(width = 1.2.dp.toPx()))

            // Retro Pokéball cursor head
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = Offset(x, cursorY - 8.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFE53935),
                radius = 5.dp.toPx(),
                center = Offset(x, cursorY - 8.dp.toPx())
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = Offset(x, cursorY - 8.dp.toPx())
            )
        }
    }
}

private fun DrawScope.drawGbaTownMapBadge(width: Float, height: Float) {
    // Bottom-Right GBA Map Icon (as seen in user's image)
    val bx = width - 28.dp.toPx()
    val by = height - 28.dp.toPx()
    val bSize = 20.dp.toPx()

    drawRoundRect(
        color = Color(0xFF333333),
        topLeft = Offset(bx - 1.dp.toPx(), by - 1.dp.toPx()),
        size = Size(bSize + 2.dp.toPx(), bSize + 2.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFFE5C158),
        topLeft = Offset(bx, by),
        size = Size(bSize, bSize),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFFFFF8E1),
        topLeft = Offset(bx + 3.dp.toPx(), by + 3.dp.toPx()),
        size = Size(bSize - 6.dp.toPx(), bSize - 6.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    // Inner map arrow
    val arrowPath = Path().apply {
        moveTo(bx + 7.dp.toPx(), by + 6.dp.toPx())
        lineTo(bx + 14.dp.toPx(), by + 10.dp.toPx())
        lineTo(bx + 7.dp.toPx(), by + 14.dp.toPx())
        close()
    }
    drawPath(arrowPath, Color(0xFF555555))
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
