package com.dexter.app.ui.compare

import com.dexter.app.domain.model.Pokemon

enum class CompareTarget { NONE, POKEMON_A, POKEMON_B }

data class CompareUiState(
    val pokemonA: Pokemon? = null,
    val pokemonB: Pokemon? = null,
    val allPokemon: List<Pokemon> = emptyList(),
    val activePickerTarget: CompareTarget = CompareTarget.NONE,
    val isLoading: Boolean = false
)
