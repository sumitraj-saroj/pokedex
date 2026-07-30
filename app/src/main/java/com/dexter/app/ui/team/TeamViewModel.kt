package com.dexter.app.ui.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dexter.app.data.repository.PokemonRepository
import com.dexter.app.domain.model.Pokemon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamUiState(isLoading = true))
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeTeamMembers(),
                repository.observeAllPokemon()
            ) { teamMap, allPokemon ->
                teamMap to allPokemon
            }.collect { (teamMap, allPokemon) ->
                _uiState.update { current ->
                    current.copy(
                        teamSlots = teamMap,
                        allPokemon = allPokemon,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun openPickerForSlot(slot: Int) {
        _uiState.update { it.copy(activePickerSlot = slot) }
    }

    fun closePicker() {
        _uiState.update { it.copy(activePickerSlot = null) }
    }

    fun selectPokemonForSlot(slot: Int, pokemon: Pokemon) {
        viewModelScope.launch {
            repository.setTeamMember(slot, pokemon.id)
            closePicker()
        }
    }

    fun removeSlot(slot: Int) {
        viewModelScope.launch {
            repository.removeTeamMember(slot)
        }
    }

    fun clearTeam() {
        viewModelScope.launch {
            repository.clearTeam()
        }
    }
}
