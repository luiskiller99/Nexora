package com.example.nexora.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nexora.ui.viewmodel.ClientePromocion
import com.example.nexora.ui.viewmodel.ProductoPromocion
import com.example.nexora.ui.viewmodel.PromocionesUiState
import java.util.Locale

@Composable
fun PromocionesScreen(
    uiState: PromocionesUiState,
    onBackClick: () -> Unit,
    onToggleCliente: (Int) -> Unit,
    onAgregarProducto: (ProductoPromocion) -> Unit,
    onQuitarProducto: (Int) -> Unit,
    onGenerarImagen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
                    ProductoRow(
                        producto = producto,
                        onAgregar = { onAgregarProducto(producto) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (uiState.productosSeleccionados.isNotEmpty()) {
                    Text(
                        text = "Carrito de promoción",
                        style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFFBFDBFE))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    uiState.productosSeleccionados.forEach { seleccionado ->
                        ProductoSeleccionadoRow(
                            producto = seleccionado,
                            onQuitar = { onQuitarProducto(seleccionado.id) }
                        )
                    }
                }
            }

            SectionCard(title = "3) Vista previa de promoción") {
                PromoPreviewCard(
                    productosSeleccionados = uiState.productosSeleccionados,
                    imagenGenerada = uiState.imagenGenerada
                )
            }

            SectionCard(title = "4) Acciones") {
                Button(
                    onClick = {
                        onGenerarImagen()
                        Toast.makeText(context, "Imagen generada (simulado)", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    )
                ) {
                    Text("Generar Imagen", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        Toast.makeText(
                            context,
                            "Enviado a ${uiState.clientesSeleccionados.size} clientes",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C3AED),
                        contentColor = Color.White
                    )
                ) {
                    Text("Enviar por WhatsApp", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xCC111827)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ClienteRow(cliente: ClientePromocion, selected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = selected, onCheckedChange = { onToggle() })
        Column {
            Text(cliente.nombre, color = Color.White, fontWeight = FontWeight.Medium)
            Text(cliente.ubicacion, color = Color(0xFF9CA3AF), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ProductoRow(producto: ProductoPromocion, onAgregar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = producto.nombre, color = Color.White)
                Text(
                    text = formatearPrecio(producto.precio),
                    color = Color(0xFF93C5FD),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            OutlinedButton(onClick = onAgregar) {
                Text("Agregar a promoción")
            }
        }
    }
}

@Composable
private fun ProductoSeleccionadoRow(producto: ProductoPromocion, onQuitar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("• ${producto.nombre}", color = Color(0xFFE5E7EB))
        OutlinedButton(onClick = onQuitar) {
            Text("Quitar")
        }
    }
}

@Composable
private fun PromoPreviewCard(
    productosSeleccionados: List<ProductoPromocion>,
    imagenGenerada: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E293B), Color(0xFF1D4ED8), Color(0xFF6D28D9))
                    )
                )
                .padding(16.dp)
        ) {
            Text(
                text = "PROMOCIÓN",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (productosSeleccionados.isEmpty()) {
                Text(
                    text = "Selecciona productos para ver la vista previa",
                    color = Color(0xFFE0E7FF)
                )
            } else {
                productosSeleccionados.forEach { producto ->
                    Text(
                        text = "• ${producto.nombre} — ${formatearPrecio(producto.precio)}",
                        color = Color(0xFFF8FAFC)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (imagenGenerada) "Imagen lista para compartir" else "Imagen pendiente de generar",
                color = if (imagenGenerada) Color(0xFFBBF7D0) else Color(0xFFFDE68A),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

private fun formatearPrecio(precio: Double): String =
    String.format(Locale.US, "$%.2f", precio)

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun PromocionesScreenPreview() {
    var state by remember {
        mutableStateOf(
            PromocionesUiState(
                clientes = listOf(
                    ClientePromocion(1, "Juan Pérez", "San José"),
                    ClientePromocion(2, "María López", "Heredia")
                ),
                productos = listOf(
                    ProductoPromocion(1, "Arroz Premium 1kg", 2.9),
                    ProductoPromocion(2, "Aceite Vegetal 900ml", 4.25)
                )
            )
        )
    }

    PromocionesScreen(
        uiState = state,
        onBackClick = {},
        onToggleCliente = {
            val set = state.clientesSeleccionados.toMutableSet()
            if (!set.add(it)) set.remove(it)
            state = state.copy(clientesSeleccionados = set)
        },
        onAgregarProducto = { producto ->
            if (state.productosSeleccionados.none { it.id == producto.id }) {
                state = state.copy(productosSeleccionados = state.productosSeleccionados + producto)
            }
        },
        onQuitarProducto = { id ->
            state = state.copy(productosSeleccionados = state.productosSeleccionados.filterNot { it.id == id })
        },
        onGenerarImagen = { state = state.copy(imagenGenerada = true) }
    )
}
