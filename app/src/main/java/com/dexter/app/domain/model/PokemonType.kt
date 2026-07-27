package com.dexter.app.domain.model

import androidx.compose.ui.graphics.Color

enum class PokemonType(val typeName: String, val seedColor: Color) {
    NORMAL("normal", Color(0xFFA8A77A)),
    FIRE("fire", Color(0xFFEE8130)),
    WATER("water", Color(0xFF6390F0)),
    ELECTRIC("electric", Color(0xFFF7D02C)),
    GRASS("grass", Color(0xFF7AC74C)),
    ICE("ice", Color(0xFF96D9D6)),
    FIGHTING("fighting", Color(0xFFC22E28)),
    POISON("poison", Color(0xFFA33EA1)),
    GROUND("ground", Color(0xFFE2BF65)),
    FLYING("flying", Color(0xFFA890F0)),
    PSYCHIC("psychic", Color(0xFFF95587)),
    BUG("bug", Color(0xFFA6B91A)),
    ROCK("rock", Color(0xFFB6A136)),
    GHOST("ghost", Color(0xFF735797)),
    DRAGON("dragon", Color(0xFF6F35FC)),
    DARK("dark", Color(0xFF705746)),
    STEEL("steel", Color(0xFFB7B7CE)),
    FAIRY("fairy", Color(0xFFD685AD));

    val capitalizedName: String
        get() = typeName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    companion object {
        fun fromString(typeStr: String): PokemonType {
            return entries.firstOrNull { it.typeName.equals(typeStr, ignoreCase = true) } ?: NORMAL
        }
    }
}

data class TypeMatchup(
    val type: PokemonType,
    val multiplier: Double
)

object TypeMatchupEngine {

