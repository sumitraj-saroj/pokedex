package com.dexter.app.domain.battle.engine

import com.dexter.app.domain.battle.model.BallCatchSummary
import com.dexter.app.domain.battle.model.CatchRateResult
import com.dexter.app.domain.battle.model.CatchStatusCondition
import com.dexter.app.domain.battle.model.PokeBallType
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonType
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

data class CatchContext(
    val pokemon: Pokemon,
    val targetLevel: Int = 50,
    val currentHpPercent: Double = 100.0, // 1.0 to 100.0
    val statusCondition: CatchStatusCondition = CatchStatusCondition.NONE,
    val activeBall: PokeBallType = PokeBallType.ULTRA_BALL,
    val customCatchRate: Int? = null,
    val turnNumber: Int = 1,
    val isNightOrCave: Boolean = true,
    val isWaterEncounter: Boolean = false,
    val isAlreadyInPokedex: Boolean = true,
    val playerPokemonLevel: Int = 60,
    val isOppositeGenderSameSpecies: Boolean = false
)

object CatchRateEngine {

    /**
     * Gets the standard base catch rate (1–255) for a given Pokémon.
     */
    fun getBaseCatchRate(pokemon: Pokemon): Int {
        val id = pokemon.id
        val name = pokemon.name.lowercase()

        // 1. Specific Legendary & Mythical Pokémon (Catch rate = 3 or 45)
        if (pokemon.isLegendary || pokemon.isMythical) {
            return when (id) {
                151, 251, 385, 492, 494 -> 45 // Mew, Celebi, Jirachi, Shaymin, Victini
                384 -> 45 // Rayquaza (ORAS delta episode = 45, default 3)
                483, 484 -> 30 // Dialga, Palkia (DP)
                888, 889 -> 10 // Zacian, Zamazenta
                else -> 3
            }
        }

        // Beldum line is notorious for having a catch rate of 3
        if (id == 374 || id == 375 || id == 376) return 3

        // Starters (Catch rate = 45)
        val starterIds = setOf(
            1, 2, 3, 4, 5, 6, 7, 8, 9, // Gen 1
            152, 153, 154, 155, 156, 157, 158, 159, 160, // Gen 2
            252, 253, 254, 255, 256, 257, 258, 259, 260, // Gen 3
            387, 388, 389, 390, 391, 392, 393, 394, 395, // Gen 4
            495, 496, 497, 498, 499, 500, 501, 502, 503, // Gen 5
            650, 651, 652, 653, 654, 655, 656, 657, 658, // Gen 6
            722, 723, 724, 725, 726, 727, 728, 729, 730, // Gen 7
            810, 811, 812, 813, 814, 815, 816, 817, 818, // Gen 8
            906, 907, 908, 909, 910, 911, 912, 913, 914  // Gen 9
        )
        if (starterIds.contains(id)) return 45

        // Pseudo-legendaries and rare dragons (Catch rate = 45)
        val pseudoIds = setOf(
            147, 148, 149, // Dratini line
            246, 247, 248, // Larvitar line
            371, 372, 373, // Bagon line
            443, 444, 445, // Gible line
            633, 634, 635, // Deino line
            704, 705, 706, // Goomy line
            782, 783, 784, // Jangmo-o line
            885, 886, 887, // Dreepy line
            996, 997, 998, // Frigibax line
            133, // Eevee
            447, 448, // Riolu, Lucario
            570, 571, // Zorua, Zoroark
            636, 637, // Larvesta, Volcarona (15)
            143, 446, // Snorlax (25), Munchlax (50)
            113, 242 // Chansey (30), Blissey (30)
        )
        if (id == 637) return 15
        if (id == 143) return 25
        if (id == 113 || id == 242) return 30
        if (pseudoIds.contains(id)) return 45

        // Ultra Beasts (Catch rate = 45 or 255 for Poipole)
        val ultraBeasts = setOf(793, 794, 795, 796, 797, 798, 799, 803, 804, 805, 806)
        if (ultraBeasts.contains(id)) return 45

        // Early route common Pokémon (Catch rate = 255)
        val commonEarlyIds = setOf(
            10, 11, 13, 14, 16, 19, 21, 41, 129, // Gen 1 (Caterpie, Pidgey, Rattata, Zubat, Magikarp)
            161, 163, 165, 167, 263, 265, 276, // Gen 2/3
            399, 401, 504, 506, 659, 661, 731, 734, 819, 821, 915, 917 // Gen 4-9
        )
        if (commonEarlyIds.contains(id)) return 255

        // General BST heuristic if not explicitly mapped
        val bst = pokemon.stats?.total ?: 400
        return when {
            bst >= 540 -> 45
            bst >= 480 -> 60
            bst >= 420 -> 90
            bst >= 360 -> 120
            bst >= 300 -> 190
            else -> 255
        }
    }

