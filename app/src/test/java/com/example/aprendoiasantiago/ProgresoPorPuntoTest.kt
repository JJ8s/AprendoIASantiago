package com.example.aprendoiasantiago

import com.example.aprendoiasantiago.data.local.DatosEjemplo
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity
import com.example.aprendoiasantiago.domain.ProgresoPorPunto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgresoPorPuntoTest {

    private val puntos = DatosEjemplo.puntos
    private val capsulas = DatosEjemplo.capsulas

    @Test
    fun sinAvance_todosLosPuntosEstanPendientes() {
        val resultado = ProgresoPorPunto.calcular(puntos, capsulas, emptySet())

        assertEquals(puntos.size, resultado.size)
        assertTrue(resultado.none { it.completado })
        assertTrue(resultado.all { it.capsulasCompletadas == 0 })
    }

    @Test
    fun alCompletarLaCapsulaDeUnPunto_ese_puntoQuedaCompletado_yLosDemasNo() {
        val capsula = capsulas.first()

        val resultado = ProgresoPorPunto.calcular(puntos, capsulas, setOf(capsula.id))

        val delPunto = resultado.first { it.punto.id == capsula.puntoId }
        assertTrue(delPunto.completado)
        assertEquals(1, delPunto.capsulasCompletadas)
        assertEquals(1, resultado.count { it.completado })
    }

    @Test
    fun puntoSinCapsulas_nuncaSeConsideraCompletado() {
        val sinCapsulas = PuntoTuristicoEntity(
            id = 99, nombre = "Punto vacío", descripcion = "", latitud = -33.44, longitud = -70.65, altitud = 540.0
        )

        val resultado = ProgresoPorPunto.calcular(listOf(sinCapsulas), capsulas, emptySet())

        assertEquals(0, resultado.single().capsulasTotal)
        assertFalse(resultado.single().completado)
    }

    @Test
    fun puntoConVariasCapsulas_soloEstaCompletadoCuandoSeCompletanTodas() {
        val punto = puntos.first()
        val dosCapsulas = listOf(
            capsulas[0].copy(id = 101, puntoId = punto.id),
            capsulas[1].copy(id = 102, puntoId = punto.id)
        )

        val unaSola = ProgresoPorPunto.calcular(listOf(punto), dosCapsulas, setOf(101L)).single()
        val ambas = ProgresoPorPunto.calcular(listOf(punto), dosCapsulas, setOf(101L, 102L)).single()

        assertFalse(unaSola.completado)
        assertEquals(1, unaSola.capsulasCompletadas)
        assertTrue(ambas.completado)
    }

    @Test
    fun capsulasConEstado_marcaSoloLasCompletadas() {
        val ids = setOf(capsulas[0].id, capsulas[2].id)

        val resultado = ProgresoPorPunto.capsulasConEstado(capsulas, ids)

        assertEquals(capsulas.size, resultado.size)
        assertEquals(ids, resultado.filter { it.completada }.map { it.capsula.id }.toSet())
    }
}
