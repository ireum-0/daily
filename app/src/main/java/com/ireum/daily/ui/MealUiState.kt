package com.ireum.daily.ui

import com.ireum.daily.data.SchoolSearchItem
import com.ireum.daily.model.Meal
import java.time.LocalDate

data class MealUiState(
    val selectedTab: AppTab = AppTab.Home,
    val isLoading: Boolean = true,
    val isSearchingSchools: Boolean = false,
    val isSetupComplete: Boolean = false,
    val hasApiKey: Boolean = false,
    val hasSchool: Boolean = false,
    val schoolName: String = "",
    val apiKeyInput: String = "",
    val schoolQuery: String = "",
    val schoolResults: List<SchoolSearchItem> = emptyList(),
    val notificationHourInput: String = "",
    val notificationMinuteInput: String = "",
    val notificationTimeText: String = "",
    val favoriteMenus: List<String> = emptyList(),
    val favoriteMealMatches: List<FavoriteMealMatchUiState> = emptyList(),
    val dateText: String = "",
    val weekRangeText: String = "",
    val selectedDate: LocalDate = LocalDate.now(),
    val weekDays: List<MealDayUiState> = emptyList(),
    val meals: List<Meal> = emptyList(),
    val mealMessage: MealMessage? = null,
    val schoolMessage: SchoolMessage? = null
)

enum class AppTab {
    Home,
    Settings
}

data class MealDayUiState(
    val date: LocalDate,
    val dayLabel: String,
    val dateLabel: String,
    val hasMeal: Boolean,
    val hasFavoriteMenu: Boolean
)

data class FavoriteMealMatchUiState(
    val mealName: String,
    val menus: List<String>
)

enum class MealMessage {
    MissingApiKey,
    MissingSchool,
    NetworkUnavailable,
    ApiError,
    NoMeal
}

enum class SchoolMessage {
    EmptyQuery,
    MissingApiKey,
    NetworkUnavailable,
    ApiError,
    NoResult,
    Saved,
    ApiKeySaved,
    NotificationTimeSaved,
    InvalidNotificationTime
}
