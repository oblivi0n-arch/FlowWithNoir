package com.oblivi0n_arch.flowwithnoir

import android.R.attr.type
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.oblivi0n_arch.flowwithnoir.screens.HistoryScreen
import com.oblivi0n_arch.flowwithnoir.screens.HomeScreen
import com.oblivi0n_arch.flowwithnoir.screens.JournalScreen
import com.oblivi0n_arch.flowwithnoir.screens.ProfileScreen
import com.oblivi0n_arch.flowwithnoir.screens.SessionScreen
import com.oblivi0n_arch.flowwithnoir.screens.SplashScreen
import com.oblivi0n_arch.flowwithnoir.screens.WelcomeScreen
import kotlinx.coroutines.launch

object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val HOME = "home"
    const val SESSION = "session/{name}/{duration}"
    const val JOURNAL = "journal/{name}/{duration}"
    const val HISTORY = "history"
    const val PROFILE = "profile"
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
                streakDays = streakDays,
                lastEntrySummary = lastEntrySummary
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen()
        }

        composable(Routes.PROFILE) { ProfileScreen() }

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
                onClose = { navController.popBackStack() },
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
                sessionName = name,
                onSkip = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onSave = { feeling, note ->
                    scope.launch {
                        journalRepository.addEntry(
                            sessionName = name,
                            durationSeconds = duration,
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
    }
}