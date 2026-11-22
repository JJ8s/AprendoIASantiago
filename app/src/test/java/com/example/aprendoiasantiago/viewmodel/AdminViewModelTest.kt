package com.example.aprendoiasantiago.ui.viewmodels

import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.model.UsuarioEntity
import com.example.aprendoiasantiago.data.repository.AuthRepository
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.ServidorRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.util.pruebaConMain
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class AdminViewModelTest : StringSpec({

    val ana = UsuarioEntity(id = 2, nombreUsuario = "ana", claveHash = "h", nombreVisible = "Ana")
    val plaza = PuntoTuristicoEntity(id = 1, nombre = "Plaza", descripcion = "d", latitud = 0.0, longitud = 0.0, altitud = 0.0)
    val cap = CapsulaEntity(id = 1, puntoId = 1, titulo = "IA", urlVideo = "u", codigoQr = "Q", puntos = 10)

    class Entorno {
        val auth = mockk<AuthRepository>(relaxed = true)
        val juego = mockk<JuegoRepository>(relaxed = true)
        val avisos = MutableStateFlow<String?>(null)
        val servidor = mockk<ServidorRepository>(relaxed = true).also { every { it.aviso } returns avisos }
        val sesion = SesionRepository().apply { iniciar(1) }
    }

    "las tres listas vienen de la base" {
        pruebaConMain {
            val e = Entorno()
            every { e.auth.observarPerfilesActivos() } returns flowOf(listOf(ana))
            every { e.juego.puntos } returns flowOf(listOf(plaza))
            every { e.juego.capsulas } returns flowOf(listOf(cap))
            val vm = AdminViewModel(e.auth, e.juego, e.sesion, e.servidor)
            backgroundScope.launch { vm.usuarios.collect { } }
            backgroundScope.launch { vm.puntos.collect { } }
            backgroundScope.launch { vm.capsulas.collect { } }

            vm.usuarios.value shouldContainExactly listOf(ana)
            vm.puntos.value shouldContainExactly listOf(plaza)
            vm.capsulas.value shouldContainExactly listOf(cap)
        }
    }

    "las acciones del CRUD llegan al repositorio" {
        pruebaConMain {
            val e = Entorno()
            every { e.auth.observarPerfilesActivos() } returns flowOf(emptyList())
            every { e.juego.puntos } returns flowOf(emptyList())
            every { e.juego.capsulas } returns flowOf(emptyList())
            val vm = AdminViewModel(e.auth, e.juego, e.sesion, e.servidor)

            vm.eliminarJugador(2)
            vm.guardarPunto(plaza)
            vm.eliminarPunto(1)
            vm.guardarCapsula(cap)
            vm.eliminarCapsula(1)

            coVerify(exactly = 1) { e.auth.eliminarJugador(2L) }
            coVerify(exactly = 1) { e.juego.guardarPunto(plaza) }
            coVerify(exactly = 1) { e.juego.eliminarPunto(1L) }
            coVerify(exactly = 1) { e.juego.guardarCapsula(cap) }
            coVerify(exactly = 1) { e.juego.eliminarCapsula(1L) }
        }
    }

    "el aviso del servidor llega a la pantalla y se puede descartar" {
        pruebaConMain {
            val e = Entorno()
            every { e.auth.observarPerfilesActivos() } returns flowOf(emptyList())
            every { e.juego.puntos } returns flowOf(emptyList())
            every { e.juego.capsulas } returns flowOf(emptyList())
            val vm = AdminViewModel(e.auth, e.juego, e.sesion, e.servidor)

            e.avisos.value = "Sin conexión"
            vm.aviso.value shouldBe "Sin conexión"

            vm.avisoVisto()
            verify(exactly = 1) { e.servidor.avisoVisto() }
        }
    }

    "sin servidor el aviso es null y cerrar sesión limpia la sesión" {
        pruebaConMain {
            val e = Entorno()
            every { e.auth.observarPerfilesActivos() } returns flowOf(emptyList())
            every { e.juego.puntos } returns flowOf(emptyList())
            every { e.juego.capsulas } returns flowOf(emptyList())
            val vm = AdminViewModel(e.auth, e.juego, e.sesion)

            vm.aviso.value.shouldBeNull()
            vm.cerrarSesion()

            e.sesion.usuarioId.value.shouldBeNull()
        }
    }
})
