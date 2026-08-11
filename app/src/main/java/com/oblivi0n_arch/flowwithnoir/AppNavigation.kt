package com.oblivi0n_arch.flowwithnoir

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
import com.oblivi0n_arch.flowwithnoir.data.SessionCatalog
import com.oblivi0n_arch.flowwithnoir.screens.HistoryScreen
import com.oblivi0n_arch.flowwithnoir.screens.HomeScreen
import com.oblivi0n_arch.flowwithnoir.screens.JournalEntryScreen
import com.oblivi0n_arch.flowwithnoir.screens.JournalListScreen
import com.oblivi0n_arch.flowwithnoir.screens.JournalMode
import com.oblivi0n_arch.flowwithnoir.screens.JournalScreen
import com.oblivi0n_arch.flowwithnoir.screens.ProfileScreen
import com.oblivi0n_arch.flowwithnoir.screens.SessionCatalogScreen
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
    const val SESSION = "session/{templateId}/{duration}"
    const val SESSION_CATALOG = "session_catalog"
    const val JOURNAL = "journal/{templateId}/{duration}"
    const val JOURNAL_ENTRY = "journal_entry"
    const val JOURNAL_LIST = "journal_list"
    const val JOURNAL_DETAIL = "journal_detail/{entryId}"
    const val JOURNAL_EDIT = "journal_edit/{entryId}"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"

    fun session(templateId: String, durationSeconds: Int) = "session/$templateId/$durationSeconds"

    fun journal(templateId: String, durationSeconds: Int) = "journal/$templateId/$durationSeconds"

    fun journalDetail(entryId: Long) = "journal_detail/$entryId"

    fun journalEdit(entryId: Long) = "journal_edit/$entryId"
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
                onSessionStart = { templateId, duration ->
                    navController.navigate(Routes.session(templateId, duration))
                },
                onAllSessionsClick = {
                    navController.navigate(Routes.SESSION_CATALOG)
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

        composable(Routes.SESSION_CATALOG) {
            SessionCatalogScreen(
                onBack = { navController.popBackStack() },
                onStart = { templateId, duration ->
                    navController.navigate(Routes.session(templateId, duration))
                }
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
                onHomeClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onProfileClick = { navController.navigate(Routes.PROFILE) },
                onEntryClick = { entryId -> navController.navigate(Routes.journalDetail(entryId)) },
                onNewEntryClick = { navController.navigate(Routes.JOURNAL_ENTRY) }
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
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
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
                navArgument("templateId") { type = NavType.StringType },
                navArgument("duration") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId").orEmpty()
            val duration = backStackEntry.arguments?.getInt("duration") ?: 0
            val template = SessionCatalog.byId(templateId)

            if (template == null) {
                LaunchedEffect(templateId) { navController.popBackStack() }
            } else {
                SessionScreen(
                    template = template,
                    durationSeconds = duration,
                    onClose = { elapsedSeconds ->
                        if (elapsedSeconds >= MIN_INTERRUPTED_SESSION_SECONDS) {
                            scope.launch {
                                journalRepository.logSession(
                                    sessionName = template.name,
                                    plannedDurationSeconds = duration,
                                    actualDurationSeconds = elapsedSeconds,
                                    isCompleted = false
                                )
                            }
                        }
                        navController.popBackStack()
                    },
                    onFinished = {
                        navController.navigate(Routes.journal(templateId, duration)) {
                            popUpTo(Routes.HOME)
                        }
                    }
                )
            }
        }

        composable(
            route = Routes.JOURNAL,
            arguments = listOf(
                navArgument("templateId") { type = NavType.StringType },
                navArgument("duration") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId").orEmpty()
            val duration = backStackEntry.arguments?.getInt("duration") ?: 0
            val name = SessionCatalog.byId(templateId)?.name ?: "Sesja"

            JournalScreen(
                mode = JournalMode.AFTER_SESSION,
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
                onSave = { feeling, note, tags ->
                    scope.launch {
                        journalRepository.logSession(
                            sessionName = name,
                            plannedDurationSeconds = duration,
                            actualDurationSeconds = duration,
                            isCompleted = true,
                            feeling = feeling,
                            note = note,
                            tags = tags
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
                mode = JournalMode.NEW,
                onSkip = {
                    navController.popBackStack()
                },
                onSave = { feeling, note, tags ->
                    scope.launch {
                        journalRepository.logSession(
                            sessionName = "Wpis dziennika",
                            plannedDurationSeconds = 0,
                            actualDurationSeconds = 0,
                            isCompleted = true,
                            feeling = feeling,
                            note = note,
                            tags = tags
                        )
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(
            route = Routes.JOURNAL_DETAIL,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
            val entry by journalRepository.getEntry(entryId).collectAsState(initial = null)

            JournalEntryScreen(
                entry = entry,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.journalEdit(entryId)) },
                onDelete = {
                    entry?.let { current ->
                        scope.launch {
                            journalRepository.deleteEntry(current)
                            navController.popBackStack()
                        }
                    }
                }
            )
        }

        composable(
            route = Routes.JOURNAL_EDIT,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
            val entry by journalRepository.getEntry(entryId).collectAsState(initial = null)

            entry?.let { current ->
                JournalScreen(
                    mode = JournalMode.EDIT,
                    initialFeeling = current.feeling,
                    initialNote = current.note,
                    initialTags = current.tags,
                    onSkip = { navController.popBackStack() },
                    onSave = { feeling, note, tags ->
                        scope.launch {
                            journalRepository.updateEntry(current, feeling, note, tags)
                            navController.popBackStack()
                        }
                    }
                )
            }
        }
    }
}

private const val MIN_INTERRUPTED_SESSION_SECONDS = 30