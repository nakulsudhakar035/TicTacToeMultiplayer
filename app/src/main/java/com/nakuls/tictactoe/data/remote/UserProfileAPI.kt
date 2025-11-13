package com.nakuls.tictactoe.data.remote

import com.nakuls.tictactoe.data.remote.dto.ProfileCreationDTO

interface UserProfileAPI {

    // A function to create the initial user profile on the server
    suspend fun createProfile(profileCreationDTO: ProfileCreationDTO): Boolean

    // A function to fetch the user's name from the server
    suspend fun fetchProfileName(): String?

}