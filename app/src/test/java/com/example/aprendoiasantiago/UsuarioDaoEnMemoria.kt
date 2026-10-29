package com.example.aprendoiasantiago

import com.example.aprendoiasantiago.data.local.NotificacionDao
import com.example.aprendoiasantiago.data.local.NotificacionEntity
import com.example.aprendoiasantiago.data.local.UsuarioDao
import com.example.aprendoiasantiago.data.local.UsuarioEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** DAO de usuarios en memoria para probar el perfil sin Room ni Android. */
class UsuarioDaoEnMemoria(vararg iniciales: UsuarioEntity) : UsuarioDao {
    val filas = MutableStateFlow(iniciales.associateBy { it.id })

    private fun cambiar(id: Long, f: (UsuarioEntity) -> UsuarioEntity) {
        val actual = filas.value[id] ?: return
        filas.value = filas.value + (id to f(actual))
    }

    override suspend fun buscarPorCredenciales(nombreUsuario: String, claveHash: String): UsuarioEntity? =
        filas.value.values.firstOrNull {
            it.nombreUsuario.equals(nombreUsuario, ignoreCase = true) && it.claveHash == claveHash
        }

    override fun observarPorId(id: Long): Flow<UsuarioEntity?> = filas.map { it[id] }
    override fun observarRanking(): Flow<List<UsuarioEntity>> = filas.map { it.values.sortedByDescending { u -> u.puntaje } }
    override fun observarPerfilesActivos(): Flow<List<UsuarioEntity>> =
        filas.map { it.values.filter { u -> u.claveHash.isNotEmpty() } }
    override suspend fun contar(): Int = filas.value.size
    override suspend fun insertarTodos(usuarios: List<UsuarioEntity>) = Unit
    override suspend fun insertarJugador(usuario: UsuarioEntity): Long = -1L
    override suspend fun eliminarPorId(id: Long) { filas.value = filas.value - id }
    override suspend fun buscarPorId(id: Long): UsuarioEntity? = filas.value[id]
    override suspend fun actualizarDatos(id: Long, nombre: String, correo: String, telefono: String) =
        cambiar(id) { it.copy(nombreVisible = nombre, correo = correo, telefono = telefono) }
    override suspend fun actualizarClave(id: Long, claveHash: String) = cambiar(id) { it.copy(claveHash = claveHash) }
    override suspend fun actualizarAvatarIndice(id: Long, indice: Int) =
        cambiar(id) { it.copy(avatarIndice = indice, avatarUri = null) }
    override suspend fun actualizarAvatarUri(id: Long, uri: String?) = cambiar(id) { it.copy(avatarUri = uri) }
    override suspend fun actualizarNotificaciones(
        id: Long, recordatorios: Boolean, nuevosPuntos: Boolean, logros: Boolean
    ) = cambiar(id) {
        it.copy(notifRecordatorios = recordatorios, notifNuevosPuntos = nuevosPuntos, notifLogros = logros)
    }
    override suspend fun actualizarPermisos(id: Long, ubicacion: Boolean, camara: Boolean) =
        cambiar(id) { it.copy(permitirUbicacion = ubicacion, permitirCamara = camara) }
}

/** DAO de notificaciones en memoria. */
class NotificacionDaoEnMemoria : NotificacionDao {
    val filas = MutableStateFlow<List<NotificacionEntity>>(emptyList())

    override fun observar(usuarioId: Long): Flow<List<NotificacionEntity>> =
        filas.map { lista -> lista.filter { it.usuarioId == usuarioId }.sortedByDescending { it.fechaMillis } }

    override suspend fun insertar(notificacion: NotificacionEntity): Long {
        val id = filas.value.size + 1L
        filas.value = filas.value + notificacion.copy(id = id)
        return id
    }

    override suspend fun marcarTodasLeidas(usuarioId: Long) {
        filas.value = filas.value.map { if (it.usuarioId == usuarioId) it.copy(leida = true) else it }
    }

    override suspend fun borrarTodas(usuarioId: Long) {
        filas.value = filas.value.filter { it.usuarioId != usuarioId }
    }
}
