package com.example.aprendoiasantiago

import com.example.aprendoiasantiago.data.local.DatosEjemplo
import com.example.aprendoiasantiago.data.local.RutaEntity
import com.example.aprendoiasantiago.data.local.RutaPuntoEntity
import com.example.aprendoiasantiago.domain.ProgresoPorPunto
import com.example.aprendoiasantiago.domain.Rutas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RutasTest {

    private fun puntosConEstado(idsCompletadas: Set<Long> = emptySet()) =
        ProgresoPorPunto.calcular(DatosEjemplo.puntos, DatosEjemplo.capsulas, idsCompletadas)

    @Test
    fun lasParadas_seOrdenanPorElCampoOrden_noPorElOrdenDeLaLista() {
        val ruta = RutaEntity(1, "Prueba", "")
        val desordenadas = listOf(
            RutaPuntoEntity(1, puntoId = 3, orden = 3),
            RutaPuntoEntity(1, puntoId = 1, orden = 1),
            RutaPuntoEntity(1, puntoId = 2, orden = 2)
        )

        val resultado = Rutas.calcular(listOf(ruta), desordenadas, puntosConEstado()).single()

        assertEquals(listOf(1L, 2L, 3L), resultado.puntos.map { it.punto.id })
    }

    @Test
    fun siUnPuntoYaNoExiste_seOmiteSinRomperLaRuta() {
        val ruta = RutaEntity(1, "Prueba", "")
        val paradas = listOf(RutaPuntoEntity(1, 1, 1), RutaPuntoEntity(1, 999, 2))

        val resultado = Rutas.calcular(listOf(ruta), paradas, puntosConEstado()).single()

        assertEquals(1, resultado.total)
    }

    @Test
    fun elAvanceDeLaRuta_cuentaLosPuntosCompletados() {
        // La cápsula 4 está en el punto 4 (La Moneda), primera parada de la ruta 1.
        val resultado = Rutas.calcular(
            DatosEjemplo.rutas, DatosEjemplo.paradas, puntosConEstado(setOf(4L))
        ).first { it.ruta.id == 1L }

        assertEquals(1, resultado.completados)
        assertEquals(4, resultado.total)
        assertFalse(resultado.completa)
    }

    @Test
    fun rutaConTodosSusPuntosCompletados_quedaCompleta() {
        val todas = DatosEjemplo.capsulas.map { it.id }.toSet()

        val resultado = Rutas.calcular(DatosEjemplo.rutas, DatosEjemplo.paradas, puntosConEstado(todas))

        assertTrue(resultado.all { it.completa })
    }

    @Test
    fun rutaSinParadas_nuncaEstaCompleta() {
        val resultado = Rutas.calcular(
            listOf(RutaEntity(5, "Vacía", "")), emptyList(), puntosConEstado()
        ).single()

        assertFalse(resultado.completa)
    }

    @Test
    fun losDatosDeEjemplo_tienenRutasConPuntosExistentes() {
        val idsPuntos = DatosEjemplo.puntos.map { it.id }.toSet()
        val idsRutas = DatosEjemplo.rutas.map { it.id }.toSet()

        assertTrue(DatosEjemplo.paradas.all { it.puntoId in idsPuntos && it.rutaId in idsRutas })
        assertTrue(DatosEjemplo.rutas.all { r -> DatosEjemplo.paradas.count { it.rutaId == r.id } >= 2 })
    }
}
