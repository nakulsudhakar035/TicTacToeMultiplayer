package com.nakuls.tictactoe.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets

val supabase = createSupabaseClient(
    supabaseUrl = "https://pnqmvpmodoubprsrxrhx.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBucW12cG1vZG91YnByc3J4cmh4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA5OTY1NTAsImV4cCI6MjA3NjU3MjU1MH0.7ya7rjHL_3Zhuccb38a_qenDjn_SlsloVXnCoFVuPvI",
) {
    install(Postgrest)
    install(Realtime)
}