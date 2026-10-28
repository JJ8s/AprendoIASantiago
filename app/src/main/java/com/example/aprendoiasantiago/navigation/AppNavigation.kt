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
import com.example.aprendoiasantiago.ui.admin.AdminScreen
import com.example.aprendoiasantiago.ui.viewmodels.AdminViewModel
import com.example.aprendoiasantiago.data.local.DatosEjemplo
import com.example.aprendoiasantiago.ui.gencont.GenContScreen
import com.example.aprendoiasantiago.ui.viewmodels.GenContViewModel
import com.example.aprendoiasantiago.ui.screens.ProgresoScreen // <-- IMPORT NUEVA PANTALLA
import com.example.aprendoiasantiago.ui.screens.CatalogoScreen

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
                        if (estado.usuario.equals(DatosEjemplo.USUARIO_ADMIN, ignoreCase = true)) {
                            // Si es Admin
                            navController.navigate("ruta_admin") {
                                popUpTo(RUTA_LOGIN) { inclusive = true }
                            }
                        } else if (estado.usuario.equals(DatosEjemplo.USUARIO_CREADOR, ignoreCase = true)) {
                            // Si es Creador de Contenido
                            navController.navigate("ruta_gencont") {
                                popUpTo(RUTA_LOGIN) { inclusive = true }
                            }
                        } else {
                            // Si es un jugador normal
                            navController.navigate(Destino.INICIO.ruta) {
                                popUpTo(RUTA_LOGIN) { inclusive = true }
                            }
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

            // 3. PANEL DE ADMINISTRADOR
            composable("ruta_admin") {
                val viewModel: AdminViewModel = viewModel(factory = fabrica)
                val usuarios by viewModel.usuarios.collectAsStateWithLifecycle()
                val puntos by viewModel.puntos.collectAsStateWithLifecycle()
                val capsulas by viewModel.capsulas.collectAsStateWithLifecycle()

                AdminScreen(
                    usuarios = usuarios,
                    puntos = puntos,
                    capsulas = capsulas,
                    onEliminarUsuario = viewModel::eliminarJugador,
                    onGuardarPunto = viewModel::guardarPunto,
                    onEliminarPunto = viewModel::eliminarPunto,
                    onGuardarCapsula = viewModel::guardarCapsula,
                    onEliminarCapsula = viewModel::eliminarCapsula,
                    onCerrarSesion = {
                        viewModel.cerrarSesion()
                        navController.navigate(RUTA_LOGIN) {
                            popUpTo("ruta_admin") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // 4. PANEL DE CREADOR DE CONTENIDO
            composable("ruta_gencont") {
                val viewModel: GenContViewModel = viewModel(factory = fabrica)
                val capsulas by viewModel.capsulas.collectAsStateWithLifecycle()
                val puntos by viewModel.puntos.collectAsStateWithLifecycle()

                GenContScreen(
                    capsulas = capsulas,
                    puntos = puntos,
                    onGuardarCapsula = viewModel::guardarCapsula,
                    onEliminarCapsula = viewModel::eliminarCapsula,
                    onCerrarSesion = {
                        viewModel.cerrarSesion()
                        navController.navigate(RUTA_LOGIN) {
                            popUpTo("ruta_gencont") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // 5. DASHBOARD PRINCIPAL
            composable(Destino.INICIO.ruta) {
                val viewModel: PerfilViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()

                InicioScreen(
                    estado = estado,
                    onIrAMapa = { navegarAPestana(Destino.MAPA) },
                    onIrAEscaner = { navegarAPestana(Destino.ESCANER) },
                    onIrAColeccion = { navegarAPestana(Destino.COLECCION) },
                    onVerProgresoClick = { navController.navigate("progreso") } // <-- ENLACE A NUEVA PANTALLA
                )
            }

            // 6. PANTALLA DE MAPA
            composable(Destino.MAPA.ruta) {
                val viewModel: MapaViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()
                MapaScreen(
                    estado = estado,
                    onSeleccionarRuta = viewModel::seleccionarRuta,
                    onVerCatalogoClick = { navController.navigate("catalogo") } // <--- ESTO CONECTA EL BOTÓN
                )
            }

            // 7. PANTALLA DE ESCÁNER QR
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

            // 8. PANTALLA DE EVIDENCIA (SELFIE)
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

            // 9. PANTALLA DE COLECCIÓN
            composable(Destino.COLECCION.ruta) {
                val viewModel: PerfilViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()

                ColeccionScreen(
                    estado = estado,
                    onVerCapsula = { capsulaId -> navController.navigate(rutaCapsula(capsulaId)) },
                    onIrAEscaner = { navController.navigate(Destino.ESCANER.ruta) }
                )
            }

            // 10. PANTALLA DE CÁPSULA (REPRODUCTOR DE VIDEO)
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

            // 11. PANTALLA DE PERFIL
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

            // 12. NUEVA PANTALLA: PUNTAJE Y PROGRESO
            composable("progreso") {
                // Instanciamos el ViewModel del perfil para obtener estadísticas actualizadas
                val viewModel: PerfilViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()

                val puntajeActual = estado.usuario?.puntaje ?: 0
                val qrTotales = estado.completadas

                ProgresoScreen(
                    puntosTotales = puntajeActual,
                    qrEncontrados = qrTotales,
                    videosVistos = qrTotales, // Se asume un video visto por cada QR escaneado
                    selfiesTomadas = estado.selfiesTomadas, // Usa estado.selfiesTomadas si lo tienes, o qrTotales como respaldo
                    puntosDescubiertos = qrTotales,
                    totalPuntosMapa = if (estado.totalCapsulas > 0) estado.totalCapsulas else 7,
                    onBackClick = { navController.popBackStack() }
                )
            }
            // 13. NUEVA PANTALLA: CATÁLOGO DE LUGARES Y QR
            composable("catalogo") {
                // Reutilizamos el ViewModel del perfil porque ya tiene la lista "progreso" cruzada lista para usar
                val viewModel: PerfilViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()

                CatalogoScreen(
                    progreso = estado.progreso, // Aquí vienen los estados (completada: true/false)
                    puntos = estado.puntos,     // Aquí vienen los nombres de las plazas/cerros
                    onBackClick = { navController.popBackStack() }
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