package com.nakuls.tictactoe.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.nakuls.tictactoe.data.local.entity.UserProfile
import com.nakuls.tictactoe.domain.utils.Constants
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class UserProfileLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>
) : UserProfileLocal {

    override suspend fun createProfile(userProfile: UserProfile): Boolean {
        try {
            dataStore.edit { preferences ->
                preferences[Constants.USERID] = userProfile.id
                preferences[Constants.USERNAMEKEY] = userProfile.name
                preferences[Constants.USEREMAILKEY] = userProfile.email
            }
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    override suspend fun fetchProfileName(): String? {
        return dataStore.data
            .map { preferences ->
                preferences[Constants.USERNAMEKEY]
            }
            .firstOrNull()
    }

}