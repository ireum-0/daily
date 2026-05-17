package com.ireum.daily.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ireum.daily.data.preferences.NotificationTime
import com.ireum.daily.data.preferences.SummaryNotificationSettings
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object SummaryNotificationScheduler {
    private const val MORNING_REQUEST_CODE = 2101
    private const val EVENING_REQUEST_CODE = 2102

    fun schedule(context: Context, settings: SummaryNotificationSettings) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        scheduleOrCancel(
            alarmManager = alarmManager,
            context = context,
            type = SummaryNotificationType.MORNING,
            enabled = settings.morningEnabled,
            notificationTime = settings.morningTime
        )
        scheduleOrCancel(
            alarmManager = alarmManager,
            context = context,
            type = SummaryNotificationType.EVENING,
            enabled = settings.eveningEnabled,
            notificationTime = settings.eveningTime
        )
    }

    private fun scheduleOrCancel(
        alarmManager: AlarmManager,
        context: Context,
        type: SummaryNotificationType,
        enabled: Boolean,
        notificationTime: NotificationTime
    ) {
        val pendingIntent = pendingIntent(context, type)
        alarmManager.cancel(pendingIntent)
        if (!enabled) return

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextTriggerMillis(notificationTime),
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun pendingIntent(
        context: Context,
        type: SummaryNotificationType
    ): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            when (type) {
                SummaryNotificationType.MORNING -> MORNING_REQUEST_CODE
                SummaryNotificationType.EVENING -> EVENING_REQUEST_CODE
            },
            Intent(context, SummaryNotificationAlarmReceiver::class.java)
                .putExtra(SummaryNotificationAlarmReceiver.EXTRA_TYPE, type.name),
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

enum class SummaryNotificationType {
    MORNING,
    EVENING
}
