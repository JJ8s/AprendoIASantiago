package com.example.aprendoiasantiago.ui.perfil

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.aprendoiasantiago.R
import com.example.aprendoiasantiago.domain.ErrorPerfil
import com.example.aprendoiasantiago.ui.components.AvatarUsuario
import com.example.aprendoiasantiago.ui.components.BotonIos
import com.example.aprendoiasantiago.ui.components.CampoIos
import com.example.aprendoiasantiago.ui.components.DivisorIos
import com.example.aprendoiasantiago.ui.components.FilaIos
import com.example.aprendoiasantiago.ui.components.ICONOS_AVATAR
import com.example.aprendoiasantiago.ui.components.PantallaIos
import com.example.aprendoiasantiago.ui.components.SeccionIos
import com.example.aprendoiasantiago.ui.components.cargarImagenCuadrada
import com.example.aprendoiasantiago.ui.viewmodels.AjustesPerfilUiState
import com.example.aprendoiasantiago.ui.viewmodels.MensajePerfil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Editar perfil: datos personales, foto de avatar y cambio de contraseña. */
@Composable
fun EditarPerfilScreen(
    estado: AjustesPerfilUiState,
    onGuardarDatos: (nombre: String, correo: String, telefono: String) -> Unit,
    onCambiarClave: (actual: String, nueva: String, confirmacion: String) -> Unit,
    onElegirAvatar: (Int) -> Unit,
    onFotoAvatar: (Bitmap) -> Unit,
    onLimpiar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    val usuario = estado.usuario

    // Al entrar y al salir se descartan mensajes de errores anteriores.
    LaunchedEffect(Unit) { onLimpiar() }

    var nombre by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.nombreVisible.orEmpty()) }
    var correo by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.correo.orEmpty()) }
    var telefono by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.telefono.orEmpty()) }
    var claveActual by rememberSaveable { mutableStateOf("") }
    var claveNueva by rememberSaveable { mutableStateOf("") }
    var claveConfirmacion by rememberSaveable { mutableStateOf("") }
    var eligiendoAvatar by rememberSaveable { mutableStateOf(false) }
    var sinPermisoCamara by rememberSaveable { mutableStateOf(false) }

    // Cuando la clave se cambió bien, se vacían los campos.
    LaunchedEffect(estado.mensaje) {
        if (estado.mensaje == MensajePerfil.CLAVE_CAMBIADA) {
            claveActual = ""
            claveNueva = ""
            claveConfirmacion = ""
        }
    }

    // --- Selección de foto ---
    val archivoCaptura = remember { File(File(contexto.cacheDir, "avatar_tmp").apply { mkdirs() }, "captura.jpg") }
    val uriCaptura = remember {
        FileProvider.getUriForFile(contexto, "${contexto.packageName}.fileprovider", archivoCaptura)
    }
    fun procesar(uri: Uri) {
        alcance.launch {
            val imagen = withContext(Dispatchers.IO) { cargarImagenCuadrada(contexto, uri, LADO_AVATAR) }
            if (imagen != null) onFotoAvatar(imagen)
        }
    }
    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) procesar(uriCaptura)
    }
    val lanzadorPermisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) lanzadorCamara.launch(uriCaptura) else sinPermisoCamara = true
    }
    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) procesar(uri)
    }

    PantallaIos(
        titulo = stringResource(R.string.perfil_editar),
        textoVolver = stringResource(R.string.perfil_titulo),
        onVolver = onVolver,
        modifier = modifier
    ) {
        // Avatar grande + acción
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AvatarUsuario(usuario, tamano = 104.dp)
            TextButton(onClick = { eligiendoAvatar = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.perfil_cambiar_foto), style = MaterialTheme.typography.bodyLarge)
            }
            if (estado.mensaje == MensajePerfil.AVATAR_ACTUALIZADO) {
                Text(
                    stringResource(R.string.perfil_msg_avatar),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        SeccionIos(encabezado = stringResource(R.string.editar_seccion_datos)) {
            CampoIos(stringResource(R.string.editar_nombre), nombre, { nombre = it })
            DivisorIos(conIcono = false)
            FilaIos(
                titulo = stringResource(R.string.editar_alias),
                detalle = "@${usuario?.nombreUsuario.orEmpty()}"
            )
            DivisorIos(conIcono = false)
            CampoIos(
                stringResource(R.string.editar_correo), correo, { correo = it },
                placeholder = stringResource(R.string.editar_opcional),
                tipoTeclado = KeyboardType.Email
            )
            DivisorIos(conIcono = false)
            CampoIos(
                stringResource(R.string.editar_telefono), telefono, { telefono = it },
                placeholder = stringResource(R.string.editar_opcional),
                tipoTeclado = KeyboardType.Phone
            )
        }
        MensajeEstado(
            error = estado.errorDatos,
            exito = estado.mensaje == MensajePerfil.DATOS_GUARDADOS,
            textoExito = stringResource(R.string.editar_msg_datos)
        )
        BotonIos(
            texto = stringResource(R.string.editar_guardar),
            onClick = { onGuardarDatos(nombre, correo, telefono) },
            modifier = Modifier.padding(top = 12.dp)
        )

        SeccionIos(
            encabezado = stringResource(R.string.editar_seccion_clave),
            nota = stringResource(R.string.editar_nota_clave)
        ) {
            CampoIos(
                stringResource(R.string.editar_clave_actual), claveActual, { claveActual = it },
                transformacion = PasswordVisualTransformation()
            )
            DivisorIos(conIcono = false)
            CampoIos(
                stringResource(R.string.editar_clave_nueva), claveNueva, { claveNueva = it },
                transformacion = PasswordVisualTransformation()
            )
            DivisorIos(conIcono = false)
            CampoIos(
                stringResource(R.string.editar_clave_confirmar), claveConfirmacion, { claveConfirmacion = it },
                transformacion = PasswordVisualTransformation()
            )
        }
        MensajeEstado(
            error = estado.errorClave,
            exito = estado.mensaje == MensajePerfil.CLAVE_CAMBIADA,
            textoExito = stringResource(R.string.editar_msg_clave)
        )
        BotonIos(
            texto = stringResource(R.string.editar_cambiar_clave),
            onClick = { onCambiarClave(claveActual, claveNueva, claveConfirmacion) },
            modifier = Modifier.padding(top = 12.dp)
        )
    }

    if (eligiendoAvatar) {
        AlertDialog(
            onDismissRequest = { eligiendoAvatar = false },
            title = { Text(stringResource(R.string.avatar_titulo)) },
            text = {
                Column {
                    val camaraPermitida = usuario?.permitirCamara != false
                    FilaOpcion(
                        texto = stringResource(
                            if (camaraPermitida) R.string.avatar_tomar_foto else R.string.avatar_camara_desactivada
                        ),
                        habilitada = camaraPermitida
                    ) {
                        eligiendoAvatar = false
                        sinPermisoCamara = false
                        if (tienePermisoCamara(contexto)) {
                            lanzadorCamara.launch(uriCaptura)
                        } else {
                            lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
                        }
                    }
                    FilaOpcion(texto = stringResource(R.string.avatar_galeria)) {
                        eligiendoAvatar = false
                        lanzadorGaleria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    Text(
                        text = stringResource(R.string.avatar_o_elige),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ICONOS_AVATAR.forEachIndexed { indice, icono ->
                            val elegido = usuario?.avatarUri == null && usuario?.avatarIndice == indice
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (elegido) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        eligiendoAvatar = false
                                        onElegirAvatar(indice)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icono,
                                    contentDescription = null,
                                    tint = if (elegido) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { eligiendoAvatar = false }) { Text(stringResource(R.string.cancelar)) }
            }
        )
    }

    if (sinPermisoCamara) {
        AlertDialog(
            onDismissRequest = { sinPermisoCamara = false },
            title = { Text(stringResource(R.string.avatar_sin_permiso_titulo)) },
            text = { Text(stringResource(R.string.avatar_sin_permiso_texto)) },
            confirmButton = {
                TextButton(onClick = { sinPermisoCamara = false }) { Text(stringResource(R.string.aceptar)) }
            }
        )
    }
}

@Composable
private fun FilaOpcion(texto: String, habilitada: Boolean = true, onClick: () -> Unit) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyLarge,
        color = if (habilitada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .then(if (habilitada) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp)
    )
}

/** Muestra el error (rojo) o la confirmación (verde) debajo de un formulario. */
@Composable
private fun MensajeEstado(error: ErrorPerfil?, exito: Boolean, textoExito: String) {
    when {
        error != null -> Text(
            text = stringResource(textoError(error)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp)
        )
        exito -> Text(
            text = textoExito,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp)
        )
    }
}

private fun textoError(error: ErrorPerfil): Int = when (error) {
    ErrorPerfil.NOMBRE_VACIO -> R.string.error_perfil_nombre_vacio
    ErrorPerfil.NOMBRE_LARGO -> R.string.error_perfil_nombre_largo
    ErrorPerfil.CORREO_INVALIDO -> R.string.error_perfil_correo
    ErrorPerfil.TELEFONO_INVALIDO -> R.string.error_perfil_telefono
    ErrorPerfil.CLAVE_ACTUAL_VACIA -> R.string.error_perfil_clave_actual_vacia
    ErrorPerfil.CLAVE_ACTUAL_INCORRECTA -> R.string.error_perfil_clave_actual_mala
    ErrorPerfil.CLAVE_NUEVA_VACIA -> R.string.error_perfil_clave_nueva_vacia
    ErrorPerfil.CLAVE_NUEVA_IGUAL -> R.string.error_perfil_clave_igual
    ErrorPerfil.CLAVES_NO_COINCIDEN -> R.string.error_claves_no_coinciden
    ErrorPerfil.ERROR_INESPERADO -> R.string.error_inesperado
}

private fun tienePermisoCamara(contexto: Context): Boolean =
    ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

private const val LADO_AVATAR = 512
