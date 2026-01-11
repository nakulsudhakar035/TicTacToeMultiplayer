package com.nakuls.tictactoe.data.remote

import com.nakuls.tictactoe.data.remote.dto.GameCreationDTO
import com.nakuls.tictactoe.data.remote.dto.GamePlayerDTO
import com.nakuls.tictactoe.data.remote.dto.MoveDTO
import com.nakuls.tictactoe.domain.model.Game
import kotlinx.coroutines.flow.Flow

interface GameAPI {

    suspend fun createGame(gameCreationDTO: GameCreationDTO): Game?

    suspend fun joinGame(gamePlayerDTO: GamePlayerDTO): Boolean

    suspend fun fetchJoinableGames(currentUserId: Int): Flow<List<Game>>

    suspend fun startListeningForGameJoins(gameId: Int): Flow<Unit>

    suspend fun makeMove(moveDTO: MoveDTO): Boolean

    suspend fun startListeningForMovesInGame(gamePlayerId: Int): Flow<Unit>
}