package com.nakuls.tictactoe.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.nakuls.tictactoe.domain.utils.Constants
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class GameLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>
) : GameLocal {

    override suspend fun createGame(gameID: Int): Boolean {
        try {
            dataStore.edit { preferences ->
                preferences[Constants.GAMEID] = gameID
            }
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    override suspend fun createGamePlayer(gamePlayerID: Int): Boolean {
        try {
            dataStore.edit { preferences ->
                preferences[Constants.GAMEPLAYERID] = gamePlayerID
            }
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    override suspend fun setIsCreater(isCreater: Boolean): Boolean {
        try {
            dataStore.edit { preferences ->
                preferences[Constants.ISGAMEOWNER] = isCreater
            }
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    override suspend fun fetchGameID(): Int? {
        return dataStore.data
            .map { preferences ->
                preferences[Constants.GAMEID]
            }
            .firstOrNull()
    }

    override suspend fun fetchGamePlayerID(): Int? {
        return dataStore.data
            .map { preferences ->
                preferences[Constants.GAMEPLAYERID]
            }
            .firstOrNull()
    }

    override suspend fun fetchIsCreater(): Boolean? {
        return dataStore.data
            .map { preferences ->
                preferences[Constants.ISGAMEOWNER]
            }
            .firstOrNull()
    }

}