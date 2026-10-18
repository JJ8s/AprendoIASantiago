package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.local.CapsulaEntity
import com.example.aprendoiasantiago.data.local.PuntoTuristicoEntity

/** Lo que pasó al leer un código QR. */
sealed interface ResultadoEscaneo {

    /**
     * El código corresponde a una cápsula.
     * [puntosGanados] es 0 si el usuario ya la había completado antes (no se suman puntos dos veces).
     */
    data class Valido(
        val capsula: CapsulaEntity,
        val punto: PuntoTuristicoEntity?,
        val puntosGanados: Int
    ) : ResultadoEscaneo {
        val esNueva: Boolean get() = puntosGanados > 0
    }

    /** El texto leído no coincide con ninguna cápsula. */
    data class Desconocido(val codigo: String) : ResultadoEscaneo

    /** Falló algo inesperado (por ejemplo, la base de datos). */
    data object Error : ResultadoEscaneo
}
