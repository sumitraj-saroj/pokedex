package com.dexter.app.domain.battle

import com.dexter.app.domain.battle.engine.Combatant
import com.dexter.app.domain.battle.engine.DamageCalculatorEngine
import com.dexter.app.domain.battle.engine.SpeedTierEngine
import com.dexter.app.domain.battle.engine.StatCalculatorEngine
import com.dexter.app.domain.battle.model.BattleAbility
import com.dexter.app.domain.battle.model.BattleField
import com.dexter.app.domain.battle.model.BattleItem
import com.dexter.app.domain.battle.model.BattleMove
import com.dexter.app.domain.battle.model.BattleWeather
import com.dexter.app.domain.battle.model.MoveCategory
import com.dexter.app.domain.battle.model.PokemonNature
import com.dexter.app.domain.battle.model.StatSpread
import com.dexter.app.domain.battle.model.StatType
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonStats
import com.dexter.app.domain.model.PokemonType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleEnginesTest {

    private val pikachu = Pokemon(
        id = 25,
        name = "pikachu",
        number = 25,
        heightM = 0.4,
        weightKg = 6.0,
        primaryType = PokemonType.ELECTRIC,
        secondaryType = null,
        stats = PokemonStats(hp = 35, attack = 55, defense = 40, spAttack = 50, spDefense = 50, speed = 90)
    )

    private val garchomp = Pokemon(
        id = 445,
        name = "garchomp",
        number = 445,
        heightM = 1.9,
        weightKg = 95.0,
        primaryType = PokemonType.DRAGON,
        secondaryType = PokemonType.GROUND,
        stats = PokemonStats(hp = 108, attack = 130, defense = 95, spAttack = 80, spDefense = 85, speed = 102)
    )

    private val toxapex = Pokemon(
        id = 748,
        name = "toxapex",
        number = 748,
        heightM = 0.7,
        weightKg = 14.5,
        primaryType = PokemonType.POISON,
        secondaryType = PokemonType.WATER,
        stats = PokemonStats(hp = 50, attack = 63, defense = 152, spAttack = 53, spDefense = 142, speed = 35)
    )

    @Test
    fun `test Level 100 Garchomp Max Attack Jolly Stat Calculation`() {
        // Base 130 Atk, 31 IVs, 252 EVs, Neutral Nature -> 359
        // Base 130 Atk, 31 IVs, 252 EVs, Jolly (Neutral for Atk) -> 359
        // Base 130 Atk, 31 IVs, 252 EVs, Adamant (+10% Atk) -> 394
        val neutralAtk = StatCalculatorEngine.calculateOtherStat(
            baseStat = 130,
            iv = 31,
            ev = 252,
            level = 100,
            natureMultiplier = 1.0
        )
        assertEquals(359, neutralAtk)

        val boostedAtk = StatCalculatorEngine.calculateOtherStat(
            baseStat = 130,
            iv = 31,
            ev = 252,
            level = 100,
            natureMultiplier = 1.1
        )
        assertEquals(394, boostedAtk)

        // HP: Base 108, 31 IV, 252 EV, Level 100 -> 420
        val maxHp = StatCalculatorEngine.calculateHp(
            baseHp = 108,
            iv = 31,
            ev = 252,
            level = 100
        )
        assertEquals(420, maxHp)
    }

    @Test
    fun `test Level 50 Stat Calculation`() {
        // Level 50 Garchomp HP: Base 108, 31 IV, 0 EV -> 183
        val hpLv50 = StatCalculatorEngine.calculateHp(108, 31, 0, 50)
        assertEquals(183, hpLv50)

        // Level 50 Garchomp Spe: Base 102, 31 IV, 252 EV, Jolly (+Spe) -> 169
        val speLv50 = StatCalculatorEngine.calculateOtherStat(102, 31, 252, 50, 1.1)
        assertEquals(169, speLv50)
    }

    @Test
    fun `test Shedinja HP is always 1`() {
        val shedinjaHp = StatCalculatorEngine.calculateHp(1, 31, 252, 100, isShedinja = true)
        assertEquals(1, shedinjaHp)
    }

    @Test
    fun `test Earthquake Garchomp vs Toxapex Damage Calculation`() {
        val attacker = Combatant(
            pokemon = garchomp,
            level = 100,
            nature = PokemonNature.ADAMANT,
            ivs = StatSpread.ALL_31,
            evs = StatSpread(attack = 252, speed = 252),
            item = BattleItem.CHOICE_BAND
        )

        val defender = Combatant(
            pokemon = toxapex,
            level = 100,
            nature = PokemonNature.BOLD,
            ivs = StatSpread.ALL_31,
            evs = StatSpread(hp = 252, defense = 252)
        )

        val move = BattleMove(
            name = "earthquake",
            displayName = "Earthquake",
            type = PokemonType.GROUND,
            category = MoveCategory.PHYSICAL,
            basePower = 100
        )

        val result = DamageCalculatorEngine.calculateDamage(attacker, defender, move)

        // Super effective (2.0x) + STAB (1.5x) + Choice Band (1.5x)
        assertTrue("Damage should be positive", result.minDamage > 0)
        assertTrue("Type multiplier should be 2.0x", result.typeMultiplier == 2.0)
        assertTrue("Should be STAB", result.isStab)
        assertTrue("Summary text should contain Garchomp and Toxapex", result.summaryFormulaText.contains("Garchomp"))
    }

    @Test
    fun `test Ground Immunity against Levitate`() {
        val attacker = Combatant(pokemon = garchomp)
        val defender = Combatant(
            pokemon = toxapex,
            ability = BattleAbility.LEVITATE
        )
        val move = BattleMove.defaultMove(PokemonType.GROUND)

        val result = DamageCalculatorEngine.calculateDamage(attacker, defender, move)
        assertEquals(0, result.minDamage)
        assertEquals(0.0, result.typeMultiplier, 0.001)
    }

    @Test
    fun `test Speed Tier Engine Ladder Generation`() {
        val benchmarks = SpeedTierEngine.getCompetitiveBenchmarks(level = 50)
        assertTrue("Benchmarks should have entries", benchmarks.isNotEmpty())

        // Top entry should be very fast (Regieleki / Swift Swim / Booster)
        val fastest = benchmarks.first()
        assertTrue("Fastest pokemon should have speed >= 200", fastest.calculatedSpeed >= 200)

        // Inject user Dragapult
        val ladder = SpeedTierEngine.createLadderWithUserPokemon(
            benchmarks = benchmarks,
            userPokemon = Pokemon(
                id = 887,
                name = "dragapult",
                number = 887,
                heightM = 3.0,
                weightKg = 50.0,
                primaryType = PokemonType.DRAGON,
                secondaryType = PokemonType.GHOST,
                stats = PokemonStats(hp = 88, attack = 120, defense = 75, spAttack = 100, spDefense = 75, speed = 142)
            ),
            level = 50,
            nature = PokemonNature.JOLLY,
            ev = 252
        )

        val userEntry = ladder.firstOrNull { it.isUserPokemon }
        assertNotNull("User pokemon should be in ladder", userEntry)
        assertEquals(213, userEntry?.calculatedSpeed) // 142 base Jolly 252 Spe at Lv 50 = 213
    }
}
