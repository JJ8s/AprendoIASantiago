package com.example.aprendoiasantiago.domain

import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity

/** Una cápsula del punto, con su estado para el jugador actual. */
data class CapsulaDelPunto(
    val capsula: CapsulaEntity,
    val completada: Boolean,
    /** Cuándo se completó (solo si [completada]). */
    val fechaMillis: Long?
)

/** Todo lo que muestra la pantalla "Detalle del punto" (Pantalla 6). */
data class DetallePunto(
    val punto: PuntoTuristicoEntity,
    val capsulas: List<CapsulaDelPunto>,
    /** Distancia aproximada desde el jugador, o `null` si no hay ubicación disponible. */
    val distanciaMetros: Double?
) {
    val completadas: Int get() = capsulas.count { it.completada }
    val total: Int get() = capsulas.size

    /** Completado = tiene cápsulas y el jugador las completó todas (igual criterio que el mapa). */
    val completado: Boolean get() = total > 0 && completadas == total

    val puntosObtenidos: Int get() = capsulas.filter { it.completada }.sumOf { it.capsula.puntos }
    val puntosPosibles: Int get() = capsulas.sumOf { it.capsula.puntos }
}

object DetallePuntos {
    fun armar(
        punto: PuntoTuristicoEntity,
        todasLasCapsulas: List<CapsulaEntity>,
        idsCompletadas: Set<Long>,
        fechas: Map<Long, Long?>,
        ubicacion: UbicacionUsuario?
    ): DetallePunto = DetallePunto(
        punto = punto,
        capsulas = todasLasCapsulas
            .filter { it.puntoId == punto.id }
            .map { c ->
                val hecha = c.id in idsCompletadas
                CapsulaDelPunto(c, hecha, if (hecha) fechas[c.id] else null)
            },
        distanciaMetros = ubicacion?.let {
            Geo.distanciaMetros(it.latitud, it.longitud, punto.latitud, punto.longitud)
        }
    )
}
