package com.oblivi0n_arch.flowwithnoir.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oblivi0n_arch.flowwithnoir.data.Feeling
import com.oblivi0n_arch.flowwithnoir.data.JournalEntry
import com.oblivi0n_arch.flowwithnoir.data.JournalTag
import com.oblivi0n_arch.flowwithnoir.data.daysWithEntries
import com.oblivi0n_arch.flowwithnoir.data.feelingCounts
import com.oblivi0n_arch.flowwithnoir.data.journalOnly
import com.oblivi0n_arch.flowwithnoir.ui.theme.FlowWithNoirTheme
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

private const val MOOD_WINDOW_DAYS = 30

@Composable
fun JournalListScreen(
    entries: List<JournalEntry>,
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onEntryClick: (Long) -> Unit = {},
    onNewEntryClick: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var feelingFilter by remember { mutableStateOf(setOf<Feeling>()) }
    var tagFilter by remember { mutableStateOf(setOf<JournalTag>()) }

    val focusManager = LocalFocusManager.current

    val journalEntries = entries.journalOnly()
    val activeFilterCount = feelingFilter.size + tagFilter.size

    val filtered = journalEntries.filter { entry ->
        val matchesQuery = query.isBlank() ||
            entry.note.contains(query, ignoreCase = true) ||
            entry.sessionName.contains(query, ignoreCase = true)
        val matchesFeeling = feelingFilter.isEmpty() || entry.feeling in feelingFilter
        val matchesTags = tagFilter.isEmpty() || entry.tags.any { it in tagFilter }
        matchesQuery && matchesFeeling && matchesTags
    }

    val grouped = filtered.groupBy {
        Instant.ofEpochMilli(it.timestampMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    }.toSortedMap(compareByDescending { it })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .screenInsets()
            .padding(horizontal = screenPaddingHorizontal)
            .padding(top = screenPaddingTop, bottom = screenPaddingBottom)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dziennik",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .clickable(onClick = onNewEntryClick)
                    .padding(horizontal = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SearchBar(
            query = query,
            onQueryChange = { query = it },
            activeFilterCount = activeFilterCount,
            isFilterOpen = showFilters,
            onFilterClick = { showFilters = !showFilters }
        )

        AnimatedVisibility(
            visible = showFilters,
            enter = fadeIn(tween(200)) + expandVertically(tween(200)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
        ) {
            FilterPanel(
                feelingFilter = feelingFilter,
                tagFilter = tagFilter,
                onToggleFeeling = { feeling ->
                    feelingFilter = if (feelingFilter.contains(feeling)) {
                        feelingFilter - feeling
                    } else {
                        feelingFilter + feeling
                    }
                },
                onToggleTag = { tag ->
                    tagFilter = if (tagFilter.contains(tag)) tagFilter - tag else tagFilter + tag
                },
                onClear = {
                    feelingFilter = emptySet()
                    tagFilter = emptySet()
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        journalEntries.isEmpty() -> "Twój dziennik jest pusty"
                        activeFilterCount > 0 || query.isNotBlank() -> "Brak wyników"
                        else -> "Brak wpisów"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                if (journalEntries.isNotEmpty()) {
                    item {
                        MoodSummary(entries = journalEntries)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
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
                    items(entriesForDate, key = { it.id }) { entry ->
                        JournalEntryCard(
                            entry = entry,
                            onClick = { onEntryClick(entry.id) }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }

        BottomNavBar(
            selected = NavTab.JOURNAL,
            onSessionsClick = onHomeClick,
            onProfileClick = onProfileClick
        )
    }
}

@Composable
private fun MoodSummary(entries: List<JournalEntry>) {
    val today = remember { LocalDate.now() }
    val activeDays = remember(entries) { entries.daysWithEntries(MOOD_WINDOW_DAYS, today).toSet() }
    val counts = remember(entries) { entries.feelingCounts(MOOD_WINDOW_DAYS, today) }
    val start = today.minusDays((MOOD_WINDOW_DAYS - 1).toLong())

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "OSTATNIE 30 DNI",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "${activeDays.size} z $MOOD_WINDOW_DAYS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(MOOD_WINDOW_DAYS) { index ->
                val date = start.plusDays(index.toLong())
                val hasEntry = activeDays.contains(date)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(26.dp)
                        .background(
                            color = if (hasEntry) MaterialTheme.colorScheme.onBackground
                            else Color.Transparent,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .border(
                            width = if (hasEntry) 0.dp else 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        if (counts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Feeling.entries.filter { counts.containsKey(it) }.forEach { feeling ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = feelingIcon(feeling),
                            contentDescription = feeling.label,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${counts[feeling]}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    activeFilterCount: Int,
    isFilterOpen: Boolean,
    onFilterClick: () -> Unit
) {
    val filterTint by animateColorAsState(
        targetValue = if (isFilterOpen || activeFilterCount > 0) {
            MaterialTheme.colorScheme.onBackground
        } else {
            MaterialTheme.colorScheme.secondary
        },
        animationSpec = tween(200),
        label = "filterTint"
    )

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
        Box(modifier = Modifier.weight(1f)) {
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
        if (query.isNotEmpty()) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Wyczyść",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onQueryChange("") }
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.clickable(onClick = onFilterClick)
        ) {
            Icon(
                imageVector = Icons.Filled.FilterList,
                contentDescription = "Filtry",
                tint = filterTint,
                modifier = Modifier.size(16.dp)
            )
            if (activeFilterCount > 0) {
                Text(
                    text = "$activeFilterCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = filterTint
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterPanel(
    feelingFilter: Set<Feeling>,
    tagFilter: Set<JournalTag>,
    onToggleFeeling: (Feeling) -> Unit,
    onToggleTag: (JournalTag) -> Unit,
    onClear: () -> Unit
) {
    Column(modifier = Modifier.padding(top = 14.dp)) {
        Text(
            text = "NASTRÓJ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Feeling.entries.forEach { feeling ->
                FeelingChip(
                    feeling = feeling,
                    isSelected = feelingFilter.contains(feeling),
                    onClick = { onToggleFeeling(feeling) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "TAGI",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            JournalTag.entries.forEach { tag ->
                TagChip(
                    tag = tag,
                    isSelected = tagFilter.contains(tag),
                    onClick = { onToggleTag(tag) },
                    isCompact = true
                )
            }
        }

        if (feelingFilter.isNotEmpty() || tagFilter.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Wyczyść filtry",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable(onClick = onClear)
            )
        }
    }
}

@Composable
private fun FeelingChip(feeling: Feeling, isSelected: Boolean, onClick: () -> Unit) {
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 0.dp else 1.dp,
        animationSpec = tween(180),
        label = "feelingChipBorder"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
        animationSpec = tween(180),
        label = "feelingChipContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.background
        else MaterialTheme.colorScheme.secondary,
        animationSpec = tween(180),
        label = "feelingChipContent"
    )

    Row(
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(999.dp))
            .border(borderWidth, MaterialTheme.colorScheme.outline, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = feelingIcon(feeling),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(11.dp)
        )
        Text(
            text = feeling.label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JournalEntryCard(entry: JournalEntry, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.displayTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (entry.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                entry.tags.forEach { tag ->
                    TagChip(tag = tag, isSelected = false, isCompact = true)
                }
            }
        }
    }
}

private fun entryMeta(entry: JournalEntry): String {
    val time = SimpleDateFormat("HH:mm", Locale("pl")).format(Date(entry.timestampMillis))
    if (!entry.isSession) return time

    val parts = mutableListOf(time)
    if (entry.title.isNotBlank()) parts += entry.sessionName
    parts += "${entry.actualDurationSeconds / 60} min"
    return parts.joinToString(" · ")
}

@Preview(showBackground = true)
@Composable
fun JournalListScreenPreview() {
    FlowWithNoirTheme {
        JournalListScreen(entries = emptyList())
    }
}
