package com.example.aprendoiasantiago.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Esquema fijo: no sigue el modo oscuro ni los colores dinámicos del sistema,
// para garantizar el mismo contraste siempre (caso: uso en la vía pública bajo sol directo).
private val EsquemaClaro = lightColorScheme(
    primary = AzulProfundo,
    onPrimary = Blanco,
    secondary = VerdeProfundo,
    onSecondary = Blanco,
    tertiary = NaranjaProfundo,
    onTertiary = Blanco,
    background = Blanco,
    onBackground = NegroSuave,
    surface = Blanco,
    onSurface = NegroSuave,
    error = RojoProfundo,
    onError = Blanco
)

@Composable
fun AprendoIASantiagoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = Typography,
        content = content
    )
}
