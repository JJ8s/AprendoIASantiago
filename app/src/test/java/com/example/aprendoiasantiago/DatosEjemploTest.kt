package com.example.aprendoiasantiago

import com.example.aprendoiasantiago.data.local.DatosEjemplo
import com.example.aprendoiasantiago.utils.Hash
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Comprueba que los datos ficticios sean coherentes antes de llegar a la base de datos. */
class DatosEjemploTest {

    @Test
    fun losCodigosQr_sonUnicos() {
        val codigos = DatosEjemplo.capsulas.map { it.codigoQr }
        assertEquals(codigos.size, codigos.toSet().size)
    }

    @Test
    fun losIds_sonUnicosEnCadaLista() {
        assertEquals(DatosEjemplo.puntos.size, DatosEjemplo.puntos.map { it.id }.toSet().size)
        assertEquals(DatosEjemplo.capsulas.size, DatosEjemplo.capsulas.map { it.id }.toSet().size)
        assertEquals(DatosEjemplo.usuarios.size, DatosEjemplo.usuarios.map { it.id }.toSet().size)
    }

    @Test
    fun cadaCapsula_apuntaAUnPuntoExistente() {
        val idsPuntos = DatosEjemplo.puntos.map { it.id }.toSet()
        assertTrue(DatosEjemplo.capsulas.all { it.puntoId in idsPuntos })
    }

    @Test
    fun lasCoordenadas_estanDentroDeSantiago() {
        assertTrue(
            DatosEjemplo.puntos.all { it.latitud in -33.65..-33.30 && it.longitud in -70.80..-70.50 }
        )
    }

    @Test
    fun losPuntajes_noSonNegativos() {
        assertTrue(DatosEjemplo.capsulas.all { it.puntos > 0 })
        assertTrue(DatosEjemplo.usuarios.all { it.puntaje >= 0 })
    }

    @Test
    fun usuarioDemo_guardaLaClaveHasheada_noEnTextoPlano() {
        val demo = DatosEjemplo.usuarios.first { it.nombreUsuario == DatosEjemplo.USUARIO_DEMO }
        assertEquals(Hash.sha256(DatosEjemplo.CLAVE_DEMO), demo.claveHash)
    }

    @Test
    fun usuariosDelRanking_noPuedenIniciarSesion() {
        val conAcceso = setOf(
            DatosEjemplo.USUARIO_DEMO,
            DatosEjemplo.USUARIO_ADMIN,
            DatosEjemplo.USUARIO_CREADOR
        )
        val sinAcceso = DatosEjemplo.usuarios.filter { it.nombreUsuario !in conAcceso }
        assertTrue(sinAcceso.all { it.claveHash.isEmpty() })
    }
}
