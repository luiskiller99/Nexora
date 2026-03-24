package com.example.nexora.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false
)

class LoginViewModel : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value)
    }

    fun onLoginClick() {
        // TODO: integrate authentication against Supabase session endpoint.
        uiState = uiState.copy(isLoading = true)
    }
}
