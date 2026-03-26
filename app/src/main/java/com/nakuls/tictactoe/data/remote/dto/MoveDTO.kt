package com.nakuls.tictactoe.data.remote.dto

import com.nakuls.tictactoe.domain.model.GamePlayer
import com.nakuls.tictactoe.domain.model.Move
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoveDTO(
    @SerialName("index")
    val index: Int,
    @SerialName("game_player_id")
    val playerID: Int,
    @SerialName("game_id")
    val gameID: Int
)
fun MoveDTO.toMove(): Move {
    return Move(
        id = 0,
        player = null,
        game = null,
        index = this.index
    )
}