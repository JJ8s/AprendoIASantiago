package com.example.aprendoiasantiago.ui.punto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aprendoiasantiago.R
import com.example.aprendoiasantiago.domain.CapsulaDelPunto
import com.example.aprendoiasantiago.domain.DetallePunto
import com.example.aprendoiasantiago.ui.components.BotonIos
import com.example.aprendoiasantiago.ui.components.BotonSecundarioIos
import com.example.aprendoiasantiago.ui.components.DivisorIos
import com.example.aprendoiasantiago.ui.components.FilaIos
import com.example.aprendoiasantiago.ui.components.PantallaIos
import com.example.aprendoiasantiago.ui.components.SeccionIos
import com.example.aprendoiasantiago.ui.theme.IosGris
import com.example.aprendoiasantiago.ui.theme.IosVerde
import com.example.aprendoiasantiago.ui.viewmodels.PuntoDetalleUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Pantalla 6: detalle de un punto turístico (descripción, estado del QR, distancia y contenido asociado). */
@Composable
fun PuntoDetalleScreen(
    estado: PuntoDetalleUiState,
    onVolver: () -> Unit,
    onIrAlEscaner: () -> Unit,
    onVerEnMapa: () -> Unit,
    onVerCapsula: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val detalle = estado.detalle
    PantallaIos(
        titulo = stringResource(R.string.punto_titulo),
        textoVolver = stringResource(R.string.destino_mapa),
        onVolver = onVolver,
        modifier = modifier
    ) {
        when {
            estado.cargando -> Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            detalle == null -> Text(
                text = stringResource(R.string.punto_no_encontrado),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(48.dp)
            )

            else -> Contenido(detalle, onIrAlEscaner, onVerEnMapa, onVerCapsula)
        }
    }
}

@Composable
private fun Contenido(
    detalle: DetallePunto,
    onIrAlEscaner: () -> Unit,
    onVerEnMapa: () -> Unit,
    onVerCapsula: (Long) -> Unit
) {
    // Cabecera: nombre y estado del QR
    Column(modifier = Modifier.padding(top = 8.dp, start = 4.dp)) {
        Text(text = detalle.punto.nombre, style = MaterialTheme.typography.titleLarge)
        EtiquetaEstado(detalle)
    }

    SeccionIos(encabezado = stringResource(R.string.punto_seccion_info)) {
        Text(
            text = detalle.punto.descripcion,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )
        DivisorIos(conIcono = false)
        FilaIos(
            titulo = stringResource(R.string.punto_distancia),
            detalle = detalle.distanciaMetros?.let { formatearDistancia(it) }
                ?: stringResource(R.string.punto_distancia_no_disponible)
        )
        DivisorIos(conIcono = false)
        FilaIos(
            titulo = stringResource(R.string.punto_coordenadas),
            detalle = stringResource(R.string.punto_coordenadas_valor, detalle.punto.latitud, detalle.punto.longitud)
        )
    }

    SeccionIos(
        encabezado = stringResource(R.string.punto_seccion_capsulas),
        nota = if (detalle.completado) null else stringResource(R.string.punto_nota_pendiente)
    ) {
        if (detalle.capsulas.isEmpty()) {
            Text(
                text = stringResource(R.string.punto_sin_capsulas),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
        }
        detalle.capsulas.forEachIndexed { indice, item ->
            if (indice > 0) DivisorIos()
            FilaCapsula(item, onVerCapsula)
        }
    }

    if (detalle.total > 0) {
        SeccionIos(encabezado = stringResource(R.string.punto_seccion_puntos)) {
            FilaIos(
                titulo = stringResource(R.string.punto_puntos_obtenidos),
                detalle = stringResource(R.string.punto_puntos_valor, detalle.puntosObtenidos, detalle.puntosPosibles)
            )
        }
    }

    Column(
        modifier = Modifier.padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BotonIos(texto = stringResource(R.string.punto_ir_escaner), onClick = onIrAlEscaner)
        BotonSecundarioIos(texto = stringResource(R.string.punto_ver_mapa), onClick = onVerEnMapa)
    }
}

@Composable
private fun EtiquetaEstado(detalle: DetallePunto) {
    val color = if (detalle.completado) IosVerde else MaterialTheme.colorScheme.tertiary
    Surface(
        shape = RoundedCornerShape(50),
        color = color,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            text = stringResource(
                if (detalle.completado) R.string.punto_estado_completado else R.string.punto_estado_pendiente,
                detalle.completadas,
                detalle.total
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun FilaCapsula(item: CapsulaDelPunto, onVerCapsula: (Long) -> Unit) {
    val formato = remember { SimpleDateFormat("dd MMM yyyy", Locale("es", "CL")) }
    if (item.completada) {
        FilaIos(
            titulo = item.capsula.titulo,
            icono = Icons.Filled.PlayCircle,
            colorIcono = IosVerde,
            detalle = item.fechaMillis?.let { formato.format(Date(it)) }
                ?: stringResource(R.string.punto_puntos_cap, item.capsula.puntos),
            conFlecha = true,
            onClick = { onVerCapsula(item.capsula.id) }
        )
    } else {
        // Sin escanear el QR en el lugar no se revela el video: se muestra bloqueada.
        FilaIos(
            titulo = stringResource(R.string.punto_capsula_bloqueada),
            icono = Icons.Filled.Lock,
            colorIcono = IosGris,
            detalle = stringResource(R.string.punto_puntos_cap, item.capsula.puntos),
            colorTitulo = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 350 → "350 m", 1250 → "1,3 km". */
internal fun formatearDistancia(metros: Double): String =
    if (metros < 1000) "${Math.round(metros)} m"
    else String.format(Locale("es", "CL"), "%.1f km", metros / 1000)
