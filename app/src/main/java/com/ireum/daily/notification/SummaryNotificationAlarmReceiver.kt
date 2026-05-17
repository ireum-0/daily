package com.ireum.daily.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.ireum.daily.MainActivity
import com.ireum.daily.R
import com.ireum.daily.core.util.TaskDateCategory
import com.ireum.daily.core.util.classifyTaskDueDate
import com.ireum.daily.core.util.findMatchingFavoriteMenus
import com.ireum.daily.data.local.DailyDatabase
import com.ireum.daily.data.local.MealEntity
import com.ireum.daily.data.local.TaskEntity
import com.ireum.daily.data.preferences.SchoolPreferences
import com.ireum.daily.model.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SummaryNotificationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sendSummaryNotification(
                    context = context.applicationContext,
                    type = intent.summaryType()
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun sendSummaryNotification(
        context: Context,
        type: SummaryNotificationType
    ) {
        if (!canPostNotifications(context)) return

        val preferences = SchoolPreferences(context)
        val school = preferences.schoolConfig.first()
        val favoriteMenus = preferences.favoriteMenus.first()
        val today = LocalDate.now()
        val targetMealDate = when (type) {
            SummaryNotificationType.MORNING -> today
            SummaryNotificationType.EVENING -> today.plusDays(1)
        }

        val database = Room.databaseBuilder(
            context,
            DailyDatabase::class.java,
            "daily.db"
        )
            .addMigrations(DailyDatabase.MIGRATION_1_2)
            .build()

        val summaryData = try {
            val tasks = database.taskDao().getTasks()
            val meals = if (school.hasRequiredCodes) {
                database.mealDao().getMealsForDate(
                    educationOfficeCode = school.educationOfficeCode,
                    schoolCode = school.schoolCode,
                    mealDate = targetMealDate.format(DateTimeFormatter.BASIC_ISO_DATE)
                )
            } else {
                emptyList()
            }
            SummaryData(tasks = tasks, meals = meals)
        } finally {
            database.close()
        }

        val body = buildNotificationBody(
            type = type,
            today = today,
            tasks = summaryData.tasks,
            meals = summaryData.meals,
            favoriteMenus = favoriteMenus
        )
        if (body.isBlank()) return

        createNotificationChannel(context)
        NotificationManagerCompat.from(context).notify(
            when (type) {
                SummaryNotificationType.MORNING -> MORNING_NOTIFICATION_ID
                SummaryNotificationType.EVENING -> EVENING_NOTIFICATION_ID
            },
            NotificationCompat.Builder(context, SUMMARY_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(
                    when (type) {
                        SummaryNotificationType.MORNING -> "오늘 요약"
                        SummaryNotificationType.EVENING -> "내일 준비 요약"
                    }
                )
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(appLaunchIntent(context))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        )
    }

    private fun buildNotificationBody(
        type: SummaryNotificationType,
        today: LocalDate,
        tasks: List<TaskEntity>,
        meals: List<MealEntity>,
        favoriteMenus: Set<String>
    ): String {
        val activeTasks = tasks.filterNot { task -> task.status == TaskStatus.DONE }
        val lines = when (type) {
            SummaryNotificationType.MORNING -> morningLines(today, activeTasks)
            SummaryNotificationType.EVENING -> eveningLines(today, activeTasks)
        }.toMutableList()

        val favoriteMealLine = favoriteMealLine(
            label = when (type) {
                SummaryNotificationType.MORNING -> "오늘"
                SummaryNotificationType.EVENING -> "내일"
            },
            meals = meals,
            favoriteMenus = favoriteMenus
        )
        if (favoriteMealLine != null) {
            lines += favoriteMealLine
        }

        return lines.joinToString("\n")
    }

    private fun morningLines(
        today: LocalDate,
        activeTasks: List<TaskEntity>
    ): List<String> {
        val todayCount = activeTasks.count { task ->
            classifyTaskDueDate(task.dueDate, today) == TaskDateCategory.TODAY
        }
        val overdueCount = activeTasks.count { task ->
            classifyTaskDueDate(task.dueDate, today) == TaskDateCategory.OVERDUE
        }
        return listOfNotNull(
            "오늘까지인 과제 ${todayCount}개",
            if (overdueCount > 0) "지난 과제 ${overdueCount}개" else null
        )
    }

    private fun eveningLines(
        today: LocalDate,
        activeTasks: List<TaskEntity>
    ): List<String> {
        val tomorrowCount = activeTasks.count { task ->
            classifyTaskDueDate(task.dueDate, today) == TaskDateCategory.TOMORROW
        }
        val weekCount = activeTasks.count { task ->
            classifyTaskDueDate(task.dueDate, today) in setOf(
                TaskDateCategory.TOMORROW,
                TaskDateCategory.THIS_WEEK
            )
        }
        return listOf(
            "내일까지인 과제 ${tomorrowCount}개",
            "이번 주 남은 과제 ${weekCount}개"
        )
    }

    private fun favoriteMealLine(
        label: String,
        meals: List<MealEntity>,
        favoriteMenus: Set<String>
    ): String? {
        if (favoriteMenus.isEmpty()) return null
        val matches = meals.mapNotNull { meal ->
            val matchedMenus = findMatchingFavoriteMenus(
                dishes = meal.dishes.lines(),
                favoriteMenus = favoriteMenus
            )
            if (matchedMenus.isEmpty()) {
                null
            } else {
                "${meal.mealName}: ${matchedMenus.joinToString(", ")}"
            }
        }
        if (matches.isEmpty()) return null
        return "$label 선호 메뉴 ${matches.joinToString(" / ")}"
    }

    private fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            SUMMARY_CHANNEL_ID,
            "Daily 요약",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "오늘과 내일의 과제와 급식 요약을 알려줍니다."
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun appLaunchIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun Intent.summaryType(): SummaryNotificationType =
        getStringExtra(EXTRA_TYPE)
            ?.let { value -> runCatching { SummaryNotificationType.valueOf(value) }.getOrNull() }
            ?: SummaryNotificationType.MORNING

    private data class SummaryData(
        val tasks: List<TaskEntity>,
        val meals: List<MealEntity>
    )

    companion object {
        const val EXTRA_TYPE = "summary_notification_type"
        private const val SUMMARY_CHANNEL_ID = "daily_summary_alerts"
        private const val MORNING_NOTIFICATION_ID = 2201
        private const val EVENING_NOTIFICATION_ID = 2202
    }
}
