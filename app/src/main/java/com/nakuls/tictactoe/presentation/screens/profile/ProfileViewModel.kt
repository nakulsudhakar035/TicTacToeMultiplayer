package com.nakuls.tictactoe.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nakuls.tictactoe.domain.repository.UserRepository
import com.nakuls.tictactoe.domain.utils.EmailAddressValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Idle : ProfileUiState()       // Initial state, ready for input
    object Saving : ProfileUiState()     // Profile is being saved (e.g., show progress bar)
    data class Success(val name: String) : ProfileUiState() // Save complete, contains the name
    data class Error(val message: String) : ProfileUiState() // Failed to save
}
class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState

    // State for the text field input, managed by the ViewModel
    private val _usernameInput = MutableStateFlow("")
    val usernameInput: StateFlow<String> = _usernameInput
    private val _useremailInput = MutableStateFlow("")
    val useremailInput: StateFlow<String> = _useremailInput

    fun onUsernameChange(newUsername: String) {
        // Simple input validation/limiting can happen here
        if (newUsername.length <= 15) {
            _usernameInput.value = newUsername.replace("\n", "")
        }
    }

    fun onEmailChange(newEmail: String) {
        if (newEmail.length <= 25) {
            _useremailInput.value = newEmail.replace("\n", "")
        }
    }

    // 2. Main action function
    fun saveProfile() {
        val username = _usernameInput.value.trim()
        val email = _useremailInput.value.trim()

        // Input Validation (should ideally be handled by a Use Case, but kept here for simplicity)
        if (username.length < 3) {
            _uiState.value = ProfileUiState.Error("Username must be at least 3 characters.")
            return
        }

        if (!EmailAddressValidator.isValidEmail(email)) {
            _uiState.value = ProfileUiState.Error("Please enter a valid Email address.")
            return
        }

        _uiState.value = ProfileUiState.Saving

        viewModelScope.launch {
            try {
                // 3. Call repository function
                if(userRepository.saveUsername(
                        username,
                        email = email
                    )){
                    _uiState.value = ProfileUiState.Success(username)
                } else {
                    _uiState.value = ProfileUiState.Error("Failed to save profile")
                }

            } catch (e: Exception) {
                // Handle potential DataStore or network errors
                _uiState.value = ProfileUiState.Error("Failed to save profile: ${e.localizedMessage}")
            }
        }
    }
}

