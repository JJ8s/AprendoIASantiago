package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.local.CapsulaDao
import com.example.aprendoiasantiago.data.local.CapsulaEntity
import com.example.aprendoiasantiago.data.local.ProgresoDao
import com.example.aprendoiasantiago.data.local.PuntoDao
import com.example.aprendoiasantiago.data.local.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.local.UsuarioDao
import com.example.aprendoiasantiago.data.local.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/** Acceso de solo lectura (por ahora) a los datos del juego: puntos, cápsulas, ranking y avance. */
class JuegoRepository(
    puntoDao: PuntoDao,
    capsulaDao: CapsulaDao,
    private val usuarioDao: UsuarioDao,
    private val progresoDao: ProgresoDao
) {
    val puntos: Flow<List<PuntoTuristicoEntity>> = puntoDao.observarTodos()
    val capsulas: Flow<List<CapsulaEntity>> = capsulaDao.observarTodas()
    val cantidadCapsulas: Flow<Int> = capsulaDao.observarCantidad()
    val ranking: Flow<List<UsuarioEntity>> = usuarioDao.observarRanking()

    fun usuario(id: Long): Flow<UsuarioEntity?> = usuarioDao.observarPorId(id)

    fun capsulasCompletadas(usuarioId: Long): Flow<Int> = progresoDao.observarCompletadas(usuarioId)
}
