package com.example.aprendoiasantiago.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aprendoiasantiago.AppContainer
import com.example.aprendoiasantiago.data.local.DatosEjemplo
import com.example.aprendoiasantiago.ui.admin.AdminScreen
import com.example.aprendoiasantiago.ui.bienvenida.BienvenidaScreen
import com.example.aprendoiasantiago.ui.capsula.CapsulaScreen
import com.example.aprendoiasantiago.ui.coleccion.ColeccionScreen
import com.example.aprendoiasantiago.ui.escaner.EscanerScreen
import com.example.aprendoiasantiago.ui.evidencia.EvidenciaScreen
import com.example.aprendoiasantiago.ui.gencont.GenContScreen
import com.example.aprendoiasantiago.ui.inicio.InicioScreen
import com.example.aprendoiasantiago.ui.login.LoginScreen
import com.example.aprendoiasantiago.ui.mapa.MapaScreen
import com.example.aprendoiasantiago.ui.perfil.AyudaScreen
import com.example.aprendoiasantiago.ui.perfil.EditarPerfilScreen
import com.example.aprendoiasantiago.ui.perfil.NotificacionesScreen
import com.example.aprendoiasantiago.ui.perfil.PerfilScreen
import com.example.aprendoiasantiago.ui.perfil.PermisosScreen
import com.example.aprendoiasantiago.ui.punto.PuntoDetalleScreen
import com.example.aprendoiasantiago.ui.screens.CatalogoScreen
import com.example.aprendoiasantiago.ui.screens.ProgresoScreen
import com.example.aprendoiasantiago.ui.viewmodels.AdminViewModel
import com.example.aprendoiasantiago.ui.viewmodels.AjustesPerfilViewModel
import com.example.aprendoiasantiago.ui.viewmodels.CapsulaViewModel
import com.example.aprendoiasantiago.ui.viewmodels.EscanerViewModel
import com.example.aprendoiasantiago.ui.viewmodels.EvidenciaViewModel
import com.example.aprendoiasantiago.ui.viewmodels.GenContViewModel
import com.example.aprendoiasantiago.ui.viewmodels.LoginViewModel
import com.example.aprendoiasantiago.ui.viewmodels.MapaViewModel
import com.example.aprendoiasantiago.ui.viewmodels.PerfilViewModel
import com.example.aprendoiasantiago.ui.viewmodels.PuntoDetalleViewModel
import com.example.aprendoiasantiago.ui.viewmodels.ViewModelFactory

