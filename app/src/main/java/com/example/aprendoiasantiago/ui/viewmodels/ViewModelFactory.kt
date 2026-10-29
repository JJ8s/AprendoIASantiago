package com.example.aprendoiasantiago.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aprendoiasantiago.AppContainer

/** Construye los ViewModels entregándoles sus dependencias desde el [AppContainer]. */
class ViewModelFactory(private val contenedor: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(LoginViewModel::class.java) ->
            LoginViewModel(contenedor.auth, contenedor.sesion)
        modelClass.isAssignableFrom(MapaViewModel::class.java) ->
            MapaViewModel(contenedor.juego, contenedor.sesion)
        modelClass.isAssignableFrom(EscanerViewModel::class.java) ->
            EscanerViewModel(
                contenedor.juego,
                contenedor.sesion,
                contenedor.ubicacion,
                contenedor.notificaciones
            )
        modelClass.isAssignableFrom(EvidenciaViewModel::class.java) ->
            EvidenciaViewModel(
                contenedor.juego,
                contenedor.sesion,
                contenedor.almacenEvidencias,
                contenedor.detectorRostro
            )
        modelClass.isAssignableFrom(CapsulaViewModel::class.java) ->
            CapsulaViewModel(contenedor.juego)
        modelClass.isAssignableFrom(PerfilViewModel::class.java) ->
            PerfilViewModel(contenedor.juego, contenedor.sesion)

        modelClass.isAssignableFrom(AjustesPerfilViewModel::class.java) ->
            AjustesPerfilViewModel(
                contenedor.perfil,
                contenedor.notificaciones,
                contenedor.sesion,
                contenedor.almacenAvatares
            )

        // --- PANEL DE ADMINISTRADOR ---
        modelClass.isAssignableFrom(AdminViewModel::class.java) ->
            AdminViewModel(contenedor.auth, contenedor.juego, contenedor.sesion)

        // --- PANEL DE CREADOR DE CONTENIDO ---
        modelClass.isAssignableFrom(GenContViewModel::class.java) ->
            GenContViewModel(contenedor.juego, contenedor.sesion)

        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    } as T
}
