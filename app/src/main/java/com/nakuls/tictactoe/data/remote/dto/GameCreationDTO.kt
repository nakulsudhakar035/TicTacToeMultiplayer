package com.nakuls.tictactoe.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameCreationDTO(
    @SerialName("creator_id")
    val createdBy: Int,
    @SerialName("game_length")
    val length: Int,
    @SerialName("game_status")
    val status: Int
)