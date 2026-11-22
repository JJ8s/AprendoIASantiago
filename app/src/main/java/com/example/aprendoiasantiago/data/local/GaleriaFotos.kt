package com.example.aprendoiasantiago.data.local

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore

/**
 * Guarda una copia de la foto en la galería del teléfono (carpeta Pictures/AprendoIA), como pide el caso para la selfie.
 * Android 10+ no necesita permisos. En Android 7–9 se necesita WRITE_EXTERNAL_STORAGE: si no está concedido,
 * la copia simplemente no se hace (la foto de la app queda guardada igual).
 */
object GaleriaFotos {

    fun publicador(contexto: Context): (Bitmap, String) -> Unit {
        val app = contexto.applicationContext
        return { bitmap, nombre -> guardar(app, bitmap, nombre) }
    }

    private fun guardar(contexto: Context, bitmap: Bitmap, nombre: String) {
        val resolver = contexto.contentResolver
        val datos = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$nombre.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AprendoIA")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val destino = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, datos) ?: return
        try {
            resolver.openOutputStream(destino)?.use { salida ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                datos.clear()
                datos.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(destino, datos, null, null)
            }
        } catch (e: Exception) {
            resolver.delete(destino, null, null)
        }
    }

    private const val CALIDAD_JPEG = 90
}
