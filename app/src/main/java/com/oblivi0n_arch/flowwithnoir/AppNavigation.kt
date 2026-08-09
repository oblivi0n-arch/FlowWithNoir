package com.oblivi0n_arch.flowwithnoir

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.oblivi0n_arch.flowwithnoir.screens.HomeScreen
import com.oblivi0n_arch.flowwithnoir.screens.SplashScreen
import com.oblivi0n_arch.flowwithnoir.screens.WelcomeScreen
import kotlinx.coroutines.launch

object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val HOME = "home"
}

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    val context = LocalContext.current
    val preferences = remember { AppPreferences(context) }
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
            HomeScreen()
        }
    }
}