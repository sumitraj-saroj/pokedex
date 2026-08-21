package com.dexter.app.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object TeamBuilder : Screen("team")
    data object Compare : Screen("compare")
    data object Quiz : Screen("quiz")
    data object RegionMap : Screen("region_map")
    data object Profile : Screen("profile")
    data object Achievements : Screen("achievements")
    data object BattleHub : Screen("battle?tab={tab}&pokemonId={pokemonId}") {
        fun createRoute(tab: String = "damage", pokemonId: Int = 0): String = "battle?tab=$tab&pokemonId=$pokemonId"
    }
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Detail : Screen("detail/{pokemonId}") {
        fun createRoute(pokemonId: Int): String = "detail/$pokemonId"
    }
}
