package com.dexter.app.data.repository

import com.dexter.app.domain.model.region.LocationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RegionRepositoryTest {

    private lateinit var repository: RegionRepository

    @Before
    fun setUp() {
        repository = RegionRepository()
    }

    @Test
    fun getAllRegions_returnsAll9Regions() {
        val regions = repository.getAllRegions()
        assertEquals(9, regions.size)
        assertEquals("Kanto", regions[0].name)
        assertEquals("Johto", regions[1].name)
        assertEquals("Hoenn", regions[2].name)
        assertEquals("Sinnoh", regions[3].name)
        assertEquals("Unova", regions[4].name)
        assertEquals("Kalos", regions[5].name)
        assertEquals("Alola", regions[6].name)
        assertEquals("Galar", regions[7].name)
        assertEquals("Paldea", regions[8].name)
    }

    @Test
    fun getRegionByNumber_validNumber_returnsCorrectRegion() {
        val kanto = repository.getRegionByNumber(1)
        assertNotNull(kanto)
        assertEquals("Kanto", kanto?.name)
        assertTrue(kanto?.starterIds?.contains(1) == true) // Bulbasaur
        assertTrue(kanto?.starterIds?.contains(4) == true) // Charmander
        assertTrue(kanto?.starterIds?.contains(7) == true) // Squirtle

        val paldea = repository.getRegionByNumber(9)
        assertNotNull(paldea)
        assertEquals("Paldea", paldea?.name)
        assertTrue(paldea?.starterIds?.contains(906) == true) // Sprigatito
    }

    @Test
    fun searchLocations_byPokemonName_findsSpawnLocations() {
        val pikachuSpawns = repository.searchLocations("Pikachu")
        assertTrue(pikachuSpawns.isNotEmpty())
        assertTrue(pikachuSpawns.any { it.second.name.contains("Viridian Forest") })

        val mewtwoSpawns = repository.searchLocations("Mewtwo")
        assertTrue(mewtwoSpawns.isNotEmpty())
        assertTrue(mewtwoSpawns.any { it.second.name.contains("Cerulean Cave") })
    }

    @Test
    fun searchLocations_byLocationName_findsCorrectLocation() {
        val results = repository.searchLocations("Area Zero")
        assertTrue(results.isNotEmpty())
        val match = results.first()
        assertEquals("Paldea", match.first.name)
        assertTrue(match.second.name.contains("Area Zero"))
    }

    @Test
    fun getLocationsForRegion_withFilterType_filtersCorrectly() {
        val legendaries = repository.getLocationsForRegion("kanto", LocationType.LEGENDARY_LAIR)
        assertTrue(legendaries.isNotEmpty())
        assertTrue(legendaries.all { it.type == LocationType.LEGENDARY_LAIR })
        assertTrue(legendaries.any { it.legendary?.pokemonName == "Mewtwo" })
    }
}
