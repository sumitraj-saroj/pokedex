package com.dexter.app.ui.region

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dexter.app.data.repository.PokemonRepository
import com.dexter.app.data.repository.RegionRepository
import com.dexter.app.domain.model.region.LocationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegionMapViewModel @Inject constructor(
    private val regionRepository: RegionRepository,
    private val pokemonRepository: PokemonRepository,
    private val regionAudioPlayer: RegionAudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RegionMapUiState(
            regions = regionRepository.getAllRegions(),
            selectedRegionNumber = 1,
            selectedRegion = regionRepository.getRegionByNumber(1),
            selectedLocationId = regionRepository.getRegionByNumber(1)?.locations?.firstOrNull()?.id,
            selectedLocation = regionRepository.getRegionByNumber(1)?.locations?.firstOrNull()
        )
    )
    val uiState: StateFlow<RegionMapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            pokemonRepository.observeAllPokemon().collect { list ->
                val map = list.associateBy { it.id }
                _uiState.update { it.copy(allPokemonMap = map) }
            }
        }

        viewModelScope.launch {
            combine(
                regionAudioPlayer.isPlaying,
                regionAudioPlayer.currentTrackTitle
            ) { isPlaying, trackTitle ->
                isPlaying to trackTitle
            }.collect { (isPlaying, trackTitle) ->
                _uiState.update {
                    it.copy(
                        isPlayingAudio = isPlaying,
                        currentPlayingTrack = trackTitle
                    )
                }
            }
        }
    }

    fun selectRegion(regionNumber: Int) {
        val region = regionRepository.getRegionByNumber(regionNumber) ?: return
        val firstLocation = region.locations.firstOrNull()
        regionAudioPlayer.stop()
        _uiState.update { current ->
            current.copy(
                selectedRegionNumber = regionNumber,
                selectedRegion = region,
                selectedLocationId = firstLocation?.id,
                selectedLocation = firstLocation,
                filterType = null
            )
        }
    }

    fun selectLocation(locationId: String?) {
        val currentRegion = _uiState.value.selectedRegion ?: return
        val location = currentRegion.locations.find { it.id == locationId }
        _uiState.update { current ->
            current.copy(
                selectedLocationId = locationId,
                selectedLocation = location
            )
        }
    }

    fun setFilterType(type: LocationType?) {
        _uiState.update { current ->
            val newType = if (current.filterType == type) null else type
            val region = current.selectedRegion
            val filtered = if (newType == null) region?.locations ?: emptyList() else region?.locations?.filter { it.type == newType } ?: emptyList()
            val newSelected = if (filtered.any { it.id == current.selectedLocationId }) {
                current.selectedLocation
            } else {
                filtered.firstOrNull()
            }
            current.copy(
                filterType = newType,
                selectedLocationId = newSelected?.id,
                selectedLocation = newSelected
            )
        }
    }

    fun toggleRegionalTheme() {
        val region = _uiState.value.selectedRegion ?: return
        val url = region.audioThemeUrl
        val title = region.audioThemeTitle.ifBlank { "${region.name} Regional Theme" }
        if (url.isNotBlank()) {
            regionAudioPlayer.togglePlayPause(url, title)
        }
    }

    fun playPokemonCry(pokemonId: Int, pokemonName: String) {
        regionAudioPlayer.playPokemonCry(pokemonId, pokemonName)
    }

    fun stopAudio() {
        regionAudioPlayer.stop()
    }

    fun onSearchQueryChanged(query: String) {
        val results = if (query.isBlank()) {
            emptyList()
        } else {
            regionRepository.searchLocations(query)
        }
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                searchResults = results,
                isSearchActive = query.isNotBlank()
            )
        }
    }

    fun selectSearchResult(regionNumber: Int, locationId: String) {
        selectRegion(regionNumber)
        selectLocation(locationId)
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList(), isSearchActive = false) }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList(), isSearchActive = false) }
    }

    override fun onCleared() {
        super.onCleared()
        regionAudioPlayer.release()
    }
}
