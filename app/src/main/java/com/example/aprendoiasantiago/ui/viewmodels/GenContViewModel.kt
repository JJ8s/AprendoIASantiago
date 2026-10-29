package com.example.aprendoiasantiago.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendoiasantiago.data.local.CapsulaEntity
import com.example.aprendoiasantiago.data.local.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GenContViewModel(
    private val juego: JuegoRepository,
    private val sesion: SesionRepository
) : ViewModel() {

    // Observadores en tiempo real
    val capsulas: StateFlow<List<CapsulaEntity>> = juego.capsulas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val puntos: StateFlow<List<PuntoTuristicoEntity>> = juego.puntos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Acciones exclusivas del Creador de Contenido
    fun guardarCapsula(capsula: CapsulaEntity) = viewModelScope.launch { juego.guardarCapsula(capsula) }
    fun eliminarCapsula(id: Long) = viewModelScope.launch { juego.eliminarCapsula(id) }

    fun cerrarSesion() = sesion.cerrar()
}