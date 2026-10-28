package com.example.aprendoiasantiago.ui.screens // Ajusta tu paquete si es necesario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity
import com.example.aprendoiasantiago.domain.CapsulaConEstado

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    progreso: List<CapsulaConEstado>,
    puntos: List<PuntoTuristicoEntity>,
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF6F8FC),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Catálogo de QR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF0D1B2A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF6F8FC))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Descubre todos los puntos QR distribuidos por Santiago. ¡Encuéntralos todos!",
                    fontSize = 14.sp,
                    color = Color(0xFF5C6B73),
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                )
            }

            // Listamos todas las cápsulas disponibles en el juego
            items(progreso) { item ->
                // Buscamos el nombre del lugar cruzando el ID
                val punto = puntos.find { it.id == item.capsula.puntoId }

                TarjetaCatalogo(
                    nombreLugar = punto?.nombre ?: "Lugar desconocido",
                    tituloCapsula = item.capsula.titulo,
                    codigoQr = item.capsula.codigoQr,
                    puntosOtorga = item.capsula.puntos,
                    completada = item.completada
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun TarjetaCatalogo(
    nombreLugar: String,
    tituloCapsula: String,
    codigoQr: String,
    puntosOtorga: Int,
    completada: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de estado (Verde si está completado, Naranja si falta)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (completada) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (completada) Icons.Default.CheckCircle else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (completada) Color(0xFF2E7D32) else Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textos descriptivos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombreLugar,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0D1B2A)
                )
                Text(
                    text = tituloCapsula,
                    fontSize = 13.sp,
                    color = Color(0xFF5C6B73)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$codigoQr  •  $puntosOtorga pts",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1565C0)
                    )
                }
            }
        }
    }
}