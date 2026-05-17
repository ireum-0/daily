package com.ireum.daily.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ireum.daily.data.preferences.SchoolPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val notificationTime = SchoolPreferences(context.applicationContext)
                        .notificationTime
                        .first()
                    FavoriteMealNotificationScheduler.scheduleDaily(
                        context = context.applicationContext,
                        notificationTime = notificationTime
                    )
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
