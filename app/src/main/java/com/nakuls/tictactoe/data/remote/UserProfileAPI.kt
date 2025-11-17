package com.nakuls.tictactoe.data.remote

import com.nakuls.tictactoe.data.remote.dto.ProfileCreationDTO

interface UserProfileAPI {

    // A function to create the initial user profile on the server
    suspend fun createProfile(profileCreationDTO: ProfileCreationDTO): ProfileCreationDTO?

}