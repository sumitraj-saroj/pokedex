package com.dexter.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.dexter.app.ui.common.GlassmorphicTopAppBar
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.dexter.app.data.repository.AppThemeMode
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.SpecialCategory
import com.dexter.app.domain.model.SyncState
import com.dexter.app.ui.common.EmptySearchResultsState
import com.dexter.app.ui.common.SearchFilterBar
import com.dexter.app.ui.common.SkeletonPokemonCard
import com.dexter.app.ui.common.SyncProgressScreen
import com.dexter.app.ui.common.TypeChip
import com.dexter.app.ui.common.bouncyClickable
import com.dexter.app.ui.common.holographicShimmer
import com.dexter.app.ui.common.spatialExpressiveSpring
import com.dexter.app.ui.filter.FilterBottomSheet
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.StatNumberStyle
import com.dexter.app.ui.theme.blendTypeSeedColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onPokemonClick: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortOptionSelect: (SortOption) -> Unit = {},
    onSortOrderSelect: (SortOrder) -> Unit = {},
    onGenerationToggle: (Int) -> Unit,
    onTypeToggle: (com.dexter.app.domain.model.PokemonType) -> Unit,
    onSpecialCategoryToggle: (SpecialCategory) -> Unit,
    onSortOptionReset: () -> Unit = {},
    onClearFilters: () -> Unit,
    onResyncClick: () -> Unit,
    onThemeToggleClick: () -> Unit,
    onProfileClick: () -> Unit,
    onRegionMapClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    var showFilterSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        FilterBottomSheet(
            uiState = uiState,
            onDismiss = { showFilterSheet = false },
            onSortOptionSelect = onSortOptionSelect,
            onSortOrderSelect = onSortOrderSelect,
            onGenerationToggle = onGenerationToggle,
            onTypeToggle = onTypeToggle,
            onSpecialCategoryToggle = onSpecialCategoryToggle,
            onClearFilters = onClearFilters
        )
    }

    val filteredList = uiState.filteredList

    if (uiState.syncState is SyncState.Syncing && uiState.pokemonList.isEmpty()) {
        SyncProgressScreen(
            current = (uiState.syncState as SyncState.Syncing).current,
            total = (uiState.syncState as SyncState.Syncing).total
        )
        return
    }

    val isExpanded = windowWidthSizeClass == WindowWidthSizeClass.Expanded

    if (isExpanded) {
        val inspectorViewModel: InspectorViewModel = hiltViewModel()
        val inspectorUiState by inspectorViewModel.uiState.collectAsStateWithLifecycle()
        var selectedPokemonId by remember { mutableStateOf<Int?>(null) }

        LaunchedEffect(filteredList) {
            if (filteredList.isNotEmpty() && (selectedPokemonId == null || filteredList.none { it.id == selectedPokemonId })) {
                val firstId = filteredList.first().id
                selectedPokemonId = firstId
                inspectorViewModel.selectPokemon(firstId)
            }
        }

        Row(modifier = modifier.fillMaxSize()) {
            // Left Pane (40% width): Scrollable search bar, quick filters, and Pokémon grid
            Box(modifier = Modifier.weight(0.40f).fillMaxHeight()) {
                HomeScreenListPane(
                    uiState = uiState,
                    filteredList = filteredList,
                    selectedPokemonId = selectedPokemonId,
                    onPokemonSelect = { pokemonId ->
                        selectedPokemonId = pokemonId
                        inspectorViewModel.selectPokemon(pokemonId)
                    },
                    onSearchQueryChange = onSearchQueryChange,
                    onOpenFilterSheet = { showFilterSheet = true },
                    onGenerationToggle = onGenerationToggle,
                    onTypeToggle = onTypeToggle,
                    onSpecialCategoryToggle = onSpecialCategoryToggle,
                    onSortOptionReset = onSortOptionReset,
                    onClearFilters = onClearFilters,
                    onResyncClick = onResyncClick,
                    onThemeToggleClick = onThemeToggleClick,
                    onProfileClick = onProfileClick,
                    onRegionMapClick = onRegionMapClick,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            // Right Pane (60% width): Selected Pokémon Live Inspector
            Box(modifier = Modifier.weight(0.60f).fillMaxHeight()) {
                PokemonInspectorPane(
                    uiState = inspectorUiState,
                    onPokemonClick = { targetId ->
                        selectedPokemonId = targetId
                        inspectorViewModel.selectPokemon(targetId)
                    },
                    onToggleCaught = inspectorViewModel::toggleCaught,
                    onToggleFavorite = inspectorViewModel::toggleFavorite,
                    onVariantSelected = inspectorViewModel::selectVariant,
                    onRetryTcgCards = inspectorViewModel::retryFetchTcgCards,
                    onOpenFullScreen = { fullId -> onPokemonClick(fullId) }
                )
            }
        }
    } else {
        HomeScreenListPane(
            uiState = uiState,
            filteredList = filteredList,
            selectedPokemonId = null,
            onPokemonSelect = { pokemonId -> onPokemonClick(pokemonId) },
            onSearchQueryChange = onSearchQueryChange,
            onOpenFilterSheet = { showFilterSheet = true },
            onGenerationToggle = onGenerationToggle,
            onTypeToggle = onTypeToggle,
            onSpecialCategoryToggle = onSpecialCategoryToggle,
            onSortOptionReset = onSortOptionReset,
            onClearFilters = onClearFilters,
            onResyncClick = onResyncClick,
            onThemeToggleClick = onThemeToggleClick,
            onProfileClick = onProfileClick,
            onRegionMapClick = onRegionMapClick,
            modifier = modifier,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun HomeScreenListPane(
    uiState: HomeUiState,
    filteredList: List<Pokemon>,
    selectedPokemonId: Int?,
    onPokemonSelect: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenFilterSheet: () -> Unit,
    onGenerationToggle: (Int) -> Unit,
    onTypeToggle: (com.dexter.app.domain.model.PokemonType) -> Unit,
    onSpecialCategoryToggle: (SpecialCategory) -> Unit,
    onSortOptionReset: () -> Unit,
    onClearFilters: () -> Unit,
    onResyncClick: () -> Unit,
    onThemeToggleClick: () -> Unit,
    onProfileClick: () -> Unit,
    onRegionMapClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            GlassmorphicTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Tight / 2)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CatchingPokemon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Dexter",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onRegionMapClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Regional Lore Map",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onResyncClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Resync Pokédex",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onThemeToggleClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = when (uiState.themeMode) {
                                AppThemeMode.LIGHT -> Icons.Default.LightMode
                                AppThemeMode.DARK -> Icons.Default.DarkMode
                                AppThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                            },
                            contentDescription = "Toggle Theme",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        val avatarUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${uiState.avatarPokemonId}.png"
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Trainer Profile",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchFilterBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                onOpenFilterPage = onOpenFilterSheet,
                selectedGenerations = uiState.selectedGenerations,
                onGenerationToggle = onGenerationToggle,
                selectedTypes = uiState.selectedTypes,
                onTypeToggle = onTypeToggle,
                selectedSpecialCategories = uiState.selectedSpecialCategories,
                onSpecialCategoryToggle = onSpecialCategoryToggle,
                sortOption = uiState.sortOption,
                sortOrder = uiState.sortOrder,
                onSortOptionReset = onSortOptionReset,
                onClearFilters = onClearFilters,
                totalActiveFilters = uiState.totalActiveFiltersCount
            )

            if (uiState.syncState is SyncState.Syncing && filteredList.isEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 130.dp),
                    contentPadding = PaddingValues(Dimens.ScreenEdgePadding),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Compact),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Compact),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(12) {
                        SkeletonPokemonCard()
                    }
                }
            } else if (filteredList.isEmpty()) {
                EmptySearchResultsState(
                    onClearFilters = onClearFilters,
                    message = if (uiState.pokemonList.isEmpty()) "No Pokémon loaded yet. Tap refresh to sync!" else "No Pokémon match your search or selected filters."
                )
            } else {
                val context = LocalContext.current
                val imageLoader = remember { context.imageLoader }
                val pokemonOfDay = remember(uiState.pokemonList) {
                    if (uiState.pokemonList.isEmpty()) null
                    else {
                        val dayOfYear = java.time.LocalDate.now().dayOfYear
                        val index = (dayOfYear * 37) % uiState.pokemonList.size
                        uiState.pokemonList.getOrNull(index) ?: uiState.pokemonList.firstOrNull()
                    }
                }
                val showHeroCard = uiState.searchQuery.isBlank() &&
                        uiState.selectedTypes.isEmpty() &&
                        uiState.selectedGenerations.isEmpty() &&
                        uiState.selectedSpecialCategories.isEmpty()

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 130.dp),
                    contentPadding = PaddingValues(
                        start = Dimens.ScreenEdgePadding,
                        top = Dimens.ScreenEdgePadding,
                        end = Dimens.ScreenEdgePadding,
                        bottom = 80.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Compact),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Compact),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (showHeroCard && pokemonOfDay != null) {
                        item(
                            span = { GridItemSpan(maxLineSpan) },
                            key = "hero_card_pokemon_of_the_day"
                        ) {
                            PokemonOfDayHeroCard(
                                pokemon = pokemonOfDay,
                                onClick = { onPokemonSelect(pokemonOfDay.id) },
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                modifier = Modifier.padding(bottom = Dimens.Tight / 2)
                            )
                        }
                    }

                    items(
                        items = filteredList,
                        key = { pokemon -> pokemon.id }
                    ) { pokemon ->
                        PokemonCardItem(
                            pokemon = pokemon,
                            isSelected = selectedPokemonId == pokemon.id,
                            onClick = { onPokemonSelect(pokemon.id) },
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PokemonCardItem(
    pokemon: Pokemon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val haptics = com.dexter.app.ui.common.rememberHapticUtils()
    val primarySeed = pokemon.primaryType.seedColor
    val blendedSeed = blendTypeSeedColors(primarySeed, pokemon.secondaryType?.seedColor)
    val cardGradient = Brush.verticalGradient(
        colors = listOf(
            blendedSeed.copy(alpha = 0.22f),
            primarySeed.copy(alpha = 0.08f)
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .bouncyClickable(hapticUtils = haptics, onClick = onClick)
            .holographicShimmer(
                enabled = pokemon.isLegendary || pokemon.isMythical,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = if (isSelected) 2.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else primarySeed.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) Dimens.ElevationLevel2 else Dimens.ElevationLevel1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Tight),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(Dimens.Default))
                    .background(cardGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pokemon.formattedNumber,
                    style = StatNumberStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Dimens.Tight / 2)
                )

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pokemon.officialArtworkUrl ?: pokemon.spriteUrl)
                        .crossfade(true)
                        .size(256)
                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                        .build(),
                    contentDescription = "${pokemon.capitalizedName}, ${pokemon.formattedNumber}, ${pokemon.primaryType.capitalizedName} type",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimens.Tight)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.Tight))

            Text(
                text = pokemon.capitalizedName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
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
        }
    }
}
