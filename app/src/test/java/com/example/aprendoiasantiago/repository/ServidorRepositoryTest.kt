package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.local.CapsulaDao
import com.example.aprendoiasantiago.data.local.PuntoDao
import com.example.aprendoiasantiago.data.local.UsuarioDao
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.model.UsuarioEntity
import com.example.aprendoiasantiago.data.remote.CapsulaRemota
import com.example.aprendoiasantiago.data.remote.ContenidoApi
import com.example.aprendoiasantiago.data.remote.ProgresoPeticion
import com.example.aprendoiasantiago.data.remote.ProgresoRespuesta
import com.example.aprendoiasantiago.data.remote.PuntoRemoto
import com.example.aprendoiasantiago.data.remote.Respuesta
import com.example.aprendoiasantiago.data.remote.TipoFallo
import com.example.aprendoiasantiago.data.remote.UsuarioRemoto
import com.example.aprendoiasantiago.data.remote.UsuariosApi
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** Guía EP4: el puente con los microservicios se prueba con las APIs simuladas (sin red real). */
class ServidorRepositoryTest : StringSpec({

    class Entorno {
        val usuariosApi = mockk<UsuariosApi>(relaxed = true)
        val contenidoApi = mockk<ContenidoApi>(relaxed = true)
        val puntoDao = mockk<PuntoDao>(relaxed = true)
        val capsulaDao = mockk<CapsulaDao>(relaxed = true)
        val usuarioDao = mockk<UsuarioDao>(relaxed = true)
        // Unconfined: las tareas "en segundo plano" se ejecutan al instante dentro del test.
        val repo = ServidorRepository(usuariosApi, contenidoApi, puntoDao, capsulaDao, usuarioDao, CoroutineScope(Dispatchers.Unconfined))
    }

    fun http(codigo: Int) = HttpException(Response.error<Any>(codigo, "".toResponseBody(null)))
    val ana = UsuarioRemoto(id = 7, nombreUsuario = "ana", nombreVisible = "Ana", puntaje = 20)

    "un login correcto devuelve el usuario del servidor" {
        runTest {
            val e = Entorno()
            coEvery { e.usuariosApi.login(any()) } returns ana

            e.repo.iniciarSesion("ana", "clave1") shouldBe Respuesta.Ok(ana)
        }
    }

    "las credenciales malas (401) se informan como NO_AUTORIZADO" {
        runTest {
            val e = Entorno()
            coEvery { e.usuariosApi.login(any()) } throws http(401)

            (e.repo.iniciarSesion("ana", "mala") as Respuesta.Fallo).tipo shouldBe TipoFallo.NO_AUTORIZADO
        }
    }

    "sin red (IOException) se informa como SIN_CONEXION" {
        runTest {
            val e = Entorno()
            coEvery { e.usuariosApi.login(any()) } throws IOException("timeout")

            (e.repo.iniciarSesion("ana", "x") as Respuesta.Fallo).tipo shouldBe TipoFallo.SIN_CONEXION
        }
    }

    "un alias repetido (409) se informa como CONFLICTO y un 500 como SERVIDOR" {
        runTest {
            val e = Entorno()
            coEvery { e.usuariosApi.registrar(any()) } throws http(409)
            (e.repo.registrar("Ana", "ana", "clave1") as Respuesta.Fallo).tipo shouldBe TipoFallo.CONFLICTO

            coEvery { e.usuariosApi.registrar(any()) } throws http(500)
            (e.repo.registrar("Ana", "ana", "clave1") as Respuesta.Fallo).tipo shouldBe TipoFallo.SERVIDOR
        }
    }

    "el progreso se manda con el id que el servidor dio en el login" {
        runTest {
            val e = Entorno()
            coEvery { e.usuariosApi.login(any()) } returns ana
            coEvery { e.usuariosApi.completarCapsula(any(), any(), any()) } returns ProgresoRespuesta(7, 3, true, 35)
            e.repo.iniciarSesion("ana", "clave1")

            e.repo.notificarProgreso("ana", 3, 15)

            coVerify(exactly = 1) { e.usuariosApi.completarCapsula(7L, 3L, ProgresoPeticion(15)) }
            coVerify(exactly = 0) { e.usuariosApi.listar() }
        }
    }

    "si no conoce el id, lo busca en la lista de usuarios del servidor" {
        runTest {
            val e = Entorno()
            coEvery { e.usuariosApi.listar() } returns listOf(ana)

            e.repo.notificarProgreso("ANA", 3, 15)

            coVerify(exactly = 1) { e.usuariosApi.completarCapsula(7L, 3L, ProgresoPeticion(15)) }
        }
    }

    "si el usuario no existe en el servidor no se manda el progreso" {
        runTest {
            val e = Entorno()
            coEvery { e.usuariosApi.listar() } returns emptyList()

            e.repo.notificarProgreso("fantasma", 3, 15)

            coVerify(exactly = 0) { e.usuariosApi.completarCapsula(any(), any(), any()) }
        }
    }

    "sincronizar copia puntos y cápsulas del servidor a la base local" {
        runTest {
            val e = Entorno()
            coEvery { e.contenidoApi.puntos() } returns listOf(PuntoRemoto(1, "Plaza", "Centro", -33.4, -70.6, 540.0))
            coEvery { e.contenidoApi.capsulas() } returns listOf(CapsulaRemota(1, 1, "IA", "u", "AIJS-001", 10))
            coEvery { e.usuariosApi.ranking() } returns emptyList()

            e.repo.sincronizar() shouldBe true

            coVerify(exactly = 1) { e.puntoDao.guardarTodos(match { it.single().nombre == "Plaza" }) }
            coVerify(exactly = 1) { e.capsulaDao.guardarTodas(match { it.single().codigoQr == "AIJS-001" }) }
        }
    }

    "un servidor sin puntos no pisa los datos locales" {
        runTest {
            val e = Entorno()
            coEvery { e.contenidoApi.puntos() } returns emptyList()
            coEvery { e.contenidoApi.capsulas() } returns emptyList()
            coEvery { e.usuariosApi.ranking() } returns emptyList()

            e.repo.sincronizar() shouldBe true

            coVerify(exactly = 0) { e.puntoDao.guardarTodos(any()) }
        }
    }

    "sincronizar sin conexión devuelve false y no toca la base" {
        runTest {
            val e = Entorno()
            coEvery { e.contenidoApi.puntos() } throws IOException()
            coEvery { e.usuariosApi.ranking() } throws IOException()

            e.repo.sincronizar() shouldBe false

            coVerify(exactly = 0) { e.puntoDao.guardarTodos(any()) }
            coVerify(exactly = 0) { e.usuarioDao.insertarJugador(any()) }
        }
    }

    "el ranking agrega jugadores de otros teléfonos y sube puntajes, sin bajarlos" {
        runTest {
            val e = Entorno()
            coEvery { e.contenidoApi.puntos() } returns emptyList()
            coEvery { e.contenidoApi.capsulas() } returns emptyList()
            coEvery { e.usuariosApi.ranking() } returns listOf(
                UsuarioRemoto(1, "nuevo", "Nuevo", 50),
                UsuarioRemoto(2, "sube", "Sube", 40),
                UsuarioRemoto(3, "baja", "Baja", 5)
            )
            coEvery { e.usuarioDao.buscarPorAlias("nuevo") } returns null
            coEvery { e.usuarioDao.buscarPorAlias("sube") } returns UsuarioEntity(id = 20, nombreUsuario = "sube", claveHash = "h", nombreVisible = "Sube", puntaje = 10)
            coEvery { e.usuarioDao.buscarPorAlias("baja") } returns UsuarioEntity(id = 30, nombreUsuario = "baja", claveHash = "h", nombreVisible = "Baja", puntaje = 25)
            coEvery { e.usuarioDao.insertarJugador(any()) } returns 99L

            e.repo.sincronizar()

            coVerify(exactly = 1) { e.usuarioDao.insertarJugador(match { it.nombreUsuario == "nuevo" && it.claveHash == "" && it.puntaje == 50 }) }
            coVerify(exactly = 1) { e.usuarioDao.actualizarPuntaje(20L, 40) }
            coVerify(exactly = 0) { e.usuarioDao.actualizarPuntaje(30L, any()) }
        }
    }

    "guardar un punto nuevo hace POST y uno existente hace PUT" {
        runTest {
            val e = Entorno()
            val nuevo = PuntoTuristicoEntity(id = 0, nombre = "N", descripcion = "d", latitud = 1.0, longitud = 2.0, altitud = 3.0)
            coEvery { e.contenidoApi.crearPunto(any()) } returns PuntoRemoto(8, "N", "d", 1.0, 2.0, 3.0)
            coEvery { e.contenidoApi.actualizarPunto(any(), any()) } returns PuntoRemoto(5, "N", "d", 1.0, 2.0, 3.0)

            (e.repo.guardarPunto(nuevo) is Respuesta.Ok) shouldBe true
            e.repo.guardarPunto(nuevo.copy(id = 5))

            coVerify(exactly = 1) { e.contenidoApi.crearPunto(any()) }
            coVerify(exactly = 1) { e.contenidoApi.actualizarPunto(5L, any()) }
        }
    }

    "los mensajes para el usuario dependen del tipo de fallo" {
        val e = Entorno()

        e.repo.mensajeDe(Respuesta.Fallo(TipoFallo.SIN_CONEXION), localmente = true) shouldBe
            "Sin conexión con el servidor: se guardó solo en este teléfono."
        e.repo.mensajeDe(Respuesta.Fallo(TipoFallo.CONFLICTO), localmente = false) shouldBe
            "Ya existe un elemento con ese código QR."
    }

    "avisar deja el mensaje disponible hasta que se marque como visto" {
        val e = Entorno()

        e.repo.avisar("hola")
        e.repo.aviso.value shouldBe "hola"
        e.repo.avisoVisto()
        e.repo.aviso.value shouldBe null
    }
})
