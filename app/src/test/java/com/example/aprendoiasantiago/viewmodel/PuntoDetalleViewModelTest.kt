package com.example.aprendoiasantiago.ui.viewmodels

import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.model.ProgresoEntity
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.domain.UbicacionUsuario
import com.example.aprendoiasantiago.ubicacion.ProveedorUbicacion
import com.example.aprendoiasantiago.util.pruebaConMain
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

class PuntoDetalleViewModelTest : StringSpec({

    val plaza = PuntoTuristicoEntity(id = 1, nombre = "Plaza de Armas", descripcion = "Centro", latitud = -33.4372, longitud = -70.6506, altitud = 540.0)
    val cap1 = CapsulaEntity(id = 10, puntoId = 1, titulo = "Qué es la IA", urlVideo = "u", codigoQr = "AIJS-001", puntos = 10)
    val cap2 = CapsulaEntity(id = 11, puntoId = 1, titulo = "Cómo aprende una máquina", urlVideo = "u", codigoQr = "AIJS-011", puntos = 15)
    val deOtroPunto = CapsulaEntity(id = 12, puntoId = 2, titulo = "Otra", urlVideo = "u", codigoQr = "AIJS-012", puntos = 99)

    fun juegoSimulado(): JuegoRepository = mockk<JuegoRepository> {
        coEvery { obtenerPunto(1L) } returns plaza
        coEvery { obtenerPunto(99L) } returns null
        every { capsulas } returns flowOf(listOf(cap1, cap2, deOtroPunto))
        every { idsCapsulasCompletadas(7L) } returns flowOf(listOf(10L))
        coEvery { progreso(7L, 10L) } returns ProgresoEntity(usuarioId = 7, capsulaId = 10, completada = true, fechaMillis = 5_000L)
    }

    fun ubicacionSimulada(resultado: UbicacionUsuario?): ProveedorUbicacion =
        mockk<ProveedorUbicacion> { coEvery { actual() } returns resultado }

    "arma el detalle con solo las cápsulas del punto y marca la completada" {
        pruebaConMain {
            val sesion = SesionRepository().apply { iniciar(7) }
            val vm = PuntoDetalleViewModel(juegoSimulado(), sesion, ubicacionSimulada(UbicacionUsuario(-33.4372, -70.6406, null)))

            vm.cargar(1)

            val estado = vm.estado.value
            estado.cargando shouldBe false
            val detalle = estado.detalle.shouldNotBeNull()
            detalle.punto shouldBe plaza
            detalle.capsulas.map { it.capsula.id } shouldContainExactly listOf(10L, 11L)
            detalle.completadas shouldBe 1
            detalle.completado shouldBe false
            detalle.puntosObtenidos shouldBe 10
            detalle.puntosPosibles shouldBe 25
            detalle.capsulas.first { it.capsula.id == 10L }.fechaMillis shouldBe 5_000L
            detalle.distanciaMetros.shouldNotBeNull()
        }
    }

    "sin permiso de ubicación la pantalla carga igual, sin distancia" {
        pruebaConMain {
            val sesion = SesionRepository().apply { iniciar(7) }
            val vm = PuntoDetalleViewModel(juegoSimulado(), sesion, ubicacionSimulada(null))

            vm.cargar(1)

            vm.estado.value.detalle.shouldNotBeNull().distanciaMetros.shouldBeNull()
        }
    }

    "si la ubicación lanza una excepción no se cae y queda sin distancia" {
        pruebaConMain {
            val sesion = SesionRepository().apply { iniciar(7) }
            val ubicacion = mockk<ProveedorUbicacion> { coEvery { actual() } throws IllegalStateException("GPS apagado") }
            val vm = PuntoDetalleViewModel(juegoSimulado(), sesion, ubicacion)

            vm.cargar(1)

            vm.estado.value.detalle.shouldNotBeNull().distanciaMetros.shouldBeNull()
        }
    }

    "un punto que no existe termina de cargar sin detalle" {
        pruebaConMain {
            val sesion = SesionRepository().apply { iniciar(7) }
            val vm = PuntoDetalleViewModel(juegoSimulado(), sesion, ubicacionSimulada(null))

            vm.cargar(99)

            vm.estado.value.cargando shouldBe false
            vm.estado.value.detalle.shouldBeNull()
        }
    }

    "sin sesión iniciada no consulta nada" {
        pruebaConMain {
            val juego = juegoSimulado()
            val vm = PuntoDetalleViewModel(juego, SesionRepository(), ubicacionSimulada(null))

            vm.cargar(1)

            vm.estado.value.cargando shouldBe true
            coVerify(exactly = 0) { juego.obtenerPunto(any()) }
        }
    }
})
