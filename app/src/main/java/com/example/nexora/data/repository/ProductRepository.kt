package com.example.nexora.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.nexora.data.model.ProductModel
import com.example.nexora.data.remote.SupabaseClientProvider
import com.example.nexora.data.remote.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import java.util.UUID

class ProductRepository(
    private val provider: SupabaseClientProvider = SupabaseClientProvider()
) {

    suspend fun getProducts(): List<ProductModel> = withContext(Dispatchers.IO) {
        val config = requireConfig()
        val url = "${config.url}/rest/v1/productos?select=id,nombre,precio,imagen_url&order=created_at.desc"
        val conn = connection(url, config, "GET")

        val code = conn.responseCode
        if (code !in 200..299) {
            throw IllegalStateException("Error listando productos: $code ${conn.errorBody()}")
        }

        val response = conn.inputStream.bufferedReader().readText()
        val array = JSONArray(response)
        buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(
                    ProductModel(
                        id = item.optString("id"),
                        nombre = item.optString("nombre"),
                        precio = item.optDouble("precio", 0.0),
                        imagenUrl = item.optString("imagen_url")
                    )
                )
            }
        }
    }

    suspend fun createProduct(context: Context, nombre: String, precio: Double, imageUri: Uri): ProductModel =
        withContext(Dispatchers.IO) {
            val config = requireConfig()
            val imageUrl = uploadImage(context, imageUri, config)
            val payload = JSONObject()
                .put("nombre", nombre)
                .put("precio", precio)
                .put("imagen_url", imageUrl)

            val url = "${config.url}/rest/v1/productos"
            val conn = connection(url, config, "POST").apply {
                setRequestProperty("Prefer", "return=representation")
                doOutput = true
            }

            conn.outputStream.use { it.write(payload.toString().toByteArray()) }
            val code = conn.responseCode
            if (code !in 200..299) {
                throw IllegalStateException("Error creando producto: $code ${conn.errorBody()}")
            }

            val response = conn.inputStream.bufferedReader().readText()
            val item = JSONArray(response).getJSONObject(0)
            ProductModel(
                id = item.optString("id"),
                nombre = item.optString("nombre"),
                precio = item.optDouble("precio", 0.0),
                imagenUrl = item.optString("imagen_url")
            )
        }

    suspend fun updateProduct(
        context: Context,
        productId: String,
        nombre: String,
        precio: Double,
        newImageUri: Uri?,
        currentImageUrl: String
    ): ProductModel = withContext(Dispatchers.IO) {
        val config = requireConfig()
        val finalImageUrl = newImageUri?.let { uploadImage(context, it, config) } ?: currentImageUrl

        val payload = JSONObject()
            .put("nombre", nombre)
            .put("precio", precio)
            .put("imagen_url", finalImageUrl)

        val safeId = URLEncoder.encode("eq.$productId", "UTF-8")
        val url = "${config.url}/rest/v1/productos?id=$safeId"
        val conn = connection(url, config, "PATCH").apply {
            setRequestProperty("Prefer", "return=representation")
            doOutput = true
        }

        conn.outputStream.use { it.write(payload.toString().toByteArray()) }
        val code = conn.responseCode
        if (code !in 200..299) {
            throw IllegalStateException("Error actualizando producto: $code ${conn.errorBody()}")
        }

        val response = conn.inputStream.bufferedReader().readText()
        val item = JSONArray(response).getJSONObject(0)
        ProductModel(
            id = item.optString("id"),
            nombre = item.optString("nombre"),
            precio = item.optDouble("precio", 0.0),
            imagenUrl = item.optString("imagen_url")
        )
    }

    suspend fun deleteProduct(productId: String) = withContext(Dispatchers.IO) {
        val config = requireConfig()
        val safeId = URLEncoder.encode("eq.$productId", "UTF-8")
        val url = "${config.url}/rest/v1/productos?id=$safeId"
        val conn = connection(url, config, "DELETE")
        val code = conn.responseCode
        if (code !in 200..299) {
            throw IllegalStateException("Error eliminando producto: $code ${conn.errorBody()}")
        }
    }

    private fun uploadImage(context: Context, uri: Uri, config: SupabaseConfig): String {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("No se pudo leer la imagen")

        val mimeType = context.contentResolver.getType(uri)?.lowercase(Locale.ROOT) ?: "image/jpeg"
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
        val fileName = "producto_${System.currentTimeMillis()}_${UUID.randomUUID()}.$extension"

        val uploadUrl = "${config.url}/storage/v1/object/Promos/$fileName"
        val conn = connection(uploadUrl, config, "POST").apply {
            setRequestProperty("Content-Type", mimeType)
            setRequestProperty("x-upsert", "true")
            doOutput = true
        }

        conn.outputStream.use { it.write(bytes) }
        val code = conn.responseCode
        if (code !in 200..299) {
            throw IllegalStateException("Error subiendo imagen: $code ${conn.errorBody()}")
        }

        return "${config.url}/storage/v1/object/public/Promos/$fileName"
    }

    private fun connection(url: String, config: SupabaseConfig, method: String): HttpURLConnection {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("apikey", config.apiKey)
            setRequestProperty("Authorization", "Bearer ${config.apiKey}")
            setRequestProperty("Content-Type", "application/json")
        }
    }

    private fun HttpURLConnection.errorBody(): String {
        return errorStream?.bufferedReader()?.readText().orEmpty()
    }

    private fun requireConfig(): SupabaseConfig {
        val config = provider.getConfig()
        require(config.isConfigured) { "Supabase no está configurado" }
        return config
    }
}
