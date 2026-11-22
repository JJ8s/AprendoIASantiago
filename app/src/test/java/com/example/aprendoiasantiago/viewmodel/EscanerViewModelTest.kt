package com.example.aprendoiasantiago.ui.viewmodels

import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.NotificacionesRepository
import com.example.aprendoiasantiago.data.repository.ResultadoEscaneo
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.domain.TipoNotificacion
import com.example.aprendoiasantiago.ubicacion.ProveedorUbicacion
import com.example.aprendoiasantiago.util.pruebaConMain
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class EscanerViewModelTest : StringSpec({

    val capsula = CapsulaEntity(id = 1, puntoId = 1, titulo = "Qué es la IA", urlVideo = "u", codigoQr = "AIJS-001", puntos = 10)

    class Entorno(usuario: Long? = 7L) {
        val juego = mockk<JuegoRepository>()
        val notificaciones = mockk<NotificacionesRepository>(relaxed = true)
        val sesion = SesionRepository().apply { if (usuario != null) iniciar(usuario) }
        val vm = EscanerViewModel(juego, sesion, mockk<ProveedorUbicacion>(), notificaciones)
    }

    "un código válido nuevo muestra el resultado y avisa el logro" {
        pruebaConMain {
            val e = Entorno()
            val valido = ResultadoEscaneo.Valido(capsula, null, puntosGanados = 10)
            coEvery { e.juego.procesarCodigo(7L, "AIJS-001", any()) } returns valido

            e.vm.onCodigoDetectado("AIJS-001")

            e.vm.estado.value.resultado shouldBe valido
            e.vm.estado.value.procesando shouldBe false
            coVerify(exactly = 1) { e.notificaciones.registrar(7L, TipoNotificacion.LOGRO, "¡Ganaste 10 puntos!", any()) }
        }
    }

    "una cápsula ya completada no vuelve a avisar puntos" {
        pruebaConMain {
            val e = Entorno()
            coEvery { e.juego.procesarCodigo(any(), any(), any()) } returns ResultadoEscaneo.Valido(capsula, null, puntosGanados = 0)

            e.vm.onCodigoDetectado("AIJS-001")

            coVerify(exactly = 0) { e.notificaciones.registrar(any(), any(), any(), any()) }
        }
    }

    "un QR desconocido se muestra como tal" {
        pruebaConMain {
            val e = Entorno()
            coEvery { e.juego.procesarCodigo(any(), "XYZ", any()) } returns ResultadoEscaneo.Desconocido("XYZ")

            e.vm.onCodigoDetectado("XYZ")

            e.vm.estado.value.resultado shouldBe ResultadoEscaneo.Desconocido("XYZ")
        }
    }

    "si la base falla el resultado es Error y la app no se cae" {
        pruebaConMain {
            val e = Entorno()
            coEvery { e.juego.procesarCodigo(any(), any(), any()) } throws IllegalStateException("db")

            e.vm.onCodigoDetectado("AIJS-001")

            e.vm.estado.value.resultado shouldBe ResultadoEscaneo.Error
        }
    }

    "si falla el aviso de logro igual queda el resultado" {
        pruebaConMain {
            val e = Entorno()
            val valido = ResultadoEscaneo.Valido(capsula, null, 10)
            coEvery { e.juego.procesarCodigo(any(), any(), any()) } returns valido
            coEvery { e.notificaciones.registrar(any(), any(), any(), any()) } throws RuntimeException("sin canal")

            e.vm.onCodigoDetectado("AIJS-001")

            e.vm.estado.value.resultado shouldBe valido
        }
    }

    "sin sesión iniciada no escanea" {
        pruebaConMain {
            val e = Entorno(usuario = null)

            e.vm.onCodigoDetectado("AIJS-001")

            e.vm.estado.value shouldBe EscanerUiState()
            coVerify(exactly = 0) { e.juego.procesarCodigo(any(), any(), any()) }
        }
    }

    "mientras hay un resultado en pantalla se ignoran nuevas lecturas" {
        pruebaConMain {
            val e = Entorno()
            coEvery { e.juego.procesarCodigo(any(), any(), any()) } returns ResultadoEscaneo.Desconocido("A")

            e.vm.onCodigoDetectado("A")
            e.vm.onCodigoDetectado("B")

            coVerify(exactly = 1) { e.juego.procesarCodigo(any(), any(), any()) }
        }
    }

    "escanearOtro limpia el resultado y reactiva la cámara" {
        pruebaConMain {
            val e = Entorno()
            coEvery { e.juego.procesarCodigo(any(), any(), any()) } returns ResultadoEscaneo.Desconocido("A")
            e.vm.onCodigoDetectado("A")
            e.vm.estado.value.escaneando shouldBe false

            e.vm.escanearOtro()

            e.vm.estado.value.escaneando shouldBe true
        }
    }
})
