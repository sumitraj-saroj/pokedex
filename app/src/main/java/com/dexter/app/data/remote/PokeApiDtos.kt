package com.dexter.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonListResponse(
    val count: Int,
    val results: List<NamedApiResourceDto>
)

@Serializable
data class NamedApiResourceDto(
    val name: String,
    val url: String
)

@Serializable
data class ApiResourceUrlDto(
    val url: String
)

@Serializable
data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<PokemonTypeSlotDto> = emptyList(),
    val stats: List<PokemonStatDto> = emptyList(),
    val sprites: PokemonSpritesDto? = null,
    val cries: PokemonCriesDto? = null,
    val abilities: List<PokemonAbilitySlotDto> = emptyList(),
    val moves: List<PokemonMoveSlotDto> = emptyList(),
    val forms: List<NamedApiResourceDto> = emptyList()
)

@Serializable
data class PokemonCriesDto(
    val latest: String? = null,
    val legacy: String? = null
)

@Serializable
data class PokemonTypeSlotDto(
    val slot: Int,
    val type: NamedApiResourceDto
)

@Serializable
data class PokemonStatDto(
    @SerialName("base_stat") val baseStat: Int,
    val stat: NamedApiResourceDto
)

@Serializable
data class PokemonAbilitySlotDto(
    @SerialName("is_hidden") val isHidden: Boolean = false,
    val slot: Int = 1,
    val ability: NamedApiResourceDto
)

@Serializable
data class PokemonMoveSlotDto(
    val move: NamedApiResourceDto,
    @SerialName("version_group_details") val versionGroupDetails: List<PokemonMoveVersionDetailDto> = emptyList()
)

@Serializable
data class PokemonMoveVersionDetailDto(
    @SerialName("level_learned_at") val levelLearnedAt: Int = 0,
    @SerialName("move_learn_method") val moveLearnMethod: NamedApiResourceDto
)

@Serializable
data class PokemonSpritesDto(
    @SerialName("front_default") val frontDefault: String? = null,
    @SerialName("front_shiny") val frontShiny: String? = null,
    val other: OtherSpritesDto? = null
)

@Serializable
data class OtherSpritesDto(
    @SerialName("official-artwork") val officialArtwork: OfficialArtworkDto? = null,
    val home: HomeSpritesDto? = null,
    val showdown: ShowdownSpritesDto? = null
)

@Serializable
data class OfficialArtworkDto(
    @SerialName("front_default") val frontDefault: String? = null,
    @SerialName("front_shiny") val frontShiny: String? = null
)

@Serializable
data class HomeSpritesDto(
    @SerialName("front_default") val frontDefault: String? = null,
    @SerialName("front_shiny") val frontShiny: String? = null
)

@Serializable
data class ShowdownSpritesDto(
    @SerialName("front_default") val frontDefault: String? = null,
    @SerialName("front_shiny") val frontShiny: String? = null
)

@Serializable
data class PokemonSpeciesDto(
    val id: Int,
    @SerialName("is_legendary") val isLegendary: Boolean = false,
    @SerialName("is_mythical") val isMythical: Boolean = false,
    @SerialName("flavor_text_entries") val flavorTextEntries: List<FlavorTextEntryDto> = emptyList(),
    val genera: List<GenusDto> = emptyList(),
    val generation: NamedApiResourceDto? = null,
    @SerialName("evolution_chain") val evolutionChain: ApiResourceUrlDto? = null,
    val varieties: List<PokemonVarietyDto> = emptyList()
)

@Serializable
data class PokemonVarietyDto(
    @SerialName("is_default") val isDefault: Boolean = true,
    val pokemon: NamedApiResourceDto
)

@Serializable
data class FlavorTextEntryDto(
    @SerialName("flavor_text") val flavorText: String,
    val language: NamedApiResourceDto
)

@Serializable
data class GenusDto(
    val genus: String,
    val language: NamedApiResourceDto
)

// Evolution Chain DTOs
@Serializable
data class EvolutionChainResponseDto(
    val id: Int,
    val chain: ChainLinkDto
)

@Serializable
data class ChainLinkDto(
    val species: NamedApiResourceDto,
    @SerialName("evolves_to") val evolvesTo: List<ChainLinkDto> = emptyList(),
    @SerialName("evolution_details") val evolutionDetails: List<EvolutionDetailDto> = emptyList()
)

@Serializable
data class EvolutionDetailDto(
    val trigger: NamedApiResourceDto? = null,
    @SerialName("min_level") val minLevel: Int? = null,
    val item: NamedApiResourceDto? = null,
    @SerialName("min_happiness") val minHappiness: Int? = null,
    @SerialName("known_move") val knownMove: NamedApiResourceDto? = null,
    @SerialName("time_of_day") val timeOfDay: String? = null
)

// Move Details DTO
@Serializable
data class MoveResponseDto(
    val id: Int,
    val name: String,
    val power: Int? = null,
    val accuracy: Int? = null,
    val type: NamedApiResourceDto,
    @SerialName("damage_class") val damageClass: NamedApiResourceDto? = null,
    @SerialName("effect_entries") val effectEntries: List<VerboseEffectDto> = emptyList(),
    @SerialName("flavor_text_entries") val flavorTextEntries: List<MoveFlavorTextDto> = emptyList()
)

@Serializable
data class VerboseEffectDto(
    val effect: String,
    @SerialName("short_effect") val shortEffect: String? = null,
    val language: NamedApiResourceDto
)

@Serializable
data class MoveFlavorTextDto(
    @SerialName("flavor_text") val flavorText: String,
    val language: NamedApiResourceDto
)

// Ability Details DTO
@Serializable
data class AbilityResponseDto(
    val id: Int,
    val name: String,
    @SerialName("effect_entries") val effectEntries: List<VerboseEffectDto> = emptyList(),
    @SerialName("flavor_text_entries") val flavorTextEntries: List<MoveFlavorTextDto> = emptyList()
)
