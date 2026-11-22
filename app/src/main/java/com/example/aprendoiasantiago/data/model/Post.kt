package com.example.aprendoiasantiago.data.model

import com.google.gson.annotations.SerializedName

/**
 * Publicación de la API pública https://jsonplaceholder.typicode.com/posts
 * (Guía 14). No se guarda en Room: la app sigue siendo offline para el juego.
 */
data class Post(
    @SerializedName("userId") val userId: Int,
    @SerializedName("id") val id: Int = 0,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String
)
