package com.nakuls.tictactoe.presentation.screens.game

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakuls.tictactoe.domain.model.Game
import com.nakuls.tictactoe.domain.repository.GameRepository
import com.nakuls.tictactoe.presentation.screens.home.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GameUiState {
    object Idle : GameUiState()       // Initial state, ready for action
    object AwaitingMove : GameUiState()     // Fetching games
    object Processing : GameUiState()     // Processing creating or joining a game
    data class Success(val name: String) : GameUiState() // Save complete, contains the name
    data class Error(val message: String) : GameUiState() // Failed to save
}

class GameViewModel(
    private val gameRepository: GameRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Idle)
    val uiState: StateFlow<GameUiState> = _uiState

    private val _gameId = MutableStateFlow<Int?>(null)
    val gameId: StateFlow<Int?> = _gameId.asStateFlow()

    private val _game = MutableStateFlow<Game?>(null)
    val game: StateFlow<Game?> = _game.asStateFlow()

    fun initializeGame(id: Int?) {
        if (_gameId.value == null) {
            _gameId.value = id
        }
    }

    init {

        if(_game.value != null) {
            _game.value!!.charArray = CharArray(
                _game.value!!.length * _game.value!!.length
            )
        }
        gameRepository.toString()
        dataStore.toString()
    }

}