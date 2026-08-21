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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

import androidx.lifecycle.SavedStateHandle

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val pokemonRepository: PokemonRepository,
    private val themePreferencesRepository: ThemePreferencesRepository,
    private val trainerPreferencesRepository: TrainerPreferencesRepository
) : ViewModel() {

    val searchQuery = savedStateHandle.getStateFlow("search_query", "")
    val selectedSortOption = savedStateHandle.getStateFlow("sort_option", SortOption.NUMBER)
    val selectedSortOrder = savedStateHandle.getStateFlow("sort_order", SortOrder.ASCENDING)
    val selectedGenerations = savedStateHandle.getStateFlow("selected_generations", emptySet<Int>())
    val selectedTypes = savedStateHandle.getStateFlow("selected_types", emptySet<PokemonType>())
    val selectedSpecialCategories = savedStateHandle.getStateFlow("selected_categories", emptySet<SpecialCategory>())

    @OptIn(FlowPreview::class)
    private val debouncedSearchQuery = searchQuery.debounce { query ->
        if (query.isEmpty()) 0L else 150L
    }

    private data class FilterState(
        val query: String,
        val sortOption: SortOption,
        val sortOrder: SortOrder,
        val generations: Set<Int>,
        val types: Set<PokemonType>,
        val specialCategories: Set<SpecialCategory>
    )

    private val filterStateFlow = kotlinx.coroutines.flow.combine(
        debouncedSearchQuery,
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
        val list = allPokemon.filter { pokemon ->
            val matchesQuery = if (filterState.query.isBlank()) {
                true
            } else {
                val q = filterState.query.trim().lowercase()
                val matchesName = pokemon.name.lowercase().contains(q)
                val matchesNumber = pokemon.number.toString().contains(q) || pokemon.formattedNumber.lowercase().contains(q)
                matchesName || matchesNumber
            }

            val matchesGen = if (filterState.generations.isEmpty()) {
                true
            } else {
                filterState.generations.contains(pokemon.effectiveGeneration)
            }

            val matchesType = if (filterState.types.isEmpty()) {
                true
            } else {
                filterState.types.any { requiredType ->
                    pokemon.primaryType == requiredType || pokemon.secondaryType == requiredType
                }
            }

            val matchesSpecial = if (filterState.specialCategories.isEmpty()) {
                true
            } else {
                filterState.specialCategories.any { category ->
                    category.matches(pokemon)
                }
            }

            matchesQuery && matchesGen && matchesType && matchesSpecial
        }

        val comparator = Comparator<com.dexter.app.domain.model.Pokemon> { a, b ->
            when (filterState.sortOption) {
                SortOption.NUMBER -> a.number.compareTo(b.number)
                SortOption.NAME -> a.name.compareTo(b.name, ignoreCase = true)
                SortOption.TOTAL_STATS -> (a.stats?.total ?: 0).compareTo(b.stats?.total ?: 0)
                SortOption.HEIGHT -> a.heightM.compareTo(b.heightM)
                SortOption.WEIGHT -> a.weightKg.compareTo(b.weightKg)
            }
        }

        val filtered = if (filterState.sortOrder == SortOrder.DESCENDING) {
            list.sortedWith(comparator.reversed())
        } else {
            list.sortedWith(comparator)
        }

        HomeUiState(
            pokemonList = allPokemon,
            filteredList = filtered,
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
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        viewModelScope.launch {
            pokemonRepository.syncPokemonData(forceResync = false)
            com.dexter.app.widget.DexterWidgetManager.updateAllWidgets(context)
        }
    }

    fun onSearchQueryChanged(query: String) {
        savedStateHandle["search_query"] = query
    }

    fun setSortOption(option: SortOption) {
        savedStateHandle["sort_option"] = option
    }

    fun setSortOrder(order: SortOrder) {
        savedStateHandle["sort_order"] = order
    }

    fun toggleGenerationFilter(generation: Int) {
        val current = selectedGenerations.value
        savedStateHandle["selected_generations"] = if (current.contains(generation)) {
            current - generation
        } else {
            current + generation
        }
    }

    fun toggleTypeFilter(type: PokemonType) {
        val current = selectedTypes.value
        savedStateHandle["selected_types"] = if (current.contains(type)) {
            current - type
        } else {
            current + type
        }
    }

    fun toggleSpecialCategory(category: SpecialCategory) {
        val current = selectedSpecialCategories.value
        savedStateHandle["selected_categories"] = if (current.contains(category)) {
            current - category
        } else {
            current + category
        }
    }

    fun clearFilters() {
        savedStateHandle["search_query"] = ""
        savedStateHandle["sort_option"] = SortOption.NUMBER
        savedStateHandle["sort_order"] = SortOrder.ASCENDING
        savedStateHandle["selected_generations"] = emptySet<Int>()
        savedStateHandle["selected_types"] = emptySet<PokemonType>()
        savedStateHandle["selected_categories"] = emptySet<SpecialCategory>()
    }

    fun triggerResync() {
        viewModelScope.launch {
            pokemonRepository.syncPokemonData(forceResync = true)
        }
    }

    fun toggleThemeMode() {
        viewModelScope.launch {
            val nextMode = when (uiState.value.themeMode) {
                AppThemeMode.LIGHT -> AppThemeMode.DARK
                AppThemeMode.DARK -> AppThemeMode.LIGHT
                AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
            }
            themePreferencesRepository.setThemeMode(nextMode)
        }
    }
}
