package com.example.aprendoiasantiago

import com.example.aprendoiasantiago.data.local.CapsulaDao
import com.example.aprendoiasantiago.data.local.CapsulaEntity
import com.example.aprendoiasantiago.data.local.DatosEjemplo
import com.example.aprendoiasantiago.data.local.ProgresoDao
import com.example.aprendoiasantiago.data.local.ProgresoEntity
import com.example.aprendoiasantiago.data.local.PuntoDao
import com.example.aprendoiasantiago.data.local.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.local.UsuarioDao
import com.example.aprendoiasantiago.data.local.UsuarioEntity
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.ResultadoEscaneo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Prueba la lógica de puntos con DAOs falsos en memoria (sin base de datos real ni Android). */
class JuegoRepositoryTest {

    private val usuarioId = 1L
    private lateinit var progreso: DaoProgresoFalso
    private lateinit var repositorio: JuegoRepository

    @Before
    fun preparar() {
        progreso = DaoProgresoFalso()
        repositorio = JuegoRepository(
            puntoDao = DaoPuntoFalso(),
            capsulaDao = DaoCapsulaFalso(),
            usuarioDao = DaoUsuarioFalso(),
            progresoDao = progreso
        )
    }

    @Test
    fun codigoValido_laPrimeraVez_sumaLosPuntosDeLaCapsula() = runBlocking {
        val capsula = DatosEjemplo.capsulas.first()

        val resultado = repositorio.procesarCodigo(usuarioId, capsula.codigoQr)

        assertTrue(resultado is ResultadoEscaneo.Valido)
        resultado as ResultadoEscaneo.Valido
        assertTrue(resultado.esNueva)
        assertEquals(capsula.puntos, resultado.puntosGanados)
        assertEquals(capsula.puntos, progreso.puntaje(usuarioId))
    }

    @Test
    fun mismoCodigoDosVeces_noSumaPuntosOtraVez() = runBlocking {
        val capsula = DatosEjemplo.capsulas.first()

        repositorio.procesarCodigo(usuarioId, capsula.codigoQr)
        val segundo = repositorio.procesarCodigo(usuarioId, capsula.codigoQr)

        segundo as ResultadoEscaneo.Valido
        assertFalse(segundo.esNueva)
        assertEquals(0, segundo.puntosGanados)
        assertEquals(capsula.puntos, progreso.puntaje(usuarioId))
    }

    @Test
    fun codigoConEspaciosYMinusculas_seReconoceIgual() = runBlocking {
        val capsula = DatosEjemplo.capsulas.first()

        val resultado = repositorio.procesarCodigo(usuarioId, "  ${capsula.codigoQr.lowercase()}  ")

        assertTrue(resultado is ResultadoEscaneo.Valido)
    }

    @Test
    fun capsulasDistintas_acumulanPuntos() = runBlocking {
        val (primera, segunda) = DatosEjemplo.capsulas

        repositorio.procesarCodigo(usuarioId, primera.codigoQr)
        repositorio.procesarCodigo(usuarioId, segunda.codigoQr)

        assertEquals(primera.puntos + segunda.puntos, progreso.puntaje(usuarioId))
    }

    @Test
    fun codigoDesconocido_noCambiaElPuntaje() = runBlocking {
        val resultado = repositorio.procesarCodigo(usuarioId, "CODIGO-INVENTADO")

        assertEquals(ResultadoEscaneo.Desconocido("CODIGO-INVENTADO"), resultado)
        assertEquals(0, progreso.puntaje(usuarioId))
    }
}

// ---- DAOs falsos: guardan todo en memoria ----

private class DaoCapsulaFalso : CapsulaDao {
    private val capsulas = DatosEjemplo.capsulas

    override fun observarTodas(): Flow<List<CapsulaEntity>> = flowOf(capsulas)
    override fun observarCantidad(): Flow<Int> = flowOf(capsulas.size)
    override suspend fun buscarPorId(id: Long): CapsulaEntity? = capsulas.firstOrNull { it.id == id }
    override suspend fun buscarPorCodigo(codigo: String): CapsulaEntity? =
        capsulas.firstOrNull { it.codigoQr.equals(codigo, ignoreCase = true) }

    override suspend fun guardarTodas(capsulas: List<CapsulaEntity>) = Unit
}

private class DaoPuntoFalso : PuntoDao {
    private val puntos = DatosEjemplo.puntos

    override fun observarTodos(): Flow<List<PuntoTuristicoEntity>> = flowOf(puntos)
    override suspend fun buscarPorId(id: Long): PuntoTuristicoEntity? = puntos.firstOrNull { it.id == id }
    override suspend fun guardarTodos(puntos: List<PuntoTuristicoEntity>) = Unit
}

private class DaoUsuarioFalso : UsuarioDao {
    override suspend fun buscarPorCredenciales(nombreUsuario: String, claveHash: String): UsuarioEntity? = null
    override fun observarPorId(id: Long): Flow<UsuarioEntity?> = flowOf(null)
    override fun observarRanking(): Flow<List<UsuarioEntity>> = flowOf(emptyList())
    override suspend fun contar(): Int = 0
    override suspend fun insertarTodos(usuarios: List<UsuarioEntity>) = Unit
}

private class DaoProgresoFalso : ProgresoDao() {
    private val completadas = mutableSetOf<Pair<Long, Long>>()
    private val puntajes = mutableMapOf<Long, Int>()

    fun puntaje(usuarioId: Long): Int = puntajes[usuarioId] ?: 0

    override fun observarCompletadas(usuarioId: Long): Flow<Int> =
        flowOf(completadas.count { it.first == usuarioId })

    // Igual que Room con OnConflictStrategy.IGNORE: devuelve -1 si la fila ya existía.
    override suspend fun registrarSiNuevo(progreso: ProgresoEntity): Long =
        if (completadas.add(progreso.usuarioId to progreso.capsulaId)) completadas.size.toLong() else -1L

    override suspend fun sumarPuntos(usuarioId: Long, puntos: Int) {
        puntajes[usuarioId] = puntaje(usuarioId) + puntos
    }
}
