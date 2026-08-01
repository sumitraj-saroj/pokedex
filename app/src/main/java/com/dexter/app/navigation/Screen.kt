package com.dexter.app.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object TeamBuilder : Screen("team")
    data object Compare : Screen("compare")
    data object Quiz : Screen("quiz")
    data object Profile : Screen("profile")
    data object Achievements : Screen("achievements")
    data object Detail : Screen("detail/{pokemonId}") {
        fun createRoute(pokemonId: Int): String = "detail/$pokemonId"
    }
}
