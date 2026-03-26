package com.nakuls.tictactoe.presentation.screens.game

import android.provider.Settings.Global.getString
import android.widget.Toast
import androidx.activity.result.launch
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nakuls.tictactoe.R
import com.nakuls.tictactoe.domain.model.Move
import com.nakuls.tictactoe.presentation.navigation.Screen
import com.nakuls.tictactoe.presentation.screens.home.AppTheme
import com.nakuls.tictactoe.presentation.screens.home.HomeViewModel
import com.nakuls.tictactoe.presentation.screens.home.NewGameButton
import com.nakuls.tictactoe.presentation.ui.theme.BackgroundLight
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.collections.List


val R_drawable_o_mark = R.drawable.o
val R_drawable_x_mark = R.drawable.x

// Enum to represent the state of each cell in the Tic-Tac-Toe grid
enum class Mark { NONE, O, X }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    navController: NavController,
    gameID: Int? = 1,//TODO remove default
    viewModel: GameViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(key1 = Unit) {
        viewModel.initializeGame(gameID)
    }

    AppTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            // Use background color that provides good contrast to the cards
            containerColor = BackgroundLight,
            bottomBar = {
                // Bottom section: New Game Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // --- Reset Game Button ---
                    Button(
                        onClick = {
                            viewModel.resetGameSession {
                                // Navigate back to Home/Lobby and clear the GameScreen from the backstack
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Profile.route) {
                                        inclusive = true
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f) // Make button wider
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
                snackbarHostState = snackbarHostState,
                modifier = Modifier.padding(paddingValues),
                viewModel = viewModel
            )
        }
    }
    // Handle Win State
    when (val state = uiState) {
        is GameUiState.Success -> {
            AlertDialog(
                onDismissRequest = { /* Handle if needed */ },
                title = { Text("Game Over!") },
                text = { Text("${state.name} won the match!") },
                confirmButton = {
                    Button(onClick = { viewModel.resetGameSession {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Profile.route) {
                                inclusive = true
                            }
                        }
                    } }) {
                        Text("Back to Lobby")
                    }
                }
            )
        }
        is GameUiState.Error -> {
            // Show error snackbar
        }
        else -> {}
    }
}

@Composable
fun Game(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()

    // 1. Observe the StateFlow from ViewModel
    val gameState by viewModel.game.collectAsStateWithLifecycle()
    val errorMessageRetry = stringResource(R.string.please_retry)
    val isCreator by viewModel.isCreator.collectAsStateWithLifecycle()

    // 2. Map the CharArray from DB (' ', 'x', 'o') to your UI Marks
    val currentBoard = remember(gameState) {
        gameState?.charArray?.map {
            when (it) {
                'x' -> Mark.X
                'o' -> Mark.O
                else -> Mark.NONE
            }
        } ?: List(9) { Mark.NONE }
    }

    // 2. Logic: Creator is X, Joiner is O.
    // Even total moves = X's turn. Odd total moves = O's turn.
    val movesMade = currentBoard.count { it != Mark.NONE }
    val isXTurn = movesMade % 2 == 0

    // 3. Determine if it's "Your Turn"
    val isMyTurn = (isXTurn && isCreator) || (!isXTurn && !isCreator)

    // This Box serves as the main container and applies padding
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 600.dp) // Prevents the UI from getting too wide on tablets
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly // Spacing between major sections
        ) {
            // --- Scoreboard Section ---
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
                    isCurrentPlayer = isMyTurn,
                    cardColor = colorResource(R.color.PlayerOCardColor) // Light blue
                )
                ScoreCard(
                    playerMark = Mark.O,
                    score = 0,
                    isCurrentPlayer = isMyTurn,
                    cardColor = colorResource(R.color.PlayerXCardColor) // Light green
                )
            }

            // --- Turn Indicator ---
            Text(
                text = if (isMyTurn) "Your Turn" else "Opponent's Turn", // Dynamic turn display
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.DarkNavy),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // --- Game Grid ---
            TicTacToeGrid(
                boardState = currentBoard,
                onCellClick = { index ->
                    // Only allow clicking if the cell is empty
                    if (currentBoard[index] == Mark.NONE && isMyTurn) {
                        scope.launch {
                            val isMoveSuccessful = viewModel.makeMove(index)
                            if (!isMoveSuccessful) {
                                snackbarHostState.showSnackbar(errorMessageRetry)
                            }
                        }
                    }
                }
            )

            Spacer(modifier = modifier) // Spacing before buttons
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
            .width(150.dp) // Fixed width for score card
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isCurrentPlayer) 3.dp else 1.dp,
                color = if (isCurrentPlayer) colorResource(R.color.TextLight)
                else Color.Transparent, // Highlight current player
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
                        colorFilter = null // To use the original colors of your drawables
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (playerMark == Mark.O) "You :" else "Other :", // Dynamic player name
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
    // This Box ensures the grid remains square and is centered
    Box(
        modifier = Modifier
            .fillMaxHeight(0.7f)
            .aspectRatio(1f) // Make the grid square
            .clip(RoundedCornerShape(16.dp)) // Rounded corners for the entire grid area
            .background(colorResource(R.color.DarkNavy)) // Dark background for the grid itself
            .border(
                2.dp,
                colorResource(R.color.TextLight).copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            ) // Subtle border
            .padding(8.dp), // Inner padding
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
            .aspectRatio(1f) // Make cells square
            .clip(RoundedCornerShape(8.dp)) // Rounded corners for individual cells
            .clickable(onClick = onClick), // Make the cell clickable
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)) // Slightly off-white for cells
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
                    modifier = Modifier.size(64.dp), // Size of the X or O mark
                    colorFilter = null
                )
            }
        }
    }
}

