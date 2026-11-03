package com.example.aprendoiasantiago.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Guarda en memoria quién tiene la sesión iniciada (un solo usuario a la vez). */
class SesionRepository {
    private val _usuarioId = MutableStateFlow<Long?>(null)
    val usuarioId: StateFlow<Long?> = _usuarioId.asStateFlow()

    fun iniciar(usuarioId: Long) {
        _usuarioId.value = usuarioId
    }

    fun cerrar() {
        _usuarioId.value = null
    }
}
