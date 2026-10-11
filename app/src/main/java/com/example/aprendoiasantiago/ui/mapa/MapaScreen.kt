package com.example.aprendoiasantiago.ui.mapa

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.aprendoiasantiago.R
import com.example.aprendoiasantiago.data.local.PuntoTuristicoEntity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

private val CENTRO_SANTIAGO = GeoPoint(-33.4372, -70.6506)
private const val ZOOM_INICIAL = 14.0
private const val ZOOM_AL_CENTRAR = 17.0

private val PERMISOS_UBICACION = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

/**
 * Mapa interactivo de OpenStreetMap (osmdroid, gratuito y sin API key).
 * Muestra un marcador por cada punto turístico guardado en Room y, si el usuario da el permiso,
 * su ubicación actual. Tocar un marcador abre su descripción.
 */
@Composable
fun MapaScreen(puntos: List<PuntoTuristicoEntity>, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    val ciclo = LocalLifecycleOwner.current.lifecycle

    val vistaMapa = remember { crearMapView(contexto) }
    val capaUbicacion = remember(vistaMapa) {
        MyLocationNewOverlay(GpsMyLocationProvider(contexto), vistaMapa).also {
            vistaMapa.overlays.add(it)
        }
    }

    var permisoConcedido by remember { mutableStateOf(tienePermisoUbicacion(contexto)) }
    var yaPidioPermiso by rememberSaveable { mutableStateOf(false) }
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        permisoConcedido = resultado.values.any { concedido -> concedido }
    }

    // Pide el permiso una sola vez al abrir el mapa (el caso exige acceso a la ubicación).
    LaunchedEffect(Unit) {
        if (!permisoConcedido && !yaPidioPermiso) {
            yaPidioPermiso = true
            lanzadorPermiso.launch(PERMISOS_UBICACION)
        }
    }

    // El MapView y el GPS siguen el ciclo de vida de la pantalla para no gastar batería en segundo plano.
    DisposableEffect(ciclo, vistaMapa, capaUbicacion, permisoConcedido) {
        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_RESUME -> {
                    vistaMapa.onResume()
                    if (permisoConcedido) capaUbicacion.enableMyLocation()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    capaUbicacion.disableMyLocation()
                    vistaMapa.onPause()
                }
                else -> Unit
            }
        }
        ciclo.addObserver(observador)
        onDispose {
            ciclo.removeObserver(observador)
            capaUbicacion.disableMyLocation()
        }
    }
    DisposableEffect(vistaMapa) {
        onDispose { vistaMapa.onDetach() }
    }

    val textoBuscando = stringResource(R.string.mapa_buscando_ubicacion)

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { vistaMapa },
            update = { mapa -> mostrarMarcadores(mapa, puntos, contexto) },
            modifier = Modifier.fillMaxSize()
        )

        FloatingActionButton(
            onClick = {
                if (!permisoConcedido) {
                    lanzadorPermiso.launch(PERMISOS_UBICACION)
                } else {
                    val ubicacion = capaUbicacion.myLocation
                    if (ubicacion != null) {
                        vistaMapa.controller.setZoom(ZOOM_AL_CENTRAR)
                        vistaMapa.controller.animateTo(ubicacion)
                    } else {
                        Toast.makeText(contexto, textoBuscando, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = stringResource(R.string.mapa_mi_ubicacion)
            )
        }
    }
}

private fun tienePermisoUbicacion(contexto: Context): Boolean =
    PERMISOS_UBICACION.any { permiso ->
        ContextCompat.checkSelfPermission(contexto, permiso) == PackageManager.PERMISSION_GRANTED
    }

private fun crearMapView(contexto: Context): MapView {
    val config = Configuration.getInstance()
    // Los servidores de OpenStreetMap exigen identificar la app; sin esto bloquean las teselas.
    config.userAgentValue = contexto.packageName
    // Caché de teselas en el almacenamiento interno de la app (no requiere permisos de archivos).
    val base = File(contexto.cacheDir, "osmdroid")
    config.osmdroidBasePath = base
    config.osmdroidTileCache = File(base, "tiles")

    return MapView(contexto).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        controller.setZoom(ZOOM_INICIAL)
        controller.setCenter(CENTRO_SANTIAGO)
    }
}

/** Reemplaza los marcadores del mapa por los de [puntos], sin tocar la capa de ubicación. */
private fun mostrarMarcadores(mapa: MapView, puntos: List<PuntoTuristicoEntity>, contexto: Context) {
    mapa.overlays.removeAll { capa -> capa is Marker }
    puntos.forEach { punto ->
        val marcador = Marker(mapa).apply {
            position = GeoPoint(punto.latitud, punto.longitud, punto.altitud)
            title = punto.nombre
            snippet = punto.descripcion
            subDescription = contexto.getString(
                R.string.mapa_coordenadas,
                punto.latitud,
                punto.longitud,
                punto.altitud
            )
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapa.overlays.add(marcador)
    }
    mapa.invalidate()
}
