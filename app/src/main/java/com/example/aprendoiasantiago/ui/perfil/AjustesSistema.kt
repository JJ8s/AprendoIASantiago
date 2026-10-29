package com.example.aprendoiasantiago.ui.perfil

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/** Abre la ficha de esta app en los Ajustes de Android (desde ahí se revocan o conceden permisos del sistema). */
fun abrirAjustesDeLaApp(contexto: Context) {
    contexto.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", contexto.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

/** Abre los ajustes de notificaciones de esta app. */
fun abrirAjustesDeNotificaciones(contexto: Context) {
    contexto.startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, contexto.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
