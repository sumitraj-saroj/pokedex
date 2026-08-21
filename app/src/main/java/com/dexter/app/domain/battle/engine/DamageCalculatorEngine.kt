package com.dexter.app.domain.battle.engine

import com.dexter.app.domain.battle.model.BattleAbility
import com.dexter.app.domain.battle.model.BattleField
import com.dexter.app.domain.battle.model.BattleItem
import com.dexter.app.domain.battle.model.BattleMove
import com.dexter.app.domain.battle.model.BattleTerrain
import com.dexter.app.domain.battle.model.BattleWeather
import com.dexter.app.domain.battle.model.CalculatedStats
import com.dexter.app.domain.battle.model.DamageRollResult
import com.dexter.app.domain.battle.model.MoveCategory
import com.dexter.app.domain.battle.model.PokemonNature
import com.dexter.app.domain.battle.model.StatSpread
import com.dexter.app.domain.battle.model.StatStages
import com.dexter.app.domain.battle.model.StatType
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonType
import com.dexter.app.domain.model.TypeMatchupEngine
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class Combatant(
    val pokemon: Pokemon,
    val level: Int = 50,
    val nature: PokemonNature = PokemonNature.HARDY,
    val ivs: StatSpread = StatSpread.ALL_31,
    val evs: StatSpread = StatSpread.ALL_0,
    val statStages: StatStages = StatStages(),
    val item: BattleItem = BattleItem.NONE,
    val ability: BattleAbility = BattleAbility.NONE,
    val isBurned: Boolean = false,
    val isParalyzed: Boolean = false,
    val teraType: PokemonType? = null,
    val currentHpPercent: Double = 100.0
)

object DamageCalculatorEngine {

