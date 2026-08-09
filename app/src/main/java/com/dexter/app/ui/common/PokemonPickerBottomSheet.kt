package com.dexter.app.ui.common

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.ui.home.PokemonCardItem
import com.dexter.app.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PokemonPickerBottomSheet(
    onDismissRequest: () -> Unit,
    onPokemonSelected: (Pokemon) -> Unit,
    pokemonList: List<Pokemon>,
    title: String = "Select a Pokémon",
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var searchQuery by remember { mutableStateOf("") }
    var selectedGenerations by remember { mutableStateOf(emptySet<Int>()) }
    var selectedTypes by remember { mutableStateOf(emptySet<PokemonType>()) }

    val filteredList by remember(pokemonList, searchQuery, selectedGenerations, selectedTypes) {
        derivedStateOf {
            pokemonList.filter { pokemon ->
                val matchesQuery = if (searchQuery.isBlank()) {
                    true
                } else {
                    val q = searchQuery.trim().lowercase()
                    val matchesName = pokemon.name.lowercase().contains(q)
                    val matchesNumber = pokemon.number.toString().contains(q) || pokemon.formattedNumber.lowercase().contains(q)
                    matchesName || matchesNumber
                }

                val matchesGen = if (selectedGenerations.isEmpty()) {
                    true
                } else {
                    selectedGenerations.contains(pokemon.effectiveGeneration)
                }

                val matchesType = if (selectedTypes.isEmpty()) {
                    true
                } else {
                    selectedTypes.any { requiredType ->
                        pokemon.primaryType == requiredType || pokemon.secondaryType == requiredType
                    }
                }

                matchesQuery && matchesGen && matchesType
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenEdgePadding, vertical = Dimens.Tight),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close picker"
                    )
                }
            }

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onOpenFilterPage = { /* Picker filter toggle */ },
                selectedGenerations = selectedGenerations,
                onGenerationToggle = { gen ->
                    selectedGenerations = if (selectedGenerations.contains(gen)) {
                        selectedGenerations - gen
                    } else {
                        selectedGenerations + gen
                    }
                },
                selectedTypes = selectedTypes,
                onTypeToggle = { type ->
                    selectedTypes = if (selectedTypes.contains(type)) {
                        selectedTypes - type
                    } else {
                        selectedTypes + type
                    }
                },
                selectedSpecialCategories = emptySet(),
                onSpecialCategoryToggle = { },
                onClearFilters = {
                    searchQuery = ""
                    selectedGenerations = emptySet()
                    selectedTypes = emptySet()
                }
            )

            Spacer(modifier = Modifier.height(Dimens.Tight))

            if (filteredList.isEmpty()) {
                EmptySearchResultsState(
                    onClearFilters = {
                        searchQuery = ""
                        selectedGenerations = emptySet()
                        selectedTypes = emptySet()
                    },
                    message = "No Pokémon match your search or selected filters."
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 130.dp),
                    contentPadding = PaddingValues(Dimens.ScreenEdgePadding),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Compact),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Compact),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(
                        items = filteredList,
                        key = { it.id }
                    ) { pokemon ->
                        PokemonCardItem(
                            pokemon = pokemon,
                            onClick = {
                                onPokemonSelected(pokemon)
                                onDismissRequest()
                            }
                        )
                    }
                }
            }
        }
    }
}
