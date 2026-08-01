package com.dexter.app.ui.team

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.domain.model.TypeMatchupEngine
import com.dexter.app.ui.common.PokemonPickerBottomSheet
import com.dexter.app.ui.common.TypeChip
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.StatNumberStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TeamBuilderScreen(
    uiState: TeamUiState,
    onSlotClick: (Int) -> Unit,
    onRemoveSlot: (Int) -> Unit,
    onClearTeam: () -> Unit,
    onSelectPokemon: (Int, Pokemon) -> Unit,
    onDismissPicker: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticUtils = com.dexter.app.ui.common.rememberHapticUtils()
    val activeMembers = remember(uiState.teamSlots) {
        uiState.teamSlots.values.toList()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                modifier = Modifier.height(48.dp),
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Tight / 2)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
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
                                hapticUtils.lightTick()
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Dimens.ScreenEdgePadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.Section)
        ) {
            // 1. 6-Slot Roster Grid
            item {
                Text(
                    text = "TEAM ROSTER (6 SLOTS)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = Dimens.Compact)
                )

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
                                onSlotClick = { onSlotClick(slot) },
                                onRemoveClick = {
                                    hapticUtils.lightTick()
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
                                onSlotClick = { onSlotClick(slot) },
                                onRemoveClick = {
                                    hapticUtils.lightTick()
                                    onRemoveSlot(slot)
                                },
                                onPokemonClick = { onPokemonClick(it) }
                            )
                        }
                    }
                }
            }

            // 2. Team Coverage Matrix Section
            item {
                Text(
                    text = "TEAM COVERAGE MATRIX",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = Dimens.Compact)
                )

                TeamCoverageMatrixCard(teamMembers = activeMembers)
            }
        }
    }

    if (uiState.activePickerSlot != null) {
        PokemonPickerBottomSheet(
            onDismissRequest = onDismissPicker,
            onPokemonSelected = { pokemon ->
                hapticUtils.lightTick()
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
    onSlotClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (pokemon == null) {
        // Empty slot card
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(115.dp)
                .clickable { onSlotClick() },
            shape = RoundedCornerShape(Dimens.Section),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(Dimens.Section)
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
        // Filled slot card
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(115.dp)
                .clickable { onSlotClick() },
            shape = RoundedCornerShape(Dimens.Section),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.ElevationLevel2)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.Tight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(Dimens.Compact))
                        .background(pokemon.primaryType.seedColor.copy(alpha = 0.2f))
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
                            .padding(Dimens.Micro)
                    )
                }

                Spacer(modifier = Modifier.width(Dimens.Compact))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = pokemon.formattedNumber,
                        style = StatNumberStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = pokemon.capitalizedName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(Dimens.Micro / 2))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Micro / 2)
                    ) {
                        TypeChip(type = pokemon.primaryType, isCompact = true)
                        pokemon.secondaryType?.let {
                            TypeChip(type = it, isCompact = true)
                        }
                    }
                }

                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Pokémon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TeamCoverageMatrixCard(
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

    // Calculate offensive coverage gaps
    // A defender type D is covered if at least 1 team member has an attacking type (primary/secondary) super-effective against D
    val coveredOffensiveTypes = remember(teamMembers) {
        allTypes.filter { defenderType ->
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
    }

    val offensiveGaps = remember(coveredOffensiveTypes) {
        allTypes.filterNot { coveredOffensiveTypes.contains(it) }
    }

    // Calculate defensive weakness summary across team
    // For each attacking type, how many team members take >1.0x damage
    val defensiveWeaknesses = remember(teamMembers) {
        allTypes.associateWith { attackingType ->
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
    }

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
                            imageVector = if (offensiveGaps.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (offensiveGaps.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Offensive Coverage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${coveredOffensiveTypes.size} / 18 Covered",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Tight))

                LinearProgressIndicator(
                    progress = { coveredOffensiveTypes.size / 18f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (offensiveGaps.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )

                Spacer(modifier = Modifier.height(Dimens.Compact))

                if (offensiveGaps.isEmpty()) {
                    Text(
                        text = "Perfect Coverage! Your team can deal super-effective damage against all 18 types.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Coverage Gaps (${offensiveGaps.size} types without super-effective coverage):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = Dimens.Tight)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Tight / 2),
                        verticalArrangement = Arrangement.spacedBy(Dimens.Tight / 2)
                    ) {
                        offensiveGaps.forEach { type ->
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
                                val (weak, resist, immune) = defensiveWeaknesses[type] ?: Triple(0, 0, 0)
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
                .padding(horizontal = Dimens.Compact, vertical = Dimens.Tight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TypeChip(type = type, isCompact = true)

            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.Micro),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (weakCount > 0) {
                    Text(
                        text = "$weakCount W",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isHighRisk) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                if (resistCount > 0) {
                    Text(
                        text = "$resistCount R",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (immuneCount > 0) {
                    Text(
                        text = "$immuneCount I",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (weakCount == 0 && resistCount == 0 && immuneCount == 0) {
                    Text(
                        text = "Neutral",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