    /**
     * Executes the comprehensive Gen 9 Damage Calculation.
     */
    fun calculateDamage(
        attacker: Combatant,
        defender: Combatant,
        move: BattleMove,
        field: BattleField = BattleField(),
        isCritical: Boolean = false
    ): DamageRollResult {
        // 1. Calculate Base Stats for Attacker & Defender
        val isAttackerShedinja = attacker.pokemon.id == 292
        val isDefenderShedinja = defender.pokemon.id == 292

        val attackerBaseStats = attacker.pokemon.stats ?: com.dexter.app.domain.model.PokemonStats(100, 100, 100, 100, 100, 100)
        val defenderBaseStats = defender.pokemon.stats ?: com.dexter.app.domain.model.PokemonStats(100, 100, 100, 100, 100, 100)

        val attackerStats = StatCalculatorEngine.calculateAllStats(
            attackerBaseStats, attacker.ivs, attacker.evs, attacker.level, attacker.nature, isAttackerShedinja
        )
        val defenderStats = StatCalculatorEngine.calculateAllStats(
            defenderBaseStats, defender.ivs, defender.evs, defender.level, defender.nature, isDefenderShedinja
        )

        // 2. Type Effectiveness Multiplier
        val defenderPrimaryType = defender.teraType ?: defender.pokemon.primaryType
        val defenderSecondaryType = if (defender.teraType != null) null else defender.pokemon.secondaryType
        val typeMultiplier = calculateTypeEffectiveness(
            moveType = move.type,
            defPrimary = defenderPrimaryType,
            defSecondary = defenderSecondaryType,
            defAbility = defender.ability,
            defItem = defender.item,
            attAbility = attacker.ability
        )

        if (typeMultiplier == 0.0) {
            val rolls = List(16) { 0 }
            return DamageRollResult(
                rolls = rolls,
                minDamage = 0,
                maxDamage = 0,
                avgDamage = 0.0,
                defenderMaxHp = defenderStats.hp,
                minPercent = 0.0,
                maxPercent = 0.0,
                avgPercent = 0.0,
                koChanceText = "Does not affect the target (0% damage)",
                summaryFormulaText = "${attacker.pokemon.capitalizedName} ${move.displayName} vs ${defender.pokemon.capitalizedName}: 0 (0.0%) -- Immune",
                typeMultiplier = 0.0,
                isStab = false,
                isCritical = isCritical,
                hitsTakenToKo = 0..0,
                ohkoChance = 0.0,
                twoHkoChance = 0.0,
                threeHkoChance = 0.0
            )
        }

        // 3. Determine Base Power with modifiers
        val effectivePower = calculateEffectivePower(move, attacker, defender, field)

        // 4. Determine Effective Attack Stat
        val effectiveAttack = calculateEffectiveAttack(attacker, defender, move, field, isCritical, attackerStats)

        // 5. Determine Effective Defense Stat
        val effectiveDefense = calculateEffectiveDefense(attacker, defender, move, field, isCritical, defenderStats)

        // 6. Base Damage formula: floor(floor(floor(2 * Level / 5 + 2) * Power * A / D) / 50) + 2
        val levelFactor = (2 * attacker.level) / 5 + 2
        val baseDamage = floor(floor(floor(levelFactor.toDouble() * effectivePower * effectiveAttack) / effectiveDefense.toDouble()) / 50.0).toInt() + 2

        // 7. Modifiers
        val targetsMult = if (field.isDoubles && move.isSpreadMove) 0.75 else 1.0
        val weatherMult = calculateWeatherMultiplier(move.type, field.weather)
        val critMult = if (isCritical) {
            if (attacker.ability == BattleAbility.SNIPER) 2.25 else 1.5
        } else 1.0
        val stabMult = calculateStabMultiplier(move.type, attacker)
        val burnMult = if (attacker.isBurned && move.category == MoveCategory.PHYSICAL && attacker.ability != BattleAbility.GUTS && move.name != "facade") 0.5 else 1.0
        val screensMult = calculateScreensMultiplier(move.category, defender, field, isCritical)
        val otherMult = calculateOtherModifiers(move, attacker, defender, field, typeMultiplier)

        val totalMultiplier = targetsMult * weatherMult * critMult * stabMult * typeMultiplier * burnMult * screensMult * otherMult

        // 8. Generate 16 Damage Rolls (85..100) per hit
        val singleHitRolls = (85..100).map { roll ->
            val rollMultiplier = roll / 100.0
            max(1, floor(baseDamage * totalMultiplier * rollMultiplier).toInt())
        }

        val hits = move.hitCount.coerceAtLeast(1)
        val rolls = singleHitRolls.map { it * hits }

        val minDamage = rolls.minOrNull() ?: 0
        val maxDamage = rolls.maxOrNull() ?: 0
        val avgDamage = rolls.average()

        val defenderHp = defenderStats.hp
        val minPercent = (minDamage.toDouble() / defenderHp) * 100.0
        val maxPercent = (maxDamage.toDouble() / defenderHp) * 100.0
        val avgPercent = (avgDamage / defenderHp) * 100.0

        // 9. Hazards and Recovery calculation for Defender
        val isGrounded = isPokemonGrounded(defender, field)
        val hasBoots = defender.item == BattleItem.HEAVY_DUTY_BOOTS || defender.ability == BattleAbility.MAGIC_GUARD

        val stealthRockDamage = if (!hasBoots && field.defenderStealthRock) {
            val rockMult = calculateTypeEffectiveness(
                moveType = PokemonType.ROCK,
                defPrimary = defenderPrimaryType,
                defSecondary = defenderSecondaryType,
                defAbility = defender.ability,
                defItem = defender.item,
                attAbility = BattleAbility.NONE
            )
            floor(defenderHp * (0.125 * rockMult)).toInt()
        } else 0

        val spikesDamage = if (!hasBoots && isGrounded && field.defenderSpikesLayers > 0) {
            val spikeFraction = when (field.defenderSpikesLayers) {
                1 -> 0.125
                2 -> 0.1667
                else -> 0.25
            }
            floor(defenderHp * spikeFraction).toInt()
        } else 0

        val totalHazards = stealthRockDamage + spikesDamage

        val leftoversHeal = if (defender.item == BattleItem.LEFTOVERS || defender.item == BattleItem.BLACK_SLUDGE) {
            floor(defenderHp / 16.0).toInt()
        } else 0

        val sitrusHeal = if (defender.item == BattleItem.SITRUS_BERRY) {
            floor(defenderHp / 4.0).toInt()
        } else 0

        // 10. KO Chance Simulation
        val (koText, ohkoChance, twoHkoChance, threeHkoChance, hitsRange) = analyzeKoProbability(
            rolls = singleHitRolls,
            hits = hits,
            defenderHp = defenderHp,
            hazards = totalHazards,
            leftoversHeal = leftoversHeal,
            sitrusHeal = sitrusHeal,
            hasFocusSash = defender.item == BattleItem.FOCUS_SASH && totalHazards == 0
        )

        // 11. Format Summary Formula
        val summaryText = buildSummaryFormula(
            attacker = attacker,
            defender = defender,
            move = move,
            field = field,
            minDmg = minDamage,
            maxDmg = maxDamage,
            minPct = minPercent,
            maxPct = maxPercent,
            koText = koText
        )

        return DamageRollResult(
            rolls = rolls,
            minDamage = minDamage,
            maxDamage = maxDamage,
            avgDamage = avgDamage,
            defenderMaxHp = defenderHp,
            minPercent = minPercent,
            maxPercent = maxPercent,
            avgPercent = avgPercent,
            koChanceText = koText,
            summaryFormulaText = summaryText,
            typeMultiplier = typeMultiplier,
            isStab = stabMult > 1.0,
            isCritical = isCritical,
            hitsTakenToKo = hitsRange,
            ohkoChance = ohkoChance,
            twoHkoChance = twoHkoChance,
            threeHkoChance = threeHkoChance,
            stealthRockDamage = stealthRockDamage,
            spikesDamage = spikesDamage,
            leftoversHeal = leftoversHeal
        )
    }

