package com.oblivi0n_arch.flowwithnoir.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oblivi0n_arch.flowwithnoir.data.JournalEntry
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@Composable
fun JournalListScreen(entries: List<JournalEntry>) {
    var query by remember { mutableStateOf("") }
    var expandedIds by remember { mutableStateOf(setOf<Long>()) }

    val journalEntries = entries.filter { it.feeling != null || it.note.isNotBlank() }
    val filtered = if (query.isBlank()) {
        journalEntries
    } else {
        journalEntries.filter {
            it.note.contains(query, ignoreCase = true) ||
                it.sessionName.contains(query, ignoreCase = true)
        }
    }
    val grouped = filtered.groupBy {
        Instant.ofEpochMilli(it.timestampMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    }.toSortedMap(compareByDescending { it })

    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val maxPullPx = with(density) { 56.dp.toPx() }
    val pullOffset = remember { Animatable(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0 && pullOffset.value > 0f) {
                    val consumed = minOf(-available.y, pullOffset.value)
                    scope.launch { pullOffset.snapTo(pullOffset.value - consumed) }
                    return Offset(0f, -consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 0f) {
                    val newValue = (pullOffset.value + available.y).coerceAtMost(maxPullPx)
                    val used = newValue - pullOffset.value
                    scope.launch { pullOffset.snapTo(newValue) }
                    return Offset(0f, used)
                }
                return Offset.Zero
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    scope.launch { pullOffset.animateTo(0f, tween(200)) }
                })
            }
            .padding(horizontal = 26.dp, vertical = 52.dp)
    ) {
        Text(
            text = "Dziennik",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { pullOffset.value.toDp() })
                .clipToBounds()
        ) {
            if (pullOffset.value > 1f) {
                SearchBar(query = query, onQueryChange = { query = it })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (journalEntries.isEmpty()) "Twój dziennik jest pusty" else "Brak wyników",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        } else {
            LazyColumn(modifier = Modifier.nestedScroll(nestedScrollConnection)) {
                grouped.forEach { (date, entriesForDate) ->
                    item {
                        Text(
                            text = SimpleDateFormat("d MMMM", Locale("pl"))
                                .format(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 8.dp, top = 14.dp)
                        )
                    }
                    items(entriesForDate) { entry ->
                        JournalEntryCard(
                            entry = entry,
                            isExpanded = expandedIds.contains(entry.id),
                            onToggle = {
                                expandedIds = if (expandedIds.contains(entry.id)) {
                                    expandedIds - entry.id
                                } else {
                                    expandedIds + entry.id
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            if (query.isEmpty()) {
                Text(
                    text = "Szukaj w dzienniku",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun JournalEntryCard(entry: JournalEntry, isExpanded: Boolean, onToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isExpanded) 2.dp else 1.dp,
                color = if (isExpanded) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onToggle)
            .animateContentSize()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = entry.sessionName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = entryMeta(entry),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            entry.feeling?.let { feeling ->
                Icon(
                    imageVector = feelingIcon(feeling),
                    contentDescription = feeling.label,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (entry.note.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = entry.note,
                style = MaterialTheme.typography.labelSmall,
                color = if (isExpanded) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.secondary,
                maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun entryMeta(entry: JournalEntry): String {
    val time = SimpleDateFormat("HH:mm", Locale("pl")).format(Date(entry.timestampMillis))
    return if (entry.plannedDurationSeconds > 0) {
        val minutes = entry.actualDurationSeconds / 60
        "$time · $minutes min"
    } else {
        time
    }
}
