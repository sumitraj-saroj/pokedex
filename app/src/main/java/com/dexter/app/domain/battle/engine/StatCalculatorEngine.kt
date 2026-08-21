package com.dexter.app.domain.battle.engine

import com.dexter.app.domain.battle.model.CalculatedStats
import com.dexter.app.domain.battle.model.PokemonNature
import com.dexter.app.domain.battle.model.StatSpread
import com.dexter.app.domain.battle.model.StatType
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonStats
import kotlin.math.floor

object StatCalculatorEngine {

    /**
     * Calculates the in-game HP stat.
     * Formula: floor((2 * Base + IV + floor(EV / 4)) * Level / 100) + Level + 10
     * Special case: Shedinja (ID 292) is always 1 HP.
     */
    fun calculateHp(
        baseHp: Int,
        iv: Int,
        ev: Int,
        level: Int,
        isShedinja: Boolean = false
    ): Int {
        if (isShedinja) return 1
        val coercedIv = iv.coerceIn(0, 31)
        val coercedEv = ev.coerceIn(0, 252)
        val coercedLevel = level.coerceIn(1, 100)

        val evComponent = floor(coercedEv / 4.0)
        val baseComponent = (2 * baseHp + coercedIv + evComponent) * coercedLevel
        return (floor(baseComponent / 100.0) + coercedLevel + 10).toInt()
    }

    /**
     * Calculates non-HP stats (Attack, Defense, Sp. Attack, Sp. Defense, Speed).
     * Formula: floor((floor((2 * Base + IV + floor(EV / 4)) * Level / 100) + 5) * NatureMultiplier)
     */
    fun calculateOtherStat(
        baseStat: Int,
        iv: Int,
        ev: Int,
        level: Int,
        natureMultiplier: Double
    ): Int {
        val coercedIv = iv.coerceIn(0, 31)
        val coercedEv = ev.coerceIn(0, 252)
        val coercedLevel = level.coerceIn(1, 100)

        val evComponent = floor(coercedEv / 4.0)
        val baseComponent = floor(((2 * baseStat + coercedIv + evComponent) * coercedLevel) / 100.0) + 5
        return floor(baseComponent * natureMultiplier).toInt()
    }

    /**
     * Calculates all 6 final in-game stats for a Pokémon given its base stats, IVs, EVs, Level, and Nature.
     */
    fun calculateAllStats(
        baseStats: PokemonStats,
        ivs: StatSpread,
        evs: StatSpread,
        level: Int,
        nature: PokemonNature,
        isShedinja: Boolean = false
    ): CalculatedStats {
        val hp = calculateHp(baseStats.hp, ivs.hp, evs.hp, level, isShedinja)
        val attack = calculateOtherStat(baseStats.attack, ivs.attack, evs.attack, level, nature.getMultiplier(StatType.ATTACK))
        val defense = calculateOtherStat(baseStats.defense, ivs.defense, evs.defense, level, nature.getMultiplier(StatType.DEFENSE))
        val spAttack = calculateOtherStat(baseStats.spAttack, ivs.spAttack, evs.spAttack, level, nature.getMultiplier(StatType.SP_ATTACK))
        val spDefense = calculateOtherStat(baseStats.spDefense, ivs.spDefense, evs.spDefense, level, nature.getMultiplier(StatType.SP_DEFENSE))
        val speed = calculateOtherStat(baseStats.speed, ivs.speed, evs.speed, level, nature.getMultiplier(StatType.SPEED))

        return CalculatedStats(
            hp = hp,
            attack = attack,
            defense = defense,
            spAttack = spAttack,
            spDefense = spDefense,
            speed = speed
        )
    }

    /**
     * Generates a standard Pokémon Showdown export format string.
     */
    fun exportToShowdown(
        pokemon: Pokemon,
        level: Int,
        nature: PokemonNature,
        item: String? = null,
        ability: String? = null,
        teraType: String? = null,
        evs: StatSpread,
        ivs: StatSpread,
        moves: List<String> = emptyList()
    ): String {
        val sb = StringBuilder()
        val name = pokemon.capitalizedName
        val itemStr = if (!item.isNullOrBlank() && item != "None") " @ $item" else ""
        sb.appendLine("$name$itemStr")

        if (!ability.isNullOrBlank() && ability != "None") {
            sb.appendLine("Ability: $ability")
        }
        if (level != 100) {
            sb.appendLine("Level: $level")
        }
        if (!teraType.isNullOrBlank()) {
            sb.appendLine("Tera Type: $teraType")
        }

        // EVs line
        val evParts = mutableListOf<String>()
        if (evs.hp > 0) evParts.add("${evs.hp} HP")
        if (evs.attack > 0) evParts.add("${evs.attack} Atk")
        if (evs.defense > 0) evParts.add("${evs.defense} Def")
        if (evs.spAttack > 0) evParts.add("${evs.spAttack} SpA")
        if (evs.spDefense > 0) evParts.add("${evs.spDefense} SpD")
        if (evs.speed > 0) evParts.add("${evs.speed} Spe")
        if (evParts.isNotEmpty()) {
            sb.appendLine("EVs: ${evParts.joinToString(" / ")}")
        }

        // Nature line
        sb.appendLine("${nature.displayName} Nature")

        // IVs line (only if not 31)
        val ivParts = mutableListOf<String>()
        if (ivs.hp != 31) ivParts.add("${ivs.hp} HP")
        if (ivs.attack != 31) ivParts.add("${ivs.attack} Atk")
        if (ivs.defense != 31) ivParts.add("${ivs.defense} Def")
        if (ivs.spAttack != 31) ivParts.add("${ivs.spAttack} SpA")
        if (ivs.spDefense != 31) ivParts.add("${ivs.spDefense} SpD")
        if (ivs.speed != 31) ivParts.add("${ivs.speed} Spe")
        if (ivParts.isNotEmpty()) {
            sb.appendLine("IVs: ${ivParts.joinToString(" / ")}")
        }

        // Moves
        if (moves.isNotEmpty()) {
            moves.forEach { move ->
                sb.appendLine("- $move")
            }
        }

        return sb.toString().trimEnd()
    }
}
