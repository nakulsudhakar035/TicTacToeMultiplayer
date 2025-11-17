package com.nakuls.tictactoe.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GamePlayerDTO(
    @SerialName("game_id")
    val gameID: Int,
    @SerialName("player_id")
    val playerID: Int
)
