package com.example.aprendoiasantiago.ui.coleccion


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aprendoiasantiago.ui.viewmodels.PerfilUiState

/**
 * Representa las 3 pestañas de filtro que pide la Pantalla 11 del PDF.
 */
private enum class FiltroColeccion {
    TODOS, VIDEOS, TURISTICOS
}

/**
 * Modelo de datos para representar cada cápsula/QR en la galería de descubrimientos.
 */
private data class DescubrimientoItem(
    val capsulaId: Long,
    val tituloVideo: String,
    val lugar: String,
    val puntos: Int,
    val fecha: String,
    val esTuristico: Boolean
)

@Composable
fun ColeccionScreen(
    estado: PerfilUiState,
    onVerCapsula: (Long) -> Unit,
    onIrAEscaner: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Estado para saber qué pestaña de filtro está activa ("Todos" por defecto)
    var filtroActual by remember { mutableStateOf(FiltroColeccion.TODOS) }

    // 2. Catálogo base de las 7 cápsulas de Santiago sincronizado con los IDs de SQLite
    val catalogoSantiago = remember {
        listOf(
            DescubrimientoItem(1L, "Introducción a la IA", "Plaza de Armas", 10, "17/10/2026", true),
            DescubrimientoItem(2L, "IA en la Vida Diaria", "Cerro Santa Lucía", 10, "17/10/2026", true),
            DescubrimientoItem(3L, "Robótica e IA", "Parque Forestal", 10, "17/10/2026", false),
            DescubrimientoItem(4L, "IA y el Medio Ambiente", "Museo Bellas Artes", 10, "17/10/2026", true),
            DescubrimientoItem(5L, "Futuro de la IA", "Barrio Lastarria", 10, "17/10/2026", false),
            DescubrimientoItem(6L, "Machine Learning Básico", "La Moneda", 10, "17/10/2026", true),
            DescubrimientoItem(7L, "Ética en la IA", "Biblioteca Nacional", 10, "17/10/2026", false)
        )
    }

    // 3. Tomamos la cantidad real de códigos QR que el jugador ya desbloqueó en SQLite
    val descubiertos = catalogoSantiago.take(estado.completadas)
    val videosDescubiertos = descubiertos
    val turisticosDescubiertos = descubiertos.filter { it.esTuristico }

    // 4. Filtramos la lista que se mostrará en pantalla según el botón presionado
    val listaMostrada = when (filtroActual) {
        FiltroColeccion.TODOS -> descubiertos
        FiltroColeccion.VIDEOS -> videosDescubiertos
        FiltroColeccion.TURISTICOS -> turisticosDescubiertos
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- TÍTULO DE LA PANTALLA ---
        Text(
            text = "Mis Descubrimientos",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // --- BARRA DE FILTROS (Todos, Videos, Turísticos) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BotonFiltro(
                texto = "Todos (${descubiertos.size})",
                seleccionado = filtroActual == FiltroColeccion.TODOS,
                onClick = { filtroActual = FiltroColeccion.TODOS },
                modifier = Modifier.weight(1f)
            )
            BotonFiltro(
                texto = "Videos (${videosDescubiertos.size})",
                seleccionado = filtroActual == FiltroColeccion.VIDEOS,
                onClick = { filtroActual = FiltroColeccion.VIDEOS },
                modifier = Modifier.weight(1f)
            )
            BotonFiltro(
                texto = "Turísticos (${turisticosDescubiertos.size})",
                seleccionado = filtroActual == FiltroColeccion.TURISTICOS,
                onClick = { filtroActual = FiltroColeccion.TURISTICOS },
                modifier = Modifier.weight(1f)
            )
        }

        // --- CONTENIDO DE LA GALERÍA ---
        if (listaMostrada.isEmpty()) {
            // Si el jugador aún no tiene descubrimientos en esta categoría, mostramos aviso
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Aún no tienes descubrimientos aquí",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Recorre Santiago y escanea los códigos QR para desbloquear cápsulas de Inteligencia Artificial.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = onIrAEscaner,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Ir al Escáner QR")
                    }
                }
            }
        } else {
            // Dibujamos una tarjeta por cada QR descubierto
            listaMostrada.forEach { item ->
                TarjetaDescubrimiento(
                    item = item,
                    onVerVideo = { onVerCapsula(item.capsulaId) }
                )
            }
        }
    }
}

/**
 * Botón reutilizable que cambia de aspecto si está seleccionado o no.
 */
@Composable
private fun BotonFiltro(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (seleccionado) {
        Button(
            onClick = onClick,
            modifier = modifier.height(42.dp)
        ) {
            Text(text = texto, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(42.dp)
        ) {
            Text(text = texto, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
    }
}

/**
 * Tarjeta individual de cada cápsula descubierta (Miniatura + Datos + Acceso al video).
 */
@Composable
private fun TarjetaDescubrimiento(
    item: DescubrimientoItem,
    onVerVideo: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Miniatura representativa del punto/video
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.esTuristico) Icons.Filled.Map else Icons.Filled.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Información del descubrimiento (Nombre del video, lugar, puntos y fecha)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.tituloVideo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.lugar,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+${item.puntos} pts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.fecha,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Botón para abrir la cápsula educativa (Pantalla 9 del PDF)
            OutlinedButton(
                onClick = onVerVideo,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Ver cápsula educativa")
            }
        }
    }
}