package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.model.Post
import com.example.aprendoiasantiago.data.remote.ApiService

/**
 * Único punto de acceso a la API de posts. Devuelve [Result] para que el ViewModel
 * no tenga que atrapar excepciones de red (sin internet, tiempo agotado, error 404, etc.).
 */
class PostRepository(private val api: ApiService) {

    suspend fun obtenerPosts(): Result<List<Post>> = llamar { api.obtenerPosts() }

    suspend fun obtenerPost(id: Int): Result<Post> = llamar { api.obtenerPost(id) }

    suspend fun crearPost(post: Post): Result<Post> = llamar { api.crearPost(post) }

    suspend fun reemplazarPost(id: Int, post: Post): Result<Post> = llamar { api.reemplazarPost(id, post) }

    suspend fun modificarTitulo(id: Int, titulo: String): Result<Post> =
        llamar { api.modificarPost(id, mapOf("title" to titulo)) }

    suspend fun eliminarPost(id: Int): Result<Unit> = llamar { api.eliminarPost(id) }

    private suspend fun <T> llamar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}
