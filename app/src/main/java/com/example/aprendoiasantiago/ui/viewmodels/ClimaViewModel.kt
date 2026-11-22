package com.example.aprendoiasantiago.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendoiasantiago.data.remote.Respuesta
import com.example.aprendoiasantiago.data.remote.TipoFallo
import com.example.aprendoiasantiago.data.repository.ClimaRepository
import com.example.aprendoiasantiago.domain.ClimaPunto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EstadoClima {
    data object Cargando : EstadoClima
    data class Disponible(val clima: ClimaPunto) : EstadoClima
    data class NoDisponible(val sinConexion: Boolean) : EstadoClima
}

class ClimaViewModel(private val repositorio: ClimaRepository) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoClima>(EstadoClima.Cargando)
    val estado: StateFlow<EstadoClima> = _estado.asStateFlow()

    fun cargar(latitud: Double, longitud: Double) {
        viewModelScope.launch {
            _estado.value = EstadoClima.Cargando
            _estado.value = when (val r = repositorio.obtener(latitud, longitud)) {
                is Respuesta.Ok -> EstadoClima.Disponible(r.valor)
                is Respuesta.Fallo -> EstadoClima.NoDisponible(sinConexion = r.tipo == TipoFallo.SIN_CONEXION)
            }
        }
    }
}
