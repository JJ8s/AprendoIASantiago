package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.local.NotificacionDao
import com.example.aprendoiasantiago.data.local.NotificacionEntity
import com.example.aprendoiasantiago.data.local.UsuarioDao
import com.example.aprendoiasantiago.domain.TipoNotificacion
import com.example.aprendoiasantiago.notificaciones.EmisorNotificaciones
import kotlinx.coroutines.flow.Flow

/**
 * Bandeja de notificaciones por jugador. Un aviso solo se guarda (y se muestra en la barra del
 * sistema) si el jugador tiene activado ese tipo en su perfil.
 */
class NotificacionesRepository(
    private val dao: NotificacionDao,
    private val usuarioDao: UsuarioDao,
    private val emisor: EmisorNotificaciones,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    fun observar(usuarioId: Long): Flow<List<NotificacionEntity>> = dao.observar(usuarioId)

    /** Devuelve `true` si el aviso se registró, `false` si el jugador tiene ese tipo desactivado. */
    suspend fun registrar(usuarioId: Long, tipo: TipoNotificacion, titulo: String, mensaje: String): Boolean {
        val usuario = usuarioDao.buscarPorId(usuarioId) ?: return false
        if (!tipo.estaActivaPara(usuario)) return false
        dao.insertar(
            NotificacionEntity(
                usuarioId = usuarioId,
                titulo = titulo,
                mensaje = mensaje,
                tipo = tipo.name,
                fechaMillis = reloj()
            )
        )
        emisor.mostrar(titulo, mensaje)
        return true
    }

    suspend fun marcarTodasLeidas(usuarioId: Long) = dao.marcarTodasLeidas(usuarioId)

    suspend fun vaciar(usuarioId: Long) = dao.borrarTodas(usuarioId)
}