    /**
     * Calculates the ball multiplier based on ball type and environment.
     */
    fun calculateBallMultiplier(
        ballType: PokeBallType,
        context: CatchContext
    ): Double {
        val pokemon = context.pokemon
        val targetLevel = context.targetLevel

        return when (ballType) {
            PokeBallType.POKE_BALL, PokeBallType.PREMIER_BALL, PokeBallType.LUXURY_BALL, PokeBallType.HEAL_BALL -> 1.0
            PokeBallType.GREAT_BALL, PokeBallType.SAFARI_BALL, PokeBallType.SPORT_BALL -> 1.5
            PokeBallType.ULTRA_BALL -> 2.0
            PokeBallType.MASTER_BALL -> 255.0

            PokeBallType.QUICK_BALL -> if (context.turnNumber == 1) 5.0 else 1.0
            PokeBallType.DUSK_BALL -> if (context.isNightOrCave) 3.0 else 1.0
            PokeBallType.NET_BALL -> {
                val isWaterOrBug = pokemon.primaryType == PokemonType.WATER || pokemon.primaryType == PokemonType.BUG ||
                        pokemon.secondaryType == PokemonType.WATER || pokemon.secondaryType == PokemonType.BUG
                if (isWaterOrBug) 3.5 else 1.0
            }
            PokeBallType.DIVE_BALL -> if (context.isWaterEncounter) 3.5 else 1.0
            PokeBallType.REPEAT_BALL -> if (context.isAlreadyInPokedex) 3.5 else 1.0
            PokeBallType.TIMER_BALL -> min(4.0, 1.0 + (context.turnNumber * 0.3))
            PokeBallType.NEST_BALL -> {
                val mult = (41 - targetLevel) / 10.0
                mult.coerceIn(1.0, 3.9)
            }
            PokeBallType.FAST_BALL -> {
                val baseSpeed = pokemon.stats?.speed ?: 80
                if (baseSpeed >= 100) 4.0 else 1.0
            }
            PokeBallType.LEVEL_BALL -> {
                val playerLv = context.playerPokemonLevel
                when {
                    playerLv >= targetLevel * 4 -> 8.0
                    playerLv >= targetLevel * 2 -> 4.0
                    playerLv > targetLevel -> 2.0
                    else -> 1.0
                }
            }
            PokeBallType.MOON_BALL -> {
                val moonEvoIds = setOf(29, 30, 31, 32, 33, 34, 35, 36, 39, 40, 300, 301, 517, 518)
                if (moonEvoIds.contains(pokemon.id)) 4.0 else 1.0
            }
            PokeBallType.LOVE_BALL -> if (context.isOppositeGenderSameSpecies) 8.0 else 1.0
            PokeBallType.DREAM_BALL -> if (context.statusCondition == CatchStatusCondition.SLEEP) 4.0 else 1.0
            PokeBallType.LURE_BALL -> if (context.isWaterEncounter) 4.0 else 1.0
            PokeBallType.BEAST_BALL -> {
                val ultraBeasts = setOf(793, 794, 795, 796, 797, 798, 799, 803, 804, 805, 806)
                if (ultraBeasts.contains(pokemon.id)) 5.0 else 0.1
            }
            PokeBallType.HEAVY_BALL -> 1.0 // Heavy ball modifies base catch rate directly
        }
    }

