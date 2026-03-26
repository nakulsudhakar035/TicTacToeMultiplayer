package com.nakuls.tictactoe.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


data class GamePlayer(
    val gamePlayerID: Int,
    val gameID: Int,
    val playerID: Int
)
