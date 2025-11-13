package com.nakuls.tictactoe.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nakuls.tictactoe.data.local.entity.UserProfile
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class UserProfileLocalDataSourceImpl(
    private val context: Context
) : UserProfileLocal {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")

    override suspend fun createProfile(userProfile: UserProfile): Boolean {
        try {
            context.dataStore.edit { preferences ->
                preferences[USER_NAME_KEY] = userProfile.name
                preferences[USER_EMAIL_KEY] = userProfile.email
            }
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    override suspend fun fetchProfileName(): String? {
        return context.dataStore.data
            .map { preferences ->
                preferences[USER_NAME_KEY]
            }
            .firstOrNull()
    }

}