package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.local.CapsulaDao
import com.example.aprendoiasantiago.data.local.PuntoDao
import com.example.aprendoiasantiago.data.local.UsuarioDao
import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.model.UsuarioEntity
import com.example.aprendoiasantiago.data.remote.CapsulaRemota
import com.example.aprendoiasantiago.data.remote.ClavePeticion
import com.example.aprendoiasantiago.data.remote.ContenidoApi
import com.example.aprendoiasantiago.data.remote.LoginPeticion
import com.example.aprendoiasantiago.data.remote.PerfilPeticion
import com.example.aprendoiasantiago.data.remote.ProgresoPeticion
import com.example.aprendoiasantiago.data.remote.PuntoRemoto
import com.example.aprendoiasantiago.data.remote.RegistroPeticion
import com.example.aprendoiasantiago.data.remote.Respuesta
import com.example.aprendoiasantiago.data.remote.TipoFallo
import com.example.aprendoiasantiago.data.remote.UsuarioRemoto
import com.example.aprendoiasantiago.data.remote.UsuariosApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Puente entre la app y los microservicios Spring Boot.
 *
 * Regla de diseño: **Room sigue siendo la base de la app** (así el juego funciona sin red) y los
 * microservicios son la fuente compartida. Por eso aquí se hace:
 *  - "empujar": mandar al servidor lo que pasa en la app (registro, perfil, progreso, CRUD de contenido);
 *  - "traer": copiar a Room lo que hay en el servidor (puntos, cápsulas, ranking).
 * Si el servidor no responde, nada se rompe: se avisa y la app sigue con sus datos locales.
 */
