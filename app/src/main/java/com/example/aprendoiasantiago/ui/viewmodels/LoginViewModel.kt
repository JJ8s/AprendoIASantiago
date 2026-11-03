package com.example.aprendoiasantiago.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendoiasantiago.data.model.UsuarioEntity
import com.example.aprendoiasantiago.data.repository.AuthRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.domain.ErrorLogin
import com.example.aprendoiasantiago.domain.ValidadorLogin
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",
    val confirmarClave: String = "",
    val nombreVisibleRegistro: String = "",
    val avatarSeleccionado: Int = 0,
    val modoRegistro: Boolean = false,
    val perfilesDisponibles: List<UsuarioEntity> = emptyList(),
    val cargando: Boolean = false,
    val error: ErrorLogin? = null,
    val autenticado: Boolean = false
)

class LoginViewModel(
    private val auth: AuthRepository,
    private val sesion: SesionRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    init {
        // Escuchamos en vivo la tabla de usuarios de SQLite para mostrar los perfiles en Pantalla 3
        viewModelScope.launch {
            auth.observarPerfilesActivos().collect { lista ->
                _estado.update { it.copy(perfilesDisponibles = lista) }
            }
        }
    }

    fun onUsuarioCambia(valor: String) {
        _estado.update { it.copy(usuario = valor, error = null) }
    }

    fun onClaveCambia(valor: String) {
        _estado.update { it.copy(clave = valor, error = null) }
    }

    fun onConfirmarClaveCambia(valor: String) {
        _estado.update { it.copy(confirmarClave = valor, error = null) }
    }

    fun onNombreRegistroCambia(valor: String) {
        _estado.update { it.copy(nombreVisibleRegistro = valor, error = null) }
    }

    fun onAvatarCambia(indice: Int) {
        _estado.update { it.copy(avatarSeleccionado = indice) }
    }

    fun cambiarModoRegistro(activo: Boolean) {
        _estado.update {
            it.copy(
                modoRegistro = activo,
                clave = "",
                confirmarClave = "",
                error = null
            )
        }
    }

    fun seleccionarPerfil(perfil: UsuarioEntity) {
        _estado.update {
            it.copy(
                usuario = perfil.nombreUsuario,
                clave = "",
                confirmarClave = "",
                modoRegistro = false,
                error = null
            )
        }
    }

    fun iniciarSesion() {
        val actual = _estado.value
        if (actual.cargando) return

        val errorValidacion = ValidadorLogin.validar(actual.usuario, actual.clave)
        if (errorValidacion != null) {
            _estado.update { it.copy(error = errorValidacion) }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            try {
                val usuario = auth.iniciarSesion(actual.usuario, actual.clave)
                if (usuario == null) {
                    _estado.update {
                        it.copy(cargando = false, error = ErrorLogin.CREDENCIALES_INCORRECTAS)
                    }
                } else {
                    sesion.iniciar(usuario.id)
                    _estado.update { it.copy(cargando = false, autenticado = true) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update { it.copy(cargando = false, error = ErrorLogin.ERROR_INESPERADO) }
            }
        }
    }

    fun registrarJugador() {
        val actual = _estado.value
        if (actual.cargando) return

        val errorValidacion = ValidadorLogin.validarRegistro(
            nombreVisible = actual.nombreVisibleRegistro,
            usuario = actual.usuario,
            clave = actual.clave,
            confirmacion = actual.confirmarClave
        )
        if (errorValidacion != null) {
            _estado.update { it.copy(error = errorValidacion) }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            try {
                val nuevoUsuario = auth.registrarJugador(
                    nombreVisible = actual.nombreVisibleRegistro,
                    alias = actual.usuario,
                    clave = actual.clave
                )
                if (nuevoUsuario == null) {
                    _estado.update {
                        it.copy(cargando = false, error = ErrorLogin.USUARIO_YA_EXISTE)
                    }
                } else {
                    // Apenas se registra en SQLite, iniciamos su sesión automáticamente
                    sesion.iniciar(nuevoUsuario.id)
                    _estado.update { it.copy(cargando = false, autenticado = true) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update { it.copy(cargando = false, error = ErrorLogin.ERROR_INESPERADO) }
            }
        }
    }
}