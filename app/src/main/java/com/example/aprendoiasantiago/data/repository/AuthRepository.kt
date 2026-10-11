package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.local.UsuarioDao
import com.example.aprendoiasantiago.data.local.UsuarioEntity
import com.example.aprendoiasantiago.utils.Hash
import kotlinx.coroutines.Deferred

class AuthRepository(
    private val usuarioDao: UsuarioDao,
    /** Termina cuando la base ya tiene sus datos iniciales. */
    private val sembradoListo: Deferred<Unit>
) {
    /** Devuelve el usuario si las credenciales son correctas, o `null` si no lo son. */
    suspend fun iniciarSesion(nombreUsuario: String, clave: String): UsuarioEntity? {
        sembradoListo.await()
        return usuarioDao.buscarPorCredenciales(
            nombreUsuario = nombreUsuario.trim(),
            claveHash = Hash.sha256(clave)
        )
    }
}
