package com.dexter.app.ui.quiz

import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizGenerationFilterTest {

    private fun createPokemon(id: Int, name: String, number: Int): Pokemon {
        return Pokemon(
            id = id,
            name = name,
            number = number,
            heightM = 1.0,
            weightKg = 10.0,
            primaryType = PokemonType.NORMAL
        )
    }

    private val samplePokemonList = listOf(
        createPokemon(1, "Bulbasaur", 1),       // Gen 1
        createPokemon(25, "Pikachu", 25),       // Gen 1
        createPokemon(150, "Mewtwo", 150),     // Gen 1
        createPokemon(152, "Chikorita", 152),  // Gen 2
        createPokemon(250, "Ho-Oh", 250),      // Gen 2
        createPokemon(252, "Treecko", 252),    // Gen 3
        createPokemon(384, "Rayquaza", 384),   // Gen 3
        createPokemon(387, "Turtwig", 387),    // Gen 4
        createPokemon(493, "Arceus", 493),     // Gen 4
        createPokemon(494, "Victini", 494),    // Gen 5
        createPokemon(650, "Chespin", 650),    // Gen 6
        createPokemon(722, "Rowlet", 722),     // Gen 7
        createPokemon(810, "Grookey", 810),    // Gen 8
        createPokemon(906, "Sprigatito", 906)  // Gen 9
    )

    private fun filterPool(pokemonList: List<Pokemon>, selectedGenerations: Set<Int>): List<Pokemon> {
        return if (selectedGenerations.isEmpty() || selectedGenerations.size == 9) {
            pokemonList
        } else {
            pokemonList.filter { selectedGenerations.contains(it.effectiveGeneration) }
        }
    }

    @Test
    fun `test all generations selected by default when empty`() {
        val pool = filterPool(samplePokemonList, emptySet())
        assertEquals(samplePokemonList.size, pool.size)
    }

    @Test
    fun `test filtering by gen 1 only`() {
        val pool = filterPool(samplePokemonList, setOf(1))
        assertEquals(3, pool.size)
        assertTrue(pool.all { it.effectiveGeneration == 1 })
        assertTrue(pool.any { it.name == "Bulbasaur" })
        assertTrue(pool.any { it.name == "Pikachu" })
        assertTrue(pool.any { it.name == "Mewtwo" })
        assertFalse(pool.any { it.name == "Chikorita" })
    }

    @Test
    fun `test filtering by gen 1 and gen 2`() {
        val pool = filterPool(samplePokemonList, setOf(1, 2))
        assertEquals(5, pool.size)
        assertTrue(pool.all { it.effectiveGeneration == 1 || it.effectiveGeneration == 2 })
        assertTrue(pool.any { it.name == "Bulbasaur" })
        assertTrue(pool.any { it.name == "Pikachu" })
        assertTrue(pool.any { it.name == "Mewtwo" })
        assertTrue(pool.any { it.name == "Chikorita" })
        assertTrue(pool.any { it.name == "Ho-Oh" })
        assertFalse(pool.any { it.name == "Treecko" })
        assertFalse(pool.any { it.name == "Sprigatito" })
    }

    @Test
    fun `test preset classic generations 1 to 3`() {
        val classicGens = setOf(1, 2, 3)
        val pool = filterPool(samplePokemonList, classicGens)
        assertEquals(7, pool.size)
        assertTrue(pool.all { it.effectiveGeneration in 1..3 })
        assertFalse(pool.any { it.effectiveGeneration > 3 })
    }

    @Test
    fun `test preset modern generations 7 to 9`() {
        val modernGens = setOf(7, 8, 9)
        val pool = filterPool(samplePokemonList, modernGens)
        assertEquals(3, pool.size)
        assertTrue(pool.all { it.effectiveGeneration in 7..9 })
        assertFalse(pool.any { it.effectiveGeneration < 7 })
    }

    @Test
    fun `test quiz ui state isAllGenerationsSelected helper`() {
        val stateDefault = QuizUiState()
        assertTrue(stateDefault.isAllGenerationsSelected)

        val stateGen12 = QuizUiState(selectedGenerations = setOf(1, 2))
        assertFalse(stateGen12.isAllGenerationsSelected)

        val stateAll9 = QuizUiState(selectedGenerations = (1..9).toSet())
        assertTrue(stateAll9.isAllGenerationsSelected)
    }
}