@Composable
fun AppNavigation(contenedor: AppContainer) {
    val fabrica = remember(contenedor) { ViewModelFactory(contenedor) }
    val navController = rememberNavController()
    // Un solo ViewModel de ajustes (dueño: la Activity) para que perfil, escáner y mapa vean el mismo estado.
    val ajustesVm: AjustesPerfilViewModel = viewModel(factory = fabrica)
    val ajustes by ajustesVm.estado.collectAsStateWithLifecycle()
    // Punto que el mapa debe centrar al abrirse (lo fija "Ver en mapa" del detalle).
    var puntoEnfocado by remember { mutableStateOf<Long?>(null) }
    val camaraPermitida = ajustes.usuario?.permitirCamara ?: true
    val ubicacionPermitida = ajustes.usuario?.permitirUbicacion ?: true
    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = Destino.deRuta(entradaActual?.destination?.route)

    // Función centralizada para cambiar entre las 5 pestañas principales sin trabarse
    fun navegarAPestana(destino: Destino) {
        if (destino == Destino.INICIO) {
            val volvioAInicio = navController.popBackStack(Destino.INICIO.ruta, inclusive = false)
            if (!volvioAInicio) {
                navController.navigate(Destino.INICIO.ruta) {
                    launchSingleTop = true
                }
            }
        } else {
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
            // 1. PANTALLA DE BIENVENIDA
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
                            navController.navigate("ruta_admin") {
                                popUpTo(RUTA_LOGIN) { inclusive = true }
                            }
                        } else if (estado.usuario.equals(DatosEjemplo.USUARIO_CREADOR, ignoreCase = true)) {
                            navController.navigate("ruta_gencont") {
                                popUpTo(RUTA_LOGIN) { inclusive = true }
                            }
                        } else {
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

            // 3. PANEL DE ADMINISTRADOR COMPLETO
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

            // 5. DASHBOARD PRINCIPAL (Con enlace a tu ProgresoScreen)
            composable(Destino.INICIO.ruta) {
                val viewModel: PerfilViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()

                InicioScreen(
                    estado = estado,
                    onIrAMapa = { navegarAPestana(Destino.MAPA) },
                    onIrAEscaner = { navegarAPestana(Destino.ESCANER) },
                    onIrAColeccion = { navegarAPestana(Destino.COLECCION) },
                    onVerProgresoClick = { navController.navigate("progreso") }
                )
            }

            // 6. MAPA (Con el detalle de tu compañero + tu botón flotante de Catálogo QR)
            composable(Destino.MAPA.ruta) {
                val viewModel: MapaViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()
                MapaScreen(
                    estado = estado,
                    onSeleccionarRuta = viewModel::seleccionarRuta,
                    ubicacionPermitida = ubicacionPermitida,
                    puntoEnfocadoId = puntoEnfocado,
                    onPuntoEnfocado = { puntoEnfocado = null },
                    onAbrirDetalle = { puntoId -> navController.navigate(rutaPunto(puntoId)) },
                    onVerCatalogoClick = { navController.navigate("catalogo") }
                )
            }

            // 7. DETALLE DE PUNTO TURÍSTICO (Nuevo de tu compañero)
            composable(
                route = RUTA_PUNTO,
                arguments = listOf(navArgument(ARG_PUNTO_ID) { type = NavType.LongType })
            ) { entrada ->
                val puntoId = entrada.arguments?.getLong(ARG_PUNTO_ID) ?: -1L
                val viewModel: PuntoDetalleViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()
                LaunchedEffect(puntoId) { viewModel.cargar(puntoId) }
                PuntoDetalleScreen(
                    estado = estado,
                    onVolver = { navController.popBackStack() },
                    onIrAlEscaner = { navegarAPestana(Destino.ESCANER) },
                    onVerEnMapa = {
                        puntoEnfocado = puntoId
                        navegarAPestana(Destino.MAPA)
                    },
                    onVerCapsula = { capsulaId -> navController.navigate(rutaCapsula(capsulaId)) }
                )
            }

            // 8. ESCÁNER QR
            composable(Destino.ESCANER.ruta) {
                val viewModel: EscanerViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()
                EscanerScreen(
                    estado = estado,
                    onCodigoDetectado = viewModel::onCodigoDetectado,
                    onEscanearOtro = viewModel::escanearOtro,
                    onVerCapsula = { capsulaId -> navController.navigate(rutaCapsula(capsulaId)) },
                    onTomarEvidencia = { capsulaId -> navController.navigate(rutaEvidencia(capsulaId)) },
                    camaraPermitida = camaraPermitida,
                    onIrAPermisos = { navController.navigate(RUTA_PERFIL_PERMISOS) }
                )
            }

            // 9. EVIDENCIA (SELFIE)
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
                    onVolver = { navController.popBackStack() },
                    camaraPermitida = camaraPermitida,
                    onIrAPermisos = { navController.navigate(RUTA_PERFIL_PERMISOS) }
                )
            }

            // 10. COLECCIÓN / DESCUBRIMIENTOS
            composable(Destino.COLECCION.ruta) {
                val viewModel: PerfilViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()

                ColeccionScreen(
                    estado = estado,
                    onVerCapsula = { capsulaId -> navController.navigate(rutaCapsula(capsulaId)) },
                    onIrAEscaner = { navController.navigate(Destino.ESCANER.ruta) }
                )
            }

            // 11. REPRODUCTOR DE CÁPSULA DE VIDEO
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

            // 12. PERFIL Y CONFIGURACIÓN (Pantalla 15 renovada)
            composable(Destino.PERFIL.ruta) {
                val viewModel: PerfilViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()
                PerfilScreen(
                    estado = estado,
                    sinLeer = ajustes.sinLeer,
                    onEditarPerfil = { navController.navigate(RUTA_PERFIL_EDITAR) },
                    onNotificaciones = { navController.navigate(RUTA_PERFIL_NOTIFICACIONES) },
                    onPermisos = { navController.navigate(RUTA_PERFIL_PERMISOS) },
                    onAyuda = { navController.navigate(RUTA_PERFIL_AYUDA) },
                    onCerrarSesion = {
                        ajustesVm.cerrarSesion()
                        navController.navigate(RUTA_LOGIN) {
                            popUpTo(Destino.INICIO.ruta) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // SUB-PANTALLAS DE PERFIL (Editar, Notificaciones, Permisos, Ayuda)
            composable(RUTA_PERFIL_EDITAR) {
                EditarPerfilScreen(
                    estado = ajustes,
                    onGuardarDatos = ajustesVm::guardarDatos,
                    onCambiarClave = ajustesVm::cambiarClave,
                    onElegirAvatar = ajustesVm::elegirAvatar,
                    onFotoAvatar = ajustesVm::usarFotoAvatar,
                    onLimpiar = ajustesVm::limpiarMensajes,
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(RUTA_PERFIL_NOTIFICACIONES) {
                NotificacionesScreen(
                    estado = ajustes,
                    onGuardar = ajustesVm::guardarNotificaciones,
                    onAvisoDePrueba = ajustesVm::enviarAvisoDePrueba,
                    onMarcarLeidas = ajustesVm::marcarLeidas,
                    onVaciar = ajustesVm::vaciarBandeja,
                    onLimpiar = ajustesVm::limpiarMensajes,
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(RUTA_PERFIL_PERMISOS) {
                PermisosScreen(
                    usuario = ajustes.usuario,
                    onGuardarPermisos = ajustesVm::guardarPermisos,
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(RUTA_PERFIL_AYUDA) {
                AyudaScreen(onVolver = { navController.popBackStack() })
            }

            // 13. TU PANTALLA 12: PUNTAJE Y PROGRESO
            composable("progreso") {
                val viewModel: PerfilViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()

                val puntajeActual = estado.usuario?.puntaje ?: 0
                val qrTotales = estado.completadas

                ProgresoScreen(
                    puntosTotales = puntajeActual,
                    qrEncontrados = qrTotales,
                    videosVistos = qrTotales,
                    selfiesTomadas = estado.selfiesTomadas,
                    puntosDescubiertos = qrTotales,
                    totalPuntosMapa = if (estado.totalCapsulas > 0) estado.totalCapsulas else 7,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 14. TU PANTALLA DE CATÁLOGO DE QR DESDE EL MAPA
            composable("catalogo") {
                val viewModel: PerfilViewModel = viewModel(factory = fabrica)
                val estado by viewModel.estado.collectAsStateWithLifecycle()

                CatalogoScreen(
                    progreso = estado.progreso,
                    puntos = estado.puntos,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

/** Barra de pestañas al estilo iOS: fondo blanco, línea fina arriba, sin "píldora" y color solo en el ícono activo. */
@Composable
private fun BarraInferior(actual: Destino, onSeleccion: (Destino) -> Unit) {
    Column {
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            Destino.entries.forEach { destino ->
                NavigationBarItem(
                    selected = destino == actual,
                    onClick = { onSeleccion(destino) },
                    icon = { Icon(imageVector = destino.icono, contentDescription = null) },
                    label = { Text(stringResource(destino.titulo)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}