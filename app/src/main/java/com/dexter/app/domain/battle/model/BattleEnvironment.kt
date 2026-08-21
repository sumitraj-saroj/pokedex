package com.dexter.app.domain.battle.model

import androidx.compose.runtime.Immutable

enum class BattleWeather(val displayName: String) {
    CLEAR("Clear"),
    SUN("Sun (Harsh Sunlight)"),
    RAIN("Rain"),
    SANDSTORM("Sandstorm"),
    SNOW("Snow"),
    HARSH_SUN("Desolate Land (Harsh Sun)"),
    HEAVY_RAIN("Primordial Sea (Heavy Rain)")
}

enum class BattleTerrain(val displayName: String) {
    NONE("None"),
    ELECTRIC("Electric Terrain"),
    GRASSY("Grassy Terrain"),
    PSYCHIC("Psychic Terrain"),
    MISTY("Misty Terrain")
}

@Immutable
data class BattleField(
    val weather: BattleWeather = BattleWeather.CLEAR,
    val terrain: BattleTerrain = BattleTerrain.NONE,
    val isDoubles: Boolean = false,

    // Attacker Side
    val attackerTailwind: Boolean = false,
    val attackerHelpingHand: Boolean = false,
    val attackerBattery: Boolean = false,

    // Defender Side (Screens & Hazards)
    val defenderReflect: Boolean = false,
    val defenderLightScreen: Boolean = false,
    val defenderAuroraVeil: Boolean = false,
    val defenderFriendGuard: Boolean = false,
    val defenderStealthRock: Boolean = false,
    val defenderSpikesLayers: Int = 0 // 0, 1, 2, 3
)
