package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.local.CapsulaDao
import com.example.aprendoiasantiago.data.local.CapsulaEntity
import com.example.aprendoiasantiago.data.local.ProgresoDao
import com.example.aprendoiasantiago.data.local.ProgresoEntity
import com.example.aprendoiasantiago.data.local.PuntoDao
import com.example.aprendoiasantiago.data.local.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.local.RutaDao
import com.example.aprendoiasantiago.data.local.RutaEntity
import com.example.aprendoiasantiago.data.local.RutaPuntoEntity
import com.example.aprendoiasantiago.data.local.UsuarioDao
import com.example.aprendoiasantiago.data.local.UsuarioEntity
import com.example.aprendoiasantiago.domain.UbicacionUsuario
import kotlinx.coroutines.flow.Flow

/** Acceso a los datos del juego: puntos, cápsulas, ranking, avance y lectura de códigos QR. */
class JuegoRepository(
    private val puntoDao: PuntoDao,
    private val capsulaDao: CapsulaDao,
    private val usuarioDao: UsuarioDao,
    private val progresoDao: ProgresoDao,
    private val rutaDao: RutaDao,
    /** Hora actual en milisegundos; se inyecta para poder probar con una hora fija. */
    private val reloj: () -> Long = System::currentTimeMillis
) {
    val puntos: Flow<List<PuntoTuristicoEntity>> = puntoDao.observarTodos()
    val capsulas: Flow<List<CapsulaEntity>> = capsulaDao.observarTodas()
    val rutas: Flow<List<RutaEntity>> = rutaDao.observarRutas()
    val paradasDeRutas: Flow<List<RutaPuntoEntity>> = rutaDao.observarParadas()
    val cantidadCapsulas: Flow<Int> = capsulaDao.observarCantidad()
    val ranking: Flow<List<UsuarioEntity>> = usuarioDao.observarRanking()

    fun usuario(id: Long): Flow<UsuarioEntity?> = usuarioDao.observarPorId(id)

    fun capsulasCompletadas(usuarioId: Long): Flow<Int> = progresoDao.observarCompletadas(usuarioId)

    fun idsCapsulasCompletadas(usuarioId: Long): Flow<List<Long>> =
        progresoDao.observarIdsCompletadas(usuarioId)

    suspend fun obtenerCapsula(id: Long): CapsulaEntity? = capsulaDao.buscarPorId(id)

    /** Avance del usuario en una cápsula (con sus fotos de evidencia), o `null` si aún no la completa. */
    suspend fun progreso(usuarioId: Long, capsulaId: Long): ProgresoEntity? =
        progresoDao.buscar(usuarioId, capsulaId)

    suspend fun guardarSelfie(usuarioId: Long, capsulaId: Long, uri: String) =
        progresoDao.guardarSelfie(usuarioId, capsulaId, uri)

    suspend fun guardarFotoEntorno(usuarioId: Long, capsulaId: Long, uri: String) =
        progresoDao.guardarFotoEntorno(usuarioId, capsulaId, uri)

    suspend fun obtenerPunto(id: Long): PuntoTuristicoEntity? = puntoDao.buscarPorId(id)

    /**
     * Valida el texto de un QR. Si corresponde a una cápsula, la marca como completada
     * y suma sus puntos al usuario (solo la primera vez).
     *
     * [ubicacion] se consulta solo si el código es válido (no se gasta GPS con códigos desconocidos) y
     * lo que devuelva se guarda junto con el avance: dónde estaba el usuario al completar la cápsula.
     */
    suspend fun procesarCodigo(
        usuarioId: Long,
        codigoLeido: String,
        ubicacion: suspend () -> UbicacionUsuario? = { null }
    ): ResultadoEscaneo {
        val codigo = codigoLeido.trim()
        val capsula = capsulaDao.buscarPorCodigo(codigo)
            ?: return ResultadoEscaneo.Desconocido(codigo)

        val donde = ubicacion()
        val esNueva = progresoDao.completarCapsula(
            usuarioId, capsula.id, capsula.puntos, reloj(),
            donde?.latitud, donde?.longitud, donde?.altitud
        )
        return ResultadoEscaneo.Valido(
            capsula = capsula,
            punto = puntoDao.buscarPorId(capsula.puntoId),
            puntosGanados = if (esNueva) capsula.puntos else 0
        )
    }

    // --- FUNCIONES DE ADMINISTRACIÓN (PANEL ADMIN Y GENCONT) ---

    suspend fun guardarPunto(punto: PuntoTuristicoEntity) =
        puntoDao.guardarPunto(punto)

    suspend fun eliminarPunto(id: Long) =
        puntoDao.eliminarPunto(id)

    suspend fun guardarCapsula(capsula: CapsulaEntity) =
        capsulaDao.guardarCapsula(capsula)

    suspend fun eliminarCapsula(id: Long) =
        capsulaDao.eliminarCapsula(id)
}
