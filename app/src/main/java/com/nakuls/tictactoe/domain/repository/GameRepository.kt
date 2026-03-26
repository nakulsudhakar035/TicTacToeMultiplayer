package com.nakuls.tictactoe.domain.repository

import com.nakuls.tictactoe.data.remote.dto.MoveDTO
import com.nakuls.tictactoe.domain.model.Game
import com.nakuls.tictactoe.domain.model.GamePlayer
import kotlinx.coroutines.flow.Flow

interface GameRepository {

    suspend fun getJoinableGamesStream(createrIDToExclude: Int): Flow<List<Game>>

    suspend fun createGame(createdBy: Int, length: Int, status: Int): GamePlayer?

    suspend fun joinGame(gameId: Int, userID: Int): Int?

    suspend fun startListeningForGameJoins(gameId: Int): Flow<Unit>

    suspend fun startListeningForMovesInGame(): Flow<MoveDTO>

    suspend fun makeMove(index: Int, playerID: Int): Boolean
}