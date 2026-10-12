package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.local.CapsulaDao
import com.example.aprendoiasantiago.data.local.CapsulaEntity
import com.example.aprendoiasantiago.data.local.ProgresoDao
import com.example.aprendoiasantiago.data.local.PuntoDao
import com.example.aprendoiasantiago.data.local.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.local.UsuarioDao
import com.example.aprendoiasantiago.data.local.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/** Acceso a los datos del juego: puntos, cápsulas, ranking, avance y lectura de códigos QR. */
class JuegoRepository(
    private val puntoDao: PuntoDao,
    private val capsulaDao: CapsulaDao,
    private val usuarioDao: UsuarioDao,
    private val progresoDao: ProgresoDao
) {
    val puntos: Flow<List<PuntoTuristicoEntity>> = puntoDao.observarTodos()
    val capsulas: Flow<List<CapsulaEntity>> = capsulaDao.observarTodas()
    val cantidadCapsulas: Flow<Int> = capsulaDao.observarCantidad()
    val ranking: Flow<List<UsuarioEntity>> = usuarioDao.observarRanking()

    fun usuario(id: Long): Flow<UsuarioEntity?> = usuarioDao.observarPorId(id)

    fun capsulasCompletadas(usuarioId: Long): Flow<Int> = progresoDao.observarCompletadas(usuarioId)

    suspend fun obtenerCapsula(id: Long): CapsulaEntity? = capsulaDao.buscarPorId(id)

    suspend fun obtenerPunto(id: Long): PuntoTuristicoEntity? = puntoDao.buscarPorId(id)

    /**
     * Valida el texto de un QR. Si corresponde a una cápsula, la marca como completada
     * y suma sus puntos al usuario (solo la primera vez).
     */
    suspend fun procesarCodigo(usuarioId: Long, codigoLeido: String): ResultadoEscaneo {
        val codigo = codigoLeido.trim()
        val capsula = capsulaDao.buscarPorCodigo(codigo)
            ?: return ResultadoEscaneo.Desconocido(codigo)

        val esNueva = progresoDao.completarCapsula(usuarioId, capsula.id, capsula.puntos)
        return ResultadoEscaneo.Valido(
            capsula = capsula,
            punto = puntoDao.buscarPorId(capsula.puntoId),
            puntosGanados = if (esNueva) capsula.puntos else 0
        )
    }
}
