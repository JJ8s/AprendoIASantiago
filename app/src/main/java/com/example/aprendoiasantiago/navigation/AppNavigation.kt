package com.example.aprendoiasantiago.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aprendoiasantiago.AppContainer
import com.example.aprendoiasantiago.ui.capsula.CapsulaScreen
import com.example.aprendoiasantiago.ui.escaner.EscanerScreen
import com.example.aprendoiasantiago.ui.evidencia.EvidenciaScreen
import com.example.aprendoiasantiago.ui.viewmodels.EvidenciaViewModel
import com.example.aprendoiasantiago.ui.inicio.InicioScreen
import com.example.aprendoiasantiago.ui.login.LoginScreen
import com.example.aprendoiasantiago.ui.mapa.MapaScreen
import com.example.aprendoiasantiago.ui.perfil.PerfilScreen
import com.example.aprendoiasantiago.ui.viewmodels.CapsulaViewModel
import com.example.aprendoiasantiago.ui.viewmodels.EscanerViewModel
import com.example.aprendoiasantiago.ui.viewmodels.LoginViewModel
import com.example.aprendoiasantiago.ui.viewmodels.MapaViewModel
import com.example.aprendoiasantiago.ui.viewmodels.PerfilViewModel
import com.example.aprendoiasantiago.ui.viewmodels.ViewModelFactory
import com.example.aprendoiasantiago.ui.coleccion.ColeccionScreen
import com.example.aprendoiasantiago.ui.bienvenida.BienvenidaScreen

@Composable
fun AppNavigation(contenedor: AppContainer) {
    val fabrica = remember(contenedor) { ViewModelFactory(contenedor) }
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = Destino.deRuta(entradaActual?.destination?.route)

    // Función centralizada para cambiar entre las 5 pestañas principales sin trabarse
    fun navegarAPestana(destino: Destino) {
        if (destino == Destino.INICIO) {
            // Si el usuario toca "Inicio", limpiamos las pantallas encima y volvemos directo al Dashboard
            val volvioAInicio = navController.popBackStack(Destino.INICIO.ruta, inclusive = false)
            if (!volvioAInicio) {
                navController.navigate(Destino.INICIO.ruta) {
                    launchSingleTop = true
                }
            }
        } else {
            // Si toca Mapa, Escáner, Colección o Perfil, navegamos dejando Inicio como base segura
            navController.navigate(destino.ruta) {
                popUpTo(Destino.INICIO.ruta) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (destinoActual != null) {
                BarraInferior(actual = destinoActual) { destino ->
                    navegarAPestana(destino)
                }
            }
        }
    ) { relleno ->
        NavHost(
            navController = navController,
            startDestination = RUTA_BIENVENIDA,
            modifier = Modifier.padding(relleno)
        ) {
            // 1. PANTALLA DE BIENVENIDA (Portada animada del juego)
            composable(RUTA_BIENVENIDA) {
                BienvenidaScreen(
                    onComenzar = {
                        navController.navigate(RUTA_LOGIN) {
                            popUpTo(RUTA_BIENVENIDA) { inclusive = true }
                        }
                    }
                )
            }

            // 2. PANTALLA DE LOGIN / SELECCIÓN DE JUGADOR
            composable(RUTA_LOGIN) {
                val viewModel: LoginViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()

                LaunchedEffect(estado.autenticado) {
                    if (estado.autenticado) {
                        navController.navigate(Destino.INICIO.ruta) {
                            popUpTo(RUTA_LOGIN) { inclusive = true }
                        }
                    }
                }

                LoginScreen(
                    estado = estado,
                    onUsuarioCambia = viewModel::onUsuarioCambia,
                    onClaveCambia = viewModel::onClaveCambia,
                    onConfirmarClaveCambia = viewModel::onConfirmarClaveCambia,
                    onIngresar = viewModel::iniciarSesion,
                    onNombreRegistroCambia = viewModel::onNombreRegistroCambia,
                    onAvatarCambia = viewModel::onAvatarCambia,
                    onCambiarModoRegistro = viewModel::cambiarModoRegistro,
                    onSeleccionarPerfil = viewModel::seleccionarPerfil,
                    onRegistrar = viewModel::registrarJugador
                )
            }

            composable(Destino.INICIO.ruta) {
                val viewModel: PerfilViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()

                InicioScreen(
                    estado = estado,
                    onIrAMapa = { navegarAPestana(Destino.MAPA) },
                    onIrAEscaner = { navegarAPestana(Destino.ESCANER) },
                    onIrAColeccion = { navegarAPestana(Destino.COLECCION) }
                )
            }

            composable(Destino.MAPA.ruta) {
                val viewModel: MapaViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()
                MapaScreen(
                    estado = estado,
                    onSeleccionarRuta = viewModel::seleccionarRuta
                )
            }

            composable(Destino.ESCANER.ruta) {
                val viewModel: EscanerViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()
                EscanerScreen(
                    estado = estado,
                    onCodigoDetectado = viewModel::onCodigoDetectado,
                    onEscanearOtro = viewModel::escanearOtro,
                    onVerCapsula = { capsulaId -> navController.navigate(rutaCapsula(capsulaId)) },
                    onTomarEvidencia = { capsulaId -> navController.navigate(rutaEvidencia(capsulaId)) }
                )
            }

            composable(
                route = RUTA_EVIDENCIA,
                arguments = listOf(navArgument(ARG_CAPSULA_ID) { type = NavType.LongType })
            ) { entrada ->
                val capsulaId = entrada.arguments?.getLong(ARG_CAPSULA_ID) ?: -1L
                val viewModel: EvidenciaViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()
                LaunchedEffect(capsulaId) { viewModel.cargar(capsulaId) }
                EvidenciaScreen(
                    estado = estado,
                    onFotoCapturada = viewModel::onFotoCapturada,
                    onErrorCaptura = viewModel::onErrorCaptura,
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(Destino.COLECCION.ruta) {
                val viewModel: PerfilViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()

                ColeccionScreen(
                    estado = estado,
                    onVerCapsula = { capsulaId -> navController.navigate(rutaCapsula(capsulaId)) },
                    onIrAEscaner = { navController.navigate(Destino.ESCANER.ruta) }
                )
            }

            composable(
                route = RUTA_CAPSULA,
                arguments = listOf(navArgument(ARG_CAPSULA_ID) { type = NavType.LongType })
            ) { entrada ->
                val capsulaId = entrada.arguments?.getLong(ARG_CAPSULA_ID) ?: -1L
                val viewModel: CapsulaViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()

                LaunchedEffect(capsulaId) {
                    viewModel.cargar(capsulaId)
                }

                CapsulaScreen(
                    estado = estado,
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(Destino.PERFIL.ruta) {
                val viewModel: PerfilViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()
                PerfilScreen(
                    estado = estado,
                    onCerrarSesion = {
                        viewModel.cerrarSesion()
                        navController.navigate(RUTA_LOGIN) {
                            popUpTo(Destino.INICIO.ruta) { inclusive = true }
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