package com.nakuls.tictactoe.domain.model

data class Move(
    val id:Int?,
    val player: Player,
    val game: Game,
    val index: Int
)