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
    val homeMealTitle: String = "오늘 급식",
    val homeMealSummary: HomeMealSummaryUiState? = null,
    val todayTasks: List<TaskUiState> = emptyList(),
    val tomorrowTasks: List<TaskUiState> = emptyList(),
    val weeklyTasks: List<TaskUiState> = emptyList(),
    val overdueTasks: List<TaskUiState> = emptyList(),
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
    val taskMessage: TaskMessage? = null,
    val importPasteInput: String = "",
    val importedTaskCandidates: List<ImportedTaskCandidateUiState> = emptyList(),
    val taskImportMessage: TaskImportMessage? = null,
    val morningSummaryEnabled: Boolean = false,
    val morningSummaryHourInput: String = "",
    val morningSummaryMinuteInput: String = "",
    val morningSummaryTimeText: String = "",
    val eveningSummaryEnabled: Boolean = false,
    val eveningSummaryHourInput: String = "",
    val eveningSummaryMinuteInput: String = "",
    val eveningSummaryTimeText: String = ""
)

enum class AppTab {
    Home,
    Meal,
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

data class HomeMealSummaryUiState(
    val title: String,
    val lines: List<String>,
    val favoriteText: String?
)

data class TaskUiState(
    val id: Long,
    val title: String,
    val subjectName: String,
    val dueDateText: String,
    val isDone: Boolean
)

data class ImportedTaskCandidateUiState(
    val id: Int,
    val titleInput: String,
    val subjectInput: String,
    val dueDateInput: String,
    val rawText: String,
    val warnings: List<String>,
    val possibleDuplicate: TaskUiState?
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
    InvalidNotificationTime,
    SummaryNotificationSettingsSaved,
    InvalidSummaryNotificationTime
}

enum class TaskMessage {
    EmptyTitle,
    InvalidDueDate,
    Saved,
    Updated,
    Deleted
}

enum class TaskImportMessage {
    EmptyPaste,
    NoCandidates,
    CandidatesFound,
    CandidateSaved,
    CandidateUpdated,
    CandidateIgnored,
    InvalidCandidateDueDate,
    EmptyCandidateTitle
}
