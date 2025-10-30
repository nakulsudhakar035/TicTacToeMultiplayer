package com.nakuls.tictactoe.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nakuls.tictactoe.presentation.screens.splash.SplashViewModel
import com.nakuls.tictactoe.presentation.ui.theme.TicTacToeTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    // State to hold the user input
    var username by remember { mutableStateOf("") }

    // Validation rules
    val minLength = 3
    val isNameValid = username.length >= minLength
    val isButtonEnabled = isNameValid

    // Keyboard Controller for dismissing the keyboard
    val keyboardController = LocalSoftwareKeyboardController.current

    // Action when the button is pressed (or keyboard 'Done' is hit)
    val saveAction = {
        if (isButtonEnabled) {
            keyboardController?.hide()
            onProfileCreated(username.trim()) // Trim whitespace before saving
        }
    }

    TicTacToeTheme {
        Scaffold(
            // Use the primary container color for a clean top bar accent
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
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Game Icon",
                    tint = MaterialTheme.colorScheme.primary, // Green color
                    modifier = Modifier.size(96.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Title and Description ---
                Text(
                    text = "What should we call you?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "This will be your permanent profile name for online matches.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                // --- Input Field ---
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        // Limit input length and only allow non-whitespace characters if desired
                        if (it.length <= 15) username = it.replace("\n", "")
                    },
                    label = { Text("Username") },
                    placeholder = { Text("e.g., GridMaster42") },
                    leadingIcon = { Icon(Icons.Filled.AccountCircle, "Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = username.isNotEmpty() && !isNameValid,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { saveAction() })
                )

                // --- Error Message ---
                if (username.isNotEmpty() && !isNameValid) {
                    Text(
                        text = "Username must be at least $minLength characters",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- Save Button ---
                Button(
                    onClick = saveAction,
                    enabled = isButtonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Save Profile and Continue", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.weight(1f)) // Push content up
            }
        }
    }
}

// --- Preview Composable ---

@Preview(showBackground = true)
@Composable
private fun ProfileCreationScreenPreview() {
    TicTacToeTheme {
        ProfileScreen(onProfileCreated = {})
    }
}
