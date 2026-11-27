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
 * `setMain` y `resetMain` envuelven a `runTest` para que el dispatcher de prueba siga activo
 * mientras `runTest` cancela `backgroundScope` y limpia los flujos de `viewModelScope`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun pruebaConMain(bloque: suspend TestScope.() -> Unit) {
    val dispatcher = UnconfinedTestDispatcher()
    Dispatchers.setMain(dispatcher)
    try {
        runTest(dispatcher) {
            bloque()
        }
    } finally {
        Dispatchers.resetMain()
    }
}