package com.example.nexora.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.nexora.ui.viewmodel.LoginViewModel

private enum class NexoraDestination {
    Login,
    Dashboard
}

@Composable
fun NexoraApp(
    loginViewModel: LoginViewModel,
    modifier: Modifier = Modifier
) {
    var currentDestination by rememberSaveable {
        mutableStateOf(NexoraDestination.Login)
    }

    when (currentDestination) {
        NexoraDestination.Login -> {
            LoginScreen(
                uiState = loginViewModel.uiState,
                onEmailChange = loginViewModel::onEmailChange,
                onPasswordChange = loginViewModel::onPasswordChange,
                onLoginClick = {
                    loginViewModel.onLoginClick()
                    currentDestination = NexoraDestination.Dashboard
                },
                modifier = modifier
            )
        }

        NexoraDestination.Dashboard -> {
            DashboardScreen(
                onClientesClick = {},
                onProductosClick = {},
                onPedidosClick = {},
                onCreditoClick = {},
                modifier = modifier
            )
        }
    }
}
