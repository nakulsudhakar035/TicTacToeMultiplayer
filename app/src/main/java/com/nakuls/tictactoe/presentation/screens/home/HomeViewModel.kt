package com.nakuls.tictactoe.presentation.screens.home

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakuls.tictactoe.domain.model.Game
import com.nakuls.tictactoe.domain.model.GamePlayer
import com.nakuls.tictactoe.domain.repository.GameRepository
import com.nakuls.tictactoe.domain.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Idle : HomeUiState()
    object LoadingGames : HomeUiState()
    object Processing : HomeUiState()
    data class NavigateToGame(val gameId: Int) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

data class HomeScreenState(
    val games: List<Game> = emptyList(),
    val hasActiveGames: Boolean = false,
    val uiState: HomeUiState = HomeUiState.Idle
)

class HomeViewModel(
    private val gameRepository: GameRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _screenState = MutableStateFlow(HomeScreenState())
    val screenState: StateFlow<HomeScreenState> = _screenState.asStateFlow()

    init {
        dataStore.data
            .map { preferences -> preferences[Constants.HASACTIVEGAMES] ?: false }
            .onEach { isGameActive ->
                _screenState.update { it.copy(hasActiveGames = isGameActive) }
            }
            .launchIn(viewModelScope)
        getGames()
    }

    fun onNavigationConsumed() {
        _screenState.update { it.copy(uiState = HomeUiState.Idle) }
    }

    private fun getGames() {
        _screenState.update { it.copy(uiState = HomeUiState.LoadingGames) }
        viewModelScope.launch {
            val createdBy = dataStore.data
                .map { preferences -> preferences[Constants.USERID] }
                .firstOrNull()
            if (createdBy != null) {
                try {
                    gameRepository.getJoinableGamesStream(createdBy).collect { games ->
                        _screenState.update { it.copy(games = games, uiState = HomeUiState.Idle) }
                    }
                } catch (ex: Exception) {
                    _screenState.update { it.copy(uiState = HomeUiState.Error("Unable to fetch available games")) }
                }
            } else {
                _screenState.update { it.copy(uiState = HomeUiState.Error("Unable to identify your profile")) }
            }
        }
    }

    fun createGame(length: Int) {
        _screenState.update { it.copy(uiState = HomeUiState.Processing) }
        viewModelScope.launch {
            val createdBy = dataStore.data
                .map { preferences -> preferences[Constants.USERID] }
                .firstOrNull()
            if (createdBy != null) {
                var gamePlayer: GamePlayer? = null
                try {
                    gamePlayer = gameRepository.createGame(
                        createdBy = createdBy,
                        length = length,
                        status = 0
                    )
                    val hasActiveGames = gamePlayer != null
                    _screenState.update { it.copy(hasActiveGames = hasActiveGames) }
                    setActiveGamesStatus(hasActiveGames)
                } catch (ex: Exception) {
                    _screenState.update { it.copy(uiState = HomeUiState.Error("Unable to create a game")) }
                } finally {
                    Log.i("TTT - checking", "inside finally")
                    if (_screenState.value.hasActiveGames && gamePlayer?.gameID != null) {
                        _screenState.update { it.copy(uiState = HomeUiState.Processing) }
                        gameRepository.startListeningForGameJoins(gamePlayer.gameID!!).collect {
                            Log.i("TTT - checking", "Listening for player 2")
                            _screenState.update { it.copy(uiState = HomeUiState.NavigateToGame(gamePlayer.gameID!!)) }
                        }
                    }
                }
            } else {
                _screenState.update { it.copy(uiState = HomeUiState.Error("Unable to identify your profile")) }
            }
        }
    }

    private suspend fun setActiveGamesStatus(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[Constants.HASACTIVEGAMES] = value
        }
    }

    fun joinGame(gameID: Int) {
        _screenState.update { it.copy(uiState = HomeUiState.Processing) }
        viewModelScope.launch {
            val userID = dataStore.data
                .map { preferences -> preferences[Constants.USERID] }
                .firstOrNull()
            if (userID != null) {
                try {
                    val gamePlayerID = gameRepository.joinGame(gameID, userID)
                    val hasActiveGames = gamePlayerID != null
                    _screenState.update { it.copy(hasActiveGames = hasActiveGames) }
                    setActiveGamesStatus(hasActiveGames)
                    _screenState.update { it.copy(uiState = HomeUiState.NavigateToGame(gameID)) }
                } catch (ex: Exception) {
                    _screenState.update { it.copy(uiState = HomeUiState.Error("Unable to join the game")) }
                }
            } else {
                _screenState.update { it.copy(uiState = HomeUiState.Error("Unable to identify your profile")) }
            }
        }
    }
}