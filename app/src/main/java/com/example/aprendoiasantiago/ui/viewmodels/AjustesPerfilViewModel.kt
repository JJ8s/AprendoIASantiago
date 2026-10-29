package com.example.aprendoiasantiago.ui.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendoiasantiago.data.local.AlmacenEvidencias
import com.example.aprendoiasantiago.data.local.NotificacionEntity
import com.example.aprendoiasantiago.data.local.UsuarioEntity
import com.example.aprendoiasantiago.data.repository.NotificacionesRepository
import com.example.aprendoiasantiago.data.repository.PerfilRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.domain.ErrorPerfil
import com.example.aprendoiasantiago.domain.TipoNotificacion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Confirmaciones breves que la pantalla muestra una vez. */
enum class MensajePerfil { DATOS_GUARDADOS, CLAVE_CAMBIADA, AVATAR_ACTUALIZADO, AVISO_DE_PRUEBA }

data class AjustesPerfilUiState(
    val usuario: UsuarioEntity? = null,
    val bandeja: List<NotificacionEntity> = emptyList(),
    val errorDatos: ErrorPerfil? = null,
    val errorClave: ErrorPerfil? = null,
    val mensaje: MensajePerfil? = null,
    val ocupado: Boolean = false
) {
    val sinLeer: Int get() = bandeja.count { !it.leida }
}

/** Estado local de la pantalla (errores y mensajes); lo persistente viene de Room. */
private data class Local(
    val errorDatos: ErrorPerfil? = null,
    val errorClave: ErrorPerfil? = null,
    val mensaje: MensajePerfil? = null,
    val ocupado: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class AjustesPerfilViewModel(
    private val perfil: PerfilRepository,
    private val notificaciones: NotificacionesRepository,
    private val sesion: SesionRepository,
    private val almacenAvatares: AlmacenEvidencias
) : ViewModel() {

    private val local = MutableStateFlow(Local())

    val estado: StateFlow<AjustesPerfilUiState> = sesion.usuarioId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(Pair<UsuarioEntity?, List<NotificacionEntity>>(null, emptyList()))
            } else {
                combine(perfil.usuario(id), notificaciones.observar(id)) { usuario, bandeja ->
                    Pair(usuario, bandeja)
                }
            }
        }
        .combine(local) { (usuario, bandeja), l ->
            AjustesPerfilUiState(usuario, bandeja, l.errorDatos, l.errorClave, l.mensaje, l.ocupado)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AjustesPerfilUiState())

    private fun idActual(): Long? = sesion.usuarioId.value

    /** Ejecuta [bloque] sin dejar que un fallo cierre la app; cualquier excepción se informa como error. */
    private fun ejecutar(alFallar: (Local) -> Local, bloque: suspend (Long) -> Unit) {
        val id = idActual() ?: return
        viewModelScope.launch {
            try {
                bloque(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                local.update(alFallar)
            }
        }
    }

    fun guardarDatos(nombre: String, correo: String, telefono: String) {
        ejecutar({ it.copy(errorDatos = ErrorPerfil.ERROR_INESPERADO) }) { id ->
            val error = perfil.guardarDatos(id, nombre, correo, telefono)
            local.update {
                it.copy(
                    errorDatos = error,
                    mensaje = if (error == null) MensajePerfil.DATOS_GUARDADOS else it.mensaje
                )
            }
        }
    }

    fun cambiarClave(actual: String, nueva: String, confirmacion: String) {
        ejecutar({ it.copy(errorClave = ErrorPerfil.ERROR_INESPERADO) }) { id ->
            val error = perfil.cambiarClave(id, actual, nueva, confirmacion)
            local.update {
                it.copy(
                    errorClave = error,
                    mensaje = if (error == null) MensajePerfil.CLAVE_CAMBIADA else it.mensaje
                )
            }
        }
    }

    fun elegirAvatar(indice: Int) {
        ejecutar({ it }) { id ->
            perfil.elegirAvatar(id, indice)?.let { almacenAvatares.borrar(it) }
            local.update { it.copy(mensaje = MensajePerfil.AVATAR_ACTUALIZADO) }
        }
    }

    /** Guarda la foto (recortada en cuadrado) como avatar del jugador. */
    fun usarFotoAvatar(bitmap: Bitmap) {
        ejecutar({ it }) { id ->
            val uri = withContext(Dispatchers.IO) { almacenAvatares.guardar(bitmap, "avatar_$id") }
            perfil.usarFotoAvatar(id, uri)?.let { withContext(Dispatchers.IO) { almacenAvatares.borrar(it) } }
            local.update { it.copy(mensaje = MensajePerfil.AVATAR_ACTUALIZADO) }
        }
    }

    fun guardarNotificaciones(recordatorios: Boolean, nuevosPuntos: Boolean, logros: Boolean) {
        ejecutar({ it }) { id -> perfil.guardarNotificaciones(id, recordatorios, nuevosPuntos, logros) }
    }

    fun guardarPermisos(ubicacion: Boolean, camara: Boolean) {
        ejecutar({ it }) { id -> perfil.guardarPermisos(id, ubicacion, camara) }
    }

    fun enviarAvisoDePrueba() {
        ejecutar({ it }) { id ->
            notificaciones.registrar(
                id,
                TipoNotificacion.SISTEMA,
                "Aviso de prueba",
                "Las notificaciones de Aprendo IA funcionan correctamente."
            )
            local.update { it.copy(mensaje = MensajePerfil.AVISO_DE_PRUEBA) }
        }
    }

    fun marcarLeidas() {
        ejecutar({ it }) { id -> notificaciones.marcarTodasLeidas(id) }
    }

    fun vaciarBandeja() {
        ejecutar({ it }) { id -> notificaciones.vaciar(id) }
    }

    /** La pantalla ya mostró el mensaje; se limpian mensajes y errores al entrar o salir de un formulario. */
    fun limpiarMensajes() {
        local.update { Local() }
    }

    fun cerrarSesion() {
        sesion.cerrar()
        local.value = Local()
    }
}
