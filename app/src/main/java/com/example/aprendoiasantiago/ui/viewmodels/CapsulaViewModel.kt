package com.example.aprendoiasantiago.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CapsulaUiState(
    val cargando: Boolean = true,
    val capsula: CapsulaEntity? = null,
    val punto: PuntoTuristicoEntity? = null
)

class CapsulaViewModel(private val juego: JuegoRepository) : ViewModel() {

    private val _estado = MutableStateFlow(CapsulaUiState())
    val estado: StateFlow<CapsulaUiState> = _estado.asStateFlow()

    private var idCargado: Long? = null

    /** Carga la cápsula una sola vez por id (evita recargar al rotar la pantalla). */
    fun cargar(capsulaId: Long) {
        if (idCargado == capsulaId) return
        idCargado = capsulaId
        viewModelScope.launch {
            _estado.value = try {
                val capsula = juego.obtenerCapsula(capsulaId)
                val punto = capsula?.let { juego.obtenerPunto(it.puntoId) }
                CapsulaUiState(cargando = false, capsula = capsula, punto = punto)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                CapsulaUiState(cargando = false)
            }
        }
    }
}
