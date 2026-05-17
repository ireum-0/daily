package com.ireum.daily.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ireum.daily.data.SchoolConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.schoolDataStore by preferencesDataStore(name = "school")

class SchoolPreferences(private val context: Context) {
    val schoolConfig: Flow<SchoolConfig> =
        context.schoolDataStore.data.map { preferences ->
            SchoolConfig(
                educationOfficeCode = preferences[Keys.educationOfficeCode].orEmpty(),
                schoolCode = preferences[Keys.schoolCode].orEmpty(),
                schoolName = preferences[Keys.schoolName].orEmpty()
            )
        }

    val neisApiKey: Flow<String> =
        context.schoolDataStore.data.map { preferences ->
            preferences[Keys.neisApiKey].orEmpty()
        }

    val favoriteMenus: Flow<Set<String>> =
        context.schoolDataStore.data.map { preferences ->
            preferences[Keys.favoriteMenus].orEmpty()
                .map(String::trim)
                .filter(String::isNotBlank)
                .toSet()
        }

    val notificationTime: Flow<NotificationTime> =
        context.schoolDataStore.data.map { preferences ->
            NotificationTime(
                hour = preferences[Keys.notificationHour] ?: DEFAULT_NOTIFICATION_HOUR,
                minute = preferences[Keys.notificationMinute] ?: DEFAULT_NOTIFICATION_MINUTE
            )
        }

    val summaryNotificationSettings: Flow<SummaryNotificationSettings> =
        context.schoolDataStore.data.map { preferences ->
            SummaryNotificationSettings(
                morningEnabled = preferences[Keys.morningSummaryEnabled] ?: DEFAULT_MORNING_ENABLED,
                morningTime = NotificationTime(
                    hour = preferences[Keys.morningSummaryHour] ?: DEFAULT_MORNING_HOUR,
                    minute = preferences[Keys.morningSummaryMinute] ?: DEFAULT_MORNING_MINUTE
                ),
                eveningEnabled = preferences[Keys.eveningSummaryEnabled] ?: DEFAULT_EVENING_ENABLED,
                eveningTime = NotificationTime(
                    hour = preferences[Keys.eveningSummaryHour] ?: DEFAULT_EVENING_HOUR,
                    minute = preferences[Keys.eveningSummaryMinute] ?: DEFAULT_EVENING_MINUTE
                )
            )
        }

    suspend fun saveSchool(schoolConfig: SchoolConfig) {
        context.schoolDataStore.edit { preferences ->
            preferences[Keys.educationOfficeCode] = schoolConfig.educationOfficeCode
            preferences[Keys.schoolCode] = schoolConfig.schoolCode
            preferences[Keys.schoolName] = schoolConfig.schoolName
        }
    }

    suspend fun saveNeisApiKey(apiKey: String) {
        context.schoolDataStore.edit { preferences ->
            preferences[Keys.neisApiKey] = apiKey.trim()
        }
    }

    suspend fun saveFavoriteMenus(favoriteMenus: Set<String>) {
        context.schoolDataStore.edit { preferences ->
            preferences[Keys.favoriteMenus] = favoriteMenus
                .map(String::trim)
                .filter(String::isNotBlank)
                .toSet()
        }
    }

    suspend fun saveNotificationTime(notificationTime: NotificationTime) {
        context.schoolDataStore.edit { preferences ->
            preferences[Keys.notificationHour] = notificationTime.hour.coerceIn(0, 23)
            preferences[Keys.notificationMinute] = notificationTime.minute.coerceIn(0, 59)
        }
    }

    suspend fun saveSummaryNotificationSettings(settings: SummaryNotificationSettings) {
        context.schoolDataStore.edit { preferences ->
            preferences[Keys.morningSummaryEnabled] = settings.morningEnabled
            preferences[Keys.morningSummaryHour] = settings.morningTime.hour.coerceIn(0, 23)
            preferences[Keys.morningSummaryMinute] = settings.morningTime.minute.coerceIn(0, 59)
            preferences[Keys.eveningSummaryEnabled] = settings.eveningEnabled
            preferences[Keys.eveningSummaryHour] = settings.eveningTime.hour.coerceIn(0, 23)
            preferences[Keys.eveningSummaryMinute] = settings.eveningTime.minute.coerceIn(0, 59)
        }
    }

    private object Keys {
        val educationOfficeCode = stringPreferencesKey("education_office_code")
        val schoolCode = stringPreferencesKey("school_code")
        val schoolName = stringPreferencesKey("school_name")
        val neisApiKey = stringPreferencesKey("neis_api_key")
        val favoriteMenus = stringSetPreferencesKey("favorite_menus")
        val notificationHour = intPreferencesKey("notification_hour")
        val notificationMinute = intPreferencesKey("notification_minute")
        val morningSummaryEnabled = booleanPreferencesKey("morning_summary_enabled")
        val morningSummaryHour = intPreferencesKey("morning_summary_hour")
        val morningSummaryMinute = intPreferencesKey("morning_summary_minute")
        val eveningSummaryEnabled = booleanPreferencesKey("evening_summary_enabled")
        val eveningSummaryHour = intPreferencesKey("evening_summary_hour")
        val eveningSummaryMinute = intPreferencesKey("evening_summary_minute")
    }

    private companion object {
        const val DEFAULT_NOTIFICATION_HOUR = 11
        const val DEFAULT_NOTIFICATION_MINUTE = 40
        const val DEFAULT_MORNING_ENABLED = false
        const val DEFAULT_MORNING_HOUR = 7
        const val DEFAULT_MORNING_MINUTE = 30
        const val DEFAULT_EVENING_ENABLED = false
        const val DEFAULT_EVENING_HOUR = 21
        const val DEFAULT_EVENING_MINUTE = 30
    }
}

data class NotificationTime(
    val hour: Int,
    val minute: Int
) {
    val displayText: String
        get() = "%02d:%02d".format(hour, minute)
}

data class SummaryNotificationSettings(
    val morningEnabled: Boolean,
    val morningTime: NotificationTime,
    val eveningEnabled: Boolean,
    val eveningTime: NotificationTime
)
