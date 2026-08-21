package com.dexter.app.domain.model.region

import androidx.compose.runtime.Immutable
import com.dexter.app.domain.model.PokemonType

enum class LocationType(val displayName: String, val emoji: String) {
    TOWN("Town", "🏡"),
    CITY("City", "🏙️"),
    ROUTE("Route", "🌿"),
    FOREST("Forest", "🌲"),
    CAVE("Cave", "🪨"),
    MOUNTAIN("Mountain", "⛰️"),
    SEA_ROUTE("Sea Route", "🌊"),
    DUNGEON("Dungeon / Ruins", "🏛️"),
    LEGENDARY_LAIR("Legendary Lair", "⭐"),
    POKEMON_LEAGUE("Pokémon League", "👑")
}

@Immutable
data class WildSpawn(
    val pokemonId: Int,
    val pokemonName: String,
    val minLevel: Int,
    val maxLevel: Int,
    val method: String, // e.g. "Tall Grass", "Surfing", "Fishing", "Cave Floor"
    val rarity: String  // e.g. "Very Common", "Common", "Uncommon", "Rare", "Very Rare"
)

@Immutable
data class GymLeader(
    val name: String,
    val title: String,
    val badgeName: String,
    val badgeEmoji: String,
    val specialtyType: PokemonType,
    val acePokemonId: Int,
    val acePokemonName: String
)

@Immutable
data class LegendaryEncounter(
    val pokemonId: Int,
    val pokemonName: String,
    val encounterType: String, // "Static Boss Encounter", "Roaming Wild", "Sacred Shrine Summon", "Story Event"
    val level: Int,
    val requirementText: String
)

@Immutable
data class RegionLocation(
    val id: String,
    val name: String,
    val type: LocationType,
    val description: String,
    val normalizedX: Float, // 0.05f to 0.95f (canvas positioning)
    val normalizedY: Float, // 0.05f to 0.95f (canvas positioning)
    val connectedToIds: List<String> = emptyList(),
    val gymLeader: GymLeader? = null,
    val legendary: LegendaryEncounter? = null,
    val wildSpawns: List<WildSpawn> = emptyList(),
    val musicThemeDescription: String = "",
    val audioUrl: String = ""
)

@Immutable
data class Region(
    val id: String,
    val number: Int,
    val name: String,
    val japaneseName: String,
    val tagline: String,
    val description: String,
    val professor: String,
    val villainTeam: String,
    val starterIds: List<Int>,
    val legendaryIds: List<Int>,
    val musicTheme: String,
    val audioThemeTitle: String = "",
    val audioThemeUrl: String = "",
    val locations: List<RegionLocation>
)
