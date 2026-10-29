package com.example.aprendoiasantiago.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// Esquema fijo: no sigue el modo oscuro ni los colores dinámicos del sistema,
// para garantizar el mismo contraste siempre (caso: uso en la vía pública bajo sol directo).
private val EsquemaClaro = lightColorScheme(
    primary = AzulProfundo,
    onPrimary = Blanco,
    secondary = VerdeProfundo,
    onSecondary = Blanco,
    tertiary = NaranjaProfundo,
    onTertiary = Blanco,
    background = FondoAgrupado,
    onBackground = NegroSuave,
    surface = Blanco,
    onSurface = NegroSuave,
    surfaceVariant = GrisRelleno,
    onSurfaceVariant = GrisTextoSecundario,
    outline = GrisSeparador,
    outlineVariant = GrisSeparador,
    error = RojoProfundo,
    onError = Blanco
)

// Esquinas redondeadas generosas, como en iOS.
private val FormasIos = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AprendoIASantiagoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = Typography,
        shapes = FormasIos,
        content = content
    )
}