    /**
     * Executes the Gen 6–9 Catch Rate mathematical formula.
     */
    fun calculateCatchRate(context: CatchContext): CatchRateResult {
        val baseCatchRate = context.customCatchRate ?: getBaseCatchRate(context.pokemon)
        val hpPercent = context.currentHpPercent.coerceIn(1.0, 100.0) / 100.0
        val statusMult = context.statusCondition.multiplier

        // Heavy Ball direct modifier
        val effectiveCatchRate = if (context.activeBall == PokeBallType.HEAVY_BALL) {
            val weight = context.pokemon.weightKg
            val mod = when {
                weight < 100.0 -> -20
                weight < 200.0 -> 0
                weight < 300.0 -> 20
                weight < 400.0 -> 30
                else -> 40
            }
            max(1, baseCatchRate + mod)
        } else {
            baseCatchRate
        }

        val ballMult = calculateBallMultiplier(context.activeBall, context)

        // Master ball instant catch
        if (context.activeBall == PokeBallType.MASTER_BALL) {
            val comparisons = generateBallComparisons(context, baseCatchRate, hpPercent, statusMult)
            return CatchRateResult(
                catchChancePercent = 100.0,
                modifiedCatchValue = 255,
                shakeProbability = 1.0,
                expectedBalls = 1.0,
                ballsFor50Percent = 1,
                ballsFor90Percent = 1,
                ballsFor95Percent = 1,
                ballsFor99Percent = 1,
                catchChanceWithinThrows = listOf(1 to 100.0, 3 to 100.0, 5 to 100.0, 10 to 100.0),
                shake0Chance = 0.0,
                shake1Chance = 0.0,
                shake2Chance = 0.0,
                shake3Chance = 0.0,
                ballComparisonList = comparisons
            )
        }

        // a = floor(((3 * maxHp - 2 * curHp) / (3 * maxHp)) * rate * ballMult) * statusMult
        val hpFactor = (3.0 - 2.0 * hpPercent) / 3.0
        val aVal = floor(floor(hpFactor * effectiveCatchRate * ballMult) * statusMult).toInt()

        val catchChance: Double
        val shakeProb: Double
        val shake0: Double
        val shake1: Double
        val shake2: Double
        val shake3: Double

        if (aVal >= 255) {
            catchChance = 1.0
            shakeProb = 1.0
            shake0 = 0.0
            shake1 = 0.0
            shake2 = 0.0
            shake3 = 0.0
        } else {
            // b = floor(65536 * (a / 255)^0.1875) or (a/255)^0.75
            // In modern games (b / 65536)^4 is the 4-shake probability
            val bVal = floor(65536.0 * (aVal / 255.0).pow(0.1875))
            shakeProb = (bVal / 65536.0).coerceIn(0.0, 1.0)
            catchChance = shakeProb.pow(4.0).coerceIn(0.0001, 1.0)

            shake0 = (1.0 - shakeProb)
            shake1 = shakeProb * (1.0 - shakeProb)
            shake2 = shakeProb.pow(2.0) * (1.0 - shakeProb)
            shake3 = shakeProb.pow(3.0) * (1.0 - shakeProb)
        }

        val catchPercent = catchChance * 100.0
        val expectedBalls = (1.0 / catchChance).coerceIn(1.0, 999.0)

        // Confidence milestones
        val n50 = if (catchChance >= 0.999) 1 else max(1, ceil(ln(1.0 - 0.50) / ln(1.0 - catchChance)).toInt())
        val n90 = if (catchChance >= 0.999) 1 else max(1, ceil(ln(1.0 - 0.90) / ln(1.0 - catchChance)).toInt())
        val n95 = if (catchChance >= 0.999) 1 else max(1, ceil(ln(1.0 - 0.95) / ln(1.0 - catchChance)).toInt())
        val n99 = if (catchChance >= 0.999) 1 else max(1, ceil(ln(1.0 - 0.99) / ln(1.0 - catchChance)).toInt())

        val throwsCheckpoints = listOf(1, 3, 5, 10, 20).map { throws ->
            val probWithinThrows = (1.0 - (1.0 - catchChance).pow(throws.toDouble())) * 100.0
            throws to min(100.0, probWithinThrows)
        }

        val comparisons = generateBallComparisons(context, baseCatchRate, hpPercent, statusMult)

        return CatchRateResult(
            catchChancePercent = catchPercent,
            modifiedCatchValue = aVal.coerceIn(0, 255),
            shakeProbability = shakeProb,
            expectedBalls = expectedBalls,
            ballsFor50Percent = n50,
            ballsFor90Percent = n90,
            ballsFor95Percent = n95,
            ballsFor99Percent = n99,
            catchChanceWithinThrows = throwsCheckpoints,
            shake0Chance = shake0 * 100.0,
            shake1Chance = shake1 * 100.0,
            shake2Chance = shake2 * 100.0,
            shake3Chance = shake3 * 100.0,
            ballComparisonList = comparisons
        )
    }

    private fun generateBallComparisons(
        context: CatchContext,
        baseCatchRate: Int,
        hpPercent: Double,
        statusMult: Double
    ): List<BallCatchSummary> {
        val priorityBalls = listOf(
            PokeBallType.QUICK_BALL,
            PokeBallType.DUSK_BALL,
            PokeBallType.ULTRA_BALL,
            PokeBallType.NET_BALL,
            PokeBallType.TIMER_BALL,
            PokeBallType.REPEAT_BALL,
            PokeBallType.GREAT_BALL,
            PokeBallType.POKE_BALL,
            PokeBallType.NEST_BALL,
            PokeBallType.FAST_BALL,
            PokeBallType.LEVEL_BALL,
            PokeBallType.HEAVY_BALL,
            PokeBallType.MASTER_BALL
        )

        return priorityBalls.map { ball ->
            if (ball == PokeBallType.MASTER_BALL) {
                BallCatchSummary(ball, 100.0, 1.0, 255.0)
            } else {
                val ballMult = calculateBallMultiplier(ball, context)
                val effectiveRate = if (ball == PokeBallType.HEAVY_BALL) {
                    val weight = context.pokemon.weightKg
                    val mod = when {
                        weight < 100.0 -> -20
                        weight < 200.0 -> 0
                        weight < 300.0 -> 20
                        weight < 400.0 -> 30
                        else -> 40
                    }
                    max(1, baseCatchRate + mod)
                } else baseCatchRate

                val hpFactor = (3.0 - 2.0 * hpPercent) / 3.0
                val a = floor(floor(hpFactor * effectiveRate * ballMult) * statusMult).toInt()

                val chance = if (a >= 255) 1.0 else {
                    val b = floor(65536.0 * (a / 255.0).pow(0.1875))
                    (b / 65536.0).pow(4.0).coerceIn(0.0001, 1.0)
                }

                BallCatchSummary(
                    ballType = ball,
                    catchChancePercent = min(100.0, chance * 100.0),
                    expectedBalls = (1.0 / chance).coerceIn(1.0, 999.0),
                    multiplierUsed = ballMult
                )
            }
        }.sortedByDescending { it.catchChancePercent }
    }
}
