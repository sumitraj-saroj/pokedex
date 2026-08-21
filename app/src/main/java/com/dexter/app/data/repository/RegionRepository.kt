package com.dexter.app.data.repository

import com.dexter.app.domain.model.region.LocationType
import com.dexter.app.domain.model.region.Region
import com.dexter.app.domain.model.region.RegionLocation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegionRepository @Inject constructor() {

    fun getAllRegions(): List<Region> = RegionData.ALL_REGIONS

    fun getRegionByNumber(number: Int): Region? = RegionData.getRegionByNumber(number)

    fun getRegionById(id: String): Region? = RegionData.getRegionById(id)

    fun searchLocations(query: String, regionNumber: Int? = null): List<Pair<Region, RegionLocation>> {
        val q = query.trim().lowercase()
        val regionsToSearch = if (regionNumber != null) {
            listOfNotNull(RegionData.getRegionByNumber(regionNumber))
        } else {
            RegionData.ALL_REGIONS
        }

        if (q.isBlank()) return emptyList()

        val results = mutableListOf<Pair<Region, RegionLocation>>()
        for (region in regionsToSearch) {
            for (location in region.locations) {
                val matchesLocationName = location.name.lowercase().contains(q)
                val matchesDescription = location.description.lowercase().contains(q)
                val matchesGymLeader = location.gymLeader?.let {
                    it.name.lowercase().contains(q) || it.acePokemonName.lowercase().contains(q)
                } ?: false
                val matchesLegendary = location.legendary?.let {
                    it.pokemonName.lowercase().contains(q)
                } ?: false
                val matchesSpawn = location.wildSpawns.any {
                    it.pokemonName.lowercase().contains(q) || it.pokemonId.toString() == q
                }

                if (matchesLocationName || matchesDescription || matchesGymLeader || matchesLegendary || matchesSpawn) {
                    results.add(region to location)
                }
            }
        }
        return results
    }

    fun getLocationsForRegion(regionId: String, filterType: LocationType? = null): List<RegionLocation> {
        val region = RegionData.getRegionById(regionId) ?: return emptyList()
        if (filterType == null) return region.locations
        return region.locations.filter { it.type == filterType }
    }
}
