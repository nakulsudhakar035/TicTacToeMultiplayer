package com.nakuls.tictactoe.data.remote.dto

import com.nakuls.tictactoe.domain.model.Game
import com.nakuls.tictactoe.domain.model.GamePlayer
import com.nakuls.tictactoe.domain.model.GameStatus
import com.nakuls.tictactoe.stratergy.RowColumnDiagonalStratergy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GamePlayerDTO(
    @SerialName("id")
    val gamePlayerID: Int,
    @SerialName("game_id")
    val gameID: Int,
    @SerialName("player_id")
    val playerID: Int
)

fun GamePlayerDTO.toGamePlayer(): GamePlayer {
    return GamePlayer(
        gamePlayerID = this.gamePlayerID,
        gameID = this.gameID,
        playerID = this.playerID
    )
}
