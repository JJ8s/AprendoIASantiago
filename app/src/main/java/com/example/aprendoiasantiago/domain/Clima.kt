package com.example.aprendoiasantiago.domain

/** Condición del tiempo, agrupada desde los códigos WMO que entrega Open-Meteo. */
enum class CondicionClima {
    DESPEJADO, PARCIAL, NUBLADO, NIEBLA, LLOVIZNA, LLUVIA, NIEVE, TORMENTA, DESCONOCIDA;

    companion object {
        fun desdeCodigo(codigo: Int): CondicionClima = when (codigo) {
            0 -> DESPEJADO
            1, 2 -> PARCIAL
            3 -> NUBLADO
            45, 48 -> NIEBLA
            in 51..57 -> LLOVIZNA
            in 61..67, in 80..82 -> LLUVIA
            in 71..77, 85, 86 -> NIEVE
            in 95..99 -> TORMENTA
            else -> DESCONOCIDA
        }
    }
}

enum class ConsejoClima { PARAGUAS, ABRIGO, HIDRATARSE, TODO_BIEN }

/** Clima actual en un punto turístico. */
data class ClimaPunto(
    val temperatura: Double,
    val sensacion: Double,
    val vientoKmh: Double,
    val condicion: CondicionClima
) {
    /** Recomendación para quien va a recorrer el lugar. */
    val consejo: ConsejoClima
        get() = when {
            condicion in setOf(CondicionClima.LLOVIZNA, CondicionClima.LLUVIA, CondicionClima.TORMENTA, CondicionClima.NIEVE) ->
                ConsejoClima.PARAGUAS
            sensacion <= 8.0 -> ConsejoClima.ABRIGO
            temperatura >= 30.0 -> ConsejoClima.HIDRATARSE
            else -> ConsejoClima.TODO_BIEN
        }
}
