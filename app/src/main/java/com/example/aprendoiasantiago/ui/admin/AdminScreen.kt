package com.example.aprendoiasantiago.ui.admin

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.local.DatosEjemplo
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.model.UsuarioEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    usuarios: List<UsuarioEntity>,
    puntos: List<PuntoTuristicoEntity>,
    capsulas: List<CapsulaEntity>,
    onEliminarUsuario: (Long) -> Unit,
    onGuardarPunto: (PuntoTuristicoEntity) -> Unit,
    onEliminarPunto: (Long) -> Unit,
    onGuardarCapsula: (CapsulaEntity) -> Unit,
    onEliminarCapsula: (Long) -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tabActual by remember { mutableStateOf(0) }
    val titulosTabs = listOf("Jugadores", "Puntos", "Cápsulas")

    var mostrarDialogoPunto by remember { mutableStateOf(false) }
    var mostrarDialogoCapsula by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.Filled.ExitToApp, "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            // Mostrar botón flotante solo en Puntos (1) y Cápsulas (2)
            if (tabActual == 1) {
                FloatingActionButton(onClick = { mostrarDialogoPunto = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Nuevo Punto")
                }
            } else if (tabActual == 2) {
                FloatingActionButton(onClick = { mostrarDialogoCapsula = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Nueva Cápsula")
                }
            }
        }
    ) { padding ->
        Column(modifier = modifier.fillMaxSize().padding(padding)) {
            // --- BARRA DE PESTAÑAS ---
            TabRow(selectedTabIndex = tabActual) {
                titulosTabs.forEachIndexed { index, titulo ->
                    Tab(
                        selected = tabActual == index,
                        onClick = { tabActual = index },
                        text = { Text(titulo, fontWeight = if (tabActual == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // --- CONTENIDO DE CADA PESTAÑA ---
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (tabActual) {
                    0 -> PestañaJugadores(usuarios, onEliminarUsuario)
                    1 -> PestañaPuntos(puntos, onEliminarPunto)
                    2 -> PestañaCapsulas(capsulas, onEliminarCapsula)
                }
            }
        }
    }

    // --- CUADROS DE DIÁLOGO PARA CREAR ---
    if (mostrarDialogoPunto) {
        DialogoCrearPunto(
            onDismiss = { mostrarDialogoPunto = false },
            onGuardar = { nombre, desc ->
                onGuardarPunto(PuntoTuristicoEntity(nombre = nombre, descripcion = desc, latitud = -33.4, longitud = -70.6, altitud = 500.0))
                mostrarDialogoPunto = false
            }
        )
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

// =========================================================================================
// SUB-COMPONENTES DE LAS PESTAÑAS
// =========================================================================================

@Composable
private fun PestañaJugadores(usuarios: List<UsuarioEntity>, onEliminarUsuario: (Long) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(usuarios) { usuario ->
            val esProtegida = usuario.nombreUsuario == DatosEjemplo.USUARIO_ADMIN || usuario.nombreUsuario == DatosEjemplo.USUARIO_CREADOR
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(usuario.nombreVisible, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("@${usuario.nombreUsuario} • ${usuario.puntaje} pts", style = MaterialTheme.typography.bodySmall)
                    }
                    if (!esProtegida) {
                        IconButton(onClick = { onEliminarUsuario(usuario.id) }) { Icon(Icons.Filled.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error) }
                    } else {
                        Text("Protegido", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun PestañaPuntos(puntos: List<PuntoTuristicoEntity>, onEliminarPunto: (Long) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(puntos) { punto ->
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(punto.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(punto.descripcion, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                    }
                    IconButton(onClick = { onEliminarPunto(punto.id) }) { Icon(Icons.Filled.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}

@Composable
private fun PestañaCapsulas(capsulas: List<CapsulaEntity>, onEliminarCapsula: (Long) -> Unit) {
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
                    IconButton(onClick = { onEliminarCapsula(capsula.id) }) { Icon(Icons.Filled.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}

// =========================================================================================
// CUADROS DE DIÁLOGO PARA FORMULARIOS
// =========================================================================================

@Composable
private fun DialogoCrearPunto(onDismiss: () -> Unit, onGuardar: (String, String) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Punto Turístico") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre (ej: Cerro San Cristóbal)") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción") })
            }
        },
        confirmButton = { Button(onClick = { onGuardar(nombre, desc) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
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