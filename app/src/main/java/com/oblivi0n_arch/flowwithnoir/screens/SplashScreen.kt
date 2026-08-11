package com.oblivi0n_arch.flowwithnoir.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oblivi0n_arch.flowwithnoir.data.SessionCatalog
import com.oblivi0n_arch.flowwithnoir.ui.theme.FlowWithNoirTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private const val START_SCALE = 0.55f
private const val INHALE_SCALE = 1f
private const val EXPAND_SCALE = 9f
private const val EXPAND_MILLIS = 420
private const val MIN_INHALE_MILLIS = 900
private const val MAX_INHALE_MILLIS = 1400

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val pattern = remember { SessionCatalog.suggestedFor().pattern }
    val scale = remember { Animatable(START_SCALE) }
    val fade = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = INHALE_SCALE,
            animationSpec = tween(
                durationMillis = inhaleMillis(pattern.inhaleSeconds),
                easing = FastOutSlowInEasing
            )
        )
        launchExpand(scale, fade)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    alpha = fade.value
                }
                .border(1.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
        )
    }
}

private suspend fun launchExpand(scale: Animatable<Float, *>, fade: Animatable<Float, *>) {
    coroutineScope {
        val fadeJob = launch {
            fade.animateTo(0f, tween(EXPAND_MILLIS, easing = LinearOutSlowInEasing))
        }
        scale.animateTo(EXPAND_SCALE, tween(EXPAND_MILLIS, easing = LinearOutSlowInEasing))
        fadeJob.join()
    }
}

private fun inhaleMillis(inhaleSeconds: Int): Int {
    val raw = inhaleSeconds * 250
    return raw.coerceIn(MIN_INHALE_MILLIS, MAX_INHALE_MILLIS)
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    FlowWithNoirTheme {
        SplashScreen(onFinished = {})
    }
}
