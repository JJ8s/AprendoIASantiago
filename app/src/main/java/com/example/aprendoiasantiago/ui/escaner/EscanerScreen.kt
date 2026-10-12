package com.example.aprendoiasantiago.ui.escaner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.aprendoiasantiago.R
import com.example.aprendoiasantiago.data.repository.ResultadoEscaneo
import com.example.aprendoiasantiago.ui.viewmodels.EscanerUiState

@Composable
fun EscanerScreen(
    estado: EscanerUiState,
    onCodigoDetectado: (String) -> Unit,
    onEscanearOtro: () -> Unit,
    onVerCapsula: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val ciclo = LocalLifecycleOwner.current.lifecycle

    var permisoConcedido by remember { mutableStateOf(tienePermisoCamara(contexto)) }
    var yaPidioPermiso by rememberSaveable { mutableStateOf(false) }
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> permisoConcedido = concedido }

    // Pide el permiso una sola vez al abrir la pantalla (el caso exige el uso de la cámara).
    LaunchedEffect(Unit) {
        if (!permisoConcedido && !yaPidioPermiso) {
            yaPidioPermiso = true
            lanzadorPermiso.launch(Manifest.permission.CAMERA)
        }
    }

    // Si el usuario da el permiso desde Ajustes, se detecta al volver a la app.
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                permisoConcedido = tienePermisoCamara(contexto)
            }
        }
        ciclo.addObserver(observador)
        onDispose { ciclo.removeObserver(observador) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (permisoConcedido) {
            VistaCamara(
                escaneando = estado.escaneando,
                onCodigoDetectado = onCodigoDetectado,
                modifier = Modifier.fillMaxSize()
            )
            // Marco de ayuda para encuadrar el QR.
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(240.dp)
                    .border(4.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(24.dp))
            )
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.escaner_instruccion),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            SinPermisoCamara(
                onPermitir = { lanzadorPermiso.launch(Manifest.permission.CAMERA) },
                onAbrirAjustes = { abrirAjustesDeLaApp(contexto) },
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .imePadding()
        ) {
            val resultado = estado.resultado
            if (resultado != null) {
                TarjetaResultado(
                    resultado = resultado,
                    onVerCapsula = onVerCapsula,
                    onEscanearOtro = onEscanearOtro
                )
            } else {
                CodigoManual(onProbar = onCodigoDetectado, habilitado = !estado.procesando)
            }
        }
    }
}

@Composable
private fun SinPermisoCamara(
    onPermitir: () -> Unit,
    onAbrirAjustes: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.escaner_permiso_titulo),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.escaner_permiso_texto),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onPermitir,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(stringResource(R.string.escaner_permiso_boton), style = MaterialTheme.typography.labelLarge)
        }
        OutlinedButton(
            onClick = onAbrirAjustes,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(stringResource(R.string.escaner_ajustes_boton), style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Alternativa al escaneo: escribir el texto del QR a mano (útil en emulador o con la cámara dañada). */
@Composable
private fun CodigoManual(onProbar: (String) -> Unit, habilitado: Boolean) {
    var texto by rememberSaveable { mutableStateOf("") }
    Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text(stringResource(R.string.escaner_manual_label)) },
                singleLine = true,
                enabled = habilitado,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (texto.isNotBlank()) onProbar(texto) }),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { onProbar(texto) },
                enabled = habilitado && texto.isNotBlank(),
                modifier = Modifier.height(56.dp)
            ) {
                Text(stringResource(R.string.escaner_manual_boton), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun TarjetaResultado(
    resultado: ResultadoEscaneo,
    onVerCapsula: (Long) -> Unit,
    onEscanearOtro: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (resultado) {
                is ResultadoEscaneo.Valido -> {
                    Text(
                        text = stringResource(R.string.resultado_valido_titulo),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(text = resultado.capsula.titulo, style = MaterialTheme.typography.titleMedium)
                    resultado.punto?.let { punto ->
                        Text(
                            text = stringResource(R.string.resultado_punto, punto.nombre),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = if (resultado.esNueva) {
                            stringResource(R.string.resultado_puntos_ganados, resultado.puntosGanados)
                        } else {
                            stringResource(R.string.resultado_repetida)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = { onVerCapsula(resultado.capsula.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            stringResource(R.string.resultado_ver_capsula),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                is ResultadoEscaneo.Desconocido -> {
                    Text(
                        text = stringResource(R.string.resultado_desconocido_titulo),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = stringResource(R.string.resultado_desconocido_texto, resultado.codigo),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                ResultadoEscaneo.Error -> {
                    Text(
                        text = stringResource(R.string.resultado_error),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            OutlinedButton(
                onClick = onEscanearOtro,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    stringResource(R.string.resultado_escanear_otro),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private fun tienePermisoCamara(contexto: Context): Boolean =
    ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

private fun abrirAjustesDeLaApp(contexto: Context) {
    val intento = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", contexto.packageName, null)
    )
    contexto.startActivity(intento)
}
