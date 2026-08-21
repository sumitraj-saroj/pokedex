package com.dexter.app.domain.model

data class PokemonGeneration(
    val number: Int,
    val regionName: String,
    val dexRange: String,
    val minId: Int,
    val maxId: Int
) {
    val displayName: String
        get() = "Gen $number"

    val fullDisplayName: String
        get() = "Gen $number ($regionName)"

    companion object {
        val ALL: List<PokemonGeneration> = listOf(
            PokemonGeneration(1, "Kanto", "#0001 - #0151", 1, 151),
            PokemonGeneration(2, "Johto", "#0152 - #0251", 152, 251),
            PokemonGeneration(3, "Hoenn", "#0252 - #0386", 252, 386),
            PokemonGeneration(4, "Sinnoh", "#0387 - #0493", 387, 493),
            PokemonGeneration(5, "Unova", "#0494 - #0649", 494, 649),
            PokemonGeneration(6, "Kalos", "#0650 - #0721", 650, 721),
            PokemonGeneration(7, "Alola", "#0722 - #0809", 722, 809),
            PokemonGeneration(8, "Galar", "#0810 - #0905", 810, 905),
            PokemonGeneration(9, "Paldea", "#0906 - #1025", 906, 1025)
        )

        fun fromNumber(number: Int): PokemonGeneration? = ALL.find { it.number == number }
    }
}
