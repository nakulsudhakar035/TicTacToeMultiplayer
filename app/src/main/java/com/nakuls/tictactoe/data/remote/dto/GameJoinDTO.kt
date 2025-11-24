package com.nakuls.tictactoe.data.remote.dto

import com.nakuls.tictactoe.domain.model.Game
import com.nakuls.tictactoe.domain.model.GameStatus
import com.nakuls.tictactoe.stratergy.RowColumnDiagonalStratergy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameJoinDTO(
    @SerialName("created_at")
    val createdAt: String?,
    @SerialName("edited_at")
    val editedAt: String?,
    @SerialName("id")
    val gameID: Int,
    val status: Int,
    val length: Int
)

fun GameJoinDTO.toGame(): Game {
    return Game(
        id = this.gameID,
        owner = "",
        status = GameStatus.entries[this.status],
        length = this.length,
        winner = null,
        moveCount = 0,
        players = listOf(),
        charArray = CharArray(9),
        winDetectionStratergy = RowColumnDiagonalStratergy(),
    )
}
