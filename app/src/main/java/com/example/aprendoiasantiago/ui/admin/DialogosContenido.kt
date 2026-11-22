package com.example.aprendoiasantiago.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.aprendoiasantiago.data.local.DatosEjemplo
import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity

/** Valida lo que escribió el usuario; devuelve el texto del error o `null` si todo está bien. */
internal object ValidadorContenido {
    fun punto(nombre: String, descripcion: String, lat: String, lon: String, alt: String): String? {
        if (nombre.isBlank()) return "Escribe el nombre."
        if (descripcion.isBlank()) return "Escribe la descripción."
        val la = lat.replace(',', '.').toDoubleOrNull()
        val lo = lon.replace(',', '.').toDoubleOrNull()
        if (la == null || la !in -90.0..90.0) return "La latitud debe estar entre -90 y 90."
        if (lo == null || lo !in -180.0..180.0) return "La longitud debe estar entre -180 y 180."
        if (alt.replace(',', '.').toDoubleOrNull() == null) return "La altitud debe ser un número."
        return null
    }

    fun capsula(titulo: String, qr: String, puntos: String, puntoId: Long?): String? {
        if (titulo.isBlank()) return "Escribe el título."
        if (qr.isBlank()) return "Escribe el código QR."
        val pts = puntos.toIntOrNull()
        if (pts == null || pts !in 1..1000) return "Los puntos deben ser un número entre 1 y 1000."
        if (puntoId == null) return "Primero crea un punto turístico."
        return null
    }
}

/** Crear (`inicial == null`) o editar un punto turístico. */
@Composable
internal fun DialogoPunto(
    inicial: PuntoTuristicoEntity?,
    onDismiss: () -> Unit,
    onGuardar: (PuntoTuristicoEntity) -> Unit
) {
    var nombre by remember { mutableStateOf(inicial?.nombre.orEmpty()) }
    var desc by remember { mutableStateOf(inicial?.descripcion.orEmpty()) }
    var lat by remember { mutableStateOf((inicial?.latitud ?: -33.4372).toString()) }
    var lon by remember { mutableStateOf((inicial?.longitud ?: -70.6506).toString()) }
    var alt by remember { mutableStateOf((inicial?.altitud ?: 540.0).toString()) }
    var error by remember { mutableStateOf<String?>(null) }
    val numerico = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (inicial == null) "Nuevo punto turístico" else "Editar punto turístico") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, singleLine = true)
                OutlinedTextField(desc, { desc = it }, label = { Text("Descripción") })
                OutlinedTextField(lat, { lat = it }, label = { Text("Latitud") }, singleLine = true, keyboardOptions = numerico)
                OutlinedTextField(lon, { lon = it }, label = { Text("Longitud") }, singleLine = true, keyboardOptions = numerico)
                OutlinedTextField(alt, { alt = it }, label = { Text("Altitud (m)") }, singleLine = true, keyboardOptions = numerico)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(onClick = {
                error = ValidadorContenido.punto(nombre, desc, lat, lon, alt)
                if (error == null) {
                    onGuardar(
                        PuntoTuristicoEntity(
                            id = inicial?.id ?: 0,
                            nombre = nombre.trim(),
                            descripcion = desc.trim(),
                            latitud = lat.replace(',', '.').toDouble(),
                            longitud = lon.replace(',', '.').toDouble(),
                            altitud = alt.replace(',', '.').toDouble()
                        )
                    )
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/** Crear (`inicial == null`) o editar una cápsula, eligiendo el punto al que pertenece. */
@Composable
internal fun DialogoCapsula(
    inicial: CapsulaEntity?,
    puntos: List<PuntoTuristicoEntity>,
    onDismiss: () -> Unit,
    onGuardar: (CapsulaEntity) -> Unit
) {
    var titulo by remember { mutableStateOf(inicial?.titulo.orEmpty()) }
    var qr by remember { mutableStateOf(inicial?.codigoQr.orEmpty()) }
    var pts by remember { mutableStateOf((inicial?.puntos ?: 10).toString()) }
    var video by remember { mutableStateOf(inicial?.urlVideo ?: DatosEjemplo.VIDEO_PRUEBA) }
    var puntoId by remember { mutableStateOf(inicial?.puntoId ?: puntos.firstOrNull()?.id) }
    var menu by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (inicial == null) "Nueva cápsula IA" else "Editar cápsula IA") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(titulo, { titulo = it }, label = { Text("Título del video") })
                OutlinedTextField(qr, { qr = it }, label = { Text("Código QR (ej: AIJS-008)") }, singleLine = true)
                OutlinedTextField(
                    pts, { pts = it }, label = { Text("Puntos") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(video, { video = it }, label = { Text("URL del video") })
                Box {
                    OutlinedButton(onClick = { menu = true }) {
                        Text("Punto: " + (puntos.firstOrNull { it.id == puntoId }?.nombre ?: "elegir…"))
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        puntos.forEach { p ->
                            DropdownMenuItem(text = { Text(p.nombre) }, onClick = { puntoId = p.id; menu = false })
                        }
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(onClick = {
                error = ValidadorContenido.capsula(titulo, qr, pts, puntoId)
                if (error == null) {
                    onGuardar(
                        CapsulaEntity(
                            id = inicial?.id ?: 0,
                            puntoId = puntoId!!,
                            titulo = titulo.trim(),
                            urlVideo = video.trim().ifBlank { DatosEjemplo.VIDEO_PRUEBA },
                            codigoQr = qr.trim(),
                            puntos = pts.toInt()
                        )
                    )
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
