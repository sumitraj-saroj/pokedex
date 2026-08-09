package com.dexter.app.ui.compare

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonStats
import com.dexter.app.ui.common.PokemonPickerBottomSheet
import com.dexter.app.ui.common.TypeChip
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.Hct
import com.dexter.app.ui.theme.StatNumberStyle
import com.dexter.app.ui.theme.blendTypeSeedColors
import com.dexter.app.ui.theme.hctToColor
import com.dexter.app.ui.theme.toHct
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

import com.dexter.app.ui.theme.LocalDarkTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CompareScreen(
    uiState: CompareUiState,
    onOpenPicker: (CompareTarget) -> Unit,
    onClosePicker: () -> Unit,
    onSelectPokemon: (Pokemon) -> Unit,
    onSwapPokemon: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticUtils = com.dexter.app.ui.common.rememberHapticUtils()
    val isDark = LocalDarkTheme.current
    val pkmnA = uiState.pokemonA
    val pkmnB = uiState.pokemonB

    // Split-tint background brush using dominant type seed colors from both Pokémon
    val colorA = remember(pkmnA, isDark) {
        if (pkmnA != null) {
            com.dexter.app.ui.theme.getOrGenerateTypeColorScheme(pkmnA.primaryType, pkmnA.secondaryType, isDark).primaryContainer.copy(alpha = if (isDark) 0.35f else 0.22f)
        } else {
            Color.Transparent
        }
    }

    val colorB = remember(pkmnB, isDark) {
        if (pkmnB != null) {
            com.dexter.app.ui.theme.getOrGenerateTypeColorScheme(pkmnB.primaryType, pkmnB.secondaryType, isDark).primaryContainer.copy(alpha = if (isDark) 0.35f else 0.22f)
        } else {
            Color.Transparent
        }
    }

    val splitBackgroundBrush = remember(colorA, colorB) {
        Brush.horizontalGradient(
            0.0f to colorA,
            0.42f to colorA,
            0.58f to colorB,
            1.0f to colorB
        )
    }

    val (compareColorA, compareColorB) = remember(pkmnA, pkmnB) {
        if (pkmnA != null && pkmnB != null) {
            getDistinctCompareColors(pkmnA, pkmnB)
        } else {
            Pair(Color.Unspecified, Color.Unspecified)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            com.dexter.app.ui.common.GlassmorphicTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Tight / 2)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Compare Mode",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(splitBackgroundBrush)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Dimens.ScreenEdgePadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.Section)
            ) {
                // 1. Selector Cards Header (Side-by-Side)
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.Compact)
                        ) {
                            CompareSelectionCard(
                                pokemon = pkmnA,
                                label = "Pokémon A",
                                onCardClick = {
                                    hapticUtils.selectionTick()
                                    onOpenPicker(CompareTarget.POKEMON_A)
                                },
                                onImageClick = { pkmnA?.let { onPokemonClick(it.id) } },
                                modifier = Modifier.weight(1f)
                            )

                            CompareSelectionCard(
                                pokemon = pkmnB,
                                label = "Pokémon B",
                                onCardClick = {
                                    hapticUtils.selectionTick()
                                    onOpenPicker(CompareTarget.POKEMON_B)
                                },
                                onImageClick = { pkmnB?.let { onPokemonClick(it.id) } },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable {
                                    hapticUtils.mediumImpact()
                                    onSwapPokemon()
                                },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            tonalElevation = Dimens.ElevationLevel3,
                            shadowElevation = Dimens.ElevationLevel2
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Swap Pokémon",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Dual Overlaid Stat Radar Chart
                if (pkmnA != null && pkmnB != null) {
                    item {
                        DualPokemonStatRadarCard(
                            pokemonA = pkmnA,
                            pokemonB = pkmnB,
                            colorA = compareColorA,
                            colorB = compareColorB
                        )
                    }

                    // 3. Base Stats Side-by-Side Comparison & Deltas
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Dimens.Section),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.ElevationLevel2)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.ScreenEdgePadding),
                                verticalArrangement = Arrangement.spacedBy(Dimens.Compact)
                            ) {
                                Text(
                                    text = "STAT COMPARISON & DELTAS",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                val statsA = pkmnA.stats ?: PokemonStats(0, 0, 0, 0, 0, 0)
                                val statsB = pkmnB.stats ?: PokemonStats(0, 0, 0, 0, 0, 0)

                                CompareStatRow(name = "HP", valA = statsA.hp, valB = statsB.hp, colorA = compareColorA, colorB = compareColorB)
                                CompareStatRow(name = "Attack", valA = statsA.attack, valB = statsB.attack, colorA = compareColorA, colorB = compareColorB)
                                CompareStatRow(name = "Defense", valA = statsA.defense, valB = statsB.defense, colorA = compareColorA, colorB = compareColorB)
                                CompareStatRow(name = "Sp. Atk", valA = statsA.spAttack, valB = statsB.spAttack, colorA = compareColorA, colorB = compareColorB)
                                CompareStatRow(name = "Sp. Def", valA = statsA.spDefense, valB = statsB.spDefense, colorA = compareColorA, colorB = compareColorB)
                                CompareStatRow(name = "Speed", valA = statsA.speed, valB = statsB.speed, colorA = compareColorA, colorB = compareColorB)
                                CompareStatRow(name = "Total", valA = statsA.total, valB = statsB.total, colorA = compareColorA, colorB = compareColorB, maxStat = 720)
                            }
                        }
                    }

                    // 4. Physical Attributes & Type Comparison Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Dimens.Section),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.ElevationLevel2)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.ScreenEdgePadding),
                                verticalArrangement = Arrangement.spacedBy(Dimens.Compact)
                            ) {
                                Text(
                                    text = "PHYSICAL PROFILE",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                AttributeCompareRow(
                                    label = "Height",
                                    valA = "${pkmnA.heightM} m",
                                    valB = "${pkmnB.heightM} m"
                                )
                                AttributeCompareRow(
                                    label = "Weight",
                                    valA = "${pkmnA.weightKg} kg",
                                    valB = "${pkmnB.weightKg} kg"
                                )
                                AttributeCompareRow(
                                    label = "Generation",
                                    valA = "Gen ${pkmnA.generation}",
                                    valB = "Gen ${pkmnB.generation}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.activePickerTarget != CompareTarget.NONE) {
        PokemonPickerBottomSheet(
            onDismissRequest = onClosePicker,
            onPokemonSelected = { pokemon ->
                hapticUtils.heavyImpact()
                onSelectPokemon(pokemon)
            },
            pokemonList = uiState.allPokemon,
            title = if (uiState.activePickerTarget == CompareTarget.POKEMON_A) "Select Pokémon A" else "Select Pokémon B"
        )
    }
}

private fun getDistinctCompareColors(pkmnA: Pokemon, pkmnB: Pokemon): Pair<Color, Color> {
    val rawA = blendTypeSeedColors(pkmnA.primaryType.seedColor, pkmnA.secondaryType?.seedColor)
    val rawB = blendTypeSeedColors(pkmnB.primaryType.seedColor, pkmnB.secondaryType?.seedColor)

    val diff = kotlin.math.abs(rawA.red - rawB.red) +
            kotlin.math.abs(rawA.green - rawB.green) +
            kotlin.math.abs(rawA.blue - rawB.blue)

    val finalB = if (diff < 0.15f) {
        val hctB = rawB.toHct()
        hctToColor(Hct((hctB.hue + 45f) % 360f, hctB.chroma.coerceAtLeast(0.4f), hctB.tone))
    } else {
        rawB
    }
    return Pair(rawA, finalB)
}

@Composable
private fun DualPokemonStatRadarCard(
    pokemonA: Pokemon,
    pokemonB: Pokemon,
    colorA: Color,
    colorB: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.Section),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.ElevationLevel2)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.ScreenEdgePadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.Compact)
        ) {
            Text(
                text = "STAT OVERLAY RADAR",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Legend Row with elegant pill chips for both Pokémon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pokémon A Legend Chip
                Surface(
                    shape = CircleShape,
                    color = colorA.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colorA.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colorA)
                        )
                        Text(
                            text = pokemonA.capitalizedName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorA,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = "VS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = Dimens.Micro)
                )

                // Pokémon B Legend Chip
                Surface(
                    shape = CircleShape,
                    color = colorB.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colorB.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colorB)
                        )
                        Text(
                            text = pokemonB.capitalizedName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorB,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            DualPokemonStatRadarChartCanvas(
                pokemonA = pokemonA,
                pokemonB = pokemonB,
                colorA = colorA,
                colorB = colorB,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.Micro)
            )
        }
    }
}

