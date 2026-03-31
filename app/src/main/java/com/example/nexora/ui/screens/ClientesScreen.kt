package com.example.nexora.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.nexora.ui.viewmodel.Cliente

@Composable
fun ClientesScreen(
    clientes: List<Cliente>,
    onBackClick: () -> Unit,
    onAgregarCliente: (String, String, String?) -> Unit,
    onAgregarDesdeContacto: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mostrarOpciones by remember { mutableStateOf(false) }
    var mostrarFormulario by remember { mutableStateOf(false) }

    val contactPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri = result.data?.data ?: return@rememberLauncherForActivityResult
            context.contentResolver.query(
                contactUri,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val name = cursor.getString(0).orEmpty()
                    val number = cursor.getString(1).orEmpty()
                    onAgregarDesdeContacto(name, number)
                    Toast.makeText(context, "Cliente agregado desde contactos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val requestContactPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            contactPickerLauncher.launch(intent)
        } else {
            Toast.makeText(context, "Permiso de contactos requerido", Toast.LENGTH_SHORT).show()
        }
    }

    val gradient = Brush.verticalGradient(listOf(Color(0xFF0B1220), Color(0xFF0F172A)))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onBackClick) {
                    Text("Volver")
                }
                Text(
                    text = "Clientes",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Button(
                onClick = { mostrarOpciones = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White
                )
            ) {
                Text("Agregar Cliente", style = MaterialTheme.typography.titleMedium)
            }

            clientes.forEach { cliente ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC111827))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(cliente.nombre, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(cliente.telefono, color = Color(0xFF93C5FD))
                        if (!cliente.ubicacion.isNullOrBlank()) {
                            Text(cliente.ubicacion, color = Color(0xFF9CA3AF))
                        }
                    }
                }
            }

            if (clientes.isEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                Text("No hay clientes aún", color = Color(0xFF9CA3AF))
            }
        }
    }

    if (mostrarOpciones) {
        AlertDialog(
            onDismissRequest = { mostrarOpciones = false },
            title = { Text("Agregar Cliente") },
            text = { Text("¿Cómo deseas agregar el cliente?") },
            confirmButton = {
                Button(onClick = {
                    mostrarOpciones = false
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                        contactPickerLauncher.launch(intent)
                    } else {
                        requestContactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                }) {
                    Text("Seleccionar desde contactos")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    mostrarOpciones = false
                    mostrarFormulario = true
                }) {
                    Text("Crear manualmente")
                }
            }
        )
    }

    if (mostrarFormulario) {
        var nombre by remember { mutableStateOf("") }
        var telefono by remember { mutableStateOf("") }
        var ubicacion by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { mostrarFormulario = false },
            title = { Text("Nuevo cliente") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                    OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") })
                    OutlinedTextField(value = ubicacion, onValueChange = { ubicacion = it }, label = { Text("Ubicación (opcional)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    onAgregarCliente(nombre, telefono, ubicacion)
                    mostrarFormulario = false
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarFormulario = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
