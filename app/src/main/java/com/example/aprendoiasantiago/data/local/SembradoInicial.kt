package com.example.aprendoiasantiago.data.local

/**
 * Llena la base la primera vez que se abre la app.
 * Los usuarios se insertan al final: su presencia marca que el sembrado terminó.
 * Todos los registros llevan id fijo y se insertan con REPLACE, así que repetirlo es seguro.
 */
class SembradoInicial(private val db: AppDatabase) {
    suspend fun ejecutarSiVacio() {
        if (db.usuarioDao().contar() > 0) return
        db.puntoDao().insertarTodos(DatosEjemplo.puntos)
        db.capsulaDao().insertarTodas(DatosEjemplo.capsulas)
        db.usuarioDao().insertarTodos(DatosEjemplo.usuarios)
    }
}
