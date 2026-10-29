package com.example.aprendoiasantiago.domain

import com.example.aprendoiasantiago.data.local.UsuarioEntity

/** Tipos de aviso que el jugador puede activar o desactivar en su perfil. */
enum class TipoNotificacion {
    RECORDATORIO,
    NUEVO_PUNTO,
    LOGRO,

    /** Aviso de prueba del sistema: se entrega siempre. */
    SISTEMA;

    /** ¿Este usuario quiere recibir avisos de este tipo? */
    fun estaActivaPara(usuario: UsuarioEntity): Boolean = when (this) {
        RECORDATORIO -> usuario.notifRecordatorios
        NUEVO_PUNTO -> usuario.notifNuevosPuntos
        LOGRO -> usuario.notifLogros
        SISTEMA -> true
    }
}
