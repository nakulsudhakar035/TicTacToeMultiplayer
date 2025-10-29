package com.nakuls.tictactoe

import com.nakuls.tictactoe.domain.model.Game
import com.nakuls.tictactoe.domain.model.Move
import com.nakuls.tictactoe.domain.model.Player

interface WinDetectionStratergy {

    fun checkIfMatchPoint(player: Player, move: Move, game: Game): Boolean

}