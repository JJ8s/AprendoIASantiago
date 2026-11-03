package com.example.aprendoiasantiago.ui.escaner

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Vista previa de la cámara trasera que lee códigos QR con ML Kit (en el propio móvil, sin internet).
 * Mientras [escaneando] sea `false` la cámara sigue visible pero no se analizan las imágenes.
 */
@Composable
fun VistaCamara(
    escaneando: Boolean,
    onCodigoDetectado: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val propietario = LocalLifecycleOwner.current

    val vistaPrevia = remember {
        PreviewView(contexto).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }

    // El analizador corre en otro hilo: se le pasa el estado más reciente por medios seguros entre hilos.
    val escaneandoRef = remember { AtomicBoolean(escaneando) }
    SideEffect { escaneandoRef.set(escaneando) }
    val alDetectar by rememberUpdatedState(onCodigoDetectado)

    DisposableEffect(propietario, vistaPrevia) {
        val ejecutor = Executors.newSingleThreadExecutor()
        val lector = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
        val futuroProveedor = ProcessCameraProvider.getInstance(contexto)
        var proveedor: ProcessCameraProvider? = null

        futuroProveedor.addListener(
            {
                val camara = futuroProveedor.get()
                proveedor = camara

                val vista = Preview.Builder().build().also {
                    it.setSurfaceProvider(vistaPrevia.surfaceProvider)
                }
                val analisis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { casoDeUso ->
                        casoDeUso.setAnalyzer(ejecutor) { imagen ->
                            analizar(imagen, lector, escaneandoRef.get()) { texto ->
                                alDetectar(texto)
                            }
                        }
                    }

                try {
                    camara.unbindAll()
                    camara.bindToLifecycle(
                        propietario,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        vista,
                        analisis
                    )
                } catch (e: Exception) {
                    // Sin cámara disponible o en uso por otra app: se queda sin vista previa.
                }
            },
            ContextCompat.getMainExecutor(contexto)
        )

        onDispose {
            proveedor?.unbindAll()
            lector.close()
            ejecutor.shutdown()
        }
    }

    AndroidView(factory = { vistaPrevia }, modifier = modifier)
}

/** Analiza un cuadro de la cámara. Siempre cierra la imagen, o la cámara dejaría de entregar cuadros. */
private fun analizar(
    imagen: ImageProxy,
    lector: BarcodeScanner,
    escaneando: Boolean,
    onTexto: (String) -> Unit
) {
    if (!escaneando) {
        imagen.close()
        return
    }
    val entrada = try {
        InputImage.fromBitmap(imagen.toBitmap(), imagen.imageInfo.rotationDegrees)
    } catch (e: Exception) {
        imagen.close()
        return
    }
    lector.process(entrada)
        .addOnSuccessListener { codigos ->
            codigos.firstNotNullOfOrNull { it.rawValue }?.let(onTexto)
        }
        .addOnCompleteListener { imagen.close() }
}
