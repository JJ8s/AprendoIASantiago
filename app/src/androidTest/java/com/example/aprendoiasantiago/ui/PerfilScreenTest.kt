package com.example.aprendoiasantiago.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.aprendoiasantiago.R
import com.example.aprendoiasantiago.data.model.UsuarioEntity
import com.example.aprendoiasantiago.ui.perfil.PerfilScreen
import com.example.aprendoiasantiago.ui.theme.AprendoIASantiagoTheme
import com.example.aprendoiasantiago.ui.viewmodels.PerfilUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Guía 15, Parte 5: pruebas de interfaz con Compose (se ejecutan en el emulador). */
class PerfilScreenTest {

    @get:Rule
    val regla = createAndroidComposeRule<ComponentActivity>()

    private val usuario = UsuarioEntity(
        id = 1, nombreUsuario = "ana", claveHash = "h", nombreVisible = "Ana Pérez", puntaje = 25
    )

    private var editar = 0
    private var cerrarSesion = 0

    private fun mostrar(estado: PerfilUiState = PerfilUiState(usuario = usuario)) {
        regla.setContent {
            AprendoIASantiagoTheme {
                PerfilScreen(
                    estado = estado,
                    sinLeer = 0,
                    onEditarPerfil = { editar++ },
                    onNotificaciones = {},
                    onPermisos = {},
                    onAyuda = {},
                    onCerrarSesion = { cerrarSesion++ }
                )
            }
        }
    }

    private fun texto(id: Int) = regla.activity.getString(id)

    @Test
    fun muestraNombreYAliasDelJugador() {
        mostrar()

        regla.onNodeWithText("Ana Pérez").assertIsDisplayed()
        regla.onNodeWithText("@ana").assertIsDisplayed()
    }

    @Test
    fun editarPerfilAvisaAlPulsarlo() {
        mostrar()

        regla.onNodeWithText(texto(R.string.perfil_editar)).performScrollTo().performClick()

        assertEquals(1, editar)
    }

    @Test
    fun cerrarSesionPideConfirmacionYCancelarNoCierra() {
        mostrar()

        regla.onNodeWithText(texto(R.string.perfil_cerrar_sesion)).performScrollTo().performClick()
        regla.onNodeWithText(texto(R.string.perfil_cerrar_titulo)).assertIsDisplayed()
        regla.onNodeWithText(texto(R.string.cancelar)).performClick()

        assertEquals(0, cerrarSesion)
    }

    @Test
    fun confirmarCerrarSesionLlamaAlCallback() {
        mostrar()

        regla.onNodeWithText(texto(R.string.perfil_cerrar_sesion)).performScrollTo().performClick()
        regla.onNode(hasText(texto(R.string.perfil_cerrar_sesion)) and hasAnyAncestor(isDialog())).performClick()

        assertEquals(1, cerrarSesion)
    }
}
