package com.example.aprendoiasantiago.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/** API externa de clima: Open-Meteo (https://open-meteo.com, gratuita y sin clave). */
data class ClimaRespuesta(val current: ClimaActualRemoto?)

data class ClimaActualRemoto(
    @SerializedName("temperature_2m") val temperatura: Double,
    @SerializedName("apparent_temperature") val sensacion: Double,
    @SerializedName("wind_speed_10m") val viento: Double,
    @SerializedName("weather_code") val codigo: Int
)

interface ClimaApi {
    @GET("v1/forecast")
    suspend fun actual(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") campos: String = "temperature_2m,apparent_temperature,wind_speed_10m,weather_code",
        @Query("timezone") zonaHoraria: String = "auto"
    ): ClimaRespuesta
}

/** Cliente propio de esta API: no comparte nada con los microservicios ni con la base local. */
object RetrofitClima {
    val api: ClimaApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(OkHttpClient.Builder().connectTimeout(6, TimeUnit.SECONDS).readTimeout(8, TimeUnit.SECONDS).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClimaApi::class.java)
    }
}
