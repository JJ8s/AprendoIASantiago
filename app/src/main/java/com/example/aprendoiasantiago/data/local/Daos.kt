package com.example.aprendoiasantiago.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query(
        "SELECT * FROM usuarios " +
            "WHERE nombreUsuario = :nombreUsuario AND claveHash = :claveHash LIMIT 1"
    )
    suspend fun buscarPorCredenciales(nombreUsuario: String, claveHash: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun observarPorId(id: Long): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios ORDER BY puntaje DESC, nombreVisible ASC")
    fun observarRanking(): Flow<List<UsuarioEntity>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(usuarios: List<UsuarioEntity>)
}

@Dao
interface PuntoDao {
    @Query("SELECT * FROM puntos_turisticos ORDER BY nombre ASC")
    fun observarTodos(): Flow<List<PuntoTuristicoEntity>>

    @Query("SELECT * FROM puntos_turisticos WHERE id = :id")
    suspend fun buscarPorId(id: Long): PuntoTuristicoEntity?

    /** Inserta o actualiza (sin borrar la fila, así no se pierde el progreso asociado). */
    @Upsert
    suspend fun guardarTodos(puntos: List<PuntoTuristicoEntity>)
}

@Dao
interface CapsulaDao {
    @Query("SELECT * FROM capsulas ORDER BY id ASC")
    fun observarTodas(): Flow<List<CapsulaEntity>>

    @Query("SELECT COUNT(*) FROM capsulas")
    fun observarCantidad(): Flow<Int>

    @Query("SELECT * FROM capsulas WHERE id = :id")
    suspend fun buscarPorId(id: Long): CapsulaEntity?

    /** El texto del QR se compara sin distinguir mayúsculas de minúsculas. */
    @Query("SELECT * FROM capsulas WHERE codigoQr = :codigo COLLATE NOCASE LIMIT 1")
    suspend fun buscarPorCodigo(codigo: String): CapsulaEntity?

    /** Inserta o actualiza (sin borrar la fila, así no se pierde el progreso asociado). */
    @Upsert
    suspend fun guardarTodas(capsulas: List<CapsulaEntity>)
}

@Dao
abstract class ProgresoDao {
    @Query("SELECT COUNT(*) FROM progreso WHERE usuarioId = :usuarioId AND completada = 1")
    abstract fun observarCompletadas(usuarioId: Long): Flow<Int>

    /** Devuelve -1 si el usuario ya había completado esa cápsula (no inserta nada). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun registrarSiNuevo(progreso: ProgresoEntity): Long

    @Query("UPDATE usuarios SET puntaje = puntaje + :puntos WHERE id = :usuarioId")
    abstract suspend fun sumarPuntos(usuarioId: Long, puntos: Int)

    /**
     * Marca la cápsula como completada y suma sus puntos, todo o nada (una sola transacción).
     * Devuelve `true` si era la primera vez; `false` si ya estaba completada (no suma puntos otra vez).
     */
    @Transaction
    open suspend fun completarCapsula(usuarioId: Long, capsulaId: Long, puntos: Int): Boolean {
        val fila = registrarSiNuevo(ProgresoEntity(usuarioId, capsulaId, completada = true))
        if (fila == -1L) return false
        sumarPuntos(usuarioId, puntos)
        return true
    }
}
