package com.example.aprendoiasantiago.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.aprendoiasantiago.AppContainer
import com.example.aprendoiasantiago.ui.escaner.EscanerScreen
import com.example.aprendoiasantiago.ui.login.LoginScreen
import com.example.aprendoiasantiago.ui.mapa.MapaScreen
import com.example.aprendoiasantiago.ui.perfil.PerfilScreen
import com.example.aprendoiasantiago.ui.viewmodels.EscanerViewModel
import com.example.aprendoiasantiago.ui.viewmodels.LoginViewModel
import com.example.aprendoiasantiago.ui.viewmodels.MapaViewModel
import com.example.aprendoiasantiago.ui.viewmodels.PerfilViewModel
import com.example.aprendoiasantiago.ui.viewmodels.ViewModelFactory

@Composable
fun AppNavigation(contenedor: AppContainer) {
    val fabrica = remember(contenedor) { ViewModelFactory(contenedor) }
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = Destino.deRuta(entradaActual?.destination?.route)

    Scaffold(
        bottomBar = {
            // La barra inferior solo existe en las pestañas, no en el login.
            if (destinoActual != null) {
                BarraInferior(actual = destinoActual) { destino ->
                    navController.navigate(destino.ruta) {
                        popUpTo(Destino.MAPA.ruta) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { relleno ->
        NavHost(
            navController = navController,
            startDestination = RUTA_LOGIN,
            modifier = Modifier.padding(relleno)
        ) {
            composable(RUTA_LOGIN) {
                val viewModel: LoginViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()

                LaunchedEffect(estado.autenticado) {
                    if (estado.autenticado) {
                        navController.navigate(Destino.MAPA.ruta) {
                            popUpTo(RUTA_LOGIN) { inclusive = true }
                        }
                    }
                }

                LoginScreen(
                    estado = estado,
                    onUsuarioCambia = viewModel::onUsuarioCambia,
                    onClaveCambia = viewModel::onClaveCambia,
                    onIngresar = viewModel::iniciarSesion
                )
            }

            composable(Destino.MAPA.ruta) {
                val viewModel: MapaViewModel = viewModel(factory = fabrica)
                val puntos by viewModel.puntos.collectAsStateWithLifecycle()
                MapaScreen(puntos = puntos)
            }

            composable(Destino.ESCANER.ruta) {
                val viewModel: EscanerViewModel = viewModel(factory = fabrica)
                val capsulas by viewModel.capsulas.collectAsStateWithLifecycle()
                EscanerScreen(capsulas = capsulas)
            }

            composable(Destino.PERFIL.ruta) {
                val viewModel: PerfilViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()
                PerfilScreen(
                    estado = estado,
                    onCerrarSesion = {
                        viewModel.cerrarSesion()
                        navController.navigate(RUTA_LOGIN) {
                            popUpTo(Destino.MAPA.ruta) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BarraInferior(actual: Destino, onSeleccion: (Destino) -> Unit) {
    NavigationBar {
        Destino.entries.forEach { destino ->
            NavigationBarItem(
                selected = destino == actual,
                onClick = { onSeleccion(destino) },
                icon = { Icon(imageVector = destino.icono, contentDescription = null) },
                label = { Text(stringResource(destino.titulo)) }
            )
        }
    }
}
