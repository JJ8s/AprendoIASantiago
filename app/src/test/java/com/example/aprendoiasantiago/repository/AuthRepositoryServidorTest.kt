package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.local.UsuarioDao
import com.example.aprendoiasantiago.data.model.UsuarioEntity
import com.example.aprendoiasantiago.data.remote.Respuesta
import com.example.aprendoiasantiago.data.remote.TipoFallo
import com.example.aprendoiasantiago.data.remote.UsuarioRemoto
import com.example.aprendoiasantiago.utils.Hash
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest

/** Login y registro con el microservicio de usuarios simulado: con red, sin red y con conflicto. */
class AuthRepositoryServidorTest : StringSpec({

    val remoto = UsuarioRemoto(id = 7, nombreUsuario = "ana", nombreVisible = "Ana", puntaje = 30)
    val hash = Hash.sha256("clave1")
    val local = UsuarioEntity(id = 1, nombreUsuario = "ana", claveHash = hash, nombreVisible = "Ana", puntaje = 10)

    class Entorno {
        val dao = mockk<UsuarioDao>(relaxed = true)
        val servidor = mockk<ServidorRepository>(relaxed = true)
        val repo = AuthRepository(dao, CompletableDeferred(Unit), servidor)
    }

    "con red, el servidor valida y la app crea el usuario local si no existía" {
        runTest {
            val e = Entorno()
            coEvery { e.servidor.iniciarSesion("ana", "clave1") } returns Respuesta.Ok(remoto)
            coEvery { e.dao.buscarPorAlias("ana") } returnsMany listOf(null, local)

            val usuario = e.repo.iniciarSesion(" ana ", "clave1")

            usuario shouldBe local
            coVerify(exactly = 1) {
                e.dao.insertarJugador(match { it.nombreUsuario == "ana" && it.claveHash == hash && it.puntaje == 30 })
            }
        }
    }

    "con red, si el usuario ya existía actualiza su clave y sube su puntaje" {
        runTest {
            val e = Entorno()
            coEvery { e.servidor.iniciarSesion("ana", "nueva") } returns Respuesta.Ok(remoto)
            coEvery { e.dao.buscarPorAlias("ana") } returns local

            e.repo.iniciarSesion("ana", "nueva")

            coVerify(exactly = 1) { e.dao.actualizarClave(1L, Hash.sha256("nueva")) }
            coVerify(exactly = 1) { e.dao.actualizarPuntaje(1L, 30) }
            coVerify(exactly = 0) { e.dao.insertarJugador(any()) }
        }
    }

    "sin conexión valida contra la base local" {
        runTest {
            val e = Entorno()
            coEvery { e.servidor.iniciarSesion(any(), any()) } returns Respuesta.Fallo(TipoFallo.SIN_CONEXION)
            coEvery { e.dao.buscarPorCredenciales("ana", hash) } returns local

            e.repo.iniciarSesion("ana", "clave1") shouldBe local
        }
    }

    "sin conexión y clave incorrecta no deja entrar" {
        runTest {
            val e = Entorno()
            coEvery { e.servidor.iniciarSesion(any(), any()) } returns Respuesta.Fallo(TipoFallo.SIN_CONEXION)
            coEvery { e.dao.buscarPorCredenciales(any(), any()) } returns null

            e.repo.iniciarSesion("ana", "mala").shouldBeNull()
        }
    }

    "registrar un alias que ya existe en el servidor devuelve null y no crea nada local" {
        runTest {
            val e = Entorno()
            coEvery { e.servidor.registrar(any(), any(), any()) } returns Respuesta.Fallo(TipoFallo.CONFLICTO)

            e.repo.registrarJugador("Ana", "ana", "clave1").shouldBeNull()

            coVerify(exactly = 0) { e.dao.insertarJugador(any()) }
        }
    }

    "registrar sin conexión crea el jugador solo en el teléfono" {
        runTest {
            val e = Entorno()
            coEvery { e.servidor.registrar(any(), any(), any()) } returns Respuesta.Fallo(TipoFallo.SIN_CONEXION)
            coEvery { e.dao.insertarJugador(any()) } returns 5L
            coEvery { e.dao.buscarPorCredenciales("ana", hash) } returns local

            e.repo.registrarJugador("Ana", "ana", "clave1") shouldBe local
        }
    }

    "eliminar un jugador también avisa al servidor" {
        runTest {
            val e = Entorno()
            coEvery { e.dao.buscarPorId(1L) } returns local

            e.repo.eliminarJugador(1L)

            coVerify(exactly = 1) { e.dao.eliminarPorId(1L) }
            coVerify(exactly = 1) { e.servidor.notificarEliminacion("ana") }
        }
    }
})
