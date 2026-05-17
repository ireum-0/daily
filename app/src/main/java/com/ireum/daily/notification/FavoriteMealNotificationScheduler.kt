package com.ireum.daily.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ireum.daily.data.preferences.NotificationTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object FavoriteMealNotificationScheduler {
    private const val REQUEST_CODE = 1140

    fun scheduleDaily(context: Context, notificationTime: NotificationTime) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(pendingIntent(context))
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextTriggerMillis(notificationTime),
            AlarmManager.INTERVAL_DAY,
            pendingIntent(context)
        )
    }

    private fun pendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, FavoriteMealAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun nextTriggerMillis(notificationTime: NotificationTime): Long {
        val zoneId = ZoneId.systemDefault()
        val now = LocalDateTime.now(zoneId)
        val todayTrigger = now
            .truncatedTo(ChronoUnit.DAYS)
            .withHour(notificationTime.hour)
            .withMinute(notificationTime.minute)

        val trigger = if (now.isBefore(todayTrigger)) {
            todayTrigger
        } else {
            todayTrigger.plusDays(1)
        }

        return trigger.atZone(zoneId).toInstant().toEpochMilli()
    }
}
