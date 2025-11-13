package com.nakuls.tictactoe.domain.repository

import com.nakuls.tictactoe.domain.model.Game
import kotlinx.coroutines.flow.Flow

interface GameRepository {

    suspend fun getJoinableGamesStream(): Flow<List<Game>>

    fun createGame(email: String): Boolean
}