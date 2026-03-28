package com.example.nexora.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.nexora.ui.viewmodel.LoginViewModel
import com.example.nexora.ui.viewmodel.PromocionesViewModel

private enum class NexoraDestination {
    Login,
    Dashboard,
    Promociones
}

@Composable
fun NexoraApp(
    loginViewModel: LoginViewModel,
    promocionesViewModel: PromocionesViewModel,
    modifier: Modifier = Modifier
) {
    var currentDestination by rememberSaveable {
        mutableStateOf(NexoraDestination.Login)
    }
    val context = LocalContext.current

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
                onPromocionesClick = { currentDestination = NexoraDestination.Promociones },
                modifier = modifier
            )
        }

        NexoraDestination.Promociones -> {
            PromocionesScreen(
                uiState = promocionesViewModel.uiState,
                onBackClick = { currentDestination = NexoraDestination.Dashboard },
                onToggleCliente = promocionesViewModel::toggleCliente,
                onToggleProducto = promocionesViewModel::toggleProducto,
                onActualizarPrecioPromo = promocionesViewModel::actualizarPrecioPromo,
                onActualizarFondo = promocionesViewModel::actualizarFondo,
                onGenerarImagen = { promocionesViewModel.generarImagen(context) },
                onEnviarPromocion = { promocionesViewModel.compartirPromocion(context) },
                modifier = modifier
            )
        }
    }
}
