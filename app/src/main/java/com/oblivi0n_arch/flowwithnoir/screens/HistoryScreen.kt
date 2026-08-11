package com.oblivi0n_arch.flowwithnoir.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oblivi0n_arch.flowwithnoir.data.JournalEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(entries: List<JournalEntry>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .screenInsets()
            .padding(horizontal = screenPaddingHorizontal)
            .padding(top = screenPaddingTop, bottom = screenPaddingBottom)
    ) {
        Text(
            text = "Historia",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Brak sesji w historii",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        } else {
            LazyColumn {
                items(entries) { entry ->
                    HistoryItem(entry)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(entry: JournalEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = entry.sessionName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = historyMeta(entry),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        entry.feeling?.let { feeling ->
            Icon(
                imageVector = feelingIcon(feeling),
                contentDescription = feeling.label,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun historyMeta(entry: JournalEntry): String {
    val date = SimpleDateFormat("d MMM, HH:mm", Locale("pl")).format(Date(entry.timestampMillis))
    if (entry.plannedDurationSeconds == 0) {
        return date
    }
    val duration = formatMinutesSeconds(entry.actualDurationSeconds)
    return if (entry.isCompleted) {
        "$date · $duration"
    } else {
        val planned = formatMinutesSeconds(entry.plannedDurationSeconds)
        "$date · $duration / $planned · przerwana"
    }
}

private fun formatMinutesSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
