package com.dexter.app.domain.battle.engine

import com.dexter.app.domain.battle.model.PokemonNature
import com.dexter.app.domain.battle.model.SpeedTierCategory
import com.dexter.app.domain.battle.model.SpeedTierEntry
import com.dexter.app.domain.battle.model.StatType
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonType
import kotlin.math.floor

object SpeedTierEngine {

    /**
     * Calculates the exact final in-game Speed value taking all modifiers into account.
     */
    fun calculateSpeedStat(
        baseSpeed: Int,
        iv: Int = 31,
        ev: Int = 252,
        level: Int = 50,
        nature: PokemonNature = PokemonNature.JOLLY,
        hasChoiceScarf: Boolean = false,
        hasBoosterEnergySpeed: Boolean = false,
        hasSwiftSwimOrChlorophyll: Boolean = false,
        hasTailwind: Boolean = false,
        statStage: Int = 0,
        isParalyzed: Boolean = false
    ): Int {
        // Base stat with IVs, EVs, Level, Nature
        val baseCalculated = StatCalculatorEngine.calculateOtherStat(
            baseStat = baseSpeed,
            iv = iv,
            ev = ev,
            level = level,
            natureMultiplier = nature.getMultiplier(StatType.SPEED)
        )

        var speed = baseCalculated.toDouble()

        // Stat Stage (-6 to +6)
        val stage = statStage.coerceIn(-6, 6)
        val stageMult = when {
            stage >= 0 -> (2 + stage) / 2.0
            else -> 2.0 / (2 - stage)
        }
        speed *= stageMult

        // Items
        if (hasChoiceScarf) speed *= 1.5
        if (hasBoosterEnergySpeed) speed *= 1.5

        // Abilities
        if (hasSwiftSwimOrChlorophyll) speed *= 2.0

        // Field
        if (hasTailwind) speed *= 2.0

        // Status
        if (isParalyzed) speed *= 0.5

        return floor(speed).toInt()
    }

