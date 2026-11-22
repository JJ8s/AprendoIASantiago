package com.example.aprendoiasantiago.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aprendoiasantiago.R
import com.example.aprendoiasantiago.data.model.Post
import com.example.aprendoiasantiago.ui.components.BotonIos
import com.example.aprendoiasantiago.ui.components.BotonSecundarioIos
import com.example.aprendoiasantiago.ui.components.CampoIos
import com.example.aprendoiasantiago.ui.components.PantallaIos
import com.example.aprendoiasantiago.ui.viewmodels.ErrorPost
import com.example.aprendoiasantiago.ui.viewmodels.PostUiState

/** Guía 14: lista de posts leídos de la API REST, con formulario para crear/editar y botón de borrar. */
@Composable
fun PostScreen(
    estado: PostUiState,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    onEditar: (Post?) -> Unit,
    onGuardar: (titulo: String, cuerpo: String) -> Unit,
    onGuardarSoloTitulo: (String) -> Unit,
    onEliminar: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    PantallaIos(
        titulo = stringResource(R.string.posts_titulo),
        textoVolver = stringResource(R.string.perfil_ayuda),
        onVolver = onVolver,
        modifier = modifier
    ) {
        FormularioPost(estado.enEdicion, onGuardar, onGuardarSoloTitulo, onCancelar = { onEditar(null) })

        estado.error?.let {
            Text(
                text = stringResource(
                    when (it) {
                        ErrorPost.SIN_CONEXION -> R.string.posts_error_conexion
                        ErrorPost.FORMULARIO_VACIO -> R.string.posts_error_vacio
                        ErrorPost.ERROR_SERVIDOR -> R.string.posts_error_servidor
                    }
                ),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            if (estado.posts.isEmpty()) BotonSecundarioIos(
                texto = stringResource(R.string.posts_reintentar),
                onClick = onReintentar,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (estado.cargando) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            // PantallaIos ya hace scroll vertical: una LazyColumn adentro fallaría (altura infinita).
            // Son ~100 posts de texto corto, así que una Column normal alcanza.
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                estado.posts.forEach { post ->
                    key(post.id) {
                        FilaPost(post, onEditar = { onEditar(post) }, onEliminar = { onEliminar(post) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FormularioPost(
    enEdicion: Post?,
    onGuardar: (String, String) -> Unit,
    onGuardarSoloTitulo: (String) -> Unit,
    onCancelar: () -> Unit
) {
    // Se reinicia cuando cambia el post en edición.
    var titulo by remember(enEdicion?.id) { mutableStateOf(enEdicion?.title.orEmpty()) }
    var cuerpo by remember(enEdicion?.id) { mutableStateOf(enEdicion?.body.orEmpty()) }

    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CampoIos(etiqueta = stringResource(R.string.posts_campo_titulo), valor = titulo, onCambio = { titulo = it })
        CampoIos(etiqueta = stringResource(R.string.posts_campo_cuerpo), valor = cuerpo, onCambio = { cuerpo = it })
        BotonIos(
            texto = stringResource(if (enEdicion == null) R.string.posts_crear else R.string.posts_guardar_put),
            onClick = {
                onGuardar(titulo, cuerpo)
                if (enEdicion == null && titulo.isNotBlank() && cuerpo.isNotBlank()) { titulo = ""; cuerpo = "" }
            }
        )
        if (enEdicion != null) {
            BotonSecundarioIos(stringResource(R.string.posts_guardar_patch), { onGuardarSoloTitulo(titulo) })
            BotonSecundarioIos(stringResource(R.string.posts_cancelar), onCancelar)
        }
    }
}

@Composable
private fun FilaPost(post: Post, onEditar: () -> Unit, onEliminar: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(post.title, style = MaterialTheme.typography.titleSmall)
            Text(
                post.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )
        }
        IconButton(onClick = onEditar) {
            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.posts_editar))
        }
        IconButton(onClick = onEliminar) {
            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.posts_eliminar))
        }
    }
}
