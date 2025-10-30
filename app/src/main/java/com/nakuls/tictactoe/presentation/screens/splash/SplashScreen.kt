package com.nakuls.tictactoe.presentation.screens.splash

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nakuls.tictactoe.presentation.navigation.Screen
import com.nakuls.tictactoe.presentation.ui.theme.TicTacToeTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = koinViewModel()
) {
    // Collect the state from the ViewModel
    val splashState by viewModel.state.collectAsStateWithLifecycle()

    // --- Navigation Logic (Side Effect) ---
    LaunchedEffect(splashState) {
        if (splashState is SplashState.Navigate) {
            val destination = (splashState as SplashState.Navigate).destination

            // Navigate and clear the back stack up to the splash screen
            navController.navigate(destination.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    // --- UI Layout ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Use the light background color from your theme
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ThumbUp,
            contentDescription = "Game Icon",
            tint = MaterialTheme.colorScheme.primary, // Green color
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tic-Tac-Toe Online",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.secondary // Gold/Amber color for accent
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    TicTacToeTheme {
        SplashScreen(navController = rememberNavController())
    }
}