package com.example.aprendoiasantiago.utils

import java.security.MessageDigest

/** Utilidades de hash. Las contraseñas nunca se guardan en texto plano. */
object Hash {
    fun sha256(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray(Charsets.UTF_8))
            .joinToString(separator = "") { byte -> "%02x".format(byte) }
}
