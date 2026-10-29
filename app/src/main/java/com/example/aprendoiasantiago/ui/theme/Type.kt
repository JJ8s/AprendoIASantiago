package com.example.aprendoiasantiago.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Escala tipográfica al estilo iOS (título grande, cuerpo de 17 sp, notas de 13 sp).
// No se puede usar la fuente SF de Apple (licencia), así que se usa la del sistema Android.
private val Fuente = FontFamily.SansSerif

val Typography = Typography(
    headlineMedium = TextStyle(
        fontFamily = Fuente, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Fuente, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Fuente, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Fuente, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Fuente, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Fuente, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Fuente, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Fuente, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Fuente, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Fuente, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp
    )
)
