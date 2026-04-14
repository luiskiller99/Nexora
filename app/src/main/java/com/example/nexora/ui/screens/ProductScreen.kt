package com.example.nexora.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nexora.data.model.ProductModel
import com.example.nexora.ui.viewmodel.ProductUiState
import com.example.nexora.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    viewModel: ProductViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductModel?>(null) }
    var deletingProduct by remember { mutableStateOf<ProductModel?>(null) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF050816), Color(0xFF0B1220)))),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White
            ) { Text("+") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onBackClick) { Text("Volver") }
                Text(
                    text = "Productos",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF60A5FA))
                }
            } else {
                ProductList(
                    uiState = uiState,
                    onEdit = { editingProduct = it },
                    onDelete = { deletingProduct = it }
                )
            }
        }
    }

    if (showCreateDialog) {
        ProductFormDialog(
            title = "Nuevo producto",
            isSaving = uiState.isSaving,
            onDismiss = { showCreateDialog = false },
            onSave = { nombre, precio, uri ->
                viewModel.createProduct(context, nombre, precio, uri)
                showCreateDialog = false
            }
        )
    }

    editingProduct?.let { product ->
        ProductFormDialog(
            title = "Editar producto",
            initialName = product.nombre,
            initialPrice = product.precio.toString(),
            initialImage = product.imagenUrl,
            isSaving = uiState.isSaving,
            onDismiss = { editingProduct = null },
            onSave = { nombre, precio, uri ->
                viewModel.updateProduct(
                    context = context,
                    productId = product.id,
                    nombre = nombre,
                    precioText = precio,
                    currentImageUrl = product.imagenUrl,
                    newImageUri = uri
                )
                editingProduct = null
            }
        )
    }

    deletingProduct?.let { product ->
        AlertDialog(
            onDismissRequest = { deletingProduct = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Seguro que quieres eliminar ${product.nombre}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct(product.id)
                        deletingProduct = null
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { deletingProduct = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ProductList(
    uiState: ProductUiState,
    onEdit: (ProductModel) -> Unit,
    onDelete: (ProductModel) -> Unit
) {
    if (uiState.products.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay productos en inventario", color = Color(0xFF9CA3AF))
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(uiState.products, key = { it.id }) { product ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xCC111827)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = product.imagenUrl,
                        contentDescription = product.nombre,
                        modifier = Modifier
                            .size(82.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(product.nombre, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("₡${"%.2f".format(product.precio)}", color = Color(0xFF93C5FD))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onEdit(product) }) { Text("Editar") }
                            Button(
                                onClick = { onDelete(product) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White)
                            ) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductFormDialog(
    title: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, Uri?) -> Unit,
    initialName: String = "",
    initialPrice: String = "",
    initialImage: String? = null
) {
    var nombre by rememberSaveable { mutableStateOf(initialName) }
    var precio by rememberSaveable { mutableStateOf(initialPrice) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = {
                        launcher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Seleccionar imagen")
                }

                val imageModel = selectedImageUri ?: initialImage
                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = "Imagen de producto",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(nombre, precio, selectedImageUri) }, enabled = !isSaving) {
                Text(if (isSaving) "Guardando..." else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancelar")
            }
        }
    )
}
