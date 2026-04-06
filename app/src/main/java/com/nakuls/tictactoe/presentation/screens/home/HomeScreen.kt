package com.nakuls.tictactoe.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nakuls.tictactoe.presentation.navigation.Screen
import com.nakuls.tictactoe.presentation.ui.theme.AccentBlue
import com.nakuls.tictactoe.presentation.ui.theme.BackgroundLight
import com.nakuls.tictactoe.presentation.ui.theme.CardBackground
import com.nakuls.tictactoe.presentation.ui.theme.HellesGreen
import org.koin.androidx.compose.koinViewModel
import com.nakuls.tictactoe.domain.model.Game

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var gameLengthInput by rememberSaveable { mutableStateOf("3") }
    val isLengthValid = remember(gameLengthInput) {
        gameLengthInput.toIntOrNull() != null && gameLengthInput.toIntOrNull()!! >= 2
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredGames = remember(screenState.games, searchQuery) {
        derivedStateOf {
            val list = if (searchQuery.isBlank()) {
                screenState.games
            } else {
                screenState.games.filter { game ->
                    val query = searchQuery.trim().lowercase()
                    game.owner.lowercase().contains(query) || game.id.toString().contains(query)
                }
            }
            list.sortedByDescending { it.id }
        }
    }

    // Navigation: observe uiState, navigate when NavigateToGame is emitted
    LaunchedEffect(screenState.uiState) {
        if (screenState.uiState is HomeUiState.NavigateToGame) {
            val gameId = (screenState.uiState as HomeUiState.NavigateToGame).gameId
            navController.navigate("game_screen/$gameId")
            viewModel.onNavigationConsumed()
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = BackgroundLight,
        ) {
            BottomSheetContent(
                gameLengthInput = gameLengthInput,
                onLengthChange = { gameLengthInput = it },
                isInputError = !isLengthValid,
                onCreateGame = {
                    val length = gameLengthInput.toIntOrNull()
                    if (isLengthValid) {
                        viewModel.createGame(length!!)
                        showBottomSheet = false
                    }
                },
                onDismiss = { showBottomSheet = false }
            )
        }
    }

    AppTheme {
        Scaffold(
            containerColor = BackgroundLight,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlayComputerButton(
                        onClick = { navController.navigate(Screen.LocalGame.route) }
                    )
                    NewGameButton(
                        isEnabled = !screenState.hasActiveGames,
                        onClick = { showBottomSheet = true }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Text(
                    text = "Make a move",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                GameSearchBar(
                    searchQuery = searchQuery,
                    onQueryChanged = { searchQuery = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (screenState.uiState is HomeUiState.LoadingGames) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = HellesGreen)
                    }
                } else {
                    AwaitingGamesList(
                        games = filteredGames.value,
                        onJoinGame = { gameId ->
                            viewModel.joinGame(gameId!!)
                        }
                    )
                }
            }
        }
    }
}

/**
 * --------------------------------------------------------------------------
 * 1. MAIN SCREEN AND THEME
 * --------------------------------------------------------------------------
 */

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = HellesGreen,
            secondary = AccentBlue,
            background = BackgroundLight,
            surface = CardBackground
        ),
        shapes = MaterialTheme.shapes.copy(
            extraLarge = RoundedCornerShape(24.dp),
            large = RoundedCornerShape(16.dp),
            medium = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(4.dp)
        ),
        typography = MaterialTheme.typography,
        content = content
    )
}


/**
 * --------------------------------------------------------------------------
 * 2. COMPONENTS
 * --------------------------------------------------------------------------
 */

@Composable
fun GameSearchBar(
    searchQuery: String,
    onQueryChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onQueryChanged,
        label = { Text("Search by Player, Email, or Game ID") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HellesGreen,
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
            focusedLabelColor = HellesGreen,
            cursorColor = HellesGreen,
            focusedContainerColor = CardBackground,
            unfocusedContainerColor = CardBackground
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun AwaitingGamesList(
    games: List<Game>,
    onJoinGame: (Int?) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(games.size) { game ->
            GameListItem(game = games[game], onJoinGame = onJoinGame)
        }
    }
}

@Composable
fun GameListItem(game: Game, onJoinGame: (Int?) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onJoinGame(game.id) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AccentBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = game.owner[0].toString(),
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "vs. ${game.owner}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "Game ID: ${game.id} | Of size: ${game.length} * ${game.length}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(
                    onClick = { onJoinGame(game.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HellesGreen),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("JOIN", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun PlayComputerButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(60.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = HellesGreen),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Text(
            text = "PLAY VS COMPUTER",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

@Composable
fun NewGameButton(isEnabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(60.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Add, contentDescription = "Start New Game")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "START NEW GAME",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun BottomSheetContent(
    gameLengthInput: String,
    onLengthChange: (String) -> Unit,
    isInputError: Boolean,
    onCreateGame: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Configure New Game",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = gameLengthInput,
            onValueChange = {
                if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                    onLengthChange(it)
                }
            },
            label = { Text("Game Length (e.g., 3 for 3x3)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            singleLine = true,
            isError = isInputError,
            supportingText = {
                if (isInputError) {
                    Text(
                        text = "Must be a number greater than or equal to 2.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )
        if (!isInputError) {
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onCreateGame,
                enabled = !isInputError,
                modifier = Modifier.weight(1f)
            ) {
                Text("CREATE GAME")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