    private fun calculateEffectivePower(
        move: BattleMove,
        attacker: Combatant,
        defender: Combatant,
        field: BattleField
    ): Int {
        var power = move.basePower.toDouble()

        // Technician
        if (attacker.ability == BattleAbility.TECHNICIAN && power <= 60) {
            power *= 1.5
        }

        // Sharpness (slicing moves)
        if (attacker.ability == BattleAbility.SHARPNESS && move.isSlicing) {
            power *= 1.5
        }

        // Strong Jaw (biting moves)
        if (attacker.ability == BattleAbility.STRONG_JAW && move.isBiting) {
            power *= 1.5
        }

        // Tough Claws (contact moves)
        if (attacker.ability == BattleAbility.TOUGH_CLAWS && move.isContact) {
            power *= 1.3
        }

        // Mega Launcher (pulse moves)
        if (attacker.ability == BattleAbility.MEGA_LAUNCHER && (move.name.contains("pulse") || move.name.contains("aura") || move.name.contains("sphere"))) {
            power *= 1.5
        }

        // Sheer Force
        if (attacker.ability == BattleAbility.SHEER_FORCE && move.isSecondaryEffect) {
            power *= 1.3
        }

        // Pinch abilities (Blaze, Torrent, Overgrow, Swarm)
        if (attacker.ability == BattleAbility.BLAZE) {
            when (move.type) {
                PokemonType.FIRE, PokemonType.WATER, PokemonType.GRASS, PokemonType.BUG -> power *= 1.5
                else -> {}
            }
        }

        // Type boosters from items (Charcoal, Mystic Water, etc.)
        if (attacker.item.boostedType == move.type) {
            power *= 1.2
        }

        // Terrains
        val attackerGrounded = isPokemonGrounded(attacker, field)
        if (attackerGrounded) {
            when (field.terrain) {
                BattleTerrain.ELECTRIC -> if (move.type == PokemonType.ELECTRIC) power *= 1.3
                BattleTerrain.GRASSY -> {
                    if (move.type == PokemonType.GRASS) power *= 1.3
                    if (move.name == "earthquake" || move.name == "bulldoze" || move.name == "magnitude") power *= 0.5
                }
                BattleTerrain.PSYCHIC -> if (move.type == PokemonType.PSYCHIC) power *= 1.3
                BattleTerrain.MISTY -> if (move.type == PokemonType.DRAGON) power *= 0.5
                BattleTerrain.NONE -> {}
            }
        }

        return max(1, power.roundToInt())
    }

