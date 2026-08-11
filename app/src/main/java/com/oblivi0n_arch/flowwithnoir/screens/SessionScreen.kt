package com.oblivi0n_arch.flowwithnoir.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oblivi0n_arch.flowwithnoir.BuildConfig
import com.oblivi0n_arch.flowwithnoir.data.BreathPhase
import com.oblivi0n_arch.flowwithnoir.data.SessionCatalog
import com.oblivi0n_arch.flowwithnoir.data.SessionTemplate
import com.oblivi0n_arch.flowwithnoir.ui.theme.FlowWithNoirTheme
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

private const val CIRCLE_MIN_SCALE = 0.58f
private const val CIRCLE_MAX_SCALE = 1f
private const val CIRCLE_TOP_UP_SCALE = 1.48f
private const val FINISH_ANIMATION_MILLIS = 1100L

private val stageSize = 300.dp
private val ringSize = 260.dp
private val breathCircleSize = 190.dp

@Composable
fun SessionScreen(
    template: SessionTemplate,
    durationSeconds: Int,
    onClose: (elapsedSeconds: Int) -> Unit,
    onFinished: () -> Unit
) {
    val totalMillis = durationSeconds * 1000L
    val hasTopUp = template.pattern.hasTopUp

    var elapsedMillis by remember { mutableLongStateOf(0L) }
    var isPaused by remember { mutableStateOf(false) }
    var isFinishing by remember { mutableStateOf(false) }
    var hapticsEnabled by remember { mutableStateOf(true) }

    val circleScale = remember { mutableFloatStateOf(CIRCLE_MIN_SCALE) }
    val ringProgress = remember { mutableFloatStateOf(0f) }
    val topUpProgress = remember { mutableFloatStateOf(0f) }

    var phase by remember { mutableStateOf(BreathPhase.INHALE) }
    val haptics = LocalHapticFeedback.current

    val secondsLeft by remember {
        derivedStateOf { ((totalMillis - elapsedMillis) / 1000L).toInt().coerceAtLeast(0) }
    }

    LaunchedEffect(isPaused, isFinishing) {
        if (isPaused || isFinishing) return@LaunchedEffect

        var lastFrame = 0L
        while (elapsedMillis < totalMillis) {
            withFrameMillis { frame ->
                if (lastFrame != 0L) {
                    elapsedMillis = (elapsedMillis + (frame - lastFrame)).coerceAtMost(totalMillis)
                }
                lastFrame = frame

                val (currentPhase, progress) = template.pattern.phaseAtMillis(elapsedMillis)
                if (currentPhase != phase) {
                    phase = currentPhase
                }
                circleScale.floatValue = breathScale(
                    phase = currentPhase,
                    progress = progress,
                    hasTopUp = hasTopUp,
                    minScale = CIRCLE_MIN_SCALE,
                    maxScale = CIRCLE_MAX_SCALE,
                    topUpScale = CIRCLE_TOP_UP_SCALE
                )
                topUpProgress.floatValue =
                    if (currentPhase == BreathPhase.INHALE_TOP) progress else 0f
                ringProgress.floatValue =
                    if (totalMillis == 0L) 0f else elapsedMillis.toFloat() / totalMillis.toFloat()
            }
        }
        isFinishing = true
    }

    LaunchedEffect(phase, hapticsEnabled) {
        if (hapticsEnabled && !isFinishing && elapsedMillis > 0) {
            val effect = if (phase == BreathPhase.INHALE_TOP) {
                HapticFeedbackType.LongPress
            } else {
                HapticFeedbackType.TextHandleMove
            }
            haptics.performHapticFeedback(effect)
        }
    }

    LaunchedEffect(isFinishing) {
        if (isFinishing) {
            delay(FINISH_ANIMATION_MILLIS)
            onFinished()
        }
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (isFinishing) 0f else 1f,
        animationSpec = tween(600),
        label = "contentAlpha"
    )
    val pausedDim by animateFloatAsState(
        targetValue = if (isPaused) 0.4f else 1f,
        animationSpec = tween(350),
        label = "pausedDim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentAlpha)
                .screenInsets()
                .padding(horizontal = 30.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${template.technique} · ${template.pattern.shortLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (BuildConfig.DEBUG) {
                    Text(
                        text = "⏩ SKIP",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.clickable {
                            elapsedMillis = (totalMillis - 2000L).coerceAtLeast(0L)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            BreathingCircle(
                scaleProvider = { circleScale.floatValue },
                progressProvider = { ringProgress.floatValue },
                topUpProvider = { topUpProgress.floatValue },
                isPaused = isPaused,
                phase = phase,
                modifier = Modifier.alpha(pausedDim)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = formatTime(secondsLeft),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(26.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControlButton(
                    icon = Icons.Filled.Close,
                    contentDescription = "Zakończ sesję",
                    onClick = { onClose((elapsedMillis / 1000L).toInt()) }
                )
                ControlButton(
                    icon = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    contentDescription = if (isPaused) "Wznów" else "Pauza",
                    isMain = true,
                    onClick = { isPaused = !isPaused }
                )
                ControlButton(
                    icon = Icons.Filled.Vibration,
                    contentDescription = if (hapticsEnabled) "Wyłącz wibracje" else "Włącz wibracje",
                    isDimmed = !hapticsEnabled,
                    onClick = { hapticsEnabled = !hapticsEnabled }
                )
            }
        }

        FinishOverlay(isVisible = isFinishing)
    }
}

@Composable
private fun BreathingCircle(
    scaleProvider: () -> Float,
    progressProvider: () -> Float,
    topUpProvider: () -> Float,
    isPaused: Boolean,
    phase: BreathPhase,
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val accentColor = MaterialTheme.colorScheme.onBackground

    val infinite = rememberInfiniteTransition(label = "idlePulse")
    val idlePulse by infinite.animateFloat(
        initialValue = 0.9f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idlePulseValue"
    )

    Box(
        modifier = modifier.size(stageSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(breathCircleSize)
                .graphicsLayer {
                    val scale = if (isPaused) idlePulse * CIRCLE_MIN_SCALE else scaleProvider()
                    scaleX = scale
                    scaleY = scale
                    alpha = 0.35f + 0.65f * ((scale - CIRCLE_MIN_SCALE) /
                        (CIRCLE_MAX_SCALE - CIRCLE_MIN_SCALE)).coerceIn(0f, 1f)
                }
                .border(1.dp, accentColor, CircleShape)
                .drawBehind {
                    drawCircle(color = accentColor, alpha = 0.05f)
                }
        )

        Canvas(modifier = Modifier.size(stageSize)) {
            val progress = topUpProvider()
            if (progress > 0f) {
                val startRadius = breathCircleSize.toPx() / 2f
                val endRadius = stageSize.toPx() / 2f
                drawCircle(
                    color = accentColor,
                    radius = startRadius + (endRadius - startRadius) * progress,
                    alpha = 0.35f * (1f - progress),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        Canvas(modifier = Modifier.size(ringSize)) {
            val stroke = 2.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)

            drawArc(
                color = outlineColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke)
            )
            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = 360f * progressProvider().coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke)
            )

            val angleRad = Math.toRadians((-90f + 360f * progressProvider()).toDouble())
            val radius = (size.minDimension - stroke) / 2f
            drawCircle(
                color = accentColor,
                radius = 3.dp.toPx(),
                center = center + Offset(
                    (radius * cos(angleRad)).toFloat(),
                    (radius * sin(angleRad)).toFloat()
                )
            )
        }

        AnimatedContent(
            targetState = if (isPaused) "WSTRZYMANE" else phase.label,
            transitionSpec = {
                (fadeIn(tween(400)) togetherWith fadeOut(tween(250)))
            },
            label = "phaseLabel"
        ) { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun FinishOverlay(isVisible: Boolean) {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(700),
        label = "finishAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "finishScale"
    )

    if (alpha > 0.01f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .border(1.dp, MaterialTheme.colorScheme.onBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "GOTOWE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    contentDescription: String,
    isMain: Boolean = false,
    isDimmed: Boolean = false,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isMain) 1f else 0.94f,
        animationSpec = tween(200),
        label = "controlScale"
    )

    Box(
        modifier = Modifier
            .size(if (isMain) 60.dp else 44.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(
                color = if (isMain) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.background,
                shape = CircleShape
            )
            .border(
                width = if (isMain) 0.dp else 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isMain) MaterialTheme.colorScheme.background
            else MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(if (isMain) 24.dp else 18.dp)
                .alpha(if (isDimmed) 0.35f else 1f)
        )
    }
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Preview(showBackground = true)
@Composable
fun SessionScreenPreview() {
    FlowWithNoirTheme {
        SessionScreen(
            template = SessionCatalog.templates.first(),
            durationSeconds = 300,
            onClose = { },
            onFinished = {}
        )
    }
}
