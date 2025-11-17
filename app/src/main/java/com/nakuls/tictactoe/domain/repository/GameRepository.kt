package com.nakuls.tictactoe.domain.repository

import com.nakuls.tictactoe.domain.model.Game
import kotlinx.coroutines.flow.Flow

interface GameRepository {

    suspend fun getJoinableGamesStream(createrIDToExclude: Int): Flow<List<Game>>

    suspend fun createGame(createdBy: Int, length: Int, status: Int): Boolean

    suspend fun joinGame(gameId: Int, userID: Int): Boolean
}