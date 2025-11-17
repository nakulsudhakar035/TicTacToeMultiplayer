package com.nakuls.tictactoe.data.repository

import com.nakuls.tictactoe.data.remote.GameAPI
import com.nakuls.tictactoe.data.remote.dto.GameCreationDTO
import com.nakuls.tictactoe.data.remote.dto.GamePlayerDTO
import com.nakuls.tictactoe.domain.model.Game
import com.nakuls.tictactoe.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow

class GameRepositoryImpl(
    private val remoteSource: GameAPI,
): GameRepository {

    override suspend fun getJoinableGamesStream(createrIDToExclude: Int): Flow<List<Game>> {
        return remoteSource.fetchJoinableGames(createrIDToExclude)
    }

    override suspend fun createGame(createdBy: Int, length: Int, status: Int): Boolean {
        return remoteSource.createGame(GameCreationDTO(createdBy,length,status))

    }

    override suspend fun joinGame(gameId: Int, userID: Int): Boolean {
        return remoteSource.joinGame(GamePlayerDTO(gameId,userID))
    }

}