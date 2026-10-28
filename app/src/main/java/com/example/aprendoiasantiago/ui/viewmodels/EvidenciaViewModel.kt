package com.example.aprendoiasantiago.ui.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprendoiasantiago.data.local.AlmacenEvidencias
import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.repository.JuegoRepository
import com.example.aprendoiasantiago.data.repository.SesionRepository
import com.example.aprendoiasantiago.domain.ResultadoRostro
import com.example.aprendoiasantiago.ml.DetectorRostro
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class PasoEvidencia { SELFIE, ENTORNO, LISTO }

enum class ErrorEvidencia { SIN_ROSTRO, VARIOS_ROSTROS, ROSTRO_LEJOS, FALLO_CAPTURA, FALLO_GUARDADO }

data class EvidenciaUiState(
    val cargando: Boolean = true,
    /** `null` cuando la cápsula no existe o el usuario aún no la completa. */
    val capsula: CapsulaEntity? = null,
    val paso: PasoEvidencia = PasoEvidencia.SELFIE,
    val procesando: Boolean = false,
    val error: ErrorEvidencia? = null
)

/**
 * Pide dos fotos por cápsula completada: una selfie (se valida que haya un rostro) y una foto del entorno.
 * Cada foto se guarda apenas es válida, así que si el usuario sale a medias retoma donde quedó.
 */
class EvidenciaViewModel(
    private val juego: JuegoRepository,
    private val sesion: SesionRepository,
    private val almacen: AlmacenEvidencias,
    private val detector: DetectorRostro
) : ViewModel() {

    private val _estado = MutableStateFlow(EvidenciaUiState())
    val estado: StateFlow<EvidenciaUiState> = _estado.asStateFlow()

    private var capsulaId: Long = -1L

    fun cargar(id: Long) {
        capsulaId = id
        val usuarioId = sesion.usuarioId.value
        viewModelScope.launch {
            val estadoInicial = try {
                val capsula = juego.obtenerCapsula(id)
                val progreso = usuarioId?.let { juego.progreso(it, id) }
                if (capsula == null || progreso == null) {
                    EvidenciaUiState(cargando = false)
                } else {
                    val paso = when {
                        progreso.selfieUri == null -> PasoEvidencia.SELFIE
                        progreso.fotoEntornoUri == null -> PasoEvidencia.ENTORNO
                        else -> PasoEvidencia.LISTO
                    }
                    EvidenciaUiState(cargando = false, capsula = capsula, paso = paso)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                EvidenciaUiState(cargando = false)
            }
            _estado.value = estadoInicial
        }
    }

    /** La cámara entregó una foto (ya derecha) para el paso actual. */
    fun onFotoCapturada(foto: Bitmap) {
        val actual = _estado.value
        if (actual.procesando || actual.paso == PasoEvidencia.LISTO) return
        val usuarioId = sesion.usuarioId.value ?: return

        _estado.update { it.copy(procesando = true, error = null) }
        viewModelScope.launch {
            val nuevo = try {
                guardarSegunPaso(actual.paso, foto, usuarioId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                actual.copy(procesando = false, error = ErrorEvidencia.FALLO_GUARDADO)
            }
            _estado.value = nuevo
        }
    }

    private suspend fun guardarSegunPaso(paso: PasoEvidencia, foto: Bitmap, usuarioId: Long): EvidenciaUiState {
        val actual = _estado.value
        if (paso == PasoEvidencia.SELFIE) {
            val error = when (detector.evaluar(foto)) {
                ResultadoRostro.VALIDO -> null
                ResultadoRostro.SIN_ROSTRO -> ErrorEvidencia.SIN_ROSTRO
                ResultadoRostro.VARIOS_ROSTROS -> ErrorEvidencia.VARIOS_ROSTROS
                ResultadoRostro.ROSTRO_LEJOS -> ErrorEvidencia.ROSTRO_LEJOS
            }
            if (error != null) return actual.copy(procesando = false, error = error)
            val uri = withContext(Dispatchers.IO) { almacen.guardar(foto, "selfie") }
            juego.guardarSelfie(usuarioId, capsulaId, uri)
            return actual.copy(procesando = false, error = null, paso = PasoEvidencia.ENTORNO)
        }
        val uri = withContext(Dispatchers.IO) { almacen.guardar(foto, "entorno") }
        juego.guardarFotoEntorno(usuarioId, capsulaId, uri)
        return actual.copy(procesando = false, error = null, paso = PasoEvidencia.LISTO)
    }

    /** La cámara no pudo sacar la foto. */
    fun onErrorCaptura() {
        _estado.update { it.copy(procesando = false, error = ErrorEvidencia.FALLO_CAPTURA) }
    }
}
