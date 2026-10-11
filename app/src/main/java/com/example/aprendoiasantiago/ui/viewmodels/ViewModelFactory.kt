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
            MapaViewModel(contenedor.juego)
        modelClass.isAssignableFrom(EscanerViewModel::class.java) ->
            EscanerViewModel(contenedor.juego)
        modelClass.isAssignableFrom(PerfilViewModel::class.java) ->
            PerfilViewModel(contenedor.juego, contenedor.sesion)
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    } as T
}
