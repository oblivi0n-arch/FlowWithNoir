package com.oblivi0n_arch.flowwithnoir

import android.R.attr.type
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.oblivi0n_arch.flowwithnoir.data.AppDatabase
import com.oblivi0n_arch.flowwithnoir.data.JournalRepository
import com.oblivi0n_arch.flowwithnoir.data.ProfileStats
import com.oblivi0n_arch.flowwithnoir.screens.HistoryScreen
import com.oblivi0n_arch.flowwithnoir.screens.HomeScreen
import com.oblivi0n_arch.flowwithnoir.screens.JournalListScreen
import com.oblivi0n_arch.flowwithnoir.screens.JournalScreen
import com.oblivi0n_arch.flowwithnoir.screens.ProfileScreen
import com.oblivi0n_arch.flowwithnoir.screens.SessionScreen
import com.oblivi0n_arch.flowwithnoir.screens.SettingsScreen
import com.oblivi0n_arch.flowwithnoir.screens.SplashScreen
import com.oblivi0n_arch.flowwithnoir.screens.WelcomeScreen
import com.oblivi0n_arch.flowwithnoir.ui.theme.ThemeMode
import kotlinx.coroutines.launch

object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val HOME = "home"
    const val SESSION = "session/{name}/{duration}"
    const val JOURNAL = "journal/{name}/{duration}"
    const val JOURNAL_ENTRY = "journal_entry"
    const val JOURNAL_LIST = "journal_list"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    val context = LocalContext.current
    val preferences = remember { AppPreferences(context) }
    val journalRepository = remember { JournalRepository(AppDatabase.getInstance(context).journalDao()) }
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = {
                    scope.launch {
                        val firstLaunch = preferences.isFirstLaunch()
                        val destination = if (firstLaunch) Routes.WELCOME else Routes.HOME
                        navController.navigate(destination) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.WELCOME) {
            WelcomeScreen(
                onStartClick = {
                    scope.launch {
                        preferences.setFirstLaunchDone()
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            var streakDays by remember { mutableIntStateOf(0) }
            val latestEntry by journalRepository.getLatestEntry()
                .collectAsState(initial = null)
            val nickname by preferences.nicknameFlow().collectAsState(initial = null)

            LaunchedEffect(latestEntry) {
                streakDays = journalRepository.getCurrentStreakDays()
            }

            val lastEntrySummary = latestEntry?.let {
                "Ostatni wpis: ${SimpleDateFormat("d MMM, HH:mm", Locale("pl")).format(Date(it.timestampMillis))}"
            } ?: "Brak wpisów"

            HomeScreen(
                onSessionClick = { name, duration ->
                    navController.navigate("session/$name/$duration")
                },
                onHistoryClick = {
                    navController.navigate(Routes.HISTORY)
                },
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
                },
                onJournalClick = {
                    navController.navigate(Routes.JOURNAL_ENTRY)
                },
                onJournalListClick = {
                    navController.navigate(Routes.JOURNAL_LIST)
                },
                streakDays = streakDays,
                lastEntrySummary = lastEntrySummary,
                nickname = nickname ?: ""
            )
        }

        composable(Routes.HISTORY) {
            val entries by journalRepository.getEntries().collectAsState(initial = emptyList())
            HistoryScreen(entries = entries)
        }

        composable(Routes.JOURNAL_LIST) {
            val entries by journalRepository.getEntries().collectAsState(initial = emptyList())
            JournalListScreen(
                entries = entries,
                onHomeClick = { navController.popBackStack() },
                onProfileClick = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(Routes.PROFILE) {
            var streakDays by remember { mutableIntStateOf(0) }
            var stats by remember { mutableStateOf(ProfileStats(0, 0, null)) }
            val nickname by preferences.nicknameFlow().collectAsState(initial = null)

            LaunchedEffect(Unit) {
                streakDays = journalRepository.getCurrentStreakDays()
                stats = journalRepository.getProfileStats()
            }

            ProfileScreen(
                nickname = nickname ?: "",
                streakDays = streakDays,
                sessionCount = stats.sessionCount,
                totalMeditatedMinutes = stats.totalMeditatedMinutes,
                mostCommonFeeling = stats.mostCommonFeeling,
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNicknameChange = { newNickname ->
                    scope.launch {
                        preferences.setNickname(newNickname)
                    }
                },
                onHomeClick = {
                    navController.popBackStack()
                },
                onJournalListClick = {
                    navController.navigate(Routes.JOURNAL_LIST)
                }
            )
        }

        composable(Routes.SETTINGS) {
            val themeMode by preferences.themeModeFlow().collectAsState(initial = ThemeMode.SYSTEM)

            SettingsScreen(
                themeMode = themeMode,
                onThemeModeChange = { newThemeMode ->
                    scope.launch {
                        preferences.setThemeMode(newThemeMode)
                    }
                }
            )
        }

        composable(
            route = Routes.SESSION,
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("duration") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val duration = backStackEntry.arguments?.getInt("duration") ?: 0

            SessionScreen(
                sessionName = name,
                durationSeconds = duration,
                onClose = { elapsedSeconds ->
                    if (elapsedSeconds >= MIN_INTERRUPTED_SESSION_SECONDS) {
                        scope.launch {
                            journalRepository.logSession(
                                sessionName = name,
                                plannedDurationSeconds = duration,
                                actualDurationSeconds = elapsedSeconds,
                                isCompleted = false
                            )
                        }
                    }
                    navController.popBackStack()
                },
                onFinished = {
                    navController.navigate("journal/$name/$duration") {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(
            route = Routes.JOURNAL,
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("duration") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val duration = backStackEntry.arguments?.getInt("duration") ?: 0

            JournalScreen(
                onSkip = {
                    scope.launch {
                        journalRepository.logSession(
                            sessionName = name,
                            plannedDurationSeconds = duration,
                            actualDurationSeconds = duration,
                            isCompleted = true
                        )
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                },
                onSave = { feeling, note ->
                    scope.launch {
                        journalRepository.logSession(
                            sessionName = name,
                            plannedDurationSeconds = duration,
                            actualDurationSeconds = duration,
                            isCompleted = true,
                            feeling = feeling,
                            note = note
                        )
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.JOURNAL_ENTRY) {
            JournalScreen(
                isStandalone = true,
                onSkip = {
                    navController.popBackStack()
                },
                onSave = { feeling, note ->
                    scope.launch {
                        journalRepository.logSession(
                            sessionName = "Wpis dziennika",
                            plannedDurationSeconds = 0,
                            actualDurationSeconds = 0,
                            isCompleted = true,
                            feeling = feeling,
                            note = note
                        )
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}

private const val MIN_INTERRUPTED_SESSION_SECONDS = 30