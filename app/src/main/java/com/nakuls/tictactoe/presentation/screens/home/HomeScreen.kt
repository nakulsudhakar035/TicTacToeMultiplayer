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
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nakuls.tictactoe.stratergy.RowColumnDiagonalStratergy
import com.nakuls.tictactoe.presentation.ui.theme.AccentBlue
import com.nakuls.tictactoe.presentation.ui.theme.BackgroundLight
import com.nakuls.tictactoe.presentation.ui.theme.CardBackground
import com.nakuls.tictactoe.presentation.ui.theme.HellesGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.koin.androidx.compose.koinViewModel
import com.nakuls.tictactoe.domain.model.Game
import com.nakuls.tictactoe.domain.model.GameStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val games by viewModel.joinableGames.collectAsState()
    // 1. State to control the visibility of the Bottom Sheet
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    // 2. State to hold the user input for game length
    var gameLengthInput by rememberSaveable { mutableStateOf("3") }
    val isLengthValid = remember(gameLengthInput) {
        gameLengthInput.toIntOrNull() != null && gameLengthInput.toIntOrNull()!! >= 2
    }

    // 1. State for the search query
    var searchQuery by rememberSaveable { mutableStateOf("") } // Store the query

    // 2. Filter the list based on the search query
    val filteredGames = remember(games, searchQuery) {
        if (searchQuery.isBlank()) {
            games // Show all games if search is empty
        } else {
            games.filter { game ->
                val query = searchQuery.trim().lowercase()

                // Add your filtering criteria here:
                val matchesPlayer = game.owner.lowercase().contains(query)
                val matchesGameId = game.id.toString().contains(query)
                // Assuming email is available in your DTO, add:
                // val matchesEmail = game.player.email.lowercase().contains(query)

                matchesPlayer || matchesGameId
            }
        }
    }

    // Use ModalBottomSheet for the slide-up window
    if (showBottomSheet) {
        // Material 3 Modal Bottom Sheet
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false // Dismiss when swiped down or tapped outside
            },
            // Optional: Customize container/content
            containerColor = BackgroundLight,
        ) {
            // Content of the Bottom Sheet
            BottomSheetContent(
                gameLengthInput = gameLengthInput,
                onLengthChange = { gameLengthInput = it },
                isInputError = !isLengthValid,
                onCreateGame = {
                    // 1. Validate input and convert to Int
                    val length = gameLengthInput.toIntOrNull()
                    if (isLengthValid) {
                        // 2. Call ViewModel action
                        viewModel.createGame(length!!)
                        // 3. Close the sheet
                        showBottomSheet = false
                    }
                },
                onDismiss = { showBottomSheet = false }
            )
        }
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
                    NewGameButton(
                        onClick = {
                            showBottomSheet = true
                    })
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Text(
                    text = "Awaiting Matches",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Top section: Search Bar and List
                GameSearchBar(
                    searchQuery = searchQuery,
                    onQueryChanged = { newQuery ->
                        searchQuery = newQuery // Update the state when text changes
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                AwaitingGamesList(
                    games = filteredGames,
                    isLoading = false,
                    error = null,
                    onJoinGame = { gameId ->
                        viewModel.joinGame(gameId!!)
                    }
                )
            }
        }
    }
}

data class HomeScreenState(
    val awaitingGames: List<Game> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class HomeScreenEvent {
    data class SearchQueryChanged(val query: String) : HomeScreenEvent()
    data class JoinGameClicked(val gameId: String) : HomeScreenEvent()
    object NewGameClicked : HomeScreenEvent()
}


/**
 * --------------------------------------------------------------------------
 * 1. MAIN SCREEN AND THEME
 * --------------------------------------------------------------------------
 */

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    // Simplified theme for a vibrant, clean look
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = HellesGreen,
            secondary = AccentBlue,
            background = BackgroundLight,
            surface = CardBackground
        ),
        shapes = MaterialTheme.shapes.copy(
            extraLarge = RoundedCornerShape(24.dp), // For the button
            large = RoundedCornerShape(16.dp), // For cards
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
    isLoading: Boolean,
    error: String?,
    onJoinGame: (Int?) -> Unit
) {
    when {
        isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HellesGreen)
            }
        }
        error != null -> {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Error loading games: $error", color = MaterialTheme.colorScheme.error)
            }
        }
        games.isEmpty() -> {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("No games currently awaiting a second player. Start a new one!", color = Color.Gray)
            }
        }
        else -> {
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
            // Player Icon & Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                // avatar/icon
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
                // Action Button
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
fun NewGameButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(60.dp),
        shape = RoundedCornerShape(24.dp), // Extra Large shape for primary action
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

// Helper Composable for the Bottom Sheet Content
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
        // --- Title ---
        Text(
            text = "Configure New Game",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- Input Field ---
        OutlinedTextField(
            value = gameLengthInput,
            onValueChange = {
                // Basic numeric filtering
                if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                    onLengthChange(it)
                }
            },
            label = { Text("Game Length (e.g., 3 for 3x3)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            singleLine = true,
            isError = isInputError, // Toggles red border and error color

            // Optional: Provide a helper text displaying the error message
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
        // --- Buttons ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Dismiss Button
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Cancel")
            }

            // Create Game Button
            Button(
                onClick = onCreateGame,
                enabled = !isInputError, // Enable only if valid number
                modifier = Modifier.weight(1f)
            ) {
                Text("CREATE GAME")
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Padding below buttons
    }
}


/**
 * --------------------------------------------------------------------------
 * 3. PREVIEW FUNCTION
 * --------------------------------------------------------------------------
 */

@Preview(showBackground = true)
@Composable
fun PreviewGameHomeScreen() {
    val mockGames = listOf(
        Game(
            1, GameStatus.UnFilled, 3, "Player 1", null,
            moveCount = 0,
            players = null,
            charArray = charArrayOf(),
            winDetectionStratergy = RowColumnDiagonalStratergy()
        ),
        Game(2, GameStatus.UnFilled, 3, "Player 2", null,
            moveCount = 0,
            players = null,
            charArray = charArrayOf(),
            winDetectionStratergy = RowColumnDiagonalStratergy()),
        Game(3, GameStatus.Filled, 4, "Player 3", null,
            moveCount = 0,
            players = null,
            charArray = charArrayOf(),
            winDetectionStratergy = RowColumnDiagonalStratergy()),
        Game(4, GameStatus.UnFilled, 5, "Player 4", null,
            moveCount = 0,
            players = null,
            charArray = charArrayOf(),
            winDetectionStratergy = RowColumnDiagonalStratergy()),
    )

    // Mock state management for preview
    var state by remember {
        mutableStateOf(
            HomeScreenState(
                awaitingGames = mockGames,
                isLoading = false
            )
        )
    }

    // Mock event handler
    val onEvent: (HomeScreenEvent) -> Unit = { event ->
        when (event) {
            is HomeScreenEvent.SearchQueryChanged -> {
                state = state.copy(searchQuery = event.query)
            }
            is HomeScreenEvent.JoinGameClicked -> {
                println("Joined game: ${event.gameId}")
            }
            is HomeScreenEvent.NewGameClicked -> {
                // Simulate loading for the button click
                runBlocking {
                    state = state.copy(isLoading = true)
                    delay(500) // Simulate delay
                    state = state.copy(isLoading = false)
                    println("Starting new game...")
                }
            }
        }
    }

    HomeScreen(
        navController = rememberNavController()
    )
}