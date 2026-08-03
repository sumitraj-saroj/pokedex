package com.dexter.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.DarkMode
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.dexter.app.data.repository.AppThemeMode
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.SyncState
import com.dexter.app.ui.common.EmptySearchResultsState
import com.dexter.app.ui.common.SearchFilterBar
import com.dexter.app.ui.common.SkeletonPokemonCard
import com.dexter.app.ui.common.SyncProgressScreen
import com.dexter.app.ui.common.holographicShimmer
import com.dexter.app.ui.common.TypeChip
import com.dexter.app.ui.theme.Dimens
import com.dexter.app.ui.theme.StatNumberStyle

import com.dexter.app.domain.model.SpecialCategory

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dexter.app.ui.filter.FilterBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
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
    modifier: Modifier = Modifier
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

    val filteredList by remember(
        uiState.pokemonList,
        uiState.searchQuery,
        uiState.sortOption,
        uiState.sortOrder,
        uiState.selectedGenerations,
        uiState.selectedTypes,
        uiState.selectedSpecialCategories
    ) {
        derivedStateOf {
            val filtered = uiState.pokemonList.filter { pokemon ->
                val matchesQuery = if (uiState.searchQuery.isBlank()) {
                    true
                } else {
                    val q = uiState.searchQuery.trim().lowercase()
                    val matchesName = pokemon.name.lowercase().contains(q)
                    val matchesNumber = pokemon.number.toString().contains(q) || pokemon.formattedNumber.lowercase().contains(q)
                    matchesName || matchesNumber
                }

                val matchesGen = if (uiState.selectedGenerations.isEmpty()) {
                    true
                } else {
                    uiState.selectedGenerations.contains(pokemon.effectiveGeneration)
                }

                val matchesType = if (uiState.selectedTypes.isEmpty()) {
                    true
                } else {
                    uiState.selectedTypes.any { requiredType ->
                        pokemon.primaryType == requiredType || pokemon.secondaryType == requiredType
                    }
                }

                val matchesSpecial = if (uiState.selectedSpecialCategories.isEmpty()) {
                    true
                } else {
                    uiState.selectedSpecialCategories.any { category ->
                        category.matches(pokemon)
                    }
                }

                matchesQuery && matchesGen && matchesType && matchesSpecial
            }

            val comparator = Comparator<com.dexter.app.domain.model.Pokemon> { a, b ->
                when (uiState.sortOption) {
                    SortOption.NUMBER -> a.number.compareTo(b.number)
                    SortOption.NAME -> a.name.compareTo(b.name, ignoreCase = true)
                    SortOption.TOTAL_STATS -> (a.stats?.total ?: 0).compareTo(b.stats?.total ?: 0)
                    SortOption.HEIGHT -> a.heightM.compareTo(b.heightM)
                    SortOption.WEIGHT -> a.weightKg.compareTo(b.weightKg)
                }
            }

            if (uiState.sortOrder == SortOrder.DESCENDING) {
                filtered.sortedWith(comparator.reversed())
            } else {
                filtered.sortedWith(comparator)
            }
        }
    }

    if (uiState.syncState is SyncState.Syncing && uiState.pokemonList.isEmpty()) {
        SyncProgressScreen(
            current = (uiState.syncState as SyncState.Syncing).current,
            total = (uiState.syncState as SyncState.Syncing).total
        )
        return
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
                            imageVector = Icons.Default.CatchingPokemon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                onOpenFilterPage = { showFilterSheet = true },
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

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 130.dp),
                    contentPadding = PaddingValues(Dimens.ScreenEdgePadding),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Compact),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Compact),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = filteredList,
                        key = { it.id }
                    ) { pokemon ->
                        androidx.compose.runtime.LaunchedEffect(pokemon.id) {
                            val currentIndex = filteredList.indexOf(pokemon)
                            if (currentIndex >= 0) {
                                for (i in 1..8) {
                                    val nextPokemon = filteredList.getOrNull(currentIndex + i)
                                    if (nextPokemon != null) {
                                        val url = nextPokemon.officialArtworkUrl ?: nextPokemon.spriteUrl
                                        if (!url.isNullOrEmpty()) {
                                            val req = ImageRequest.Builder(context).data(url).build()
                                            imageLoader.enqueue(req)
                                        }
                                    }
                                }
                            }
                        }

                        PokemonCardItem(
                            pokemon = pokemon,
                            onClick = { onPokemonClick(pokemon.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PokemonCardItem(
    pokemon: Pokemon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = com.dexter.app.ui.common.rememberHapticUtils()
    val cardColor = pokemon.primaryType.seedColor.copy(alpha = 0.15f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                haptics.selectionTick()
                onClick()
            }
            .holographicShimmer(enabled = pokemon.isLegendary || pokemon.isMythical),
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(Dimens.Compact))
                    .background(cardColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pokemon.formattedNumber,
                    style = StatNumberStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Dimens.Tight / 2)
                )

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pokemon.officialArtworkUrl ?: pokemon.spriteUrl)
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
                text = pokemon.capitalizedName,
                style = MaterialTheme.typography.labelLarge,
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
        }
    }
}
