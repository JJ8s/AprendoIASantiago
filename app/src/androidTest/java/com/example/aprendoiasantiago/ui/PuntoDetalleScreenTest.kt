package com.example.aprendoiasantiago.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.aprendoiasantiago.R
import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity
import com.example.aprendoiasantiago.domain.CapsulaDelPunto
import com.example.aprendoiasantiago.domain.DetallePunto
import com.example.aprendoiasantiago.ui.punto.PuntoDetalleScreen
import com.example.aprendoiasantiago.ui.theme.AprendoIASantiagoTheme
import com.example.aprendoiasantiago.ui.viewmodels.PuntoDetalleUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PuntoDetalleScreenTest {

    @get:Rule
    val regla = createAndroidComposeRule<ComponentActivity>()

    private val plaza = PuntoTuristicoEntity(
        id = 1, nombre = "Plaza de Armas", descripcion = "Corazón histórico de Santiago",
        latitud = -33.4372, longitud = -70.6506, altitud = 540.0
    )
    private val hecha = CapsulaEntity(id = 10, puntoId = 1, titulo = "Qué es la IA", urlVideo = "u", codigoQr = "AIJS-010", puntos = 10)
    private val pendiente = CapsulaEntity(id = 11, puntoId = 1, titulo = "Secreto de prueba", urlVideo = "u", codigoQr = "AIJS-011", puntos = 15)

    private var capsulaAbierta = -1L
    private var iraEscaner = 0
    private var verMapa = 0

    private fun mostrar(estado: PuntoDetalleUiState) {
        regla.setContent {
            AprendoIASantiagoTheme {
                PuntoDetalleScreen(
                    estado = estado,
                    onVolver = {},
                    onIrAlEscaner = { iraEscaner++ },
                    onVerEnMapa = { verMapa++ },
                    onVerCapsula = { capsulaAbierta = it }
                )
            }
        }
    }

    private fun texto(id: Int, vararg args: Any) = regla.activity.getString(id, *args)

    private fun detalleMixto() = PuntoDetalleUiState(
        cargando = false,
        detalle = DetallePunto(
            punto = plaza,
            capsulas = listOf(
                CapsulaDelPunto(hecha, completada = true, fechaMillis = 1_700_000_000_000L),
                CapsulaDelPunto(pendiente, completada = false, fechaMillis = null)
            ),
            distanciaMetros = 350.0
        )
    )

    @Test
    fun muestraNombreEstadoPendienteYDistancia() {
        mostrar(detalleMixto())

        regla.onNodeWithText("Plaza de Armas").assertIsDisplayed()
        regla.onNodeWithText(texto(R.string.punto_estado_pendiente, 1, 2)).assertIsDisplayed()
        regla.onNodeWithText("350 m").assertExists()
    }

    @Test
    fun laCapsulaPendienteNoRevelaSuTitulo() {
        mostrar(detalleMixto())

        regla.onNodeWithText(texto(R.string.punto_capsula_bloqueada)).assertExists()
        regla.onNodeWithText("Secreto de prueba").assertDoesNotExist()
    }

    @Test
    fun tocarUnaCapsulaCompletadaLaAbre() {
        mostrar(detalleMixto())

        regla.onNodeWithText("Qué es la IA").performScrollTo().performClick()

        assertEquals(10L, capsulaAbierta)
    }

    @Test
    fun losBotonesAvisanAlPulsarlos() {
        mostrar(detalleMixto())

        regla.onNodeWithText(texto(R.string.punto_ir_escaner)).performScrollTo().performClick()
        regla.onNodeWithText(texto(R.string.punto_ver_mapa)).performScrollTo().performClick()

        assertEquals(1, iraEscaner)
        assertEquals(1, verMapa)
    }

    @Test
    fun unPuntoQueNoExisteMuestraElAviso() {
        mostrar(PuntoDetalleUiState(cargando = false, detalle = null))

        regla.onNodeWithText(texto(R.string.punto_no_encontrado)).assertIsDisplayed()
    }

    @Test
    fun sinUbicacionDiceNoDisponible() {
        mostrar(detalleMixto().let { it.copy(detalle = it.detalle!!.copy(distanciaMetros = null)) })

        regla.onNodeWithText(texto(R.string.punto_distancia_no_disponible)).assertExists()
    }
}
