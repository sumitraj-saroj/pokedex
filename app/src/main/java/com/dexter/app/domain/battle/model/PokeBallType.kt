package com.dexter.app.domain.battle.model

import com.dexter.app.domain.model.PokemonType

enum class PokeBallType(
    val displayName: String,
    val description: String,
    val emoji: String,
    val baseMultiplier: Double = 1.0
) {
    POKE_BALL("Poké Ball", "Standard Poké Ball with 1.0x catch rate", "🔴", 1.0),
    GREAT_BALL("Great Ball", "Higher performance ball with 1.5x catch rate", "🔵", 1.5),
    ULTRA_BALL("Ultra Ball", "Ultra-performance ball with 2.0x catch rate", "🟡", 2.0),
    MASTER_BALL("Master Ball", "The ultimate ball that never fails (100% catch rate)", "🟣", 255.0),

    // Situational Balls
    QUICK_BALL("Quick Ball", "5.0x multiplier on Turn 1 (1.0x after)", "⚡", 5.0),
    DUSK_BALL("Dusk Ball", "3.0x multiplier at night or inside caves", "🌑", 3.0),
    NET_BALL("Net Ball", "3.5x multiplier on Water or Bug-type Pokémon", "🕸️", 3.5),
    DIVE_BALL("Dive Ball", "3.5x multiplier on water encounters / surfing", "🌊", 3.5),
    NEST_BALL("Nest Ball", "Up to 3.9x multiplier on lower-level Pokémon (Lv 1–29)", "🪺", 3.9),
    REPEAT_BALL("Repeat Ball", "3.5x multiplier on species already registered in Pokédex", "🔁", 3.5),
    TIMER_BALL("Timer Ball", "Increases by 0.3x per turn up to 4.0x at Turn 10+", "⏳", 4.0),
    HEAVY_BALL("Heavy Ball", "Modifies base catch rate based on target weight (+20 to +40 for heavy)", "⚖️", 1.0),
    FAST_BALL("Fast Ball", "4.0x multiplier on Pokémon with base Speed 100+", "💨", 4.0),
    LEVEL_BALL("Level Ball", "Up to 8.0x multiplier when player's Pokémon level is higher", "📈", 8.0),
    MOON_BALL("Moon Ball", "4.0x multiplier on Pokémon that evolve using a Moon Stone", "🌙", 4.0),
    DREAM_BALL("Dream Ball", "4.0x multiplier on sleeping Pokémon (1.0x otherwise)", "💤", 4.0),
    LOVE_BALL("Love Ball", "8.0x multiplier on opposite gender of same species", "💖", 8.0),
    LURE_BALL("Lure Ball", "4.0x / 5.0x multiplier on wild Pokémon hooked by rod", "🎣", 4.0),
    BEAST_BALL("Beast Ball", "5.0x multiplier on Ultra Beasts (0.1x on regular Pokémon)", "🪐", 0.1),
    PREMIER_BALL("Premier Ball", "Commemorative ball with 1.0x catch rate", "⚪", 1.0),
    LUXURY_BALL("Luxury Ball", "Comfortable ball with 1.0x catch rate (boosts friendship)", "👑", 1.0),
    HEAL_BALL("Heal Ball", "Fully restores HP and status upon capture (1.0x catch rate)", "💖", 1.0),
    SAFARI_BALL("Safari Ball", "Special Safari Zone ball with 1.5x catch rate", "🌿", 1.5),
    SPORT_BALL("Sport Ball", "Bug-Catching Contest ball with 1.5x catch rate", "🏆", 1.5)
}

enum class CatchStatusCondition(
    val displayName: String,
    val multiplier: Double,
    val emoji: String
) {
    NONE("None", 1.0, "⚪"),
    SLEEP("Sleep", 2.5, "💤"),
    FREEZE("Freeze", 2.5, "❄️"),
    PARALYSIS("Paralysis", 1.5, "⚡"),
    POISON("Poison / Toxic", 1.5, "☠️"),
    BURN("Burn", 1.5, "🔥")
}
