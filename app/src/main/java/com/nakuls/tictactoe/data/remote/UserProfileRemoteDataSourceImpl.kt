package com.nakuls.tictactoe.data.remote

import com.nakuls.tictactoe.data.remote.dto.ProfileCreationDTO
import io.ktor.client.HttpClient
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

    override suspend fun createProfile(requestBody: ProfileCreationDTO): Boolean {
        val response = client.post(USERS_ENDPOINT) {
            // Add Supabase required headers
            headers["apikey"] = apiKey
            headers["Authorization"] = "Bearer $auth_key"
            headers["Content-Type"] = ContentType.Application.Json.toString()

            // Set the request body
            setBody(requestBody)
        }
        return response.status == HttpStatusCode.Created
    }

    override suspend fun fetchProfileName(): String? {
        val response = client.get(USERS_ENDPOINT) {
            headers["apikey"] = apiKey
            headers["Authorization"] = "Bearer $apiKey"

            // In a real app, you would add a filter like eq(user_id, current_user_id)
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