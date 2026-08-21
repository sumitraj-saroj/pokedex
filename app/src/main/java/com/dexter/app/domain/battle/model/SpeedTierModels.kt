package com.dexter.app.domain.battle.model

import androidx.compose.runtime.Immutable
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonType

enum class SpeedTierCategory(val displayName: String, val badgeColor: Long) {
    TIER_PLUS_TWO("+2 Boost / Swift Swim / Booster Spe", 0xFF9C27B0),
    TIER_PLUS_ONE("+1 Boost / Choice Scarf / Tailwind", 0xFF2196F3),
    TIER_MAX_POSITIVE("Max Speed Positive Nature (252+)", 0xFF4CAF50),
    TIER_MAX_NEUTRAL("Max Speed Neutral Nature (252)", 0xFFFF9800),
    TIER_BULKY("Standard / Bulky (0 - 100 EVs)", 0xFF607D8B),
    TIER_MIN_SPEED("Min Speed (0 IVs -Nature / Trick Room)", 0xFFE91E63)
}

@Immutable
data class SpeedTierEntry(
    val id: String,
    val pokemonId: Int,
    val pokemonName: String,
    val spriteUrl: String?,
    val primaryType: PokemonType,
    val secondaryType: PokemonType?,
    val baseSpeed: Int,
    val calculatedSpeed: Int,
    val category: SpeedTierCategory,
    val spreadDescription: String,
    val level: Int,
    val nature: PokemonNature,
    val ev: Int,
    val iv: Int,
    val stage: Int,
    val item: String?,
    val isUserPokemon: Boolean = false
) {
    val capitalizedName: String
        get() = pokemonName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
