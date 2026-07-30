package com.dexter.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dexter.app.data.repository.AppThemeMode
import com.dexter.app.data.repository.PokemonRepository
import com.dexter.app.data.repository.ThemePreferencesRepository
import com.dexter.app.data.repository.TrainerPreferencesRepository
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.domain.model.SpecialCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import androidx.lifecycle.SavedStateHandle

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val pokemonRepository: PokemonRepository,
    private val themePreferencesRepository: ThemePreferencesRepository,
    private val trainerPreferencesRepository: TrainerPreferencesRepository
) : ViewModel() {

    val searchQuery = savedStateHandle.getStateFlow("search_query", "")
    private val selectedSortOption = MutableStateFlow(SortOption.NUMBER)
    private val selectedSortOrder = MutableStateFlow(SortOrder.ASCENDING)
    private val selectedGenerations = MutableStateFlow<Set<Int>>(emptySet())
    private val selectedTypes = MutableStateFlow<Set<PokemonType>>(emptySet())
    private val selectedSpecialCategories = MutableStateFlow<Set<SpecialCategory>>(emptySet())

    private data class FilterState(
        val query: String,
        val sortOption: SortOption,
        val sortOrder: SortOrder,
        val generations: Set<Int>,
        val types: Set<PokemonType>,
        val specialCategories: Set<SpecialCategory>
    )

    private val filterStateFlow = kotlinx.coroutines.flow.combine(
        searchQuery,
        selectedSortOption,
        selectedSortOrder,
        selectedGenerations,
        selectedTypes,
        selectedSpecialCategories
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        FilterState(
            query = args[0] as String,
            sortOption = args[1] as SortOption,
            sortOrder = args[2] as SortOrder,
            generations = args[3] as Set<Int>,
            types = args[4] as Set<PokemonType>,
            specialCategories = args[5] as Set<SpecialCategory>
        )
    }

    val uiState: StateFlow<HomeUiState> = com.dexter.app.util.combine(
        pokemonRepository.observeAllPokemon(),
        pokemonRepository.syncState,
        filterStateFlow,
        themePreferencesRepository.themeModeFlow,
        trainerPreferencesRepository.trainerDataFlow
    ) { allPokemon, syncState, filterState, themeMode, trainerData ->
        HomeUiState(
            pokemonList = allPokemon,
            syncState = syncState,
            searchQuery = filterState.query,
            sortOption = filterState.sortOption,
            sortOrder = filterState.sortOrder,
            selectedGenerations = filterState.generations,
            selectedTypes = filterState.types,
            selectedSpecialCategories = filterState.specialCategories,
            themeMode = themeMode,
            avatarPokemonId = trainerData.avatarPokemonId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        viewModelScope.launch {
            pokemonRepository.syncPokemonData(forceResync = false)
        }
    }

    fun onSearchQueryChanged(query: String) {
        savedStateHandle["search_query"] = query
    }

    fun setSortOption(option: SortOption) {
        selectedSortOption.value = option
    }

    fun setSortOrder(order: SortOrder) {
        selectedSortOrder.value = order
    }

    fun toggleGenerationFilter(generation: Int) {
        val current = selectedGenerations.value
        selectedGenerations.value = if (current.contains(generation)) {
            current - generation
        } else {
            current + generation
        }
    }

    fun toggleTypeFilter(type: PokemonType) {
        val current = selectedTypes.value
        selectedTypes.value = if (current.contains(type)) {
            current - type
        } else {
            current + type
        }
    }

    fun toggleSpecialCategory(category: SpecialCategory) {
        val current = selectedSpecialCategories.value
        selectedSpecialCategories.value = if (current.contains(category)) {
            current - category
        } else {
            current + category
        }
    }

    fun clearFilters() {
        savedStateHandle["search_query"] = ""
        selectedSortOption.value = SortOption.NUMBER
        selectedSortOrder.value = SortOrder.ASCENDING
        selectedGenerations.value = emptySet()
        selectedTypes.value = emptySet()
        selectedSpecialCategories.value = emptySet()
    }

    fun triggerResync() {
        viewModelScope.launch {
            pokemonRepository.syncPokemonData(forceResync = true)
        }
    }

    fun toggleThemeMode() {
        viewModelScope.launch {
            val nextMode = when (uiState.value.themeMode) {
                AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
                AppThemeMode.LIGHT -> AppThemeMode.DARK
                AppThemeMode.DARK -> AppThemeMode.SYSTEM
            }
            themePreferencesRepository.setThemeMode(nextMode)
        }
    }
}
