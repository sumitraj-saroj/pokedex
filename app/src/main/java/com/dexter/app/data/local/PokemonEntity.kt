package com.dexter.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon")
data class PokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val number: Int,
    val heightM: Double,
    val weightKg: Double,
    val primaryType: String,
    val secondaryType: String?,
    val isLegendary: Boolean,
    val isMythical: Boolean,
    val category: String,
    val flavorText: String,
    val spriteUrl: String,
    val shinySpriteUrl: String?,
    val officialArtworkUrl: String?,
    val shinyArtworkUrl: String?,
    val homeArtworkUrl: String?,
    val animatedSpriteUrl: String?,
    val pixelSpriteUrl: String?,
    val cryAudioUrl: String?,
    val generation: Int,
    val evolutionChainId: Int? = null
)

@Entity(tableName = "pokemon_stats")
data class PokemonStatsEntity(
    @PrimaryKey val pokemonId: Int,
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val spAttack: Int,
    val spDefense: Int,
    val speed: Int
)

@Entity(tableName = "user_collection")
data class UserCollectionEntity(
    @PrimaryKey val pokemonId: Int,
    val isCaught: Boolean = false,
    val isFavorite: Boolean = false,
    val shinyOwned: Boolean = false,
    val isAlpha: Boolean = false,
    val ashOwned: Boolean = false
)

@Entity(tableName = "evolutions")
data class EvolutionEntity(
    @PrimaryKey val id: String, // chainId_speciesId
    val chainId: Int,
    val speciesId: Int,
    val speciesName: String,
    val evolvesFromSpeciesId: Int?,
    val trigger: String,
    val conditionText: String,
    val spriteUrl: String
)

@Entity(tableName = "pokemon_moves")
data class PokemonMoveEntity(
    @PrimaryKey val id: String, // pokemonId_moveName
    val pokemonId: Int,
    val moveName: String,
    val learnMethod: String,
    val levelLearnedAt: Int
)

@Entity(tableName = "move_details")
data class MoveDetailEntity(
    @PrimaryKey val moveName: String,
    val displayName: String,
    val type: String,
    val power: Int?,
    val accuracy: Int?,
    val damageClass: String,
    val effectText: String
)

@Entity(tableName = "pokemon_abilities")
data class PokemonAbilityEntity(
    @PrimaryKey val id: String, // pokemonId_abilityName
    val pokemonId: Int,
    val abilityName: String,
    val displayName: String,
    val isHidden: Boolean,
    val effectText: String
)

@Entity(tableName = "pokemon_forms")
data class PokemonFormEntity(
    @PrimaryKey val id: Int, // form pokemonId (e.g., 10103)
    val basePokemonId: Int,
    val formName: String,
    val displayName: String,
    val primaryType: String,
    val secondaryType: String?,
    val spriteUrl: String,
    val shinySpriteUrl: String?,
    val officialArtworkUrl: String?,
    val shinyArtworkUrl: String?,
    val homeArtworkUrl: String?,
    val animatedSpriteUrl: String?,
    val cryAudioUrl: String?,
    val heightM: Double,
    val weightKg: Double,
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val spAttack: Int,
    val spDefense: Int,
    val speed: Int
)
