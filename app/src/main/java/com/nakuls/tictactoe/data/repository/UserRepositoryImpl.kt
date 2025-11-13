package com.nakuls.tictactoe.data.repository

import com.nakuls.tictactoe.data.local.UserProfileLocal
import com.nakuls.tictactoe.data.local.entity.UserProfile
import com.nakuls.tictactoe.data.remote.UserProfileAPI
import com.nakuls.tictactoe.data.remote.dto.ProfileCreationDTO
import com.nakuls.tictactoe.domain.repository.UserRepository

class UserRepositoryImpl(
    private val localSource: UserProfileLocal,
    private val remoteSource: UserProfileAPI
): UserRepository {

    override suspend fun getRegisteredUsername(): String? {
        return localSource.fetchProfileName()
    }

    override suspend fun saveUsername(
        name: String,
        email:String
    ): Boolean {
        val profileCreationDto = ProfileCreationDTO(
            name = name,
            score = 0,
            status = 0,
            email = email
        )
        if (
            remoteSource.createProfile(
                profileCreationDTO = profileCreationDto
            )
        ){
            localSource.createProfile(
                UserProfile(
                    name,
                    email
                )
            )
            return true;
        } else {
            return false;
        }
    }

}