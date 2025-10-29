package com.nakuls.tictactoe

import com.nakuls.tictactoe.domain.model.Game
import com.nakuls.tictactoe.domain.model.Move
import com.nakuls.tictactoe.domain.model.Player

class RowColumnDiagonalStratergy: WinDetectionStratergy {

    override fun checkIfMatchPoint(
        player: Player,
        move: Move,
        game: Game
    ): Boolean {
        return checkIfRowWiseMatchPoint(
            player,
            move,
            game
        )
       || checkIfColumnWiseMatchPoint(
            player,
            move,
            game
       )
       || checkIfDiagonallyMatchPoint(
            player,
            move,
            game
       )

    }

    fun checkIfRowWiseMatchPoint(
        player: Player,
        move: Move,
        game: Game
    ): Boolean {

        val chars = game.charArray

        var startIndex = move.index
        while(startIndex%game.length!=0){
            startIndex = startIndex - 1
        }
        var symbolCount = 0
        for (i in startIndex until startIndex+game.length) {
            if(chars[i]==player.symbol){
                symbolCount++
            }
        }
        if(symbolCount == game.length){
            println("By Row logic")
            return true
        }
        return false;
    }

    fun checkIfColumnWiseMatchPoint(
        player: Player,
        move: Move,
        game: Game
    ): Boolean {

        val chars = game.charArray
        val size = game.length*game.length

        var startIndex = move.index
        while(startIndex > game.length){
            startIndex = startIndex - game.length
        }
        var symbolCount = 0
        for (i in startIndex until size step game.length) {
            if(chars[i]==player.symbol){
                symbolCount++
            }
        }
        if(symbolCount == game.length){
            println("By Column logic")
            return true
        }
        return false;
    }

    fun checkIfDiagonallyMatchPoint(
        player: Player,
        move: Move,
        game: Game
    ): Boolean {

        val index = move.index
        val chars = game.charArray
        val size = game.length*game.length
        if(index%(game.length-1)==0 && index != size-1){
            var symbolCount = 0;
            for(i in game.length-1 until size step game.length-1){
                if(chars[i]==player.symbol) {
                    symbolCount++
                }
            }
            if(symbolCount==game.length){
                println("By diagonal logic")
                return true
            }
        }
        if(index%(game.length+1)==0){
            var symbolCount = 0;
            for(i in 0 until size step game.length+1){
                if(chars[i]==player.symbol) {
                    symbolCount++
                }
            }
            if(symbolCount==game.length){
                println("By diagonal logic")
                return true
            }
        }

        return false
    }

}