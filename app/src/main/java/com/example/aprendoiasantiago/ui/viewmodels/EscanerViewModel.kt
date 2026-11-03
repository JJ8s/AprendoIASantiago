package com.example.aprendoiasantiago.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.ResultadoEscaneo
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.ubicacion.ProveedorUbicacion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EscanerUiState(
    /** Mientras es `true` se ignoran nuevas lecturas (evita contar dos veces el mismo QR). */
    val procesando: Boolean = false,
    val resultado: ResultadoEscaneo? = null
) {
    /** La cámara solo analiza imágenes cuando no hay un resultado en pantalla. */
    val escaneando: Boolean get() = !procesando && resultado == null
}

class EscanerViewModel(
    private val juego: JuegoRepository,
    private val sesion: SesionRepository,
    private val ubicacion: ProveedorUbicacion
) : ViewModel() {

    private val _estado = MutableStateFlow(EscanerUiState())
    val estado: StateFlow<EscanerUiState> = _estado.asStateFlow()

    /** Se llama cada vez que la cámara lee un QR (o se escribe un código a mano). */
    fun onCodigoDetectado(codigo: String) {
        if (!_estado.value.escaneando) return
        val usuarioId = sesion.usuarioId.value ?: return

        _estado.update { it.copy(procesando = true) }
        viewModelScope.launch {
            val resultado = try {
                juego.procesarCodigo(usuarioId, codigo) { ubicacion.actual() }
            } catch (e: CancellationException) {
                throw e // Nunca se traga la cancelación de una corrutina.
            } catch (e: Exception) {
                ResultadoEscaneo.Error
            }
            _estado.value = EscanerUiState(resultado = resultado)
        }
    }

    /** Descarta el resultado y vuelve a activar la lectura. */
    fun escanearOtro() {
        _estado.value = EscanerUiState()
    }
}
