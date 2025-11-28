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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nakuls.tictactoe.R
import com.nakuls.tictactoe.domain.model.Move
import com.nakuls.tictactoe.presentation.screens.home.AppTheme
import com.nakuls.tictactoe.presentation.screens.home.HomeViewModel
import com.nakuls.tictactoe.presentation.screens.home.NewGameButton
import com.nakuls.tictactoe.presentation.ui.theme.BackgroundLight
import org.koin.androidx.compose.koinViewModel
import kotlin.collections.List

// Define some example colors and resources (you'll need to adapt these)
val PlayerOCardColor = Color(0xFFC7E8F3) // Light blue for O's score card
val PlayerXCardColor = Color(0xFFE8F3C7) // Light green for X's score card
val DarkNavy = Color(0xFF1A2A44) // A dark background color (for cards etc.)
val TextLight = Color(0xFFF0F0F0) // Light text color

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
    LaunchedEffect(key1 = Unit) {
        viewModel.initializeGame(gameID)
    }

    AppTheme {
        Scaffold(
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
                                tint = DarkNavy,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reset Game",
                                color = DarkNavy,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Game(
                currentTurn = Mark.X,
                onCellClicked = {},
                modifier = Modifier.padding(paddingValues),
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun Game(
    modifier: Modifier = Modifier,
    currentTurn: Mark = Mark.X, // Who's turn is it?
    board: List<Mark> = remember { List(9) { Mark.NONE } }, // 3x3 board state
    onCellClicked: (Int) -> Unit = {},
    viewModel: GameViewModel
) {
    // For demonstration, let's create a local mutable board state
    // In a real app, this would come from the ViewModel
    var currentBoard by remember { mutableStateOf(board) }
    var currentPlayerTurn by remember { mutableStateOf(currentTurn) }

    // This Box serves as the main container and applies padding
    Box(
        modifier = modifier.fillMaxSize()
            .padding(top = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp) // Spacing between major sections
        ) {
            // --- Scoreboard Section ---
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScoreCard(
                    playerMark = Mark.O,
                    score = 0,
                    isCurrentPlayer = currentPlayerTurn == Mark.O,
                    cardColor = PlayerOCardColor // Light blue
                )
                ScoreCard(
                    playerMark = Mark.X,
                    score = 0,
                    isCurrentPlayer = currentPlayerTurn == Mark.X,
                    cardColor = PlayerXCardColor // Light green
                )
            }

            // --- Turn Indicator ---
            Text(
                text = if (currentPlayerTurn == Mark.X) "Your Turn" else "Opponent's Turn", // Dynamic turn display
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextLight,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // --- Game Grid ---
            TicTacToeGrid(
                boardState = currentBoard,
                onCellClick = { index ->
                    // Example logic to update board (replace with ViewModel logic)
                    if (currentBoard[index] == Mark.NONE) {
                        /*val newBoard = currentBoard.toMutableList()
                        newBoard[index] = currentPlayerTurn
                        currentBoard = newBoard

                        // Switch turn for demonstration
                        currentPlayerTurn = if (currentPlayerTurn == Mark.O) Mark.X else Mark.O

                        // In real app, call ViewModel:
                        onCellClicked(index)*/
                        viewModel.makeMove(index)
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
                color = if (isCurrentPlayer) TextLight else Color.Transparent, // Highlight current player
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
                    text = if (playerMark == Mark.O) "You :" else "Bot :", // Dynamic player name
                    color = DarkNavy,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = score.toString(),
                color = DarkNavy,
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
            .fillMaxWidth()
            .aspectRatio(1f) // Make the grid square
            .clip(RoundedCornerShape(16.dp)) // Rounded corners for the entire grid area
            .background(DarkNavy) // Dark background for the grid itself
            .border(2.dp, TextLight.copy(alpha = 0.5f), RoundedCornerShape(16.dp)) // Subtle border
            .padding(8.dp), // Inner padding
        contentAlignment = Alignment.Center
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // 3 columns for Tic-Tac-Toe
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp), // Spacing between cells
            verticalArrangement = Arrangement.spacedBy(8.dp),   // Spacing between rows
        ) {
            itemsIndexed(boardState) { index, mark ->
                GridCell(
                    mark = mark,
                    onClick = { onCellClick(index) }
                )
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

