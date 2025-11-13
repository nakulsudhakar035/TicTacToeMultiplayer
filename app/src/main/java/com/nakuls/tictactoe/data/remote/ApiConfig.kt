package com.nakuls.tictactoe.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.websocket.pingInterval

object ApiConfig {

    // IMPORTANT: Replace with your actual Supabase project ID
    private const val BASE_URL = "https://pnqmvpmodoubprsrxrhx.supabase.co"

    val httpClient = HttpClient(Android) {

        // 1. Content Negotiation: JSON Serialization
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        // 2. DefaultRequest: Set Base URL for REST calls
        install(DefaultRequest) {
            url(BASE_URL)
        }

        // 3. HttpTimeout: Set timeouts for REST calls
        install(HttpTimeout) {
            // Setting the timeout via the plugin is cleaner than the engine block
            requestTimeoutMillis = 15_000L
            connectTimeoutMillis = 10_000L
            socketTimeoutMillis = 10_000L
        }

        // 4. WebSockets: Mandatory for real-time game state
        install(WebSockets) {
            // WebSockets typically have a much longer timeout/ping interval
            pingInterval = 5.seconds // Auto-ping interval
        }

        // 5. Logging
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    // In an Android app, using Log.d is generally better
                    // for Logcat filtering than println, but this respects
                    // your original request.
                    println("Ktor Log: $message")
                }
            }
        }

        // Removed the redundant 'engine' block that was causing confusion.
    }
}