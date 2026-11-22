package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.remote.ClimaApi
import com.example.aprendoiasantiago.data.remote.Respuesta
import com.example.aprendoiasantiago.data.remote.TipoFallo
import com.example.aprendoiasantiago.data.remote.llamarApi
import com.example.aprendoiasantiago.domain.ClimaPunto
import com.example.aprendoiasantiago.domain.CondicionClima

/** Consulta el clima actual de unas coordenadas en la API externa (Open-Meteo). No guarda nada en Room. */
class ClimaRepository(private val api: ClimaApi) {

    suspend fun obtener(latitud: Double, longitud: Double): Respuesta<ClimaPunto> =
        when (val r = llamarApi { api.actual(latitud, longitud) }) {
            is Respuesta.Fallo -> r
            is Respuesta.Ok -> r.valor.current
                ?.let {
                    Respuesta.Ok(ClimaPunto(it.temperatura, it.sensacion, it.viento, CondicionClima.desdeCodigo(it.codigo)))
                }
                ?: Respuesta.Fallo(TipoFallo.SERVIDOR, "respuesta sin datos")
        }
}
