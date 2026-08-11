package com.oblivi0n_arch.flowwithnoir.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.oblivi0n_arch.flowwithnoir.AppPreferences
import com.oblivi0n_arch.flowwithnoir.MainActivity
import com.oblivi0n_arch.flowwithnoir.R
import com.oblivi0n_arch.flowwithnoir.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = AppPreferences(appContext).reminderSettings()
                if (!settings.isEnabled) return@launch
                if (settings.skipAfterSession && hasSessionToday(appContext)) return@launch
                showNotification(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun hasSessionToday(context: Context): Boolean {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        return AppDatabase.getInstance(context).journalDao()
            .getCompletedTimestamps()
            .any { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() == today }
    }

    private fun showNotification(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val systemManager = context.getSystemService(NotificationManager::class.java)
        if (systemManager?.getNotificationChannel(CHANNEL_ID) == null) {
            systemManager?.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Przypomnienia",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Codzienne przypomnienie o sesji"
                }
            )
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Chwila dla siebie")
            .setContentText("Kilka minut oddechu wystarczy.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        runCatching { manager.notify(NOTIFICATION_ID, notification) }
    }

    companion object {
        const val CHANNEL_ID = "daily_reminder"
        private const val NOTIFICATION_ID = 4202
    }
}
