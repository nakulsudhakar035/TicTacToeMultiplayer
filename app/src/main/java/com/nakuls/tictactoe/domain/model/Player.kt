package com.nakuls.tictactoe.domain.model
data class Player
(
    var id:Int?,
    var name: String,
    var status: PlayerStatus,
    var score: Int?,
    var symbol: Char
)