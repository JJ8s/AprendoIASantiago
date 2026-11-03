package com.example.aprendoiasantiago.data.local

/**
 * Prepara la base en cada inicio de la app:
 * - Puntos, cápsulas y rutas (contenido fijo): se actualizan siempre con @Upsert. Así, si cambias los datos
 *   de ejemplo, la app los toma sin tener que desinstalarla, y el progreso del usuario no se pierde.
 * - Usuarios: solo se crean la primera vez, para no reiniciar los puntajes ya ganados.
 */
class SembradoInicial(private val db: AppDatabase) {
    suspend fun ejecutar() {
        db.puntoDao().guardarTodos(DatosEjemplo.puntos)
        db.capsulaDao().guardarTodas(DatosEjemplo.capsulas)
        db.rutaDao().guardarRutas(DatosEjemplo.rutas)
        db.rutaDao().guardarParadas(DatosEjemplo.paradas)
        db.usuarioDao().insertarTodos(DatosEjemplo.usuarios)

    }
}
