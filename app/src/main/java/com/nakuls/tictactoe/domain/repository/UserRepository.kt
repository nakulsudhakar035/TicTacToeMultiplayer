package com.nakuls.tictactoe.domain.repository

interface UserRepository {
    suspend fun getUsername(): String?
    suspend fun saveUsername(name: String)
}