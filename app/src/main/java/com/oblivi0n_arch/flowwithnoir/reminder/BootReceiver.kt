package com.oblivi0n_arch.flowwithnoir.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.oblivi0n_arch.flowwithnoir.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = AppPreferences(appContext).reminderSettings()
                if (settings.isEnabled) {
                    ReminderScheduler.schedule(appContext, settings.hour, settings.minute)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
