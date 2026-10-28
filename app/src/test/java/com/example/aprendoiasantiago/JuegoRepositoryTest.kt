package com.example.aprendoiasantiago

import com.example.aprendoiasantiago.data.local.CapsulaDao
import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.local.DatosEjemplo
import com.example.aprendoiasantiago.data.local.ProgresoDao
import com.example.aprendoiasantiago.data.model.ProgresoEntity
import com.example.aprendoiasantiago.data.local.PuntoDao
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.local.RutaDao
import com.example.aprendoiasantiago.data.model.RutaEntity
import com.example.aprendoiasantiago.data.model.RutaPuntoEntity
import com.example.aprendoiasantiago.data.local.UsuarioDao
import com.example.aprendoiasantiago.data.model.UsuarioEntity
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.ResultadoEscaneo
import com.example.aprendoiasantiago.domain.UbicacionUsuario
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
    private val HORA_FIJA = 1_760_000_000_000L
    private lateinit var progreso: DaoProgresoFalso
    private lateinit var repositorio: JuegoRepository

    @Before
    fun preparar() {
        progreso = DaoProgresoFalso()
        repositorio = JuegoRepository(
            puntoDao = DaoPuntoFalso(),
            capsulaDao = DaoCapsulaFalso(),
            usuarioDao = DaoUsuarioFalso(),
            progresoDao = progreso,
            rutaDao = DaoRutaFalso(),
            reloj = { HORA_FIJA }
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
    fun alCompletarUnaCapsula_seGuardaLaFechaDelMomento() = runBlocking {
        val capsula = DatosEjemplo.capsulas.first()

        repositorio.procesarCodigo(usuarioId, capsula.codigoQr)

        assertEquals(HORA_FIJA, progreso.fechaDe(usuarioId, capsula.id))
    }

    @Test
    fun alCompletarUnaCapsula_seGuardaLaUbicacionDelUsuario() = runBlocking {
        val capsula = DatosEjemplo.capsulas.first()
        val ubicacion = UbicacionUsuario(latitud = -33.4372, longitud = -70.6506, altitud = 541.0)

        repositorio.procesarCodigo(usuarioId, capsula.codigoQr) { ubicacion }

        val avance = repositorio.progreso(usuarioId, capsula.id)
        assertEquals(-33.4372, avance?.latitud ?: 0.0, 1e-9)
        assertEquals(-70.6506, avance?.longitud ?: 0.0, 1e-9)
        assertEquals(541.0, avance?.altitud ?: 0.0, 1e-9)
    }

    @Test
    fun sinUbicacion_laCapsulaSeCompletaIgual_yLaUbicacionQuedaVacia() = runBlocking {
        val capsula = DatosEjemplo.capsulas.first()

        val resultado = repositorio.procesarCodigo(usuarioId, capsula.codigoQr) { null }

        assertTrue(resultado is ResultadoEscaneo.Valido)
        val avance = repositorio.progreso(usuarioId, capsula.id)
        assertEquals(null, avance?.latitud)
        assertEquals(null, avance?.altitud)
    }

    @Test
    fun codigoDesconocido_noConsultaElGps() = runBlocking {
        var consultas = 0

        repositorio.procesarCodigo(usuarioId, "CODIGO-INVENTADO") {
            consultas++
            null
        }

        assertEquals(0, consultas)
    }

    @Test
    fun evidencia_seGuardaEnLaCapsulaCompletada() = runBlocking {
        val capsula = DatosEjemplo.capsulas.first()
        repositorio.procesarCodigo(usuarioId, capsula.codigoQr)

        repositorio.guardarSelfie(usuarioId, capsula.id, "file:///selfie.jpg")
        repositorio.guardarFotoEntorno(usuarioId, capsula.id, "file:///entorno.jpg")

        val avance = repositorio.progreso(usuarioId, capsula.id)
        assertEquals("file:///selfie.jpg", avance?.selfieUri)
        assertEquals("file:///entorno.jpg", avance?.fotoEntornoUri)
    }

    @Test
    fun capsulaSinCompletar_noTieneProgreso() = runBlocking {
        assertEquals(null, repositorio.progreso(usuarioId, DatosEjemplo.capsulas.first().id))
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
    override fun observarPerfilesActivos(): Flow<List<UsuarioEntity>> = flowOf(emptyList())
    override suspend fun insertarTodos(usuarios: List<UsuarioEntity>) = Unit
    override suspend fun insertarJugador(usuario: UsuarioEntity): Long = -1L
}

private class DaoRutaFalso : RutaDao {
    override fun observarRutas(): Flow<List<RutaEntity>> = flowOf(DatosEjemplo.rutas)
    override fun observarParadas(): Flow<List<RutaPuntoEntity>> = flowOf(DatosEjemplo.paradas)
    override suspend fun guardarRutas(rutas: List<RutaEntity>) = Unit
    override suspend fun guardarParadas(paradas: List<RutaPuntoEntity>) = Unit
}

private class DaoProgresoFalso : ProgresoDao() {
    private val completadas = mutableSetOf<Pair<Long, Long>>()
    private val fechas = mutableMapOf<Pair<Long, Long>, Long?>()
    private val ubicaciones = mutableMapOf<Pair<Long, Long>, Triple<Double?, Double?, Double?>>()
    private val selfies = mutableMapOf<Pair<Long, Long>, String>()
    private val entornos = mutableMapOf<Pair<Long, Long>, String>()
    private val puntajes = mutableMapOf<Long, Int>()

    fun fechaDe(usuarioId: Long, capsulaId: Long): Long? = fechas[usuarioId to capsulaId]

    fun puntaje(usuarioId: Long): Int = puntajes[usuarioId] ?: 0

    override fun observarCompletadas(usuarioId: Long): Flow<Int> =
        flowOf(completadas.count { it.first == usuarioId })

    override fun observarIdsCompletadas(usuarioId: Long): Flow<List<Long>> =
        flowOf(completadas.filter { it.first == usuarioId }.map { it.second })

    // Igual que Room con OnConflictStrategy.IGNORE: devuelve -1 si la fila ya existía.
    override suspend fun registrarSiNuevo(progreso: ProgresoEntity): Long {
        val clave = progreso.usuarioId to progreso.capsulaId
        if (!completadas.add(clave)) return -1L
        fechas[clave] = progreso.fechaMillis
        ubicaciones[clave] = Triple(progreso.latitud, progreso.longitud, progreso.altitud)
        return completadas.size.toLong()
    }

    override suspend fun buscar(usuarioId: Long, capsulaId: Long): ProgresoEntity? {
        val clave = usuarioId to capsulaId
        if (clave !in completadas) return null
        return ProgresoEntity(
            usuarioId, capsulaId, completada = true, fechaMillis = fechas[clave],
            latitud = ubicaciones[clave]?.first, longitud = ubicaciones[clave]?.second,
            altitud = ubicaciones[clave]?.third,
            selfieUri = selfies[clave], fotoEntornoUri = entornos[clave]
        )
    }

    override suspend fun guardarSelfie(usuarioId: Long, capsulaId: Long, uri: String) {
        selfies[usuarioId to capsulaId] = uri
    }

    override suspend fun guardarFotoEntorno(usuarioId: Long, capsulaId: Long, uri: String) {
        entornos[usuarioId to capsulaId] = uri
    }

    override suspend fun sumarPuntos(usuarioId: Long, puntos: Int) {
        puntajes[usuarioId] = puntaje(usuarioId) + puntos
    }
}
