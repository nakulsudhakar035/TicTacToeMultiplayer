package com.nakuls.tictactoe.presentation.screens.game

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakuls.tictactoe.WinDetectionStrategy
import com.nakuls.tictactoe.data.remote.dto.toMove
import com.nakuls.tictactoe.domain.model.Game
import com.nakuls.tictactoe.domain.model.GameStatus
import com.nakuls.tictactoe.domain.model.Player
import com.nakuls.tictactoe.domain.model.PlayerStatus
import com.nakuls.tictactoe.domain.repository.GameRepository
import com.nakuls.tictactoe.domain.utils.Constants
import com.nakuls.tictactoe.presentation.screens.home.HomeUiState
import com.nakuls.tictactoe.stratergy.RowColumnDiagonalStratergy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.remove

sealed class GameUiState {
    object Idle : GameUiState()       // Initial state, ready for action
    object AwaitingMove : GameUiState()     // Fetching games
    object Processing : GameUiState()     // Processing creating or joining a game
    data class Success(val name: String) : GameUiState() // Save complete, contains the name
    data class Error(val message: String) : GameUiState() // Failed to save
}

class GameViewModel(
    private val gameRepository: GameRepository,
    private val dataStore: DataStore<Preferences>,
    private val winDetectionStrategy: WinDetectionStrategy
) : ViewModel() {

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Idle)
    val uiState: StateFlow<GameUiState> = _uiState

    private val _gameId = MutableStateFlow<Int?>(null)
    val gameId: StateFlow<Int?> = _gameId.asStateFlow()

    private val _gamePlayerId = MutableStateFlow<Int?>(null)
    val gamePlayerId: StateFlow<Int?> = _gamePlayerId.asStateFlow()

    private val _game = MutableStateFlow<Game?>(null)
    val game: StateFlow<Game?> = _game.asStateFlow()

    val isCreator: StateFlow<Boolean> = dataStore.data
        .map { preferences ->
            val value = preferences[Constants.ISGAMEOWNER] ?: false
            Log.d("TTT-Auth", "Flow emitted isCreator: $value")
            value
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun initializeGame(id: Int?) {
        if(_game.value == null) {
            _gameId.value = id
            _game.value = Game(
                id = id,
                status = GameStatus.UnFilled, // Use your actual enum
                length = 3,
                owner = "",
                winner = null,
                moveCount = 0,
                players = emptyList(),
                charArray = CharArray(9) { ' ' },
                winDetectionStratergy = RowColumnDiagonalStratergy()
            )
        }

        // Start listening for moves as soon as we have a Game ID
        observeOpponentMoves(id)
    }

    private fun observeOpponentMoves(gameId: Int?) {

        viewModelScope.launch {

            if (gameId == null) return@launch // Safety check
            _uiState.value = GameUiState.Idle
            // 1. Get our own ID from DataStore to filter out our own moves
            val myRoleIsCreator = dataStore.data.map { it[Constants.ISGAMEOWNER] ?: false }.first()
            val myPlayerId = dataStore.data.map { it[Constants.GAMEPLAYERID] }.firstOrNull() ?: return@launch

            // 2. Start the realtime listener
            gameRepository.startListeningForMovesInGame()
                .collect { move ->
                    val player = Player(
                        id = 0,
                        name = "",
                        status = PlayerStatus.Active,
                        score = 0,
                        symbol = 'o',
                        email = ""
                    )
                    Log.i("TTT - observeOpponentMoves",move.toString())
                    if (move.playerID == myPlayerId) {
                        // Now we are sure myRoleIsCreator is accurate for THIS session
                        val mark = if (myRoleIsCreator) 'x' else 'o'
                        val name = if (myRoleIsCreator) "You" else "Opponent"
                        player.name = name
                        player.symbol = mark
                        updateLocalBoard(move.index, mark)
                        _uiState.value = GameUiState.AwaitingMove
                    } else {
                        val mark = if (!myRoleIsCreator) 'x' else 'o'
                        val name = if (!myRoleIsCreator) "You" else "Opponent"
                        player.symbol = mark
                        player.name = name
                        updateLocalBoard(move.index, mark)
                        //_uiState.value = GameUiState.AwaitingMove
                    }
                    if ((_game.value!!).moveCount <= (_game.value!!).charArray.size && winDetectionStrategy.checkIfMatchPoint(
                            player,
                            move.toMove(),
                            _game.value!!)) {
                        println("${player.name} wins")
                        _uiState.value = GameUiState.Success(player.name)
                        Log.i("TTT", "Winner found: ${player.name}")
                    }
                }
        }
    }

    init {
        gameRepository.toString()
        dataStore.toString()
    }

    private fun updateLocalBoard(index: Int, char: Char) {
        val current = _game.value ?: return
        // 1. Create a NEW array instance (different memory address)
        val newArray = current.charArray.copyOf()
        newArray[index] = char
        // 2. Push a NEW Game object into the StateFlow
        // This triggers the UI update
        _game.value = current.copy(
            charArray = newArray,
            moveCount = current.moveCount + 1)
        Log.i("TTT - updateLocalBoard",_game.value.toString())
    }

    suspend fun makeMove(index: Int): Boolean {
        // 1. Get the player ID from DataStore
        val createdBy = dataStore.data
            .map { preferences ->
                preferences[Constants.GAMEPLAYERID]
            }
            .firstOrNull() ?: return false // Return false if no ID found

        // 2. Call the repository directly (suspend call)
        return gameRepository.makeMove(index, createdBy)
    }

    fun resetGameSession(onComplete: () -> Unit) {
        _uiState.value = GameUiState.Processing
        viewModelScope.launch {
            dataStore.edit { preferences ->
                // Clear only game-related keys, keep user/auth keys
                preferences.remove(Constants.GAMEPLAYERID)
                preferences.remove(Constants.ISGAMEOWNER)
                preferences.remove(Constants.HASACTIVEGAMES)
            }
            _uiState.value = GameUiState.Idle
            onComplete()
        }
    }
}