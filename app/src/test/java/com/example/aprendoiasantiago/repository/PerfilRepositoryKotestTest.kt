package com.example.aprendoiasantiago.data.repository

import com.example.aprendoiasantiago.data.local.UsuarioDao
import com.example.aprendoiasantiago.data.model.UsuarioEntity
import com.example.aprendoiasantiago.domain.ErrorPerfil
import com.example.aprendoiasantiago.utils.Hash
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest

/** Kotest + MockK: el DAO se simula y se comprueba qué se guarda (y qué NO) en la base. */
class PerfilRepositoryKotestTest : StringSpec({

    fun usuario(clave: String = "vieja1", avatarUri: String? = null, permitirUbicacion: Boolean = true) =
        UsuarioEntity(
            id = 1, nombreUsuario = "ana", claveHash = Hash.sha256(clave), nombreVisible = "Ana",
            avatarUri = avatarUri, permitirUbicacion = permitirUbicacion
        )

    "guardarDatos válidos recorta los espacios y guarda en la base" {
        runTest {
            val dao = mockk<UsuarioDao>(relaxed = true)
            val repo = PerfilRepository(dao)

            repo.guardarDatos(1, "  Ana María ", " ana@correo.cl ", "") shouldBe null

            coVerify(exactly = 1) { dao.actualizarDatos(1L, "Ana María", "ana@correo.cl", "") }
        }
    }

    "guardarDatos con un correo inválido devuelve el error y no toca la base" {
        runTest {
            val dao = mockk<UsuarioDao>(relaxed = true)
            val repo = PerfilRepository(dao)

            repo.guardarDatos(1, "Ana", "correo-malo", "") shouldBe ErrorPerfil.CORREO_INVALIDO

            coVerify(exactly = 0) { dao.actualizarDatos(any(), any(), any(), any()) }
        }
    }

    "cambiarClave con la actual correcta guarda el hash de la nueva" {
        runTest {
            val dao = mockk<UsuarioDao>(relaxed = true)
            coEvery { dao.buscarPorId(1L) } returns usuario(clave = "vieja1")
            val repo = PerfilRepository(dao)

            repo.cambiarClave(1, "vieja1", "nueva22", "nueva22") shouldBe null

            coVerify(exactly = 1) { dao.actualizarClave(1L, Hash.sha256("nueva22")) }
        }
    }

    "cambiarClave con la actual incorrecta no cambia nada" {
        runTest {
            val dao = mockk<UsuarioDao>(relaxed = true)
            coEvery { dao.buscarPorId(1L) } returns usuario(clave = "vieja1")
            val repo = PerfilRepository(dao)

            repo.cambiarClave(1, "equivocada", "nueva22", "nueva22") shouldBe ErrorPerfil.CLAVE_ACTUAL_INCORRECTA

            coVerify(exactly = 0) { dao.actualizarClave(any(), any()) }
        }
    }

    "cambiarClave con confirmación distinta falla antes de consultar la base" {
        runTest {
            val dao = mockk<UsuarioDao>(relaxed = true)
            val repo = PerfilRepository(dao)

            repo.cambiarClave(1, "vieja1", "nueva22", "otra") shouldBe ErrorPerfil.CLAVES_NO_COINCIDEN

            coVerify(exactly = 0) { dao.buscarPorId(any()) }
        }
    }

    "cambiarClave de un usuario que ya no existe devuelve error inesperado" {
        runTest {
            val dao = mockk<UsuarioDao>(relaxed = true)
            coEvery { dao.buscarPorId(1L) } returns null
            val repo = PerfilRepository(dao)

            repo.cambiarClave(1, "vieja1", "nueva22", "nueva22") shouldBe ErrorPerfil.ERROR_INESPERADO
        }
    }

    "elegirAvatar prediseñado devuelve la foto anterior para poder borrarla" {
        runTest {
            val dao = mockk<UsuarioDao>(relaxed = true)
            coEvery { dao.buscarPorId(1L) } returns usuario(avatarUri = "file:///avatares/a.jpg")
            val repo = PerfilRepository(dao)

            repo.elegirAvatar(1, 3) shouldBe "file:///avatares/a.jpg"

            coVerify(exactly = 1) { dao.actualizarAvatarIndice(1L, 3) }
        }
    }

    "usarFotoAvatar guarda la nueva ruta y devuelve la anterior" {
        runTest {
            val dao = mockk<UsuarioDao>(relaxed = true)
            coEvery { dao.buscarPorId(1L) } returns usuario(avatarUri = "file:///avatares/a.jpg")
            val repo = PerfilRepository(dao)

            repo.usarFotoAvatar(1, "file:///avatares/b.jpg") shouldBe "file:///avatares/a.jpg"

            coVerify(exactly = 1) { dao.actualizarAvatarUri(1L, "file:///avatares/b.jpg") }
        }
    }

    "ubicacionPermitida refleja la base y vale true si el usuario no existe" {
        runTest {
            val dao = mockk<UsuarioDao>(relaxed = true)
            coEvery { dao.buscarPorId(1L) } returns usuario(permitirUbicacion = false)
            coEvery { dao.buscarPorId(2L) } returns null
            val repo = PerfilRepository(dao)

            repo.ubicacionPermitida(1) shouldBe false
            repo.ubicacionPermitida(2) shouldBe true
        }
    }
})
