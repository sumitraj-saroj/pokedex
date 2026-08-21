package com.dexter.app.ui.region

import androidx.compose.runtime.Immutable
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.region.LocationType
import com.dexter.app.domain.model.region.MapStyle
import com.dexter.app.domain.model.region.Region
import com.dexter.app.domain.model.region.RegionLocation

@Immutable
data class RegionMapUiState(
    val regions: List<Region> = emptyList(),
    val selectedRegionNumber: Int = 1,
    val selectedRegion: Region? = null,
    val selectedLocationId: String? = null,
    val selectedLocation: RegionLocation? = null,
    val filterType: LocationType? = null,
    val mapStyle: MapStyle = MapStyle.PIXEL_RETRO,
    val searchQuery: String = "",
    val searchResults: List<Pair<Region, RegionLocation>> = emptyList(),
    val allPokemonMap: Map<Int, Pokemon> = emptyMap(),
    val isSearchActive: Boolean = false,
    val isPlayingAudio: Boolean = false,
    val currentPlayingTrack: String? = null
) {
    val displayedLocations: List<RegionLocation>
        get() {
            val list = selectedRegion?.locations ?: emptyList()
            return if (filterType == null) list else list.filter { it.type == filterType }
        }
}
