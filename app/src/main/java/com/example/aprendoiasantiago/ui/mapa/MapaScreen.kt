package com.example.aprendoiasantiago.ui.mapa

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
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
import com.example.aprendoiasantiago.domain.PuntoConEstado
import com.example.aprendoiasantiago.domain.RutaConPuntos
import com.example.aprendoiasantiago.ui.viewmodels.MapaUiState
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

private val CENTRO_SANTIAGO = GeoPoint(-33.4372, -70.6506)
private const val ZOOM_INICIAL = 14.0
private const val ZOOM_AL_CENTRAR = 17.0
private const val MARGEN_RUTA_PX = 160
private const val ANCHO_LINEA_RUTA = 10f

/** Verde oscuro (buen contraste al aire libre) para los puntos ya completados. */
private const val COLOR_COMPLETADO = 0xFF2E7D32.toInt()

/** Azul fuerte para la línea de la ruta elegida. */
private const val COLOR_RUTA = 0xFF1565C0.toInt()

// Política de OpenStreetMap: el User-Agent debe nombrar la app de forma única y estable.
// No uses el nombre del paquete (com.example...) ni el valor por defecto de la librería: los bloquean.
// Opcional y recomendado: agrega al final un contacto tuyo, por ejemplo "(https://tu-sitio o correo)".
private const val USER_AGENT = "AprendoIASantiago/1.0 (proyecto academico MVP)"

// Si cambias el User-Agent o te bloquean de nuevo, cambia este nombre para empezar con una caché limpia
// (la caché guarda hasta la imagen de "Access blocked" que respondió el servidor).
private const val CARPETA_CACHE = "osmdroid_v2"

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
fun MapaScreen(
    estado: MapaUiState,
    onSeleccionarRuta: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
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
    val rutaElegida = estado.rutaSeleccionada

    // Al elegir una ruta, el mapa se acerca para que se vea completa.
    LaunchedEffect(rutaElegida?.ruta?.id) {
        val paradas = rutaElegida?.puntos.orEmpty()
        if (paradas.size >= 2) {
            val cuadro = BoundingBox.fromGeoPoints(
                paradas.map { GeoPoint(it.punto.latitud, it.punto.longitud) }
            )
            vistaMapa.zoomToBoundingBox(cuadro, true, MARGEN_RUTA_PX)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { vistaMapa },
            update = { mapa ->
                mostrarRuta(mapa, rutaElegida)
                mostrarMarcadores(mapa, estado.puntos, rutaElegida, contexto)
            },
            modifier = Modifier.fillMaxSize()
        )

        if (estado.rutas.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(top = 8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = estado.rutas, key = { "ruta-${it.ruta.id}" }) { ruta ->
                    FilterChip(
                        selected = ruta.ruta.id == estado.rutaSeleccionadaId,
                        onClick = { onSeleccionarRuta(ruta.ruta.id) },
                        label = {
                            Text(
                                stringResource(
                                    R.string.mapa_ruta_etiqueta,
                                    ruta.ruta.nombre,
                                    ruta.completados,
                                    ruta.total
                                )
                            )
                        }
                    )
                }
            }
        }

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
    config.userAgentValue = USER_AGENT
    // Caché de teselas en el almacenamiento interno de la app (no requiere permisos de archivos).
    val base = File(contexto.cacheDir, CARPETA_CACHE)
    config.osmdroidBasePath = base
    config.osmdroidTileCache = File(base, "tiles")

    return MapView(contexto).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        // Teselas más grandes en pantallas de alta densidad: se leen mejor al aire libre.
        setTilesScaledToDpi(true)
        controller.setZoom(ZOOM_INICIAL)
        controller.setCenter(CENTRO_SANTIAGO)
        // Atribución obligatoria: muestra "© OpenStreetMap contributors" sobre el mapa.
        // Va abajo a la izquierda para que el botón flotante (abajo a la derecha) no la tape.
        overlays.add(
            CopyrightOverlay(contexto).apply {
                setAlignBottom(true)
                setAlignRight(false)
            }
        )
    }
}

/**
 * Reemplaza los marcadores del mapa por los de [puntos], sin tocar la capa de ubicación.
 * Los puntos ya completados se dibujan en verde; el resto conserva el color por defecto.
 */
private fun mostrarMarcadores(
    mapa: MapView,
    puntos: List<PuntoConEstado>,
    ruta: RutaConPuntos?,
    contexto: Context
) {
    mapa.overlays.removeAll { capa -> capa is Marker }
    // Con una ruta elegida, cada parada lleva su número de visita en el título.
    val ordenPorPunto = ruta?.puntos?.withIndex()?.associate { (i, p) -> p.punto.id to i + 1 }.orEmpty()
    puntos.forEach { estado ->
        val punto = estado.punto
        val orden = ordenPorPunto[punto.id]
        val textoEstado = contexto.getString(
            if (estado.completado) R.string.mapa_estado_completado else R.string.mapa_estado_pendiente,
            estado.capsulasCompletadas,
            estado.capsulasTotal
        )
        val marcador = Marker(mapa).apply {
            position = GeoPoint(punto.latitud, punto.longitud, punto.altitud)
            title = if (orden != null) contexto.getString(R.string.mapa_ruta_parada, orden, punto.nombre) else punto.nombre
            snippet = punto.descripcion
            subDescription = contexto.getString(
                R.string.mapa_coordenadas,
                punto.latitud,
                punto.longitud,
                punto.altitud
            ) + "\n" + textoEstado
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            if (estado.completado) {
                // mutate() evita teñir también los marcadores pendientes, que comparten el mismo dibujo.
                icon = icon?.mutate()?.also { it.setTint(COLOR_COMPLETADO) }
            }
        }
        mapa.overlays.add(marcador)
    }
    mapa.invalidate()
}

/** Dibuja (o quita) la línea de la ruta elegida, uniendo sus paradas en orden. */
private fun mostrarRuta(mapa: MapView, ruta: RutaConPuntos?) {
    mapa.overlays.removeAll { capa -> capa is Polyline }
    val paradas = ruta?.puntos.orEmpty()
    if (paradas.size >= 2) {
        val linea = Polyline(mapa).apply {
            setPoints(paradas.map { GeoPoint(it.punto.latitud, it.punto.longitud) })
            outlinePaint.color = COLOR_RUTA
            outlinePaint.strokeWidth = ANCHO_LINEA_RUTA
        }
        // Índice 0: la línea queda debajo de los marcadores y de la ubicación del usuario.
        mapa.overlays.add(0, linea)
    }
    mapa.invalidate()
}
