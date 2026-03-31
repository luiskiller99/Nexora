package com.example.nexora.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import java.util.UUID

data class Cliente(
    val id: String,
    val nombre: String,
    val telefono: String,
    val ubicacion: String? = null
)

class ClientesViewModel : ViewModel() {
    val listaClientes = mutableStateListOf<Cliente>()

    fun agregarCliente(nombre: String, telefono: String, ubicacion: String?) {
        if (nombre.isBlank() || telefono.isBlank()) return
        listaClientes.add(
            Cliente(
                id = UUID.randomUUID().toString(),
                nombre = nombre.trim(),
                telefono = telefono.trim(),
                ubicacion = ubicacion?.trim().takeUnless { it.isNullOrBlank() }
            )
        )
    }

    fun agregarDesdeContacto(nombre: String, telefono: String) {
        if (nombre.isBlank() || telefono.isBlank()) return
        listaClientes.add(
            Cliente(
                id = UUID.randomUUID().toString(),
                nombre = nombre.trim(),
                telefono = telefono.trim()
            )
        )
    }
}
