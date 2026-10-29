package com.example.aprendoiasantiago.ubicacion

import com.example.aprendoiasantiago.domain.UbicacionUsuario

/** Respeta el interruptor "Ubicación" del perfil: si está apagado, no se consulta el GPS. */
class ProveedorUbicacionFiltrado(
    private val base: ProveedorUbicacion,
    private val permitida: suspend () -> Boolean
) : ProveedorUbicacion {
    override suspend fun actual(): UbicacionUsuario? = if (permitida()) base.actual() else null
}
