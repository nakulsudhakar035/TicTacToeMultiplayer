package com.nakuls.tictactoe.data.remote

import com.nakuls.tictactoe.data.remote.dto.GameCreationDTO
import com.nakuls.tictactoe.domain.model.Game
import kotlinx.coroutines.flow.Flow

interface GameAPI {

    suspend fun createGame(gameCreationDTO: GameCreationDTO): Boolean

    suspend fun fetchJoinableGames(): Flow<List<Game>>
}