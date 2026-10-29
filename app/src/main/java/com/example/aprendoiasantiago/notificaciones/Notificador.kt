package com.example.aprendoiasantiago.notificaciones

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.aprendoiasantiago.MainActivity
import com.example.aprendoiasantiago.R

/** Muestra un aviso en la barra del sistema. Interfaz para poder reemplazarla en las pruebas. */
interface EmisorNotificaciones {
    fun mostrar(titulo: String, mensaje: String)
}

/** Notificaciones locales (sin servidor): se disparan desde la propia app. */
class Notificador(context: Context) : EmisorNotificaciones {

    private val app = context.applicationContext

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                app.getString(R.string.notif_canal_nombre),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            app.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    @SuppressLint("MissingPermission") // Se revisa en puedeMostrar() justo antes de notificar.
    override fun mostrar(titulo: String, mensaje: String) {
        if (!puedeMostrar(app)) return
        val abrirApp = PendingIntent.getActivity(
            app,
            0,
            Intent(app, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificacion = NotificationCompat.Builder(app, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(app).notify(System.currentTimeMillis().toInt(), notificacion)
    }

    companion object {
        private const val CANAL_ID = "aprendo_ia_general"

        /** Permiso de notificaciones (Android 13+) concedido y avisos no bloqueados en Ajustes. */
        fun puedeMostrar(context: Context): Boolean {
            val permiso = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            return permiso && NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
}
