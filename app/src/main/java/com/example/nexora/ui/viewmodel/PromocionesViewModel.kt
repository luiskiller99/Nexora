package com.example.nexora.ui.viewmodel

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexora.data.remote.SupabasePromocionesService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class ClientePromocion(
    val id: Int,
    val nombre: String,
    val ubicacion: String,
    val telefono: String
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
    val totalOriginal: Double = 0.0,
    val precioPromo: Double = 0.0,
    val imagenUri: Uri? = null,
    val fondoUri: Uri? = null,
    val precioPromoEditado: Boolean = false,
    val linkPromocion: String? = null,
    val guardandoPromocion: Boolean = false
)

class PromocionesViewModel : ViewModel() {
    private val supabaseService = SupabasePromocionesService()

    var uiState by mutableStateOf(
        PromocionesUiState(
            clientes = listOf(
                ClientePromocion(1, "Juan Pérez", "San José", "50688881111"),
                ClientePromocion(2, "María López", "Heredia", "50687772222"),
                ClientePromocion(3, "Carlos Vega", "Alajuela", "50686663333")
            ),
            productos = listOf(
                ProductoPromocion(1, "Arroz Premium 1kg", 2900.0),
                ProductoPromocion(2, "Aceite Vegetal 900ml", 4250.0),
                ProductoPromocion(3, "Frijoles Negros 800g", 3100.0),
                ProductoPromocion(4, "Leche Entera 1L", 1850.0)
            )
        )
    )
        private set

    fun toggleCliente(clienteId: Int) {
        val seleccionados = uiState.clientesSeleccionados.toMutableSet()
        if (!seleccionados.add(clienteId)) seleccionados.remove(clienteId)
        uiState = uiState.copy(clientesSeleccionados = seleccionados)
    }

    fun toggleProducto(producto: ProductoPromocion) {
        val seleccionados = uiState.productosSeleccionados.toMutableList()
        val existente = seleccionados.indexOfFirst { it.id == producto.id }
        if (existente >= 0) seleccionados.removeAt(existente) else seleccionados.add(producto)

        val total = calcularTotal(seleccionados)
        uiState = uiState.copy(
            productosSeleccionados = seleccionados,
            totalOriginal = total,
            precioPromo = if (uiState.precioPromoEditado) uiState.precioPromo else total
        )
    }

    fun calcularTotal(productos: List<ProductoPromocion> = uiState.productosSeleccionados): Double =
        productos.sumOf { it.precio }

    fun actualizarPrecioPromo(valor: String) {
        val promo = valor.toDoubleOrNull() ?: return
        uiState = uiState.copy(precioPromo = promo, precioPromoEditado = true)
    }

    fun actualizarFondo(uri: Uri?) {
        uiState = uiState.copy(fondoUri = uri)
    }

    fun generarImagen(context: Context): Uri? {
        if (uiState.productosSeleccionados.isEmpty()) {
            Toast.makeText(context, "Selecciona productos primero", Toast.LENGTH_SHORT).show()
            return null
        }
        val uri = generarImagenPromocion(context)
        uiState = uiState.copy(imagenUri = uri)
        Toast.makeText(context, "Imagen generada", Toast.LENGTH_SHORT).show()
        return uri
    }

    fun guardarPromocionEnSupabase(context: Context, baseDomain: String) {
        val imagen = uiState.imagenUri ?: generarImagen(context) ?: return
        uiState = uiState.copy(guardandoPromocion = true)

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val result = supabaseService.guardarPromocion(
                    context = context,
                    imagenUri = imagen,
                    titulo = if (uiState.precioPromo < uiState.totalOriginal) "🔥 PROMOCIÓN 🔥" else "Catálogo Nexora",
                    precioNormal = uiState.totalOriginal,
                    precioPromo = uiState.precioPromo,
                    productos = uiState.productosSeleccionados.map { it.nombre to it.precio }
                )
                supabaseService.buildShareLink(baseDomain, result.promocionId)
            }.onSuccess { link ->
                uiState = uiState.copy(guardandoPromocion = false, linkPromocion = link)
                Toast.makeText(context, "Promoción guardada en Supabase", Toast.LENGTH_SHORT).show()
            }.onFailure {
                uiState = uiState.copy(guardandoPromocion = false)
                Toast.makeText(context, "Error guardando promoción: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun compartirPromocion(context: Context) {
        val imagen = uiState.imagenUri ?: generarImagen(context) ?: return
        val telefonos = uiState.clientes.filter { it.id in uiState.clientesSeleccionados }.map { it.telefono }
        if (telefonos.isEmpty()) {
            Toast.makeText(context, "Selecciona clientes", Toast.LENGTH_SHORT).show()
            return
        }
        compartirPorWhatsApp(context, imagen, telefonos, uiState.linkPromocion)
    }

    fun generarImagenPromocion(context: Context): Uri {
        val size = 1080
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val basePaint = Paint().apply { color = Color.rgb(15, 23, 42); style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), basePaint)

        uiState.fondoUri?.let { uri ->
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)?.let {
                    val scaled = Bitmap.createScaledBitmap(it, size, size, true)
                    canvas.drawBitmap(scaled, 0f, 0f, null)
                }
            }
        }

        val overlay = Paint().apply { color = Color.argb(140, 7, 12, 27); style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), overlay)

        val isPromo = uiState.precioPromo < uiState.totalOriginal
        if (isPromo) {
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 78f
                typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            }
            canvas.drawText("🔥 PROMOCIÓN 🔥", 80f, 140f, titlePaint)
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(226, 232, 240); textSize = 40f }
        var y = if (isPromo) 230f else 160f
        uiState.productosSeleccionados.forEach { producto ->
            canvas.drawText("• ${producto.nombre} — ${formatearColones(producto.precio)}", 80f, y, textPaint)
            y += 52f
        }

        if (isPromo) {
            val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(191, 219, 254)
                textSize = 46f
                isStrikeThruText = true
            }
            canvas.drawText("Precio normal: ${formatearColones(uiState.totalOriginal)}", 80f, 860f, normalPaint)

            val promoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(196, 181, 253)
                textSize = 62f
                typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            }
            canvas.drawText("PRECIO PROMO: ${formatearColones(uiState.precioPromo)}", 80f, 960f, promoPaint)
        } else {
            val totalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(191, 219, 254)
                textSize = 56f
                typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            }
            canvas.drawText("Total: ${formatearColones(uiState.totalOriginal)}", 80f, 940f, totalPaint)
        }

        val file = File(context.cacheDir, "promo_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun compartirPorWhatsApp(context: Context, uri: Uri, telefonos: List<String>, link: String?) {
        try {
            telefonos.forEach { telefono ->
                val texto = if (link.isNullOrBlank()) "Promoción Nexora" else "Promoción Nexora: $link"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    setPackage("com.whatsapp")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, texto)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    data = Uri.parse("whatsapp://send?phone=$telefono")
                }
                context.startActivity(intent)
            }
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "WhatsApp no instalado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatearColones(valor: Double): String =
        "₡" + valor.toInt().toString().reversed().chunked(3).joinToString(".").reversed()
}
