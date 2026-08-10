package com.oblivi0n_arch.flowwithnoir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.oblivi0n_arch.flowwithnoir.ui.theme.FlowWithNoirTheme
import com.oblivi0n_arch.flowwithnoir.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val preferences = remember { AppPreferences(context) }
            val themeMode by preferences.themeModeFlow().collectAsState(initial = ThemeMode.SYSTEM)

            FlowWithNoirTheme(themeMode = themeMode) {
                AppNavigation()
            }
        }
    }
}