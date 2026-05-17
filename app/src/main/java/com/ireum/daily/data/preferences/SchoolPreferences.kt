package com.ireum.daily.data.preferences

import android.content.Context
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

    private object Keys {
        val educationOfficeCode = stringPreferencesKey("education_office_code")
        val schoolCode = stringPreferencesKey("school_code")
        val schoolName = stringPreferencesKey("school_name")
        val neisApiKey = stringPreferencesKey("neis_api_key")
        val favoriteMenus = stringSetPreferencesKey("favorite_menus")
        val notificationHour = intPreferencesKey("notification_hour")
        val notificationMinute = intPreferencesKey("notification_minute")
    }

    private companion object {
        const val DEFAULT_NOTIFICATION_HOUR = 11
        const val DEFAULT_NOTIFICATION_MINUTE = 40
    }
}

data class NotificationTime(
    val hour: Int,
    val minute: Int
) {
    val displayText: String
        get() = "%02d:%02d".format(hour, minute)
}
