package com.nakuls.tictactoe.presentation.screens.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
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

val R_drawable_o_mark = R.drawable.o
val R_drawable_x_mark = R.drawable.x

enum class Mark { NONE, O, X }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    navController: NavController,
    gameID: Int? = 1,
    viewModel: GameViewModel = koinViewModel()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Initialize game once, keyed to the game ID
    LaunchedEffect(gameID) {
        if (gameID != null) {
            viewModel.onIntent(GameIntent.Initialize(gameID))
        }
    }

    // Handle one-off uiState transitions: navigation and error feedback
    LaunchedEffect(screenState.uiState) {
        when (val uiState = screenState.uiState) {
            is GameUiState.SessionReset -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Profile.route) { inclusive = true }
                }
            }
            is GameUiState.Error -> {
                snackbarHostState.showSnackbar(uiState.message)
            }
            else -> {}
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
                                contentDescription = "Reset Game",
                                tint = colorResource(R.color.DarkNavy),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "End Game",
                                color = colorResource(R.color.DarkNavy),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Game(
                modifier = Modifier.padding(paddingValues),
                screenState = screenState,
                onMakeMove = { index -> viewModel.onIntent(GameIntent.MakeMove(index)) }
            )
        }
    }

    // Game over dialog — shown when the uiState is GameOver
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
            }
        )
    }
}

@Composable
fun Game(
    modifier: Modifier = Modifier,
    screenState: GameScreenState,
    onMakeMove: (Int) -> Unit
) {
    val currentBoard = remember(screenState.game) {
        screenState.game?.charArray?.map {
            when (it) {
                'x' -> Mark.X
                'o' -> Mark.O
                else -> Mark.NONE
            }
        } ?: List(9) { Mark.NONE }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 600.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScoreCard(
                    playerMark = Mark.X,
                    score = 0,
                    isCurrentPlayer = screenState.isMyTurn,
                    cardColor = colorResource(R.color.PlayerOCardColor)
                )
                ScoreCard(
                    playerMark = Mark.O,
                    score = 0,
                    isCurrentPlayer = screenState.isMyTurn,
                    cardColor = colorResource(R.color.PlayerXCardColor)
                )
            }

            Text(
                text = if (screenState.isMyTurn) "Your Turn" else "Opponent's Turn",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.DarkNavy),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            TicTacToeGrid(
                boardState = currentBoard,
                onCellClick = { index ->
                    if (currentBoard[index] == Mark.NONE && screenState.isMyTurn) {
                        onMakeMove(index)
                    }
                }
            )

            Spacer(modifier = modifier)
        }
    }
}

@Composable
fun ScoreCard(
    playerMark: Mark,
    score: Int,
    isCurrentPlayer: Boolean,
    cardColor: Color
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isCurrentPlayer) 3.dp else 1.dp,
                color = if (isCurrentPlayer) colorResource(R.color.TextLight)
                else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val markDrawable = when (playerMark) {
                    Mark.O -> R_drawable_o_mark
                    Mark.X -> R_drawable_x_mark
                    else -> null
                }
                if (markDrawable != null) {
                    Image(
                        painter = painterResource(id = markDrawable),
                        contentDescription = "$playerMark Mark",
                        modifier = Modifier.size(36.dp),
                        colorFilter = null
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (playerMark == Mark.O) "You :" else "Other :",
                    color = colorResource(R.color.DarkNavy),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = score.toString(),
                color = colorResource(R.color.DarkNavy),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TicTacToeGrid(
    boardState: List<Mark>,
    onCellClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight(0.7f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.DarkNavy))
            .border(
                2.dp,
                colorResource(R.color.TextLight).copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 0 until 3) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (j in 0 until 3) {
                        val index = i * 3 + j
                        Box(modifier = Modifier.weight(1f)) {
                            GridCell(
                                mark = boardState[index],
                                onClick = { onCellClick(index) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridCell(
    mark: Mark,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val markDrawable = when (mark) {
                Mark.O -> R_drawable_o_mark
                Mark.X -> R_drawable_x_mark
                Mark.NONE -> null
            }
            if (markDrawable != null) {
                Image(
                    painter = painterResource(id = markDrawable),
                    contentDescription = "$mark Mark",
                    modifier = Modifier.size(64.dp),
                    colorFilter = null
                )
            }
        }
    }
}
