package com.nakuls.tictactoe.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

val dataStoreModule = module {
    // 2. Define the DataStore instance as a Koin Singleton
    single {
        // We retrieve the Application Context, which is the receiver for the
        // top-level 'dataStore' property defined above.
        androidContext().dataStore
    }
}