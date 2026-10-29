package com.example.aprendoiasantiago.ui.perfil

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aprendoiasantiago.R
import com.example.aprendoiasantiago.ui.components.DivisorIos
import com.example.aprendoiasantiago.ui.components.FilaIos
import com.example.aprendoiasantiago.ui.components.PantallaIos
import com.example.aprendoiasantiago.ui.components.SeccionIos
import com.example.aprendoiasantiago.ui.theme.IosAzul

/** Dirección genérica de soporte (cámbiala por la real de Triskeledu cuando exista). */
const val CORREO_SOPORTE = "soporte@aprendoia-santiago.example"

/** Ayuda y soporte: preguntas frecuentes y un botón que abre la app de correo ya redactada. */
@Composable
fun AyudaScreen(onVolver: () -> Unit, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    var sinApp by remember { mutableStateOf(false) }
    val asunto = stringResource(R.string.ayuda_correo_asunto)
    val cuerpo = stringResource(R.string.ayuda_correo_cuerpo)

    PantallaIos(
        titulo = stringResource(R.string.perfil_ayuda),
        textoVolver = stringResource(R.string.perfil_titulo),
        onVolver = onVolver,
        modifier = modifier
    ) {
        SeccionIos(
            encabezado = stringResource(R.string.ayuda_contacto),
            nota = stringResource(R.string.ayuda_contacto_nota, CORREO_SOPORTE)
        ) {
            FilaIos(
                titulo = stringResource(R.string.ayuda_enviar_correo),
                icono = Icons.Filled.Email,
                colorIcono = IosAzul,
                conFlecha = true,
                onClick = {
                    val intento = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(CORREO_SOPORTE))
                        putExtra(Intent.EXTRA_SUBJECT, asunto)
                        putExtra(Intent.EXTRA_TEXT, cuerpo)
                    }
                    try {
                        sinApp = false
                        contexto.startActivity(intento)
                    } catch (e: ActivityNotFoundException) {
                        sinApp = true
                    }
                }
            )
        }
        if (sinApp) {
            Text(
                text = stringResource(R.string.ayuda_sin_app_correo),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
        }

        SeccionIos(encabezado = stringResource(R.string.ayuda_faq)) {
            PreguntaFrecuente(R.string.ayuda_p1, R.string.ayuda_r1)
            DivisorIos(conIcono = false)
            PreguntaFrecuente(R.string.ayuda_p2, R.string.ayuda_r2)
            DivisorIos(conIcono = false)
            PreguntaFrecuente(R.string.ayuda_p3, R.string.ayuda_r3)
        }
    }
}

@Composable
private fun PreguntaFrecuente(pregunta: Int, respuesta: Int) {
    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
        Text(text = stringResource(pregunta), style = MaterialTheme.typography.titleSmall)
        Text(
            text = stringResource(respuesta),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
