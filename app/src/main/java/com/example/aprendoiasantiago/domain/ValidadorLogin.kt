package com.example.aprendoiasantiago.domain

/** Errores posibles al iniciar sesión o registrar un jugador. */
enum class ErrorLogin {
    NOMBRE_VACIO,
    USUARIO_VACIO,
    CLAVE_VACIA,
    CONFIRMACION_VACIA,
    CLAVES_NO_COINCIDEN,
    CREDENCIALES_INCORRECTAS,
    USUARIO_YA_EXISTE,
    ERROR_INESPERADO
}

/** Reglas de validación del formulario de login y registro. */
object ValidadorLogin {
    fun validar(usuario: String, clave: String): ErrorLogin? = when {
        usuario.isBlank() -> ErrorLogin.USUARIO_VACIO
        clave.isBlank() -> ErrorLogin.CLAVE_VACIA
        else -> null
    }

    /** [confirmacion] es lo que el jugador escribió en "Confirmar contraseña": debe ser idéntico a [clave]. */
    fun validarRegistro(
        nombreVisible: String,
        usuario: String,
        clave: String,
        confirmacion: String
    ): ErrorLogin? = when {
        nombreVisible.isBlank() -> ErrorLogin.NOMBRE_VACIO
        usuario.isBlank() -> ErrorLogin.USUARIO_VACIO
        clave.isBlank() -> ErrorLogin.CLAVE_VACIA
        confirmacion.isEmpty() -> ErrorLogin.CONFIRMACION_VACIA
        confirmacion != clave -> ErrorLogin.CLAVES_NO_COINCIDEN // Distingue mayúsculas y espacios, como la clave real.
        else -> null
    }
}
