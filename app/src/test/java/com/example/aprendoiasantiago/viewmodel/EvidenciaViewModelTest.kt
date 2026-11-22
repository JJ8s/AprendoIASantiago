package com.example.aprendoiasantiago.ui.viewmodels

import android.graphics.Bitmap
import com.example.aprendoiasantiago.data.local.AlmacenEvidencias
import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.model.ProgresoEntity
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.domain.ResultadoRostro
import com.example.aprendoiasantiago.ml.DetectorRostro
import com.example.aprendoiasantiago.util.pruebaConMain
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

/** Solo se prueban los caminos que no escriben archivos (la captura real se prueba en el emulador). */
class EvidenciaViewModelTest : StringSpec({

    val capsula = CapsulaEntity(id = 5, puntoId = 1, titulo = "IA", urlVideo = "u", codigoQr = "Q", puntos = 10)

    class Entorno(usuario: Long? = 7L) {
        val juego = mockk<JuegoRepository>()
        val almacen = mockk<AlmacenEvidencias>(relaxed = true)
        val detector = mockk<DetectorRostro>()
        val sesion = SesionRepository().apply { if (usuario != null) iniciar(usuario) }
        val vm = EvidenciaViewModel(juego, sesion, almacen, detector)

        fun conProgreso(progreso: ProgresoEntity?) {
            coEvery { juego.obtenerCapsula(5L) } returns capsula
            coEvery { juego.progreso(7L, 5L) } returns progreso
        }
    }

    "una cápsula completada sin fotos empieza en el paso de la selfie" {
        pruebaConMain {
            val e = Entorno()
            e.conProgreso(ProgresoEntity(7, 5, completada = true))

            e.vm.cargar(5)

            e.vm.estado.value.paso shouldBe PasoEvidencia.SELFIE
            e.vm.estado.value.capsula shouldBe capsula
        }
    }

    "si ya hay selfie retoma en la foto del entorno" {
        pruebaConMain {
            val e = Entorno()
            e.conProgreso(ProgresoEntity(7, 5, completada = true, selfieUri = "file:///s.jpg"))

            e.vm.cargar(5)

            e.vm.estado.value.paso shouldBe PasoEvidencia.ENTORNO
        }
    }

    "si ya están las dos fotos queda en LISTO" {
        pruebaConMain {
            val e = Entorno()
            e.conProgreso(ProgresoEntity(7, 5, completada = true, selfieUri = "s", fotoEntornoUri = "e"))

            e.vm.cargar(5)

            e.vm.estado.value.paso shouldBe PasoEvidencia.LISTO
        }
    }

    "sin progreso (cápsula no completada) no hay evidencia que pedir" {
        pruebaConMain {
            val e = Entorno()
            e.conProgreso(null)

            e.vm.cargar(5)

            e.vm.estado.value.cargando shouldBe false
            e.vm.estado.value.capsula.shouldBeNull()
        }
    }

    "si la base falla termina de cargar sin datos" {
        pruebaConMain {
            val e = Entorno()
            coEvery { e.juego.obtenerCapsula(any()) } throws IllegalStateException("db")

            e.vm.cargar(5)

            e.vm.estado.value shouldBe EvidenciaUiState(cargando = false)
        }
    }

    "una selfie sin rostro se rechaza y no se guarda nada" {
        pruebaConMain {
            val e = Entorno()
            e.conProgreso(ProgresoEntity(7, 5, completada = true))
            coEvery { e.detector.evaluar(any()) } returns ResultadoRostro.SIN_ROSTRO
            e.vm.cargar(5)

            e.vm.onFotoCapturada(mockk<Bitmap>())

            e.vm.estado.value.error shouldBe ErrorEvidencia.SIN_ROSTRO
            e.vm.estado.value.paso shouldBe PasoEvidencia.SELFIE
            e.vm.estado.value.procesando shouldBe false
            coVerify(exactly = 0) { e.almacen.guardar(any(), any()) }
        }
    }

    "varios rostros y rostro lejos tienen su propio error" {
        pruebaConMain {
            val e = Entorno()
            e.conProgreso(ProgresoEntity(7, 5, completada = true))
            e.vm.cargar(5)

            coEvery { e.detector.evaluar(any()) } returns ResultadoRostro.VARIOS_ROSTROS
            e.vm.onFotoCapturada(mockk<Bitmap>())
            e.vm.estado.value.error shouldBe ErrorEvidencia.VARIOS_ROSTROS

            coEvery { e.detector.evaluar(any()) } returns ResultadoRostro.ROSTRO_LEJOS
            e.vm.onFotoCapturada(mockk<Bitmap>())
            e.vm.estado.value.error shouldBe ErrorEvidencia.ROSTRO_LEJOS
        }
    }

    "si el detector falla se informa FALLO_GUARDADO" {
        pruebaConMain {
            val e = Entorno()
            e.conProgreso(ProgresoEntity(7, 5, completada = true))
            coEvery { e.detector.evaluar(any()) } throws IllegalStateException("ml kit")
            e.vm.cargar(5)

            e.vm.onFotoCapturada(mockk<Bitmap>())

            e.vm.estado.value.error shouldBe ErrorEvidencia.FALLO_GUARDADO
        }
    }

    "un fallo de la cámara se muestra como FALLO_CAPTURA" {
        pruebaConMain {
            val e = Entorno()

            e.vm.onErrorCaptura()

            e.vm.estado.value.error shouldBe ErrorEvidencia.FALLO_CAPTURA
        }
    }

    "en el paso LISTO una foto nueva se ignora" {
        pruebaConMain {
            val e = Entorno()
            e.conProgreso(ProgresoEntity(7, 5, completada = true, selfieUri = "s", fotoEntornoUri = "e"))
            e.vm.cargar(5)

            e.vm.onFotoCapturada(mockk<Bitmap>())

            coVerify(exactly = 0) { e.detector.evaluar(any()) }
        }
    }
})
