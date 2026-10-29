package com.example.aprendoiasantiago

import com.example.aprendoiasantiago.data.local.UsuarioEntity
import com.example.aprendoiasantiago.data.repository.NotificacionesRepository
import com.example.aprendoiasantiago.data.repository.PerfilRepository
import com.example.aprendoiasantiago.domain.ErrorPerfil
import com.example.aprendoiasantiago.domain.TipoNotificacion
import com.example.aprendoiasantiago.notificaciones.EmisorNotificaciones
import com.example.aprendoiasantiago.utils.Hash
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PerfilRepositoryTest {
    private lateinit var usuarios: UsuarioDaoEnMemoria
    private lateinit var perfil: PerfilRepository

    @Before
    fun preparar() {
        usuarios = UsuarioDaoEnMemoria(
            UsuarioEntity(id = 1, nombreUsuario = "ana", claveHash = Hash.sha256("vieja1"), nombreVisible = "Ana"),
            UsuarioEntity(id = 2, nombreUsuario = "beto", claveHash = Hash.sha256("clave2"), nombreVisible = "Beto")
        )
        perfil = PerfilRepository(usuarios)
    }

    @Test
    fun guardarDatos_validos_persisteSoloEnEseUsuario() = runBlocking {
        val error = perfil.guardarDatos(1, "  Ana María ", " ana@correo.cl ", "+56 9 1234 5678")
        assertNull(error)
        val ana = usuarios.buscarPorId(1)!!
        assertEquals("Ana María", ana.nombreVisible)
        assertEquals("ana@correo.cl", ana.correo)
        assertEquals("+56 9 1234 5678", ana.telefono)
        assertEquals("Beto", usuarios.buscarPorId(2)!!.nombreVisible)
    }

    @Test
    fun guardarDatos_invalidos_noCambiaNada() = runBlocking {
        assertEquals(ErrorPerfil.CORREO_INVALIDO, perfil.guardarDatos(1, "Ana", "malo", ""))
        assertEquals("Ana", usuarios.buscarPorId(1)!!.nombreVisible)
        assertEquals("", usuarios.buscarPorId(1)!!.correo)
    }

    @Test
    fun cambiarClave_conActualCorrecta_guardaElNuevoHash() = runBlocking {
        assertNull(perfil.cambiarClave(1, "vieja1", "nueva22", "nueva22"))
        assertEquals(Hash.sha256("nueva22"), usuarios.buscarPorId(1)!!.claveHash)
    }

    @Test
    fun cambiarClave_conActualIncorrecta_noCambia() = runBlocking {
        assertEquals(ErrorPerfil.CLAVE_ACTUAL_INCORRECTA, perfil.cambiarClave(1, "mala", "nueva22", "nueva22"))
        assertEquals(Hash.sha256("vieja1"), usuarios.buscarPorId(1)!!.claveHash)
    }

    @Test
    fun elegirAvatarPrediseniado_quitaLaFotoPropia() = runBlocking {
        perfil.usarFotoAvatar(1, "file:///avatares/a.jpg")
        val fotoAnterior = perfil.elegirAvatar(1, 3)
        assertEquals("file:///avatares/a.jpg", fotoAnterior)
        val ana = usuarios.buscarPorId(1)!!
        assertEquals(3, ana.avatarIndice)
        assertNull(ana.avatarUri)
    }

    @Test
    fun nuevaFoto_devuelveLaAnteriorParaBorrarla() = runBlocking {
        assertNull(perfil.usarFotoAvatar(1, "file:///avatares/a.jpg"))
        assertEquals("file:///avatares/a.jpg", perfil.usarFotoAvatar(1, "file:///avatares/b.jpg"))
        assertEquals("file:///avatares/b.jpg", usuarios.buscarPorId(1)!!.avatarUri)
    }

    @Test
    fun preferenciasYPermisos_sonPorJugador() = runBlocking {
        perfil.guardarNotificaciones(1, recordatorios = false, nuevosPuntos = true, logros = false)
        perfil.guardarPermisos(1, ubicacion = false, camara = true)
        val ana = usuarios.buscarPorId(1)!!
        assertFalse(ana.notifRecordatorios)
        assertFalse(ana.notifLogros)
        assertFalse(perfil.ubicacionPermitida(1))
        // Beto conserva los valores por defecto.
        assertTrue(usuarios.buscarPorId(2)!!.notifRecordatorios)
        assertTrue(perfil.ubicacionPermitida(2))
    }

    // --- Notificaciones ---

    private class EmisorFalso : EmisorNotificaciones {
        val enviados = mutableListOf<String>()
        override fun mostrar(titulo: String, mensaje: String) { enviados += titulo }
    }

    @Test
    fun aviso_activado_seGuardaYSeMuestra() = runBlocking {
        val dao = NotificacionDaoEnMemoria()
        val emisor = EmisorFalso()
        val repo = NotificacionesRepository(dao, usuarios, emisor) { 1_000L }

        assertTrue(repo.registrar(1, TipoNotificacion.LOGRO, "¡10 puntos!", "Bien"))
        assertEquals(1, dao.filas.value.size)
        assertEquals(1_000L, dao.filas.value.first().fechaMillis)
        assertEquals(listOf("¡10 puntos!"), emisor.enviados)
    }

    @Test
    fun aviso_desactivadoEnElPerfil_noSeGuardaNiSeMuestra() = runBlocking {
        perfil.guardarNotificaciones(1, recordatorios = true, nuevosPuntos = true, logros = false)
        val dao = NotificacionDaoEnMemoria()
        val emisor = EmisorFalso()
        val repo = NotificacionesRepository(dao, usuarios, emisor)

        assertFalse(repo.registrar(1, TipoNotificacion.LOGRO, "x", "y"))
        assertTrue(dao.filas.value.isEmpty())
        assertTrue(emisor.enviados.isEmpty())
        // El aviso de prueba del sistema se entrega siempre.
        assertTrue(repo.registrar(1, TipoNotificacion.SISTEMA, "prueba", "ok"))
    }

    @Test
    fun marcarLeidasYVaciar_afectanSoloAlJugador() = runBlocking {
        val dao = NotificacionDaoEnMemoria()
        val repo = NotificacionesRepository(dao, usuarios, EmisorFalso())
        repo.registrar(1, TipoNotificacion.SISTEMA, "a", "a")
        repo.registrar(2, TipoNotificacion.SISTEMA, "b", "b")

        repo.marcarTodasLeidas(1)
        assertTrue(dao.filas.value.first { it.usuarioId == 1L }.leida)
        assertFalse(dao.filas.value.first { it.usuarioId == 2L }.leida)

        repo.vaciar(1)
        assertEquals(1, dao.filas.value.size)
    }

    @Test
    fun usuarioInexistente_noRegistraNada() = runBlocking {
        val repo = NotificacionesRepository(NotificacionDaoEnMemoria(), usuarios, EmisorFalso())
        assertFalse(repo.registrar(99, TipoNotificacion.SISTEMA, "x", "y"))
    }
}
