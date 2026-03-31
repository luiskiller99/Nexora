package com.example.nexora.data.remote

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

data class GuardarPromocionResult(
    val promocionId: String,
    val imagenPublicaUrl: String
)

class SupabasePromocionesService(
    private val provider: SupabaseClientProvider = SupabaseClientProvider()
) {
    fun guardarPromocion(
        context: Context,
        imagenUri: Uri,
        titulo: String,
        precioNormal: Double,
        precioPromo: Double,
        productos: List<Pair<String, Double>>
    ): GuardarPromocionResult {
        val config = provider.getConfig()
        require(config.isConfigured) { "Supabase no está configurado" }

        val imageUrl = subirImagenAlBucket(context, imagenUri, config)
        val promoId = insertarPromocion(config, titulo, imageUrl, precioNormal, precioPromo)
        insertarProductosPromocion(config, promoId, productos)

        return GuardarPromocionResult(promocionId = promoId, imagenPublicaUrl = imageUrl)
    }

    private fun subirImagenAlBucket(context: Context, uri: Uri, config: SupabaseConfig): String {
        val fileName = "promo_${System.currentTimeMillis()}_${UUID.randomUUID()}.png"
        val uploadUrl = "${config.url}/storage/v1/object/promos/$fileName"

        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("No se pudo leer imagen")

        val conn = (URL(uploadUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("apikey", config.apiKey)
            setRequestProperty("Authorization", "Bearer ${config.apiKey}")
            setRequestProperty("Content-Type", "image/png")
            setRequestProperty("x-upsert", "true")
        }

        conn.outputStream.use { it.write(bytes) }
        val code = conn.responseCode
        if (code !in 200..299) {
            val errorBody = conn.errorStream?.bufferedReader()?.readText().orEmpty()
            error("Error subiendo imagen a Supabase Storage: $code $errorBody")
        }

        return "${config.url}/storage/v1/object/public/promos/$fileName"
    }

    private fun insertarPromocion(
        config: SupabaseConfig,
        titulo: String,
        imagenUrl: String,
        precioNormal: Double,
        precioPromo: Double
    ): String {
        val url = "${config.url}/rest/v1/promociones"
        val payload = JSONObject()
            .put("titulo", titulo)
            .put("imagen_url", imagenUrl)
            .put("precio_normal", precioNormal)
            .put("precio_promo", precioPromo)

        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("apikey", config.apiKey)
            setRequestProperty("Authorization", "Bearer ${config.apiKey}")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Prefer", "return=representation")
        }

        conn.outputStream.use { it.write(payload.toString().toByteArray()) }
        val code = conn.responseCode
        if (code !in 200..299) {
            val errorBody = conn.errorStream?.bufferedReader()?.readText().orEmpty()
            error("Error insertando promoción: $code $errorBody")
        }

        val body = conn.inputStream.bufferedReader().readText()
        val arr = JSONArray(body)
        return arr.getJSONObject(0).getString("id")
    }

    private fun insertarProductosPromocion(
        config: SupabaseConfig,
        promocionId: String,
        productos: List<Pair<String, Double>>
    ) {
        if (productos.isEmpty()) return
        val url = "${config.url}/rest/v1/promocion_productos"
        val payload = JSONArray()
        productos.forEach { (nombre, precio) ->
            payload.put(
                JSONObject()
                    .put("promocion_id", promocionId)
                    .put("nombre", nombre)
                    .put("precio", precio)
            )
        }

        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("apikey", config.apiKey)
            setRequestProperty("Authorization", "Bearer ${config.apiKey}")
            setRequestProperty("Content-Type", "application/json")
        }

        conn.outputStream.use { it.write(payload.toString().toByteArray()) }
        val code = conn.responseCode
        if (code !in 200..299) {
            val errorBody = conn.errorStream?.bufferedReader()?.readText().orEmpty()
            error("Error guardando productos de promoción: $code $errorBody")
        }
    }

    fun buildShareLink(baseDomain: String, promocionId: String): String {
        val safeDomain = baseDomain.trim().trimEnd('/')
        val encodedId = URLEncoder.encode(promocionId, "UTF-8")
        return "$safeDomain?id=$encodedId"
    }
}
