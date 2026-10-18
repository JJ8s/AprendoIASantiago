package com.example.aprendoiasantiago

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AprendoApp : Application() {
    /** Alcance de corrutinas ligado a la vida del proceso (no se usa GlobalScope). */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this, appScope)
    }
}
