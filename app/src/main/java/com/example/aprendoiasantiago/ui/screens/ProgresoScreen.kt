package com.example.aprendoiasantiago.ui.screens // Ajusta si tu paquete de pantallas es distinto

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgresoScreen(
    puntosTotales: Int,
    qrEncontrados: Int,
    videosVistos: Int,
    selfiesTomadas: Int,
    puntosDescubiertos: Int,
    totalPuntosMapa: Int = 7, // O 30 si quieres mostrar el texto del mockup
    puntosPorQr: Int = qrEncontrados * 10,
    puntosPorSelfies: Int = selfiesTomadas * 100,
    onBackClick: () -> Unit
) {
    val metaMaximaPuntos = 10000
    val otrosDesafios = (puntosTotales - puntosPorQr - puntosPorSelfies).coerceAtLeast(0)

    // Dispara la animación al entrar a la pantalla
    var iniciarAnimacion by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        iniciarAnimacion = true
    }

    // Porcentaje del anillo circular (sobre los 10.000 puntos máximos de la rúbrica)
    val progresoAnilloObjetivo = (puntosTotales.toFloat() / metaMaximaPuntos.toFloat()).coerceIn(0f, 1f)
    val progresoAnilloAnimado by animateFloatAsState(
        targetValue = if (iniciarAnimacion) progresoAnilloObjetivo else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "anilloProgreso"
    )

    // Porcentaje de la barra horizontal (lugares descubiertos o progreso general)
    val progresoBarraObjetivo = if (totalPuntosMapa > 0) {
        (puntosDescubiertos.toFloat() / totalPuntosMapa.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val progresoBarraAnimado by animateFloatAsState(
        targetValue = if (iniciarAnimacion) progresoBarraObjetivo else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "barraProgreso"
    )

    val puntosAnimados by animateIntAsState(
        targetValue = if (iniciarAnimacion) puntosTotales else 0,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "contadorPuntos"
    )

    val formatoMiles = remember { NumberFormat.getIntegerInstance(Locale("es", "CL")) }

    // Paleta de colores estilo Mockup Oficial
    val fondoPantalla = Color(0xFFF6F8FC)
    val azulPrimario = Color(0xFF1565C0)
    val azulBrillante = Color(0xFF2979FF)
    val azulClaroAnillo = Color(0xFF64B5F6)
    val pistaGris = Color(0xFFE1E8F5)
    val textoOscuro = Color(0xFF0D1B2A)
    val textoSecundario = Color(0xFF5C6B73)

    Scaffold(
        containerColor = fondoPantalla,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mi Progreso",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = textoOscuro
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = textoOscuro
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = fondoPantalla
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. GRÁFICO CIRCULAR (GAUGE DE PUNTOS TOTALES)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val grosorTrazo = 18.dp.toPx()
                    // Arco de fondo (Gris azulado suave, abierto abajo como en el mockup)
                    drawArc(
                        color = pistaGris,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = grosorTrazo, cap = StrokeCap.Round)
                    )
                    // Arco de progreso con degradado azul atractivo
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(azulPrimario, azulBrillante, azulClaroAnillo, azulPrimario)
                        ),
                        startAngle = 135f,
                        sweepAngle = 270f * progresoAnilloAnimado,
                        useCenter = false,
                        style = Stroke(width = grosorTrazo, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatoMiles.format(puntosAnimados),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textoOscuro
                    )
                    Text(
                        text = "Puntos totales",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textoSecundario
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = azulBrillante.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Meta: 10.000 pts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = azulPrimario,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. TRES TARJETAS DE CONTADORES (QR, Videos, Selfies)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TarjetaEstadisticaMini(
                    valor = qrEncontrados.toString(),
                    etiqueta = "QR encontrados",
                    modifier = Modifier.weight(1f)
                )
                TarjetaEstadisticaMini(
                    valor = videosVistos.toString(),
                    etiqueta = "Videos vistos",
                    modifier = Modifier.weight(1f)
                )
                TarjetaEstadisticaMini(
                    valor = selfiesTomadas.toString(),
                    etiqueta = "Selfies turísticas",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // 3. SECCIÓN PROGRESO GENERAL (Barra horizontal)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Progreso general",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = textoOscuro
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(50))
                            .background(pistaGris)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progresoBarraAnimado.coerceAtLeast(0.04f))
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(azulPrimario, azulBrillante)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    val porcentajeTexto = (progresoBarraAnimado * 100).toInt()
                    Text(
                        text = "$porcentajeTexto%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textoOscuro
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$puntosDescubiertos de $totalPuntosMapa puntos descubiertos",
                    fontSize = 13.sp,
                    color = textoSecundario
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // 4. SECCIÓN TIPO DE PUNTOS (Desglose en tarjeta blanca)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tipo de puntos",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = textoOscuro
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(18.dp),
                            spotColor = Color.Black.copy(alpha = 0.08f)
                        ),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        FilaTipoPunto(
                            icono = Icons.Default.QrCodeScanner,
                            colorIcono = Color(0xFF1E68FF),
                            titulo = "Puntos por QR",
                            puntos = "$puntosPorQr pts"
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color(0xFFF0F3F8)
                        )
                        FilaTipoPunto(
                            icono = Icons.Default.CameraAlt,
                            colorIcono = Color(0xFF00838F),
                            titulo = "Puntos por selfies",
                            puntos = "$puntosPorSelfies pts"
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color(0xFFF0F3F8)
                        )
                        FilaTipoPunto(
                            icono = Icons.Default.EmojiEvents,
                            colorIcono = Color(0xFFFF9800),
                            titulo = "Otros desafíos",
                            puntos = "$otrosDesafios pts"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TarjetaEstadisticaMini(
    valor: String,
    etiqueta: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(92.dp)
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = valor,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0D1B2A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = etiqueta,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5C6B73),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FilaTipoPunto(
    icono: ImageVector,
    colorIcono: Color,
    titulo: String,
    puntos: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorIcono.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colorIcono),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = titulo,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF0D1B2A),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = puntos,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D1B2A)
        )
    }
}

