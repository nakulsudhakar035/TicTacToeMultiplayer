package com.nakuls.tictactoe.presentation

import android.app.Application
import com.nakuls.tictactoe.BuildConfig
import com.nakuls.tictactoe.data.local.dataStoreModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import com.nakuls.tictactoe.presentation.di.appModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import java.util.concurrent.TimeUnit

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initHttpClient()

        startKoin {
            androidLogger() // Koin logs
            androidContext(this@MainApplication) // Provide Android context
            modules(
                appModule
            )
            properties(mapOf("API_KEY" to BuildConfig.SUPABASE_API_KEY))
            properties(mapOf("AUTH_KEY" to BuildConfig.SUPABASE_AUTH_KEY))
            properties(mapOf("SUPABASE_URL" to BuildConfig.SUPABASE_URL))
        }
    }

    private fun initHttpClient() {
        HttpClient(OkHttp) {
            install(WebSockets) {
                maxFrameSize = Long.MAX_VALUE
            }
            engine {
                config {
                    retryOnConnectionFailure(true)
                    pingInterval(30, TimeUnit.SECONDS)
                }
            }
        }
    }
}