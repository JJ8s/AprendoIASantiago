package com.example.aprendoiasantiago.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aprendoiasantiago.ui.theme.IosVerde

/**
 * Piezas de diseño al estilo iOS: fondo agrupado, secciones en tarjetas redondeadas,
 * filas con ícono de color, interruptores y barra superior con "‹ Volver".
 * Se mantienen zonas táctiles de al menos 48 dp.
 */

/** Pantalla con barra superior al estilo iOS (flecha atrás + título centrado) y contenido con scroll. */
@Composable
fun PantallaIos(
    titulo: String,
    textoVolver: String,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable(onClick = onVolver)
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = textoVolver,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            content = contenido
        )
    }
}

/** Sección agrupada: encabezado pequeño arriba, tarjeta blanca redondeada y nota explicativa abajo. */
@Composable
fun SeccionIos(
    modifier: Modifier = Modifier,
    encabezado: String? = null,
    nota: String? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth().padding(top = 20.dp)) {
        if (encabezado != null) {
            Text(
                text = encabezado.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
            )
        }
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = contenido)
        }
        if (nota != null) {
            Text(
                text = nota,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp)
            )
        }
    }
}

/** Línea fina entre filas, con sangría a la izquierda como en iOS. */
@Composable
fun DivisorIos(conIcono: Boolean = true) {
    HorizontalDivider(
        modifier = Modifier.padding(start = if (conIcono) 56.dp else 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline
    )
}

/** Cuadrado redondeado de color con un ícono blanco, igual que en Ajustes de iOS. */
@Composable
fun IconoCuadrado(icono: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(30.dp).background(color, RoundedCornerShape(7.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
    }
}

/** Fila de una sección: ícono opcional, título, detalle a la derecha, y flecha si es navegable. */
@Composable
fun FilaIos(
    titulo: String,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    colorIcono: Color = MaterialTheme.colorScheme.primary,
    detalle: String? = null,
    colorTitulo: Color = MaterialTheme.colorScheme.onSurface,
    conFlecha: Boolean = false,
    onClick: (() -> Unit)? = null,
    extremo: @Composable (RowScope.() -> Unit)? = null
) {
    val base = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    Row(
        modifier = base
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icono != null) {
            IconoCuadrado(icono, colorIcono)
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodyLarge,
            color = colorTitulo,
            modifier = Modifier.weight(1f)
        )
        if (detalle != null) {
            Text(
                text = detalle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        extremo?.invoke(this)
        if (conFlecha) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Interruptor verde estilo iOS. */
@Composable
fun InterruptorIos(
    activo: Boolean,
    modifier: Modifier = Modifier,
    onCambio: (Boolean) -> Unit
) {
    Switch(
        checked = activo,
        onCheckedChange = onCambio,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = IosVerde,
            checkedBorderColor = IosVerde,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = MaterialTheme.colorScheme.outline,
            uncheckedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

/** Campo de texto plano dentro de una fila: etiqueta a la izquierda, valor a la derecha. */
@Composable
fun CampoIos(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    tipoTeclado: KeyboardType = KeyboardType.Text,
    transformacion: VisualTransformation = VisualTransformation.None
) {
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp).padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(112.dp)
        )
        TextField(
            value = valor,
            onValueChange = onCambio,
            singleLine = true,
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyLarge) },
            textStyle = MaterialTheme.typography.bodyLarge,
            visualTransformation = transformacion,
            keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

/** Botón de ancho completo con esquinas redondeadas (acción principal de un formulario). */
@Composable
fun BotonIos(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = habilitado,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp)
    ) {
        Text(text = texto, style = MaterialTheme.typography.labelLarge)
    }
}
