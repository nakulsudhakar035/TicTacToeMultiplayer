package com.nakuls.tictactoe.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Profile : Screen("profile_screen")
    object Home : Screen("home_screen")

    object Game : Screen("game_screen/{gameId}") {
        fun createRoute(gameId: String) = "game_screen/$gameId"
    }
}