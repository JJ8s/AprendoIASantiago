package com.example.aprendoiasantiago.ui.evidencia

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/** Permite que la pantalla pida una foto a la cámara que dibuja [CamaraCaptura]. */
class ControladorCaptura {
    @Volatile
    internal var captura: ImageCapture? = null
    private val ejecutor: ExecutorService = Executors.newSingleThreadExecutor()

    /** Saca una foto; los resultados llegan en otro hilo, ya derecha y con tamaño moderado. */
    fun capturar(onFoto: (Bitmap) -> Unit, onError: () -> Unit) {
        val casoDeUso = captura
        if (casoDeUso == null) {
            onError()
            return
        }
        casoDeUso.takePicture(
            ejecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imagen: ImageProxy) {
                    val foto = try {
                        imagen.toBitmap().girar(imagen.imageInfo.rotationDegrees)
                    } catch (e: Exception) {
                        null
                    } finally {
                        imagen.close() // Siempre se cierra, o la cámara deja de entregar fotos.
                    }
                    if (foto != null) onFoto(foto) else onError()
                }

                override fun onError(exception: ImageCaptureException) = onError()
            }
        )
    }

    fun cerrar() {
        captura = null
        ejecutor.shutdown()
    }
}

private fun Bitmap.girar(grados: Int): Bitmap {
    if (grados == 0) return this
    val matriz = Matrix().apply { postRotate(grados.toFloat()) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matriz, true)
}

/** Vista previa de la cámara frontal o trasera, lista para sacar fotos con [controlador]. */
@Composable
fun CamaraCaptura(
    frontal: Boolean,
    controlador: ControladorCaptura,
    modifier: Modifier = Modifier
) {
    val contexto: Context = LocalContext.current
    val propietario = LocalLifecycleOwner.current
    val vistaPrevia = remember {
        PreviewView(contexto).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }

    // Al cambiar de cámara (selfie -> entorno) el efecto se reinicia con la nueva.
    DisposableEffect(propietario, vistaPrevia, frontal) {
        val futuroProveedor = ProcessCameraProvider.getInstance(contexto)
        var proveedor: ProcessCameraProvider? = null

        futuroProveedor.addListener(
            {
                val camara = futuroProveedor.get()
                proveedor = camara

                val vista = Preview.Builder().build().also {
                    it.setSurfaceProvider(vistaPrevia.surfaceProvider)
                }
                // Fotos de ~1 MP: sobran para evidencia y se procesan rápido en cualquier móvil.
                val captura = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setResolutionSelector(
                        ResolutionSelector.Builder()
                            .setResolutionStrategy(
                                ResolutionStrategy(
                                    Size(1280, 960),
                                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER
                                )
                            )
                            .build()
                    )
                    .build()

                try {
                    camara.unbindAll()
                    camara.bindToLifecycle(
                        propietario,
                        if (frontal) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA,
                        vista,
                        captura
                    )
                    controlador.captura = captura
                } catch (e: Exception) {
                    // Sin esa cámara en el equipo o en uso: queda sin vista previa y capturar() avisa el error.
                    controlador.captura = null
                }
            },
            ContextCompat.getMainExecutor(contexto)
        )

        onDispose {
            controlador.captura = null
            proveedor?.unbindAll()
        }
    }

    AndroidView(factory = { vistaPrevia }, modifier = modifier)
}
