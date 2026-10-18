package com.example.aprendoiasantiago.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MainScreen() {
    // Estado para recordar qué pestaña está seleccionada
    var pestanaSeleccionada by remember { mutableStateOf(0) }

    // Lista de opciones para nuestra barra inferior
    val items = listOf("Mapa", "Escáner QR", "Perfil")
    val iconos = listOf(Icons.Filled.Map, Icons.Filled.CameraAlt, Icons.Filled.Person)

    // Scaffold actúa como la estructura base de la pantalla[cite: 26]
    Scaffold(
        bottomBar = {
            // Contenedor principal de la navegación inferior[cite: 36]
            NavigationBar {
                items.forEachIndexed { index, item ->
                    // Elemento interactivo individual[cite: 36]
                    NavigationBarItem(
                        icon = { Icon(imageVector = iconos[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = pestanaSeleccionada == index,
                        onClick = { pestanaSeleccionada = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        // El innerPadding asegura que el contenido no quede oculto bajo la barra[cite: 26]
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            // Aquí mostraremos temporalmente un texto para ver que la navegación funciona
            Text(text = "Pantalla actual: ${items[pestanaSeleccionada]}")
        }
    }
}

