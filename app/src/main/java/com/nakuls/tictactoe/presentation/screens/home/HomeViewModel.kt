package com.nakuls.tictactoe.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakuls.tictactoe.domain.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.nakuls.tictactoe.domain.model.Game

class HomeViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _joinableGames = MutableStateFlow<List<Game>>(emptyList())
    val joinableGames: StateFlow<List<Game>> = _joinableGames

    init {
        getGames()
    }

    fun getGames() {
        viewModelScope.launch {
            gameRepository.getJoinableGamesStream().collect {
                _joinableGames.value = it
            }
        }
    }
}