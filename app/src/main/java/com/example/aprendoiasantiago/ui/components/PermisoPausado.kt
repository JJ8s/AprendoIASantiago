package com.example.aprendoiasantiago.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aprendoiasantiago.R

/** Aviso que reemplaza a la cámara cuando el jugador la apagó en Perfil → Permisos. */
@Composable
fun CamaraPausada(onIrAPermisos: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.permiso_pausado_camara_titulo),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.permiso_pausado_camara_texto),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Button(onClick = onIrAPermisos, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.permiso_pausado_boton), style = MaterialTheme.typography.labelLarge)
        }
    }
}
