package com.dexter.app.domain.model

import androidx.compose.runtime.Immutable

enum class ArtworkType(val displayName: String) {
    OFFICIAL("Official"),
    SHINY("Shiny"),
    HOME("HOME"),
    ANIMATED("Animated"),
    PIXEL("Pixel")
}

@Immutable
data class PokemonStats(
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val spAttack: Int,
    val spDefense: Int,
    val speed: Int
) {
    val total: Int
        get() = hp + attack + defense + spAttack + spDefense + speed
}

@Immutable
data class UserCollection(
    val pokemonId: Int,
    val isCaught: Boolean = false,
    val isFavorite: Boolean = false,
    val shinyOwned: Boolean = false,
    val isAlpha: Boolean = false,
    val ashOwned: Boolean = false
)

@Immutable
sealed interface SyncState {
    @Immutable data object Idle : SyncState
    @Immutable data class Syncing(val current: Int, val total: Int) : SyncState
    @Immutable data object Completed : SyncState
    @Immutable data class Error(val message: String) : SyncState
}

@Immutable
data class EvolutionNode(
    val id: String,
    val chainId: Int,
    val speciesId: Int,
    val speciesName: String,
    val evolvesFromSpeciesId: Int?,
    val trigger: String,
    val conditionText: String,
    val spriteUrl: String
) {
    val capitalizedName: String
        get() = speciesName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

@Immutable
data class MoveDetail(
    val moveName: String,
    val displayName: String,
    val type: PokemonType,
    val power: Int?,
    val accuracy: Int?,
    val damageClass: String, // physical, special, status
    val effectText: String
)

@Immutable
data class PokemonMove(
    val id: String,
    val pokemonId: Int,
    val moveName: String,
    val learnMethod: String, // level-up, machine, egg, tutor
    val levelLearnedAt: Int,
    val detail: MoveDetail?
) {
    val displayName: String
        get() = detail?.displayName ?: moveName.replace("-", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

@Immutable
data class PokemonAbility(
    val id: String,
    val pokemonId: Int,
    val abilityName: String,
    val displayName: String,
    val isHidden: Boolean,
    val effectText: String
)

@Immutable
data class PokemonForm(
    val id: Int,
    val basePokemonId: Int,
    val formName: String,
    val displayName: String,
    val primaryType: PokemonType,
    val secondaryType: PokemonType?,
    val spriteUrl: String,
    val shinySpriteUrl: String?,
    val officialArtworkUrl: String?,
    val shinyArtworkUrl: String?,
    val homeArtworkUrl: String?,
    val animatedSpriteUrl: String?,
    val cryAudioUrl: String?,
    val heightM: Double,
    val weightKg: Double,
    val stats: PokemonStats
)

@Immutable
data class Pokemon(
    val id: Int,
    val name: String,
    val number: Int,
    val heightM: Double,
    val weightKg: Double,
    val primaryType: PokemonType,
    val secondaryType: PokemonType? = null,
    val isLegendary: Boolean = false,
    val isMythical: Boolean = false,
    val category: String = "",
    val flavorText: String = "",
    val spriteUrl: String = "",
    val shinySpriteUrl: String? = null,
    val officialArtworkUrl: String? = null,
    val shinyArtworkUrl: String? = null,
    val homeArtworkUrl: String? = null,
    val animatedSpriteUrl: String? = null,
    val pixelSpriteUrl: String? = null,
    val cryAudioUrl: String? = null,
    val generation: Int = 1,
    val evolutionChainId: Int? = null,
    val stats: PokemonStats? = null,
    val collection: UserCollection? = null
) {
    val formattedNumber: String
        get() = "#%04d".format(number)

    val effectiveGeneration: Int
        get() = when (number) {
            in 1..151 -> 1
            in 152..251 -> 2
            in 252..386 -> 3
            in 387..493 -> 4
            in 494..649 -> 5
            in 650..721 -> 6
            in 722..809 -> 7
            in 810..905 -> 8
            in 906..1025 -> 9
            else -> if (generation in 1..9) generation else 1
        }

    val capitalizedName: String
        get() = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    fun getArtworkUrl(type: ArtworkType): String {
        return when (type) {
            ArtworkType.OFFICIAL -> officialArtworkUrl ?: spriteUrl
            ArtworkType.SHINY -> shinyArtworkUrl ?: shinySpriteUrl ?: officialArtworkUrl ?: spriteUrl
            ArtworkType.HOME -> homeArtworkUrl ?: officialArtworkUrl ?: spriteUrl
            ArtworkType.ANIMATED -> animatedSpriteUrl ?: spriteUrl
            ArtworkType.PIXEL -> pixelSpriteUrl ?: spriteUrl
        }
    }
}