    private fun calculateEffectiveAttack(
        attacker: Combatant,
        defender: Combatant,
        move: BattleMove,
        field: BattleField,
        isCritical: Boolean,
        stats: CalculatedStats
    ): Int {
        val isSpecial = move.category == MoveCategory.SPECIAL
        val rawStat = if (isSpecial) stats.spAttack else stats.attack
        val statType = if (isSpecial) StatType.SP_ATTACK else StatType.ATTACK

        // Stat Stage Multiplier (Critical hits ignore negative attack stages)
        val stage = if (isSpecial) attacker.statStages.spAttack else attacker.statStages.attack
        val effectiveStage = if (isCritical && stage < 0) 0 else stage
        var attack = rawStat * getStageMultiplier(effectiveStage)

        // Unaware on defender ignores attacker's stat stage boosts
        if (defender.ability == BattleAbility.UNAWARE && effectiveStage > 0) {
            attack = rawStat.toDouble()
        }

        // Items
        if (!isSpecial && attacker.item == BattleItem.CHOICE_BAND) attack *= 1.5
        if (isSpecial && attacker.item == BattleItem.CHOICE_SPECS) attack *= 1.5
        if (attacker.item == BattleItem.BOOSTER_ENERGY && (attacker.ability == BattleAbility.PROTOSYNTHESIS || attacker.ability == BattleAbility.QUARK_DRIVE)) {
            attack *= 1.3
        }

        // Abilities
        if (!isSpecial && attacker.ability == BattleAbility.HUGE_POWER) attack *= 2.0
        if (!isSpecial && attacker.ability == BattleAbility.GUTS && (attacker.isBurned || attacker.isParalyzed)) attack *= 1.5
        if (isSpecial && attacker.ability == BattleAbility.SOLAR_POWER && (field.weather == BattleWeather.SUN || field.weather == BattleWeather.HARSH_SUN)) attack *= 1.5
        if (attacker.ability == BattleAbility.TRANSISTOR && move.type == PokemonType.ELECTRIC) attack *= 1.3
        if (attacker.ability == BattleAbility.DRAGONS_MAW && move.type == PokemonType.DRAGON) attack *= 1.5
        if (attacker.ability == BattleAbility.STEELY_SPIRIT && move.type == PokemonType.STEEL) attack *= 1.5

        // Ruin Abilities
        if (!isSpecial && defender.ability == BattleAbility.TABLETS_OF_RUIN) attack *= 0.75
        if (isSpecial && defender.ability == BattleAbility.VESSEL_OF_RUIN) attack *= 0.75

        return max(1, attack.roundToInt())
    }

