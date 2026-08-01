package com.dexter.app.domain.mapper

import com.dexter.app.domain.model.PokemonType
import org.junit.Assert.assertEquals
import org.junit.Test

class PokemonMapperTest {

    @Test
    fun `test type parsing maps valid string to PokemonType`() {
        val type = PokemonType.fromString("fire")
        assertEquals(PokemonType.FIRE, type)
    }

    @Test
    fun `test type parsing maps unknown string to NORMAL`() {
        val type = PokemonType.fromString("unknown_type")
        assertEquals(PokemonType.NORMAL, type)
    }
}
