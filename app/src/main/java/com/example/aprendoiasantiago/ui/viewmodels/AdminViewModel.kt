package com.example.aprendoiasantiago.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendoiasantiago.data.local.CapsulaEntity
import com.example.aprendoiasantiago.data.local.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.local.UsuarioEntity
import com.example.aprendoiasantiago.data.repository.AuthRepository
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(
    private val auth: AuthRepository,
    private val juego: JuegoRepository, // <-- Inyectamos el repositorio del juego
    private val sesion: SesionRepository
) : ViewModel() {

    // 1. Observadores en tiempo real (Las 3 listas para las 3 pestañas)
    val usuarios: StateFlow<List<UsuarioEntity>> = auth.observarPerfilesActivos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val puntos: StateFlow<List<PuntoTuristicoEntity>> = juego.puntos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val capsulas: StateFlow<List<CapsulaEntity>> = juego.capsulas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Acciones del Administrador
    fun eliminarJugador(id: Long) = viewModelScope.launch { auth.eliminarJugador(id) }

    fun guardarPunto(punto: PuntoTuristicoEntity) = viewModelScope.launch { juego.guardarPunto(punto) }
    fun eliminarPunto(id: Long) = viewModelScope.launch { juego.eliminarPunto(id) }

    fun guardarCapsula(capsula: CapsulaEntity) = viewModelScope.launch { juego.guardarCapsula(capsula) }
    fun eliminarCapsula(id: Long) = viewModelScope.launch { juego.eliminarCapsula(id) }

    fun cerrarSesion() = sesion.cerrar()
}