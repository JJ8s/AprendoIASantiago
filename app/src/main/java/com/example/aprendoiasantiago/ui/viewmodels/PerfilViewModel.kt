package com.example.aprendoiasantiago.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendoiasantiago.data.local.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.local.UsuarioEntity
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.domain.CapsulaConEstado
import com.example.aprendoiasantiago.domain.ProgresoPorPunto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class PerfilUiState(
    val usuario: UsuarioEntity? = null,
    /** Todas las cápsulas, cada una marcada como completada o pendiente. */
    val progreso: List<CapsulaConEstado> = emptyList(),
    /** Puntos turísticos, para saber en qué lugar está cada cápsula. */
    val puntos: List<PuntoTuristicoEntity> = emptyList(),
    val ranking: List<UsuarioEntity> = emptyList()
) {
    val completadas: Int get() = progreso.count { it.completada }
    val totalCapsulas: Int get() = progreso.size
}

@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModel(
    juego: JuegoRepository,
    private val sesion: SesionRepository
) : ViewModel() {

    val estado: StateFlow<PerfilUiState> = sesion.usuarioId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(PerfilUiState())
            } else {
                combine(
                    juego.usuario(id),
                    juego.capsulas,
                    juego.idsCapsulasCompletadas(id),
                    juego.ranking,
                    juego.puntos
                ) { usuario, capsulas, idsCompletadas, ranking, puntos ->
                    PerfilUiState(
                        usuario = usuario,
                        progreso = ProgresoPorPunto.capsulasConEstado(capsulas, idsCompletadas.toSet()),
                        puntos = puntos,
                        ranking = ranking
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PerfilUiState()
        )

    fun cerrarSesion() {
        sesion.cerrar()
    }
}
