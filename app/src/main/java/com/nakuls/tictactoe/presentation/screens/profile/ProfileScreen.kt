package com.nakuls.tictactoe.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nakuls.tictactoe.domain.utils.EmailAddressValidator
import com.nakuls.tictactoe.presentation.navigation.Screen
import com.nakuls.tictactoe.presentation.ui.theme.TicTacToeTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val usernameInput by viewModel.usernameInput.collectAsStateWithLifecycle()
    val useremailInput by viewModel.useremailInput.collectAsStateWithLifecycle()

    val minLength = 3
    val isNameValid = usernameInput.length >= minLength
    val isEmailValid = EmailAddressValidator.isValidEmail(useremailInput)
    val isButtonEnabled = isNameValid && uiState != ProfileUiState.Saving && isEmailValid

    val keyboardController = LocalSoftwareKeyboardController.current

    // --- Side Effect: Navigation and SnackBar ---
    LaunchedEffect(uiState) {
        when (uiState) {
            is ProfileUiState.Success -> {
                // Profile saved successfully, navigate to Home

                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Profile.route) { inclusive = true }
                }
            }
            is ProfileUiState.Error -> {
                // Show error message (e.g., using a Snackbar or Toast)
                // For simplicity, we'll just print it for now:
                println("Error: ${(uiState as ProfileUiState.Error).message}")
            }
            else -> {}
        }
    }

    TicTacToeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Setup Your Profile") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.5f))

                // --- Branding Icon ---
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = "Game Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(96.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Input Field ---
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = viewModel::onUsernameChange, // Pass control to ViewModel
                    label = { Text("Username") },
                    placeholder = { Text("e.g., winner") },
                    leadingIcon = { Icon(Icons.Filled.AccountCircle, "Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = usernameInput.isNotEmpty() && !isNameValid,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.saveProfile() })
                )

                // --- Error Message/Status ---
                if (usernameInput.isNotEmpty() && !isNameValid) {
                    Text(
                        text = "Username must be at least $minLength characters",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = useremailInput,
                    onValueChange = viewModel::onEmailChange, // Pass control to ViewModel
                    label = { Text("Email address") },
                    placeholder = { Text("e.g., winner@gmail.com") },
                    leadingIcon = { Icon(Icons.Filled.Email, "Email address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = useremailInput.isNotEmpty() && !isEmailValid,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.saveProfile() })
                )
                if (uiState is ProfileUiState.Saving) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- Save Button ---
                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.saveProfile()
                    },
                    enabled = isButtonEnabled,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(
                        text = if (uiState is ProfileUiState.Saving) "Saving..." else "Save Profile and Continue",
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

// --- Preview Composable ---

@Preview(showBackground = true)
@Composable
private fun ProfileCreationScreenPreview() {
    TicTacToeTheme {
        ProfileScreen(navController = rememberNavController())
    }
}
