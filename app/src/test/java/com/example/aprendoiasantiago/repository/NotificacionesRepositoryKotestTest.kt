package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.local.NotificacionDao
import com.example.aprendoiasantiago.data.local.UsuarioDao
import com.example.aprendoiasantiago.data.model.UsuarioEntity
import com.example.aprendoiasantiago.domain.TipoNotificacion
import com.example.aprendoiasantiago.notificaciones.EmisorNotificaciones
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest

class NotificacionesRepositoryKotestTest : StringSpec({

    fun usuario(recordatorios: Boolean = true, nuevosPuntos: Boolean = true, logros: Boolean = true) =
        UsuarioEntity(
            id = 1, nombreUsuario = "ana", claveHash = "h", nombreVisible = "Ana",
            notifRecordatorios = recordatorios, notifNuevosPuntos = nuevosPuntos, notifLogros = logros
        )

    class Entorno(val usuarioGuardado: UsuarioEntity?) {
        val dao = mockk<NotificacionDao>(relaxed = true)
        val usuarioDao = mockk<UsuarioDao>().also { coEvery { it.buscarPorId(1L) } returns usuarioGuardado }
        val emisor = mockk<EmisorNotificaciones>(relaxed = true)
        val repo = NotificacionesRepository(dao, usuarioDao, emisor) { 1_000L }
    }

    "un aviso de un tipo activado se guarda con la hora actual y se muestra" {
        runTest {
            val e = Entorno(usuario())

            e.repo.registrar(1, TipoNotificacion.LOGRO, "¡10 puntos!", "Completaste una cápsula") shouldBe true

            coVerify(exactly = 1) {
                e.dao.insertar(
                    match { it.usuarioId == 1L && it.titulo == "¡10 puntos!" && it.tipo == "LOGRO" && it.fechaMillis == 1_000L }
                )
            }
            verify(exactly = 1) { e.emisor.mostrar("¡10 puntos!", "Completaste una cápsula") }
        }
    }

    "un aviso de un tipo desactivado ni se guarda ni se muestra" {
        runTest {
            val e = Entorno(usuario(logros = false))

            e.repo.registrar(1, TipoNotificacion.LOGRO, "x", "y") shouldBe false

            coVerify(exactly = 0) { e.dao.insertar(any()) }
            verify(exactly = 0) { e.emisor.mostrar(any(), any()) }
        }
    }

    "el aviso de prueba del sistema se entrega aunque todo esté desactivado" {
        runTest {
            val e = Entorno(usuario(recordatorios = false, nuevosPuntos = false, logros = false))

            e.repo.registrar(1, TipoNotificacion.SISTEMA, "Prueba", "ok") shouldBe true

            verify(exactly = 1) { e.emisor.mostrar("Prueba", "ok") }
        }
    }

    "para un usuario que no existe no registra nada" {
        runTest {
            val e = Entorno(null)

            e.repo.registrar(1, TipoNotificacion.SISTEMA, "x", "y") shouldBe false

            coVerify(exactly = 0) { e.dao.insertar(any()) }
        }
    }

    "marcarTodasLeidas y vaciar delegan en el DAO con el usuario correcto" {
        runTest {
            val e = Entorno(usuario())

            e.repo.marcarTodasLeidas(1)
            e.repo.vaciar(1)

            coVerify(exactly = 1) { e.dao.marcarTodasLeidas(1L) }
            coVerify(exactly = 1) { e.dao.borrarTodas(1L) }
        }
    }

    "cada tipo consulta su propia preferencia" {
        val soloRecordatorios = usuario(recordatorios = true, nuevosPuntos = false, logros = false)

        TipoNotificacion.RECORDATORIO.estaActivaPara(soloRecordatorios) shouldBe true
        TipoNotificacion.NUEVO_PUNTO.estaActivaPara(soloRecordatorios) shouldBe false
        TipoNotificacion.LOGRO.estaActivaPara(soloRecordatorios) shouldBe false
        TipoNotificacion.SISTEMA.estaActivaPara(soloRecordatorios) shouldBe true
    }
})
