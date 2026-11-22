package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.local.CapsulaDao
import com.example.aprendoiasantiago.data.local.ProgresoDao
import com.example.aprendoiasantiago.data.local.PuntoDao
import com.example.aprendoiasantiago.data.local.RutaDao
import com.example.aprendoiasantiago.data.local.UsuarioDao
import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.model.UsuarioEntity
import com.example.aprendoiasantiago.data.remote.CapsulaRemota
import com.example.aprendoiasantiago.data.remote.PuntoRemoto
import com.example.aprendoiasantiago.data.remote.Respuesta
import com.example.aprendoiasantiago.data.remote.TipoFallo
import io.kotest.core.spec.style.StringSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest

/** CRUD de contenido y envío de progreso: servidor primero, Room como respaldo. */
class JuegoRepositoryServidorTest : StringSpec({

    val punto = PuntoTuristicoEntity(id = 0, nombre = "Plaza", descripcion = "d", latitud = 1.0, longitud = 2.0, altitud = 3.0)
    val capsula = CapsulaEntity(id = 0, puntoId = 1, titulo = "IA", urlVideo = "u", codigoQr = "AIJS-100", puntos = 10)

    class Entorno {
        val puntoDao = mockk<PuntoDao>(relaxed = true)
        val capsulaDao = mockk<CapsulaDao>(relaxed = true)
        val usuarioDao = mockk<UsuarioDao>(relaxed = true)
        val progresoDao = mockk<ProgresoDao>(relaxed = true)
        val servidor = mockk<ServidorRepository>(relaxed = true)
        val repo = JuegoRepository(puntoDao, capsulaDao, usuarioDao, progresoDao, mockk<RutaDao>(relaxed = true), { 1_000L }, servidor)

        init {
            every { servidor.mensajeDe(any(), any()) } returns "mensaje"
        }
    }

    "guardar un punto con servidor lo guarda en Room con el id que decidió el servidor" {
        runTest {
            val e = Entorno()
            coEvery { e.servidor.guardarPunto(punto) } returns Respuesta.Ok(PuntoRemoto(8, "Plaza", "d", 1.0, 2.0, 3.0))

            e.repo.guardarPunto(punto)

            coVerify(exactly = 1) { e.puntoDao.guardarTodos(match { it.single().id == 8L }) }
        }
    }

    "guardar un punto sin conexión lo deja solo en Room y avisa" {
        runTest {
            val e = Entorno()
            coEvery { e.servidor.guardarPunto(punto) } returns Respuesta.Fallo(TipoFallo.SIN_CONEXION)

            e.repo.guardarPunto(punto)

            coVerify(exactly = 1) { e.puntoDao.guardarTodos(listOf(punto)) }
            verify(exactly = 1) { e.servidor.avisar("mensaje") }
        }
    }

    "una cápsula con QR repetido (conflicto) no se guarda en ningún lado y se avisa" {
        runTest {
            val e = Entorno()
            coEvery { e.servidor.guardarCapsula(capsula) } returns Respuesta.Fallo(TipoFallo.CONFLICTO)

            e.repo.guardarCapsula(capsula)

            coVerify(exactly = 0) { e.capsulaDao.guardarTodas(any()) }
            verify(exactly = 1) { e.servidor.avisar("mensaje") }
        }
    }

    "guardar una cápsula con servidor usa el id del servidor" {
        runTest {
            val e = Entorno()
            coEvery { e.servidor.guardarCapsula(capsula) } returns Respuesta.Ok(CapsulaRemota(12, 1, "IA", "u", "AIJS-100", 10))

            e.repo.guardarCapsula(capsula)

            coVerify(exactly = 1) { e.capsulaDao.guardarTodas(match { it.single().id == 12L }) }
        }
    }

    "eliminar sin conexión borra igual de Room; con un error del servidor no borra" {
        runTest {
            val e = Entorno()
            coEvery { e.servidor.eliminarCapsula(1L) } returns Respuesta.Fallo(TipoFallo.SIN_CONEXION)
            coEvery { e.servidor.eliminarCapsula(2L) } returns Respuesta.Fallo(TipoFallo.SERVIDOR, "HTTP 500")

            e.repo.eliminarCapsula(1L)
            e.repo.eliminarCapsula(2L)

            coVerify(exactly = 1) { e.capsulaDao.eliminarCapsula(1L) }
            coVerify(exactly = 0) { e.capsulaDao.eliminarCapsula(2L) }
        }
    }

    "completar una cápsula nueva avisa el progreso al servidor" {
        runTest {
            val e = Entorno()
            coEvery { e.capsulaDao.buscarPorCodigo("AIJS-001") } returns capsula.copy(id = 1, codigoQr = "AIJS-001")
            coEvery { e.progresoDao.completarCapsula(any(), any(), any(), any(), any(), any(), any()) } returns true
            coEvery { e.usuarioDao.buscarPorId(7L) } returns UsuarioEntity(id = 7, nombreUsuario = "ana", claveHash = "h", nombreVisible = "Ana")

            e.repo.procesarCodigo(7L, "AIJS-001")

            verify(exactly = 1) { e.servidor.notificarProgreso("ana", 1L, 10) }
        }
    }

    "volver a escanear la misma cápsula no vuelve a avisar" {
        runTest {
            val e = Entorno()
            coEvery { e.capsulaDao.buscarPorCodigo("AIJS-001") } returns capsula.copy(id = 1, codigoQr = "AIJS-001")
            coEvery { e.progresoDao.completarCapsula(any(), any(), any(), any(), any(), any(), any()) } returns false

            e.repo.procesarCodigo(7L, "AIJS-001")

            verify(exactly = 0) { e.servidor.notificarProgreso(any(), any(), any()) }
        }
    }
})
