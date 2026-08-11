package com.oblivi0n_arch.flowwithnoir.screens

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.oblivi0n_arch.flowwithnoir.BuildConfig
import com.oblivi0n_arch.flowwithnoir.ReminderSettings
import com.oblivi0n_arch.flowwithnoir.reminder.ReminderReceiver
import com.oblivi0n_arch.flowwithnoir.reminder.ReminderScheduler
import com.oblivi0n_arch.flowwithnoir.ui.theme.FlowWithNoirTheme
import com.oblivi0n_arch.flowwithnoir.ui.theme.ThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val RESET_CONFIRMATION_WORD = "RESET"

@Composable
fun SettingsScreen(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    reminder: ReminderSettings = ReminderSettings(),
    onBack: () -> Unit = {},
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onReminderEnabledChange: (Boolean) -> Unit = {},
    onReminderTimeChange: (hour: Int, minute: Int) -> Unit = { _, _ -> },
    onSkipAfterSessionChange: (Boolean) -> Unit = {},
    onClearJournal: () -> Unit = {},
    onFullReset: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var notificationsAllowed by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    var showTimePicker by remember { mutableStateOf(false) }
    var showClearJournalDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    DisposableEffect(activity) {
        val lifecycle = activity?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsAllowed =
                    NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (isGranted) {
            onReminderEnabledChange(true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .screenInsets()
            .padding(horizontal = screenPaddingHorizontal)
            .padding(top = screenPaddingTop, bottom = screenPaddingBottom)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Wstecz",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onBack)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Ustawienia",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SectionLabel("PRZYPOMNIENIA", topPadding = 26.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            ) {
                SettingsToggleRow(
                    title = "Codzienne",
                    subtitle = if (reminder.isEnabled) {
                        "Codziennie około ${reminder.timeLabel}"
                    } else {
                        "Wyłączone"
                    },
                    isChecked = reminder.isEnabled,
                    onCheckedChange = { wantsEnabled ->
                        if (!wantsEnabled) {
                            onReminderEnabledChange(false)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !notificationsAllowed
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onReminderEnabledChange(true)
                        }
                    }
                )

                AnimatedVisibility(
                    visible = reminder.isEnabled,
                    enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                    exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
                ) {
                    Column {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTimePicker = true }
                                .padding(vertical = 14.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Godzina",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = reminder.timeLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(999.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        SettingsToggleRow(
                            title = "Pomiń po sesji",
                            subtitle = "Cisza, jeśli już dziś medytowałaś",
                            isChecked = reminder.skipAfterSession,
                            onCheckedChange = onSkipAfterSessionChange
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = reminder.isEnabled && !notificationsAllowed,
                enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                PermissionNotice(
                    onOpenSettings = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    }
                )
            }

            SectionLabel("WYGLĄD", topPadding = 26.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            ) {
                ThemeModeOption(
                    label = "Systemowy",
                    isSelected = themeMode == ThemeMode.SYSTEM,
                    onClick = { onThemeModeChange(ThemeMode.SYSTEM) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                ThemeModeOption(
                    label = "Jasny",
                    isSelected = themeMode == ThemeMode.LIGHT,
                    onClick = { onThemeModeChange(ThemeMode.LIGHT) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                ThemeModeOption(
                    label = "Ciemny",
                    isSelected = themeMode == ThemeMode.DARK,
                    onClick = { onThemeModeChange(ThemeMode.DARK) }
                )
            }

            SectionLabel("DANE", topPadding = 26.dp)

            Text(
                text = "Wyczyść dziennik",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showClearJournalDialog = true }
                    .padding(vertical = 12.dp)
            )
            Text(
                text = "Usuwa wpisy i historię sesji. Nick, motyw i przypomnienia zostają.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Reset aplikacji",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showResetDialog = true }
                    .padding(vertical = 12.dp)
            )
            Text(
                text = "Czyści wszystko i wraca do stanu pierwszego uruchomienia.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            if (BuildConfig.DEBUG) {
                SectionLabel("DEBUG", topPadding = 26.dp)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                context.sendBroadcast(
                                    Intent(context, ReminderReceiver::class.java)
                                )
                            }
                            .padding(vertical = 14.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Wyślij przypomnienie teraz",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Cała ścieżka odbiornika, łącznie z pomijaniem po sesji",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(
                            text = "→",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Column(modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp)) {
                        Text(
                            text = "Najbliższy alarm",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (reminder.isEnabled) {
                                formatTrigger(
                                    ReminderScheduler.nextTriggerMillis(
                                        reminder.hour,
                                        reminder.minute
                                    )
                                )
                            } else {
                                "Przypomnienie wyłączone"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Built by @oblivi0n-arch",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = reminder.hour,
            initialMinute = reminder.minute,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                showTimePicker = false
                onReminderTimeChange(hour, minute)
            }
        )
    }

    if (showClearJournalDialog) {
        AlertDialog(
            onDismissRequest = { showClearJournalDialog = false },
            containerColor = MaterialTheme.colorScheme.background,
            title = { Text("Wyczyścić dziennik?") },
            text = { Text("Usuniesz wszystkie wpisy i historię sesji. Tej operacji nie da się cofnąć.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearJournalDialog = false
                    onClearJournal()
                }) {
                    Text("Wyczyść")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearJournalDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    if (showResetDialog) {
        ResetDialog(
            onDismiss = { showResetDialog = false },
            onConfirm = {
                showResetDialog = false
                onFullReset()
            }
        )
    }
}

@Composable
private fun ResetDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    var typed by remember { mutableStateOf("") }
    val isConfirmed = typed.trim().equals(RESET_CONFIRMATION_WORD, ignoreCase = false)

    val confirmColor by animateColorAsState(
        targetValue = if (isConfirmed) MaterialTheme.colorScheme.onBackground
        else MaterialTheme.colorScheme.outline,
        animationSpec = tween(200),
        label = "resetConfirmColor"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = { Text("Zresetować aplikację?") },
        text = {
            Column {
                Text(
                    text = "Usuniesz wszystkie wpisy i sesje, nick, motyw i przypomnienia. " +
                        "Aplikacja wróci do stanu pierwszego uruchomienia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    if (typed.isEmpty()) {
                        Text(
                            text = "Wpisz $RESET_CONFIRMATION_WORD, żeby potwierdzić",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    BasicTextField(
                        value = typed,
                        onValueChange = { typed = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = isConfirmed) {
                Text(text = "Resetuj", color = confirmColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = { Text("O której przypomnieć?") },
        text = {
            TimeInput(
                state = state,
                colors = TimePickerDefaults.colors(
                    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.onBackground,
                    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.background,
                    timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.background,
                    timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
private fun PermissionNotice(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.NotificationsOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(
                text = "Powiadomienia są zablokowane",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Otwórz ustawienia systemu",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable(onClick = onOpenSettings)
            )
        }
    }
}

private fun formatTrigger(triggerMillis: Long): String {
    val time = SimpleDateFormat("EEEE, HH:mm", Locale("pl")).format(Date(triggerMillis))
    val minutesAway = (triggerMillis - System.currentTimeMillis()) / 60_000L
    return "$time · za $minutesAway min"
}

private fun Context.findActivity(): ComponentActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is ComponentActivity) return current
        current = current.baseContext
    }
    return null
}

@Composable
private fun SectionLabel(text: String, topPadding: Dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(top = topPadding, bottom = 10.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 14.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        NoirSwitch(isChecked = isChecked)
    }
}

@Composable
private fun NoirSwitch(isChecked: Boolean) {
    val trackColor by animateColorAsState(
        targetValue = if (isChecked) MaterialTheme.colorScheme.onBackground else Color.Transparent,
        animationSpec = tween(200),
        label = "switchTrack"
    )
    val knobColor by animateColorAsState(
        targetValue = if (isChecked) MaterialTheme.colorScheme.background
        else MaterialTheme.colorScheme.outline,
        animationSpec = tween(200),
        label = "switchKnob"
    )
    val knobOffset by animateDpAsState(
        targetValue = if (isChecked) 16.dp else 0.dp,
        animationSpec = tween(200),
        label = "switchOffset"
    )

    Box(
        modifier = Modifier
            .width(38.dp)
            .height(22.dp)
            .background(trackColor, CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobOffset)
                .size(14.dp)
                .background(knobColor, CircleShape)
        )
    }
}

@Composable
private fun ThemeModeOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (isSelected) MaterialTheme.colorScheme.onBackground
                    else Color.Transparent,
                    shape = CircleShape
                )
                .border(1.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    FlowWithNoirTheme {
        SettingsScreen(themeMode = ThemeMode.SYSTEM)
    }
}
