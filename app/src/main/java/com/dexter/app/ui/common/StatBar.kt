package com.dexter.app.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Pentagon
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dexter.app.domain.model.PokemonStats
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.StatNumberStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class StatViewMode {
    RADAR, BARS
}

@Composable
fun PokemonStatRadarChart(
    stats: List<Int>,
    typeColor: Color,
    modifier: Modifier = Modifier,
    maxStat: Int = 255
) {
    val statNames = listOf("HP", "ATK", "DEF", "SP.ATK", "SP.DEF", "SPEED")
    val textMeasurer = rememberTextMeasurer()
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(stats) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = EaseOutCubic)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.15f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = (size.width.coerceAtMost(size.height) / 2f) * 0.68f
            val numAxes = 6

            // Angle for axis i (starting at top: -PI/2)
            fun getAngle(i: Int): Double = -PI / 2 + (i * 2 * PI / numAxes)

            // 1. Draw 4 concentric background grid polygons (25%, 50%, 75%, 100%)
            val gridLevels = listOf(0.25f, 0.50f, 0.75f, 1.00f)
            for (level in gridLevels) {
                val gridPath = Path()
                val radiusAtLevel = outerRadius * level
                for (i in 0 until numAxes) {
                    val angle = getAngle(i)
                    val x = center.x + (radiusAtLevel * cos(angle)).toFloat()
                    val y = center.y + (radiusAtLevel * sin(angle)).toFloat()
                    if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                }
                gridPath.close()
                drawPath(
                    path = gridPath,
                    color = outlineColor,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // 2. Draw radial axis lines from center to outer radius
            for (i in 0 until numAxes) {
                val angle = getAngle(i)
                val endX = center.x + (outerRadius * cos(angle)).toFloat()
                val endY = center.y + (outerRadius * sin(angle)).toFloat()
                drawLine(
                    color = outlineColor,
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 3. Draw stat polygon
            val progress = animProgress.value
            val statPath = Path()
            val statVertices = ArrayList<Offset>(numAxes)

            for (i in 0 until numAxes) {
                val statValue = stats.getOrElse(i) { 0 }
                val ratio = (statValue.toFloat() / maxStat).coerceIn(0f, 1f) * progress
                val radius = outerRadius * ratio
                val angle = getAngle(i)
                val x = center.x + (radius * cos(angle)).toFloat()
                val y = center.y + (radius * sin(angle)).toFloat()
                val vertex = Offset(x, y)
                statVertices.add(vertex)
                if (i == 0) statPath.moveTo(x, y) else statPath.lineTo(x, y)
            }
            statPath.close()

            // Fill with radial gradient brush
            drawPath(
                path = statPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        typeColor.copy(alpha = 0.55f),
                        typeColor.copy(alpha = 0.18f)
                    ),
                    center = center,
                    radius = outerRadius * 1.1f
                )
            )

            // Outline stroke
            drawPath(
                path = statPath,
                color = typeColor,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Vertex dots
            for (vertex in statVertices) {
                drawCircle(
                    color = typeColor,
                    radius = 4.dp.toPx(),
                    center = vertex
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 1.5.dp.toPx(),
                    center = vertex
                )
            }

            // 4. Draw axis text labels and stat values at vertex endpoints
            val labelRadius = outerRadius + 18.dp.toPx()
            val labelStyle = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = onSurfaceVariantColor
            )
            val valStyle = StatNumberStyle.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor
            )

            for (i in 0 until numAxes) {
                val angle = getAngle(i)
                val cosA = cos(angle)
                val sinA = sin(angle)

                val lx = center.x + (labelRadius * cosA).toFloat()
                val ly = center.y + (labelRadius * sinA).toFloat()

                val name = statNames.getOrElse(i) { "" }
                val rawVal = stats.getOrElse(i) { 0 }
                val currentVal = (rawVal * progress).toInt()

                val nameResult = textMeasurer.measure(name, style = labelStyle)
                val valResult = textMeasurer.measure(currentVal.toString(), style = valStyle)

                val totalWidth = nameResult.size.width.coerceAtLeast(valResult.size.width)
                val totalHeight = nameResult.size.height + valResult.size.height

                val tx = when {
                    cosA > 0.3 -> lx
                    cosA < -0.3 -> lx - totalWidth
                    else -> lx - totalWidth / 2f
                }

                val ty = when {
                    sinA > 0.3 -> ly
                    sinA < -0.3 -> ly - totalHeight
                    else -> ly - totalHeight / 2f
                }

                drawText(
                    textMeasurer = textMeasurer,
                    text = name,
                    topLeft = Offset(tx + (totalWidth - nameResult.size.width) / 2f, ty),
                    style = labelStyle
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = currentVal.toString(),
                    topLeft = Offset(tx + (totalWidth - valResult.size.width) / 2f, ty + nameResult.size.height),
                    style = valStyle
                )
            }
        }
    }
}

