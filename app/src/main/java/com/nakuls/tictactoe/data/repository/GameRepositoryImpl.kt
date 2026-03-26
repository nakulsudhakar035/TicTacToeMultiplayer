package com.nakuls.tictactoe.data.repository

import com.nakuls.tictactoe.data.local.GameLocal
import com.nakuls.tictactoe.data.remote.GameAPI
import com.nakuls.tictactoe.data.remote.dto.GameCreationDTO
import com.nakuls.tictactoe.data.remote.dto.GameJoiningPlayerDTO
import com.nakuls.tictactoe.data.remote.dto.MoveDTO
import com.nakuls.tictactoe.domain.model.Game
import com.nakuls.tictactoe.domain.model.GamePlayer
import com.nakuls.tictactoe.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class GameRepositoryImpl(
    private val localSource: GameLocal,
    private val remoteSource: GameAPI,
): GameRepository {

    override suspend fun getJoinableGamesStream(createrIDToExclude: Int): Flow<List<Game>> {
        return remoteSource.fetchJoinableGames(createrIDToExclude)
    }

    override suspend fun createGame(createdBy: Int, length: Int, status: Int): GamePlayer? {
        val gamePlayer = remoteSource.createGame(GameCreationDTO(createdBy,length,status))
        if(gamePlayer == null){
            return null
        }
        localSource.createGamePlayer(gamePlayer.gamePlayerID)
        localSource.createGame(gamePlayer.gameID)
        localSource.setIsCreater(true)
        return gamePlayer
    }

    override suspend fun joinGame(gameId: Int, userID: Int): Int? {
        val gamePlayer = remoteSource.joinGame(GameJoiningPlayerDTO(gameId, userID))
        if(gamePlayer == null){
            return null
        }
        localSource.createGamePlayer(gamePlayer.gamePlayerID)
        localSource.createGame(gamePlayer.gameID)
        localSource.setIsCreater(false)
        return gamePlayer.gamePlayerID
    }

    override suspend fun startListeningForGameJoins(gameId: Int): Flow<Unit> {
        return remoteSource.startListeningForGameJoins(gameId)
    }

    override suspend fun startListeningForMovesInGame(): Flow<MoveDTO> {
        val gameID = localSource.fetchGameID()
        if (gameID != null) {
            return remoteSource.startListeningForMovesInGame(gameID)
        } else {
            return emptyFlow()
        }
    }

    override suspend fun makeMove(index: Int, playerID: Int): Boolean {
        val gameID = localSource.fetchGameID()
        if (gameID != null) {
            return remoteSource.makeMove(MoveDTO(index, playerID, gameID))
        } else {
            return false
        }
    }

}