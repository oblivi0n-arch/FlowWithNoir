package com.oblivi0n_arch.flowwithnoir.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oblivi0n_arch.flowwithnoir.ui.theme.FlowWithNoirTheme

@Composable
fun HomeScreen(
    onSessionClick: (name: String, durationSeconds: Int) -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onJournalClick: () -> Unit = {},
    streakDays: Int = 0,
    lastEntrySummary: String = "Brak wpisów"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 26.dp, vertical = 52.dp)
    ) {
        Text(
            text = "Dzień dobry, użytkowniku",
            //TODO: make a small library with 10 different texts which are randomized depending on
            // a date. Also add the display for user's real name
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Co dziś wybierzesz?",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 18.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "$streakDays",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "dni z rzędu\nbez przerwy",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
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
        //placeholders
        SessionItem(name = "Poranny oddech", meta = "5 min · box breathing", durationSeconds = 300, isActive = true, onClick = { onSessionClick("Poranny oddech", 300) })
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        SessionItem(name = "Uziemienie", meta = "10 min · body scan", durationSeconds = 600, onClick = { onSessionClick("Uziemienie", 600) })
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        SessionItem(name = "Przed snem", meta = "8 min · 4-7-8", durationSeconds = 480, onClick = { onSessionClick("Przed snem", 480) })
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        SessionItem(name = "Życzliwość", meta = "7 min · metta", durationSeconds = 420, onClick = { onSessionClick("Życzliwość", 420) })
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavIcon(icon = Icons.Filled.SelfImprovement, isActive = true)
            NavIcon(icon = Icons.Filled.MenuBook, isActive = false)
            NavIcon(icon = Icons.Filled.AccountCircle, isActive = false, onClick = onProfileClick)
        }
    }
}

@Composable
fun SessionItem(name: String, meta: String, durationSeconds: Int, isActive: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = meta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (isActive) MaterialTheme.colorScheme.onBackground
                    else androidx.compose.ui.graphics.Color.Transparent,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
    }
}

@Composable
fun NavIcon(icon: ImageVector, isActive: Boolean, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) MaterialTheme.colorScheme.onBackground
            else MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(22.dp)
        )
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(
                    color = if (isActive) MaterialTheme.colorScheme.onBackground
                    else Color.Transparent,
                    shape = CircleShape
                )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    FlowWithNoirTheme {
        HomeScreen(onSessionClick = { _, _ -> }, onHistoryClick = {}, onProfileClick = {})
    }
}