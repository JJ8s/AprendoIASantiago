package com.example.aprendoiasantiago.ui.capsula

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.aprendoiasantiago.R

/**
 * Reproduce un video por internet con Media3 (ExoPlayer) y controles estándar.
 * Se pausa al salir de la pantalla y libera el reproductor al desaparecer de la composición.
 */
@Composable
fun ReproductorVideo(url: String, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    val ciclo = LocalLifecycleOwner.current.lifecycle
    var hayError by remember(url) { mutableStateOf(false) }

    val reproductor = remember(url) {
        ExoPlayer.Builder(contexto).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(reproductor, ciclo) {
        val oyente = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                hayError = true
            }
        }
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_PAUSE) reproductor.pause()
        }
        reproductor.addListener(oyente)
        ciclo.addObserver(observador)
        onDispose {
            ciclo.removeObserver(observador)
            reproductor.removeListener(oyente)
            reproductor.release()
        }
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (hayError) {
            Text(
                text = stringResource(R.string.capsula_video_error),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            AndroidView(
                factory = { ctx -> PlayerView(ctx).apply { player = reproductor } },
                update = { vista -> vista.player = reproductor },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
