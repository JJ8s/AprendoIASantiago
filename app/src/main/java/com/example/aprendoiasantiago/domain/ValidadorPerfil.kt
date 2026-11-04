package com.example.aprendoiasantiago.domain

enum class ErrorPerfil {
    NOMBRE_VACIO,
    NOMBRE_LARGO,
    CORREO_INVALIDO,
    TELEFONO_INVALIDO,
    CLAVE_ACTUAL_VACIA,
    CLAVE_ACTUAL_INCORRECTA,
    CLAVE_NUEVA_VACIA,
    CLAVE_NUEVA_IGUAL,
    CLAVES_NO_COINCIDEN,
    ERROR_INESPERADO
}

/** Reglas del formulario de edición de perfil (lógica pura, sin Android, fácil de probar). */
object ValidadorPerfil {
    const val LARGO_MAXIMO_NOMBRE = 40

    // Forma simple "algo@algo.algo": suficiente para un correo de contacto local.
    private val PATRON_CORREO = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    /** Correo y teléfono son opcionales: vacío es válido, pero si se escriben deben tener formato correcto. */
    fun validarDatos(nombre: String, correo: String, telefono: String): ErrorPerfil? {
        val nombreLimpio = nombre.trim()
        if (nombreLimpio.isEmpty()) return ErrorPerfil.NOMBRE_VACIO
        if (nombreLimpio.length > LARGO_MAXIMO_NOMBRE) return ErrorPerfil.NOMBRE_LARGO

        val correoLimpio = correo.trim()
        if (correoLimpio.isNotEmpty() && !PATRON_CORREO.matches(correoLimpio)) {
            return ErrorPerfil.CORREO_INVALIDO
        }

        val telefonoLimpio = telefono.trim()
        if (telefonoLimpio.isNotEmpty()) {
            val soloPermitidos = telefonoLimpio.all { it.isDigit() || it in "+ -()" }
            val digitos = telefonoLimpio.count { it.isDigit() }
            if (!soloPermitidos || digitos !in 8..15) return ErrorPerfil.TELEFONO_INVALIDO
        }
        return null
    }

    fun validarClave(actual: String, nueva: String, confirmacion: String): ErrorPerfil? = when {
        actual.isEmpty() -> ErrorPerfil.CLAVE_ACTUAL_VACIA
        nueva.isEmpty() -> ErrorPerfil.CLAVE_NUEVA_VACIA
        nueva == actual -> ErrorPerfil.CLAVE_NUEVA_IGUAL
        nueva != confirmacion -> ErrorPerfil.CLAVES_NO_COINCIDEN
        else -> null
    }
}
