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
    val schoolMessage: SchoolMessage? = null,
    val tasks: List<TaskUiState> = emptyList(),
    val taskTitleInput: String = "",
    val taskSubjectInput: String = "",
    val taskDueDateInput: String = "",
    val editingTaskId: Long? = null,
    val taskMessage: TaskMessage? = null
)

enum class AppTab {
    Home,
    Task,
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

data class TaskUiState(
    val id: Long,
    val title: String,
    val subjectName: String,
    val dueDateText: String,
    val isDone: Boolean
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

enum class TaskMessage {
    EmptyTitle,
    InvalidDueDate,
    Saved,
    Updated,
    Deleted
}
