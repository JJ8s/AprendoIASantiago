package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.local.UsuarioDao
import com.example.aprendoiasantiago.data.model.UsuarioEntity
import com.example.aprendoiasantiago.data.remote.Respuesta
import com.example.aprendoiasantiago.data.remote.TipoFallo
import com.example.aprendoiasantiago.data.remote.UsuarioRemoto
import com.example.aprendoiasantiago.utils.Hash
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val usuarioDao: UsuarioDao,
    /** Termina cuando la base ya tiene sus datos iniciales. */
    private val sembradoListo: Deferred<Unit>,
    /** Microservicios (opcional): si es `null` o no responde, todo funciona solo con la base local. */
    private val servidor: ServidorRepository? = null
) {
    /** Observa en tiempo real todos los perfiles creados en el dispositivo (Pantalla 3). */
    fun observarPerfilesActivos(): Flow<List<UsuarioEntity>> =
        usuarioDao.observarPerfilesActivos()

    /** Devuelve el usuario si las credenciales son correctas, o `null` si no lo son. */
    suspend fun iniciarSesion(nombreUsuario: String, clave: String): UsuarioEntity? {
        sembradoListo.await()
        val alias = nombreUsuario.trim()
        // 1) Con red: el microservicio valida las credenciales y la app deja el perfil al día en Room.
        val remoto = servidor?.iniciarSesion(alias, clave)
        if (remoto is Respuesta.Ok) return espejarLocal(remoto.valor, clave)
        // 2) Sin red (o usuario creado solo en este teléfono): se valida contra la base local.
        return usuarioDao.buscarPorCredenciales(nombreUsuario = alias, claveHash = Hash.sha256(clave))
    }

    /** Crea o actualiza en Room al usuario que el servidor acaba de aceptar, para que el resto de la app lo use. */
    private suspend fun espejarLocal(remoto: UsuarioRemoto, clave: String): UsuarioEntity? {
        val hash = Hash.sha256(clave)
        val local = usuarioDao.buscarPorAlias(remoto.nombreUsuario)
        if (local == null) {
            usuarioDao.insertarJugador(
                UsuarioEntity(
                    nombreUsuario = remoto.nombreUsuario,
                    claveHash = hash,
                    nombreVisible = remoto.nombreVisible,
                    puntaje = remoto.puntaje,
                    correo = remoto.correo.orEmpty(),
                    telefono = remoto.telefono.orEmpty()
                )
            )
        } else {
            if (local.claveHash != hash) usuarioDao.actualizarClave(local.id, hash)
            if (remoto.puntaje > local.puntaje) usuarioDao.actualizarPuntaje(local.id, remoto.puntaje)
        }
        servidor?.sincronizarEnSegundoPlano()
        return usuarioDao.buscarPorAlias(remoto.nombreUsuario)
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

        // Con red, el alias debe ser único en el servidor. Si no hay red, se registra solo en el teléfono.
        val remoto = servidor?.registrar(nombreLimpio, aliasLimpio, clave)
        if (remoto is Respuesta.Fallo && remoto.tipo == TipoFallo.CONFLICTO) return null

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
        val alias = usuarioDao.buscarPorId(id)?.nombreUsuario
        usuarioDao.eliminarPorId(id)
        if (alias != null) servidor?.notificarEliminacion(alias)
    }



}