    private fun calculateEffectiveDefense(
        attacker: Combatant,
        defender: Combatant,
        move: BattleMove,
        field: BattleField,
        isCritical: Boolean,
        stats: CalculatedStats
    ): Int {
        val isSpecial = move.category == MoveCategory.SPECIAL && move.name != "psystrike" && move.name != "psyshock" && move.name != "secret-sword"
        val rawStat = if (isSpecial) stats.spDefense else stats.defense

        // Stat Stage Multiplier (Critical hits ignore positive defense stages)
        val stage = if (isSpecial) defender.statStages.spDefense else defender.statStages.defense
        val effectiveStage = if (isCritical && stage > 0) 0 else stage
        var defense = rawStat * getStageMultiplier(effectiveStage)

        // Items
        if (isSpecial && defender.item == BattleItem.ASSAULT_VEST) defense *= 1.5
        if (defender.item == BattleItem.EVIOLITE) defense *= 1.5

        // Abilities
        if (!isSpecial && defender.ability == BattleAbility.FUR_COAT) defense *= 2.0
        if (isSpecial && defender.ability == BattleAbility.ICE_SCALES) defense *= 2.0

        // Ruin abilities
        if (!isSpecial && attacker.ability == BattleAbility.SWORD_OF_RUIN) defense *= 0.75
        if (isSpecial && attacker.ability == BattleAbility.BEADS_OF_RUIN) defense *= 0.75

        // Weather boosts
        val defPrimary = defender.teraType ?: defender.pokemon.primaryType
        val defSecondary = if (defender.teraType != null) null else defender.pokemon.secondaryType
        val isRock = defPrimary == PokemonType.ROCK || defSecondary == PokemonType.ROCK
        val isIce = defPrimary == PokemonType.ICE || defSecondary == PokemonType.ICE

        if (isSpecial && isRock && field.weather == BattleWeather.SANDSTORM) defense *= 1.5
        if (!isSpecial && isIce && field.weather == BattleWeather.SNOW) defense *= 1.5

        return max(1, defense.roundToInt())
    }

    private fun calculateTypeEffectiveness(
        moveType: PokemonType,
        defPrimary: PokemonType,
        defSecondary: PokemonType?,
        defAbility: BattleAbility,
        defItem: BattleItem,
        attAbility: BattleAbility
    ): Double {
        // Ability immunities
        if (defAbility == BattleAbility.LEVITATE && moveType == PokemonType.GROUND) return 0.0
        if (defItem == BattleItem.AIR_BALLOON && moveType == PokemonType.GROUND) return 0.0
        if (defAbility == BattleAbility.WATER_ABSORB && moveType == PokemonType.WATER) return 0.0
        if (defAbility == BattleAbility.VOLT_ABSORB && moveType == PokemonType.ELECTRIC) return 0.0
        if (defAbility == BattleAbility.FLASH_FIRE && moveType == PokemonType.FIRE) return 0.0
        if (defAbility == BattleAbility.EARTH_EATER && moveType == PokemonType.GROUND) return 0.0
        if (defAbility == BattleAbility.SAP_SIPPER && moveType == PokemonType.GRASS) return 0.0

        val matchups = TypeMatchupEngine.calculateDefensiveMatchups(defPrimary, defSecondary)
        var mult = matchups.firstOrNull { it.type == moveType }?.multiplier ?: 1.0

        // Ability modifiers
        if (defAbility == BattleAbility.THICK_FAT && (moveType == PokemonType.FIRE || moveType == PokemonType.ICE)) {
            mult *= 0.5
        }
        if (defAbility == BattleAbility.HEATPROOF && moveType == PokemonType.FIRE) {
            mult *= 0.5
        }
        if (defAbility == BattleAbility.FLUFFY && moveType == PokemonType.FIRE) {
            mult *= 2.0
        }
        if (attAbility == BattleAbility.TINTED_LENS && mult < 1.0 && mult > 0.0) {
            mult *= 2.0
        }
        if (defAbility == BattleAbility.SOLID_ROCK && mult > 1.0) {
            mult *= 0.75
        }

        return mult
    }

    private fun calculateStabMultiplier(moveType: PokemonType, attacker: Combatant): Double {
        val primary = attacker.pokemon.primaryType
        val secondary = attacker.pokemon.secondaryType
        val tera = attacker.teraType
        val isAdaptability = attacker.ability == BattleAbility.ADAPTABILITY

        val isBaseType = moveType == primary || moveType == secondary
        val isTeraType = tera != null && moveType == tera

        return when {
            isTeraType && isBaseType -> if (isAdaptability) 2.25 else 2.0
            isTeraType -> 1.5
            isBaseType -> if (isAdaptability) 2.0 else 1.5
            attacker.ability == BattleAbility.LIBERO -> 1.5
            else -> 1.0
        }
    }

