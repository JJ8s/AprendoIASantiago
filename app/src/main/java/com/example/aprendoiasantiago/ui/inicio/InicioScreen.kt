package com.example.aprendoiasantiago.ui.inicio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aprendoiasantiago.ui.viewmodels.PerfilUiState

@Composable
fun InicioScreen(
    estado: PerfilUiState,
    onIrAMapa: () -> Unit,
    onIrAEscaner: () -> Unit,
    onIrAColeccion: () -> Unit,
    onVerProgresoClick: () -> Unit, // <--- NUEVO: Evento para abrir la Pantalla 12
    modifier: Modifier = Modifier
) {
    // 1. Extraemos los datos del usuario desde el estado (si aún carga, usamos valores por defecto)
    val nombre = estado.usuario?.nombreVisible ?: "Explorador"
    val puntaje = estado.usuario?.puntaje ?: 0
    val completadas = estado.completadas
    val total = if (estado.totalCapsulas > 0) estado.totalCapsulas else 7

    // 2. Calculamos el progreso entre 0.0 y 1.0 para la barra visual
    val progreso = (completadas.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val porcentaje = (progreso * 100).toInt()

    // 3. Contenedor principal con scroll vertical por si la pantalla del teléfono es pequeña
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- ENCABEZADO DE BIENVENIDA ---
        Column {
            Text(
                text = "¡Hola, $nombre!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Sigue explorando Santiago",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // --- TARJETAS DE ESTADÍSTICAS (2 Filas x 2 Columnas) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaEstadistica(
                valor = "$puntaje",
                etiqueta = "Puntos totales",
                modifier = Modifier.weight(1f)
            )
            TarjetaEstadistica(
                valor = "$completadas",
                etiqueta = "QR encontrados",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaEstadistica(
                valor = "$completadas",
                etiqueta = "Videos vistos",
                modifier = Modifier.weight(1f)
            )
            TarjetaEstadistica(
                valor = "$completadas",
                etiqueta = "Lugares visitados",
                modifier = Modifier.weight(1f)
            )
        }

        // --- SECCIÓN DE PROGRESO (AHORA ES CLICKEABLE) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable { onVerProgresoClick() }, // <--- NUEVO: Al tocar abre la Pantalla 12
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tu progreso",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$porcentaje%",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon( // <--- NUEVO: Flecha para indicar que es un botón
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ver detalle de progreso",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(MaterialTheme.shapes.small), // <--- NUEVO: Bordes redondeados en la barra
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "$completadas de $total puntos descubiertos",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // --- TARJETA DE DESAFÍO ACTUAL ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Desafío actual",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "Visita Plaza de Armas",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Escanea el QR del punto histórico y gana 100 puntos extra con una selfie turística.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onIrAEscaner,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Escanear QR ahora", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // --- ACCESOS RÁPIDOS ---
        Text(
            text = "Accesos rápidos",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onIrAMapa,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Icon(imageVector = Icons.Filled.Map, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Mapa")
            }

            OutlinedButton(
                onClick = onIrAColeccion,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Icon(imageVector = Icons.Filled.PhotoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Colección")
            }
        }
    }
}

@Composable
private fun TarjetaEstadistica(
    valor: String,
    etiqueta: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}