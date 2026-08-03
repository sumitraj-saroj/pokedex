package com.dexter.app.ui.compare

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
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
import com.dexter.app.domain.model.PokemonStats
import com.dexter.app.ui.common.PokemonPickerBottomSheet
import com.dexter.app.ui.common.TypeChip
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.StatNumberStyle
import com.dexter.app.ui.theme.blendTypeSeedColors
import com.dexter.app.ui.theme.generateMaterial3ColorScheme

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
    val isDark = isSystemInDarkTheme()
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
                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Compare Mode",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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

                // 2. Base Stats Side-by-Side Comparison
                if (pkmnA != null && pkmnB != null) {
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

                                CompareStatRow(name = "HP", valA = statsA.hp, valB = statsB.hp)
                                CompareStatRow(name = "Attack", valA = statsA.attack, valB = statsB.attack)
                                CompareStatRow(name = "Defense", valA = statsA.defense, valB = statsB.defense)
                                CompareStatRow(name = "Sp. Atk", valA = statsA.spAttack, valB = statsB.spAttack)
                                CompareStatRow(name = "Sp. Def", valA = statsA.spDefense, valB = statsB.spDefense)
                                CompareStatRow(name = "Speed", valA = statsA.speed, valB = statsB.speed)
                                CompareStatRow(name = "Total", valA = statsA.total, valB = statsB.total, maxStat = 720)
                            }
                        }
                    }

                    // 3. Physical Attributes & Type Comparison Card
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
private fun CompareStatRow(
    name: String,
    valA: Int,
    valB: Int,
    maxStat: Int = 255
) {
    val delta = valA - valB

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
                    fontWeight = if (delta > 0) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (delta > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )

                if (delta > 0) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Text(
                            text = "+$delta",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = Dimens.Micro, vertical = 1.dp)
                        )
                    }
                }
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
                if (delta < 0) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 2.dp)
                    ) {
                        Text(
                            text = "+${-delta}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = Dimens.Micro, vertical = 1.dp)
                        )
                    }
                }

                Text(
                    text = valB.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (delta < 0) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (delta < 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                            if (delta > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
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
                            if (delta < 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
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
