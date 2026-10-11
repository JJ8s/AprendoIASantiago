package com.example.aprendoiasantiago

import android.content.Context
import com.example.aprendoiasantiago.data.local.AppDatabase
import com.example.aprendoiasantiago.data.local.SembradoInicial
import com.example.aprendoiasantiago.data.repository.AuthRepository
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

/**
 * Crea y entrega las dependencias de la app (inyección manual, sin librerías extra).
 * Vive tanto como el proceso de la aplicación.
 */
class AppContainer(context: Context, appScope: CoroutineScope) {
    private val db = AppDatabase.crear(context)

    /** Se lanza al crear el contenedor; quien necesite los datos iniciales hace `await()`. */
    private val sembradoListo = appScope.async { SembradoInicial(db).ejecutarSiVacio() }

    val sesion = SesionRepository()
    val auth = AuthRepository(db.usuarioDao(), sembradoListo)
    val juego = JuegoRepository(
        puntoDao = db.puntoDao(),
        capsulaDao = db.capsulaDao(),
        usuarioDao = db.usuarioDao(),
        progresoDao = db.progresoDao()
    )
}
