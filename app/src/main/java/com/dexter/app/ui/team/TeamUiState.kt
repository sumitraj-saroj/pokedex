package com.dexter.app.ui.team

import com.dexter.app.domain.model.Pokemon

data class TeamUiState(
    val teamSlots: Map<Int, Pokemon> = emptyMap(),
    val allPokemon: List<Pokemon> = emptyList(),
    val activePickerSlot: Int? = null,
    val isLoading: Boolean = false
)
