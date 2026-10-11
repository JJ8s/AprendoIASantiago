package com.example.aprendoiasantiago.ui.login

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aprendoiasantiago.R
import com.example.aprendoiasantiago.data.local.DatosEjemplo
import com.example.aprendoiasantiago.domain.ErrorLogin
import com.example.aprendoiasantiago.ui.viewmodels.LoginUiState

@Composable
fun LoginScreen(
    estado: LoginUiState,
    onUsuarioCambia: (String) -> Unit,
    onClaveCambia: (String) -> Unit,
    onIngresar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.login_titulo),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.login_subtitulo),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

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
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = estado.clave,
            onValueChange = onClaveCambia,
            label = { Text(stringResource(R.string.login_clave)) },
            singleLine = true,
            isError = errorEnClave,
            enabled = !estado.cargando,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onIngresar() }),
            modifier = Modifier.fillMaxWidth()
        )

        estado.error?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(error.mensaje()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onIngresar,
            enabled = !estado.cargando,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = stringResource(
                    if (estado.cargando) R.string.login_ingresando else R.string.login_ingresar
                ),
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(
                R.string.login_pista,
                DatosEjemplo.USUARIO_DEMO,
                DatosEjemplo.CLAVE_DEMO
            ),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@StringRes
private fun ErrorLogin.mensaje(): Int = when (this) {
    ErrorLogin.USUARIO_VACIO -> R.string.error_usuario_vacio
    ErrorLogin.CLAVE_VACIA -> R.string.error_clave_vacia
    ErrorLogin.CREDENCIALES_INCORRECTAS -> R.string.error_credenciales
    ErrorLogin.ERROR_INESPERADO -> R.string.error_inesperado
}
