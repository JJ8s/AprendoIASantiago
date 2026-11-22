package com.example.aprendoiasantiago.ui.viewmodels

import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.model.UsuarioEntity
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.domain.CapsulaConEstado
import com.example.aprendoiasantiago.util.pruebaConMain
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/** Kotest + MockK: el JuegoRepository se simula; la SesionRepository es la real (es solo memoria). */
class PerfilViewModelTest : StringSpec({

    val demo = UsuarioEntity(id = 1, nombreUsuario = "demo", claveHash = "h", nombreVisible = "Usuario Demo", puntaje = 10)
    val capsula1 = CapsulaEntity(id = 1, puntoId = 1, titulo = "Uno", urlVideo = "u", codigoQr = "AIJS-001", puntos = 10)
    val capsula2 = CapsulaEntity(id = 2, puntoId = 2, titulo = "Dos", urlVideo = "u", codigoQr = "AIJS-002", puntos = 15)
    val plaza = PuntoTuristicoEntity(id = 1, nombre = "Plaza de Armas", descripcion = "d", latitud = -33.4, longitud = -70.6, altitud = 540.0)

    fun juegoSimulado(): JuegoRepository = mockk<JuegoRepository> {
        every { usuario(1L) } returns flowOf(demo)
        every { capsulas } returns flowOf(listOf(capsula1, capsula2))
        every { idsCapsulasCompletadas(1L) } returns flowOf(listOf(1L))
        every { ranking } returns flowOf(listOf(demo))
        every { puntos } returns flowOf(listOf(plaza))
    }

    "sin sesión iniciada el estado queda vacío" {
        pruebaConMain {
            val vm = PerfilViewModel(juegoSimulado(), SesionRepository())
            backgroundScope.launch { vm.estado.collect { } }

            vm.estado.value shouldBe PerfilUiState()
        }
    }

    "con sesión iniciada carga usuario, progreso y ranking" {
        pruebaConMain {
            val sesion = SesionRepository().apply { iniciar(1) }
            val vm = PerfilViewModel(juegoSimulado(), sesion)
            backgroundScope.launch { vm.estado.collect { } }

            val estado = vm.estado.value
            estado.usuario shouldBe demo
            estado.totalCapsulas shouldBe 2
            estado.completadas shouldBe 1
            estado.ranking shouldContainExactly listOf(demo)
            estado.puntos shouldContainExactly listOf(plaza)
        }
    }

    "cerrarSesion limpia la sesión y el estado" {
        pruebaConMain {
            val sesion = SesionRepository().apply { iniciar(1) }
            val vm = PerfilViewModel(juegoSimulado(), sesion)
            backgroundScope.launch { vm.estado.collect { } }

            vm.cerrarSesion()

            sesion.usuarioId.value shouldBe null
            vm.estado.value shouldBe PerfilUiState()
        }
    }

    // ---- Regla de negocio: cuántas selfies tomó el jugador (cada selfie suma 100 puntos) ----

    fun estadoCon(puntaje: Int, vararg completadas: CapsulaEntity) = PerfilUiState(
        usuario = demo.copy(puntaje = puntaje),
        progreso = completadas.map { CapsulaConEstado(it, completada = true) }
    )

    "sin usuario no hay selfies" {
        PerfilUiState().selfiesTomadas shouldBe 0
    }

    "los puntos de las cápsulas no cuentan como selfies, aunque una valga 15" {
        estadoCon(25, capsula1, capsula2).selfiesTomadas shouldBe 0
    }

    "cada 100 puntos sobre los de las cápsulas es una selfie" {
        estadoCon(25 + 200, capsula1, capsula2).selfiesTomadas shouldBe 2
    }

    "una cápsula creada con muchos puntos no se confunde con selfies" {
        // Error detectado con este test: antes se asumía que todo QR valía 10 puntos.
        val grande = capsula1.copy(id = 9, puntos = 120)
        estadoCon(120, grande).selfiesTomadas shouldBe 0
    }
})
