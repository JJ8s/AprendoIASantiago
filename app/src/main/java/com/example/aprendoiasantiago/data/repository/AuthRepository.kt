package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.local.UsuarioDao
import com.example.aprendoiasantiago.data.model.UsuarioEntity
import com.example.aprendoiasantiago.utils.Hash
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val usuarioDao: UsuarioDao,
    /** Termina cuando la base ya tiene sus datos iniciales. */
    private val sembradoListo: Deferred<Unit>
) {
    /** Observa en tiempo real todos los perfiles creados en el dispositivo (Pantalla 3). */
    fun observarPerfilesActivos(): Flow<List<UsuarioEntity>> =
        usuarioDao.observarPerfilesActivos()

    /** Devuelve el usuario si las credenciales son correctas, o `null` si no lo son. */
    suspend fun iniciarSesion(nombreUsuario: String, clave: String): UsuarioEntity? {
        sembradoListo.await()
        return usuarioDao.buscarPorCredenciales(
            nombreUsuario = nombreUsuario.trim(),
            claveHash = Hash.sha256(clave)
        )
    }

    /**
     * Registra un nuevo jugador local en SQLite (Pantalla 2).
     * Devuelve el usuario creado, o `null` si el alias ya estaba ocupado.
     */
    suspend fun registrarJugador(
        nombreVisible: String,
        alias: String,
        clave: String
    ): UsuarioEntity? {
        sembradoListo.await()
        val aliasLimpio = alias.trim()
        val nombreLimpio = nombreVisible.trim()

        val nuevoId = usuarioDao.insertarJugador(
            UsuarioEntity(
                nombreUsuario = aliasLimpio,
                claveHash = Hash.sha256(clave),
                nombreVisible = nombreLimpio,
                puntaje = 0
            )
        )
        // En Room, si OnConflictStrategy.IGNORE detecta un alias repetido, devuelve -1L
        if (nuevoId == -1L) return null

        return usuarioDao.buscarPorCredenciales(
            nombreUsuario = aliasLimpio,
            claveHash = Hash.sha256(clave)
        )
    }

    /** Elimina un jugador de la base de datos por su ID (Panel Admin) */
    suspend fun eliminarJugador(id: Long) {
        usuarioDao.eliminarPorId(id)

    }



}