package com.example.aprendoiasantiago.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendoiasantiago.data.model.Post
import com.example.aprendoiasantiago.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ErrorPost { SIN_CONEXION, FORMULARIO_VACIO, ERROR_SERVIDOR }

data class PostUiState(
    val cargando: Boolean = false,
    val posts: List<Post> = emptyList(),
    val error: ErrorPost? = null,
    /** Post que se está editando (null = el formulario crea uno nuevo). */
    val enEdicion: Post? = null
)

class PostViewModel(
    private val repositorio: PostRepository,
    private val idUsuarioApi: Int = 1
) : ViewModel() {

    private val _estado = MutableStateFlow(PostUiState())
    val estado: StateFlow<PostUiState> = _estado.asStateFlow()

    init { cargar() }

    fun cargar() {
        _estado.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            repositorio.obtenerPosts()
                .onSuccess { lista -> _estado.update { it.copy(cargando = false, posts = lista) } }
                .onFailure { e -> _estado.update { it.copy(cargando = false, error = clasificar(e)) } }
        }
    }

    fun editar(post: Post?) = _estado.update { it.copy(enEdicion = post, error = null) }

    /** POST si no hay nada en edición, PUT si se está editando un post. */
    fun guardar(titulo: String, cuerpo: String) {
        if (titulo.isBlank() || cuerpo.isBlank()) {
            _estado.update { it.copy(error = ErrorPost.FORMULARIO_VACIO) }
            return
        }
        val actual = _estado.value.enEdicion
        viewModelScope.launch {
            if (actual == null) {
                repositorio.crearPost(Post(userId = idUsuarioApi, title = titulo.trim(), body = cuerpo.trim()))
                    // La API simula la escritura (siempre devuelve id 101), así que se refleja en la lista local.
                    .onSuccess { nuevo -> _estado.update { it.copy(posts = listOf(nuevo) + it.posts, error = null) } }
                    .onFailure { e -> _estado.update { it.copy(error = clasificar(e)) } }
            } else {
                repositorio.reemplazarPost(actual.id, actual.copy(title = titulo.trim(), body = cuerpo.trim()))
                    .onSuccess { editado -> reemplazarLocal(editado) }
                    .onFailure { e -> _estado.update { it.copy(error = clasificar(e)) } }
            }
        }
    }

    /** PATCH: cambia solo el título del post en edición. */
    fun guardarSoloTitulo(titulo: String) {
        val actual = _estado.value.enEdicion ?: return
        if (titulo.isBlank()) {
            _estado.update { it.copy(error = ErrorPost.FORMULARIO_VACIO) }
            return
        }
        viewModelScope.launch {
            repositorio.modificarTitulo(actual.id, titulo.trim())
                // La API devuelve el post parcial; se conserva el cuerpo que ya teníamos.
                .onSuccess { reemplazarLocal(actual.copy(title = it.title)) }
                .onFailure { e -> _estado.update { it.copy(error = clasificar(e)) } }
        }
    }

    fun eliminar(post: Post) {
        viewModelScope.launch {
            repositorio.eliminarPost(post.id)
                .onSuccess { _estado.update { s -> s.copy(posts = s.posts.filterNot { it.id == post.id }, error = null) } }
                .onFailure { e -> _estado.update { it.copy(error = clasificar(e)) } }
        }
    }

    private fun reemplazarLocal(editado: Post) = _estado.update { s ->
        s.copy(posts = s.posts.map { if (it.id == editado.id) editado else it }, enEdicion = null, error = null)
    }

    private fun clasificar(e: Throwable) =
        if (e is java.io.IOException) ErrorPost.SIN_CONEXION else ErrorPost.ERROR_SERVIDOR
}
