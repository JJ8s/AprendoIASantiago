package com.example.aprendoiasantiago.ui.mapa

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

/**
 * Iconos del mapa dibujados con Canvas (formas vectoriales propias, sin imágenes externas).
 *  - Pin azul: punto pendiente.   - Pin verde con ✓: punto completado.
 *  - Pin naranja con número: parada de la ruta activa (si no está completado).
 *  - Punto azul con aro blanco: ubicación del jugador.
 */
object IconosMapa {

    const val COLOR_PENDIENTE = 0xFF1565C0.toInt()
    const val COLOR_COMPLETADO = 0xFF2E7D32.toInt()
    const val COLOR_PARADA = 0xFFEF6C00.toInt()

    private const val ANCHO_DP = 40f
    private const val ALTO_DP = 52f

    private val cache = HashMap<String, Bitmap>()

    /** Pin con punta abajo. [orden] > 0 muestra el número de parada; [completado] muestra un ✓. */
    fun pin(contexto: Context, completado: Boolean, orden: Int?): Drawable {
        val color = when {
            completado -> COLOR_COMPLETADO
            orden != null -> COLOR_PARADA
            else -> COLOR_PENDIENTE
        }
        val clave = "pin-$color-${if (completado) "ok" else orden ?: 0}"
        val bitmap = cache.getOrPut(clave) { dibujarPin(contexto, color, completado, orden) }
        return BitmapDrawable(contexto.resources, bitmap)
    }

    /** Punto de ubicación del jugador (círculo azul con aro blanco y sombra). */
    fun ubicacion(contexto: Context): Bitmap =
        cache.getOrPut("ubicacion") {
            val d = contexto.resources.displayMetrics.density
            val lado = (36 * d).toInt()
            val bmp = Bitmap.createBitmap(lado, lado, Bitmap.Config.ARGB_8888)
            val c = Canvas(bmp)
            val centro = lado / 2f
            val pintura = Paint(Paint.ANTI_ALIAS_FLAG)
            pintura.color = 0x331565C0
            c.drawCircle(centro, centro, 17 * d, pintura)          // halo
            pintura.color = 0xFFFFFFFF.toInt()
            c.drawCircle(centro, centro, 10 * d, pintura)          // aro blanco
            pintura.color = 0xFF1565C0.toInt()
            c.drawCircle(centro, centro, 7 * d, pintura)           // punto
            bmp
        }

    private fun dibujarPin(contexto: Context, color: Int, completado: Boolean, orden: Int?): Bitmap {
        val d = contexto.resources.displayMetrics.density
        val ancho = (ANCHO_DP * d).toInt()
        val alto = (ALTO_DP * d).toInt()
        val bmp = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        val cx = ancho / 2f
        val radio = 15f * d
        val cy = 3f * d + radio + 2f * d
        val punta = alto - 2f * d

        // Silueta del pin: círculo + triángulo hacia la punta.
        val silueta = Path().apply {
            addCircle(cx, cy, radio, Path.Direction.CW)
            moveTo(cx - radio * 0.78f, cy + radio * 0.62f)
            lineTo(cx, punta)
            lineTo(cx + radio * 0.78f, cy + radio * 0.62f)
            close()
        }

        val pintura = Paint(Paint.ANTI_ALIAS_FLAG)
        // Sombra suave
        pintura.style = Paint.Style.FILL
        pintura.color = 0x33000000
        c.save(); c.translate(0f, 1.5f * d); c.drawPath(silueta, pintura); c.restore()
        // Borde blanco
        pintura.style = Paint.Style.STROKE
        pintura.strokeWidth = 3f * d
        pintura.strokeJoin = Paint.Join.ROUND
        pintura.color = 0xFFFFFFFF.toInt()
        c.drawPath(silueta, pintura)
        // Relleno de color
        pintura.style = Paint.Style.FILL
        pintura.color = color
        c.drawPath(silueta, pintura)

        // Círculo interior blanco
        pintura.color = 0xFFFFFFFF.toInt()
        c.drawCircle(cx, cy, radio * 0.62f, pintura)

        pintura.color = color
        when {
            completado -> {
                // Check dibujado con trazo
                pintura.style = Paint.Style.STROKE
                pintura.strokeWidth = 2.6f * d
                pintura.strokeCap = Paint.Cap.ROUND
                pintura.strokeJoin = Paint.Join.ROUND
                val check = Path().apply {
                    moveTo(cx - radio * 0.32f, cy + radio * 0.02f)
                    lineTo(cx - radio * 0.08f, cy + radio * 0.28f)
                    lineTo(cx + radio * 0.36f, cy - radio * 0.24f)
                }
                c.drawPath(check, pintura)
            }
            orden != null -> {
                pintura.style = Paint.Style.FILL
                pintura.textAlign = Paint.Align.CENTER
                pintura.typeface = Typeface.DEFAULT_BOLD
                pintura.textSize = (if (orden > 9) 11f else 14f) * d
                val base = cy - (pintura.descent() + pintura.ascent()) / 2f
                c.drawText(orden.toString(), cx, base, pintura)
            }
            else -> {
                pintura.style = Paint.Style.FILL
                c.drawCircle(cx, cy, radio * 0.26f, pintura)
            }
        }
        return bmp
    }
}
