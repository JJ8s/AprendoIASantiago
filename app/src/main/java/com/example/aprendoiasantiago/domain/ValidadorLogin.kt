package com.example.aprendoiasantiago.domain

/** Errores posibles al iniciar sesión. La interfaz decide qué texto mostrar para cada uno. */
enum class ErrorLogin {
    USUARIO_VACIO,
    CLAVE_VACIA,
    CREDENCIALES_INCORRECTAS,
    ERROR_INESPERADO
}

/** Reglas de validación del formulario de login (lógica pura, fácil de probar). */
object ValidadorLogin {
    /** Devuelve el primer error encontrado, o `null` si los datos son aceptables. */
    fun validar(usuario: String, clave: String): ErrorLogin? = when {
        usuario.isBlank() -> ErrorLogin.USUARIO_VACIO
        clave.isBlank() -> ErrorLogin.CLAVE_VACIA
        else -> null
    }
}
