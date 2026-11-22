package com.example.aprendoiasantiago.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity
import com.example.aprendoiasantiago.domain.CapsulaConEstado
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Tipos de evento para el filtro
enum class FiltroHistorial(val titulo: String) {
    TODOS("Todos"), QR("QR"), VIDEOS("Videos"), SELFIES("Selfies")
}

// Estructura de datos para un evento en la línea de tiempo
data class EventoHistorial(
    val id: String,
    val titulo: String,
    val subtitulo: String,
    val hora: String,
    val puntos: String,
    val tipo: FiltroHistorial,
    val fechaMillis: Long,
    val icono: ImageVector,
    val colorFondo: Color,
    val colorIcono: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    progreso: List<CapsulaConEstado>,
    puntos: List<PuntoTuristicoEntity>,
    onBackClick: () -> Unit
) {
    var filtroSeleccionado by remember { mutableStateOf(FiltroHistorial.TODOS) }

    // 1. Generar la lista de eventos a partir del progreso completado
    val eventos = remember(progreso, puntos) {
        val listaGenerada = mutableListOf<EventoHistorial>()
        val formatoHora = SimpleDateFormat("HH:mm", Locale("es", "CL"))

        progreso.filter { it.completada }.forEach { item ->
            val punto = puntos.find { it.id == item.capsula.puntoId }
            val nombreLugar = punto?.nombre ?: "Lugar desconocido"
            // Como la BD vieja no tenía fechaMillis, usamos la actual si es null
            val fechaM = item.fechaMillis ?: System.currentTimeMillis()
            val horaStr = formatoHora.format(Date(fechaM))

            // Evento: QR Escaneado
            listaGenerada.add(
                EventoHistorial(
                    id = "qr_${item.capsula.id}",
                    titulo = "QR escaneado",
                    subtitulo = nombreLugar,
                    hora = horaStr,
                    puntos = "+10 pts",
                    tipo = FiltroHistorial.QR,
                    fechaMillis = fechaM,
                    icono = Icons.Default.QrCode2,
                    colorFondo = Color(0xFFE8F5E9),
                    colorIcono = Color(0xFF2E7D32)
                )
            )

            // Evento: Video Visto (Asumimos que vio el video al escanear)
            listaGenerada.add(
                EventoHistorial(
                    id = "vid_${item.capsula.id}",
                    titulo = "Video visto",
                    subtitulo = item.capsula.titulo,
                    hora = horaStr,
                    puntos = "",
                    tipo = FiltroHistorial.VIDEOS,
                    fechaMillis = fechaM + 1000,
                    icono = Icons.Default.PlayArrow,
                    colorFondo = Color(0xFFE3F2FD),
                    colorIcono = Color(0xFF1565C0)
                )
            )

            // Evento: Selfie (Si tomó foto)
            if (item.selfieUri != null) {
                listaGenerada.add(
                    EventoHistorial(
                        id = "selfie_${item.capsula.id}",
                        titulo = "Selfie turística",
                        subtitulo = nombreLugar,
                        hora = horaStr,
                        puntos = "+100 pts",
                        tipo = FiltroHistorial.SELFIES,
                        fechaMillis = fechaM + 2000,
                        icono = Icons.Default.CameraAlt,
                        colorFondo = Color(0xFFE3F2FD),
                        colorIcono = Color(0xFF1565C0)
                    )
                )
            }
        }
        listaGenerada.sortedByDescending { it.fechaMillis }
    }

    // 2. Filtrar y agrupar por fecha
    val eventosFiltrados = if (filtroSeleccionado == FiltroHistorial.TODOS) {
        eventos
    } else {
        eventos.filter { it.tipo == filtroSeleccionado }
    }

    val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL"))
    val eventosAgrupados = eventosFiltrados.groupBy { formatoFecha.format(Date(it.fechaMillis)) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Historial", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Chips de filtro
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroHistorial.entries.forEach { filtro ->
                    val seleccionado = filtro == filtroSeleccionado
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (seleccionado) MaterialTheme.colorScheme.primary else Color(0xFFF0F0F0),
                        modifier = Modifier
                            .clickable { filtroSeleccionado = filtro }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = filtro.titulo,
                            color = if (seleccionado) Color.White else Color.DarkGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Lista de Historial
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                eventosAgrupados.forEach { (fecha, listaEventos) ->
                    item {
                        Text(
                            text = fecha,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }

                    items(listaEventos) { evento ->
                        ItemTimeline(evento)
                    }
                }
            }
        }
    }
}

@Composable
fun ItemTimeline(evento: EventoHistorial) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Columna izquierda: Línea y círculo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0))
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFF0F0F0))
            )
        }

        // Tarjeta del evento
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(evento.colorFondo),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = evento.icono,
                        contentDescription = null,
                        tint = evento.colorIcono,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = evento.titulo,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = evento.subtitulo,
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Text(
                        text = evento.hora,
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                if (evento.puntos.isNotEmpty()) {
                    Text(
                        text = evento.puntos,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}