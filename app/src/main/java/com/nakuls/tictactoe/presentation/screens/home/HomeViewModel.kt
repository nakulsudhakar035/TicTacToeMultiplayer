package com.nakuls.tictactoe.presentation.screens.home

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakuls.tictactoe.domain.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.nakuls.tictactoe.domain.model.Game
import com.nakuls.tictactoe.domain.utils.Constants
import com.nakuls.tictactoe.presentation.screens.profile.ProfileUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import java.lang.Exception

sealed class HomeUiState {
    object Idle : HomeUiState()       // Initial state, ready for action
    object LoadingGames : HomeUiState()     // Fetching games
    object Processing : HomeUiState()     // Processing creating or joining a game
    data class Success(val name: String) : HomeUiState() // Save complete, contains the name
    data class Error(val message: String) : HomeUiState() // Failed to save
}
sealed class HomeNavigationEvent {
    data class NavigateToGame(val gameId: Int) : HomeNavigationEvent()
}
class HomeViewModel(
    private val gameRepository: GameRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState
    private val _joinableGames = MutableStateFlow<List<Game>>(emptyList())
    val joinableGames: StateFlow<List<Game>> = _joinableGames
    private val _hasActiveGames = MutableStateFlow(false)
    var hasActiveGames: StateFlow<Boolean> = _hasActiveGames
    private val _navigationEvents = MutableSharedFlow<HomeNavigationEvent>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    init {
        // 1. Create a flow pipeline to extract the Boolean from DataStore
        val dataStoreFlow = dataStore.data
            .map { preferences ->
                preferences[Constants.HASACTIVEGAMES] ?: false
            }
        // 2. Launch a coroutine to continuously COLLECT the DataStore flow.
        // Every time DataStore emits a new value, we update the private MutableStateFlow.
        dataStoreFlow
            .onEach { isGameActive ->
                // 3. Imperatively assign the collected Boolean value to the MutableStateFlow's value property
                _hasActiveGames.value = isGameActive
            }
            // 4. Start the collection and tie it to the ViewModel's lifecycle
            .launchIn(viewModelScope)
        getGames()
    }

    fun getGames() {
        _uiState.value = HomeUiState.LoadingGames
        viewModelScope.launch {
            val createdBy =  dataStore.data
                .map { preferences ->
                    preferences[Constants.USERID]
                }
                .firstOrNull()
            if(createdBy != null) {
                try {
                    _uiState.value = HomeUiState.Idle
                    gameRepository.getJoinableGamesStream(createdBy).collect {
                        _joinableGames.value = it
                    }
                } catch (ex: kotlin.Exception){
                    _uiState.value = HomeUiState.Error("Unable to fetch available games")
                }
            } else {
                _uiState.value = HomeUiState.Error("Unable to identify your profile")
                //TODO log user out
            }
        }
    }

    fun createGame(length: Int){
        _uiState.value = HomeUiState.Processing
        viewModelScope.launch {
            val createdBy =  dataStore.data
                .map { preferences ->
                    preferences[Constants.USERID]
                }
                .firstOrNull()

            if(createdBy != null) {
                var game: Game? = null
                try {
                    game = gameRepository.createGame(
                        createdBy = createdBy,
                        length = length,
                        status = 0
                    )
                    _hasActiveGames.value = game!=null
                    setActiveGamesStatus(_hasActiveGames.value)
                } catch (ex: Exception){
                    _uiState.value = HomeUiState.Error("Unable to create a game")
                } finally {
                    _uiState.value = HomeUiState.Idle
                    if(_hasActiveGames.value && game!= null && game.id != null){
                        gameRepository.startListeningForGameJoins(game.id!!).collect {
                            // navigate when joiner arrives
                            Log.i("checking","Listening for player 2")
                            _navigationEvents.emit(HomeNavigationEvent.NavigateToGame(game.id!!))
                        }
                    }
                }
            } else {
                _uiState.value = HomeUiState.Error("Unable to identify your profile")
                //TODO log user out
            }
        }
    }

    private suspend fun setActiveGamesStatus(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[Constants.HASACTIVEGAMES] = value
        }
    }

    fun joinGame(gameID: Int){
        _uiState.value = HomeUiState.Processing
        viewModelScope.launch {
            val userID = dataStore.data
                .map { preferences ->
                    preferences[Constants.USERID]
                }
                .firstOrNull()

            if(userID != null) {
                try {
                    _hasActiveGames.value = gameRepository.joinGame(
                        gameID,
                        userID
                    )
                    setActiveGamesStatus(_hasActiveGames.value)
                    _navigationEvents.emit(HomeNavigationEvent.NavigateToGame(gameID))
                } catch (ex: Exception){
                    _uiState.value = HomeUiState.Error("Unable to join the game")
                } finally {
                    _uiState.value = HomeUiState.Idle
                }
            } else {
                _uiState.value = HomeUiState.Error("Unable to identify your profile")
                //TODO log user out
            }
        }
    }
}