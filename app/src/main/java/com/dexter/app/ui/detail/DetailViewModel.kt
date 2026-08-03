package com.dexter.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dexter.app.data.repository.PokemonRepository
import com.dexter.app.data.repository.TcgCardRepository
import com.dexter.app.data.repository.ThemePreferencesRepository
import com.dexter.app.domain.model.PokemonForm
import com.dexter.app.domain.model.PokemonVariant
import com.dexter.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pokemonRepository: PokemonRepository,
    private val tcgCardRepository: TcgCardRepository,
    private val themePreferencesRepository: ThemePreferencesRepository
) : ViewModel() {

    private val pokemonId: Int = checkNotNull(savedStateHandle["pokemonId"])

    private val _selectedVariant = MutableStateFlow<PokemonVariant>(PokemonVariant.Official)
    private val _retryTcgCardsTrigger = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            pokemonRepository.ensurePokemonDetailExtras(pokemonId)
        }
    }

    private val pokemonFlow = pokemonRepository.observePokemonById(pokemonId)

    private val evolutionsFlow = pokemonFlow.flatMapLatest { pokemon ->
        val chainId = pokemon?.evolutionChainId
        if (chainId != null) {
            pokemonRepository.observeEvolutionChain(chainId)
        } else {
            flowOf(emptyList())
        }
    }

    private val movesFlow = pokemonRepository.observeMovesForPokemon(pokemonId)
    private val abilitiesFlow = pokemonRepository.observeAbilitiesForPokemon(pokemonId)
    private val formsFlow = pokemonRepository.observeFormsForPokemon(pokemonId)

    private val tcgCardsFlow: Flow<TcgCardsUiState> = combine(pokemonFlow, _retryTcgCardsTrigger) { pokemon, _ -> pokemon }
        .flatMapLatest { pokemon ->
            if (pokemon == null) {
                flowOf(TcgCardsUiState.Loading)
            } else {
                tcgCardRepository.getCardsForPokemon(pokemon.capitalizedName).map { resource ->
                    when (resource) {
                        is Resource.Loading -> TcgCardsUiState.Loading
                        is Resource.Success -> {
                            if (resource.data.isEmpty()) TcgCardsUiState.Empty else TcgCardsUiState.Success(resource.data)
                        }
                        is Resource.Error -> TcgCardsUiState.Error(resource.message)
                    }
                }
            }
        }

    val uiState: StateFlow<DetailUiState> = combine(
        pokemonFlow,
        themePreferencesRepository.themeModeFlow,
        _selectedVariant,
        evolutionsFlow,
        movesFlow,
        abilitiesFlow,
        formsFlow,
        tcgCardsFlow
    ) { args ->
        val pokemon = args[0] as? com.dexter.app.domain.model.Pokemon
        val themeMode = args[1] as com.dexter.app.data.repository.AppThemeMode
        val selectedVariant = args[2] as PokemonVariant
        @Suppress("UNCHECKED_CAST")
        val evolutions = args[3] as List<com.dexter.app.domain.model.EvolutionNode>
        @Suppress("UNCHECKED_CAST")
        val moves = args[4] as List<com.dexter.app.domain.model.PokemonMove>
        @Suppress("UNCHECKED_CAST")
        val abilities = args[5] as List<com.dexter.app.domain.model.PokemonAbility>
        @Suppress("UNCHECKED_CAST")
        val forms = args[6] as List<PokemonForm>
        val tcgCardsUiState = args[7] as TcgCardsUiState

        DetailUiState(
            pokemon = pokemon,
            isLoading = pokemon == null,
            themeMode = themeMode,
            selectedVariant = selectedVariant,
            evolutionNodes = evolutions,
            moves = moves,
            abilities = abilities,
            forms = forms,
            tcgCardsUiState = tcgCardsUiState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DetailUiState()
    )

    fun selectVariant(variant: PokemonVariant) {
        _selectedVariant.value = variant
    }

    fun toggleCaught() {
        uiState.value.pokemon?.let { pokemon ->
            val current = pokemon.collection?.isCaught ?: false
            viewModelScope.launch {
                pokemonRepository.toggleCaught(pokemon.id, !current)
            }
        }
    }

    fun toggleFavorite() {
        uiState.value.pokemon?.let { pokemon ->
            val current = pokemon.collection?.isFavorite ?: false
            viewModelScope.launch {
                pokemonRepository.toggleFavorite(pokemon.id, !current)
            }
        }
    }

    fun retryFetchTcgCards() {
        _retryTcgCardsTrigger.value += 1
    }
}
