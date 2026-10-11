package com.example.aprendoiasantiago.ui.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aprendoiasantiago.R
import com.example.aprendoiasantiago.ui.viewmodels.PerfilUiState

@Composable
fun PerfilScreen(
    estado: PerfilUiState,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.perfil_titulo),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(12.dp))

        estado.usuario?.let { usuario ->
            Text(text = usuario.nombreVisible, style = MaterialTheme.typography.headlineMedium)
            Text(text = "@${usuario.nombreUsuario}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.perfil_puntaje, usuario.puntaje),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(
                    R.string.perfil_progreso,
                    estado.completadas,
                    estado.totalCapsulas
                ),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.perfil_ranking),
            style = MaterialTheme.typography.titleMedium
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(items = estado.ranking, key = { _, usuario -> usuario.id }) { indice, usuario ->
                val esActual = usuario.id == estado.usuario?.id
                val nombre = if (esActual) {
                    "${usuario.nombreVisible} ${stringResource(R.string.perfil_tu)}"
                } else {
                    usuario.nombreVisible
                }
                val peso = if (esActual) FontWeight.Bold else FontWeight.Normal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.perfil_posicion, indice + 1, nombre),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = peso,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.puntos_formato, usuario.puntaje),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = peso
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = stringResource(R.string.perfil_cerrar_sesion),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
