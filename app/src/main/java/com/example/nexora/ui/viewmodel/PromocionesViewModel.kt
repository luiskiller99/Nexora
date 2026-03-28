package com.example.nexora.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class ClientePromocion(
    val id: Int,
    val nombre: String,
    val ubicacion: String
)

data class ProductoPromocion(
    val id: Int,
    val nombre: String,
    val precio: Double
)

data class PromocionesUiState(
    val clientes: List<ClientePromocion> = emptyList(),
    val productos: List<ProductoPromocion> = emptyList(),
    val clientesSeleccionados: Set<Int> = emptySet(),
    val productosSeleccionados: List<ProductoPromocion> = emptyList(),
    val imagenGenerada: Boolean = false
)

class PromocionesViewModel : ViewModel() {
    var uiState by mutableStateOf(
        PromocionesUiState(
            clientes = listOf(
                ClientePromocion(id = 1, nombre = "Juan Pérez", ubicacion = "San José"),
                ClientePromocion(id = 2, nombre = "María López", ubicacion = "Heredia"),
                ClientePromocion(id = 3, nombre = "Carlos Vega", ubicacion = "Alajuela")
            ),
            productos = listOf(
                ProductoPromocion(id = 1, nombre = "Arroz Premium 1kg", precio = 2.90),
                ProductoPromocion(id = 2, nombre = "Aceite Vegetal 900ml", precio = 4.25),
                ProductoPromocion(id = 3, nombre = "Frijoles Negros 800g", precio = 3.10),
                ProductoPromocion(id = 4, nombre = "Leche Entera 1L", precio = 1.85)
            )
        )
    )
        private set

    fun toggleCliente(clienteId: Int) {
        val seleccionados = uiState.clientesSeleccionados.toMutableSet()
        if (!seleccionados.add(clienteId)) {
            seleccionados.remove(clienteId)
        }
        uiState = uiState.copy(clientesSeleccionados = seleccionados)
    }

    fun agregarProducto(producto: ProductoPromocion) {
        if (uiState.productosSeleccionados.any { it.id == producto.id }) return
        uiState = uiState.copy(productosSeleccionados = uiState.productosSeleccionados + producto)
    }

    fun quitarProducto(productoId: Int) {
        uiState = uiState.copy(
            productosSeleccionados = uiState.productosSeleccionados.filterNot { it.id == productoId }
        )
    }

    fun marcarImagenGenerada() {
        uiState = uiState.copy(imagenGenerada = true)
    }
}
