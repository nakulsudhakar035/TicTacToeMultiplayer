package com.nakuls.tictactoe.stratergy

/**
 * Minimax algorithm for the computer player.
 * Assumes the human plays 'x' and the computer plays 'o'.
 * Returns the board index of the best move for 'o'.
 */
object MiniMaxStrategy {

    fun getBestMove(board: CharArray, length: Int): Int {
        var bestScore = Int.MIN_VALUE
        var bestMove = -1
        for (i in board.indices) {
            if (board[i] == ' ') {
                board[i] = 'o'
                val score = minimax(board, length, depth = 0, isMaximizing = false)
                board[i] = ' '
                if (score > bestScore) {
                    bestScore = score
                    bestMove = i
                }
            }
        }
        return bestMove
    }

    private fun minimax(board: CharArray, length: Int, depth: Int, isMaximizing: Boolean): Int {
        val winner = checkWinner(board, length)
        if (winner == 'o') return 10 - depth   // computer wins sooner = better
        if (winner == 'x') return depth - 10   // human wins sooner = worse
        if (board.none { it == ' ' }) return 0 // draw

        return if (isMaximizing) {
            var best = Int.MIN_VALUE
            for (i in board.indices) {
                if (board[i] == ' ') {
                    board[i] = 'o'
                    best = maxOf(best, minimax(board, length, depth + 1, false))
                    board[i] = ' '
                }
            }
            best
        } else {
            var best = Int.MAX_VALUE
            for (i in board.indices) {
                if (board[i] == ' ') {
                    board[i] = 'x'
                    best = minOf(best, minimax(board, length, depth + 1, true))
                    board[i] = ' '
                }
            }
            best
        }
    }

    private fun checkWinner(board: CharArray, length: Int): Char? {
        // Rows
        for (row in 0 until length) {
            val start = row * length
            val sym = board[start]
            if (sym != ' ' && (1 until length).all { board[start + it] == sym }) return sym
        }
        // Columns
        for (col in 0 until length) {
            val sym = board[col]
            if (sym != ' ' && (1 until length).all { board[col + it * length] == sym }) return sym
        }
        // Main diagonal (top-left → bottom-right)
        val mainSym = board[0]
        if (mainSym != ' ' && (1 until length).all { board[it * (length + 1)] == mainSym }) return mainSym
        // Anti-diagonal (top-right → bottom-left)
        val antiSym = board[length - 1]
        if (antiSym != ' ' && (1 until length).all { board[(it + 1) * (length - 1)] == antiSym }) return antiSym

        return null
    }
}
