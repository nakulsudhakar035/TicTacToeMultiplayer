package com.nakuls.tictactoe.data.remote

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nakuls.tictactoe.data.remote.dto.ProfileCreationDTO
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Returning
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode

class UserProfileRemoteDataSourceImpl(
    private val supabaseClient: SupabaseClient
) : UserProfileAPI {

    override suspend fun createProfile(profileCreationDTO: ProfileCreationDTO): ProfileCreationDTO? {

        return try {
            val result = supabaseClient.postgrest["player"].insert(
                value = profileCreationDTO,
                request = {
                    // We set the returning preference inside the lambda
                    select(
                        columns = Columns.list(
                        "id", "name",
                        "status", "score", "email"
                    ))

                    /*Returning.Representation(Columns.list(
                        "id", "name",
                        "status", "score", "email"
                    ))*/
                }
            )
            Log.i("create player",result.data)
            result.decodeSingle<ProfileCreationDTO>()
        } catch (e: Exception) {
            // Handle RLS errors, constraint violations, or network issues
            println("Error inserting game player: ${e.message}")
            null
        }
    }

}