@Composable
fun DualPokemonStatRadarChartCanvas(
    pokemonA: Pokemon,
    pokemonB: Pokemon,
    colorA: Color,
    colorB: Color,
    modifier: Modifier = Modifier,
    maxStat: Int = 255
) {
    val statsA = remember(pokemonA) {
        val s = pokemonA.stats ?: PokemonStats(0, 0, 0, 0, 0, 0)
        listOf(s.hp, s.attack, s.defense, s.spAttack, s.spDefense, s.speed)
    }
    val statsB = remember(pokemonB) {
        val s = pokemonB.stats ?: PokemonStats(0, 0, 0, 0, 0, 0)
        listOf(s.hp, s.attack, s.defense, s.spAttack, s.spDefense, s.speed)
    }

    val statNames = listOf("HP", "ATK", "DEF", "SP.ATK", "SP.DEF", "SPEED")
    val textMeasurer = rememberTextMeasurer()
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(pokemonA.id, pokemonB.id) {
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
            val outerRadius = (size.width.coerceAtMost(size.height) / 2f) * 0.65f
            val numAxes = 6

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

            val progress = animProgress.value

            // 3. Draw Polygon A
            val statPathA = Path()
            val statVerticesA = ArrayList<Offset>(numAxes)
            for (i in 0 until numAxes) {
                val statValue = statsA.getOrElse(i) { 0 }
                val ratio = (statValue.toFloat() / maxStat).coerceIn(0f, 1f) * progress
                val radius = outerRadius * ratio
                val angle = getAngle(i)
                val x = center.x + (radius * cos(angle)).toFloat()
                val y = center.y + (radius * sin(angle)).toFloat()
                val vertex = Offset(x, y)
                statVerticesA.add(vertex)
                if (i == 0) statPathA.moveTo(x, y) else statPathA.lineTo(x, y)
            }
            statPathA.close()

            drawPath(
                path = statPathA,
                brush = Brush.radialGradient(
                    colors = listOf(
                        colorA.copy(alpha = 0.45f),
                        colorA.copy(alpha = 0.12f)
                    ),
                    center = center,
                    radius = outerRadius * 1.1f
                )
            )
            drawPath(
                path = statPathA,
                color = colorA,
                style = Stroke(width = 2.5.dp.toPx())
            )
            for (vertex in statVerticesA) {
                drawCircle(color = colorA, radius = 4.dp.toPx(), center = vertex)
                drawCircle(color = Color.White.copy(alpha = 0.9f), radius = 1.5.dp.toPx(), center = vertex)
            }

            // 4. Draw Polygon B
            val statPathB = Path()
            val statVerticesB = ArrayList<Offset>(numAxes)
            for (i in 0 until numAxes) {
                val statValue = statsB.getOrElse(i) { 0 }
                val ratio = (statValue.toFloat() / maxStat).coerceIn(0f, 1f) * progress
                val radius = outerRadius * ratio
                val angle = getAngle(i)
                val x = center.x + (radius * cos(angle)).toFloat()
                val y = center.y + (radius * sin(angle)).toFloat()
                val vertex = Offset(x, y)
                statVerticesB.add(vertex)
                if (i == 0) statPathB.moveTo(x, y) else statPathB.lineTo(x, y)
            }
            statPathB.close()

            drawPath(
                path = statPathB,
                brush = Brush.radialGradient(
                    colors = listOf(
                        colorB.copy(alpha = 0.45f),
                        colorB.copy(alpha = 0.12f)
                    ),
                    center = center,
                    radius = outerRadius * 1.1f
                )
            )
            drawPath(
                path = statPathB,
                color = colorB,
                style = Stroke(width = 2.5.dp.toPx())
            )
            for (vertex in statVerticesB) {
                drawCircle(color = colorB, radius = 4.dp.toPx(), center = vertex)
                drawCircle(color = Color.White.copy(alpha = 0.9f), radius = 1.5.dp.toPx(), center = vertex)
            }

            // 5. Draw Axis text labels and stat values at vertex endpoints
            val labelRadius = outerRadius + 22.dp.toPx()
            val labelStyle = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = onSurfaceVariantColor
            )
            val valStyleA = StatNumberStyle.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorA
            )
            val valStyleB = StatNumberStyle.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorB
            )
            val slashStyle = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = onSurfaceVariantColor
            )

            for (i in 0 until numAxes) {
                val angle = getAngle(i)
                val cosA = cos(angle)
                val sinA = sin(angle)

                val lx = center.x + (labelRadius * cosA).toFloat()
                val ly = center.y + (labelRadius * sinA).toFloat()

                val name = statNames.getOrElse(i) { "" }
                val rawValA = statsA.getOrElse(i) { 0 }
                val rawValB = statsB.getOrElse(i) { 0 }
                val currentValA = (rawValA * progress).toInt()
                val currentValB = (rawValB * progress).toInt()

                val nameResult = textMeasurer.measure(name, style = labelStyle)
                val valAResult = textMeasurer.measure(currentValA.toString(), style = valStyleA)
                val slashResult = textMeasurer.measure(" / ", style = slashStyle)
                val valBResult = textMeasurer.measure(currentValB.toString(), style = valStyleB)

                val line2Width = valAResult.size.width + slashResult.size.width + valBResult.size.width
                val line2Height = maxOf(valAResult.size.height, valBResult.size.height)
                val totalWidth = maxOf(nameResult.size.width, line2Width)
                val totalHeight = nameResult.size.height + line2Height

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

                // Line 1: Name
                drawText(
                    textMeasurer = textMeasurer,
                    text = name,
                    topLeft = Offset(tx + (totalWidth - nameResult.size.width) / 2f, ty),
                    style = labelStyle
                )

                // Line 2: Values (ValA / ValB)
                val line2Top = ty + nameResult.size.height
                val line2Left = tx + (totalWidth - line2Width) / 2f

                drawText(
                    textMeasurer = textMeasurer,
                    text = currentValA.toString(),
                    topLeft = Offset(line2Left, line2Top),
                    style = valStyleA
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = " / ",
                    topLeft = Offset(line2Left + valAResult.size.width, line2Top),
                    style = slashStyle
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = currentValB.toString(),
                    topLeft = Offset(line2Left + valAResult.size.width + slashResult.size.width, line2Top),
                    style = valStyleB
                )
            }
        }
    }
}

