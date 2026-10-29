package com.example.aprendoiasantiago.ui.perfil

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.aprendoiasantiago.R
import com.example.aprendoiasantiago.data.local.UsuarioEntity
import com.example.aprendoiasantiago.ui.components.DivisorIos
import com.example.aprendoiasantiago.ui.components.FilaIos
import com.example.aprendoiasantiago.ui.components.InterruptorIos
import com.example.aprendoiasantiago.ui.components.PantallaIos
import com.example.aprendoiasantiago.ui.components.SeccionIos
import com.example.aprendoiasantiago.ui.theme.IosAzul
import com.example.aprendoiasantiago.ui.theme.IosGris
import com.example.aprendoiasantiago.ui.theme.IosVerde

private val PERMISOS_UBICACION = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

/**
 * Permisos de cámara y ubicación. Android no deja que una app revoque sus propios permisos, así que cada
 * uno tiene (1) un interruptor de la app, guardado en SQLite, que pausa su uso dentro de Aprendo IA, y
 * (2) el estado real del permiso del sistema, con acceso directo a los Ajustes para revocarlo del todo.
 */
@Composable
fun PermisosScreen(
    usuario: UsuarioEntity?,
    onGuardarPermisos: (ubicacion: Boolean, camara: Boolean) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val ciclo = LocalLifecycleOwner.current.lifecycle

    var camaraSistema by remember { mutableStateOf(tieneCamara(contexto)) }
    var ubicacionSistema by remember { mutableStateOf(tieneUbicacion(contexto)) }

    // Al volver de Ajustes se vuelve a leer el estado real de los permisos.
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                camaraSistema = tieneCamara(contexto)
                ubicacionSistema = tieneUbicacion(contexto)
            }
        }
        ciclo.addObserver(observador)
        onDispose { ciclo.removeObserver(observador) }
    }

    val pedirCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        camaraSistema = tieneCamara(contexto)
    }
    val pedirUbicacion = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        ubicacionSistema = tieneUbicacion(contexto)
    }

    val ubicacionActiva = usuario?.permitirUbicacion ?: true
    val camaraActiva = usuario?.permitirCamara ?: true

    PantallaIos(
        titulo = stringResource(R.string.perfil_permisos),
        textoVolver = stringResource(R.string.perfil_titulo),
        onVolver = onVolver,
        modifier = modifier
    ) {

        SeccionIos(
            encabezado = stringResource(R.string.permisos_ubicacion),
            nota = stringResource(R.string.permisos_ubicacion_nota)
        ) {
            FilaIos(
                titulo = stringResource(R.string.permisos_usar_en_app),
                icono = Icons.Filled.LocationOn,
                colorIcono = IosAzul,
                extremo = {
                    InterruptorIos(ubicacionActiva) { activar ->
                        onGuardarPermisos(activar, camaraActiva)
                        if (activar && !ubicacionSistema) pedirUbicacion.launch(PERMISOS_UBICACION)
                    }
                }
            )
            DivisorIos()
            FilaIos(
                titulo = stringResource(R.string.permisos_estado_sistema),
                detalle = stringResource(if (ubicacionSistema) R.string.permisos_concedido else R.string.permisos_no_concedido)
            )
        }

        SeccionIos(
            encabezado = stringResource(R.string.permisos_camara),
            nota = stringResource(R.string.permisos_camara_nota)
        ) {
            FilaIos(
                titulo = stringResource(R.string.permisos_usar_en_app),
                icono = Icons.Filled.CameraAlt,
                colorIcono = IosVerde,
                extremo = {
                    InterruptorIos(camaraActiva) { activar ->
                        onGuardarPermisos(ubicacionActiva, activar)
                        if (activar && !camaraSistema) pedirCamara.launch(Manifest.permission.CAMERA)
                    }
                }
            )
            DivisorIos()
            FilaIos(
                titulo = stringResource(R.string.permisos_estado_sistema),
                detalle = stringResource(if (camaraSistema) R.string.permisos_concedido else R.string.permisos_no_concedido)
            )
        }

        SeccionIos(nota = stringResource(R.string.permisos_ajustes_nota)) {
            FilaIos(
                titulo = stringResource(R.string.permisos_abrir_ajustes),
                icono = Icons.Filled.Settings,
                colorIcono = IosGris,
                conFlecha = true,
                onClick = { abrirAjustesDeLaApp(contexto) }
            )
        }
    }
}

private fun tieneCamara(contexto: Context): Boolean =
    ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

private fun tieneUbicacion(contexto: Context): Boolean = PERMISOS_UBICACION.any {
    ContextCompat.checkSelfPermission(contexto, it) == PackageManager.PERMISSION_GRANTED
}
