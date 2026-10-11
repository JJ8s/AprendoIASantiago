package com.example.aprendoiasantiago.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(puntos: List<PuntoTuristicoEntity>)
}

@Dao
interface CapsulaDao {
    @Query("SELECT * FROM capsulas ORDER BY id ASC")
    fun observarTodas(): Flow<List<CapsulaEntity>>

    @Query("SELECT COUNT(*) FROM capsulas")
    fun observarCantidad(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(capsulas: List<CapsulaEntity>)
}

@Dao
interface ProgresoDao {
    @Query("SELECT COUNT(*) FROM progreso WHERE usuarioId = :usuarioId AND completada = 1")
    fun observarCompletadas(usuarioId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(progreso: ProgresoEntity)
}
