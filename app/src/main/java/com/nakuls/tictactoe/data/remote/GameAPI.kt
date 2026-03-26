package com.nakuls.tictactoe.data.remote

import com.nakuls.tictactoe.data.remote.dto.GameCreationDTO
import com.nakuls.tictactoe.data.remote.dto.GameJoiningPlayerDTO
import com.nakuls.tictactoe.data.remote.dto.GamePlayerDTO
import com.nakuls.tictactoe.data.remote.dto.MoveDTO
import com.nakuls.tictactoe.domain.model.Game
import com.nakuls.tictactoe.domain.model.GamePlayer
import kotlinx.coroutines.flow.Flow

interface GameAPI {

    suspend fun createGame(gameCreationDTO: GameCreationDTO): GamePlayer?

    suspend fun joinGame(gameJoiningPlayerDTO: GameJoiningPlayerDTO): GamePlayerDTO?

    suspend fun fetchJoinableGames(currentUserId: Int): Flow<List<Game>>

    suspend fun startListeningForGameJoins(gameId: Int): Flow<Unit>

    suspend fun makeMove(moveDTO: MoveDTO): Boolean

    suspend fun startListeningForMovesInGame(gameId: Int): Flow<MoveDTO>
}