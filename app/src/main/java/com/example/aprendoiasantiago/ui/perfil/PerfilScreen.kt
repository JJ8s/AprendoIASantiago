package com.example.aprendoiasantiago.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aprendoiasantiago.R
import com.example.aprendoiasantiago.ui.components.AvatarUsuario
import com.example.aprendoiasantiago.ui.components.DivisorIos
import com.example.aprendoiasantiago.ui.components.FilaIos
import com.example.aprendoiasantiago.ui.components.SeccionIos
import com.example.aprendoiasantiago.ui.theme.IosAzul
import com.example.aprendoiasantiago.ui.theme.IosMorado
import com.example.aprendoiasantiago.ui.theme.IosNaranja
import com.example.aprendoiasantiago.ui.theme.IosRojo
import com.example.aprendoiasantiago.ui.theme.IosVerde
import com.example.aprendoiasantiago.ui.viewmodels.PerfilUiState

/** Pantalla 15: perfil y configuración. Es el "menú" desde el que se llega a cada ajuste. */
@Composable
fun PerfilScreen(
    estado: PerfilUiState,
    sinLeer: Int,
    onEditarPerfil: () -> Unit,
    onNotificaciones: () -> Unit,
    onPermisos: () -> Unit,
    onAyuda: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmandoSalida by rememberSaveable { mutableStateOf(false) }
    val usuario = estado.usuario
    val posicion = estado.ranking.indexOfFirst { it.id == usuario?.id } + 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.perfil_titulo),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp, start = 4.dp)
        )

        if (usuario != null) {
            // Tarjeta de cabecera: avatar, nombre y alias (toda la tarjeta abre "Editar perfil").
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clickable(onClick = onEditarPerfil)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AvatarUsuario(usuario, tamano = 72.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = usuario.nombreVisible,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "@${usuario.nombreUsuario}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (usuario.correo.isNotBlank()) {
                            Text(
                                text = usuario.correo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            SeccionIos(encabezado = stringResource(R.string.perfil_seccion_resumen)) {
                FilaIos(
                    titulo = stringResource(R.string.perfil_dato_puntaje),
                    detalle = stringResource(R.string.puntos_formato, usuario.puntaje)
                )
                DivisorIos(conIcono = false)
                FilaIos(
                    titulo = stringResource(R.string.perfil_dato_capsulas),
                    detalle = stringResource(R.string.perfil_dato_capsulas_valor, estado.completadas, estado.totalCapsulas)
                )
                if (posicion > 0) {
                    DivisorIos(conIcono = false)
                    FilaIos(
                        titulo = stringResource(R.string.perfil_dato_posicion),
                        detalle = "#$posicion"
                    )
                }
            }
        }

        SeccionIos(encabezado = stringResource(R.string.perfil_seccion_cuenta)) {
            FilaIos(
                titulo = stringResource(R.string.perfil_editar),
                icono = Icons.Filled.Person,
                colorIcono = IosAzul,
                conFlecha = true,
                onClick = onEditarPerfil
            )
            DivisorIos()
            FilaIos(
                titulo = stringResource(R.string.perfil_cambiar_foto),
                icono = Icons.Filled.CameraAlt,
                colorIcono = IosMorado,
                conFlecha = true,
                onClick = onEditarPerfil
            )
        }

        SeccionIos(encabezado = stringResource(R.string.perfil_seccion_preferencias)) {
            FilaIos(
                titulo = stringResource(R.string.perfil_notificaciones),
                icono = Icons.Filled.Notifications,
                colorIcono = IosRojo,
                detalle = if (sinLeer > 0) sinLeer.toString() else null,
                conFlecha = true,
                onClick = onNotificaciones
            )
            DivisorIos()
            FilaIos(
                titulo = stringResource(R.string.perfil_permisos),
                icono = Icons.Filled.Lock,
                colorIcono = IosVerde,
                conFlecha = true,
                onClick = onPermisos
            )
            DivisorIos()
            FilaIos(
                titulo = stringResource(R.string.perfil_ayuda),
                icono = Icons.Filled.Help,
                colorIcono = IosNaranja,
                conFlecha = true,
                onClick = onAyuda
            )
        }

        if (estado.ranking.isNotEmpty()) {
            SeccionIos(encabezado = stringResource(R.string.perfil_ranking)) {
                estado.ranking.take(LIMITE_RANKING).forEachIndexed { indice, jugador ->
                    val esActual = jugador.id == usuario?.id
                    if (indice > 0) DivisorIos(conIcono = false)
                    FilaIos(
                        titulo = stringResource(
                            R.string.perfil_posicion,
                            indice + 1,
                            if (esActual) "${jugador.nombreVisible} ${stringResource(R.string.perfil_tu)}" else jugador.nombreVisible
                        ),
                        detalle = stringResource(R.string.puntos_formato, jugador.puntaje),
                        colorTitulo = if (esActual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        SeccionIos {
            FilaIos(
                titulo = stringResource(R.string.perfil_cerrar_sesion),
                colorTitulo = MaterialTheme.colorScheme.error,
                onClick = { confirmandoSalida = true }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (confirmandoSalida) {
        AlertDialog(
            onDismissRequest = { confirmandoSalida = false },
            title = { Text(stringResource(R.string.perfil_cerrar_titulo)) },
            text = { Text(stringResource(R.string.perfil_cerrar_texto)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmandoSalida = false
                    onCerrarSesion()
                }) {
                    Text(
                        stringResource(R.string.perfil_cerrar_sesion),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoSalida = false }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }
}

private const val LIMITE_RANKING = 5
