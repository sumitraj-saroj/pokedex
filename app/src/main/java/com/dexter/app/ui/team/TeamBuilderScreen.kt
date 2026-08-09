package com.dexter.app.ui.team

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.domain.model.TypeMatchupEngine
import com.dexter.app.ui.common.PokemonPickerBottomSheet
import com.dexter.app.ui.common.TypeChip
import com.dexter.app.ui.common.glassmorphicContainer
import com.dexter.app.ui.common.rememberHapticUtils
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.StatNumberStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class TeamSynergyRank(val label: String, val title: String, val badgeColor: Color) {
    RANK_S("S", "Flawless Battle Synergy", Color(0xFFFFD700)),
    RANK_A("A", "Strong Battle Synergy", Color(0xFFA855F7)),
    RANK_B("B", "Moderate Battle Synergy", Color(0xFF3B82F6)),
    RANK_C("C", "Unbalanced / Incomplete", Color(0xFF94A3B8))
}

data class TeamSynergyMetrics(
    val rank: TeamSynergyRank,
    val totalScore: Int,
    val offenseScore: Float,
    val defenseScore: Float,
    val speedScore: Float,
    val bulkScore: Float,
    val diversityScore: Float,
    val synergyScore: Float,
    val coveredTypes: Set<PokemonType>,
    val offensiveGaps: List<PokemonType>,
    val severeWeaknesses: List<Pair<PokemonType, Int>>,
    val defensiveSummary: Map<PokemonType, Triple<Int, Int, Int>>
)

