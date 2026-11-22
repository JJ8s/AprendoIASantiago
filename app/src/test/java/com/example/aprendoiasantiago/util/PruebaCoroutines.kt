package com.example.aprendoiasantiago.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Ejecuta un test con corrutinas y reemplaza el hilo principal de Android (`Dispatchers.Main`).
 *
 * Los ViewModel lanzan su trabajo en `viewModelScope`, que usa `Dispatchers.Main`; en un test de
 * JVM ese hilo no existe, así que se sustituye por uno de prueba que ejecuta todo al instante.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun pruebaConMain(bloque: suspend TestScope.() -> Unit) = runTest {
    Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
    try {
        bloque()
    } finally {
        Dispatchers.resetMain()
    }
}
