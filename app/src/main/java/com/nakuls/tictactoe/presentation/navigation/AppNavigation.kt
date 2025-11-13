package com.nakuls.tictactoe.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

//        composable(Screen.Profile.route) {
//            ProfileCreationScreen(
//                onProfileCreated = { username ->
//                    // When profile is saved, go to Home and clear stack
//                    navController.navigate(Screen.Home.route) {
//                        popUpTo(Screen.Profile.route) { inclusive = true }
//                    }
//                }
//            )
//        }
//
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController
            )
        }
//
//        composable(route = Screen.Game.route) { backStackEntry ->
//            val gameId = backStackEntry.arguments?.getString("gameId")
//            GameScreen(navController = navController, gameId = gameId)
//        }
    }
}