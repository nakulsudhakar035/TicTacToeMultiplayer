package com.nakuls.tictactoe.presentation.screens.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakuls.tictactoe.WinDetectionStrategy
import com.nakuls.tictactoe.domain.model.Game
import com.nakuls.tictactoe.domain.model.GameStatus
import com.nakuls.tictactoe.domain.model.Move
import com.nakuls.tictactoe.domain.model.Player
import com.nakuls.tictactoe.domain.model.PlayerStatus
import com.nakuls.tictactoe.stratergy.MiniMaxStrategy
import com.nakuls.tictactoe.stratergy.RowColumnDiagonalStratergy
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LocalGameViewModel(
    private val winDetectionStrategy: WinDetectionStrategy
) : ViewModel() {

    // Human is always 'x' (creator, goes first). Computer is always 'o'.
    private val humanPlayer = Player(id = 0, name = "You", status = PlayerStatus.Active, score = 0, symbol = 'x', email = "")
    private val computerPlayer = Player(id = 1, name = "Computer", status = PlayerStatus.Active, score = 0, symbol = 'o', email = "")

    private val _screenState = MutableStateFlow(GameScreenState())
    val screenState: StateFlow<GameScreenState> = _screenState.asStateFlow()

    // Reuse the same GameIntent sealed class for a consistent UI contract
    fun onIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.Initialize -> startGame()
            is GameIntent.MakeMove -> humanMove(intent.index)
            is GameIntent.ResetSession -> _screenState.update { it.copy(uiState = GameUiState.SessionReset) }
        }
    }

    private fun startGame() {
        // Allow restart when called from "Play Again" (game is over) or on first launch
        val current = _screenState.value
        if (current.game != null && current.uiState !is GameUiState.GameOver) return
        val game = Game(
            id = null,
            status = GameStatus.UnFilled,
            length = 3,
            owner = "You",
            winner = null,
            moveCount = 0,
            players = emptyList(),
            charArray = CharArray(9) { ' ' },
            winDetectionStratergy = RowColumnDiagonalStratergy()
        )
        _screenState.update {
            it.copy(
                game = game,
                isCreator = true,  // human is always 'x' / creator
                isMyTurn = true    // human goes first
            )
        }
    }

    private fun humanMove(index: Int) {
        val game = _screenState.value.game ?: return
        if (!_screenState.value.isMyTurn) return
        if (game.charArray[index] != ' ') return

        val afterHuman = applyMove(game, index, humanPlayer.symbol)
        _screenState.update { it.copy(game = afterHuman, isMyTurn = false) }

        if (checkAndEmitResult(afterHuman, humanPlayer, index)) return

        // Hand off to computer after a short delay so the human sees their move
        _screenState.update { it.copy(uiState = GameUiState.AwaitingOpponent) }
        viewModelScope.launch {
            delay(500L)
            computerMove(afterHuman)
        }
    }

    private fun computerMove(game: Game) {
        val bestIndex = MiniMaxStrategy.getBestMove(game.charArray.copyOf(), game.length)
        if (bestIndex == -1) return // board full, shouldn't happen here
        Log.d("TTT-LocalGame", "Computer plays index $bestIndex")

        val afterComputer = applyMove(game, bestIndex, computerPlayer.symbol)
        _screenState.update { it.copy(game = afterComputer) }

        if (checkAndEmitResult(afterComputer, computerPlayer, bestIndex)) return

        // Back to human's turn
        _screenState.update { it.copy(isMyTurn = true, uiState = GameUiState.Idle) }
    }

    /**
     * Checks win and draw after a move. Returns true if the game ended.
     */
    private fun checkAndEmitResult(game: Game, player: Player, moveIndex: Int): Boolean {
        val move = Move(id = null, player = player, game = null, index = moveIndex)
        if (winDetectionStrategy.checkIfMatchPoint(player, move, game)) {
            _screenState.update { it.copy(uiState = GameUiState.GameOver(winnerName = player.name)) }
            return true
        }
        if (game.charArray.none { it == ' ' }) {
            _screenState.update { it.copy(uiState = GameUiState.GameOver(winnerName = "", isDraw = true)) }
            return true
        }
        return false
    }

    private fun applyMove(game: Game, index: Int, symbol: Char): Game {
        val newArray = game.charArray.copyOf()
        newArray[index] = symbol
        return game.copy(charArray = newArray, moveCount = game.moveCount + 1)
    }
}
