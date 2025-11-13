package com.nakuls.tictactoe.domain.repository

interface UserRepository {

    suspend fun getRegisteredUsername(): String?
    suspend fun saveUsername(name: String, email: String) : Boolean
}