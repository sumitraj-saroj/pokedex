package com.dexter.app.domain.battle.model

import androidx.compose.runtime.Immutable
import com.dexter.app.domain.model.PokemonType

enum class MoveCategory(val displayName: String) {
    PHYSICAL("Physical"),
    SPECIAL("Special"),
    STATUS("Status")
}

@Immutable
data class BattleMove(
    val name: String,
    val displayName: String,
    val type: PokemonType,
    val category: MoveCategory,
    val basePower: Int,
    val accuracy: Int = 100,
    val hitCount: Int = 1,
    val isCritical: Boolean = false,
    val isSpreadMove: Boolean = false,
    val isContact: Boolean = false,
    val isSlicing: Boolean = false,
    val isBiting: Boolean = false,
    val isPunching: Boolean = false,
    val isSound: Boolean = false,
    val isSecondaryEffect: Boolean = false
) {
    companion object {
        fun defaultMove(type: PokemonType = PokemonType.NORMAL): BattleMove {
            return BattleMove(
                name = "tackle",
                displayName = "Tackle",
                type = type,
                category = MoveCategory.PHYSICAL,
                basePower = 40,
                accuracy = 100,
                hitCount = 1,
                isContact = true
            )
        }

        // Common competitive presets
        val POPULAR_COMPETITIVE_MOVES = listOf(
            BattleMove("earthquake", "Earthquake", PokemonType.GROUND, MoveCategory.PHYSICAL, 100, isSpreadMove = true),
            BattleMove("close-combat", "Close Combat", PokemonType.FIGHTING, MoveCategory.PHYSICAL, 120, isContact = true),
            BattleMove("moonblast", "Moonblast", PokemonType.FAIRY, MoveCategory.SPECIAL, 95, isSecondaryEffect = true),
            BattleMove("shadow-ball", "Shadow Ball", PokemonType.GHOST, MoveCategory.SPECIAL, 80, isSecondaryEffect = true),
            BattleMove("draco-meteor", "Draco Meteor", PokemonType.DRAGON, MoveCategory.SPECIAL, 130),
            BattleMove("dragon-darts", "Dragon Darts", PokemonType.DRAGON, MoveCategory.PHYSICAL, 50, hitCount = 2),
            BattleMove("flamethrower", "Flamethrower", PokemonType.FIRE, MoveCategory.SPECIAL, 90, isSecondaryEffect = true),
            BattleMove("fire-blast", "Fire Blast", PokemonType.FIRE, MoveCategory.SPECIAL, 110, isSecondaryEffect = true),
            BattleMove("flare-blitz", "Flare Blitz", PokemonType.FIRE, MoveCategory.PHYSICAL, 120, isContact = true, isSecondaryEffect = true),
            BattleMove("surf", "Surf", PokemonType.WATER, MoveCategory.SPECIAL, 90, isSpreadMove = true),
            BattleMove("hydro-pump", "Hydro Pump", PokemonType.WATER, MoveCategory.SPECIAL, 110),
            BattleMove("water-spout", "Water Spout", PokemonType.WATER, MoveCategory.SPECIAL, 150, isSpreadMove = true),
            BattleMove("thunderbolt", "Thunderbolt", PokemonType.ELECTRIC, MoveCategory.SPECIAL, 90, isSecondaryEffect = true),
            BattleMove("wild-charge", "Wild Charge", PokemonType.ELECTRIC, MoveCategory.PHYSICAL, 90, isContact = true),
            BattleMove("ice-beam", "Ice Beam", PokemonType.ICE, MoveCategory.SPECIAL, 90, isSecondaryEffect = true),
            BattleMove("blizzard", "Blizzard", PokemonType.ICE, MoveCategory.SPECIAL, 110, isSpreadMove = true, isSecondaryEffect = true),
            BattleMove("energy-ball", "Energy Ball", PokemonType.GRASS, MoveCategory.SPECIAL, 90, isSecondaryEffect = true),
            BattleMove("leaf-storm", "Leaf Storm", PokemonType.GRASS, MoveCategory.SPECIAL, 130),
            BattleMove("grassy-glide", "Grassy Glide", PokemonType.GRASS, MoveCategory.PHYSICAL, 55, isContact = true),
            BattleMove("psychic", "Psychic", PokemonType.PSYCHIC, MoveCategory.SPECIAL, 90, isSecondaryEffect = true),
            BattleMove("psystrike", "Psystrike", PokemonType.PSYCHIC, MoveCategory.SPECIAL, 100), // hits defense
            BattleMove("sludge-bomb", "Sludge Bomb", PokemonType.POISON, MoveCategory.SPECIAL, 90, isSecondaryEffect = true),
            BattleMove("gunk-shot", "Gunk Shot", PokemonType.POISON, MoveCategory.PHYSICAL, 120, isSecondaryEffect = true),
            BattleMove("brave-bird", "Brave Bird", PokemonType.FLYING, MoveCategory.PHYSICAL, 120, isContact = true),
            BattleMove("air-slash", "Air Slash", PokemonType.FLYING, MoveCategory.SPECIAL, 75, isSecondaryEffect = true),
            BattleMove("hurricane", "Hurricane", PokemonType.FLYING, MoveCategory.SPECIAL, 110, isSecondaryEffect = true),
            BattleMove("stone-edge", "Stone Edge", PokemonType.ROCK, MoveCategory.PHYSICAL, 100),
            BattleMove("rock-slide", "Rock Slide", PokemonType.ROCK, MoveCategory.PHYSICAL, 75, isSpreadMove = true, isSecondaryEffect = true),
            BattleMove("u-turn", "U-turn", PokemonType.BUG, MoveCategory.PHYSICAL, 70, isContact = true),
            BattleMove("bug-buzz", "Bug Buzz", PokemonType.BUG, MoveCategory.SPECIAL, 90, isSound = true, isSecondaryEffect = true),
            BattleMove("crunch", "Crunch", PokemonType.DARK, MoveCategory.PHYSICAL, 80, isContact = true, isBiting = true, isSecondaryEffect = true),
            BattleMove("dark-pulse", "Dark Pulse", PokemonType.DARK, MoveCategory.SPECIAL, 80, isSecondaryEffect = true),
            BattleMove("knock-off", "Knock Off", PokemonType.DARK, MoveCategory.PHYSICAL, 65, isContact = true),
            BattleMove("sucker-punch", "Sucker Punch", PokemonType.DARK, MoveCategory.PHYSICAL, 70, isContact = true),
            BattleMove("flash-cannon", "Flash Cannon", PokemonType.STEEL, MoveCategory.SPECIAL, 80, isSecondaryEffect = true),
            BattleMove("iron-head", "Iron Head", PokemonType.STEEL, MoveCategory.PHYSICAL, 80, isContact = true, isSecondaryEffect = true),
            BattleMove("make-it-rain", "Make It Rain", PokemonType.STEEL, MoveCategory.SPECIAL, 120, isSpreadMove = true),
            BattleMove("gigaton-hammer", "Gigaton Hammer", PokemonType.STEEL, MoveCategory.PHYSICAL, 160),
            BattleMove("body-press", "Body Press", PokemonType.FIGHTING, MoveCategory.PHYSICAL, 80, isContact = true), // uses Defense
            BattleMove("hyper-voice", "Hyper Voice", PokemonType.NORMAL, MoveCategory.SPECIAL, 90, isSound = true, isSpreadMove = true),
            BattleMove("extreme-speed", "Extreme Speed", PokemonType.NORMAL, MoveCategory.PHYSICAL, 80, isContact = true),
            BattleMove("facade", "Facade", PokemonType.NORMAL, MoveCategory.PHYSICAL, 70, isContact = true)
        )
    }
}
