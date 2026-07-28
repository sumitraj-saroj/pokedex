package com.dexter.app.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Special filter categories for Pokémon beyond type and generation.
 * Each category defines a color for its chip and a matching function.
 */
enum class SpecialCategory(
    val displayName: String,
    val chipColor: Color,
    val emoji: String
) {
    LEGENDARY("Legendary", Color(0xFFFFD700), "⭐"),
    MYTHICAL("Mythical", Color(0xFFDA70D6), "✨"),
    STARTER("Starter", Color(0xFF4CAF50), "🌱"),
    PSEUDO_LEGENDARY("Pseudo-Legendary", Color(0xFF7B68EE), "💎"),
    FOSSIL("Fossil", Color(0xFFBCAAA4), "🦴"),
    ULTRA_BEAST("Ultra Beast", Color(0xFF00BCD4), "🌀"),
    PARADOX("Paradox", Color(0xFFE91E63), "⏳"),
    BABY("Baby", Color(0xFFFFB6C1), "🍼"),
    CAUGHT("Caught", Color(0xFFEF5350), "🔴"),
    FAVORITES("Favorites", Color(0xFFFF4081), "❤️");

    fun matches(pokemon: Pokemon): Boolean = when (this) {
        LEGENDARY -> pokemon.isLegendary || pokemon.id in LEGENDARY_IDS
        MYTHICAL -> pokemon.isMythical || pokemon.id in MYTHICAL_IDS
        STARTER -> pokemon.id in STARTER_IDS
        PSEUDO_LEGENDARY -> pokemon.id in PSEUDO_LEGENDARY_IDS
        FOSSIL -> pokemon.id in FOSSIL_IDS
        ULTRA_BEAST -> pokemon.id in ULTRA_BEAST_IDS
        PARADOX -> pokemon.id in PARADOX_IDS
        BABY -> pokemon.id in BABY_IDS
        CAUGHT -> pokemon.collection?.isCaught == true
        FAVORITES -> pokemon.collection?.isFavorite == true
    }

    companion object {
        // Legendary Pokémon National IDs
        private val LEGENDARY_IDS = setOf(
            144, 145, 146, 150, // Gen 1
            243, 244, 245, 249, 250, // Gen 2: Raikou, Entei, Suicune, Lugia, Ho-Oh
            377, 378, 379, 380, 381, 382, 383, 384, // Gen 3
            480, 481, 482, 483, 484, 485, 486, 487, 488, // Gen 4
            638, 639, 640, 641, 642, 643, 644, 645, 646, // Gen 5
            716, 717, 718, // Gen 6
            772, 773, 785, 786, 787, 788, 789, 790, 791, 792, 800, // Gen 7
            888, 889, 890, 891, 892, 894, 895, 896, 897, 898, // Gen 8
            1001, 1002, 1003, 1004, 1007, 1008, 1014, 1015, 1016, 1017, 1024 // Gen 9
        )

        // Mythical Pokémon National IDs
        private val MYTHICAL_IDS = setOf(
            151, // Gen 1: Mew
            251, // Gen 2: Celebi
            385, 386, // Gen 3
            489, 490, 491, 492, 493, // Gen 4
            494, 647, 648, 649, // Gen 5
            719, 720, 721, // Gen 6
            801, 802, 807, 808, 809, // Gen 7
            893, // Gen 8
            1025 // Gen 9
        )
        // Starters and their full evolution lines (Gen 1–9)
        private val STARTER_IDS = setOf(
            // Gen 1: Bulbasaur, Charmander, Squirtle lines
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            // Gen 2: Chikorita, Cyndaquil, Totodile lines
            152, 153, 154, 155, 156, 157, 158, 159, 160,
            // Gen 3: Treecko, Torchic, Mudkip lines
            252, 253, 254, 255, 256, 257, 258, 259, 260,
            // Gen 4: Turtwig, Chimchar, Piplup lines
            387, 388, 389, 390, 391, 392, 393, 394, 395,
            // Gen 5: Snivy, Tepig, Oshawott lines
            495, 496, 497, 498, 499, 500, 501, 502, 503,
            // Gen 6: Chespin, Fennekin, Froakie lines
            650, 651, 652, 653, 654, 655, 656, 657, 658,
            // Gen 7: Rowlet, Litten, Popplio lines
            722, 723, 724, 725, 726, 727, 728, 729, 730,
            // Gen 8: Grookey, Scorbunny, Sobble lines
            810, 811, 812, 813, 814, 815, 816, 817, 818,
            // Gen 9: Sprigatito, Fuecoco, Quaxly lines
            906, 907, 908, 909, 910, 911, 912, 913, 914
        )

        // Pseudo-Legendaries and their full evolution lines
        private val PSEUDO_LEGENDARY_IDS = setOf(
            147, 148, 149,   // Dratini → Dragonite
            246, 247, 248,   // Larvitar → Tyranitar
            371, 372, 373,   // Bagon → Salamence
            374, 375, 376,   // Beldum → Metagross
            443, 444, 445,   // Gible → Garchomp
            633, 634, 635,   // Deino → Hydreigon
            704, 705, 706,   // Goomy → Goodra
            782, 783, 784,   // Jangmo-o → Kommo-o
            885, 886, 887,   // Dreepy → Dragapult
            996, 997, 998    // Frigibax → Baxcalibur
        )

        // Fossil Pokémon and their evolutions
        private val FOSSIL_IDS = setOf(
            138, 139,  // Omanyte, Omastar
            140, 141,  // Kabuto, Kabutops
            142,       // Aerodactyl
            345, 346,  // Lileep, Cradily
            347, 348,  // Anorith, Armaldo
            408, 409,  // Cranidos, Rampardos
            410, 411,  // Shieldon, Bastiodon
            564, 565,  // Tirtouga, Carracosta
            566, 567,  // Archen, Archeops
            696, 697,  // Tyrunt, Tyrantrum
            698, 699,  // Amaura, Aurorus
            880, 881, 882, 883 // Dracozolt, Arctozolt, Dracovish, Arctovish
        )

        // Ultra Beasts
        private val ULTRA_BEAST_IDS = setOf(
            793, // Nihilego
            794, // Buzzwole
            795, // Pheromosa
            796, // Xurkitree
            797, // Celesteela
            798, // Kartana
            799, // Guzzlord
            803, // Poipole
            804, // Naganadel
            805, // Stakataka
            806  // Blacephalon
        )

        // Paradox Pokémon (Scarlet & Violet)
        private val PARADOX_IDS = setOf(
            984, 985, 986, 987, 988, 989,       // Great Tusk → Sandy Shocks
            990, 991, 992, 993, 994, 995,       // Iron Treads → Iron Thorns
            1005, 1006,                          // Roaring Moon, Iron Valiant
            1009, 1010,                          // Walking Wake, Iron Leaves
            1020, 1021,                          // Gouging Fire, Raging Bolt
            1023, 1024                           // Iron Boulder, Iron Crown
        )

        // Baby Pokémon
        private val BABY_IDS = setOf(
            172, // Pichu
            173, // Cleffa
            174, // Igglybuff
            175, // Togepi
            236, // Tyrogue
            238, // Smoochum
            239, // Elekid
            240, // Magby
            298, // Azurill
            360, // Wynaut
            406, // Budew
            433, // Chingling
            438, // Bonsly
            439, // Mime Jr.
            440, // Happiny
            446, // Munchlax
            447, // Riolu
            458, // Mantyke
            848  // Toxel
        )
    }
}
