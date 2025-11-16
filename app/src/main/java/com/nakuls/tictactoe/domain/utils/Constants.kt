package com.nakuls.tictactoe.domain.utils

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object Constants {
    public val USERNAMEKEY = stringPreferencesKey("user_name")
    public val USEREMAILKEY = stringPreferencesKey("user_email")
    public val USERID = intPreferencesKey("user_id")
}