class ServidorRepository(
    private val usuariosApi: UsuariosApi,
    private val contenidoApi: ContenidoApi,
    private val puntoDao: PuntoDao,
    private val capsulaDao: CapsulaDao,
    private val usuarioDao: UsuarioDao,
    /** Para las tareas "en segundo plano" que no deben hacer esperar al jugador (progreso, perfil). */
    private val alcance: CoroutineScope
) {
    private val _aviso = MutableStateFlow<String?>(null)
    /** Último mensaje para el usuario (por ejemplo "Sin conexión con el servidor"). */
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    private val idsRemotos = mutableMapOf<String, Long>()

    fun avisar(texto: String) { _aviso.value = texto }
    fun avisoVisto() { _aviso.value = null }

    // ------------------------------------------------------------------------------------------
    // Usuarios
    // ------------------------------------------------------------------------------------------

    suspend fun iniciarSesion(alias: String, clave: String): Respuesta<UsuarioRemoto> =
        llamar { usuariosApi.login(LoginPeticion(alias, clave)) }.alRecordarId()

    suspend fun registrar(nombreVisible: String, alias: String, clave: String): Respuesta<UsuarioRemoto> =
        llamar { usuariosApi.registrar(RegistroPeticion(nombreVisible, alias, clave)) }.alRecordarId()

    fun notificarPerfil(alias: String, nombre: String, correo: String, telefono: String) = enSegundoPlano {
        val id = idRemoto(alias) ?: return@enSegundoPlano
        llamar { usuariosApi.actualizarPerfil(id, PerfilPeticion(nombre, correo, telefono)) }
    }

    fun notificarClave(alias: String, actual: String, nueva: String) = enSegundoPlano {
        val id = idRemoto(alias) ?: return@enSegundoPlano
        llamar { usuariosApi.cambiarClave(id, ClavePeticion(actual, nueva)) }
    }

    fun notificarEliminacion(alias: String) = enSegundoPlano {
        val id = idRemoto(alias) ?: return@enSegundoPlano
        llamar { usuariosApi.eliminar(id) }
        idsRemotos.remove(alias.lowercase())
    }

    /** Avisa al servidor que el jugador completó una cápsula y luego refresca el ranking. */
    fun notificarProgreso(alias: String, capsulaId: Long, puntos: Int) = enSegundoPlano {
        val id = idRemoto(alias) ?: return@enSegundoPlano
        llamar { usuariosApi.completarCapsula(id, capsulaId, ProgresoPeticion(puntos)) }
        sincronizarRanking()
    }

    /** Id del usuario en el servidor (se busca por alias y se recuerda). `null` si no hay conexión o no existe. */
    private suspend fun idRemoto(alias: String): Long? {
        idsRemotos[alias.lowercase()]?.let { return it }
        val lista = (llamar { usuariosApi.listar() } as? Respuesta.Ok)?.valor ?: return null
        lista.forEach { idsRemotos[it.nombreUsuario.lowercase()] = it.id }
        return idsRemotos[alias.lowercase()]
    }

    // ------------------------------------------------------------------------------------------
    // Traer datos del servidor a Room
    // ------------------------------------------------------------------------------------------

    /** Copia puntos, cápsulas y ranking del servidor a la base local. Devuelve `true` si el servidor respondió. */
    suspend fun sincronizar(): Boolean {
        val contenido = sincronizarContenido()
        val ranking = sincronizarRanking()
        return contenido && ranking
    }

    fun sincronizarEnSegundoPlano() = enSegundoPlano { sincronizar() }

    private suspend fun sincronizarContenido(): Boolean {
        val puntos = (llamar { contenidoApi.puntos() } as? Respuesta.Ok)?.valor ?: return false
        val capsulas = (llamar { contenidoApi.capsulas() } as? Respuesta.Ok)?.valor ?: return false
        if (puntos.isEmpty()) return true // un servidor vacío no debe borrar ni pisar lo que ya hay
        return try {
            puntoDao.guardarTodos(puntos.map { it.aEntidad() }) // primero los puntos: las cápsulas dependen de ellos
            capsulaDao.guardarTodas(capsulas.map { it.aEntidad() })
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun sincronizarRanking(): Boolean {
        val remotos = (llamar { usuariosApi.ranking() } as? Respuesta.Ok)?.valor ?: return false
        try {
            for (r in remotos) {
                idsRemotos[r.nombreUsuario.lowercase()] = r.id
                val local = usuarioDao.buscarPorAlias(r.nombreUsuario)
                if (local == null) {
                    // Jugador de otro teléfono: aparece en el ranking pero no puede entrar aquí (clave vacía).
                    usuarioDao.insertarJugador(
                        UsuarioEntity(nombreUsuario = r.nombreUsuario, claveHash = "", nombreVisible = r.nombreVisible, puntaje = r.puntaje)
                    )
                } else if (r.puntaje > local.puntaje) {
                    usuarioDao.actualizarPuntaje(local.id, r.puntaje)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return false
        }
        return true
    }

    // ------------------------------------------------------------------------------------------
    // CRUD de contenido (Panel de administración y Creador de contenido)
    // ------------------------------------------------------------------------------------------

    suspend fun guardarPunto(p: PuntoTuristicoEntity): Respuesta<PuntoRemoto> =
        llamar { if (p.id == 0L) contenidoApi.crearPunto(p.aRemoto()) else contenidoApi.actualizarPunto(p.id, p.aRemoto()) }

    suspend fun eliminarPunto(id: Long): Respuesta<Unit> = llamar { contenidoApi.eliminarPunto(id) }

    suspend fun guardarCapsula(c: CapsulaEntity): Respuesta<CapsulaRemota> =
        llamar { if (c.id == 0L) contenidoApi.crearCapsula(c.aRemota()) else contenidoApi.actualizarCapsula(c.id, c.aRemota()) }

    suspend fun eliminarCapsula(id: Long): Respuesta<Unit> = llamar { contenidoApi.eliminarCapsula(id) }

    /** Texto para el usuario según por qué falló la llamada. */
    fun mensajeDe(fallo: Respuesta.Fallo, localmente: Boolean): String = when (fallo.tipo) {
        TipoFallo.SIN_CONEXION ->
            if (localmente) "Sin conexión con el servidor: se guardó solo en este teléfono." else "Sin conexión con el servidor."
        TipoFallo.CONFLICTO -> "Ya existe un elemento con ese código QR."
        TipoFallo.NO_ENCONTRADO -> "El servidor no encontró ese elemento."
        TipoFallo.NO_AUTORIZADO -> "El servidor rechazó las credenciales."
        TipoFallo.SERVIDOR -> "El servidor respondió con un error (${fallo.detalle})."
    }

    // ------------------------------------------------------------------------------------------

    private fun Respuesta<UsuarioRemoto>.alRecordarId(): Respuesta<UsuarioRemoto> {
        if (this is Respuesta.Ok) idsRemotos[valor.nombreUsuario.lowercase()] = valor.id
        return this
    }

    private fun enSegundoPlano(bloque: suspend () -> Unit) {
        alcance.launch { bloque() }
    }

    private suspend fun <T> llamar(bloque: suspend () -> T): Respuesta<T> =
        try {
            Respuesta.Ok(bloque())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            Respuesta.Fallo(
                when (e.code()) {
                    401 -> TipoFallo.NO_AUTORIZADO
                    404 -> TipoFallo.NO_ENCONTRADO
                    409 -> TipoFallo.CONFLICTO
                    else -> TipoFallo.SERVIDOR
                },
                "HTTP ${e.code()}"
            )
        } catch (e: IOException) {
            Respuesta.Fallo(TipoFallo.SIN_CONEXION, e.message.orEmpty())
        } catch (e: Exception) {
            Respuesta.Fallo(TipoFallo.SERVIDOR, e.message.orEmpty())
        }
}

// ---------- Conversiones entre lo que viaja por la red y lo que guarda Room ----------

fun PuntoRemoto.aEntidad() = PuntoTuristicoEntity(id, nombre, descripcion, latitud, longitud, altitud)
fun PuntoTuristicoEntity.aRemoto() = PuntoRemoto(id, nombre, descripcion, latitud, longitud, altitud)
fun CapsulaRemota.aEntidad() = CapsulaEntity(id, puntoId, titulo, urlVideo, codigoQr, puntos)
fun CapsulaEntity.aRemota() = CapsulaRemota(id, puntoId, titulo, urlVideo, codigoQr, puntos)