    /**
     * Generates a standard competitive benchmark list for the specified level (50 or 100).
     */
    fun getCompetitiveBenchmarks(level: Int = 50): List<SpeedTierEntry> {
        val rawBenchmarks = listOf(
            // +2 Speed / Weather / Booster Spe
            BenchmarkDef("Regieleki", 894, PokemonType.ELECTRIC, null, 200, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_PLUS_TWO, "Transistor + Swift Swim / +2 Boost (252+ Spe)"),
            BenchmarkDef("Iron Bundle", 992, PokemonType.ICE, PokemonType.WATER, 136, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_PLUS_TWO, "Booster Energy Speed (+50% Spe, 252+ Spe)", hasBoosterEnergy = true),
            BenchmarkDef("Flutter Mane", 987, PokemonType.GHOST, PokemonType.FAIRY, 135, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_PLUS_TWO, "Booster Energy Speed (+50% Spe, 252+ Spe)", hasBoosterEnergy = true),
            BenchmarkDef("Roaring Moon", 1005, PokemonType.DRAGON, PokemonType.DARK, 119, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_PLUS_TWO, "Booster Energy / Dragon Dance +1 (252+ Spe)", hasBoosterEnergy = true),
            BenchmarkDef("Barraskewda", 834, PokemonType.WATER, null, 136, 252, 31, PokemonNature.ADAMANT, SpeedTierCategory.TIER_PLUS_TWO, "Swift Swim Rain (252 Spe)", hasSwiftSwim = true),

            // +1 Choice Scarf / Tailwind
            BenchmarkDef("Dragapult", 887, PokemonType.DRAGON, PokemonType.GHOST, 142, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_PLUS_ONE, "Choice Scarf (252+ Spe)", hasScarf = true),
            BenchmarkDef("Koraidon", 1007, PokemonType.FIGHTING, PokemonType.DRAGON, 135, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_PLUS_ONE, "Choice Scarf (252+ Spe)", hasScarf = true),
            BenchmarkDef("Miraidon", 1008, PokemonType.ELECTRIC, PokemonType.DRAGON, 135, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_PLUS_ONE, "Choice Scarf (252+ Spe)", hasScarf = true),
            BenchmarkDef("Meowscarada", 908, PokemonType.GRASS, PokemonType.DARK, 123, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_PLUS_ONE, "Choice Scarf (252+ Spe)", hasScarf = true),
            BenchmarkDef("Garchomp", 445, PokemonType.DRAGON, PokemonType.GROUND, 102, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_PLUS_ONE, "Choice Scarf (252+ Spe)", hasScarf = true),
            BenchmarkDef("Landorus-Therian", 645, PokemonType.GROUND, PokemonType.FLYING, 91, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_PLUS_ONE, "Choice Scarf (252+ Spe)", hasScarf = true),
            BenchmarkDef("Urshifu", 892, PokemonType.FIGHTING, PokemonType.DARK, 97, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_PLUS_ONE, "Choice Scarf (252+ Spe)", hasScarf = true),
            BenchmarkDef("Gholdengo", 1000, PokemonType.STEEL, PokemonType.GHOST, 84, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_PLUS_ONE, "Choice Scarf (252+ Spe)", hasScarf = true),

            // Max Speed Positive Nature (252+)
            BenchmarkDef("Regieleki", 894, PokemonType.ELECTRIC, null, 200, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Timid (252+ Spe)"),
            BenchmarkDef("Deoxys-Speed", 386, PokemonType.PSYCHIC, null, 180, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Jolly (252+ Spe)"),
            BenchmarkDef("Ninjask", 291, PokemonType.BUG, PokemonType.FLYING, 160, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Jolly (252+ Spe)"),
            BenchmarkDef("Pheromosa", 795, PokemonType.BUG, PokemonType.FIGHTING, 151, 252, 31, PokemonNature.NAIVE, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Naive (252+ Spe)"),
            BenchmarkDef("Dragapult", 887, PokemonType.DRAGON, PokemonType.GHOST, 142, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Jolly (252+ Spe)"),
            BenchmarkDef("Zeraora", 807, PokemonType.ELECTRIC, null, 143, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Jolly (252+ Spe)"),
            BenchmarkDef("Iron Bundle", 992, PokemonType.ICE, PokemonType.WATER, 136, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Timid (252+ Spe)"),
            BenchmarkDef("Flutter Mane", 987, PokemonType.GHOST, PokemonType.FAIRY, 135, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Timid (252+ Spe)"),
            BenchmarkDef("Chien-Pao", 1002, PokemonType.DARK, PokemonType.ICE, 135, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Jolly (252+ Spe)"),
            BenchmarkDef("Koraidon", 1007, PokemonType.FIGHTING, PokemonType.DRAGON, 135, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Jolly (252+ Spe)"),
            BenchmarkDef("Miraidon", 1008, PokemonType.ELECTRIC, PokemonType.DRAGON, 135, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Timid (252+ Spe)"),
            BenchmarkDef("Meowscarada", 908, PokemonType.GRASS, PokemonType.DARK, 123, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Jolly (252+ Spe)"),
            BenchmarkDef("Greninja", 658, PokemonType.WATER, PokemonType.DARK, 122, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Timid (252+ Spe)"),
            BenchmarkDef("Cinderace", 815, PokemonType.FIRE, null, 119, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Jolly (252+ Spe)"),
            BenchmarkDef("Gengar", 94, PokemonType.GHOST, PokemonType.POISON, 110, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Timid (252+ Spe)"),
            BenchmarkDef("Latios", 381, PokemonType.DRAGON, PokemonType.PSYCHIC, 110, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Timid (252+ Spe)"),
            BenchmarkDef("Garchomp", 445, PokemonType.DRAGON, PokemonType.GROUND, 102, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Jolly (252+ Spe)"),
            BenchmarkDef("Zapdos", 145, PokemonType.ELECTRIC, PokemonType.FLYING, 100, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Timid (252+ Spe)"),
            BenchmarkDef("Charizard", 6, PokemonType.FIRE, PokemonType.FLYING, 100, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Timid (252+ Spe)"),
            BenchmarkDef("Volcarona", 637, PokemonType.BUG, PokemonType.FIRE, 100, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Timid (252+ Spe)"),
            BenchmarkDef("Urshifu", 892, PokemonType.FIGHTING, PokemonType.DARK, 97, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Jolly (252+ Spe)"),
            BenchmarkDef("Mimikyu", 778, PokemonType.GHOST, PokemonType.FAIRY, 96, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Jolly (252+ Spe)"),
            BenchmarkDef("Landorus-Therian", 645, PokemonType.GROUND, PokemonType.FLYING, 91, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Jolly (252+ Spe)"),
            BenchmarkDef("Great Tusk", 984, PokemonType.GROUND, PokemonType.FIGHTING, 87, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Jolly (252+ Spe)"),
            BenchmarkDef("Rillaboom", 812, PokemonType.GRASS, null, 85, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Jolly (252+ Spe)"),
            BenchmarkDef("Gholdengo", 1000, PokemonType.STEEL, PokemonType.GHOST, 84, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Timid (252+ Spe)"),
            BenchmarkDef("Dragonite", 149, PokemonType.DRAGON, PokemonType.FLYING, 80, 252, 31, PokemonNature.JOLLY, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Jolly (252+ Spe)"),
            BenchmarkDef("Heatran", 485, PokemonType.FIRE, PokemonType.STEEL, 77, 252, 31, PokemonNature.TIMID, SpeedTierCategory.TIER_MAX_POSITIVE, "Max Speed Timid (252+ Spe)"),

            // Max Speed Neutral Nature (252)
            BenchmarkDef("Dragapult", 887, PokemonType.DRAGON, PokemonType.GHOST, 142, 252, 31, PokemonNature.MODEST, SpeedTierCategory.TIER_MAX_NEUTRAL, "Max Speed Modest/Adamant (252 Spe)"),
            BenchmarkDef("Flutter Mane", 987, PokemonType.GHOST, PokemonType.FAIRY, 135, 252, 31, PokemonNature.MODEST, SpeedTierCategory.TIER_MAX_NEUTRAL, "Max Speed Modest (252 Spe)"),
            BenchmarkDef("Chien-Pao", 1002, PokemonType.DARK, PokemonType.ICE, 135, 252, 31, PokemonNature.ADAMANT, SpeedTierCategory.TIER_MAX_NEUTRAL, "Max Speed Adamant (252 Spe)"),
            BenchmarkDef("Garchomp", 445, PokemonType.DRAGON, PokemonType.GROUND, 102, 252, 31, PokemonNature.ADAMANT, SpeedTierCategory.TIER_MAX_NEUTRAL, "Max Speed Adamant (252 Spe)"),
            BenchmarkDef("Landorus-Therian", 645, PokemonType.GROUND, PokemonType.FLYING, 91, 252, 31, PokemonNature.ADAMANT, SpeedTierCategory.TIER_MAX_NEUTRAL, "Max Speed Adamant (252 Spe)"),
            BenchmarkDef("Urshifu", 892, PokemonType.FIGHTING, PokemonType.DARK, 97, 252, 31, PokemonNature.ADAMANT, SpeedTierCategory.TIER_MAX_NEUTRAL, "Max Speed Adamant (252 Spe)"),
            BenchmarkDef("Great Tusk", 984, PokemonType.GROUND, PokemonType.FIGHTING, 87, 252, 31, PokemonNature.ADAMANT, SpeedTierCategory.TIER_MAX_NEUTRAL, "Max Speed Adamant (252 Spe)"),
            BenchmarkDef("Rillaboom", 812, PokemonType.GRASS, null, 85, 252, 31, PokemonNature.ADAMANT, SpeedTierCategory.TIER_MAX_NEUTRAL, "Max Speed Adamant (252 Spe)"),
            BenchmarkDef("Gholdengo", 1000, PokemonType.STEEL, PokemonType.GHOST, 84, 252, 31, PokemonNature.MODEST, SpeedTierCategory.TIER_MAX_NEUTRAL, "Max Speed Modest (252 Spe)"),
            BenchmarkDef("Dragonite", 149, PokemonType.DRAGON, PokemonType.FLYING, 80, 252, 31, PokemonNature.ADAMANT, SpeedTierCategory.TIER_MAX_NEUTRAL, "Max Speed Adamant (252 Spe)"),

            // Bulky / Standard (0 - 100 EVs)
            BenchmarkDef("Garchomp", 445, PokemonType.DRAGON, PokemonType.GROUND, 102, 0, 31, PokemonNature.HARDY, SpeedTierCategory.TIER_BULKY, "Uninvested (0 Spe)"),
            BenchmarkDef("Landorus-Therian", 645, PokemonType.GROUND, PokemonType.FLYING, 91, 0, 31, PokemonNature.IMPISH, SpeedTierCategory.TIER_BULKY, "Bulky Impish (0 Spe)"),
            BenchmarkDef("Rotom-Wash", 479, PokemonType.ELECTRIC, PokemonType.WATER, 86, 0, 31, PokemonNature.BOLD, SpeedTierCategory.TIER_BULKY, "Bulky Bold (0 Spe)"),
            BenchmarkDef("Corviknight", 823, PokemonType.FLYING, PokemonType.STEEL, 67, 0, 31, PokemonNature.IMPISH, SpeedTierCategory.TIER_BULKY, "Bulky Impish (0 Spe)"),
            BenchmarkDef("Skeledirge", 911, PokemonType.FIRE, PokemonType.GHOST, 66, 0, 31, PokemonNature.BOLD, SpeedTierCategory.TIER_BULKY, "Uninvested (0 Spe)"),
            BenchmarkDef("Tyranitar", 248, PokemonType.ROCK, PokemonType.DARK, 61, 0, 31, PokemonNature.CAREFUL, SpeedTierCategory.TIER_BULKY, "Uninvested (0 Spe)"),
            BenchmarkDef("Incineroar", 727, PokemonType.FIRE, PokemonType.DARK, 60, 0, 31, PokemonNature.CAREFUL, SpeedTierCategory.TIER_BULKY, "VGC Bulky Support (0 Spe)"),
            BenchmarkDef("Kingambit", 983, PokemonType.DARK, PokemonType.STEEL, 50, 0, 31, PokemonNature.ADAMANT, SpeedTierCategory.TIER_BULKY, "Uninvested Adamant (0 Spe)"),
            BenchmarkDef("Toxapex", 748, PokemonType.POISON, PokemonType.WATER, 35, 0, 31, PokemonNature.BOLD, SpeedTierCategory.TIER_BULKY, "Bulky Bold (0 Spe)"),
            BenchmarkDef("Dondozo", 977, PokemonType.WATER, null, 35, 0, 31, PokemonNature.IMPISH, SpeedTierCategory.TIER_BULKY, "Bulky Impish (0 Spe)"),

            // Min Speed (0 IVs, -Nature / Trick Room)
            BenchmarkDef("Kingambit", 983, PokemonType.DARK, PokemonType.STEEL, 50, 0, 0, PokemonNature.BRAVE, SpeedTierCategory.TIER_MIN_SPEED, "Min Speed Brave (0 Spe IVs)"),
            BenchmarkDef("Iron Hands", 994, PokemonType.FIGHTING, PokemonType.ELECTRIC, 50, 0, 0, PokemonNature.BRAVE, SpeedTierCategory.TIER_MIN_SPEED, "Min Speed Brave (0 Spe IVs)"),
            BenchmarkDef("Ursaluna", 901, PokemonType.GROUND, PokemonType.NORMAL, 50, 0, 0, PokemonNature.BRAVE, SpeedTierCategory.TIER_MIN_SPEED, "Min Speed Trick Room (0 Spe IVs)"),
            BenchmarkDef("Ting-Lu", 1003, PokemonType.DARK, PokemonType.GROUND, 45, 0, 0, PokemonNature.RELAXED, SpeedTierCategory.TIER_MIN_SPEED, "Min Speed (0 Spe IVs)"),
            BenchmarkDef("Amoonguss", 591, PokemonType.GRASS, PokemonType.POISON, 30, 0, 0, PokemonNature.SASSY, SpeedTierCategory.TIER_MIN_SPEED, "Min Speed Sassy (0 Spe IVs / Trick Room)"),
            BenchmarkDef("Hatterene", 858, PokemonType.PSYCHIC, PokemonType.FAIRY, 29, 0, 0, PokemonNature.QUIET, SpeedTierCategory.TIER_MIN_SPEED, "Min Speed Quiet (0 Spe IVs / Trick Room)"),
            BenchmarkDef("Torkoal", 324, PokemonType.FIRE, null, 20, 0, 0, PokemonNature.QUIET, SpeedTierCategory.TIER_MIN_SPEED, "Min Speed Quiet (0 Spe IVs / Drought)")
        )

        return rawBenchmarks.map { def ->
            val finalSpeed = calculateSpeedStat(
                baseSpeed = def.baseSpeed,
                iv = def.iv,
                ev = def.ev,
                level = level,
                nature = def.nature,
                hasChoiceScarf = def.hasScarf,
                hasBoosterEnergySpeed = def.hasBoosterEnergy,
                hasSwiftSwimOrChlorophyll = def.hasSwiftSwim
            )

            SpeedTierEntry(
                id = "${def.pokemonId}_${def.category.name}_${def.spreadDescription.hashCode()}",
                pokemonId = def.pokemonId,
                pokemonName = def.name,
                spriteUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${def.pokemonId}.png",
                primaryType = def.primaryType,
                secondaryType = def.secondaryType,
                baseSpeed = def.baseSpeed,
                calculatedSpeed = finalSpeed,
                category = def.category,
                spreadDescription = def.spreadDescription,
                level = level,
                nature = def.nature,
                ev = def.ev,
                iv = def.iv,
                stage = 0,
                item = if (def.hasScarf) "Choice Scarf" else if (def.hasBoosterEnergy) "Booster Energy" else null,
                isUserPokemon = false
            )
        }.sortedByDescending { it.calculatedSpeed }
    }

    /**
     * Integrates the user's customized Pokémon into the speed ladder.
     */
    fun createLadderWithUserPokemon(
        benchmarks: List<SpeedTierEntry>,
        userPokemon: Pokemon,
        level: Int = 50,
        nature: PokemonNature = PokemonNature.JOLLY,
        iv: Int = 31,
        ev: Int = 252,
        statStage: Int = 0,
        hasScarf: Boolean = false,
        hasBoosterEnergy: Boolean = false,
        hasSwiftSwim: Boolean = false,
        hasTailwind: Boolean = false,
        isParalyzed: Boolean = false
    ): List<SpeedTierEntry> {
        val baseSpeed = userPokemon.stats?.speed ?: 100
        val userCalculatedSpeed = calculateSpeedStat(
            baseSpeed = baseSpeed,
            iv = iv,
            ev = ev,
            level = level,
            nature = nature,
            hasChoiceScarf = hasScarf,
            hasBoosterEnergySpeed = hasBoosterEnergy,
            hasSwiftSwimOrChlorophyll = hasSwiftSwim,
            hasTailwind = hasTailwind,
            statStage = statStage,
            isParalyzed = isParalyzed
        )

        val stageText = when {
            statStage > 0 -> "+$statStage Stage "
            statStage < 0 -> "$statStage Stage "
            else -> ""
        }
        val itemText = if (hasScarf) "Choice Scarf " else if (hasBoosterEnergy) "Booster Spe " else ""
        val tailwindText = if (hasTailwind) "Tailwind " else ""
        val paraText = if (isParalyzed) "Paralyzed " else ""

        val spreadDesc = "Custom: $stageText$itemText$tailwindText$paraText$ev EVs / ${nature.displayName} Nature"

        val userEntry = SpeedTierEntry(
            id = "user_custom_${userPokemon.id}",
            pokemonId = userPokemon.id,
            pokemonName = userPokemon.name,
            spriteUrl = userPokemon.spriteUrl,
            primaryType = userPokemon.primaryType,
            secondaryType = userPokemon.secondaryType,
            baseSpeed = baseSpeed,
            calculatedSpeed = userCalculatedSpeed,
            category = if (hasScarf || hasBoosterEnergy || hasSwiftSwim || hasTailwind || statStage > 0) SpeedTierCategory.TIER_PLUS_ONE else SpeedTierCategory.TIER_MAX_POSITIVE,
            spreadDescription = spreadDesc,
            level = level,
            nature = nature,
            ev = ev,
            iv = iv,
            stage = statStage,
            item = if (hasScarf) "Choice Scarf" else if (hasBoosterEnergy) "Booster Energy" else null,
            isUserPokemon = true
        )

        val combined = benchmarks.filterNot { it.isUserPokemon } + userEntry
        return combined.sortedByDescending { it.calculatedSpeed }
    }

    private data class BenchmarkDef(
        val name: String,
        val pokemonId: Int,
        val primaryType: PokemonType,
        val secondaryType: PokemonType?,
        val baseSpeed: Int,
        val ev: Int,
        val iv: Int,
        val nature: PokemonNature,
        val category: SpeedTierCategory,
        val spreadDescription: String,
        val hasScarf: Boolean = false,
        val hasBoosterEnergy: Boolean = false,
        val hasSwiftSwim: Boolean = false
    )
}
