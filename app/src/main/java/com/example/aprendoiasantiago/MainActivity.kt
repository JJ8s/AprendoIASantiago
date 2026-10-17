package com.example.aprendoiasantiago

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.aprendoiasantiago.navigation.AppNavigation
import com.example.aprendoiasantiago.ui.theme.AprendoIASantiagoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // La app usa solo tema claro de alto contraste (uso bajo sol directo):
        // se fuerzan íconos oscuros en las barras del sistema.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        val container = (application as AprendoApp).container
        setContent {
            AprendoIASantiagoTheme {
                AppNavigation(container)
            }
        }
    }

}