private fun calculateTeamSynergyMetrics(teamMembers: List<Pokemon>): TeamSynergyMetrics {
    if (teamMembers.isEmpty()) {
        return TeamSynergyMetrics(
            rank = TeamSynergyRank.RANK_C,
            totalScore = 0,
            offenseScore = 0f,
            defenseScore = 0f,
            speedScore = 0f,
            bulkScore = 0f,
            diversityScore = 0f,
            synergyScore = 0f,
            coveredTypes = emptySet(),
            offensiveGaps = PokemonType.entries.toList(),
            severeWeaknesses = emptyList(),
            defensiveSummary = emptyMap()
        )
    }

    val allTypes = PokemonType.entries

    // 1. Offensive Type Coverage (super-effective against defender)
    val coveredTypes = allTypes.filter { defenderType ->
        teamMembers.any { member ->
            val primaryEffective = TypeMatchupEngine.calculateDefensiveMatchups(defenderType, null)
                .firstOrNull { it.type == member.primaryType }?.multiplier ?: 1.0
            val secondaryEffective = member.secondaryType?.let { sec ->
                TypeMatchupEngine.calculateDefensiveMatchups(defenderType, null)
                    .firstOrNull { it.type == sec }?.multiplier ?: 1.0
            } ?: 1.0
            primaryEffective > 1.0 || secondaryEffective > 1.0
        }
    }.toSet()

    val offensiveGaps = allTypes.filterNot { coveredTypes.contains(it) }

    // 2. Defensive Summary across Team
    val defensiveSummary = allTypes.associateWith { attackingType ->
        var weakCount = 0
        var resistCount = 0
        var immuneCount = 0

        teamMembers.forEach { member ->
            val matchups = TypeMatchupEngine.calculateDefensiveMatchups(member.primaryType, member.secondaryType)
            val mult = matchups.firstOrNull { it.type == attackingType }?.multiplier ?: 1.0
            when {
                mult == 0.0 -> immuneCount++
                mult > 1.0 -> weakCount++
                mult < 1.0 -> resistCount++
            }
        }
        Triple(weakCount, resistCount, immuneCount)
    }

    val severeWeaknesses = defensiveSummary.entries
        .filter { it.value.first >= 3 }
        .map { it.key to it.value.first }

    // 3. Compute 6 Radar Axis Values (0.1 .. 1.0)
    val avgOffense = teamMembers.map { member ->
        val s = member.stats
        if (s != null) (s.attack + s.spAttack) / 2f else 75f
    }.average().toFloat()
    val offenseScore = ((avgOffense / 130f) * 0.5f + (coveredTypes.size / 18f) * 0.5f).coerceIn(0.1f, 1f)

    val avgDefense = teamMembers.map { member ->
        val s = member.stats
        if (s != null) (s.defense + s.spDefense) / 2f else 75f
    }.average().toFloat()
    val totalResistances = defensiveSummary.values.sumOf { it.second + it.third }
    val maxResistances = (teamMembers.size * 6).toFloat()
    val defenseScore = ((avgDefense / 130f) * 0.5f + (totalResistances / maxResistances).coerceAtMost(1f) * 0.5f).coerceIn(0.1f, 1f)

    val avgSpeed = teamMembers.map { member ->
        member.stats?.speed?.toFloat() ?: 75f
    }.average().toFloat()
    val speedScore = (avgSpeed / 130f).coerceIn(0.1f, 1f)

    val avgHp = teamMembers.map { member ->
        member.stats?.hp?.toFloat() ?: 75f
    }.average().toFloat()
    val bulkScore = (avgHp / 130f).coerceIn(0.1f, 1f)

    val uniqueTypes = teamMembers.flatMap { listOfNotNull(it.primaryType, it.secondaryType) }.distinct().size
    val diversityScore = (uniqueTypes / 10f).coerceIn(0.1f, 1f)

    val overlapPenalty = (severeWeaknesses.size * 0.25f)
    val rosterFill = teamMembers.size / 6f
    val synergyScore = ((1f - overlapPenalty).coerceAtLeast(0.1f) * 0.6f + rosterFill * 0.4f).coerceIn(0.1f, 1f)

    // 4. Calculate Score (0 .. 100)
    val rosterPart = (teamMembers.size / 6f) * 25f
    val coveragePart = (coveredTypes.size / 18f) * 35f
    val defensePart = (30f - (severeWeaknesses.size * 8f)).coerceIn(0f, 30f)
    val diversityPart = (uniqueTypes / 10f).coerceAtMost(1f) * 10f

    val totalScore = (rosterPart + coveragePart + defensePart + diversityPart).toInt().coerceIn(0, 100)

    val rank = when {
        totalScore >= 88 -> TeamSynergyRank.RANK_S
        totalScore >= 72 -> TeamSynergyRank.RANK_A
        totalScore >= 55 -> TeamSynergyRank.RANK_B
        else -> TeamSynergyRank.RANK_C
    }

    return TeamSynergyMetrics(
        rank = rank,
        totalScore = totalScore,
        offenseScore = offenseScore,
        defenseScore = defenseScore,
        speedScore = speedScore,
        bulkScore = bulkScore,
        diversityScore = diversityScore,
        synergyScore = synergyScore,
        coveredTypes = coveredTypes,
        offensiveGaps = offensiveGaps,
        severeWeaknesses = severeWeaknesses,
        defensiveSummary = defensiveSummary
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TeamBuilderScreen(
    uiState: TeamUiState,
    onSlotClick: (Int) -> Unit,
    onRemoveSlot: (Int) -> Unit,
    onClearTeam: () -> Unit,
    onSwapSlots: (Int, Int) -> Unit = { _, _ -> },
    onSelectPokemon: (Int, Pokemon) -> Unit,
    onDismissPicker: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticUtils = rememberHapticUtils()
    val activeMembers = remember(uiState.teamSlots) {
        uiState.teamSlots.values.toList()
    }
    val metrics = remember(activeMembers) {
        calculateTeamSynergyMetrics(activeMembers)
    }

    // Drag-and-Drop Reordering State
    var draggingSlot by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var hoveredSlot by remember { mutableStateOf<Int?>(null) }
    val slotBoundsMap = remember { mutableStateMapOf<Int, Rect>() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            com.dexter.app.ui.common.GlassmorphicTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Tight / 2)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Team Builder",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                },
                actions = {
                    if (activeMembers.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                hapticUtils.errorPulse()
                                onClearTeam()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear team",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = Dimens.ScreenEdgePadding,
                top = Dimens.ScreenEdgePadding,
                end = Dimens.ScreenEdgePadding,
                bottom = 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.Section)
        ) {
            // 1. 6-Slot Roster Grid with Drag-and-Drop Reordering
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TEAM ROSTER (6 SLOTS)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = Dimens.Micro)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Micro)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Hold & drag to swap",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Compact))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Compact)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Dimens.Compact)
                    ) {
                        (1..3).forEach { slot ->
                            TeamSlotCard(
                                slotNumber = slot,
                                pokemon = uiState.teamSlots[slot],
                                isDragging = draggingSlot == slot,
                                isHovered = hoveredSlot == slot,
                                dragOffset = if (draggingSlot == slot) dragOffset else Offset.Zero,
                                onPositioned = { rect -> slotBoundsMap[slot] = rect },
                                onDragStart = {
                                    hapticUtils.heavyImpact()
                                    draggingSlot = slot
                                    dragOffset = Offset.Zero
                                },
                                onDrag = { amount ->
                                    dragOffset += amount
                                    val originRect = slotBoundsMap[slot]
                                    if (originRect != null) {
                                        val center = originRect.center + dragOffset
                                        val target = slotBoundsMap.entries.firstOrNull { (s, r) -> s != slot && r.contains(center) }?.key
                                        if (target != hoveredSlot) {
                                            if (target != null) hapticUtils.selectionTick()
                                            hoveredSlot = target
                                        }
                                    }
                                },
                                onDragEnd = {
                                    val from = draggingSlot
                                    val to = hoveredSlot
                                    if (from != null && to != null && from != to) {
                                        hapticUtils.heavyImpact()
                                        onSwapSlots(from, to)
                                    }
                                    draggingSlot = null
                                    hoveredSlot = null
                                    dragOffset = Offset.Zero
                                },
                                onSlotClick = {
                                    hapticUtils.selectionTick()
                                    onSlotClick(slot)
                                },
                                onRemoveClick = {
                                    hapticUtils.heavyImpact()
                                    onRemoveSlot(slot)
                                },
                                onPokemonClick = { onPokemonClick(it) }
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Dimens.Compact)
                    ) {
                        (4..6).forEach { slot ->
                            TeamSlotCard(
                                slotNumber = slot,
                                pokemon = uiState.teamSlots[slot],
                                isDragging = draggingSlot == slot,
                                isHovered = hoveredSlot == slot,
                                dragOffset = if (draggingSlot == slot) dragOffset else Offset.Zero,
                                onPositioned = { rect -> slotBoundsMap[slot] = rect },
                                onDragStart = {
                                    hapticUtils.heavyImpact()
                                    draggingSlot = slot
                                    dragOffset = Offset.Zero
                                },
                                onDrag = { amount ->
                                    dragOffset += amount
                                    val originRect = slotBoundsMap[slot]
                                    if (originRect != null) {
                                        val center = originRect.center + dragOffset
                                        val target = slotBoundsMap.entries.firstOrNull { (s, r) -> s != slot && r.contains(center) }?.key
                                        if (target != hoveredSlot) {
                                            if (target != null) hapticUtils.selectionTick()
                                            hoveredSlot = target
                                        }
                                    }
                                },
                                onDragEnd = {
                                    val from = draggingSlot
                                    val to = hoveredSlot
                                    if (from != null && to != null && from != to) {
                                        hapticUtils.heavyImpact()
                                        onSwapSlots(from, to)
                                    }
                                    draggingSlot = null
                                    hoveredSlot = null
                                    dragOffset = Offset.Zero
                                },
                                onSlotClick = {
                                    hapticUtils.selectionTick()
                                    onSlotClick(slot)
                                },
                                onRemoveClick = {
                                    hapticUtils.heavyImpact()
                                    onRemoveSlot(slot)
                                },
                                onPokemonClick = { onPokemonClick(it) }
                            )
                        }
                    }
                }
            }

            // 2. Team Synergy Rating & Canvas Radar Chart Section
            item {
                Text(
                    text = "BATTLE SYNERGY RADAR",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = Dimens.Compact)
                )

                TeamSynergyCard(metrics = metrics, activeMembers = activeMembers)
            }

            // 3. Weakness Overlap Warnings Section
            if (activeMembers.isNotEmpty()) {
                item {
                    Text(
                        text = "DEFENSIVE WEAKNESS OVERLAPS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = Dimens.Compact)
                    )

                    WeaknessOverlapWarningCard(metrics = metrics)
                }
            }

            // 4. Team Coverage Matrix Section
            item {
                Text(
                    text = "OFFENSIVE TYPE COVERAGE MATRIX",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = Dimens.Compact)
                )

                TeamCoverageMatrixCard(metrics = metrics, teamMembers = activeMembers)
            }
        }
    }

    if (uiState.activePickerSlot != null) {
        PokemonPickerBottomSheet(
            onDismissRequest = onDismissPicker,
            onPokemonSelected = { pokemon ->
                hapticUtils.heavyImpact()
                onSelectPokemon(uiState.activePickerSlot, pokemon)
            },
            pokemonList = uiState.allPokemon,
            title = "Select Pokémon for Slot ${uiState.activePickerSlot}"
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TeamSlotCard(
    slotNumber: Int,
    pokemon: Pokemon?,
    isDragging: Boolean,
    isHovered: Boolean,
    dragOffset: Offset,
    onPositioned: (Rect) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onSlotClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerShape = RoundedCornerShape(Dimens.Section)
    val cardBorder = if (isHovered) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else null

    if (pokemon == null) {
        // Empty slot card
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(132.dp)
                .onGloballyPositioned { coordinates ->
                    onPositioned(coordinates.boundsInWindow())
                }
                .clickable { onSlotClick() },
            shape = containerShape,
            border = cardBorder,
            colors = CardDefaults.cardColors(
                containerColor = if (isHovered) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = containerShape
                    )
                    .padding(Dimens.Compact),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Pokémon",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.Micro))
                    Text(
                        text = "Slot $slotNumber",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap to add",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    } else {
        // Filled slot card with drag capabilities
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(132.dp)
                .onGloballyPositioned { coordinates ->
                    onPositioned(coordinates.boundsInWindow())
                }
                .pointerInput(slotNumber, pokemon) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { onDragStart() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount)
                        },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragEnd() }
                    )
                }
                .zIndex(if (isDragging) 100f else 1f)
                .graphicsLayer {
                    if (isDragging) {
                        translationX = dragOffset.x
                        translationY = dragOffset.y
                        scaleX = 1.05f
                        scaleY = 1.05f
                        shadowElevation = 16.dp.toPx()
                        alpha = 0.92f
                    }
                }
                .clickable { onSlotClick() },
            shape = containerShape,
            border = cardBorder,
            colors = CardDefaults.cardColors(
                containerColor = if (isHovered) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isDragging) Dimens.ElevationLevel3 else Dimens.ElevationLevel2
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.Compact, vertical = Dimens.Micro),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header: Number & Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pokemon.formattedNumber,
                        style = StatNumberStyle.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Drag item",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(14.dp)
                        )
                        IconButton(
                            onClick = onRemoveClick,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Pokémon",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Pokémon Artwork Image
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(Dimens.Compact))
                        .background(pokemon.primaryType.seedColor.copy(alpha = 0.15f))
                        .clickable { onPokemonClick(pokemon.id) },
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
                            .padding(2.dp)
                    )
                }

                // Pokémon Name Only
                Text(
                    text = pokemon.capitalizedName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun TeamSynergyCard(
    metrics: TeamSynergyMetrics,
    activeMembers: List<Pokemon>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .glassmorphicContainer(
                backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f),
                borderColor = metrics.rank.badgeColor.copy(alpha = 0.5f),
                borderWidth = 1.5.dp,
                shape = RoundedCornerShape(Dimens.Section)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.ElevationLevel2)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.ScreenEdgePadding)
        ) {
            // Header Rating Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Compact)
                ) {
                    // Rank Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = metrics.rank.badgeColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.5.dp, metrics.rank.badgeColor),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = metrics.rank.label,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = metrics.rank.badgeColor
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = metrics.rank.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${activeMembers.size} / 6 Roster Members",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Dimens.Compact))

                // Score Chip
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Dimens.Compact, vertical = Dimens.Micro),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = metrics.rank.badgeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${metrics.totalScore} / 100",
                            style = StatNumberStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Compact))

            // Score Progress Bar
            LinearProgressIndicator(
                progress = { metrics.totalScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = metrics.rank.badgeColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            Spacer(modifier = Modifier.height(Dimens.Section))

            // 6-Axis Synergy Radar Canvas Chart
            if (activeMembers.isNotEmpty()) {
                TeamSynergyRadarChart(metrics = metrics)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add Pokémon to roster to generate Battle Synergy Radar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun TeamSynergyRadarChart(
    metrics: TeamSynergyMetrics,
    modifier: Modifier = Modifier
) {
    val axisNames = listOf("OFFENSE", "DEFENSE", "SPEED", "BULK", "DIVERSITY", "SYNERGY")
    val axisValues = remember(metrics) {
        listOf(
            metrics.offenseScore,
            metrics.defenseScore,
            metrics.speedScore,
            metrics.bulkScore,
            metrics.diversityScore,
            metrics.synergyScore
        )
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(metrics) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = EaseOutCubic)
        )
    }

    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.2f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = (size.width.coerceAtMost(size.height) / 2f) * 0.65f
            val numAxes = 6

            fun getAngle(i: Int): Double = -PI / 2 + (i * 2 * PI / numAxes)

            // 1. Grid Polygons (25%, 50%, 75%, 100%)
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

            // 2. Radial Axis Lines
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

            // 3. Polygon
            val progress = animProgress.value
            val statPath = Path()
            val statVertices = ArrayList<Offset>(numAxes)

            for (i in 0 until numAxes) {
                val valRatio = axisValues.getOrElse(i) { 0f } * progress
                val radius = outerRadius * valRatio
                val angle = getAngle(i)
                val x = center.x + (radius * cos(angle)).toFloat()
                val y = center.y + (radius * sin(angle)).toFloat()
                val vertex = Offset(x, y)
                statVertices.add(vertex)
                if (i == 0) statPath.moveTo(x, y) else statPath.lineTo(x, y)
            }
            statPath.close()

            // Fill gradient
            drawPath(
                path = statPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.55f),
                        tertiaryColor.copy(alpha = 0.22f)
                    ),
                    center = center,
                    radius = outerRadius * 1.1f
                )
            )

            // Outline stroke
            drawPath(
                path = statPath,
                color = primaryColor,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Vertices
            for (vertex in statVertices) {
                drawCircle(
                    color = primaryColor,
                    radius = 4.5.dp.toPx(),
                    center = vertex
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = vertex
                )
            }

            // 4. Axis Labels & Percentages
            val labelRadius = outerRadius + 18.dp.toPx()
            val labelStyle = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = onSurfaceVariantColor
            )
            val valStyle = StatNumberStyle.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = onSurfaceColor
            )

            for (i in 0 until numAxes) {
                val angle = getAngle(i)
                val cosA = cos(angle)
                val sinA = sin(angle)

                val lx = center.x + (labelRadius * cosA).toFloat()
                val ly = center.y + (labelRadius * sinA).toFloat()

                val name = axisNames.getOrElse(i) { "" }
                val scorePct = (axisValues.getOrElse(i) { 0f } * 100 * progress).toInt()
                val pctStr = "$scorePct%"

                val nameResult = textMeasurer.measure(name, style = labelStyle)
                val valResult = textMeasurer.measure(pctStr, style = valStyle)

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
                    text = pctStr,
                    topLeft = Offset(tx + (totalWidth - valResult.size.width) / 2f, ty + nameResult.size.height),
                    style = valStyle
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeaknessOverlapWarningCard(
    metrics: TeamSynergyMetrics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.Section),
        colors = CardDefaults.cardColors(
            containerColor = if (metrics.severeWeaknesses.isNotEmpty()) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.ElevationLevel1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.ScreenEdgePadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.Tight)
            ) {
                Icon(
                    imageVector = if (metrics.severeWeaknesses.isNotEmpty()) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (metrics.severeWeaknesses.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (metrics.severeWeaknesses.isNotEmpty()) "Weakness Overlap Warnings" else "Defensive Balance Check",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (metrics.severeWeaknesses.isNotEmpty()) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(Dimens.Compact))

            if (metrics.severeWeaknesses.isEmpty()) {
                Text(
                    text = "Great balance! No 3+ team members share a common type weakness.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Alert: 3+ team members share common weaknesses to the following types:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f),
                    modifier = Modifier.padding(bottom = Dimens.Compact)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Compact),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Compact)
                ) {
                    metrics.severeWeaknesses.forEach { (type, count) ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Dimens.Compact, vertical = Dimens.Micro),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimens.Micro)
                            ) {
                                TypeChip(type = type, isCompact = true)
                                Text(
                                    text = "$count Pokémon Weak",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TeamCoverageMatrixCard(
    metrics: TeamSynergyMetrics,
    teamMembers: List<Pokemon>,
    modifier: Modifier = Modifier
) {
    if (teamMembers.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimens.Section),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.Section),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(Dimens.Compact))
                Text(
                    text = "Add Pokémon to view coverage",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Coverage matrix calculates defensive weaknesses and offensive type gaps across your 6 roster slots.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val allTypes = PokemonType.entries

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.Section)
    ) {
        // --- OFFENSIVE TYPE COVERAGE CARD ---
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
                    .padding(Dimens.ScreenEdgePadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Tight)
                    ) {
                        Icon(
                            imageVector = if (metrics.offensiveGaps.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (metrics.offensiveGaps.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Offensive Coverage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${metrics.coveredTypes.size} / 18 Covered",
                        style = StatNumberStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Tight))

                LinearProgressIndicator(
                    progress = { metrics.coveredTypes.size / 18f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (metrics.offensiveGaps.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )

                Spacer(modifier = Modifier.height(Dimens.Compact))

                if (metrics.offensiveGaps.isEmpty()) {
                    Text(
                        text = "Perfect Coverage! Your team can deal super-effective damage against all 18 types.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Coverage Gaps (${metrics.offensiveGaps.size} types without super-effective coverage):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = Dimens.Tight)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Tight / 2),
                        verticalArrangement = Arrangement.spacedBy(Dimens.Tight / 2)
                    ) {
                        metrics.offensiveGaps.forEach { type ->
                            TypeChip(type = type, isCompact = false)
                        }
                    }
                }
            }
        }

        // --- DEFENSIVE WEAKNESS MATRIX CARD ---
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
                    .padding(Dimens.ScreenEdgePadding)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Tight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Defensive Weakness Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Compact))

                Text(
                    text = "Combined defensive vulnerabilities across active team members:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Dimens.Compact)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.Compact / 2)
                ) {
                    allTypes.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.Compact)
                        ) {
                            pair.forEach { type ->
                                val (weak, resist, immune) = metrics.defensiveSummary[type] ?: Triple(0, 0, 0)
                                DefensiveTypeRowItem(
                                    type = type,
                                    weakCount = weak,
                                    resistCount = resist,
                                    immuneCount = immune,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DefensiveTypeRowItem(
    type: PokemonType,
    weakCount: Int,
    resistCount: Int,
    immuneCount: Int,
    modifier: Modifier = Modifier
) {
    val isHighRisk = weakCount >= 3

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.Compact),
        color = if (isHighRisk) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TypeChip(
                type = type,
                isCompact = true,
                modifier = Modifier.width(66.dp)
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (weakCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isHighRisk) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Text(
                                text = "${weakCount}W",
                                style = StatNumberStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
                                color = if (isHighRisk) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (resistCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = "${resistCount}R",
                                style = StatNumberStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (immuneCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = "${immuneCount}I",
                                style = StatNumberStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (weakCount == 0 && resistCount == 0 && immuneCount == 0) {
                        Text(
                            text = "Neutral",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
