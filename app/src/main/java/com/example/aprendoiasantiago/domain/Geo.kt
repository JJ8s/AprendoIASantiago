package com.example.aprendoiasantiago.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Cálculos geográficos simples (sin Android, fáciles de probar). */
object Geo {
    private const val RADIO_TIERRA_METROS = 6_371_000.0

    /** Distancia en metros sobre la superficie terrestre entre dos coordenadas (fórmula de Haversine). */
    fun distanciaMetros(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val f1 = Math.toRadians(lat1)
        val f2 = Math.toRadians(lat2)
        val df = Math.toRadians(lat2 - lat1)
        val dl = Math.toRadians(lon2 - lon1)
        val a = sin(df / 2) * sin(df / 2) + cos(f1) * cos(f2) * sin(dl / 2) * sin(dl / 2)
        return RADIO_TIERRA_METROS * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}