package com.dexter.app.domain.battle.model

enum class BattleAbility(
    val displayName: String,
    val description: String,
    val isAttackerAbility: Boolean = true,
    val isDefenderAbility: Boolean = true
) {
    NONE("None", "No battle-relevant ability", true, true),

    // Offensive Abilities
    HUGE_POWER("Huge Power / Pure Power", "Doubles user's raw Attack stat", true, false),
    ADAPTABILITY("Adaptability", "Boosts Same-Type Attack Bonus (STAB) from 1.5x to 2.0x", true, false),
    BLAZE("Blaze / Torrent / Overgrow / Swarm", "Boosts respective type attacks by 1.5x", true, false),
    SHEER_FORCE("Sheer Force", "+30% damage on attacks with secondary effects (negates secondary effects)", true, false),
    TECHNICIAN("Technician", "Boosts moves with base power 60 or less by 1.5x", true, false),
    TINTED_LENS("Tinted Lens", "Doubles damage of 'Not Very Effective' moves", true, false),
    SNIPER("Sniper", "Critical hits deal 2.25x damage instead of 1.5x", true, false),
    SHARPNESS("Sharpness", "+50% power for cutting/slicing moves", true, false),
    TOUGH_CLAWS("Tough Claws", "+30% power for contact moves", true, false),
    STRONG_JAW("Strong Jaw", "+50% power for biting moves", true, false),
    MEGA_LAUNCHER("Mega Launcher", "+50% power for aura/pulse moves", true, false),
    GUTS("Guts", "+50% Attack when afflicted with a status condition; ignores Burn penalty", true, false),
    SOLAR_POWER("Solar Power", "+50% Sp. Attack in harsh sunlight", true, false),
    TRANSISTOR("Transistor", "+30% power for Electric-type moves", true, false),
    DRAGONS_MAW("Dragon's Maw", "+50% power for Dragon-type moves", true, false),
    STEELY_SPIRIT("Steely Spirit", "+50% power for Steel-type moves", true, false),
    SUPREME_OVERLORD("Supreme Overlord", "+10% to +50% power based on defeated allies", true, false),
    SWORD_OF_RUIN("Sword of Ruin", "Lowers Defense of all other Pokémon by 25%", true, false),
    BEADS_OF_RUIN("Beads of Ruin", "Lowers Sp. Defense of all other Pokémon by 25%", true, false),
    TABLETS_OF_RUIN("Tablets of Ruin", "Lowers Attack of all other Pokémon by 25%", false, true),
    VESSEL_OF_RUIN("Vessel of Ruin", "Lowers Sp. Attack of all other Pokémon by 25%", false, true),
    PROTOSYNTHESIS("Protosynthesis", "+30% highest stat (+50% Speed) in Sun or with Booster Energy", true, true),
    QUARK_DRIVE("Quark Drive", "+30% highest stat (+50% Speed) in Electric Terrain or with Booster Energy", true, true),
    LIBERO("Libero / Protean", "Changes type to the move being used (grants STAB)", true, false),
    NEUROFORCE("Neuroforce", "+25% damage on Super Effective attacks", true, false),
    SUPER_LUCK("Super Luck", "Increases critical hit ratio by +1 stage", true, false),
    AERILATE("Aerilate / Pixilate / Refrigerate / Galvanize", "Turns Normal moves into type moves with +20% boost", true, false),

    // Defensive Abilities
    MULTISCALE("Multiscale / Shadow Shield", "Halves damage taken when at full HP", false, true),
    FUR_COAT("Fur Coat", "Doubles user's raw Defense stat", false, true),
    ICE_SCALES("Ice Scales", "Halves special damage taken", false, true),
    FLUFFY("Fluffy", "Halves contact move damage, doubles Fire damage taken", false, true),
    THICK_FAT("Thick Fat", "Halves damage taken from Fire and Ice attacks", false, true),
    LEVITATE("Levitate", "Immunity to Ground-type attacks and hazards", false, true),
    WATER_ABSORB("Water Absorb / Storm Drain / Dry Skin", "Immunity to Water attacks", false, true),
    VOLT_ABSORB("Volt Absorb / Motor Drive / Lightning Rod", "Immunity to Electric attacks", false, true),
    FLASH_FIRE("Flash Fire", "Immunity to Fire attacks and boosts own Fire attacks", false, true),
    EARTH_EATER("Earth Eater", "Immunity to Ground attacks and heals HP", false, true),
    SAP_SIPPER("Sap Sipper", "Immunity to Grass attacks and boosts Attack", false, true),
    UNAWARE("Unaware", "Ignores the opponent's stat stage boosts", true, true),
    SOLID_ROCK("Solid Rock / Filter / Prism Armor", "Reduces Super Effective damage taken by 25%", false, true),
    HEATPROOF("Heatproof", "Halves damage taken from Fire attacks and burn", false, true),
    MAGIC_GUARD("Magic Guard", "Immune to indirect damage (hazards, weather, Life Orb recoil)", false, true),
    INTIMIDATE("Intimidate", "Lowers opponent's Attack by 1 stage on switch-in", false, true);

    companion object {
        fun fromString(name: String): BattleAbility {
            val clean = name.lowercase().replace("-", "").replace(" ", "").replace("_", "")
            return entries.firstOrNull {
                it.name.lowercase().replace("_", "") == clean ||
                it.displayName.lowercase().replace(" ", "").replace("/", "").replace("-", "").contains(clean)
            } ?: NONE
        }
    }
}
