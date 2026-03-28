package com.example.nexora.ui.screens

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nexora.ui.viewmodel.ClientePromocion
import com.example.nexora.ui.viewmodel.ProductoPromocion
import com.example.nexora.ui.viewmodel.PromocionesUiState

@Composable
fun PromocionesScreen(
    uiState: PromocionesUiState,
    onBackClick: () -> Unit,
    onToggleCliente: (Int) -> Unit,
    onToggleProducto: (ProductoPromocion) -> Unit,
    onActualizarPrecioPromo: (String) -> Unit,
    onActualizarFondo: (Uri?) -> Unit,
    onGenerarImagen: () -> Unit,
    onEnviarPromocion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var precioPromoInput by remember(uiState.precioPromo) { mutableStateOf(uiState.precioPromo.toInt().toString()) }

    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onActualizarFondo(result.data?.data)
        }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0B1220), Color(0xFF0A0F1A), Color(0xFF0D1028))
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFBFDBFE))
                ) {
                    Text("Volver")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Promociones",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            SectionCard(title = "1) Selección de clientes") {
                uiState.clientes.forEach { cliente ->
                    ClienteRow(
                        cliente = cliente,
                        selected = cliente.id in uiState.clientesSeleccionados,
                        onToggle = { onToggleCliente(cliente.id) }
                    )
                }
            }

            SectionCard(title = "2) Productos en promoción") {
                uiState.productos.forEach { producto ->
                    val seleccionado = uiState.productosSeleccionados.any { it.id == producto.id }
                    ProductoSelectableCard(
                        producto = producto,
                        selected = seleccionado,
                        onClick = { onToggleProducto(producto) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (uiState.productosSeleccionados.isNotEmpty()) {
                    Text(
                        text = "Carrito (${uiState.productosSeleccionados.size})",
                        style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFF93C5FD))
                    )
                    uiState.productosSeleccionados.forEach { seleccionado ->
                        Text("• ${seleccionado.nombre}", color = Color(0xFFE2E8F0), style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total: ${formatearColones(uiState.totalOriginal)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFFBFDBFE),
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "Precio Promo: ${formatearColones(uiState.precioPromo)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFFC4B5FD),
                        fontWeight = FontWeight.Bold
                    )
                )

                OutlinedTextField(
                    value = precioPromoInput,
                    onValueChange = {
                        precioPromoInput = it
                        onActualizarPrecioPromo(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Editar precio promo (opcional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedContainerColor = Color(0xFF111827),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF93C5FD),
                        unfocusedLabelColor = Color(0xFF9CA3AF),
                        focusedIndicatorColor = Color(0xFF6366F1),
                        unfocusedIndicatorColor = Color(0xFF1F2937)
                    )
                )
            }

            SectionCard(title = "3) Vista previa de promoción") {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_PICK).apply {
                            type = "image/*"
                        }
                        pickerLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Seleccionar fondo")
                }
                Spacer(modifier = Modifier.height(10.dp))
                PromoPreviewCard(
                    productosSeleccionados = uiState.productosSeleccionados,
                    totalOriginal = uiState.totalOriginal,
                    precioPromo = uiState.precioPromo,
                    fondoUri = uiState.fondoUri
                )
            }

            SectionCard(title = "4) Acciones") {
                Button(
                    onClick = onGenerarImagen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB), contentColor = Color.White)
                ) {
                    Text("Generar Imagen", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onEnviarPromocion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED), contentColor = Color.White)
                ) {
                    Text("Enviar Promoción", style = MaterialTheme.typography.titleMedium)
                }

                if (uiState.imagenUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Imagen lista para compartir",
                        color = Color(0xFFBBF7D0),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xCC111827)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ClienteRow(cliente: ClientePromocion, selected: Boolean, onToggle: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = selected, onCheckedChange = { onToggle() })
        Column {
            Text(cliente.nombre, color = Color.White, fontWeight = FontWeight.Medium)
            Text("${cliente.ubicacion} • ${cliente.telefono}", color = Color(0xFF9CA3AF), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ProductoSelectableCard(producto: ProductoPromocion, selected: Boolean, onClick: () -> Unit) {
    val borderBrush = Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF7C3AED)))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then(
                if (selected) Modifier.border(2.dp, borderBrush, RoundedCornerShape(12.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFF1E293B) else Color(0xFF0F172A)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = producto.nombre, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(text = formatearColones(producto.precio), color = Color(0xFF93C5FD), style = MaterialTheme.typography.bodySmall)
            }
            if (selected) {
                Text("✔", color = Color(0xFF22C55E), style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun PromoPreviewCard(
    productosSeleccionados: List<ProductoPromocion>,
    totalOriginal: Double,
    precioPromo: Double,
    fondoUri: Uri?
) {
    val context = LocalContext.current
    val imageBitmap = remember(fondoUri) {
        fondoUri?.let { uri ->
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(340.dp)) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Fondo promoción",
                    modifier = Modifier.fillMaxSize()
                )
                Box(modifier = Modifier.fillMaxSize().background(Color(0x80070C1B)))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF1D4ED8), Color(0xFF6D28D9))))
                )
            }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    text = "🔥 PROMOCIÓN 🔥",
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.ExtraBold)
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (productosSeleccionados.isEmpty()) {
                    Text("Selecciona productos para generar el flyer", color = Color(0xFFE0E7FF))
                } else {
                    productosSeleccionados.forEach { producto ->
                        Text("• ${producto.nombre}", color = Color(0xFFF8FAFC))
                    }
                }
                Spacer(modifier = Modifier.weight(1f))

                val hayDescuento = precioPromo < totalOriginal
                Text(
                    text = "Precio normal: ${formatearColones(totalOriginal)}",
                    color = Color(0xFFBFDBFE),
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = if (hayDescuento) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None
                    )
                )
                Text(
                    text = "PRECIO PROMO: ${formatearColones(precioPromo)}",
                    color = Color(0xFFC4B5FD),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }
    }
}

private fun formatearColones(valor: Double): String =
    "₡" + valor.toInt().toString().reversed().chunked(3).joinToString(".").reversed()

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun PromocionesScreenPreview() {
    var state by remember {
        mutableStateOf(
            PromocionesUiState(
                clientes = listOf(
                    ClientePromocion(1, "Juan Pérez", "San José", "50688881111"),
                    ClientePromocion(2, "María López", "Heredia", "50687772222")
                ),
                productos = listOf(
                    ProductoPromocion(1, "Arroz Premium 1kg", 2900.0),
                    ProductoPromocion(2, "Aceite Vegetal 900ml", 4250.0)
                )
            )
        )
    }

    PromocionesScreen(
        uiState = state,
        onBackClick = {},
        onToggleCliente = {
            val set = state.clientesSeleccionados.toMutableSet(); if (!set.add(it)) set.remove(it)
            state = state.copy(clientesSeleccionados = set)
        },
        onToggleProducto = { producto ->
            val list = state.productosSeleccionados.toMutableList()
            val i = list.indexOfFirst { it.id == producto.id }
            if (i >= 0) list.removeAt(i) else list.add(producto)
            val total = list.sumOf { it.precio }
            state = state.copy(productosSeleccionados = list, totalOriginal = total, precioPromo = total)
        },
        onActualizarPrecioPromo = { input -> input.toDoubleOrNull()?.let { state = state.copy(precioPromo = it) } },
        onActualizarFondo = {},
        onGenerarImagen = {},
        onEnviarPromocion = {}
    )
}
