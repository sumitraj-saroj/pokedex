package com.dexter.app.ui.region

import com.dexter.app.data.repository.RegionData
import com.dexter.app.data.repository.RegionRepository
import com.dexter.app.domain.model.region.LocationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RegionMapTest {

    private lateinit var regionRepository: RegionRepository

    @Before
    fun setUp() {
        regionRepository = RegionRepository()
    }

    @Test
    fun testAllNineRegionsPresentWithData() {
        val regions = regionRepository.getAllRegions()
        assertEquals(9, regions.size)

        for (i in 1..9) {
            val region = regionRepository.getRegionByNumber(i)
            assertNotNull("Region $i should exist", region)
            assertTrue("Region $i should have locations", region!!.locations.isNotEmpty())
            assertTrue("Region $i should have starters", region.starterIds.isNotEmpty())
            assertTrue("Region $i should have legendaries", region.legendaryIds.isNotEmpty())
            assertTrue("Region $i should have description", region.description.isNotBlank())
            assertTrue("Region $i should have music theme", region.musicTheme.isNotBlank())
        }
    }

    @Test
    fun testKantoLocationsAndSpawns() {
        val kanto = regionRepository.getRegionById("kanto")
        assertNotNull(kanto)

        val palletTown = kanto?.locations?.find { it.id == "pallet_town" }
        assertNotNull(palletTown)
        assertTrue(palletTown!!.wildSpawns.any { it.pokemonName == "Pidgey" })

        val ceruleanCave = kanto?.locations?.find { it.id == "cerulean_cave" }
        assertNotNull(ceruleanCave)
        assertEquals(150, ceruleanCave?.legendary?.pokemonId)
        assertEquals("Mewtwo", ceruleanCave?.legendary?.pokemonName)

        val powerPlant = kanto?.locations?.find { it.id == "power_plant" }
        assertEquals(145, powerPlant?.legendary?.pokemonId)
    }

    @Test
    fun testJohtoLocationsAndSpawns() {
        val johto = regionRepository.getRegionById("johto")
        assertNotNull(johto)

        val bellTower = johto?.locations?.find { it.id == "bell_tower" }
        assertNotNull(bellTower)
        assertEquals(250, bellTower?.legendary?.pokemonId) // Ho-Oh

        val whirlIslands = johto?.locations?.find { it.id == "whirl_islands" }
        assertNotNull(whirlIslands)
        assertEquals(249, whirlIslands?.legendary?.pokemonId) // Lugia
    }

    @Test
    fun testHoennAndSinnohApexLocations() {
        val hoenn = regionRepository.getRegionById("hoenn")
        assertNotNull(hoenn)
        val skyPillar = hoenn?.locations?.find { it.id == "sky_pillar" }
        assertEquals("Rayquaza", skyPillar?.legendary?.pokemonName)

        val sinnoh = regionRepository.getRegionById("sinnoh")
        assertNotNull(sinnoh)
        val spearPillar = sinnoh?.locations?.find { it.id == "spear_pillar" }
        assertTrue(spearPillar?.legendary?.pokemonName?.contains("Dialga") == true)
    }

    @Test
    fun testPaldeaAndAreaZero() {
        val paldea = regionRepository.getRegionById("paldea")
        assertNotNull(paldea)
        val areaZero = paldea?.locations?.find { it.id == "area_zero" }
        assertNotNull(areaZero)
        assertTrue(areaZero!!.wildSpawns.any { it.pokemonName == "Roaring Moon" })
        assertTrue(areaZero.wildSpawns.any { it.pokemonName == "Iron Valiant" })
    }

    @Test
    fun testSearchLocationsAcrossRegions() {
        val results = regionRepository.searchLocations("Rayquaza")
        assertTrue(results.isNotEmpty())
        assertEquals("Hoenn", results.first().first.name)
        assertEquals("sky_pillar", results.first().second.id)
    }

    @Test
    fun testUiStateDisplayedLocationsFilter() {
        val kanto = regionRepository.getRegionByNumber(1)!!
        val stateAll = RegionMapUiState(
            selectedRegion = kanto,
            filterType = null
        )
        assertEquals(kanto.locations.size, stateAll.displayedLocations.size)

        val stateLegendaries = RegionMapUiState(
            selectedRegion = kanto,
            filterType = LocationType.LEGENDARY_LAIR
        )
        assertTrue(stateLegendaries.displayedLocations.all { it.type == LocationType.LEGENDARY_LAIR })
        assertTrue(stateLegendaries.displayedLocations.any { it.name == "Cerulean Cave" })
    }
}
