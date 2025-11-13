package com.nakuls.tictactoe.data.local

import com.nakuls.tictactoe.data.local.entity.UserProfile

interface UserProfileLocal {

    // A function to create the initial user profile locally
    suspend fun createProfile(userProfile: UserProfile): Boolean

    // A function to fetch the user's name from the local
    suspend fun fetchProfileName(): String?
}