package com.example.aprendoiasantiago.data.remote

import com.example.aprendoiasantiago.data.model.Post
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Los cinco métodos HTTP sobre /posts (la API de pruebas simula las escrituras, no las guarda). */
interface ApiService {
    @GET("posts")
    suspend fun obtenerPosts(): List<Post>

    @GET("posts/{id}")
    suspend fun obtenerPost(@Path("id") id: Int): Post

    @POST("posts")
    suspend fun crearPost(@Body post: Post): Post

    @PUT("posts/{id}")
    suspend fun reemplazarPost(@Path("id") id: Int, @Body post: Post): Post

    @PATCH("posts/{id}")
    suspend fun modificarPost(@Path("id") id: Int, @Body cambios: Map<String, String>): Post

    @DELETE("posts/{id}")
    suspend fun eliminarPost(@Path("id") id: Int)
}
