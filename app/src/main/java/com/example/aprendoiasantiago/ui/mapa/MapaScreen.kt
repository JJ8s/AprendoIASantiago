package com.example.aprendoiasantiago.ui.mapa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aprendoiasantiago.R
import com.example.aprendoiasantiago.data.local.PuntoTuristicoEntity
import com.example.aprendoiasantiago.ui.components.EncabezadoPantalla

/** Etapa 1: lista los puntos guardados en Room. El mapa interactivo llega en la etapa 2. */
@Composable
fun MapaScreen(puntos: List<PuntoTuristicoEntity>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            EncabezadoPantalla(
                titulo = stringResource(R.string.mapa_titulo),
                aviso = stringResource(R.string.mapa_aviso)
            )
        }
        items(items = puntos, key = { it.id }) { punto ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = punto.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(text = punto.descripcion, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = stringResource(
                            R.string.mapa_coordenadas,
                            punto.latitud,
                            punto.longitud,
                            punto.altitud
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
