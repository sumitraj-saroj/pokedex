package com.dexter.app.domain.battle

import com.dexter.app.domain.battle.engine.CatchContext
import com.dexter.app.domain.battle.engine.CatchRateEngine
import com.dexter.app.domain.battle.model.CatchStatusCondition
import com.dexter.app.domain.battle.model.PokeBallType
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonStats
import com.dexter.app.domain.model.PokemonType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatchRateEngineTest {

    private val caterpie = Pokemon(
        id = 10,
        name = "caterpie",
        number = 10,
        heightM = 0.3,
        weightKg = 2.9,
        primaryType = PokemonType.BUG,
        secondaryType = null,
        stats = PokemonStats(45, 30, 35, 20, 20, 45)
    )

    private val mewtwo = Pokemon(
        id = 150,
        name = "mewtwo",
        number = 150,
        heightM = 2.0,
        weightKg = 122.0,
        primaryType = PokemonType.PSYCHIC,
        secondaryType = null,
        isLegendary = true,
        stats = PokemonStats(106, 110, 90, 154, 90, 130)
    )

    private val snorlax = Pokemon(
        id = 143,
        name = "snorlax",
        number = 143,
        heightM = 2.1,
        weightKg = 460.0,
        primaryType = PokemonType.NORMAL,
        secondaryType = null,
        stats = PokemonStats(160, 110, 65, 65, 110, 30)
    )

    @Test
    fun `test base catch rate lookup`() {
        assertEquals(255, CatchRateEngine.getBaseCatchRate(caterpie))
        assertEquals(3, CatchRateEngine.getBaseCatchRate(mewtwo))
        assertEquals(25, CatchRateEngine.getBaseCatchRate(snorlax))
    }

    @Test
    fun `test Master Ball guarantees 100 percent catch`() {
        val context = CatchContext(
            pokemon = mewtwo,
            currentHpPercent = 100.0,
            activeBall = PokeBallType.MASTER_BALL
        )
        val result = CatchRateEngine.calculateCatchRate(context)
        assertEquals(100.0, result.catchChancePercent, 0.001)
        assertEquals(1.0, result.expectedBalls, 0.001)
    }

    @Test
    fun `test Caterpie full HP standard Poke Ball has very high catch rate`() {
        val context = CatchContext(
            pokemon = caterpie,
            currentHpPercent = 100.0,
            statusCondition = CatchStatusCondition.NONE,
            activeBall = PokeBallType.POKE_BALL
        )
        val result = CatchRateEngine.calculateCatchRate(context)
        // Rate 255 at full HP (1/3 hp factor = 85 modified rate -> approx 33.3% - 40%)
        assertTrue("Caterpie catch rate should be > 30%", result.catchChancePercent > 30.0)
    }

    @Test
    fun `test Sleep and 1 HP drastically increase catch rate for Legendary`() {
        // Mewtwo Full HP, Normal Ball, No Status
        val fullHpContext = CatchContext(
            pokemon = mewtwo,
            currentHpPercent = 100.0,
            statusCondition = CatchStatusCondition.NONE,
            activeBall = PokeBallType.POKE_BALL
        )
        val fullHpResult = CatchRateEngine.calculateCatchRate(fullHpContext)

        // Mewtwo 1% HP (False Swipe), Sleep, Dusk Ball
        val nuzlockeContext = CatchContext(
            pokemon = mewtwo,
            currentHpPercent = 1.0,
            statusCondition = CatchStatusCondition.SLEEP,
            activeBall = PokeBallType.DUSK_BALL,
            isNightOrCave = true
        )
        val nuzlockeResult = CatchRateEngine.calculateCatchRate(nuzlockeContext)

        assertTrue(
            "Nuzlocke conditions should give significantly higher catch rate than full HP",
            nuzlockeResult.catchChancePercent > fullHpResult.catchChancePercent
        )
        assertTrue(
            "Expected balls for nuzlocke condition should be lower",
            nuzlockeResult.expectedBalls < fullHpResult.expectedBalls
        )
    }

    @Test
    fun `test Quick Ball Turn 1 vs Turn 2`() {
        val turn1Context = CatchContext(
            pokemon = mewtwo,
            turnNumber = 1,
            activeBall = PokeBallType.QUICK_BALL
        )
        val turn2Context = CatchContext(
            pokemon = mewtwo,
            turnNumber = 2,
            activeBall = PokeBallType.QUICK_BALL
        )

        val turn1Result = CatchRateEngine.calculateCatchRate(turn1Context)
        val turn2Result = CatchRateEngine.calculateCatchRate(turn2Context)

        assertTrue(
            "Quick Ball Turn 1 should be much higher than Turn 2",
            turn1Result.catchChancePercent > turn2Result.catchChancePercent
        )
    }

    @Test
    fun `test Heavy Ball weight bonus for Snorlax`() {
        // Snorlax is 460kg, gets +40 to base catch rate with Heavy Ball
        val heavyContext = CatchContext(
            pokemon = snorlax,
            activeBall = PokeBallType.HEAVY_BALL
        )
        val standardContext = CatchContext(
            pokemon = snorlax,
            activeBall = PokeBallType.POKE_BALL
        )

        val heavyResult = CatchRateEngine.calculateCatchRate(heavyContext)
        val standardResult = CatchRateEngine.calculateCatchRate(standardContext)

        assertTrue(
            "Heavy Ball should have higher catch rate on Snorlax than standard Poke Ball",
            heavyResult.catchChancePercent > standardResult.catchChancePercent
        )
    }
}
