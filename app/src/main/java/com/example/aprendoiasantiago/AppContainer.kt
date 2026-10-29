package com.example.aprendoiasantiago

import android.content.Context
import com.example.aprendoiasantiago.data.local.AlmacenEvidencias
import com.example.aprendoiasantiago.data.local.AppDatabase
import com.example.aprendoiasantiago.data.local.SembradoInicial
import com.example.aprendoiasantiago.data.repository.AuthRepository
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.NotificacionesRepository
import com.example.aprendoiasantiago.data.repository.PerfilRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.ml.DetectorRostro
import com.example.aprendoiasantiago.ml.MlKitDetectorRostro
import com.example.aprendoiasantiago.ubicacion.ProveedorUbicacion
import com.example.aprendoiasantiago.notificaciones.Notificador
import com.example.aprendoiasantiago.ubicacion.ProveedorUbicacionAndroid
import com.example.aprendoiasantiago.ubicacion.ProveedorUbicacionFiltrado
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import java.io.File

/**
 * Crea y entrega las dependencias de la app (inyección manual, sin librerías extra).
 * Vive tanto como el proceso de la aplicación.
 */
class AppContainer(context: Context, appScope: CoroutineScope) {
    private val db = AppDatabase.crear(context)

    /** Se lanza al crear el contenedor; quien necesite los datos iniciales hace `await()`. */
    private val sembradoListo = appScope.async { SembradoInicial(db).ejecutar() }

    val sesion = SesionRepository()
    val auth = AuthRepository(db.usuarioDao(), sembradoListo)
    val almacenEvidencias = AlmacenEvidencias(File(context.filesDir, "evidencias"))
    val detectorRostro: DetectorRostro = MlKitDetectorRostro()
    val almacenAvatares = AlmacenEvidencias(File(context.filesDir, "avatares"))
    val perfil = PerfilRepository(db.usuarioDao())
    val notificaciones = NotificacionesRepository(db.notificacionDao(), db.usuarioDao(), Notificador(context))

    /** El GPS solo se consulta si el jugador activo no apagó "Ubicación" en su perfil. */
    val ubicacion: ProveedorUbicacion = ProveedorUbicacionFiltrado(ProveedorUbicacionAndroid(context)) {
        val id = sesion.usuarioId.value
        id == null || perfil.ubicacionPermitida(id)
    }

    val juego = JuegoRepository(
        puntoDao = db.puntoDao(),
        capsulaDao = db.capsulaDao(),
        usuarioDao = db.usuarioDao(),
        progresoDao = db.progresoDao(),
        rutaDao = db.rutaDao()
    )
}
