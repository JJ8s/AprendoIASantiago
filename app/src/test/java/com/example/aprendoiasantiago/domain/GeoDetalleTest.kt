package com.example.aprendoiasantiago.domain

import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class GeoDetalleTest : StringSpec({

    "la distancia entre dos puntos iguales es cero" {
        Geo.distanciaMetros(-33.44, -70.65, -33.44, -70.65) shouldBe (0.0 plusOrMinus 0.001)
    }

    "Plaza de Armas a La Moneda son unos 650 metros" {
        Geo.distanciaMetros(-33.4372, -70.6506, -33.4429, -70.6537) shouldBe (650.0 plusOrMinus 100.0)
    }

    "un grado de longitud en el ecuador son unos 111 km y es simétrica" {
        val ida = Geo.distanciaMetros(0.0, 0.0, 0.0, 1.0)
        ida shouldBe (111_195.0 plusOrMinus 200.0)
        Geo.distanciaMetros(0.0, 1.0, 0.0, 0.0) shouldBe (ida plusOrMinus 0.001)
    }

    val plaza = PuntoTuristicoEntity(id = 1, nombre = "Plaza", descripcion = "d", latitud = -33.4372, longitud = -70.6506, altitud = 540.0)
    val c1 = CapsulaEntity(id = 10, puntoId = 1, titulo = "A", urlVideo = "u", codigoQr = "Q1", puntos = 10)
    val c2 = CapsulaEntity(id = 11, puntoId = 1, titulo = "B", urlVideo = "u", codigoQr = "Q2", puntos = 15)
    val deOtro = CapsulaEntity(id = 12, puntoId = 2, titulo = "C", urlVideo = "u", codigoQr = "Q3", puntos = 99)

    "armar el detalle usa solo las cápsulas del punto y pone la fecha solo a las completadas" {
        val d = DetallePuntos.armar(plaza, listOf(c1, c2, deOtro), setOf(10L), mapOf(10L to 5_000L, 11L to 6_000L), null)

        d.capsulas.map { it.capsula.id } shouldBe listOf(10L, 11L)
        d.capsulas[0].fechaMillis shouldBe 5_000L
        d.capsulas[1].fechaMillis.shouldBeNull()
        d.completadas shouldBe 1
        d.total shouldBe 2
        d.puntosObtenidos shouldBe 10
        d.puntosPosibles shouldBe 25
        d.completado shouldBe false
    }

    "el punto queda completado cuando todas sus cápsulas están hechas" {
        DetallePuntos.armar(plaza, listOf(c1, c2), setOf(10L, 11L), emptyMap(), null).completado shouldBe true
    }

    "un punto sin cápsulas nunca está completado" {
        DetallePuntos.armar(plaza, emptyList(), emptySet(), emptyMap(), null).completado shouldBe false
    }

    "con ubicación calcula la distancia y sin ella queda null" {
        DetallePuntos.armar(plaza, emptyList(), emptySet(), emptyMap(), UbicacionUsuario(-33.4372, -70.6406, null))
            .distanciaMetros.shouldNotBeNull()
        DetallePuntos.armar(plaza, emptyList(), emptySet(), emptyMap(), null).distanciaMetros.shouldBeNull()
    }
})
