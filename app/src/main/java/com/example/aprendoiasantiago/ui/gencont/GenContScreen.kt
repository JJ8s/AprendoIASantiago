package com.example.aprendoiasantiago.ui.gencont

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.ui.admin.DialogoCapsula
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenContScreen(
    capsulas: List<CapsulaEntity>,
    puntos: List<PuntoTuristicoEntity>,
    onGuardarCapsula: (CapsulaEntity) -> Unit,
    onEliminarCapsula: (Long) -> Unit,
    onCerrarSesion: () -> Unit,
    aviso: String? = null,
    onAvisoVisto: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var mostrarDialogoCapsula by remember { mutableStateOf(false) }
    var capsulaEnEdicion by remember { mutableStateOf<CapsulaEntity?>(null) }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(aviso) {
        if (aviso != null) {
            snackbar.showSnackbar(aviso)
            onAvisoVisto()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
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
            FloatingActionButton(onClick = { capsulaEnEdicion = null; mostrarDialogoCapsula = true }) {
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
                            IconButton(onClick = { capsulaEnEdicion = capsula; mostrarDialogoCapsula = true }) {
                                Icon(Icons.Filled.Edit, "Editar")
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
        DialogoCapsula(
            inicial = capsulaEnEdicion,
            puntos = puntos,
            onDismiss = { mostrarDialogoCapsula = false },
            onGuardar = { onGuardarCapsula(it); mostrarDialogoCapsula = false }
        )
    }
}
