package com.nakuls.tictactoe.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nakuls.tictactoe.domain.repository.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

// DataStore instance (Keep this here)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tictactoe_user_prefs")

class UserRepositoryImpl(private val context: Context): UserRepository {

    private val USER_NAME_KEY = stringPreferencesKey("user_name")

    override suspend fun getUsername(): String? {
        return context.dataStore.data
            .map { preferences ->
                preferences[USER_NAME_KEY]
            }
            .firstOrNull()
    }

    override suspend fun saveUsername(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }

}