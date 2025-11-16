package com.nakuls.tictactoe.data.remote.dto

import com.nakuls.tictactoe.stratergy.RowColumnDiagonalStratergy
import com.nakuls.tictactoe.domain.model.Game
import com.nakuls.tictactoe.domain.model.GameStatus
import com.nakuls.tictactoe.domain.model.Player
import com.nakuls.tictactoe.domain.model.PlayerStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameDTO(
    @SerialName("created_at")
    val createdAt: String?,
    @SerialName("edited_at")
    val editedAt: String?,
    @SerialName("id")
    val gameID: Int,
    val status: Int,
    val length: Int,
    @SerialName("created_by") val player: PlayerInfo
)

@Serializable
data class PlayerInfo(
    @SerialName("name") val name: String
)

fun GameDTO.toGame(): Game {
    return Game(
        id = this.gameID,
        owner = this.player.name,
        status = GameStatus.entries[this.status],
        length = this.length,
        winner = null,
        moveCount = 0,
        players = listOf(
            Player(
                name = "Two",
                status = PlayerStatus.Active,
                symbol = 'x',
                id = null,
                score = 0,
                email = "",
            )
        ),
        charArray = CharArray(9),
        winDetectionStratergy = RowColumnDiagonalStratergy(),
    )
}
