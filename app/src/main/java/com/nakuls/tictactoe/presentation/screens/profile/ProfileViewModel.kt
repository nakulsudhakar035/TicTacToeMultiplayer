package com.nakuls.tictactoe.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakuls.tictactoe.domain.repository.UserRepository
import com.nakuls.tictactoe.domain.utils.EmailAddressValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Saving : ProfileUiState()
    data class Success(val name: String) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

data class ProfileScreenState(
    val username: String = "",
    val email: String = "",
    val isSaveEnabled: Boolean = false,
    val uiState: ProfileUiState = ProfileUiState.Idle
)

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow(ProfileScreenState())
    val screenState: StateFlow<ProfileScreenState> = _screenState.asStateFlow()

    fun onUsernameChange(newUsername: String) {
        if (newUsername.length <= 15) {
            _screenState.update { state ->
                val updated = state.copy(username = newUsername.replace("\n", ""))
                updated.copy(isSaveEnabled = computeIsSaveEnabled(updated))
            }
        }
    }

    fun onEmailChange(newEmail: String) {
        if (newEmail.length <= 25) {
            _screenState.update { state ->
                val updated = state.copy(email = newEmail.replace("\n", ""))
                updated.copy(isSaveEnabled = computeIsSaveEnabled(updated))
            }
        }
    }

    private fun computeIsSaveEnabled(state: ProfileScreenState): Boolean {
        return state.username.trim().length >= 3
            && EmailAddressValidator.isValidEmail(state.email.trim())
            && state.uiState != ProfileUiState.Saving
    }

    fun saveProfile() {
        val username = _screenState.value.username.trim()
        val email = _screenState.value.email.trim()

        if (username.length < 3) {
            _screenState.update { it.copy(uiState = ProfileUiState.Error("Username must be at least 3 characters.")) }
            return
        }
        if (!EmailAddressValidator.isValidEmail(email)) {
            _screenState.update { it.copy(uiState = ProfileUiState.Error("Please enter a valid Email address.")) }
            return
        }

        _screenState.update { it.copy(uiState = ProfileUiState.Saving, isSaveEnabled = false) }

        viewModelScope.launch {
            try {
                if (userRepository.saveUsername(username, email = email)) {
                    _screenState.update { it.copy(uiState = ProfileUiState.Success(username)) }
                } else {
                    _screenState.update {
                        it.copy(
                            uiState = ProfileUiState.Error("Failed to save profile"),
                            isSaveEnabled = computeIsSaveEnabled(it)
                        )
                    }
                }
            } catch (e: Exception) {
                _screenState.update {
                    it.copy(
                        uiState = ProfileUiState.Error("Failed to save profile: ${e.localizedMessage}"),
                        isSaveEnabled = computeIsSaveEnabled(it)
                    )
                }
            }
        }
    }
}
