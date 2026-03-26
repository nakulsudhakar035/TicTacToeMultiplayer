package com.nakuls.tictactoe.data.local

interface GameLocal {

    suspend fun createGame(gameID: Int): Boolean
    // A function to create the initial user profile locally
    suspend fun createGamePlayer(gamePlayerID: Int): Boolean
    suspend fun setIsCreater(isCreater: Boolean): Boolean
    suspend fun fetchGameID(): Int?
    // A function to fetch the user's name from the local
    suspend fun fetchGamePlayerID(): Int?
    suspend fun fetchIsCreater(): Boolean?
}