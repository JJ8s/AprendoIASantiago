package com.example.aprendoiasantiago.domain

/** Resultado de revisar una selfie. Solo [VALIDO] permite guardarla. */
enum class ResultadoRostro { VALIDO, SIN_ROSTRO, VARIOS_ROSTROS, ROSTRO_LEJOS }

/**
 * Reglas para aceptar una selfie como evidencia: exactamente un rostro y que ocupe una parte razonable
 * de la foto. Es lógica pura (sin Android) para poder probarla; la detección real la hace ML Kit.
 */
object ValidadorRostro {

    /** El rostro debe medir al menos esta fracción del ancho de la foto. */
    const val PROPORCION_MINIMA = 0.20f

    /** [anchosRostros]: ancho en píxeles de cada rostro detectado; [anchoImagen]: ancho de la foto. */
    fun evaluar(anchosRostros: List<Int>, anchoImagen: Int): ResultadoRostro = when {
        anchosRostros.isEmpty() -> ResultadoRostro.SIN_ROSTRO
        anchosRostros.size > 1 -> ResultadoRostro.VARIOS_ROSTROS
        anchoImagen <= 0 -> ResultadoRostro.SIN_ROSTRO
        anchosRostros.single().toFloat() / anchoImagen < PROPORCION_MINIMA -> ResultadoRostro.ROSTRO_LEJOS
        else -> ResultadoRostro.VALIDO
    }
}
