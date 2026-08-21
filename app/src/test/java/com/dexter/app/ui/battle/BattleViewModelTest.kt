package com.dexter.app.ui.battle

import androidx.lifecycle.SavedStateHandle
import com.dexter.app.data.repository.PokemonRepository
import com.dexter.app.domain.battle.model.BattleAbility
import com.dexter.app.domain.battle.model.BattleItem
import com.dexter.app.domain.battle.model.BattleMove
import com.dexter.app.domain.battle.model.BattleTerrain
import com.dexter.app.domain.battle.model.BattleWeather
import com.dexter.app.domain.battle.model.MoveCategory
import com.dexter.app.domain.battle.model.PokemonNature
import com.dexter.app.domain.battle.model.SpeedTierCategory
import com.dexter.app.domain.battle.model.StatSpread
import com.dexter.app.domain.battle.model.StatType
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonStats
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.domain.model.SyncState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BattleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakePokemonList = listOf(
        Pokemon(
            id = 887,
            name = "dragapult",
            number = 887,
            heightM = 3.0,
            weightKg = 50.0,
            primaryType = PokemonType.DRAGON,
            secondaryType = PokemonType.GHOST,
            stats = PokemonStats(hp = 88, attack = 120, defense = 75, spAttack = 100, spDefense = 75, speed = 142)
        ),
        Pokemon(
            id = 748,
            name = "toxapex",
            number = 748,
            heightM = 0.7,
            weightKg = 14.5,
            primaryType = PokemonType.POISON,
            secondaryType = PokemonType.WATER,
            stats = PokemonStats(hp = 50, attack = 63, defense = 152, spAttack = 53, spDefense = 142, speed = 35)
        ),
        Pokemon(
            id = 445,
            name = "garchomp",
            number = 445,
            heightM = 1.9,
            weightKg = 95.0,
            primaryType = PokemonType.DRAGON,
            secondaryType = PokemonType.GROUND,
            stats = PokemonStats(hp = 108, attack = 130, defense = 95, spAttack = 80, spDefense = 85, speed = 102)
        )
    )

    private val fakeRepository = object : PokemonRepository {
        override val syncState: StateFlow<SyncState> = MutableStateFlow(SyncState.Completed)
        override fun observeAllPokemon(): Flow<List<Pokemon>> = flowOf(fakePokemonList)
        override fun observePokemonById(id: Int): Flow<Pokemon?> = flowOf(fakePokemonList.firstOrNull { it.id == id })
        override fun observeEvolutionChain(chainId: Int) = flowOf(emptyList<com.dexter.app.domain.model.EvolutionNode>())
        override fun observeMovesForPokemon(pokemonId: Int) = flowOf(emptyList<com.dexter.app.domain.model.PokemonMove>())
        override fun observeAbilitiesForPokemon(pokemonId: Int) = flowOf(emptyList<com.dexter.app.domain.model.PokemonAbility>())
        override fun observeFormsForPokemon(basePokemonId: Int) = flowOf(emptyList<com.dexter.app.domain.model.PokemonForm>())
        override fun observeTeamMembers() = flowOf(emptyMap<Int, Pokemon>())
        override suspend fun setTeamMember(slot: Int, pokemonId: Int) {}
        override suspend fun removeTeamMember(slot: Int) {}
        override suspend fun clearTeam() {}
        override suspend fun swapTeamSlots(fromSlot: Int, toSlot: Int) {}
        override suspend fun ensurePokemonDetailExtras(pokemonId: Int) {}
        override suspend fun syncPokemonData(forceResync: Boolean) {}
        override suspend fun toggleCaught(pokemonId: Int, isCaught: Boolean) {}
        override suspend fun toggleFavorite(pokemonId: Int, isFavorite: Boolean) {}
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state and tab selection`() = runTest {
        val viewModel = BattleViewModel(fakeRepository, SavedStateHandle())
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(BattleTab.DAMAGE_CALC, state.activeTab)
        assertNotNull(state.damageCalc.calculationResult)

        // Switch to Stat Calc
        viewModel.selectTab(BattleTab.STAT_CALC)
        assertEquals(BattleTab.STAT_CALC, viewModel.uiState.value.activeTab)

        // Switch to Speed Tiers
        viewModel.selectTab(BattleTab.SPEED_TIERS)
        assertEquals(BattleTab.SPEED_TIERS, viewModel.uiState.value.activeTab)
    }

    @Test
    fun `test damage calculation updates with attacker and defender changes`() = runTest {
        val viewModel = BattleViewModel(fakeRepository, SavedStateHandle())
        testDispatcher.scheduler.advanceUntilIdle()

        // Set Garchomp as Attacker
        val garchomp = fakePokemonList.first { it.name == "garchomp" }
        viewModel.setAttackerPokemon(garchomp)
        viewModel.setAttackerNature(PokemonNature.ADAMANT)
        viewModel.setAttackerEv(StatType.ATTACK, 252)
        viewModel.setAttackerItem(BattleItem.CHOICE_BAND)

        // Set Earthquake as move
        viewModel.selectMove(
            BattleMove("earthquake", "Earthquake", PokemonType.GROUND, MoveCategory.PHYSICAL, 100)
        )

        testDispatcher.scheduler.advanceUntilIdle()
        val result = viewModel.uiState.value.damageCalc.calculationResult
        assertNotNull(result)
        assertTrue("Damage should be greater than 0", (result?.minDamage ?: 0) > 0)
        assertTrue("Summary should contain Garchomp", result?.summaryFormulaText?.contains("Garchomp") == true)

        // Test Swap Attacker & Defender
        viewModel.swapAttackerAndDefender()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("toxapex", viewModel.uiState.value.damageCalc.attacker.pokemon.name)
        assertEquals("garchomp", viewModel.uiState.value.damageCalc.defender.pokemon.name)
    }

    @Test
    fun `test weather and screen toggles in damage calc`() = runTest {
        val viewModel = BattleViewModel(fakeRepository, SavedStateHandle())
        testDispatcher.scheduler.advanceUntilIdle()

        // Fire move in Sun
        viewModel.selectMove(
            BattleMove("flamethrower", "Flamethrower", PokemonType.FIRE, MoveCategory.SPECIAL, 90)
        )
        val clearResult = viewModel.uiState.value.damageCalc.calculationResult?.minDamage ?: 0

        viewModel.setWeather(BattleWeather.SUN)
        testDispatcher.scheduler.advanceUntilIdle()
        val sunResult = viewModel.uiState.value.damageCalc.calculationResult?.minDamage ?: 0
        assertTrue("Damage in sun should be higher for Fire move", sunResult > clearResult)

        // Screen reduction
        viewModel.toggleLightScreen()
        testDispatcher.scheduler.advanceUntilIdle()
        val screenResult = viewModel.uiState.value.damageCalc.calculationResult?.minDamage ?: 0
        assertTrue("Light screen should reduce special damage", screenResult < sunResult)
    }

    @Test
    fun `test stat calculator calculations and showdown export`() = runTest {
        val viewModel = BattleViewModel(fakeRepository, SavedStateHandle())
        testDispatcher.scheduler.advanceUntilIdle()

        val garchomp = fakePokemonList.first { it.name == "garchomp" }
        viewModel.setStatCalcPokemon(garchomp)
        viewModel.setStatCalcLevel(100)
        viewModel.setStatCalcNature(PokemonNature.JOLLY)
        viewModel.setStatCalcAllIvs(31)
        viewModel.setStatCalcEv(StatType.ATTACK, 252)
        viewModel.setStatCalcEv(StatType.SPEED, 252)
        viewModel.setStatCalcItem(BattleItem.CHOICE_SCARF)

        testDispatcher.scheduler.advanceUntilIdle()
        val stats = viewModel.uiState.value.statCalc.calculatedStats
        assertNotNull(stats)
        assertEquals(359, stats?.attack) // Neutral Lv 100 Garchomp 252 Atk = 359
        assertEquals(333, stats?.speed) // Jolly (+10%) Lv 100 Garchomp 252 Spe = 333

        val showdownText = viewModel.uiState.value.statCalc.showdownText
        assertTrue("Showdown text contains Garchomp", showdownText.contains("Garchomp"))
        assertTrue("Showdown text contains Choice Scarf", showdownText.contains("Choice Scarf"))
        assertTrue("Showdown text contains Jolly Nature", showdownText.contains("Jolly Nature"))
    }

    @Test
    fun `test speed tier ladder ranking and custom modifiers`() = runTest {
        val viewModel = BattleViewModel(fakeRepository, SavedStateHandle())
        testDispatcher.scheduler.advanceUntilIdle()

        val dragapult = fakePokemonList.first { it.name == "dragapult" }
        viewModel.setSpeedTierUserPokemon(dragapult)
        viewModel.setSpeedTierLevel(50)
        viewModel.setUserSpeedNature(PokemonNature.JOLLY)
        viewModel.setUserSpeedEv(252)

        testDispatcher.scheduler.advanceUntilIdle()
        val ladder = viewModel.uiState.value.speedTier.ladder
        assertTrue("Ladder should be populated", ladder.isNotEmpty())

        val userEntry = ladder.firstOrNull { it.isUserPokemon }
        assertNotNull("User Pokemon should be in ladder", userEntry)
        assertEquals(213, userEntry?.calculatedSpeed)

        // Add Choice Scarf (+50% speed)
        viewModel.setUserSpeedScarf(true)
        testDispatcher.scheduler.advanceUntilIdle()
        val scarfSpeed = viewModel.uiState.value.speedTier.ladder.first { it.isUserPokemon }.calculatedSpeed
        assertEquals(319, scarfSpeed) // 213 * 1.5 = 319.5 -> 319
    }

    @Test
    fun `test send from stat calc to damage calc`() = runTest {
        val viewModel = BattleViewModel(fakeRepository, SavedStateHandle())
        testDispatcher.scheduler.advanceUntilIdle()

        val garchomp = fakePokemonList.first { it.name == "garchomp" }
        viewModel.setStatCalcPokemon(garchomp)
        viewModel.setStatCalcLevel(100)
        viewModel.setStatCalcNature(PokemonNature.ADAMANT)
        viewModel.setStatCalcEv(StatType.ATTACK, 252)
        viewModel.setStatCalcItem(BattleItem.CHOICE_BAND)

        viewModel.sendStatCalcToDamageCalc()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(BattleTab.DAMAGE_CALC, viewModel.uiState.value.activeTab)
        assertEquals("garchomp", viewModel.uiState.value.damageCalc.attacker.pokemon.name)
        assertEquals(100, viewModel.uiState.value.damageCalc.attacker.level)
        assertEquals(PokemonNature.ADAMANT, viewModel.uiState.value.damageCalc.attacker.nature)
        assertEquals(BattleItem.CHOICE_BAND, viewModel.uiState.value.damageCalc.attacker.item)
    }

    @Test
    fun `test catch calculator state and calculations in ViewModel`() = runTest {
        val viewModel = BattleViewModel(fakeRepository, SavedStateHandle())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectTab(BattleTab.CATCH_CALC)
        assertEquals(BattleTab.CATCH_CALC, viewModel.uiState.value.activeTab)

        val garchomp = fakePokemonList.first { it.name == "garchomp" }
        viewModel.setCatchCalcPokemon(garchomp)
        viewModel.setCatchCalcLevel(50)
        viewModel.setCatchCalcHpPercent(100.0)
        viewModel.setCatchCalcStatus(com.dexter.app.domain.battle.model.CatchStatusCondition.NONE)
        viewModel.setCatchCalcBall(com.dexter.app.domain.battle.model.PokeBallType.ULTRA_BALL)

        testDispatcher.scheduler.advanceUntilIdle()
        val fullHpResult = viewModel.uiState.value.catchCalc.calculationResult
        assertNotNull(fullHpResult)

        // Lower HP to 1% (False Swipe) + Sleep
        viewModel.setCatchCalcHpPercent(1.0)
        viewModel.setCatchCalcStatus(com.dexter.app.domain.battle.model.CatchStatusCondition.SLEEP)
        viewModel.setCatchCalcBall(com.dexter.app.domain.battle.model.PokeBallType.DUSK_BALL)
        viewModel.setCatchCalcNightOrCave(true)

        testDispatcher.scheduler.advanceUntilIdle()
        val nuzlockeResult = viewModel.uiState.value.catchCalc.calculationResult
        assertNotNull(nuzlockeResult)

        assertTrue(
            "Nuzlocke condition catch rate should be higher",
            nuzlockeResult!!.catchChancePercent > fullHpResult!!.catchChancePercent
        )
        assertTrue(
            "Nuzlocke expected balls should be fewer",
            nuzlockeResult.expectedBalls < fullHpResult.expectedBalls
        )
        assertTrue(
            "Ball comparison list should have entries",
            nuzlockeResult.ballComparisonList.isNotEmpty()
        )
    }
}
