package com.example.aprendoiasantiago.ml

import android.graphics.Bitmap
import com.example.aprendoiasantiago.domain.ResultadoRostro
import com.example.aprendoiasantiago.domain.ValidadorRostro
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/** Revisa si una foto sirve como selfie. Es una interfaz para poder cambiar la implementación. */
interface DetectorRostro {
    /** [bitmap] ya viene derecho (sin rotación pendiente). */
    suspend fun evaluar(bitmap: Bitmap): ResultadoRostro
}

/** Detección de rostros con ML Kit: corre en el propio móvil, sin internet. */
class MlKitDetectorRostro : DetectorRostro {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setMinFaceSize(ValidadorRostro.PROPORCION_MINIMA)
            .build()
    )

    override suspend fun evaluar(bitmap: Bitmap): ResultadoRostro =
        suspendCancellableCoroutine { continuacion ->
            detector.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { rostros ->
                    val anchos = rostros.map { it.boundingBox.width() }
                    continuacion.resume(ValidadorRostro.evaluar(anchos, bitmap.width))
                }
                .addOnFailureListener { error -> continuacion.resumeWithException(error) }
        }
}
