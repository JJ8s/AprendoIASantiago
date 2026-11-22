package com.example.aprendoiasantiago.ui.viewmodels

import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.model.RutaEntity
import com.example.aprendoiasantiago.data.model.RutaPuntoEntity
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.util.pruebaConMain
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class MapaViewModelTest : StringSpec({

    val plaza = PuntoTuristicoEntity(id = 1, nombre = "Plaza", descripcion = "d", latitud = -33.4, longitud = -70.6, altitud = 540.0)
    val cerro = PuntoTuristicoEntity(id = 2, nombre = "Cerro", descripcion = "d", latitud = -33.44, longitud = -70.64, altitud = 600.0)
    val cap1 = CapsulaEntity(id = 10, puntoId = 1, titulo = "A", urlVideo = "u", codigoQr = "Q1", puntos = 10)
    val cap2 = CapsulaEntity(id = 11, puntoId = 2, titulo = "B", urlVideo = "u", codigoQr = "Q2", puntos = 10)

    fun juego(): JuegoRepository = mockk {
        every { puntos } returns flowOf(listOf(plaza, cerro))
        every { capsulas } returns flowOf(listOf(cap1, cap2))
        every { rutas } returns flowOf(listOf(RutaEntity(1, "Centro", "d")))
        every { paradasDeRutas } returns flowOf(listOf(RutaPuntoEntity(1, 2, 1), RutaPuntoEntity(1, 1, 2)))
        every { idsCapsulasCompletadas(7L) } returns flowOf(listOf(10L))
    }

    "sin sesión todos los puntos aparecen pendientes" {
        pruebaConMain {
            val vm = MapaViewModel(juego(), SesionRepository())
            backgroundScope.launch { vm.estado.collect { } }

            vm.estado.value.puntos.map { it.completado } shouldBe listOf(false, false)
        }
    }

    "con sesión marca como completado el punto cuya cápsula ya se escaneó" {
        pruebaConMain {
            val vm = MapaViewModel(juego(), SesionRepository().apply { iniciar(7) })
            backgroundScope.launch { vm.estado.collect { } }

            vm.estado.value.puntos.first { it.punto.id == 1L }.completado shouldBe true
            vm.estado.value.puntos.first { it.punto.id == 2L }.completado shouldBe false
        }
    }

    "las rutas traen sus paradas en el orden de recorrido" {
        pruebaConMain {
            val vm = MapaViewModel(juego(), SesionRepository().apply { iniciar(7) })
            backgroundScope.launch { vm.estado.collect { } }

            val ruta = vm.estado.value.rutas.single()
            ruta.puntos.map { it.punto.id } shouldBe listOf(2L, 1L)
            ruta.completados shouldBe 1
            ruta.completa shouldBe false
        }
    }

    "seleccionar una ruta la dibuja y tocarla de nuevo la quita" {
        pruebaConMain {
            val vm = MapaViewModel(juego(), SesionRepository())
            backgroundScope.launch { vm.estado.collect { } }

            vm.seleccionarRuta(1L)
            vm.estado.value.rutaSeleccionada.shouldNotBeNull()

            vm.seleccionarRuta(1L)
            vm.estado.value.rutaSeleccionada.shouldBeNull()
        }
    }

    "elegir otra ruta reemplaza a la anterior y null la quita" {
        pruebaConMain {
            val vm = MapaViewModel(juego(), SesionRepository())
            backgroundScope.launch { vm.estado.collect { } }

            vm.seleccionarRuta(1L)
            vm.seleccionarRuta(2L)
            vm.estado.value.rutaSeleccionadaId shouldBe 2L

            vm.seleccionarRuta(null)
            vm.estado.value.rutaSeleccionadaId.shouldBeNull()
        }
    }
})
