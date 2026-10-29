package com.example.aprendoiasantiago.ui.perfil

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.aprendoiasantiago.R
import com.example.aprendoiasantiago.data.local.NotificacionEntity
import com.example.aprendoiasantiago.domain.TipoNotificacion
import com.example.aprendoiasantiago.notificaciones.Notificador
import com.example.aprendoiasantiago.ui.components.BotonIos
import com.example.aprendoiasantiago.ui.components.DivisorIos
import com.example.aprendoiasantiago.ui.components.FilaIos
import com.example.aprendoiasantiago.ui.components.InterruptorIos
import com.example.aprendoiasantiago.ui.components.PantallaIos
import com.example.aprendoiasantiago.ui.components.SeccionIos
import com.example.aprendoiasantiago.ui.theme.IosAzul
import com.example.aprendoiasantiago.ui.theme.IosMorado
import com.example.aprendoiasantiago.ui.theme.IosNaranja
import com.example.aprendoiasantiago.ui.theme.IosRojo
import com.example.aprendoiasantiago.ui.viewmodels.AjustesPerfilUiState
import com.example.aprendoiasantiago.ui.viewmodels.MensajePerfil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Preferencias de avisos (se guardan en SQLite) y bandeja de notificaciones recibidas (Pantalla 14). */
@Composable
fun NotificacionesScreen(
    estado: AjustesPerfilUiState,
    onGuardar: (recordatorios: Boolean, nuevosPuntos: Boolean, logros: Boolean) -> Unit,
    onAvisoDePrueba: () -> Unit,
    onMarcarLeidas: () -> Unit,
    onVaciar: () -> Unit,
    onLimpiar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val ciclo = LocalLifecycleOwner.current.lifecycle
    val usuario = estado.usuario

    LaunchedEffect(Unit) { onLimpiar() }

    var sistemaActivo by remember { mutableStateOf(Notificador.puedeMostrar(contexto)) }
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) sistemaActivo = Notificador.puedeMostrar(contexto)
        }
        ciclo.addObserver(observador)
        onDispose { ciclo.removeObserver(observador) }
    }
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        sistemaActivo = Notificador.puedeMostrar(contexto)
    }

    val recordatorios = usuario?.notifRecordatorios ?: true
    val nuevosPuntos = usuario?.notifNuevosPuntos ?: true
    val logros = usuario?.notifLogros ?: true

    PantallaIos(
        titulo = stringResource(R.string.perfil_notificaciones),
        textoVolver = stringResource(R.string.perfil_titulo),
        onVolver = onVolver,
        modifier = modifier
    ) {
        SeccionIos(
            encabezado = stringResource(R.string.notif_seccion_sistema),
            nota = stringResource(R.string.notif_sistema_nota)
        ) {
            FilaIos(
                titulo = stringResource(R.string.notif_permitir),
                icono = Icons.Filled.Notifications,
                colorIcono = IosRojo,
                detalle = stringResource(if (sistemaActivo) R.string.notif_activadas else R.string.notif_desactivadas),
                extremo = {
                    if (!sistemaActivo) {
                        TextButton(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                abrirAjustesDeNotificaciones(contexto)
                            }
                        }) { Text(stringResource(R.string.notif_activar)) }
                    }
                }
            )
        }

        SeccionIos(
            encabezado = stringResource(R.string.notif_seccion_tipos),
            nota = stringResource(R.string.notif_tipos_nota)
        ) {
            FilaIos(
                titulo = stringResource(R.string.notif_tipo_recordatorios),
                icono = Icons.Filled.Schedule,
                colorIcono = IosNaranja,
                extremo = { InterruptorIos(recordatorios) { onGuardar(it, nuevosPuntos, logros) } }
            )
            DivisorIos()
            FilaIos(
                titulo = stringResource(R.string.notif_tipo_nuevos_puntos),
                icono = Icons.Filled.Place,
                colorIcono = IosAzul,
                extremo = { InterruptorIos(nuevosPuntos) { onGuardar(recordatorios, it, logros) } }
            )
            DivisorIos()
            FilaIos(
                titulo = stringResource(R.string.notif_tipo_logros),
                icono = Icons.Filled.EmojiEvents,
                colorIcono = IosMorado,
                extremo = { InterruptorIos(logros) { onGuardar(recordatorios, nuevosPuntos, it) } }
            )
        }

        BotonIos(
            texto = stringResource(R.string.notif_enviar_prueba),
            onClick = onAvisoDePrueba,
            modifier = Modifier.padding(top = 16.dp)
        )
        if (estado.mensaje == MensajePerfil.AVISO_DE_PRUEBA) {
            Text(
                text = stringResource(R.string.notif_msg_prueba),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
        }

        // --- Bandeja ---
        SeccionIos(encabezado = stringResource(R.string.notif_bandeja)) {
            if (estado.bandeja.isEmpty()) {
                Text(
                    text = stringResource(R.string.notif_vacia),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            } else {
                estado.bandeja.forEachIndexed { indice, aviso ->
                    if (indice > 0) DivisorIos(conIcono = false)
                    FilaAviso(aviso)
                }
            }
        }
        if (estado.bandeja.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onMarcarLeidas, enabled = estado.sinLeer > 0) {
                    Text(stringResource(R.string.notif_marcar_leidas))
                }
                TextButton(onClick = onVaciar) {
                    Text(stringResource(R.string.notif_vaciar), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun FilaAviso(aviso: NotificacionEntity) {
    val formato = remember { SimpleDateFormat("dd MMM · HH:mm", Locale("es", "CL")) }
    val color = when (aviso.tipo) {
        TipoNotificacion.LOGRO.name -> IosMorado
        TipoNotificacion.NUEVO_PUNTO.name -> IosAzul
        TipoNotificacion.RECORDATORIO.name -> IosNaranja
        else -> IosRojo
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(10.dp)
                .background(if (aviso.leida) androidx.compose.ui.graphics.Color.Transparent else color, CircleShape)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = aviso.titulo, style = MaterialTheme.typography.titleSmall)
            Text(text = aviso.mensaje, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = formato.format(Date(aviso.fechaMillis)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
