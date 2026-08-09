package com.dexter.app.ui.home

import androidx.compose.runtime.Immutable
import com.dexter.app.data.repository.AppThemeMode
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.domain.model.SpecialCategory
import com.dexter.app.domain.model.SyncState

enum class SortOption(val displayName: String) {
    NUMBER("Pokédex #"),
    NAME("Name"),
    TOTAL_STATS("Total Stats"),
    HEIGHT("Height"),
    WEIGHT("Weight")
}

enum class SortOrder(val displayName: String) {
    ASCENDING("Ascending"),
    DESCENDING("Descending")
}

@Immutable
data class HomeUiState(
    val pokemonList: List<Pokemon> = emptyList(),
    val filteredList: List<Pokemon> = emptyList(),
    val syncState: SyncState = SyncState.Idle,
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.NUMBER,
    val sortOrder: SortOrder = SortOrder.ASCENDING,
    val selectedGenerations: Set<Int> = emptySet(),
    val selectedTypes: Set<PokemonType> = emptySet(),
    val selectedSpecialCategories: Set<SpecialCategory> = emptySet(),
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val avatarPokemonId: Int = 25
) {
    val totalActiveFiltersCount: Int
        get() = selectedGenerations.size + selectedTypes.size + selectedSpecialCategories.size + (if (sortOption != SortOption.NUMBER || sortOrder != SortOrder.ASCENDING) 1 else 0)
}

