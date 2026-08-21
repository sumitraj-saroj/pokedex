package com.dexter.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PokemonGenerationTest {

    @Test
    fun `test all 9 generations are defined with valid metadata`() {
        assertEquals(9, PokemonGeneration.ALL.size)

        val gen1 = PokemonGeneration.fromNumber(1)
        assertNotNull(gen1)
        assertEquals("Kanto", gen1?.regionName)
        assertEquals(1, gen1?.minId)
        assertEquals(151, gen1?.maxId)
        assertEquals("Gen 1", gen1?.displayName)
        assertEquals("Gen 1 (Kanto)", gen1?.fullDisplayName)

        val gen2 = PokemonGeneration.fromNumber(2)
        assertNotNull(gen2)
        assertEquals("Johto", gen2?.regionName)
        assertEquals(152, gen2?.minId)
        assertEquals(251, gen2?.maxId)

        val gen9 = PokemonGeneration.fromNumber(9)
        assertNotNull(gen9)
        assertEquals("Paldea", gen9?.regionName)
        assertEquals(906, gen9?.minId)
        assertEquals(1025, gen9?.maxId)
    }

    @Test
    fun `test invalid generation number returns null`() {
        assertNull(PokemonGeneration.fromNumber(0))
        assertNull(PokemonGeneration.fromNumber(10))
        assertNull(PokemonGeneration.fromNumber(-1))
    }

    @Test
    fun `test contiguous generation dex ranges`() {
        for (i in 0 until PokemonGeneration.ALL.size - 1) {
            val current = PokemonGeneration.ALL[i]
            val next = PokemonGeneration.ALL[i + 1]
            assertEquals(current.maxId + 1, next.minId)
        }
    }
}
