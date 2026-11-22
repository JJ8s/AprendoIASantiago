package com.example.aprendoiasantiago.ui.viewmodels

import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.util.pruebaConMain
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class CapsulaViewModelTest : StringSpec({

    val punto = PuntoTuristicoEntity(id = 1, nombre = "Plaza", descripcion = "d", latitud = 0.0, longitud = 0.0, altitud = 0.0)
    val capsula = CapsulaEntity(id = 5, puntoId = 1, titulo = "IA", urlVideo = "u", codigoQr = "Q", puntos = 10)

    "carga la cápsula con su punto" {
        pruebaConMain {
            val juego = mockk<JuegoRepository>()
            coEvery { juego.obtenerCapsula(5L) } returns capsula
            coEvery { juego.obtenerPunto(1L) } returns punto
            val vm = CapsulaViewModel(juego)

            vm.cargar(5)

            vm.estado.value shouldBe CapsulaUiState(cargando = false, capsula = capsula, punto = punto)
        }
    }

    "una cápsula que no existe termina de cargar sin datos" {
        pruebaConMain {
            val juego = mockk<JuegoRepository>()
            coEvery { juego.obtenerCapsula(9L) } returns null
            val vm = CapsulaViewModel(juego)

            vm.cargar(9)

            vm.estado.value.cargando shouldBe false
            vm.estado.value.capsula.shouldBeNull()
        }
    }

    "si la base falla no se cae" {
        pruebaConMain {
            val juego = mockk<JuegoRepository>()
            coEvery { juego.obtenerCapsula(any()) } throws IllegalStateException("db")
            val vm = CapsulaViewModel(juego)

            vm.cargar(5)

            vm.estado.value shouldBe CapsulaUiState(cargando = false)
        }
    }

    "cargar dos veces el mismo id consulta la base una sola vez" {
        pruebaConMain {
            val juego = mockk<JuegoRepository>()
            coEvery { juego.obtenerCapsula(5L) } returns capsula
            coEvery { juego.obtenerPunto(1L) } returns punto
            val vm = CapsulaViewModel(juego)

            vm.cargar(5)
            vm.cargar(5)

            coVerify(exactly = 1) { juego.obtenerCapsula(5L) }
        }
    }
})