    private fun calculateWeatherMultiplier(moveType: PokemonType, weather: BattleWeather): Double {
        return when (weather) {
            BattleWeather.SUN -> when (moveType) {
                PokemonType.FIRE -> 1.5
                PokemonType.WATER -> 0.5
                else -> 1.0
            }
            BattleWeather.RAIN -> when (moveType) {
                PokemonType.WATER -> 1.5
                PokemonType.FIRE -> 0.5
                else -> 1.0
            }
            BattleWeather.HARSH_SUN -> when (moveType) {
                PokemonType.FIRE -> 1.5
                PokemonType.WATER -> 0.0
                else -> 1.0
            }
            BattleWeather.HEAVY_RAIN -> when (moveType) {
                PokemonType.WATER -> 1.5
                PokemonType.FIRE -> 0.0
                else -> 1.0
            }
            else -> 1.0
        }
    }

    private fun calculateScreensMultiplier(
        category: MoveCategory,
        defender: Combatant,
        field: BattleField,
        isCritical: Boolean
    ): Double {
        if (isCritical) return 1.0 // Crits ignore screens
        val isDoubles = field.isDoubles

        var mult = 1.0
        if (field.defenderAuroraVeil) {
            mult *= if (isDoubles) 0.66 else 0.5
        } else {
            if (category == MoveCategory.PHYSICAL && field.defenderReflect) {
                mult *= if (isDoubles) 0.66 else 0.5
            }
            if (category == MoveCategory.SPECIAL && field.defenderLightScreen) {
                mult *= if (isDoubles) 0.66 else 0.5
            }
        }
        if (field.defenderFriendGuard) {
            mult *= 0.75
        }
        return mult
    }

    private fun calculateOtherModifiers(
        move: BattleMove,
        attacker: Combatant,
        defender: Combatant,
        field: BattleField,
        typeMultiplier: Double
    ): Double {
        var mult = 1.0

        // Attacker Item
        if (attacker.item == BattleItem.LIFE_ORB) mult *= 1.3
        if (attacker.item == BattleItem.EXPERT_BELT && typeMultiplier > 1.0) mult *= 1.2
        if (attacker.item == BattleItem.MUSCLE_BAND && move.category == MoveCategory.PHYSICAL) mult *= 1.1
        if (attacker.item == BattleItem.WISE_GLASSES && move.category == MoveCategory.SPECIAL) mult *= 1.1

        // Defender Ability
        if (defender.ability == BattleAbility.MULTISCALE && defender.currentHpPercent >= 99.9) {
            mult *= 0.5
        }
        if (defender.ability == BattleAbility.FLUFFY && move.isContact) {
            mult *= 0.5
        }

        // Attacker Helping Hand
        if (field.attackerHelpingHand) mult *= 1.5

        return mult
    }

    private fun isPokemonGrounded(combatant: Combatant, field: BattleField): Boolean {
        if (combatant.item == BattleItem.AIR_BALLOON) return false
        if (combatant.ability == BattleAbility.LEVITATE) return false
        val primary = combatant.teraType ?: combatant.pokemon.primaryType
        val secondary = if (combatant.teraType != null) null else combatant.pokemon.secondaryType
        if (primary == PokemonType.FLYING || secondary == PokemonType.FLYING) return false
        return true
    }

    private fun getStageMultiplier(stage: Int): Double {
        val s = stage.coerceIn(-6, 6)
        return when {
            s >= 0 -> (2 + s) / 2.0
            else -> 2.0 / (2 - s)
        }
    }

    private data class KoAnalysis(
        val text: String,
        val ohko: Double,
        val twoHko: Double,
        val threeHko: Double,
        val hitsRange: IntRange
    )

