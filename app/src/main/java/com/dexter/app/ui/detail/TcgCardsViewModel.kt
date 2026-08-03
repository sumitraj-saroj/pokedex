package com.dexter.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dexter.app.data.repository.TcgCardRepository
import com.dexter.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TcgCardsViewModel @Inject constructor(
    private val tcgCardRepository: TcgCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TcgCardsUiState>(TcgCardsUiState.Loading)
    val uiState: StateFlow<TcgCardsUiState> = _uiState.asStateFlow()

    private var currentPokemonName: String? = null

    fun loadCardsForPokemon(pokemonName: String) {
        if (currentPokemonName == pokemonName && _uiState.value !is TcgCardsUiState.Error) {
            return
        }
        currentPokemonName = pokemonName
        fetchCards(pokemonName)
    }

    fun retry() {
        currentPokemonName?.let { fetchCards(it) }
    }

    private fun fetchCards(pokemonName: String) {
        viewModelScope.launch {
            tcgCardRepository.getCardsForPokemon(pokemonName).collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> TcgCardsUiState.Loading
                    is Resource.Success -> {
                        if (resource.data.isEmpty()) {
                            TcgCardsUiState.Empty
                        } else {
                            TcgCardsUiState.Success(resource.data)
                        }
                    }
                    is Resource.Error -> TcgCardsUiState.Error(resource.message)
                }
            }
        }
    }
}
