package com.example.aprendoiasantiago.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import com.example.aprendoiasantiago.data.local.UsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Avatares prediseñados. El índice se guarda en la base de datos (`avatarIndice`). */
val ICONOS_AVATAR = listOf(
    Icons.Filled.Person,
    Icons.Filled.Explore,
    Icons.Filled.RocketLaunch,
    Icons.Filled.EmojiEvents,
    Icons.Filled.School
)

/** Círculo con la foto del usuario si la tiene; si no, el ícono prediseñado que eligió. */
@Composable
fun AvatarUsuario(usuario: UsuarioEntity?, tamano: Dp, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    val foto by produceState<Bitmap?>(initialValue = null, usuario?.avatarUri) {
        val uri = usuario?.avatarUri
        value = if (uri == null) null else withContext(Dispatchers.IO) {
            cargarImagenCuadrada(contexto, Uri.parse(uri), 256)
        }
    }
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        val imagen = foto
        if (imagen != null) {
            Image(
                bitmap = imagen.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(tamano)
            )
        } else {
            val icono = ICONOS_AVATAR[(usuario?.avatarIndice ?: 0).coerceIn(0, ICONOS_AVATAR.lastIndex)]
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(tamano * 0.55f)
            )
        }
    }
}

/**
 * Lee una imagen (galería, cámara o archivo propio), la corrige según su orientación EXIF,
 * la recorta en cuadrado y la reduce a [lado] píxeles. Llamar fuera del hilo principal.
 */
fun cargarImagenCuadrada(contexto: android.content.Context, uri: Uri, lado: Int): Bitmap? = try {
    val resolver = contexto.contentResolver

    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
    if (limites.outWidth <= 0 || limites.outHeight <= 0) {
        null
    } else {
        var muestreo = 1
        while (minOf(limites.outWidth, limites.outHeight) / (muestreo * 2) >= lado) muestreo *= 2

        val original = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = muestreo })
        }
        if (original == null) {
            null
        } else {
            val grados = resolver.openInputStream(uri)?.use { flujo ->
                when (ExifInterface(flujo).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
            val girada = if (grados == 0f) original else Bitmap.createBitmap(
                original, 0, 0, original.width, original.height, Matrix().apply { postRotate(grados) }, true
            )
            val menor = minOf(girada.width, girada.height)
            val recortada = Bitmap.createBitmap(
                girada, (girada.width - menor) / 2, (girada.height - menor) / 2, menor, menor
            )
            if (menor > lado) Bitmap.createScaledBitmap(recortada, lado, lado, true) else recortada
        }
    }
} catch (e: Exception) {
    null
}
