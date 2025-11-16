package com.nakuls.tictactoe.data.repository

import com.nakuls.tictactoe.data.remote.GameAPI
import com.nakuls.tictactoe.data.remote.GameRemoteDataSourceImpl
import com.nakuls.tictactoe.data.remote.UserProfileAPI
import com.nakuls.tictactoe.data.remote.dto.GameCreationDTO
import com.nakuls.tictactoe.domain.model.Game
import com.nakuls.tictactoe.domain.repository.GameRepository
import io.ktor.client.HttpClient
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

}