package com.example.aprendoiasantiago.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// ---------- Objetos que viajan por la red (mismos nombres que en los microservicios Spring Boot) ----------

data class UsuarioRemoto(
    val id: Long,
    val nombreUsuario: String,
    val nombreVisible: String,
    val puntaje: Int,
    val correo: String? = null,
    val telefono: String? = null
)

data class RegistroPeticion(val nombreVisible: String, val nombreUsuario: String, val clave: String)
data class LoginPeticion(val nombreUsuario: String, val clave: String)
data class PerfilPeticion(val nombreVisible: String, val correo: String, val telefono: String)
data class ClavePeticion(val claveActual: String, val claveNueva: String)
data class ProgresoPeticion(val puntos: Int)
data class ProgresoRespuesta(val usuarioId: Long, val capsulaId: Long, val nuevo: Boolean, val puntajeTotal: Int)

data class PuntoRemoto(
    val id: Long = 0,
    val nombre: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val altitud: Double
)

data class CapsulaRemota(
    val id: Long = 0,
    val puntoId: Long,
    val titulo: String,
    val urlVideo: String,
    val codigoQr: String,
    val puntos: Int
)

// ---------- Microservicio de usuarios (puerto 8081) ----------

interface UsuariosApi {
    @POST("usuarios") suspend fun registrar(@Body datos: RegistroPeticion): UsuarioRemoto
    @POST("auth/login") suspend fun login(@Body datos: LoginPeticion): UsuarioRemoto
    @GET("usuarios") suspend fun listar(): List<UsuarioRemoto>
    @PUT("usuarios/{id}") suspend fun actualizarPerfil(@Path("id") id: Long, @Body datos: PerfilPeticion): UsuarioRemoto
    @PUT("usuarios/{id}/clave") suspend fun cambiarClave(@Path("id") id: Long, @Body datos: ClavePeticion)
    @DELETE("usuarios/{id}") suspend fun eliminar(@Path("id") id: Long)
    @GET("ranking") suspend fun ranking(): List<UsuarioRemoto>
    @POST("usuarios/{id}/progreso/{capsulaId}")
    suspend fun completarCapsula(
        @Path("id") id: Long,
        @Path("capsulaId") capsulaId: Long,
        @Body datos: ProgresoPeticion
    ): ProgresoRespuesta
}

// ---------- Microservicio de contenido (puerto 8082) ----------

interface ContenidoApi {
    @GET("puntos") suspend fun puntos(): List<PuntoRemoto>
    @POST("puntos") suspend fun crearPunto(@Body punto: PuntoRemoto): PuntoRemoto
    @PUT("puntos/{id}") suspend fun actualizarPunto(@Path("id") id: Long, @Body punto: PuntoRemoto): PuntoRemoto
    @DELETE("puntos/{id}") suspend fun eliminarPunto(@Path("id") id: Long)

    @GET("capsulas") suspend fun capsulas(): List<CapsulaRemota>
    @POST("capsulas") suspend fun crearCapsula(@Body capsula: CapsulaRemota): CapsulaRemota
    @PUT("capsulas/{id}") suspend fun actualizarCapsula(@Path("id") id: Long, @Body capsula: CapsulaRemota): CapsulaRemota
    @DELETE("capsulas/{id}") suspend fun eliminarCapsula(@Path("id") id: Long)
}

/** Clientes Retrofit de los dos microservicios. Las direcciones se configuran en `app/build.gradle.kts`. */
object RetrofitMicroservicios {
    // Tiempos cortos: si el servidor está apagado, la app sigue funcionando sin red casi de inmediato.
    private val http by lazy {
        OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .build()
    }

    private fun <T> crear(url: String, api: Class<T>): T =
        Retrofit.Builder()
            .baseUrl(url)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api)

    val usuarios: UsuariosApi by lazy { crear(com.example.aprendoiasantiago.BuildConfig.URL_USUARIOS, UsuariosApi::class.java) }
    val contenido: ContenidoApi by lazy { crear(com.example.aprendoiasantiago.BuildConfig.URL_CONTENIDO, ContenidoApi::class.java) }
}
