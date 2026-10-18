package com.example.aprendoiasantiago.ubicacion

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import com.example.aprendoiasantiago.domain.UbicacionUsuario
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** Entrega la ubicación actual del usuario. Es una interfaz para poder reemplazarla en las pruebas. */
interface ProveedorUbicacion {
    /** Devuelve la ubicación, o `null` si no hay permiso, el GPS está apagado o no responde a tiempo. */
    suspend fun actual(): UbicacionUsuario?
}

/**
 * Usa el LocationManager de Android (GPS del móvil, sin servicios de pago ni librerías extra).
 * Primero reutiliza una posición reciente; si no hay, pide una nueva y espera unos segundos.
 */
class ProveedorUbicacionAndroid(context: Context) : ProveedorUbicacion {

    private val app = context.applicationContext
    private val manager = app.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission") // El permiso se revisa en tienePermiso() antes de usar el GPS.
    override suspend fun actual(): UbicacionUsuario? {
        if (!tienePermiso()) return null
        return try {
            val activos = manager.getProviders(true)
            if (activos.isEmpty()) return null

            val conocidas = activos.mapNotNull { manager.getLastKnownLocation(it) }
            val reciente = conocidas
                .filter { System.currentTimeMillis() - it.time <= EDAD_MAXIMA_MS }
                .maxByOrNull { it.time }

            val ubicacion = reciente
                ?: pedirNueva(elegirProveedor(activos))
                ?: conocidas.maxByOrNull { it.time } // Último recurso: la última posición conocida, aunque sea vieja.
            ubicacion?.aUbicacionUsuario()
        } catch (e: SecurityException) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun pedirNueva(proveedor: String): Location? =
        withTimeoutOrNull(ESPERA_MAXIMA_MS) {
            suspendCancellableCoroutine { continuacion ->
                val senal = CancellationSignal()
                continuacion.invokeOnCancellation { senal.cancel() }
                LocationManagerCompat.getCurrentLocation(
                    manager,
                    proveedor,
                    senal,
                    ContextCompat.getMainExecutor(app)
                ) { ubicacion -> continuacion.resume(ubicacion) }
            }
        }

    private fun elegirProveedor(activos: List<String>): String = when {
        LocationManager.GPS_PROVIDER in activos -> LocationManager.GPS_PROVIDER
        LocationManager.NETWORK_PROVIDER in activos -> LocationManager.NETWORK_PROVIDER
        else -> activos.first()
    }

    private fun tienePermiso(): Boolean = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).any { ContextCompat.checkSelfPermission(app, it) == PackageManager.PERMISSION_GRANTED }

    private fun Location.aUbicacionUsuario() = UbicacionUsuario(
        latitud = latitude,
        longitud = longitude,
        altitud = if (hasAltitude()) altitude else null
    )

    private companion object {
        const val EDAD_MAXIMA_MS = 2 * 60 * 1000L
        const val ESPERA_MAXIMA_MS = 4_000L
    }
}
