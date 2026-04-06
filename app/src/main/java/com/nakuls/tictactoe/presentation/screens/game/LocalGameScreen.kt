package com.nakuls.tictactoe.presentation.screens.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nakuls.tictactoe.R
import com.nakuls.tictactoe.presentation.navigation.Screen
import com.nakuls.tictactoe.presentation.screens.home.AppTheme
import com.nakuls.tictactoe.presentation.ui.theme.BackgroundLight
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalGameScreen(
    navController: NavController,
    viewModel: LocalGameViewModel = koinViewModel()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Start the local game once on first composition
    LaunchedEffect(Unit) {
        viewModel.onIntent(GameIntent.Initialize(0))
    }

    // Navigate home when session is reset
    LaunchedEffect(screenState.uiState) {
        if (screenState.uiState is GameUiState.SessionReset) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Profile.route) { inclusive = true }
            }
        }
    }

    AppTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = BackgroundLight,
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { viewModel.onIntent(GameIntent.ResetSession) },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Exit Game",
                                tint = colorResource(R.color.DarkNavy),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Exit Game",
                                color = colorResource(R.color.DarkNavy),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            // Reuse the same Game composable as the online screen
            Game(
                modifier = Modifier.padding(paddingValues),
                screenState = screenState,
                onMakeMove = { index -> viewModel.onIntent(GameIntent.MakeMove(index)) }
            )
        }
    }

    // Game over / draw dialog
    if (screenState.uiState is GameUiState.GameOver) {
        val gameOver = screenState.uiState as GameUiState.GameOver
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Game Over!") },
            text = {
                Text(
                    if (gameOver.isDraw) "It's a draw!"
                    else "${gameOver.winnerName} won the match!"
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.onIntent(GameIntent.ResetSession) }) {
                    Text("Back to Lobby")
                }
            },
            dismissButton = {
                Button(onClick = { viewModel.onIntent(GameIntent.Initialize(0)) }) {
                    Text("Play Again")
                }
            }
        )
    }
}
