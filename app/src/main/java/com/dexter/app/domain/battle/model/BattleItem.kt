package com.dexter.app.domain.battle.model

import com.dexter.app.domain.model.PokemonType

enum class BattleItem(
    val displayName: String,
    val description: String,
    val boostedType: PokemonType? = null
) {
    NONE("None", "No held item"),
    LIFE_ORB("Life Orb", "+30% damage, but user takes 10% recoil per attack"),
    CHOICE_BAND("Choice Band", "+50% Attack, but locks user into one move"),
    CHOICE_SPECS("Choice Specs", "+50% Sp. Attack, but locks user into one move"),
    CHOICE_SCARF("Choice Scarf", "+50% Speed, but locks user into one move"),
    ASSAULT_VEST("Assault Vest", "+50% Sp. Defense, but user cannot use status moves"),
    EVIOLITE("Eviolite", "+50% Defense and Sp. Defense if not fully evolved"),
    LEFTOVERS("Leftovers", "Restores 1/16 (6.25%) max HP at the end of each turn"),
    FOCUS_SASH("Focus Sash", "Survives any lethal attack with 1 HP if at full HP"),
    EXPERT_BELT("Expert Belt", "+20% damage when hitting super-effectively"),
    MUSCLE_BAND("Muscle Band", "+10% damage for Physical attacks"),
    WISE_GLASSES("Wise Glasses", "+10% damage for Special attacks"),
    BOOSTER_ENERGY("Booster Energy", "+30% highest stat (+50% Speed) via Protosynthesis/Quark Drive"),
    WEAKNESS_POLICY("Weakness Policy", "+2 Attack and +2 Sp. Attack when hit super-effectively"),
    ROCKY_HELMET("Rocky Helmet", "Damages the attacker for 1/6 HP on contact moves"),
    HEAVY_DUTY_BOOTS("Heavy-Duty Boots", "Immune to hazards (Stealth Rock, Spikes, Toxic Spikes)"),
    SITRUS_BERRY("Sitrus Berry", "Restores 25% max HP when falling below 50% HP"),
    AIR_BALLOON("Air Balloon", "Immune to Ground attacks until hit by any attack"),
    BLACK_SLUDGE("Black Sludge", "Heals 1/16 HP for Poison-types, damages others"),
    LIGHT_CLAY("Light Clay", "Reflect and Light Screen last 8 turns instead of 5"),

    // Type-enhancing items (1.2x multiplier)
    CHARCOAL("Charcoal", "+20% Fire-type attacks", PokemonType.FIRE),
    MYSTIC_WATER("Mystic Water", "+20% Water-type attacks", PokemonType.WATER),
    MIRACLE_SEED("Miracle Seed", "+20% Grass-type attacks", PokemonType.GRASS),
    MAGNET("Magnet", "+20% Electric-type attacks", PokemonType.ELECTRIC),
    NEVER_MELT_ICE("Never-Melt Ice", "+20% Ice-type attacks", PokemonType.ICE),
    BLACK_BELT("Black Belt", "+20% Fighting-type attacks", PokemonType.FIGHTING),
    POISON_BARB("Poison Barb", "+20% Poison-type attacks", PokemonType.POISON),
    SOFT_SAND("Soft Sand", "+20% Ground-type attacks", PokemonType.GROUND),
    SHARP_BEAK("Sharp Beak", "+20% Flying-type attacks", PokemonType.FLYING),
    TWISTED_SPOON("Twisted Spoon", "+20% Psychic-type attacks", PokemonType.PSYCHIC),
    SILVER_POWDER("Silver Powder", "+20% Bug-type attacks", PokemonType.BUG),
    HARD_STONE("Hard Stone", "+20% Rock-type attacks", PokemonType.ROCK),
    SPELL_TAG("Spell Tag", "+20% Ghost-type attacks", PokemonType.GHOST),
    DRAGON_FANG("Dragon Fang", "+20% Dragon-type attacks", PokemonType.DRAGON),
    BLACK_GLASSES("Black Glasses", "+20% Dark-type attacks", PokemonType.DARK),
    METAL_COAT("Metal Coat", "+20% Steel-type attacks", PokemonType.STEEL),
    SILK_SCARF("Silk Scarf", "+20% Normal-type attacks", PokemonType.NORMAL),
    FAIRY_FEATHER("Fairy Feather", "+20% Fairy-type attacks", PokemonType.FAIRY);

    companion object {
        fun fromString(name: String): BattleItem {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) } ?: NONE
        }
    }
}
