package com.nakuls.tictactoe

import com.nakuls.tictactoe.domain.model.Game
import com.nakuls.tictactoe.domain.model.GameStatus
import com.nakuls.tictactoe.domain.model.Player
import com.nakuls.tictactoe.domain.model.PlayerStatus

fun main() {

    val tictactoe = TicTacToe()
    tictactoe.startGame()
}

class TicTacToe {

    fun startGame(){
        val playerOne = Player(
            name = "One",
            status = PlayerStatus.Active,
            symbol = 'x',
            id = null,
            score = 0
        )
        val playerTwo = Player(
            name = "Two",
            status = PlayerStatus.Active,
            symbol = 'o',
            id = null,
            score = 0
        )
        val game = Game(
            id = 1,
            status = GameStatus.UnFilled,
            length = 3,
            owner = playerOne,
            winner = null,
            moveCount = 0,
            players = listOf(playerOne, playerTwo),
            charArray = CharArray(9),
            winDetectionStratergy = RowColumnDiagonalStratergy()
        )
        game.displayMoves()
        game.requestMove()
//        var move = Move(null, playerOne, game, 0)
//        game.makeMove(move)
//        move = Move(null, playerTwo, game, 1)
//        game.makeMove(move)
//        move = Move(null, playerOne, game, 8)
//        game.makeMove(move)
//        move = Move(null, playerTwo, game, 3)
//        game.makeMove(move)
//        move = Move(null, playerOne, game, 4)
//        game.makeMove(move)
//        move = Move(null, playerTwo, game, 1)
//        game.makeMove(move)
//        move = Move(null, playerOne, game, 5)
//        game.makeMove(move)

    }
}