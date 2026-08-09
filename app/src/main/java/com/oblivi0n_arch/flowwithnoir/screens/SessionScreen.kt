package com.oblivi0n_arch.flowwithnoir.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oblivi0n_arch.flowwithnoir.ui.theme.FlowWithNoirTheme
import kotlinx.coroutines.delay

@Composable
fun SessionScreen(
    sessionName: String,
    durationSeconds: Int,
    onClose: () -> Unit,
    onFinished: () -> Unit
) {
    var secondsLeft by remember { mutableIntStateOf(durationSeconds) }
    var isPaused by remember { mutableStateOf(false) }
    var isInhale by remember { mutableStateOf(true) }

    LaunchedEffect(isPaused) {
        while (secondsLeft > 0) {
            if (!isPaused) {
                delay(1000)
                secondsLeft -= 1
                isInhale = (secondsLeft / 4) % 2 == 0
            } else {
                delay(200)
            }
        }
        if (secondsLeft <= 0) {
            onFinished()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 30.dp, vertical = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = sessionName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(190.dp)
                .border(1.dp, MaterialTheme.colorScheme.onBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isInhale) "WDECH" else "WYDECH",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

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
            ControlButton(symbol = "✕", onClick = onClose)
            ControlButton(
                symbol = if (isPaused) "▶" else "❚❚",
                isMain = true,
                onClick = { isPaused = !isPaused }
            )
            ControlButton(symbol = "♪", onClick = { /* TODO: no-op, app is soundless */ })
        }
    }
}

@Composable
private fun ControlButton(symbol: String, isMain: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(if (isMain) 60.dp else 44.dp)
            .clickable(onClick = onClick)
            .background(
                color = if (isMain) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.background,
                shape = CircleShape
            )
            .border(
                width = if (isMain) 0.dp else 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = if (isMain) MaterialTheme.colorScheme.background
            else MaterialTheme.colorScheme.secondary
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
            sessionName = "Poranny oddech",
            durationSeconds = 300,
            onClose = {},
            onFinished = {}
        )
    }
}