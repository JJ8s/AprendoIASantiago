package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.remote.ClimaActualRemoto
import com.example.aprendoiasantiago.data.remote.ClimaApi
import com.example.aprendoiasantiago.data.remote.ClimaRespuesta
import com.example.aprendoiasantiago.data.remote.Respuesta
import com.example.aprendoiasantiago.data.remote.TipoFallo
import com.example.aprendoiasantiago.domain.CondicionClima
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.io.IOException

class ClimaRepositoryTest : StringSpec({

    "convierte la respuesta de la API externa en el modelo de la app" {
        runTest {
            val api = mockk<ClimaApi>()
            coEvery { api.actual(-33.44, -70.65) } returns ClimaRespuesta(ClimaActualRemoto(21.5, 20.0, 12.0, 61))

            val r = ClimaRepository(api).obtener(-33.44, -70.65) as Respuesta.Ok

            r.valor.temperatura shouldBe 21.5
            r.valor.vientoKmh shouldBe 12.0
            r.valor.condicion shouldBe CondicionClima.LLUVIA
        }
    }

    "una respuesta sin el bloque current es un fallo del servicio" {
        runTest {
            val api = mockk<ClimaApi>()
            coEvery { api.actual(any(), any()) } returns ClimaRespuesta(null)

            (ClimaRepository(api).obtener(0.0, 0.0) as Respuesta.Fallo).tipo shouldBe TipoFallo.SERVIDOR
        }
    }

    "sin internet devuelve SIN_CONEXION" {
        runTest {
            val api = mockk<ClimaApi>()
            coEvery { api.actual(any(), any()) } throws IOException("sin red")

            (ClimaRepository(api).obtener(0.0, 0.0) as Respuesta.Fallo).tipo shouldBe TipoFallo.SIN_CONEXION
        }
    }

    "cualquier otro error inesperado se informa como fallo, sin romper la app" {
        runTest {
            val api = mockk<ClimaApi>()
            coEvery { api.actual(any(), any()) } throws IllegalStateException("json roto")

            (ClimaRepository(api).obtener(0.0, 0.0) as Respuesta.Fallo).tipo shouldBe TipoFallo.SERVIDOR
        }
    }
})
