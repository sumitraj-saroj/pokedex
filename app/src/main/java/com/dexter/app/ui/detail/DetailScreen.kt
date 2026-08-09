package com.dexter.app.ui.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import com.dexter.app.ui.common.bounceOnStateChange
import com.dexter.app.ui.common.spatialExpressiveSpring
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dexter.app.data.repository.AppThemeMode
import com.dexter.app.domain.model.PokemonVariant
import com.dexter.app.ui.common.PokemonStatsSection
import com.dexter.app.ui.common.PulsingPokeballLoader
import com.dexter.app.ui.common.TypeChip
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.StatNumberStyle
import com.dexter.app.ui.theme.animateColorSchemeAsState
import com.dexter.app.ui.theme.rememberTypeColorScheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun DetailScreen(
    uiState: DetailUiState,
    onBackClick: () -> Unit,
    onToggleCaught: () -> Unit,
    onToggleFavorite: () -> Unit,
    onVariantSelected: (PokemonVariant) -> Unit,
    onPokemonClick: (Int) -> Unit,
    onRetryTcgCards: () -> Unit = {},
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val basePokemon = uiState.pokemon

    if (basePokemon == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            PulsingPokeballLoader(message = "Loading Pokémon...")
        }
        return
    }

    val selectedVariant = uiState.selectedVariant
    val activeForm = (selectedVariant as? PokemonVariant.FormVariant)?.form

    val activePrimaryType = activeForm?.primaryType ?: basePokemon.primaryType
    val activeSecondaryType = activeForm?.secondaryType ?: basePokemon.secondaryType
    val activeStats = activeForm?.stats ?: basePokemon.stats
    val activeHeightM = activeForm?.heightM ?: basePokemon.heightM
    val activeWeightKg = activeForm?.weightKg ?: basePokemon.weightKg
    val activeName = activeForm?.displayName ?: basePokemon.capitalizedName
    val activeArtworkUrl = selectedVariant.getMainArtworkUrl(basePokemon)

    val variants = remember(basePokemon, uiState.forms) {
        PokemonVariant.buildVariantsForPokemon(basePokemon, uiState.forms)
    }

    val isDark = com.dexter.app.ui.theme.LocalDarkTheme.current

    val targetTypeColorScheme = rememberTypeColorScheme(
        primaryType = activePrimaryType,
        secondaryType = activeSecondaryType,
        isDark = isDark
    )

    val animatedColorScheme = animateColorSchemeAsState(targetColorScheme = targetTypeColorScheme)

    androidx.compose.runtime.CompositionLocalProvider(
        com.dexter.app.ui.theme.LocalDarkTheme provides isDark
    ) {
        MaterialExpressiveTheme(
            colorScheme = animatedColorScheme,
            motionScheme = MotionScheme.expressive()
        ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
            topBar = {
                val hapticUtils = com.dexter.app.ui.common.rememberHapticUtils()
                com.dexter.app.ui.common.GlassmorphicTopAppBar(
                    title = {
                        Text(
                            text = basePokemon.formattedNumber,
                            style = StatNumberStyle.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(Dimens.MinTouchTarget)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        ButtonGroup(
                            modifier = Modifier.padding(end = Dimens.Tight)
                        ) {
                            IconButton(
                                onClick = {
                                    hapticUtils.lightTick()
                                    onToggleCaught()
                                },
                                modifier = Modifier.size(Dimens.MinTouchTarget)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CatchingPokemon,
                                    contentDescription = "Toggle Caught Status",
                                    tint = if (basePokemon.collection?.isCaught == true) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    },
                                    modifier = Modifier.bounceOnStateChange(basePokemon.collection?.isCaught == true)
                                )
                            }
                            IconButton(
                                onClick = {
                                    hapticUtils.lightTick()
                                    onToggleFavorite()
                                },
                                modifier = Modifier.size(Dimens.MinTouchTarget)
                            ) {
                                Icon(
                                    imageVector = if (basePokemon.collection?.isFavorite == true) {
                                        Icons.Default.Favorite
                                    } else {
                                        Icons.Default.FavoriteBorder
                                    },
                                    contentDescription = "Toggle Favorite",
                                    tint = if (basePokemon.collection?.isFavorite == true) {
                                        Color.Red
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    },
                                    modifier = Modifier.bounceOnStateChange(basePokemon.collection?.isFavorite == true)
                                )
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Interactive 3D Multi-Layered Holographic Header Trading Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.ScreenEdgePadding, vertical = Dimens.Tight)
                ) {
                    val cardModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedBounds(
                                rememberSharedContentState(key = "pokemon_card_${basePokemon.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = spatialExpressiveSpring()
                            )
                        }
                    } else {
                        Modifier
                    }

                    val isSpecialPokemon = basePokemon.isLegendary || basePokemon.isMythical || selectedVariant != PokemonVariant.Official

                    Interactive3DTradingCard(
                        pokemon = basePokemon,
                        activeName = activeName,
                        activeArtworkUrl = activeArtworkUrl,
                        activePrimaryType = activePrimaryType,
                        activeSecondaryType = activeSecondaryType,
                        activeStats = activeStats,
                        activeHeightM = activeHeightM,
                        activeWeightKg = activeWeightKg,
                        isSpecialPokemon = isSpecialPokemon,
                        modifier = cardModifier,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }

                // Name & Category
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.ScreenEdgePadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = activeName,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Dimens.Micro))

                    Text(
                        text = basePokemon.category,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Tight))

                // Variant Preview Strip (below Name, above Type Chips)
                VariantPreviewStrip(
                    pokemon = basePokemon,
                    variants = variants,
                    selectedVariant = selectedVariant,
                    onVariantSelected = onVariantSelected
                )

                Spacer(modifier = Modifier.height(Dimens.Tight))

                // Type Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TypeChip(type = activePrimaryType)
                    activeSecondaryType?.let {
                        Spacer(modifier = Modifier.width(Dimens.Tight))
                        TypeChip(type = it)
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Default))

                // Height & Weight metrics card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.ScreenEdgePadding, vertical = Dimens.Tight),
                    shape = RoundedCornerShape(Dimens.Default),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimens.ElevationLevel1)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.Default),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "WEIGHT",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(Dimens.Micro))
                            Text(
                                text = "$activeWeightKg kg",
                                style = StatNumberStyle,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(Dimens.Major)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "HEIGHT",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(Dimens.Micro))
                            Text(
                                text = "$activeHeightM m",
                                style = StatNumberStyle,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Flavor Text
                if (basePokemon.flavorText.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.ScreenEdgePadding, vertical = Dimens.Tight),
                        shape = RoundedCornerShape(Dimens.Default),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.ElevationLevel1)
                    ) {
                        Text(
                            text = basePokemon.flavorText,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.Default)
                        )
                    }
                }

                // Base Stats Section
                activeStats?.let { stats ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.ScreenEdgePadding, vertical = Dimens.Tight),
                        shape = RoundedCornerShape(Dimens.Default),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.ElevationLevel1)
                    ) {
                        PokemonStatsSection(
                            stats = stats,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(Dimens.Default)
                        )
                    }
                }

                // Defensive Type Matchup Matrix
                TypeMatchupSection(
                    primaryType = activePrimaryType,
                    secondaryType = activeSecondaryType,
                    modifier = Modifier.padding(horizontal = Dimens.ScreenEdgePadding, vertical = Dimens.Tight)
                )

                // Interactive Evolution Tree
                EvolutionTreeSection(
                    evolutionNodes = uiState.evolutionNodes,
                    onPokemonClick = onPokemonClick,
                    modifier = Modifier.padding(horizontal = Dimens.ScreenEdgePadding, vertical = Dimens.Tight)
                )

                // Moves & Abilities Section
                MovesAndAbilitiesSection(
                    abilities = uiState.abilities,
                    moves = uiState.moves,
                    modifier = Modifier.padding(horizontal = Dimens.ScreenEdgePadding, vertical = Dimens.Tight)
                )

                // TCG Pokémon Cards Gallery Section
                com.dexter.app.ui.detail.components.TcgCardsSection(
                    uiState = uiState.tcgCardsUiState,
                    onRetry = onRetryTcgCards,
                    modifier = Modifier.padding(horizontal = Dimens.ScreenEdgePadding, vertical = Dimens.Tight)
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
}
