package com.nakuls.tictactoe.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProfileCreationDTO(
    val id: Int? = null,
    val name: String,
    val score: Int,
    val status: Int,
    val email: String
)
