package com.example.aprendoiasantiago.ui.viewmodels

import com.example.aprendoiasantiago.data.model.Post
import com.example.aprendoiasantiago.data.remote.ApiService
import com.example.aprendoiasantiago.data.repository.PostRepository
import com.example.aprendoiasantiago.util.pruebaConMain
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException

/** Guía 14 + 15: se simula la API (ApiService) y se prueba el repositorio real junto al ViewModel. */
class PostViewModelTest : StringSpec({

    val p1 = Post(userId = 1, id = 1, title = "Primero", body = "Cuerpo 1")
    val p2 = Post(userId = 1, id = 2, title = "Segundo", body = "Cuerpo 2")

    fun apiConLista(): ApiService = mockk<ApiService>(relaxed = true).also {
        coEvery { it.obtenerPosts() } returns listOf(p1, p2)
    }

    fun crear(api: ApiService) = PostViewModel(PostRepository(api))

    "al abrir la pantalla descarga los posts" {
        pruebaConMain {
            val vm = crear(apiConLista())

            vm.estado.value.cargando shouldBe false
            vm.estado.value.posts shouldContainExactly listOf(p1, p2)
            vm.estado.value.error.shouldBeNull()
        }
    }

    "sin internet muestra SIN_CONEXION y no se queda cargando" {
        pruebaConMain {
            val api = mockk<ApiService>(relaxed = true)
            coEvery { api.obtenerPosts() } throws IOException("sin red")

            val vm = crear(api)

            vm.estado.value.error shouldBe ErrorPost.SIN_CONEXION
            vm.estado.value.cargando shouldBe false
            vm.estado.value.posts shouldBe emptyList()
        }
    }

    "un fallo que no es de red se informa como ERROR_SERVIDOR" {
        pruebaConMain {
            val api = mockk<ApiService>(relaxed = true)
            coEvery { api.obtenerPosts() } throws IllegalStateException("json roto")

            crear(api).estado.value.error shouldBe ErrorPost.ERROR_SERVIDOR
        }
    }

    "reintentar después de un error carga la lista" {
        pruebaConMain {
            val api = mockk<ApiService>(relaxed = true)
            coEvery { api.obtenerPosts() } throws IOException() andThen listOf(p1)
            val vm = crear(api)

            vm.cargar()

            vm.estado.value.error.shouldBeNull()
            vm.estado.value.posts shouldContainExactly listOf(p1)
        }
    }

    "guardar con campos vacíos no llama a la API" {
        pruebaConMain {
            val api = apiConLista()
            val vm = crear(api)

            vm.guardar("  ", "cuerpo")

            vm.estado.value.error shouldBe ErrorPost.FORMULARIO_VACIO
            coVerify(exactly = 0) { api.crearPost(any()) }
        }
    }

    "crear (POST) envía el texto sin espacios y agrega el post arriba de la lista" {
        pruebaConMain {
            val api = apiConLista()
            val devuelto = Post(userId = 1, id = 101, title = "Nuevo", body = "Texto")
            coEvery { api.crearPost(any()) } returns devuelto
            val vm = crear(api)

            vm.guardar("  Nuevo ", " Texto ")

            coVerify(exactly = 1) { api.crearPost(Post(userId = 1, title = "Nuevo", body = "Texto")) }
            vm.estado.value.posts shouldContainExactly listOf(devuelto, p1, p2)
        }
    }

    "editar (PUT) reemplaza el post y sale del modo edición" {
        pruebaConMain {
            val api = apiConLista()
            val editado = p1.copy(title = "Cambiado", body = "Otro")
            coEvery { api.reemplazarPost(1, editado) } returns editado
            val vm = crear(api)
            vm.editar(p1)

            vm.guardar("Cambiado", "Otro")

            vm.estado.value.posts shouldContainExactly listOf(editado, p2)
            vm.estado.value.enEdicion.shouldBeNull()
        }
    }

    "PATCH cambia solo el título y conserva el cuerpo" {
        pruebaConMain {
            val api = apiConLista()
            coEvery { api.modificarPost(1, mapOf("title" to "Solo título")) } returns p1.copy(title = "Solo título")
            val vm = crear(api)
            vm.editar(p1)

            vm.guardarSoloTitulo("Solo título")

            vm.estado.value.posts.first().title shouldBe "Solo título"
            vm.estado.value.posts.first().body shouldBe "Cuerpo 1"
        }
    }

    "eliminar (DELETE) quita el post de la lista" {
        pruebaConMain {
            val api = apiConLista()
            val vm = crear(api)

            vm.eliminar(p1)

            coVerify(exactly = 1) { api.eliminarPost(1) }
            vm.estado.value.posts shouldContainExactly listOf(p2)
        }
    }

    "si el DELETE falla el post sigue en la lista y se avisa" {
        pruebaConMain {
            val api = apiConLista()
            coEvery { api.eliminarPost(1) } throws IOException()
            val vm = crear(api)

            vm.eliminar(p1)

            vm.estado.value.posts shouldContainExactly listOf(p1, p2)
            vm.estado.value.error shouldBe ErrorPost.SIN_CONEXION
        }
    }
})