@Composable
private fun CompareSelectionCard(
    pokemon: Pokemon?,
    label: String,
    onCardClick: () -> Unit,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(Dimens.Section),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.ElevationLevel2)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Tight),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Dimens.Micro))

            if (pokemon != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.1f)
                        .clip(RoundedCornerShape(Dimens.Compact))
                        .background(pokemon.primaryType.seedColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(pokemon.officialArtworkUrl ?: pokemon.spriteUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = pokemon.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimens.Tight)
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Tight))

                Text(
                    text = pokemon.formattedNumber,
                    style = StatNumberStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Text(
                    text = pokemon.capitalizedName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(Dimens.Micro))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Micro),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TypeChip(type = pokemon.primaryType, isCompact = true)
                    pokemon.secondaryType?.let {
                        TypeChip(type = it, isCompact = true)
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Tight))

                // Tap hint pill
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Change",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.1f)
                        .clip(RoundedCornerShape(Dimens.Compact))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tap to Pick",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatDeltaChip(
    delta: Int,
    modifier: Modifier = Modifier
) {
    val containerColor: Color
    val textColor: Color
    val textStr: String

    when {
        delta > 0 -> {
            containerColor = Color(0xFF2E7D32).copy(alpha = 0.2f)
            textColor = Color(0xFF4CAF50)
            textStr = "+$delta"
        }
        delta < 0 -> {
            containerColor = Color(0xFFC62828).copy(alpha = 0.2f)
            textColor = Color(0xFFEF5350)
            textStr = "$delta"
        }
        else -> {
            containerColor = MaterialTheme.colorScheme.surfaceVariant
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
            textStr = "="
        }
    }

    Surface(
        shape = CircleShape,
        color = containerColor,
        modifier = modifier
    ) {
        Text(
            text = textStr,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun CompareStatRow(
    name: String,
    valA: Int,
    valB: Int,
    colorA: Color,
    colorB: Color,
    maxStat: Int = 255
) {
    val deltaA = valA - valB
    val deltaB = valB - valA

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.Micro / 2)
    ) {
        // Title and Values Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left (A) Value & Delta Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.Micro)
            ) {
                Text(
                    text = valA.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (deltaA > 0) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (deltaA > 0) colorA else MaterialTheme.colorScheme.onSurface
                )

                StatDeltaChip(delta = deltaA)
            }

            // Stat Name in Center
            Text(
                text = name.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Right (B) Value & Delta Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.Micro)
            ) {
                StatDeltaChip(delta = deltaB)

                Text(
                    text = valB.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (deltaB > 0) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (deltaB > 0) colorB else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Side-by-Side Dual Stat Bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Micro)
        ) {
            // Left Stat Bar (A) - fills right-to-left
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = (valA.toFloat() / maxStat).coerceIn(0.05f, 1.0f))
                        .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                        .background(
                            if (deltaA > 0) colorA else colorA.copy(alpha = 0.45f)
                        )
                )
            }

            // Right Stat Bar (B) - fills left-to-right
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = (valB.toFloat() / maxStat).coerceIn(0.05f, 1.0f))
                        .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                        .background(
                            if (deltaB > 0) colorB else colorB.copy(alpha = 0.45f)
                        )
                )
            }
        }
    }
}

@Composable
private fun AttributeCompareRow(
    label: String,
    valA: String,
    valB: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = valA,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Text(
            text = valB,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

