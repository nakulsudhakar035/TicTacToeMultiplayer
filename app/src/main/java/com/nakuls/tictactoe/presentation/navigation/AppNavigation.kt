package com.nakuls.tictactoe.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nakuls.tictactoe.presentation.screens.game.GameScreen
import com.nakuls.tictactoe.presentation.screens.home.HomeScreen
import com.nakuls.tictactoe.presentation.screens.profile.ProfileScreen
import com.nakuls.tictactoe.presentation.screens.splash.SplashScreen

@Composable
fun AppNavigation(
    modifier: Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route // Always start at Splash
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                navController = navController,

            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.Game.route,
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getInt("gameId")
            GameScreen(
                navController = navController,
                gameID = gameId
            )
        }
    }
}