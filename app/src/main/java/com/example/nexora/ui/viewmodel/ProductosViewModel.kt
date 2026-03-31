package com.example.nexora.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.UUID

data class Producto(
    val id: String,
    val nombre: String,
    val precio: Double,
    val imagenUri: String
)

class ProductosViewModel : ViewModel() {
    val listaProductos = mutableStateListOf<Producto>()

    var imagenSeleccionadaUri by mutableStateOf<Uri?>(null)
        private set

    fun seleccionarImagen(uri: Uri?) {
        imagenSeleccionadaUri = uri
    }

    fun agregarProducto(nombre: String, precio: String) {
        val precioDouble = precio.toDoubleOrNull() ?: return
        if (nombre.isBlank() || precioDouble < 0.0 || imagenSeleccionadaUri == null) return

        listaProductos.add(
            Producto(
                id = UUID.randomUUID().toString(),
                nombre = nombre.trim(),
                precio = precioDouble,
                imagenUri = imagenSeleccionadaUri.toString()
            )
        )
        imagenSeleccionadaUri = null
    }
}
