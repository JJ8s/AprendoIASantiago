package com.example.aprendoiasantiago.ui.viewmodels

import com.example.aprendoiasantiago.data.model.UsuarioEntity
import com.example.aprendoiasantiago.data.repository.AuthRepository
import com.example.aprendoiasantiago.data.repository.ServidorNoDisponibleException
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.domain.ErrorLogin
import com.example.aprendoiasantiago.util.pruebaConMain
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

class LoginViewModelTest : StringSpec({

    val demo = UsuarioEntity(id = 1, nombreUsuario = "demo", claveHash = "h", nombreVisible = "Usuario Demo")

    fun authSimulado(): AuthRepository = mockk<AuthRepository> {
        every { observarPerfilesActivos() } returns flowOf(listOf(demo))
    }

    "al abrir la pantalla muestra los perfiles guardados" {
        pruebaConMain {
            val vm = LoginViewModel(authSimulado(), SesionRepository())

            vm.estado.value.perfilesDisponibles shouldContainExactly listOf(demo)
        }
    }

    "con el usuario vacío muestra el error y no consulta la base" {
        pruebaConMain {
            val auth = authSimulado()
            val vm = LoginViewModel(auth, SesionRepository())
            vm.onClaveCambia("demo1234")

            vm.iniciarSesion()

            vm.estado.value.error shouldBe ErrorLogin.USUARIO_VACIO
            coVerify(exactly = 0) { auth.iniciarSesion(any(), any()) }
        }
    }

    "con credenciales correctas inicia la sesión del usuario" {
        pruebaConMain {
            val auth = authSimulado()
            coEvery { auth.iniciarSesion("demo", "demo1234") } returns demo
            val sesion = SesionRepository()
            val vm = LoginViewModel(auth, sesion)
            vm.onUsuarioCambia("demo")
            vm.onClaveCambia("demo1234")

            vm.iniciarSesion()

            vm.estado.value.autenticado shouldBe true
            vm.estado.value.cargando shouldBe false
            sesion.usuarioId.value shouldBe 1L
        }
    }

    "con credenciales incorrectas no inicia sesión y avisa" {
        pruebaConMain {
            val auth = authSimulado()
            coEvery { auth.iniciarSesion(any(), any()) } returns null
            val sesion = SesionRepository()
            val vm = LoginViewModel(auth, sesion)
            vm.onUsuarioCambia("demo")
            vm.onClaveCambia("mala")

            vm.iniciarSesion()

            vm.estado.value.error shouldBe ErrorLogin.CREDENCIALES_INCORRECTAS
            vm.estado.value.autenticado shouldBe false
            sesion.usuarioId.value shouldBe null
        }
    }

    "si el servidor de usuarios no responde (modo estricto) muestra el aviso y no inicia sesión" {
        pruebaConMain {
            val auth = authSimulado()
            coEvery { auth.iniciarSesion(any(), any()) } throws ServidorNoDisponibleException("caído")
            val sesion = SesionRepository()
            val vm = LoginViewModel(auth, sesion)
            vm.onUsuarioCambia("demo")
            vm.onClaveCambia("demo1234")

            vm.iniciarSesion()

            vm.estado.value.error shouldBe ErrorLogin.SERVIDOR_NO_DISPONIBLE
            vm.estado.value.cargando shouldBe false
            vm.estado.value.autenticado shouldBe false
            sesion.usuarioId.value shouldBe null
        }
    }

    "si la base de datos falla muestra un error inesperado y no se queda cargando" {
        pruebaConMain {
            val auth = authSimulado()
            coEvery { auth.iniciarSesion(any(), any()) } throws RuntimeException("base caída")
            val vm = LoginViewModel(auth, SesionRepository())
            vm.onUsuarioCambia("demo")
            vm.onClaveCambia("demo1234")

            vm.iniciarSesion()

            vm.estado.value.error shouldBe ErrorLogin.ERROR_INESPERADO
            vm.estado.value.cargando shouldBe false
        }
    }

    "el registro con contraseñas distintas no llama al repositorio" {
        pruebaConMain {
            val auth = authSimulado()
            val vm = LoginViewModel(auth, SesionRepository())
            vm.cambiarModoRegistro(true)
            vm.onNombreRegistroCambia("Ana")
            vm.onUsuarioCambia("ana")
            vm.onClaveCambia("abc123")
            vm.onConfirmarClaveCambia("otra")

            vm.registrarJugador()

            vm.estado.value.error shouldBe ErrorLogin.CLAVES_NO_COINCIDEN
            coVerify(exactly = 0) { auth.registrarJugador(any(), any(), any()) }
        }
    }

    "registrar un alias que ya existe muestra USUARIO_YA_EXISTE" {
        pruebaConMain {
            val auth = authSimulado()
            coEvery { auth.registrarJugador(any(), any(), any()) } returns null
            val sesion = SesionRepository()
            val vm = LoginViewModel(auth, sesion)
            vm.cambiarModoRegistro(true)
            vm.onNombreRegistroCambia("Ana")
            vm.onUsuarioCambia("demo")
            vm.onClaveCambia("abc123")
            vm.onConfirmarClaveCambia("abc123")

            vm.registrarJugador()

            vm.estado.value.error shouldBe ErrorLogin.USUARIO_YA_EXISTE
            sesion.usuarioId.value shouldBe null
        }
    }

    "un registro correcto inicia la sesión del jugador nuevo" {
        pruebaConMain {
            val auth = authSimulado()
            val nuevo = demo.copy(id = 9, nombreUsuario = "ana", nombreVisible = "Ana")
            coEvery { auth.registrarJugador("Ana", "ana", "abc123") } returns nuevo
            val sesion = SesionRepository()
            val vm = LoginViewModel(auth, sesion)
            vm.cambiarModoRegistro(true)
            vm.onNombreRegistroCambia("Ana")
            vm.onUsuarioCambia("ana")
            vm.onClaveCambia("abc123")
            vm.onConfirmarClaveCambia("abc123")

            vm.registrarJugador()

            vm.estado.value.autenticado shouldBe true
            sesion.usuarioId.value shouldBe 9L
        }
    }
})
