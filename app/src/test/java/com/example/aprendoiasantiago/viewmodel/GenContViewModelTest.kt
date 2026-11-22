package com.example.aprendoiasantiago.ui.viewmodels

import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.util.pruebaConMain
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class GenContViewModelTest : StringSpec({

    val cap = CapsulaEntity(id = 1, puntoId = 1, titulo = "IA", urlVideo = "u", codigoQr = "Q", puntos = 10)

    "lista las cápsulas y guarda o elimina a través del repositorio" {
        pruebaConMain {
            val juego = mockk<JuegoRepository>(relaxed = true)
            every { juego.capsulas } returns flowOf(listOf(cap))
            every { juego.puntos } returns flowOf(emptyList())
            val vm = GenContViewModel(juego, SesionRepository())
            backgroundScope.launch { vm.capsulas.collect { } }

            vm.capsulas.value shouldContainExactly listOf(cap)
            vm.guardarCapsula(cap)
            vm.eliminarCapsula(1)

            coVerify(exactly = 1) { juego.guardarCapsula(cap) }
            coVerify(exactly = 1) { juego.eliminarCapsula(1L) }
        }
    }

    "cerrar sesión limpia la sesión" {
        pruebaConMain {
            val juego = mockk<JuegoRepository>(relaxed = true)
            every { juego.capsulas } returns flowOf(emptyList())
            every { juego.puntos } returns flowOf(emptyList())
            val sesion = SesionRepository().apply { iniciar(3) }

            GenContViewModel(juego, sesion).cerrarSesion()

            sesion.usuarioId.value.shouldBeNull()
        }
    }
})