    private fun analyzeKoProbability(
        rolls: List<Int>,
        hits: Int,
        defenderHp: Int,
        hazards: Int,
        leftoversHeal: Int,
        sitrusHeal: Int,
        hasFocusSash: Boolean
    ): KoAnalysis {
        val effectiveStartHp = max(1, defenderHp - hazards)

        // 1. OHKO Chance
        var ohkoSuccessCount = 0
        rolls.forEach { roll ->
            val totalDamage = roll * hits
            if (totalDamage >= effectiveStartHp && !hasFocusSash) {
                ohkoSuccessCount++
            }
        }
        val ohkoChance = (ohkoSuccessCount.toDouble() / rolls.size) * 100.0

        // 2. 2HKO Chance (256 pairs of rolls)
        var twoHkoSuccessCount = 0
        var totalCombinations = 0
        rolls.forEach { roll1 ->
            val dmg1 = roll1 * hits
            val hpAfterTurn1 = effectiveStartHp - dmg1
            val healedHp = if (hpAfterTurn1 > 0) {
                var hp = hpAfterTurn1 + leftoversHeal
                if (hpAfterTurn1 <= defenderHp / 2 && sitrusHeal > 0) {
                    hp += sitrusHeal
                }
                min(defenderHp, hp)
            } else 0

            rolls.forEach { roll2 ->
                val dmg2 = roll2 * hits
                totalCombinations++
                if (dmg1 >= effectiveStartHp || dmg2 >= healedHp) {
                    twoHkoSuccessCount++
                }
            }
        }
        val twoHkoChance = (twoHkoSuccessCount.toDouble() / totalCombinations) * 100.0

        // 3. Estimate 3HKO
        val minDmg = (rolls.minOrNull() ?: 0) * hits
        val maxDmg = (rolls.maxOrNull() ?: 0) * hits
        val threeHkoChance = when {
            twoHkoChance >= 99.9 -> 100.0
            minDmg * 3 >= effectiveStartHp -> 100.0
            maxDmg * 3 < effectiveStartHp -> 0.0
            else -> 60.0
        }

        // Format Text
        val hazardExtra = if (hazards > 0) " after hazards" else ""
        val text = when {
            ohkoChance >= 100.0 -> "Guaranteed OHKO$hazardExtra"
            ohkoChance > 0.0 -> "${"%.1f".format(ohkoChance)}% chance to OHKO$hazardExtra"
            twoHkoChance >= 100.0 -> "Guaranteed 2HKO$hazardExtra"
            twoHkoChance > 0.0 -> "${"%.1f".format(twoHkoChance)}% chance to 2HKO$hazardExtra"
            threeHkoChance >= 100.0 -> "Guaranteed 3HKO$hazardExtra"
            threeHkoChance > 0.0 -> "Possible 3HKO (${"%.1f".format(threeHkoChance)}%)"
            minDmg * 4 >= effectiveStartHp -> "Guaranteed 4HKO"
            else -> "Possible 5+ HKO"
        }

        val hitsRange = when {
            ohkoChance >= 100.0 -> 1..1
            twoHkoChance >= 100.0 -> 1..2
            threeHkoChance >= 100.0 -> 2..3
            else -> 3..5
        }

        return KoAnalysis(text, ohkoChance, twoHkoChance, threeHkoChance, hitsRange)
    }

    private fun buildSummaryFormula(
        attacker: Combatant,
        defender: Combatant,
        move: BattleMove,
        field: BattleField,
        minDmg: Int,
        maxDmg: Int,
        minPct: Double,
        maxPct: Double,
        koText: String
    ): String {
        val attName = attacker.pokemon.capitalizedName
        val defName = defender.pokemon.capitalizedName
        val moveName = move.displayName

        val attBoost = when {
            attacker.statStages.attack > 0 -> "+${attacker.statStages.attack} "
            attacker.statStages.attack < 0 -> "${attacker.statStages.attack} "
            else -> ""
        }

        val itemText = if (attacker.item != BattleItem.NONE) "${attacker.item.displayName} " else ""
        val weatherText = if (field.weather != BattleWeather.CLEAR) " in ${field.weather.displayName}" else ""

        return "$attBoost$itemText$attName $moveName vs $defName$weatherText: $minDmg-$maxDmg (${"%.1f".format(minPct)} - ${"%.1f".format(maxPct)}%) -- $koText"
    }
}
