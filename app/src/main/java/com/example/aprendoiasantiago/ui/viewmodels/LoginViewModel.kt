package com.example.aprendoiasantiago.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun onUsuarioCambia(valor: String) {
        _estado.update { it.copy(usuario = valor, error = null) }
    }

    fun onClaveCambia(valor: String) {
        _estado.update { it.copy(clave = valor, error = null) }
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
                throw e // Nunca se traga la cancelación de una corrutina.
            } catch (e: Exception) {
                _estado.update { it.copy(cargando = false, error = ErrorLogin.ERROR_INESPERADO) }
            }
        }
    }
}
