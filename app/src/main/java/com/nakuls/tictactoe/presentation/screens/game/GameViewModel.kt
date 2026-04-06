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
import com.nakuls.tictactoe.stratergy.RowColumnDiagonalStratergy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class GameUiState {
    object Idle : GameUiState()
    object AwaitingOpponent : GameUiState()
    object Processing : GameUiState()
    data class GameOver(val winnerName: String, val isDraw: Boolean = false) : GameUiState()
    data class Error(val message: String) : GameUiState()
    object SessionReset : GameUiState()
}

sealed class GameIntent {
    data class Initialize(val gameId: Int) : GameIntent()
    data class MakeMove(val index: Int) : GameIntent()
    object ResetSession : GameIntent()
}

data class GameScreenState(
    val game: Game? = null,
    val isCreator: Boolean = false,
    val isMyTurn: Boolean = false,
    val uiState: GameUiState = GameUiState.Idle
)

class GameViewModel(
    private val gameRepository: GameRepository,
    private val dataStore: DataStore<Preferences>,
    private val winDetectionStrategy: WinDetectionStrategy
) : ViewModel() {

    private val _screenState = MutableStateFlow(GameScreenState())
    val screenState: StateFlow<GameScreenState> = _screenState.asStateFlow()

    fun onIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.Initialize -> initializeGame(intent.gameId)
            is GameIntent.MakeMove -> makeMove(intent.index)
            is GameIntent.ResetSession -> resetGameSession()
        }
    }

    private fun initializeGame(id: Int) {
        if (_screenState.value.game != null) return
        viewModelScope.launch {
            val isCreator = dataStore.data.map { it[Constants.ISGAMEOWNER] ?: false }.first()
            val game = Game(
                id = id,
                status = GameStatus.UnFilled,
                length = 3,
                owner = "",
                winner = null,
                moveCount = 0,
                players = emptyList(),
                charArray = CharArray(9) { ' ' },
                winDetectionStratergy = RowColumnDiagonalStratergy()
            )
            // Creator (X) always goes first
            _screenState.update { it.copy(game = game, isCreator = isCreator, isMyTurn = isCreator) }
            observeOpponentMoves()
        }
    }

    private fun observeOpponentMoves() {
        viewModelScope.launch {
            val myRoleIsCreator = dataStore.data.map { it[Constants.ISGAMEOWNER] ?: false }.first()
            val myPlayerId = dataStore.data.map { it[Constants.GAMEPLAYERID] }.firstOrNull()
                ?: return@launch

            gameRepository.startListeningForMovesInGame().collect { move ->
                val isMyMove = move.playerID == myPlayerId
                // Creator is always 'x', joiner is always 'o'
                val mark = if (isMyMove == myRoleIsCreator) 'x' else 'o'
                val name = if (isMyMove) "You" else "Opponent"

                Log.i("TTT - observeOpponentMoves", move.toString())
                updateLocalBoard(move.index, mark)

                val currentGame = _screenState.value.game ?: return@collect
                val player = Player(
                    id = 0,
                    name = name,
                    status = PlayerStatus.Active,
                    score = 0,
                    symbol = mark,
                    email = ""
                )

                if (currentGame.moveCount <= currentGame.charArray.size &&
                    winDetectionStrategy.checkIfMatchPoint(player, move.toMove(), currentGame)
                ) {
                    Log.i("TTT", "Winner found: $name")
                    _screenState.update { it.copy(uiState = GameUiState.GameOver(name)) }
                } else if (isMyMove) {
                    _screenState.update { it.copy(uiState = GameUiState.AwaitingOpponent) }
                }
            }
        }
    }

    private fun makeMove(index: Int) {
        viewModelScope.launch {
            val createdBy = dataStore.data
                .map { preferences -> preferences[Constants.GAMEPLAYERID] }
                .firstOrNull() ?: run {
                _screenState.update { it.copy(uiState = GameUiState.Error("Unable to identify player")) }
                return@launch
            }
            val success = gameRepository.makeMove(index, createdBy)
            if (!success) {
                _screenState.update { it.copy(uiState = GameUiState.Error("Move failed, please retry")) }
            }
        }
    }

    private fun updateLocalBoard(index: Int, char: Char) {
        val current = _screenState.value.game ?: return
        val newArray = current.charArray.copyOf()
        newArray[index] = char
        val newMoveCount = current.moveCount + 1
        // Even moveCount → X's turn (creator). Odd → O's turn (joiner).
        val isCreator = _screenState.value.isCreator
        val isMyTurn = (newMoveCount % 2 == 0) == isCreator
        _screenState.update {
            it.copy(
                game = current.copy(charArray = newArray, moveCount = newMoveCount),
                isMyTurn = isMyTurn
            )
        }
        Log.i("TTT - updateLocalBoard", _screenState.value.game.toString())
    }

    private fun resetGameSession() {
        _screenState.update { it.copy(uiState = GameUiState.Processing) }
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences.remove(Constants.GAMEPLAYERID)
                preferences.remove(Constants.ISGAMEOWNER)
                preferences.remove(Constants.HASACTIVEGAMES)
            }
            _screenState.update { it.copy(uiState = GameUiState.SessionReset) }
        }
    }
}