    // Standard Gen 6+ 18x18 Type Effectiveness Matrix
    // Chart format: matrix[attacker][defender] -> multiplier (0.0, 0.5, 1.0, 2.0)
    private val matrix: Map<PokemonType, Map<PokemonType, Double>> = mapOf(
        PokemonType.NORMAL to mapOf(
            PokemonType.ROCK to 0.5, PokemonType.GHOST to 0.0, PokemonType.STEEL to 0.5
        ),
        PokemonType.FIRE to mapOf(
            PokemonType.FIRE to 0.5, PokemonType.WATER to 0.5, PokemonType.GRASS to 2.0,
            PokemonType.ICE to 2.0, PokemonType.BUG to 2.0, PokemonType.ROCK to 0.5,
            PokemonType.DRAGON to 0.5, PokemonType.STEEL to 2.0
        ),
        PokemonType.WATER to mapOf(
            PokemonType.FIRE to 2.0, PokemonType.WATER to 0.5, PokemonType.GRASS to 0.5,
            PokemonType.GROUND to 2.0, PokemonType.ROCK to 2.0, PokemonType.DRAGON to 0.5
        ),
        PokemonType.ELECTRIC to mapOf(
            PokemonType.WATER to 2.0, PokemonType.ELECTRIC to 0.5, PokemonType.GRASS to 0.5,
            PokemonType.GROUND to 0.0, PokemonType.FLYING to 2.0, PokemonType.DRAGON to 0.5
        ),
        PokemonType.GRASS to mapOf(
            PokemonType.FIRE to 0.5, PokemonType.WATER to 2.0, PokemonType.GRASS to 0.5,
            PokemonType.POISON to 0.5, PokemonType.GROUND to 2.0, PokemonType.FLYING to 0.5,
            PokemonType.BUG to 0.5, PokemonType.ROCK to 2.0, PokemonType.DRAGON to 0.5,
            PokemonType.STEEL to 0.5
        ),
        PokemonType.ICE to mapOf(
            PokemonType.FIRE to 0.5, PokemonType.WATER to 0.5, PokemonType.GRASS to 2.0,
            PokemonType.ICE to 0.5, PokemonType.GROUND to 2.0, PokemonType.FLYING to 2.0,
            PokemonType.DRAGON to 2.0, PokemonType.STEEL to 0.5
        ),
        PokemonType.FIGHTING to mapOf(
            PokemonType.NORMAL to 2.0, PokemonType.ICE to 2.0, PokemonType.POISON to 0.5,
            PokemonType.FLYING to 0.5, PokemonType.PSYCHIC to 0.5, PokemonType.BUG to 0.5,
            PokemonType.ROCK to 2.0, PokemonType.GHOST to 0.0, PokemonType.DARK to 2.0,
            PokemonType.STEEL to 2.0, PokemonType.FAIRY to 0.5
        ),
        PokemonType.POISON to mapOf(
            PokemonType.GRASS to 2.0, PokemonType.POISON to 0.5, PokemonType.GROUND to 0.5,
            PokemonType.ROCK to 0.5, PokemonType.GHOST to 0.5, PokemonType.STEEL to 0.0,
            PokemonType.FAIRY to 2.0
        ),
        PokemonType.GROUND to mapOf(
            PokemonType.FIRE to 2.0, PokemonType.ELECTRIC to 2.0, PokemonType.GRASS to 0.5,
            PokemonType.POISON to 2.0, PokemonType.FLYING to 0.0, PokemonType.BUG to 0.5,
            PokemonType.ROCK to 2.0, PokemonType.STEEL to 2.0
        ),
        PokemonType.FLYING to mapOf(
            PokemonType.ELECTRIC to 0.5, PokemonType.GRASS to 2.0, PokemonType.FIGHTING to 2.0,
            PokemonType.BUG to 2.0, PokemonType.ROCK to 0.5, PokemonType.STEEL to 0.5
        ),
        PokemonType.PSYCHIC to mapOf(
            PokemonType.FIGHTING to 2.0, PokemonType.POISON to 2.0, PokemonType.PSYCHIC to 0.5,
            PokemonType.DARK to 0.0, PokemonType.STEEL to 0.5
        ),
        PokemonType.BUG to mapOf(
            PokemonType.FIRE to 0.5, PokemonType.GRASS to 2.0, PokemonType.FIGHTING to 0.5,
            PokemonType.POISON to 0.5, PokemonType.FLYING to 0.5, PokemonType.PSYCHIC to 2.0,
            PokemonType.GHOST to 0.5, PokemonType.DARK to 2.0, PokemonType.STEEL to 0.5,
            PokemonType.FAIRY to 0.5
        ),
        PokemonType.ROCK to mapOf(
            PokemonType.FIRE to 2.0, PokemonType.ICE to 2.0, PokemonType.FIGHTING to 0.5,
            PokemonType.GROUND to 0.5, PokemonType.FLYING to 2.0, PokemonType.BUG to 2.0,
            PokemonType.STEEL to 0.5
        ),
        PokemonType.GHOST to mapOf(
            PokemonType.NORMAL to 0.0, PokemonType.PSYCHIC to 2.0, PokemonType.GHOST to 2.0,
            PokemonType.DARK to 0.5
        ),
        PokemonType.DRAGON to mapOf(
            PokemonType.DRAGON to 2.0, PokemonType.STEEL to 0.5, PokemonType.FAIRY to 0.0
        ),
        PokemonType.DARK to mapOf(
            PokemonType.FIGHTING to 0.5, PokemonType.PSYCHIC to 2.0, PokemonType.GHOST to 2.0,
            PokemonType.DARK to 0.5, PokemonType.FAIRY to 0.5
        ),
        PokemonType.STEEL to mapOf(
            PokemonType.FIRE to 0.5, PokemonType.WATER to 0.5, PokemonType.ELECTRIC to 0.5,
            PokemonType.ICE to 2.0, PokemonType.ROCK to 2.0, PokemonType.STEEL to 0.5,
            PokemonType.FAIRY to 2.0
        ),
        PokemonType.FAIRY to mapOf(
            PokemonType.FIRE to 0.5, PokemonType.FIGHTING to 2.0, PokemonType.POISON to 0.5,
            PokemonType.DRAGON to 2.0, PokemonType.DARK to 2.0, PokemonType.STEEL to 0.5
        )
    )

    fun calculateDefensiveMatchups(
        primaryType: PokemonType,
        secondaryType: PokemonType?
    ): List<TypeMatchup> {
        return PokemonType.entries.map { attackingType ->
            val mult1 = matrix[attackingType]?.get(primaryType) ?: 1.0
            val mult2 = if (secondaryType != null) {
                matrix[attackingType]?.get(secondaryType) ?: 1.0
            } else 1.0
            TypeMatchup(attackingType, mult1 * mult2)
        }
    }
}
