package com.example.aprendoiasantiago.ui.evidencia

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.aprendoiasantiago.R
import com.example.aprendoiasantiago.ui.components.CamaraPausada
import com.example.aprendoiasantiago.ui.viewmodels.ErrorEvidencia
import com.example.aprendoiasantiago.ui.viewmodels.EvidenciaUiState
import com.example.aprendoiasantiago.ui.viewmodels.PasoEvidencia

@Composable
fun EvidenciaScreen(
    estado: EvidenciaUiState,
    onFotoCapturada: (android.graphics.Bitmap) -> Unit,
    onErrorCaptura: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    camaraPermitida: Boolean = true,
    onIrAPermisos: () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxSize()) {
        IconButton(onClick = onVolver, modifier = Modifier.padding(4.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.capsula_volver)
            )
        }
        when {
            estado.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            estado.capsula == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.evidencia_no_disponible),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }

            estado.paso == PasoEvidencia.LISTO -> EvidenciaCompleta(estado.capsula.titulo, onVolver)

            !camaraPermitida -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CamaraPausada(onIrAPermisos = onIrAPermisos)
            }

            else -> PasoConCamara(estado, onFotoCapturada, onErrorCaptura)
        }
    }
}

@Composable
private fun EvidenciaCompleta(titulo: String, onVolver: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.evidencia_listo_titulo),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(text = titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.evidencia_listo_texto),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onVolver, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(stringResource(R.string.capsula_volver), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun PasoConCamara(
    estado: EvidenciaUiState,
    onFotoCapturada: (android.graphics.Bitmap) -> Unit,
    onErrorCaptura: () -> Unit
) {
    val contexto = LocalContext.current
    val ciclo = LocalLifecycleOwner.current.lifecycle

    var permisoConcedido by remember { mutableStateOf(tienePermisoCamara(contexto)) }
    var yaPidioPermiso by rememberSaveable { mutableStateOf(false) }
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> permisoConcedido = concedido }

    LaunchedEffect(Unit) {
        if (!permisoConcedido && !yaPidioPermiso) {
            yaPidioPermiso = true
            lanzadorPermiso.launch(Manifest.permission.CAMERA)
        }
    }
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) permisoConcedido = tienePermisoCamara(contexto)
        }
        ciclo.addObserver(observador)
        onDispose { ciclo.removeObserver(observador) }
    }

    val controlador = remember { ControladorCaptura() }
    DisposableEffect(controlador) { onDispose { controlador.cerrar() } }

    val esSelfie = estado.paso == PasoEvidencia.SELFIE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(
                if (esSelfie) R.string.evidencia_paso_selfie else R.string.evidencia_paso_entorno
            ),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(
                if (esSelfie) R.string.evidencia_instruccion_selfie else R.string.evidencia_instruccion_entorno
            ),
            style = MaterialTheme.typography.bodyLarge
        )

        if (permisoConcedido) {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(3f / 4f)) {
                CamaraCaptura(
                    frontal = esSelfie,
                    controlador = controlador,
                    modifier = Modifier.fillMaxSize()
                )
            }
            estado.error?.let { error ->
                Text(
                    text = stringResource(textoError(error)),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Button(
                onClick = { controlador.capturar(onFotoCapturada, onErrorCaptura) },
                enabled = !estado.procesando,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (estado.procesando) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.evidencia_tomar_foto), style = MaterialTheme.typography.labelLarge)
                }
            }
        } else {
            Text(
                text = stringResource(R.string.escaner_permiso_texto),
                style = MaterialTheme.typography.bodyLarge
            )
            Button(
                onClick = { lanzadorPermiso.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(stringResource(R.string.escaner_permiso_boton), style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
                onClick = { abrirAjustesDeLaApp(contexto) },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(stringResource(R.string.escaner_ajustes_boton), style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

private fun textoError(error: ErrorEvidencia): Int = when (error) {
    ErrorEvidencia.SIN_ROSTRO -> R.string.evidencia_error_sin_rostro
    ErrorEvidencia.VARIOS_ROSTROS -> R.string.evidencia_error_varios_rostros
    ErrorEvidencia.ROSTRO_LEJOS -> R.string.evidencia_error_rostro_lejos
    ErrorEvidencia.FALLO_CAPTURA -> R.string.evidencia_error_captura
    ErrorEvidencia.FALLO_GUARDADO -> R.string.evidencia_error_guardado
}

private fun tienePermisoCamara(contexto: Context): Boolean =
    ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

private fun abrirAjustesDeLaApp(contexto: Context) {
    contexto.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", contexto.packageName, null))
    )
}
