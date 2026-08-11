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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oblivi0n_arch.flowwithnoir.data.SessionCatalog
import com.oblivi0n_arch.flowwithnoir.data.SessionTemplate
import com.oblivi0n_arch.flowwithnoir.data.WeekDayState
import com.oblivi0n_arch.flowwithnoir.ui.theme.FlowWithNoirTheme
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSessionStart: (templateId: String, durationSeconds: Int) -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onJournalClick: () -> Unit = {},
    onJournalListClick: () -> Unit = {},
    onAllSessionsClick: () -> Unit = {},
    streakDays: Int = 0,
    weekDays: List<WeekDayState> = emptyList(),
    lastEntrySummary: String = "Brak wpisów",
    nickname: String = "",
    playIntro: Boolean = false
) {
    val now = remember { LocalTime.now() }
    val featured = remember { SessionCatalog.featured(now) }
    val suggested = remember { SessionCatalog.suggestedFor(now) }
    val greeting = remember { greetingFor(now, LocalDate.now()) }

    var sheetTemplate by remember { mutableStateOf<SessionTemplate?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = screenPaddingHorizontal)
            .padding(top = screenPaddingTop, bottom = screenPaddingBottom)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (nickname.isBlank()) greeting else "$greeting, $nickname",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.staggeredReveal(0, playIntro)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Co dziś wybierzesz?",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.staggeredReveal(1, playIntro)
            )

            StreakRow(
                streakDays = streakDays,
                weekDays = weekDays,
                modifier = Modifier.staggeredReveal(2, playIntro)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredReveal(3, playIntro)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable(onClick = onJournalClick)
                    .padding(vertical = 14.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dziennik",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = lastEntrySummary,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredReveal(4, playIntro)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SESJE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Icon(
                    imageVector = Icons.Filled.Archive,
                    contentDescription = "Historia sesji",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onHistoryClick)
                )
            }

            featured.forEachIndexed { index, template ->
                SessionItem(
                    template = template,
                    isSuggested = template.id == suggested.id,
                    onClick = { sheetTemplate = template },
                    modifier = Modifier.staggeredReveal(5 + index, playIntro)
                )
                if (index < featured.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredReveal(5 + featured.size, playIntro)
                    .clickable(onClick = onAllSessionsClick)
                    .padding(vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wszystkie sesje",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "→",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        BottomNavBar(
            selected = NavTab.SESSIONS,
            onJournalClick = onJournalListClick,
            onProfileClick = onProfileClick,
            modifier = Modifier.staggeredReveal(6 + featured.size, playIntro)
        )
    }

    sheetTemplate?.let { template ->
        SessionStartSheet(
            template = template,
            sheetState = sheetState,
            onDismiss = { sheetTemplate = null },
            onStart = { id, duration ->
                sheetTemplate = null
                onSessionStart(id, duration)
            }
        )
    }
}

@Composable
private fun StreakRow(
    streakDays: Int,
    weekDays: List<WeekDayState>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 18.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$streakDays",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (streakDays == 1) "dzień z rzędu\nbez przerwy" else "dni z rzędu\nbez przerwy",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        if (weekDays.size == WEEK_LABELS.size) {
            WeekStrip(weekDays)
        }
    }
}

private val WEEK_LABELS = listOf("P", "W", "Ś", "C", "P", "S", "N")

@Composable
private fun WeekStrip(weekDays: List<WeekDayState>) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        weekDays.forEachIndexed { index, state ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.alpha(if (state == WeekDayState.FUTURE) 0.35f else 1f)
            ) {
                Text(
                    text = WEEK_LABELS[index],
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                    color = if (state == WeekDayState.TODAY) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.secondary
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            color = if (state == WeekDayState.DONE) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                Color.Transparent
                            },
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = if (state == WeekDayState.TODAY) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun SessionItem(
    template: SessionTemplate,
    isSuggested: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = template.meta(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        if (isSuggested) {
            BreathingDot(pattern = template.pattern)
        } else {
            StatusDot(isActive = false)
        }
    }
}

private val morningGreetings = listOf(
    "Dzień dobry",
    "Nowy dzień",
    "Spokojnego poranka",
    "Zacznijmy powoli"
)

private val dayGreetings = listOf(
    "Cześć",
    "Chwila dla siebie",
    "Zwolnij na moment",
    "Dobrze, że jesteś"
)

private val eveningGreetings = listOf(
    "Dobry wieczór",
    "Koniec dnia",
    "Czas wyhamować",
    "Wieczorna cisza"
)

private val nightGreetings = listOf(
    "Późna pora",
    "Ciszej już",
    "Noc nie śpi",
    "Odpocznij"
)

internal fun greetingFor(time: LocalTime, date: LocalDate): String {
    val pool = when (time.hour) {
        in 5..11 -> morningGreetings
        in 12..17 -> dayGreetings
        in 18..22 -> eveningGreetings
        else -> nightGreetings
    }
    return pool[date.dayOfYear % pool.size]
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    FlowWithNoirTheme {
        HomeScreen(onSessionStart = { _, _ -> }, onHistoryClick = {}, onProfileClick = {})
    }
}
