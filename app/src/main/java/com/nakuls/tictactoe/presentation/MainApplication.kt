package com.nakuls.tictactoe.presentation

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import com.nakuls.tictactoe.presentation.di.appModule

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger() // Koin logs
            androidContext(this@MainApplication) // Provide Android context
            modules(appModule) // Your app's modules
        }
    }
}