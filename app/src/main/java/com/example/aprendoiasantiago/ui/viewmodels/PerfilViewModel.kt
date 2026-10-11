package com.example.aprendoiasantiago.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendoiasantiago.data.local.UsuarioEntity
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class PerfilUiState(
    val usuario: UsuarioEntity? = null,
    val completadas: Int = 0,
    val totalCapsulas: Int = 0,
    val ranking: List<UsuarioEntity> = emptyList()
)

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
                    juego.capsulasCompletadas(id),
                    juego.cantidadCapsulas,
                    juego.ranking
                ) { usuario, completadas, total, ranking ->
                    PerfilUiState(usuario, completadas, total, ranking)
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
