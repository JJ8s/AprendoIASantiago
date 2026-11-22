package com.example.aprendoiasantiago.ui.viewmodels

import com.example.aprendoiasantiago.data.local.AlmacenEvidencias
import com.example.aprendoiasantiago.data.model.NotificacionEntity
import com.example.aprendoiasantiago.data.model.UsuarioEntity
import com.example.aprendoiasantiago.data.repository.NotificacionesRepository
import com.example.aprendoiasantiago.data.repository.PerfilRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.domain.ErrorPerfil
import com.example.aprendoiasantiago.domain.TipoNotificacion
import com.example.aprendoiasantiago.util.pruebaConMain
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class AjustesPerfilViewModelTest : StringSpec({

    val ana = UsuarioEntity(id = 7, nombreUsuario = "ana", claveHash = "h", nombreVisible = "Ana")
    val avisos = listOf(
        NotificacionEntity(id = 1, usuarioId = 7, titulo = "a", mensaje = "m", tipo = "LOGRO", fechaMillis = 1, leida = false),
        NotificacionEntity(id = 2, usuarioId = 7, titulo = "b", mensaje = "m", tipo = "LOGRO", fechaMillis = 2, leida = true)
    )

    class Entorno(usuario: Long? = 7L) {
        val perfil = mockk<PerfilRepository>(relaxed = true)
        val notificaciones = mockk<NotificacionesRepository>(relaxed = true)
        val almacen = mockk<AlmacenEvidencias>(relaxed = true)
        val sesion = SesionRepository().apply { if (usuario != null) iniciar(usuario) }
        val vm = AjustesPerfilViewModel(perfil, notificaciones, sesion, almacen)
    }

    fun Entorno.conDatos(ana: UsuarioEntity, avisos: List<NotificacionEntity>) {
        every { perfil.usuario(7L) } returns flowOf(ana)
        every { notificaciones.observar(7L) } returns flowOf(avisos)
    }

    "muestra el usuario, la bandeja y cuántos avisos faltan por leer" {
        pruebaConMain {
            val e = Entorno().also { it.conDatos(ana, avisos) }
            backgroundScope.launch { e.vm.estado.collect { } }

            e.vm.estado.value.usuario shouldBe ana
            e.vm.estado.value.bandeja.size shouldBe 2
            e.vm.estado.value.sinLeer shouldBe 1
        }
    }

    "guardar datos válidos muestra la confirmación" {
        pruebaConMain {
            val e = Entorno().also { it.conDatos(ana, avisos) }
            backgroundScope.launch { e.vm.estado.collect { } }
            coEvery { e.perfil.guardarDatos(7L, "Ana", "a@b.cl", "") } returns null

            e.vm.guardarDatos("Ana", "a@b.cl", "")

            e.vm.estado.value.mensaje shouldBe MensajePerfil.DATOS_GUARDADOS
            e.vm.estado.value.errorDatos.shouldBeNull()
        }
    }

    "guardar datos inválidos muestra el error y no la confirmación" {
        pruebaConMain {
            val e = Entorno().also { it.conDatos(ana, avisos) }
            backgroundScope.launch { e.vm.estado.collect { } }
            coEvery { e.perfil.guardarDatos(any(), any(), any(), any()) } returns ErrorPerfil.CORREO_INVALIDO

            e.vm.guardarDatos("Ana", "malo", "")

            e.vm.estado.value.errorDatos shouldBe ErrorPerfil.CORREO_INVALIDO
            e.vm.estado.value.mensaje.shouldBeNull()
        }
    }

    "si guardar lanza una excepción queda ERROR_INESPERADO" {
        pruebaConMain {
            val e = Entorno().also { it.conDatos(ana, avisos) }
            backgroundScope.launch { e.vm.estado.collect { } }
            coEvery { e.perfil.guardarDatos(any(), any(), any(), any()) } throws RuntimeException("db")

            e.vm.guardarDatos("Ana", "", "")

            e.vm.estado.value.errorDatos shouldBe ErrorPerfil.ERROR_INESPERADO
        }
    }

    "cambiar la clave bien o mal informa cada caso" {
        pruebaConMain {
            val e = Entorno().also { it.conDatos(ana, avisos) }
            backgroundScope.launch { e.vm.estado.collect { } }
            coEvery { e.perfil.cambiarClave(7L, "vieja", "nueva1", "nueva1") } returns null
            coEvery { e.perfil.cambiarClave(7L, "mala", "nueva1", "nueva1") } returns ErrorPerfil.CLAVE_ACTUAL_INCORRECTA

            e.vm.cambiarClave("vieja", "nueva1", "nueva1")
            e.vm.estado.value.mensaje shouldBe MensajePerfil.CLAVE_CAMBIADA

            e.vm.limpiarMensajes()
            e.vm.cambiarClave("mala", "nueva1", "nueva1")
            e.vm.estado.value.errorClave shouldBe ErrorPerfil.CLAVE_ACTUAL_INCORRECTA
        }
    }

    "elegir un avatar borra la foto propia anterior y confirma" {
        pruebaConMain {
            val e = Entorno().also { it.conDatos(ana, avisos) }
            backgroundScope.launch { e.vm.estado.collect { } }
            coEvery { e.perfil.elegirAvatar(7L, 3) } returns "file:///vieja.jpg"

            e.vm.elegirAvatar(3)

            verify(exactly = 1) { e.almacen.borrar("file:///vieja.jpg") }
            e.vm.estado.value.mensaje shouldBe MensajePerfil.AVATAR_ACTUALIZADO
        }
    }

    "guardar notificaciones y permisos llega al repositorio" {
        pruebaConMain {
            val e = Entorno().also { it.conDatos(ana, avisos) }

            e.vm.guardarNotificaciones(true, false, true)
            e.vm.guardarPermisos(ubicacion = false, camara = true)

            coVerify(exactly = 1) { e.perfil.guardarNotificaciones(7L, true, false, true) }
            coVerify(exactly = 1) { e.perfil.guardarPermisos(7L, false, true) }
        }
    }

    "el aviso de prueba, marcar leídas y vaciar usan el repositorio de avisos" {
        pruebaConMain {
            val e = Entorno().also { it.conDatos(ana, avisos) }
            backgroundScope.launch { e.vm.estado.collect { } }

            e.vm.enviarAvisoDePrueba()
            e.vm.marcarLeidas()
            e.vm.vaciarBandeja()

            coVerify(exactly = 1) { e.notificaciones.registrar(7L, TipoNotificacion.SISTEMA, any(), any()) }
            coVerify(exactly = 1) { e.notificaciones.marcarTodasLeidas(7L) }
            coVerify(exactly = 1) { e.notificaciones.vaciar(7L) }
            e.vm.estado.value.mensaje shouldBe MensajePerfil.AVISO_DE_PRUEBA
        }
    }

    "sin sesión las acciones no hacen nada" {
        pruebaConMain {
            val e = Entorno(usuario = null)

            e.vm.guardarDatos("Ana", "", "")
            e.vm.marcarLeidas()

            coVerify(exactly = 0) { e.perfil.guardarDatos(any(), any(), any(), any()) }
            coVerify(exactly = 0) { e.notificaciones.marcarTodasLeidas(any()) }
        }
    }

    "cerrar sesión limpia la sesión y los mensajes" {
        pruebaConMain {
            val e = Entorno().also { it.conDatos(ana, avisos) }
            backgroundScope.launch { e.vm.estado.collect { } }
            coEvery { e.perfil.guardarDatos(any(), any(), any(), any()) } returns null
            e.vm.guardarDatos("Ana", "", "")

            e.vm.cerrarSesion()

            e.sesion.usuarioId.value.shouldBeNull()
            e.vm.estado.value.mensaje.shouldBeNull()
        }
    }
})
