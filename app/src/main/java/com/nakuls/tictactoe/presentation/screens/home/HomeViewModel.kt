package com.nakuls.tictactoe.presentation.screens.home

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class HomeViewModel(
    private val gameRepository: GameRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _joinableGames = MutableStateFlow<List<Game>>(emptyList())
    val joinableGames: StateFlow<List<Game>> = _joinableGames

    init {
        getGames()
    }

    fun getGames() {
        viewModelScope.launch {
            val createdBy =  dataStore.data
                .map { preferences ->
                    preferences[Constants.USERID]
                }
                .firstOrNull()
            if(createdBy != null) {
                gameRepository.getJoinableGamesStream(createdBy).collect {
                    _joinableGames.value = it
                }
            }
        }
    }

    fun createGame(length: Int){

        viewModelScope.launch {
            val createdBy =  dataStore.data
                .map { preferences ->
                    preferences[Constants.USERID]
                }
                .firstOrNull()

            if(createdBy != null) {
                gameRepository.createGame(
                    createdBy = createdBy,
                    length = length,
                    status = 0
                )
            }
        }
    }

    fun joinGame(gameID: Int){

        viewModelScope.launch {
            val userID = dataStore.data
                .map { preferences ->
                    preferences[Constants.USERID]
                }
                .firstOrNull()

            if(userID != null) {
                gameRepository.joinGame(
                    gameID,
                    userID
                )
            }
        }
    }
}