package com.example.aprendoiasantiago.domain

import com.example.aprendoiasantiago.data.local.RutaEntity
import com.example.aprendoiasantiago.data.local.RutaPuntoEntity

/** Posición del usuario al completar una cápsula. La altitud puede faltar si el GPS no la entrega. */
data class UbicacionUsuario(
    val latitud: Double,
    val longitud: Double,
    val altitud: Double?
)

/** Una ruta con sus puntos en orden de recorrido y el avance del usuario en ellos. */
data class RutaConPuntos(
    val ruta: RutaEntity,
    val puntos: List<PuntoConEstado>
) {
    val total: Int get() = puntos.size
    val completados: Int get() = puntos.count { it.completado }
    val completa: Boolean get() = total > 0 && completados == total
}

object Rutas {

    /** Une rutas, sus paradas y el estado de cada punto. Los puntos que ya no existen se omiten. */
    fun calcular(
        rutas: List<RutaEntity>,
        paradas: List<RutaPuntoEntity>,
        puntos: List<PuntoConEstado>
    ): List<RutaConPuntos> {
        val puntosPorId = puntos.associateBy { it.punto.id }
        val paradasPorRuta = paradas.groupBy { it.rutaId }
        return rutas.map { ruta ->
            val ordenadas = paradasPorRuta[ruta.id].orEmpty()
                .sortedBy { it.orden }
                .mapNotNull { puntosPorId[it.puntoId] }
            RutaConPuntos(ruta, ordenadas)
        }
    }
}
