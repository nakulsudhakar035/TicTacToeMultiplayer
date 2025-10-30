package com.nakuls.tictactoe.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakuls.tictactoe.domain.repository.UserRepository
import com.nakuls.tictactoe.presentation.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SplashState {
    object Loading : SplashState()
    data class Navigate(val destination: Screen) : SplashState()
}

class SplashViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state

    init {
        determineNextDestination()
    }

    private fun determineNextDestination() {
        viewModelScope.launch {
            // 1. Minimum display time for the user (optional, but good practice)
            delay(1500L) // Wait for 1.5 seconds

            // 2. Check DataStore for profile
            val username = userRepository.getUsername()

            // 3. Determine and set the final navigation destination
            val destination = if (username.isNullOrBlank()) {
                Screen.Profile // No user found -> Go to Profile Creation
            } else {
                Screen.Home // User found -> Go to Home/Lobby Screen
            }

            _state.value = SplashState.Navigate(destination)
        }
    }
}