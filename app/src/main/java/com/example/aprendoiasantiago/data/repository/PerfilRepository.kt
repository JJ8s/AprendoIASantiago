package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.local.UsuarioDao
import com.example.aprendoiasantiago.data.local.UsuarioEntity
import com.example.aprendoiasantiago.domain.ErrorPerfil
import com.example.aprendoiasantiago.domain.ValidadorPerfil
import com.example.aprendoiasantiago.utils.Hash
import kotlinx.coroutines.flow.Flow

/** Lee y modifica los datos de perfil y configuración del jugador (todo persiste en SQLite). */
class PerfilRepository(private val usuarioDao: UsuarioDao) {

    fun usuario(id: Long): Flow<UsuarioEntity?> = usuarioDao.observarPorId(id)

    /** Guarda nombre, correo y teléfono. Devuelve `null` si todo salió bien, o el error de validación. */
    suspend fun guardarDatos(id: Long, nombre: String, correo: String, telefono: String): ErrorPerfil? {
        ValidadorPerfil.validarDatos(nombre, correo, telefono)?.let { return it }
        usuarioDao.actualizarDatos(id, nombre.trim(), correo.trim(), telefono.trim())
        return null
    }

    /** Cambia la contraseña tras comprobar la actual. Devuelve `null` si salió bien. */
    suspend fun cambiarClave(id: Long, actual: String, nueva: String, confirmacion: String): ErrorPerfil? {
        ValidadorPerfil.validarClave(actual, nueva, confirmacion)?.let { return it }
        val usuario = usuarioDao.buscarPorId(id) ?: return ErrorPerfil.ERROR_INESPERADO
        if (usuario.claveHash != Hash.sha256(actual)) return ErrorPerfil.CLAVE_ACTUAL_INCORRECTA
        usuarioDao.actualizarClave(id, Hash.sha256(nueva))
        return null
    }

    /** Devuelve la foto anterior (para poder borrar el archivo) o `null` si no había. */
    suspend fun elegirAvatar(id: Long, indice: Int): String? {
        val anterior = usuarioDao.buscarPorId(id)?.avatarUri
        usuarioDao.actualizarAvatarIndice(id, indice)
        return anterior
    }

    /** Guarda la foto de avatar y devuelve la anterior (para borrarla del almacenamiento). */
    suspend fun usarFotoAvatar(id: Long, uri: String): String? {
        val anterior = usuarioDao.buscarPorId(id)?.avatarUri
        usuarioDao.actualizarAvatarUri(id, uri)
        return anterior
    }

    suspend fun guardarNotificaciones(id: Long, recordatorios: Boolean, nuevosPuntos: Boolean, logros: Boolean) =
        usuarioDao.actualizarNotificaciones(id, recordatorios, nuevosPuntos, logros)

    suspend fun guardarPermisos(id: Long, ubicacion: Boolean, camara: Boolean) =
        usuarioDao.actualizarPermisos(id, ubicacion, camara)

    suspend fun ubicacionPermitida(id: Long): Boolean = usuarioDao.buscarPorId(id)?.permitirUbicacion ?: true
}
