package com.dexter.app.ui.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dexter.app.data.repository.PokemonRepository
import com.dexter.app.domain.model.Pokemon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompareViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompareUiState(isLoading = true))
    val uiState: StateFlow<CompareUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAllPokemon().collect { list ->
                _uiState.update { current ->
                    val first = current.pokemonA ?: list.firstOrNull()
                    val second = current.pokemonB ?: list.getOrNull(3) ?: list.getOrNull(1)

                    current.copy(
                        pokemonA = first,
                        pokemonB = second,
                        allPokemon = list,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun openPicker(target: CompareTarget) {
        _uiState.update { it.copy(activePickerTarget = target) }
    }

    fun closePicker() {
        _uiState.update { it.copy(activePickerTarget = CompareTarget.NONE) }
    }

    fun selectPokemon(pokemon: Pokemon) {
        _uiState.update { current ->
            when (current.activePickerTarget) {
                CompareTarget.POKEMON_A -> current.copy(pokemonA = pokemon, activePickerTarget = CompareTarget.NONE)
                CompareTarget.POKEMON_B -> current.copy(pokemonB = pokemon, activePickerTarget = CompareTarget.NONE)
                CompareTarget.NONE -> current
            }
        }
    }

    fun swapPokemon() {
        _uiState.update { current ->
            current.copy(
                pokemonA = current.pokemonB,
                pokemonB = current.pokemonA
            )
        }
    }
}
