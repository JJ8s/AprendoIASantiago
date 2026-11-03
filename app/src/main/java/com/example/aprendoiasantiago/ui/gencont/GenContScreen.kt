package com.example.aprendoiasantiago.ui.gencont

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.local.DatosEjemplo
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenContScreen(
    capsulas: List<CapsulaEntity>,
    puntos: List<PuntoTuristicoEntity>,
    onGuardarCapsula: (CapsulaEntity) -> Unit,
    onEliminarCapsula: (Long) -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarDialogoCapsula by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Creador de Contenido", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.Filled.ExitToApp, "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogoCapsula = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva Cápsula")
            }
        }
    ) { padding ->
        Column(modifier = modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Gestión de Cápsulas IA", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(capsulas) { capsula ->
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(capsula.titulo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("QR: ${capsula.codigoQr} • ${capsula.puntos} pts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { onEliminarCapsula(capsula.id) }) {
                                Icon(Icons.Filled.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoCapsula) {
        DialogoCrearCapsula(
            puntos = puntos,
            onDismiss = { mostrarDialogoCapsula = false },
            onGuardar = { titulo, url, qr, puntoId, pts ->
                onGuardarCapsula(CapsulaEntity(puntoId = puntoId, titulo = titulo, urlVideo = url, codigoQr = qr, puntos = pts))
                mostrarDialogoCapsula = false
            }
        )
    }
}

@Composable
private fun DialogoCrearCapsula(puntos: List<PuntoTuristicoEntity>, onDismiss: () -> Unit, onGuardar: (String, String, String, Long, Int) -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var qr by remember { mutableStateOf("") }
    val puntoSeleccionado = if (puntos.isNotEmpty()) puntos.first().id else 1L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Cápsula IA") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título del Video") })
                OutlinedTextField(value = qr, onValueChange = { qr = it }, label = { Text("Código QR (ej: AIJS-008)") })
                Text("Video por defecto y 10 pts asignados automáticamente.", style = MaterialTheme.typography.labelSmall)
            }
        },
        confirmButton = { Button(onClick = { onGuardar(titulo, DatosEjemplo.VIDEO_PRUEBA, qr, puntoSeleccionado, 10) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

