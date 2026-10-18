package com.example.aprendoiasantiago.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aprendoiasantiago.R
import com.example.aprendoiasantiago.data.local.DatosEjemplo
import com.example.aprendoiasantiago.data.local.UsuarioEntity
import com.example.aprendoiasantiago.domain.ErrorLogin
import com.example.aprendoiasantiago.ui.viewmodels.LoginUiState

@Composable
fun LoginScreen(
    estado: LoginUiState,
    onUsuarioCambia: (String) -> Unit,
    onClaveCambia: (String) -> Unit,
    onIngresar: () -> Unit,
    onNombreRegistroCambia: (String) -> Unit = {},
    onAvatarCambia: (Int) -> Unit = {},
    onCambiarModoRegistro: (Boolean) -> Unit = {},
    onSeleccionarPerfil: (UsuarioEntity) -> Unit = {},
    onRegistrar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var claveVisible by rememberSaveable { mutableStateOf(false) }
    val iconosAvatar = listOf(
        Icons.Filled.Person,
        Icons.Filled.Explore,
        Icons.Filled.RocketLaunch,
        Icons.Filled.EmojiEvents,
        Icons.Filled.School
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (estado.modoRegistro) {
            // =========================================================================
            // PANTALLA 2 DEL PDF: CREAR PERFIL DE JUGADOR LOCAL EN SQLITE
            // =========================================================================
            Text(
                text = "Crear Perfil de Jugador",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Tu progreso se guardará en este dispositivo",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            // Selector de Avatar / Icono
            Text(
                text = "Elige tu avatar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                iconosAvatar.forEachIndexed { index, icono ->
                    val seleccionado = estado.avatarSeleccionado == index
                    Surface(
                        shape = CircleShape,
                        color = if (seleccionado) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = if (seleccionado) {
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            null
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clickable { onAvatarCambia(index) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icono,
                                contentDescription = null,
                                tint = if (seleccionado) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = estado.nombreVisibleRegistro,
                onValueChange = onNombreRegistroCambia,
                label = { Text("Nombre del jugador (ej: María González)") },
                singleLine = true,
                isError = estado.error == ErrorLogin.NOMBRE_VACIO,
                enabled = !estado.cargando,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estado.usuario,
                onValueChange = onUsuarioCambia,
                label = { Text("Alias o usuario (ej: maria_santiago)") },
                singleLine = true,
                isError = estado.error == ErrorLogin.USUARIO_VACIO ||
                        estado.error == ErrorLogin.USUARIO_YA_EXISTE,
                enabled = !estado.cargando,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estado.clave,
                onValueChange = onClaveCambia,
                label = { Text("Contraseña para tu perfil") },
                singleLine = true,
                isError = estado.error == ErrorLogin.CLAVE_VACIA,
                enabled = !estado.cargando,
                visualTransformation = if (claveVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { claveVisible = !claveVisible }) {
                        Icon(
                            imageVector = if (claveVisible) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = null
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onRegistrar() }),
                modifier = Modifier.fillMaxWidth()
            )

            estado.error?.let { error ->
                Text(
                    text = error.textoMensaje(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = onRegistrar,
                enabled = !estado.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(imageVector = Icons.Filled.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (estado.cargando) "Guardando..." else "Guardar y Comenzar",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            OutlinedButton(
                onClick = { onCambiarModoRegistro(false) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ya tengo un perfil (Seleccionar jugador)")
            }
        } else {
            // =========================================================================
            // PANTALLA 3 DEL PDF: SELECCIÓN DE JUGADOR ACTIVO E INICIO DE SESIÓN
            // =========================================================================
            Text(
                text = "¿Quién juega hoy?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Selecciona tu perfil o escribe tu usuario y contraseña",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            // Lista de perfiles guardados en SQLite
            if (estado.perfilesDisponibles.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    estado.perfilesDisponibles.forEach { perfil ->
                        val seleccionado = estado.usuario.equals(perfil.nombreUsuario, ignoreCase = true)
                        TarjetaPerfilJugador(
                            perfil = perfil,
                            seleccionado = seleccionado,
                            onClick = { onSeleccionarPerfil(perfil) }
                        )
                    }
                }
            }

            val errorEnUsuario = estado.error == ErrorLogin.USUARIO_VACIO ||
                    estado.error == ErrorLogin.CREDENCIALES_INCORRECTAS
            val errorEnClave = estado.error == ErrorLogin.CLAVE_VACIA ||
                    estado.error == ErrorLogin.CREDENCIALES_INCORRECTAS

            OutlinedTextField(
                value = estado.usuario,
                onValueChange = onUsuarioCambia,
                label = { Text(stringResource(R.string.login_usuario)) },
                singleLine = true,
                isError = errorEnUsuario,
                enabled = !estado.cargando,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estado.clave,
                onValueChange = onClaveCambia,
                label = { Text(stringResource(R.string.login_clave)) },
                singleLine = true,
                isError = errorEnClave,
                enabled = !estado.cargando,
                visualTransformation = if (claveVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { claveVisible = !claveVisible }) {
                        Icon(
                            imageVector = if (claveVisible) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = stringResource(
                                if (claveVisible) R.string.login_ocultar_clave else R.string.login_mostrar_clave
                            )
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onIngresar() }),
                modifier = Modifier.fillMaxWidth()
            )

            estado.error?.let { error ->
                Text(
                    text = error.textoMensaje(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = onIngresar,
                enabled = !estado.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = stringResource(
                        if (estado.cargando) R.string.login_ingresando else R.string.login_ingresar
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            OutlinedButton(
                onClick = { onCambiarModoRegistro(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Filled.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("+ Crear nuevo jugador")
            }

            Text(
                text = "Demo: ${DatosEjemplo.USUARIO_DEMO}/${DatosEjemplo.CLAVE_DEMO} | Admin: Admin/123 | Contenido: GenCont/123",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TarjetaPerfilJugador(
    perfil: UsuarioEntity,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val etiquetaRol = when (perfil.nombreUsuario.lowercase()) {
        "admin" -> "Rol: Administrador"
        "gencont" -> "Rol: Creador de Contenido"
        else -> "${perfil.puntaje} pts acumulados"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (seleccionado) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = perfil.nombreVisible.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column {
                    Text(
                        text = perfil.nombreVisible,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "@${perfil.nombreUsuario} • $etiquetaRol",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Text(
                text = if (seleccionado) "Seleccionado" else "Elegir",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ErrorLogin.textoMensaje(): String = when (this) {
    ErrorLogin.NOMBRE_VACIO -> "Ingresa el nombre visible del jugador"
    ErrorLogin.USUARIO_VACIO -> stringResource(R.string.error_usuario_vacio)
    ErrorLogin.CLAVE_VACIA -> stringResource(R.string.error_clave_vacia)
    ErrorLogin.CREDENCIALES_INCORRECTAS -> stringResource(R.string.error_credenciales)
    ErrorLogin.USUARIO_YA_EXISTE -> "Ese alias ya está registrado en este dispositivo"
    ErrorLogin.ERROR_INESPERADO -> stringResource(R.string.error_inesperado)
}
