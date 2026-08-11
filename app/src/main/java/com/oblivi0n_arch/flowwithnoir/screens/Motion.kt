package com.oblivi0n_arch.flowwithnoir.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oblivi0n_arch.flowwithnoir.data.BreathPhase
import com.oblivi0n_arch.flowwithnoir.data.BreathPattern
import kotlinx.coroutines.delay

const val STAGGER_STEP_MILLIS = 60L
private const val STAGGER_DURATION_MILLIS = 420
private val staggerRise = 20.dp

private const val DOT_MIN_SCALE = 0.5f
private const val DOT_MAX_SCALE = 1f
private const val DOT_TOP_UP_SCALE = 1.25f

fun breathScale(
    phase: BreathPhase,
    progress: Float,
    hasTopUp: Boolean,
    minScale: Float,
    maxScale: Float,
    topUpScale: Float
): Float {
    val clamped = progress.coerceIn(0f, 1f)
    val peak = if (hasTopUp) topUpScale else maxScale
    return when (phase) {
        BreathPhase.INHALE -> {
            val eased = FastOutSlowInEasing.transform(clamped)
            minScale + (maxScale - minScale) * eased
        }
        BreathPhase.INHALE_TOP -> {
            val eased = LinearOutSlowInEasing.transform(clamped)
            maxScale + (topUpScale - maxScale) * eased
        }
        BreathPhase.HOLD_IN -> peak
        BreathPhase.EXHALE -> {
            val eased = FastOutSlowInEasing.transform(clamped)
            peak - (peak - minScale) * eased
        }
        BreathPhase.HOLD_OUT -> minScale
    }
}

@Composable
fun Modifier.staggeredReveal(order: Int, isPlaying: Boolean): Modifier {
    val progress = remember { Animatable(if (isPlaying) 0f else 1f) }
    val risePx = with(LocalDensity.current) { staggerRise.toPx() }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            progress.snapTo(0f)
            delay(order * STAGGER_STEP_MILLIS)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(STAGGER_DURATION_MILLIS, easing = LinearOutSlowInEasing)
            )
        } else {
            progress.snapTo(1f)
        }
    }

    return this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * risePx
    }
}

@Composable
fun BreathingRing(
    pattern: BreathPattern,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 150.dp,
    strokeDp: Dp = 1.dp,
    minScale: Float = 0.62f
) {
    val cycleMillis = (pattern.cycleSeconds * 1000).coerceAtLeast(1000)
    val transition = rememberInfiniteTransition(label = "breathRing")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(cycleMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "breathRingTime"
    )

    val strokeColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer {
                val (phase, phaseProgress) = pattern.phaseAtMillis((time * cycleMillis).toLong())
                val scale = breathScale(
                    phase = phase,
                    progress = phaseProgress,
                    hasTopUp = pattern.hasTopUp,
                    minScale = minScale,
                    maxScale = 1f,
                    topUpScale = 1.12f
                )
                scaleX = scale
                scaleY = scale
                alpha = 0.45f + 0.55f * ((scale - minScale) / (1f - minScale)).coerceIn(0f, 1f)
            }
            .border(strokeDp, strokeColor, CircleShape)
    )
}

@Composable
fun BreathingDot(
    pattern: BreathPattern,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 8.dp
) {
    val cycleMillis = (pattern.cycleSeconds * 1000).coerceAtLeast(1000)
    val transition = rememberInfiniteTransition(label = "breathDot")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(cycleMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "breathDotTime"
    )

    val fillColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer {
                val (phase, phaseProgress) =
                    pattern.phaseAtMillis((time * cycleMillis).toLong())
                val scale = breathScale(
                    phase = phase,
                    progress = phaseProgress,
                    hasTopUp = pattern.hasTopUp,
                    minScale = DOT_MIN_SCALE,
                    maxScale = DOT_MAX_SCALE,
                    topUpScale = DOT_TOP_UP_SCALE
                )
                scaleX = scale
                scaleY = scale
                alpha = 0.45f + 0.55f *
                    ((scale - DOT_MIN_SCALE) / (DOT_MAX_SCALE - DOT_MIN_SCALE)).coerceIn(0f, 1f)
            }
            .background(fillColor, CircleShape)
            .border(1.dp, fillColor, CircleShape)
    )
}
