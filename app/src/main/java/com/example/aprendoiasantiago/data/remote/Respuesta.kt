package com.example.aprendoiasantiago.data.remote

enum class TipoFallo { SIN_CONEXION, CONFLICTO, NO_AUTORIZADO, NO_ENCONTRADO, SERVIDOR }

/** Resultado de una llamada a un microservicio: o salió bien ([Ok]) o falló con un motivo claro ([Fallo]). */
sealed interface Respuesta<out T> {
    data class Ok<out T>(val valor: T) : Respuesta<T>
    data class Fallo(val tipo: TipoFallo, val detalle: String = "") : Respuesta<Nothing>
}

/** Ejecuta una llamada de red y convierte sus errores en un [Respuesta.Fallo] (nunca lanza, salvo cancelación). */
suspend fun <T> llamarApi(bloque: suspend () -> T): Respuesta<T> =
    try {
        Respuesta.Ok(bloque())
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: retrofit2.HttpException) {
        Respuesta.Fallo(TipoFallo.SERVIDOR, "HTTP ${e.code()}")
    } catch (e: java.io.IOException) {
        Respuesta.Fallo(TipoFallo.SIN_CONEXION, e.message.orEmpty())
    } catch (e: Exception) {
        Respuesta.Fallo(TipoFallo.SERVIDOR, e.message.orEmpty())
    }