@Composable
fun PokemonStatRadarChart(
    stats: PokemonStats,
    typeColor: Color,
    modifier: Modifier = Modifier,
    maxStat: Int = 255
) {
    val statList = remember(stats) {
        listOf(
            stats.hp,
            stats.attack,
            stats.defense,
            stats.spAttack,
            stats.spDefense,
            stats.speed
        )
    }
    PokemonStatRadarChart(
        stats = statList,
        typeColor = typeColor,
        modifier = modifier,
        maxStat = maxStat
    )
}

@Composable
fun StatBar(
    label: String,
    value: Int,
    maxStat: Int = 255,
    barColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    var animateStart by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateStart = true }

    val progress by animateFloatAsState(
        targetValue = if (animateStart) (value.toFloat() / maxStat).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "stat_progress"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.Micro),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(Dimens.Major * 2.2f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.toString().padStart(3, ' '),
            modifier = Modifier.width(Dimens.Major * 1.3f),
            style = StatNumberStyle,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
        Box(
            modifier = Modifier
                .padding(start = Dimens.Tight)
                .weight(1f)
                .height(Dimens.Tight + Dimens.Micro / 2)
                .clip(RoundedCornerShape(Dimens.Micro))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(Dimens.Micro))
                    .background(barColor)
            )
        }
    }
}

@Composable
fun PokemonStatsSection(
    stats: PokemonStats,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    initialViewMode: StatViewMode = StatViewMode.RADAR
) {
    var viewMode by rememberSaveable { mutableStateOf(initialViewMode) }
    val hapticUtils = rememberHapticUtils()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.Micro)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BASE STATS",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Segment View Toggle (Radar vs Bars)
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = Dimens.Micro)
            ) {
                Row(
                    modifier = Modifier.padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val radarSelected = viewMode == StatViewMode.RADAR
                    val barSelected = viewMode == StatViewMode.BARS

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (radarSelected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .clickable {
                                if (!radarSelected) {
                                    hapticUtils.selectionTick()
                                    viewMode = StatViewMode.RADAR
                                }
                            }
                            .padding(horizontal = Dimens.Tight, vertical = Dimens.Micro / 2),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pentagon,
                                contentDescription = "Radar View",
                                modifier = Modifier.size(14.dp),
                                tint = if (radarSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Radar",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (radarSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (barSelected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .clickable {
                                if (!barSelected) {
                                    hapticUtils.selectionTick()
                                    viewMode = StatViewMode.BARS
                                }
                            }
                            .padding(horizontal = Dimens.Tight, vertical = Dimens.Micro / 2),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Bars View",
                                modifier = Modifier.size(14.dp),
                                tint = if (barSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Bars",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (barSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        AnimatedContent(
            targetState = viewMode,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "StatViewModeTransition"
        ) { mode ->
            when (mode) {
                StatViewMode.RADAR -> {
                    PokemonStatRadarChart(
                        stats = stats,
                        typeColor = accentColor,
                        modifier = Modifier.padding(vertical = Dimens.Micro)
                    )
                }
                StatViewMode.BARS -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimens.Micro)
                    ) {
                        StatBar(label = "HP", value = stats.hp, barColor = accentColor)
                        StatBar(label = "ATK", value = stats.attack, barColor = accentColor)
                        StatBar(label = "DEF", value = stats.defense, barColor = accentColor)
                        StatBar(label = "SP.ATK", value = stats.spAttack, barColor = accentColor)
                        StatBar(label = "SP.DEF", value = stats.spDefense, barColor = accentColor)
                        StatBar(label = "SPEED", value = stats.speed, barColor = accentColor)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.Tight),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TOTAL",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stats.total.toString(),
                style = StatNumberStyle.copy(fontFamily = FontFamily.Monospace),
                color = accentColor
            )
        }
    }
}
