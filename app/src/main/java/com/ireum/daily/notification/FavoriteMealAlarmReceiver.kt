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
import com.ireum.daily.core.util.findMatchingFavoriteMenus
import com.ireum.daily.core.util.subjectParticle
import com.ireum.daily.data.local.DailyDatabase
import com.ireum.daily.data.local.MealEntity
import com.ireum.daily.data.preferences.SchoolPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FavoriteMealAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sendNotificationIfNeeded(context.applicationContext)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun sendNotificationIfNeeded(context: Context) {
        if (!canPostNotifications(context)) return

        val preferences = SchoolPreferences(context)
        val school = preferences.schoolConfig.first()
        if (!school.hasRequiredCodes) return

        val favoriteMenus = preferences.favoriteMenus.first()
        if (favoriteMenus.isEmpty()) return

        val tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE)
        val database = Room.databaseBuilder(
            context,
            DailyDatabase::class.java,
            "daily.db"
        )
            .addMigrations(DailyDatabase.MIGRATION_1_2)
            .build()

        val breakfastMeals = try {
            database.mealDao()
                .getMealsForDate(
                    educationOfficeCode = school.educationOfficeCode,
                    schoolCode = school.schoolCode,
                    mealDate = tomorrow
                )
                .filter { meal -> meal.isBreakfast() }
        } finally {
            database.close()
        }

        val matchedMenus = findMatchingFavoriteMenus(
            dishes = breakfastMeals.flatMap { meal -> meal.dishes.lines() },
            favoriteMenus = favoriteMenus
        )
        if (matchedMenus.isEmpty()) return

        createNotificationChannel(context)
        NotificationManagerCompat.from(context).notify(
            FAVORITE_MEAL_NOTIFICATION_ID,
            NotificationCompat.Builder(context, FAVORITE_MEAL_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("선호 메뉴 알림")
                .setContentText(matchedMenus.toNotificationText())
                .setStyle(NotificationCompat.BigTextStyle().bigText(matchedMenus.toNotificationText()))
                .setContentIntent(appLaunchIntent(context))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        )
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
            FAVORITE_MEAL_CHANNEL_ID,
            "선호 메뉴 알림",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "다음날 조식에 선호 메뉴가 포함되면 알려줍니다."
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

    private fun List<String>.toNotificationText(): String {
        val menuText = joinToString(", ")
        return "다음날 조식에 $menuText${menuText.subjectParticle()} 포함돼 있어요."
    }

    private fun MealEntity.isBreakfast(): Boolean =
        mealCode == "1" || mealName.contains("조식")

    private companion object {
        const val FAVORITE_MEAL_CHANNEL_ID = "favorite_meal_alerts"
        const val FAVORITE_MEAL_NOTIFICATION_ID = 114001
    }
}
