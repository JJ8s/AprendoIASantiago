package com.example.aprendoiasantiago.data.local

import android.graphics.Bitmap
import android.net.Uri
import java.io.File

/**
 * Guarda las fotos de evidencia en el almacenamiento privado de la app (no necesita permisos
 * ni aparece en la galería). Solo se guarda la ruta en la base de datos, no la imagen.
 */
class AlmacenEvidencias(private val directorio: File) {

    /** Guarda [bitmap] como JPEG y devuelve su dirección (`file://...`). Llamar fuera del hilo principal. */
    fun guardar(bitmap: Bitmap, prefijo: String): String {
        directorio.mkdirs()
        val archivo = File(directorio, "${prefijo}_${System.currentTimeMillis()}.jpg")
        archivo.outputStream().use { salida ->
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)) {
                "No se pudo guardar la imagen"
            }
        }
        return Uri.fromFile(archivo).toString()
    }

    /** Borra una foto guardada antes. Solo toca archivos dentro de este directorio. */
    fun borrar(uri: String) {
        val ruta = Uri.parse(uri).path ?: return
        val archivo = File(ruta)
        if (archivo.canonicalFile.parentFile == directorio.canonicalFile) archivo.delete()
    }

    private companion object {
        const val CALIDAD_JPEG = 85
    }
}
