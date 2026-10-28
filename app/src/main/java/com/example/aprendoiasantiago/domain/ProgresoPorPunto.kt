package com.example.aprendoiasantiago.domain

import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity

/** Un punto turístico junto con cuántas de sus cápsulas completó el usuario. */
data class PuntoConEstado(
    val punto: PuntoTuristicoEntity,
    val capsulasTotal: Int,
    val capsulasCompletadas: Int
) {
    /** Un punto está completado cuando tiene cápsulas y el usuario las completó todas. */
    val completado: Boolean get() = capsulasTotal > 0 && capsulasCompletadas == capsulasTotal
}

/** Una cápsula con su estado de avance para el usuario (completada o pendiente). */
data class CapsulaConEstado(
    val capsula: CapsulaEntity,
    val completada: Boolean
)

/** Cruza puntos, cápsulas y avance del usuario (lógica pura, fácil de probar). */
object ProgresoPorPunto {

    fun calcular(
        puntos: List<PuntoTuristicoEntity>,
        capsulas: List<CapsulaEntity>,
        idsCompletadas: Set<Long>
    ): List<PuntoConEstado> {
        val capsulasPorPunto = capsulas.groupBy { it.puntoId }
        return puntos.map { punto ->
            val delPunto = capsulasPorPunto[punto.id].orEmpty()
            PuntoConEstado(
                punto = punto,
                capsulasTotal = delPunto.size,
                capsulasCompletadas = delPunto.count { it.id in idsCompletadas }
            )
        }
    }

    fun capsulasConEstado(
        capsulas: List<CapsulaEntity>,
        idsCompletadas: Set<Long>
    ): List<CapsulaConEstado> =
        capsulas.map { CapsulaConEstado(it, completada = it.id in idsCompletadas) }
}
