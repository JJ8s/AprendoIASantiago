package com.example.aprendoiasantiago

import com.example.aprendoiasantiago.domain.ResultadoRostro
import com.example.aprendoiasantiago.domain.ValidadorRostro
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidadorRostroTest {

    @Test
    fun sinRostros_esSinRostro() {
        assertEquals(ResultadoRostro.SIN_ROSTRO, ValidadorRostro.evaluar(emptyList(), 1000))
    }

    @Test
    fun dosRostros_esVariosRostros() {
        assertEquals(ResultadoRostro.VARIOS_ROSTROS, ValidadorRostro.evaluar(listOf(400, 300), 1000))
    }

    @Test
    fun rostroPequeno_esRostroLejos() {
        assertEquals(ResultadoRostro.ROSTRO_LEJOS, ValidadorRostro.evaluar(listOf(150), 1000))
    }

    @Test
    fun rostroJustoEnElMinimo_esValido() {
        assertEquals(ResultadoRostro.VALIDO, ValidadorRostro.evaluar(listOf(200), 1000))
    }

    @Test
    fun unRostroGrande_esValido() {
        assertEquals(ResultadoRostro.VALIDO, ValidadorRostro.evaluar(listOf(500), 1000))
    }

    @Test
    fun anchoDeImagenInvalido_noExplota() {
        assertEquals(ResultadoRostro.SIN_ROSTRO, ValidadorRostro.evaluar(listOf(100), 0))
    }
}
