package com.example.aprendoiasantiago

import android.content.Context
import com.example.aprendoiasantiago.data.local.AlmacenEvidencias
import com.example.aprendoiasantiago.data.local.GaleriaFotos
import com.example.aprendoiasantiago.data.local.AppDatabase
import com.example.aprendoiasantiago.data.local.SembradoInicial
import com.example.aprendoiasantiago.data.repository.AuthRepository
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.NotificacionesRepository
import com.example.aprendoiasantiago.data.remote.RetrofitClima
import com.example.aprendoiasantiago.data.remote.RetrofitInstance
import com.example.aprendoiasantiago.data.remote.RetrofitMicroservicios
import com.example.aprendoiasantiago.data.repository.PerfilRepository
import com.example.aprendoiasantiago.data.repository.PostRepository
import com.example.aprendoiasantiago.data.repository.ClimaRepository
import com.example.aprendoiasantiago.data.repository.ServidorRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.ml.DetectorRostro
import com.example.aprendoiasantiago.ml.MlKitDetectorRostro
import com.example.aprendoiasantiago.notificaciones.Notificador
import com.example.aprendoiasantiago.ubicacion.ProveedorUbicacion
import com.example.aprendoiasantiago.ubicacion.ProveedorUbicacionAndroid
import com.example.aprendoiasantiago.ubicacion.ProveedorUbicacionFiltrado
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File

class AppContainer(context: Context, appScope: CoroutineScope) {
    private val db = AppDatabase.crear(context)

    private val sembradoListo = appScope.async { SembradoInicial(db).ejecutar() }

    val sesion = SesionRepository()

    /** Puente con los microservicios Spring Boot (usuarios y contenido). */
    val servidor = ServidorRepository(
        usuariosApi = RetrofitMicroservicios.usuarios,
        contenidoApi = RetrofitMicroservicios.contenido,
        puntoDao = db.puntoDao(),
        capsulaDao = db.capsulaDao(),
        usuarioDao = db.usuarioDao(),
        alcance = appScope
    )
    val auth = AuthRepository(db.usuarioDao(), sembradoListo, servidor)
    val almacenEvidencias = AlmacenEvidencias(File(context.filesDir, "evidencias"), GaleriaFotos.publicador(context))
    val detectorRostro: DetectorRostro = MlKitDetectorRostro()
    val almacenAvatares = AlmacenEvidencias(File(context.filesDir, "avatares"))
    val posts = PostRepository(RetrofitInstance.api)

    /** API externa de clima (Open-Meteo): independiente de Room y de los microservicios. */
    val clima = ClimaRepository(RetrofitClima.api)
    val perfil = PerfilRepository(db.usuarioDao(), servidor)
    val notificaciones = NotificacionesRepository(db.notificacionDao(), db.usuarioDao(), Notificador(context))

    val ubicacion: ProveedorUbicacion = ProveedorUbicacionFiltrado(ProveedorUbicacionAndroid(context)) {
        val id = sesion.usuarioId.value
        id == null || perfil.ubicacionPermitida(id)
    }

    val juego = JuegoRepository(
        puntoDao = db.puntoDao(),
        capsulaDao = db.capsulaDao(),
        usuarioDao = db.usuarioDao(),
        progresoDao = db.progresoDao(),
        rutaDao = db.rutaDao(),
        servidor = servidor
    )

    init {
        // Al abrir la app: cuando la base local está lista, se trae lo último del servidor (si responde).
        appScope.launch {
            sembradoListo.await()
            servidor.sincronizar()
        }
    }
}