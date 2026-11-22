package com.example.aprendoiasantiago.ui.admin

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
    /** Mensaje del servidor (sin conexión, QR repetido…); se muestra una vez y luego se descarta. */
    aviso: String? = null,
    onAvisoVisto: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var tabActual by remember { mutableStateOf(0) }
    val titulosTabs = listOf("Jugadores", "Puntos", "Cápsulas")

    var mostrarDialogoPunto by remember { mutableStateOf(false) }
    var mostrarDialogoCapsula by remember { mutableStateOf(false) }
    // null = el diálogo crea; con valor = el diálogo edita ese elemento (el "Update" del CRUD)
    var puntoEnEdicion by remember { mutableStateOf<PuntoTuristicoEntity?>(null) }
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
                FloatingActionButton(onClick = { puntoEnEdicion = null; mostrarDialogoPunto = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Nuevo Punto")
                }
            } else if (tabActual == 2) {
                FloatingActionButton(onClick = { capsulaEnEdicion = null; mostrarDialogoCapsula = true }) {
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
                    1 -> PestañaPuntos(puntos, onEliminarPunto) { puntoEnEdicion = it; mostrarDialogoPunto = true }
                    2 -> PestañaCapsulas(capsulas, onEliminarCapsula) { capsulaEnEdicion = it; mostrarDialogoCapsula = true }
                }
            }
        }
    }

    // --- CUADROS DE DIÁLOGO PARA CREAR / EDITAR ---
    if (mostrarDialogoPunto) {
        DialogoPunto(
            inicial = puntoEnEdicion,
            onDismiss = { mostrarDialogoPunto = false },
            onGuardar = { onGuardarPunto(it); mostrarDialogoPunto = false }
        )
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
private fun PestañaPuntos(puntos: List<PuntoTuristicoEntity>, onEliminarPunto: (Long) -> Unit, onEditar: (PuntoTuristicoEntity) -> Unit) {
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
                    IconButton(onClick = { onEditar(punto) }) { Icon(Icons.Filled.Edit, "Editar") }
                    IconButton(onClick = { onEliminarPunto(punto.id) }) { Icon(Icons.Filled.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}

@Composable
private fun PestañaCapsulas(capsulas: List<CapsulaEntity>, onEliminarCapsula: (Long) -> Unit, onEditar: (CapsulaEntity) -> Unit) {
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
                    IconButton(onClick = { onEditar(capsula) }) { Icon(Icons.Filled.Edit, "Editar") }
                    IconButton(onClick = { onEliminarCapsula(capsula.id) }) { Icon(Icons.Filled.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}
