package com.oblivi0n_arch.flowwithnoir.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.oblivi0n_arch.flowwithnoir.data.SessionCatalog
import com.oblivi0n_arch.flowwithnoir.ui.theme.FlowWithNoirTheme
import kotlinx.coroutines.launch

enum class PracticeTime(
    val label: String,
    val description: String,
    val hour: Int,
    val minute: Int
) {
    MORNING("Rano", "Zanim dzień się rozkręci", 7, 30),
    EVENING("Wieczorem", "Żeby wyhamować przed snem", 21, 0),
    ANYTIME("Kiedy się da", "Bez sztywnej pory", 20, 0)
}

data class OnboardingResult(
    val nickname: String,
    val isReminderEnabled: Boolean,
    val hour: Int,
    val minute: Int
)

private const val PAGE_COUNT = 4

@Composable
fun OnboardingScreen(onFinish: (OnboardingResult) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })

    var nickname by remember { mutableStateOf("") }
    var practiceTime by remember { mutableStateOf<PracticeTime?>(null) }
    var isReminderEnabled by remember { mutableStateOf(false) }
    var notificationsAllowed by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
        isReminderEnabled = isGranted
    }

    val selectedTime = practiceTime ?: PracticeTime.ANYTIME

    fun finish() {
        onFinish(
            OnboardingResult(
                nickname = nickname.trim(),
                isReminderEnabled = isReminderEnabled && notificationsAllowed,
                hour = selectedTime.hour,
                minute = selectedTime.minute
            )
        )
    }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .screenInsets()
            .imePadding()
            .padding(horizontal = 30.dp)
            .padding(top = 20.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PageIndicator(currentPage = pagerState.currentPage)

            AnimatedVisibility(
                visible = pagerState.currentPage > 0,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {
                Text(
                    text = "Pomiń",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clickable { finish() }
                        .padding(4.dp)
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> IntroPage()
                1 -> NicknamePage(nickname = nickname, onNicknameChange = { nickname = it })
                2 -> PracticeTimePage(
                    selected = practiceTime,
                    onSelect = { practiceTime = it }
                )
                else -> ReminderPage(
                    practiceTime = selectedTime,
                    isReminderEnabled = isReminderEnabled && notificationsAllowed,
                    notificationsAllowed = notificationsAllowed,
                    onToggle = { wantsEnabled ->
                        if (!wantsEnabled) {
                            isReminderEnabled = false
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !notificationsAllowed
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            isReminderEnabled = true
                        }
                    }
                )
            }
        }

        Button(
            onClick = {
                if (pagerState.currentPage == PAGE_COUNT - 1) {
                    finish()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background
            )
        ) {
            Text(if (pagerState.currentPage == PAGE_COUNT - 1) "Zaczynajmy" else "Dalej")
        }
    }
}

@Composable
private fun PageIndicator(currentPage: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(PAGE_COUNT) { index ->
            val width by animateDpAsState(
                targetValue = if (index == currentPage) 20.dp else 6.dp,
                animationSpec = tween(250),
                label = "indicatorWidth"
            )
            Box(
                modifier = Modifier
                    .width(width)
                    .height(3.dp)
                    .background(
                        color = if (index <= currentPage) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(999.dp)
                    )
            )
        }
    }
}

@Composable
private fun PageScaffold(
    eyebrow: String,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = eyebrow,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(32.dp))
        content()
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun IntroPage() {
    val pattern = remember { SessionCatalog.suggestedFor().pattern }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BreathingRing(pattern = pattern, sizeDp = 150.dp)
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Zwolnij.\nZacznij tutaj.",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Trzy minuty ciszy potrafią zmienić resztę dnia. Zacznijmy razem.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun NicknamePage(nickname: String, onNicknameChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current

    PageScaffold(
        eyebrow = "KROK 1",
        title = "Jak mam się\ndo ciebie zwracać?",
        subtitle = "Użyję tego tylko w powitaniu na ekranie głównym. Możesz zostawić puste."
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(999.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            if (nickname.isEmpty()) {
                Text(
                    text = "Twoje imię lub pseudonim",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            BasicTextField(
                value = nickname,
                onValueChange = onNicknameChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PracticeTimePage(selected: PracticeTime?, onSelect: (PracticeTime) -> Unit) {
    PageScaffold(
        eyebrow = "KROK 2",
        title = "Kiedy chcesz\npraktykować?",
        subtitle = "Ustawię na tej podstawie godzinę przypomnienia. Zmienisz ją później w ustawieniach."
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
        ) {
            PracticeTime.entries.forEachIndexed { index, option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(option) }
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = option.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (selected == option) {
                                    MaterialTheme.colorScheme.onBackground
                                } else {
                                    androidx.compose.ui.graphics.Color.Transparent
                                },
                                shape = CircleShape
                            )
                            .border(1.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                    )
                }
                if (index < PracticeTime.entries.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
private fun ReminderPage(
    practiceTime: PracticeTime,
    isReminderEnabled: Boolean,
    notificationsAllowed: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val timeLabel = "%02d:%02d".format(practiceTime.hour, practiceTime.minute)

    PageScaffold(
        eyebrow = "KROK 3",
        title = "Przypomnieć ci\no sesji?",
        subtitle = "Jedno powiadomienie dziennie. Jeśli tego dnia już medytowałaś, aplikacja milczy."
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                .clickable { onToggle(!isReminderEnabled) }
                .padding(vertical = 16.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Codziennie o $timeLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isReminderEnabled) "Włączone" else "Wyłączone",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            OnboardingSwitch(isChecked = isReminderEnabled)
        }

        AnimatedVisibility(
            visible = !notificationsAllowed && isReminderEnabled,
            enter = fadeIn(tween(200)) + expandVertically(tween(200)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
        ) {
            Text(
                text = "Powiadomienia są zablokowane w ustawieniach systemu. " +
                    "Możesz je włączyć później w ustawieniach aplikacji.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun OnboardingSwitch(isChecked: Boolean) {
    val knobOffset by animateDpAsState(
        targetValue = if (isChecked) 16.dp else 0.dp,
        animationSpec = tween(200),
        label = "onboardingSwitchOffset"
    )

    Box(
        modifier = Modifier
            .width(38.dp)
            .height(22.dp)
            .background(
                color = if (isChecked) MaterialTheme.colorScheme.onBackground
                else androidx.compose.ui.graphics.Color.Transparent,
                shape = CircleShape
            )
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobOffset)
                .size(14.dp)
                .background(
                    color = if (isChecked) MaterialTheme.colorScheme.background
                    else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    FlowWithNoirTheme {
        OnboardingScreen(onFinish = {})
    }
}
