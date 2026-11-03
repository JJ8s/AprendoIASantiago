package com.example.aprendoiasantiago.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.domain.ProgresoPorPunto
import com.example.aprendoiasantiago.domain.PuntoConEstado
import com.example.aprendoiasantiago.domain.RutaConPuntos
import com.example.aprendoiasantiago.domain.Rutas
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class MapaUiState(
    /** Puntos turísticos con su estado (pendiente/completado) para el usuario con sesión iniciada. */
    val puntos: List<PuntoConEstado> = emptyList(),
    val rutas: List<RutaConPuntos> = emptyList(),
    /** Ruta que el usuario eligió para verla dibujada; `null` = solo los puntos. */
    val rutaSeleccionadaId: Long? = null
) {
    val rutaSeleccionada: RutaConPuntos? get() = rutas.firstOrNull { it.ruta.id == rutaSeleccionadaId }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MapaViewModel(
    juego: JuegoRepository,
    sesion: SesionRepository
) : ViewModel() {

    private val puntosConEstado: Flow<List<PuntoConEstado>> = sesion.usuarioId
        .flatMapLatest { usuarioId ->
            val idsCompletadas = if (usuarioId == null) {
                flowOf(emptyList<Long>())
            } else {
                juego.idsCapsulasCompletadas(usuarioId)
            }
            combine(juego.puntos, juego.capsulas, idsCompletadas) { puntos, capsulas, ids ->
                ProgresoPorPunto.calcular(puntos, capsulas, ids.toSet())
            }
        }

    private val seleccion = MutableStateFlow<Long?>(null)

    val estado: StateFlow<MapaUiState> = combine(
        puntosConEstado,
        juego.rutas,
        juego.paradasDeRutas,
        seleccion
    ) { puntos, rutas, paradas, elegida ->
        MapaUiState(
            puntos = puntos,
            rutas = Rutas.calcular(rutas, paradas, puntos),
            rutaSeleccionadaId = elegida
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MapaUiState()
    )

    /** Elige una ruta para dibujarla; `null` la quita. Tocar la ruta ya elegida también la quita. */
    fun seleccionarRuta(id: Long?) {
        seleccion.value = if (id == seleccion.value) null else id
    }
}
