package com.example.nexora.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexora.data.model.ProductModel
import com.example.nexora.data.repository.ProductRepository
import kotlinx.coroutines.launch

data class ProductUiState(
    val products: List<ProductModel> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null
)

class ProductViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    var uiState by mutableStateOf(ProductUiState(isLoading = true))
        private set

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, message = null)
            runCatching { repository.getProducts() }
                .onSuccess { products ->
                    uiState = uiState.copy(products = products, isLoading = false)
                }
                .onFailure { error ->
                    uiState = uiState.copy(isLoading = false, message = error.message ?: "Error cargando productos")
                }
        }
    }

    fun createProduct(context: Context, nombre: String, precioText: String, imageUri: Uri?) {
        val precio = precioText.toDoubleOrNull()
        when {
            nombre.isBlank() -> {
                uiState = uiState.copy(message = "El nombre es obligatorio")
                return
            }

            precio == null || precio < 0 -> {
                uiState = uiState.copy(message = "Precio inválido")
                return
            }

            imageUri == null -> {
                uiState = uiState.copy(message = "Selecciona una imagen")
                return
            }
        }

        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true, message = null)
            runCatching { repository.createProduct(context, nombre.trim(), precio, imageUri) }
                .onSuccess {
                    uiState = uiState.copy(isSaving = false, message = "Producto creado")
                    loadProducts()
                }
                .onFailure { error ->
                    uiState = uiState.copy(isSaving = false, message = error.message ?: "No se pudo crear")
                }
        }
    }

    fun updateProduct(
        context: Context,
        productId: String,
        nombre: String,
        precioText: String,
        currentImageUrl: String,
        newImageUri: Uri?
    ) {
        val precio = precioText.toDoubleOrNull()
        when {
            nombre.isBlank() -> {
                uiState = uiState.copy(message = "El nombre es obligatorio")
                return
            }

            precio == null || precio < 0 -> {
                uiState = uiState.copy(message = "Precio inválido")
                return
            }
        }

        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true, message = null)
            runCatching {
                repository.updateProduct(
                    context = context,
                    productId = productId,
                    nombre = nombre.trim(),
                    precio = precio,
                    newImageUri = newImageUri,
                    currentImageUrl = currentImageUrl
                )
            }.onSuccess {
                uiState = uiState.copy(isSaving = false, message = "Producto actualizado")
                loadProducts()
            }.onFailure { error ->
                uiState = uiState.copy(isSaving = false, message = error.message ?: "No se pudo actualizar")
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true, message = null)
            runCatching { repository.deleteProduct(productId) }
                .onSuccess {
                    uiState = uiState.copy(isSaving = false, message = "Producto eliminado")
                    loadProducts()
                }
                .onFailure { error ->
                    uiState = uiState.copy(isSaving = false, message = error.message ?: "No se pudo eliminar")
                }
        }
    }

    fun consumeMessage() {
        uiState = uiState.copy(message = null)
    }
}
