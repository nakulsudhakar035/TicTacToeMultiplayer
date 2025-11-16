package com.nakuls.tictactoe.data.remote

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nakuls.tictactoe.data.remote.dto.ProfileCreationDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode

class UserProfileRemoteDataSourceImpl(
    private val client: HttpClient,
    private val auth_key: String,
    private val apiKey: String
) : UserProfileAPI {

    private val USERS_ENDPOINT = "/rest/v1/player"

    override suspend fun createProfile(requestBody: ProfileCreationDTO): ProfileCreationDTO? {
        val response = client.post(USERS_ENDPOINT) {
            // Add Supabase required headers
            headers["apikey"] = apiKey
            headers["Authorization"] = "Bearer $auth_key"
            headers["Content-Type"] = ContentType.Application.Json.toString()
            headers["Prefer"] = "return=representation"

            // Set the request body
            setBody(requestBody)
        }
        if(response.status == HttpStatusCode.Created){
            val returnedPlayerList = response.body<List<ProfileCreationDTO>>()
            val createdProfile = returnedPlayerList.firstOrNull()
            return createdProfile
        }
        return null
    }

    override suspend fun fetchProfileName(): String? {
        val response = client.get(USERS_ENDPOINT) {
            headers["apikey"] = apiKey
            headers["Authorization"] = "Bearer $apiKey"
        }
        return if (response.status == HttpStatusCode.OK) {
            // In a real app, you parse the List<UserDto> and extract the name
            // For simplicity, returning a placeholder string
            "FetchedUserNameFromKtor"
        } else {
            null
        }
    }

}