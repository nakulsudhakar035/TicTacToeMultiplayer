package com.nakuls.tictactoe.domain.model

import com.nakuls.tictactoe.WinDetectionStratergy

class Game(
    var id: Int?,
    var status: GameStatus,
    var length: Int,
    var owner: Player,
    var winner: Player?,
    var moveCount: Int,
    val players: List<Player>?,
    var charArray: CharArray,
    val winDetectionStratergy: WinDetectionStratergy
){
    fun createGame(){
       charArray = CharArray(length*length)
    }

    fun fetchJoinnableGames(){

    }

    fun joinGame(){

    }

    fun makeMove(move: Move){

        charArray[move.index] = move.player.symbol
        displayMoves()
        if (moveCount > length && winDetectionStratergy.checkIfMatchPoint(
            move.player,
            move,
            this)) {
            println("${move.player.name} wins")
            return
        }
        this.moveCount++
        requestMove()
    }

    fun setStatus(){

    }

    fun requestMove(){
        if(players != null){
            val playerIndex = moveCount%players.size

            val player = players.get(playerIndex)
            println("${player.name}'s move. Please enter the index")

            val index = readln().toInt()
            val move = Move(null, player, this, index)
            makeMove(move)

        }
    }

    fun displayMoves(){
        println()
        var index = 0;
        for (char in charArray){
            print(char)
            if((index+1)%length==0){
                println()
            } else {
                print('|')
            }
            index++
        }
    }
}