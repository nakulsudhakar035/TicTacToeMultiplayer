package com.nakuls.tictactoe.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GameCreationDTO(
    val createdBy: String,
    val length: Int,
    val status: Int
)