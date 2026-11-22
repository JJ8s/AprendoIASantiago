package com.example.aprendoiasantiago.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.domain.DetallePunto
import com.example.aprendoiasantiago.domain.DetallePuntos
import com.example.aprendoiasantiago.domain.UbicacionUsuario
import com.example.aprendoiasantiago.ubicacion.ProveedorUbicacion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class PuntoDetalleUiState(
    val cargando: Boolean = true,
    /** `null` con `cargando = false` significa que el punto no existe (por ejemplo, fue eliminado). */
    val detalle: DetallePunto? = null
)

class PuntoDetalleViewModel(
    private val juego: JuegoRepository,
    private val sesion: SesionRepository,
    private val ubicacion: ProveedorUbicacion
) : ViewModel() {

    private val _estado = MutableStateFlow(PuntoDetalleUiState())
    val estado: StateFlow<PuntoDetalleUiState> = _estado.asStateFlow()

    private val posicion = MutableStateFlow<UbicacionUsuario?>(null)
    private var trabajo: Job? = null

    /** Carga el punto y sigue en vivo el avance del jugador (si completa una cápsula, se actualiza solo). */
    fun cargar(puntoId: Long) {
        val usuarioId = sesion.usuarioId.value ?: return
        trabajo?.cancel()
        _estado.value = PuntoDetalleUiState()
        trabajo = viewModelScope.launch {
            val punto = juego.obtenerPunto(puntoId)
            if (punto == null) {
                _estado.value = PuntoDetalleUiState(cargando = false, detalle = null)
                return@launch
            }
            // La ubicación se pide aparte: si tarda o no hay permiso, el resto de la pantalla ya se ve.
            launch {
                posicion.value = try {
                    ubicacion.actual()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    null
                }
            }
            combine(juego.capsulas, juego.idsCapsulasCompletadas(usuarioId), posicion) { capsulas, ids, donde ->
                Triple(capsulas, ids.toSet(), donde)
            }.collect { (capsulas, ids, donde) ->
                val fechas = ids.associateWith { id -> juego.progreso(usuarioId, id)?.fechaMillis }
                _estado.value = PuntoDetalleUiState(
                    cargando = false,
                    detalle = DetallePuntos.armar(punto, capsulas, ids, fechas, donde)
                )
            }
        }
    }
}
