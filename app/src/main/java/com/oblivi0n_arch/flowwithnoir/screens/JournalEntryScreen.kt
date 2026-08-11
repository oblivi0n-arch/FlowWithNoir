package com.oblivi0n_arch.flowwithnoir.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oblivi0n_arch.flowwithnoir.data.Feeling
import com.oblivi0n_arch.flowwithnoir.data.JournalEntry
import com.oblivi0n_arch.flowwithnoir.data.JournalTag
import com.oblivi0n_arch.flowwithnoir.ui.theme.FlowWithNoirTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JournalEntryScreen(
    entry: JournalEntry?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMissing by remember { mutableStateOf(false) }

    LaunchedEffect(entry) {
        showMissing = false
        if (entry == null) {
            delay(400)
            showMissing = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = screenPaddingHorizontal)
            .padding(top = screenPaddingTop, bottom = screenPaddingBottom)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Wstecz",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onBack)
            )

            if (entry != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edytuj",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = onEdit)
                    )
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Usuń",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { showDeleteDialog = true }
                    )
                }
            }
        }

        if (entry == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (showMissing) {
                    Text(
                        text = "Nie znaleziono wpisu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            return@Column
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = fullDate(entry.timestampMillis).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = entry.displayTitle,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (entry.isSession) {
            Spacer(modifier = Modifier.height(12.dp))
            SessionPill(entry)
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 18.dp)
        ) {
            Text(
                text = entry.note.ifBlank { "Bez notatki" },
                style = MaterialTheme.typography.bodyMedium,
                color = if (entry.note.isBlank()) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.onBackground
            )
        }

        if (entry.feeling != null || entry.tags.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))

            entry.feeling?.let { feeling ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .border(1.dp, MaterialTheme.colorScheme.onBackground, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = feelingIcon(feeling),
                            contentDescription = feeling.label,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = feeling.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            if (entry.tags.isNotEmpty()) {
                if (entry.feeling != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                TagRow(tags = entry.tags)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.background,
            title = { Text("Usunąć wpis?") },
            text = { Text("Tej operacji nie da się cofnąć.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagRow(tags: List<JournalTag>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            TagChip(tag = tag, isSelected = false, isCompact = true)
        }
    }
}

@Composable
private fun SessionPill(entry: JournalEntry) {
    val status = if (entry.isCompleted) "ukończona" else "przerwana"
    Row(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${entry.sessionName} · ${formatDuration(entry.actualDurationSeconds)} · $status",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

private fun fullDate(timestampMillis: Long): String =
    SimpleDateFormat("EEEE, d MMMM · HH:mm", Locale("pl")).format(Date(timestampMillis))

private fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Preview(showBackground = true)
@Composable
fun JournalEntryScreenPreview() {
    FlowWithNoirTheme {
        JournalEntryScreen(
            entry = JournalEntry(
                id = 1,
                sessionName = "Poranny oddech",
                plannedDurationSeconds = 300,
                actualDurationSeconds = 300,
                timestampMillis = System.currentTimeMillis(),
                isCompleted = true,
                feeling = Feeling.CALM,
                note = "Trzecia minuta i myśli wreszcie odpuściły. Barki zeszły w dół same z siebie.",
                tags = listOf(JournalTag.WORK, JournalTag.REST),
                title = "Barki wreszcie zeszły"
            ),
            onBack = {},
            onEdit = {},
            onDelete = {}
        )
    }
}
