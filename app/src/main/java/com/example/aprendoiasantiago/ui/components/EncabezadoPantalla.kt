package com.example.aprendoiasantiago.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Título de pantalla con un texto de apoyo debajo. */
@Composable
fun EncabezadoPantalla(titulo: String, aviso: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = titulo, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = aviso, style = MaterialTheme.typography.bodyMedium)
    }
}
