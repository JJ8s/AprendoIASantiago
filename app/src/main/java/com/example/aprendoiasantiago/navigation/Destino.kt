package com.example.aprendoiasantiago.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.aprendoiasantiago.R

/** Ruta de la pantalla de login (no aparece en la barra inferior). */
const val RUTA_LOGIN = "login"

/** Pantalla de una cápsula (se abre desde el escáner; no es una pestaña). */
const val ARG_CAPSULA_ID = "capsulaId"
const val RUTA_CAPSULA = "capsula/{$ARG_CAPSULA_ID}"

fun rutaCapsula(capsulaId: Long): String = "capsula/$capsulaId"

/** Pestañas de la barra inferior: cada una reúne su ruta, su título y su ícono en un solo lugar. */
enum class Destino(
    val ruta: String,
    @StringRes val titulo: Int,
    val icono: ImageVector
) {
    MAPA("mapa", R.string.destino_mapa, Icons.Filled.Map),
    ESCANER("escaner", R.string.destino_escaner, Icons.Filled.CameraAlt),
    PERFIL("perfil", R.string.destino_perfil, Icons.Filled.Person);

    companion object {
        /** Devuelve la pestaña que corresponde a una ruta, o `null` (por ejemplo, en el login). */
        fun deRuta(ruta: String?): Destino? = entries.firstOrNull { it.ruta == ruta }
    }
}
