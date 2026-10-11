package com.example.aprendoiasantiago.ui.escaner

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
import com.example.aprendoiasantiago.data.local.CapsulaEntity
import com.example.aprendoiasantiago.ui.components.EncabezadoPantalla

/** Etapa 1: lista las cápsulas y sus códigos QR de prueba. La cámara llega en la etapa 2. */
@Composable
fun EscanerScreen(capsulas: List<CapsulaEntity>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            EncabezadoPantalla(
                titulo = stringResource(R.string.escaner_titulo),
                aviso = stringResource(R.string.escaner_aviso)
            )
        }
        items(items = capsulas, key = { it.id }) { capsula ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = capsula.codigoQr, style = MaterialTheme.typography.titleMedium)
                    Text(text = capsula.titulo, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = stringResource(
                            R.string.escaner_capsula,
                            capsula.urlVideo,
                            capsula.puntos
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
