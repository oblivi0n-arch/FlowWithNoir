package com.oblivi0n_arch.flowwithnoir.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oblivi0n_arch.flowwithnoir.data.Feeling
import com.oblivi0n_arch.flowwithnoir.ui.theme.FlowWithNoirTheme

@Composable
fun ProfileScreen(
    nickname: String = "",
    streakDays: Int = 0,
    sessionCount: Int = 0,
    totalMeditatedMinutes: Int = 0,
    mostCommonFeeling: Feeling? = null,
    onSettingsClick: () -> Unit = {},
    onNicknameChange: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onJournalListClick: () -> Unit = {}
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                text = "PROFIL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable(onClick = onSettingsClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Ustawienia",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showEditDialog = true }
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .border(1.dp, MaterialTheme.colorScheme.onBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nickname.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = nickname.ifBlank { "Dodaj nick" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edytuj nick",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        StatsGrid(
            streakDays = streakDays,
            sessionCount = sessionCount,
            totalMeditatedMinutes = totalMeditatedMinutes,
            mostCommonFeeling = mostCommonFeeling
        )

        Spacer(modifier = Modifier.weight(1f))

        BottomNavBar(
            selected = NavTab.PROFILE,
            onSessionsClick = onHomeClick,
            onJournalClick = onJournalListClick
        )
    }

    if (showEditDialog) {
        NicknameEditDialog(
            initialValue = nickname,
            onDismiss = { showEditDialog = false },
            onConfirm = { newNickname ->
                onNicknameChange(newNickname)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun StatsGrid(
    streakDays: Int,
    sessionCount: Int,
    totalMeditatedMinutes: Int,
    mostCommonFeeling: Feeling?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            StatCell(value = "$streakDays", label = "dni z rzędu", modifier = Modifier.weight(1f))
            StatDivider()
            StatCell(value = "$sessionCount", label = "sesji łącznie", modifier = Modifier.weight(1f))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            StatCell(
                value = formatMinutes(totalMeditatedMinutes),
                label = "czasu medytacji",
                modifier = Modifier.weight(1f)
            )
            StatDivider()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                if (mostCommonFeeling != null) {
                    Icon(
                        imageVector = feelingIcon(mostCommonFeeling),
                        contentDescription = mostCommonFeeling.label,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "najczęstszy nastrój",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(14.dp)) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outline)
    )
}

@Composable
private fun NicknameEditDialog(
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = {
            Text(
                text = "Twój nick",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(999.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Twoje imię lub pseudonim",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = { value = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onConfirm(value.trim())
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Pojawi się w powitaniu na ekranie głównym. Może zostać puste.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value.trim()) }) {
                Text(
                    text = "Zapisz",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Anuluj",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    )
}

private fun formatMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    FlowWithNoirTheme {
        ProfileScreen(nickname = "Alicja", streakDays = 12, sessionCount = 34, totalMeditatedMinutes = 280)
    }
}
