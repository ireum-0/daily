package com.ireum.daily.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ireum.daily.data.MealRefreshResult
import com.ireum.daily.data.MealRepository
import com.ireum.daily.data.SchoolConfig
import com.ireum.daily.data.SchoolSearchItem
import com.ireum.daily.data.SchoolSearchResult
import com.ireum.daily.data.TaskRepository
import com.ireum.daily.data.local.TaskEntity
import com.ireum.daily.data.preferences.NotificationTime
import com.ireum.daily.data.preferences.SummaryNotificationSettings
import com.ireum.daily.core.util.TaskDateCategory
import com.ireum.daily.core.util.classifyTaskDueDate
import com.ireum.daily.core.util.findMatchingFavoriteMenus
import com.ireum.daily.core.util.startOfSchoolWeek
import com.ireum.daily.model.Meal
import com.ireum.daily.model.TaskStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MealViewModel(
    private val mealRepository: MealRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val weekStart = MutableStateFlow(LocalDate.now().startOfSchoolWeek())
    private val selectedDate = MutableStateFlow(weekStart.value)
    private val selectedTab = MutableStateFlow(AppTab.Home)
    private val loading = MutableStateFlow(true)
    private val searchingSchools = MutableStateFlow(false)
    private val mealMessage = MutableStateFlow<MealMessage?>(null)
    private val apiKeyInput = MutableStateFlow("")
    private val schoolQuery = MutableStateFlow("")
    private val schoolResults = MutableStateFlow<List<SchoolSearchItem>>(emptyList())
    private val schoolMessage = MutableStateFlow<SchoolMessage?>(null)
    private val notificationHourInput = MutableStateFlow("")
    private val notificationMinuteInput = MutableStateFlow("")
    private val taskTitleInput = MutableStateFlow("")
    private val taskSubjectInput = MutableStateFlow("")
    private val taskDueDateInput = MutableStateFlow("")
    private val editingTaskId = MutableStateFlow<Long?>(null)
    private val taskMessage = MutableStateFlow<TaskMessage?>(null)
    private val morningSummaryEnabled = MutableStateFlow(false)
    private val morningSummaryHourInput = MutableStateFlow("")
    private val morningSummaryMinuteInput = MutableStateFlow("")
    private val eveningSummaryEnabled = MutableStateFlow(false)
    private val eveningSummaryHourInput = MutableStateFlow("")
    private val eveningSummaryMinuteInput = MutableStateFlow("")

    private val setupState = combine(
        mealRepository.schoolConfig,
        mealRepository.hasNeisApiKey
    ) { school, hasApiKey ->
        SetupState(
            school = school,
            hasApiKey = hasApiKey,
            hasSchool = school.hasRequiredCodes
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val weeklyMeals = weekStart.flatMapLatest { startDate ->
        mealRepository.observeMeals(
            startDate = startDate,
            endDate = startDate.endOfSchoolWeek()
        )
    }

    private val mealDisplayState = combine(
        weekStart,
        selectedDate,
        weeklyMeals,
        mealRepository.favoriteMenus
    ) { startDate, selected, meals, favoriteMenus ->
        val selectedNeisDate = selected.toNeisDate()
        val weekDays = (0L until SCHOOL_WEEK_DAY_COUNT).map { offset ->
            val date = startDate.plusDays(offset)
            val neisDate = date.toNeisDate()
            val dayMeals = meals.filter { meal -> meal.mealDate == neisDate }
            MealDayUiState(
                date = date,
                dayLabel = date.format(DateTimeFormatter.ofPattern("E", Locale.KOREAN)),
                dateLabel = date.format(DateTimeFormatter.ofPattern("M.d", Locale.KOREAN)),
                hasMeal = dayMeals.isNotEmpty(),
                hasFavoriteMenu = dayMeals.findFavoriteMealMatches(favoriteMenus).isNotEmpty()
            )
        }
        val selectedMeals = meals.filter { meal -> meal.mealDate == selectedNeisDate }
        val homeMealDate = LocalDate.now()
            .takeIf { date -> date in startDate..startDate.endOfSchoolWeek() }
            ?: startDate
        val homeMeals = meals.filter { meal -> meal.mealDate == homeMealDate.toNeisDate() }
        MealDisplayState(
            selectedDate = selected,
            selectedMeals = selectedMeals,
            homeMeals = homeMeals,
            favoriteMenus = favoriteMenus.toList().sorted(),
            favoriteMealMatches = selectedMeals.findFavoriteMealMatches(favoriteMenus),
            weekDays = weekDays,
            selectedDateText = selected.format(DateTimeFormatter.ofPattern("yyyy.MM.dd E", Locale.KOREAN)),
            weekRangeText = "${startDate.format(DateTimeFormatter.ofPattern("M.d", Locale.KOREAN))} - ${
                startDate.endOfSchoolWeek().format(DateTimeFormatter.ofPattern("M.d", Locale.KOREAN))
            }"
        )
    }

    private val refreshState = combine(
        loading,
        mealMessage
    ) { isLoading, message ->
        RefreshState(isLoading = isLoading, message = message)
    }

    private val schoolSearchState = combine(
        searchingSchools,
        apiKeyInput,
        schoolQuery,
        schoolResults,
        schoolMessage
    ) { isSearching, apiKey, query, results, message ->
        SchoolSearchUiState(
            isSearching = isSearching,
            apiKey = apiKey,
            query = query,
            results = results,
            message = message
        )
    }

    private val notificationInputState = combine(
        notificationHourInput,
        notificationMinuteInput,
        mealRepository.notificationTime
    ) { hourInput, minuteInput, notificationTime ->
        NotificationInputState(
            hourInput = hourInput,
            minuteInput = minuteInput,
            timeText = notificationTime.displayText
        )
    }

    private val summaryNotificationInputState = combine(
        morningSummaryEnabled,
        morningSummaryHourInput,
        morningSummaryMinuteInput,
        eveningSummaryEnabled,
        eveningSummaryHourInput,
        eveningSummaryMinuteInput,
        mealRepository.summaryNotificationSettings
    ) { values ->
        val morningEnabled = values[0] as Boolean
        val morningHour = values[1] as String
        val morningMinute = values[2] as String
        val eveningEnabled = values[3] as Boolean
        val eveningHour = values[4] as String
        val eveningMinute = values[5] as String
        val settings = values[6] as SummaryNotificationSettings

        SummaryNotificationInputState(
            morningEnabled = morningEnabled,
            morningHourInput = morningHour,
            morningMinuteInput = morningMinute,
            morningTimeText = settings.morningTime.displayText,
            eveningEnabled = eveningEnabled,
            eveningHourInput = eveningHour,
            eveningMinuteInput = eveningMinute,
            eveningTimeText = settings.eveningTime.displayText
        )
    }

    private val taskInputState = combine(
        taskTitleInput,
        taskSubjectInput,
        taskDueDateInput,
        editingTaskId,
        taskMessage
    ) { title, subject, dueDate, editingId, message ->
        TaskInputState(
            title = title,
            subject = subject,
            dueDate = dueDate,
            editingTaskId = editingId,
            message = message
        )
    }

    private val screenState = combine(
        setupState,
        mealDisplayState,
        refreshState,
        schoolSearchState,
        selectedTab
    ) { setup, mealDisplay, refresh, searchState, tab ->
        ScreenState(
            setup = setup,
            mealDisplay = mealDisplay,
            refresh = refresh,
            searchState = searchState,
            selectedTab = tab
        )
    }

    val uiState = combine(
        screenState,
        notificationInputState,
        taskRepository.observeTasks(),
        taskInputState,
        summaryNotificationInputState
    ) { screen, notificationState, tasks, taskInput, summaryNotificationState ->
        val taskUiStates = tasks.map(TaskEntity::toUiState)
        val activeTasks = tasks.filterNot { task -> task.status == TaskStatus.DONE }
        val today = LocalDate.now()
        MealUiState(
            selectedTab = screen.selectedTab,
            isLoading = screen.refresh.isLoading,
            isSearchingSchools = screen.searchState.isSearching,
            isSetupComplete = screen.setup.isComplete,
            hasApiKey = screen.setup.hasApiKey,
            hasSchool = screen.setup.hasSchool,
            schoolName = screen.setup.school.schoolName,
            apiKeyInput = screen.searchState.apiKey,
            schoolQuery = screen.searchState.query,
            schoolResults = screen.searchState.results,
            notificationHourInput = notificationState.hourInput,
            notificationMinuteInput = notificationState.minuteInput,
            notificationTimeText = notificationState.timeText,
            favoriteMenus = screen.mealDisplay.favoriteMenus,
            favoriteMealMatches = screen.mealDisplay.favoriteMealMatches,
            homeMealSummary = screen.mealDisplay.homeMeals.toHomeMealSummary(
                favoriteMenus = screen.mealDisplay.favoriteMenus
            ),
            todayTasks = activeTasks.toTaskUiStates(TaskDateCategory.TODAY, today),
            tomorrowTasks = activeTasks.toTaskUiStates(TaskDateCategory.TOMORROW, today),
            weeklyTasks = activeTasks.toTaskUiStates(TaskDateCategory.THIS_WEEK, today).take(3),
            overdueTasks = activeTasks.toTaskUiStates(TaskDateCategory.OVERDUE, today),
            dateText = screen.mealDisplay.selectedDateText,
            weekRangeText = screen.mealDisplay.weekRangeText,
            selectedDate = screen.mealDisplay.selectedDate,
            weekDays = screen.mealDisplay.weekDays,
            meals = screen.mealDisplay.selectedMeals,
            mealMessage = screen.refresh.message ?: if (
                screen.setup.isComplete &&
                !screen.refresh.isLoading &&
                screen.mealDisplay.selectedMeals.isEmpty()
            ) {
                MealMessage.NoMeal
            } else {
                null
            },
            schoolMessage = screen.searchState.message,
            tasks = taskUiStates,
            taskTitleInput = taskInput.title,
            taskSubjectInput = taskInput.subject,
            taskDueDateInput = taskInput.dueDate,
            editingTaskId = taskInput.editingTaskId,
            taskMessage = taskInput.message,
            morningSummaryEnabled = summaryNotificationState.morningEnabled,
            morningSummaryHourInput = summaryNotificationState.morningHourInput,
            morningSummaryMinuteInput = summaryNotificationState.morningMinuteInput,
            morningSummaryTimeText = summaryNotificationState.morningTimeText,
            eveningSummaryEnabled = summaryNotificationState.eveningEnabled,
            eveningSummaryHourInput = summaryNotificationState.eveningHourInput,
            eveningSummaryMinuteInput = summaryNotificationState.eveningMinuteInput,
            eveningSummaryTimeText = summaryNotificationState.eveningTimeText
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MealUiState()
    )

    init {
        viewModelScope.launch {
            mealRepository.neisApiKey.collect { savedApiKey ->
                if (apiKeyInput.value.isBlank()) {
                    apiKeyInput.value = savedApiKey
                }
            }
        }
        viewModelScope.launch {
            mealRepository.notificationTime.collect { notificationTime ->
                if (notificationHourInput.value.isBlank()) {
                    notificationHourInput.value = notificationTime.hour.toString().padStart(2, '0')
                }
                if (notificationMinuteInput.value.isBlank()) {
                    notificationMinuteInput.value = notificationTime.minute.toString().padStart(2, '0')
                }
            }
        }
        viewModelScope.launch {
            mealRepository.summaryNotificationSettings.collect { settings ->
                morningSummaryEnabled.value = settings.morningEnabled
                eveningSummaryEnabled.value = settings.eveningEnabled
                if (morningSummaryHourInput.value.isBlank()) {
                    morningSummaryHourInput.value = settings.morningTime.hour.toString().padStart(2, '0')
                }
                if (morningSummaryMinuteInput.value.isBlank()) {
                    morningSummaryMinuteInput.value = settings.morningTime.minute.toString().padStart(2, '0')
                }
                if (eveningSummaryHourInput.value.isBlank()) {
                    eveningSummaryHourInput.value = settings.eveningTime.hour.toString().padStart(2, '0')
                }
                if (eveningSummaryMinuteInput.value.isBlank()) {
                    eveningSummaryMinuteInput.value = settings.eveningTime.minute.toString().padStart(2, '0')
                }
            }
        }
        refresh()
    }

    fun selectTab(tab: AppTab) {
        selectedTab.value = tab
    }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun refresh() {
        viewModelScope.launch {
            loading.value = true
            mealMessage.value = null
            mealRepository.seedDefaultSchoolIfNeeded()
            val startDate = weekStart.value
            mealMessage.value = when (mealRepository.refreshMeals(startDate, startDate.endOfSchoolWeek())) {
                MealRefreshResult.Success -> null
                MealRefreshResult.MissingApiKey -> MealMessage.MissingApiKey
                MealRefreshResult.MissingSchool -> MealMessage.MissingSchool
                MealRefreshResult.NetworkUnavailable -> MealMessage.NetworkUnavailable
                MealRefreshResult.ApiError -> MealMessage.ApiError
                MealRefreshResult.NoMeal -> MealMessage.NoMeal
            }
            loading.value = false
        }
    }

    fun updateSchoolQuery(query: String) {
        schoolQuery.value = query
        schoolMessage.value = null
    }

    fun updateApiKey(apiKey: String) {
        apiKeyInput.value = apiKey
        schoolMessage.value = null
    }

    fun saveApiKey() {
        viewModelScope.launch {
            mealRepository.saveNeisApiKey(apiKeyInput.value)
            schoolMessage.value = SchoolMessage.ApiKeySaved
        }
    }

    fun updateNotificationHour(hour: String) {
        notificationHourInput.value = hour.filter(Char::isDigit).take(2)
        schoolMessage.value = null
    }

    fun updateNotificationMinute(minute: String) {
        notificationMinuteInput.value = minute.filter(Char::isDigit).take(2)
        schoolMessage.value = null
    }

    fun saveNotificationTime() {
        viewModelScope.launch {
            val hour = notificationHourInput.value.toIntOrNull()
            val minute = notificationMinuteInput.value.toIntOrNull()
            if (hour == null || hour !in 0..23 || minute == null || minute !in 0..59) {
                schoolMessage.value = SchoolMessage.InvalidNotificationTime
                return@launch
            }

            mealRepository.saveNotificationTime(
                NotificationTime(
                    hour = hour,
                    minute = minute
                )
            )
            notificationHourInput.value = hour.toString().padStart(2, '0')
            notificationMinuteInput.value = minute.toString().padStart(2, '0')
            schoolMessage.value = SchoolMessage.NotificationTimeSaved
        }
    }

    fun setMorningSummaryEnabled(enabled: Boolean) {
        morningSummaryEnabled.value = enabled
        schoolMessage.value = null
    }

    fun updateMorningSummaryHour(hour: String) {
        morningSummaryHourInput.value = hour.filter(Char::isDigit).take(2)
        schoolMessage.value = null
    }

    fun updateMorningSummaryMinute(minute: String) {
        morningSummaryMinuteInput.value = minute.filter(Char::isDigit).take(2)
        schoolMessage.value = null
    }

    fun setEveningSummaryEnabled(enabled: Boolean) {
        eveningSummaryEnabled.value = enabled
        schoolMessage.value = null
    }

    fun updateEveningSummaryHour(hour: String) {
        eveningSummaryHourInput.value = hour.filter(Char::isDigit).take(2)
        schoolMessage.value = null
    }

    fun updateEveningSummaryMinute(minute: String) {
        eveningSummaryMinuteInput.value = minute.filter(Char::isDigit).take(2)
        schoolMessage.value = null
    }

    fun saveSummaryNotificationSettings() {
        viewModelScope.launch {
            val morningHour = morningSummaryHourInput.value.toIntOrNull()
            val morningMinute = morningSummaryMinuteInput.value.toIntOrNull()
            val eveningHour = eveningSummaryHourInput.value.toIntOrNull()
            val eveningMinute = eveningSummaryMinuteInput.value.toIntOrNull()
            if (
                morningHour == null || morningHour !in 0..23 ||
                morningMinute == null || morningMinute !in 0..59 ||
                eveningHour == null || eveningHour !in 0..23 ||
                eveningMinute == null || eveningMinute !in 0..59
            ) {
                schoolMessage.value = SchoolMessage.InvalidSummaryNotificationTime
                return@launch
            }

            mealRepository.saveSummaryNotificationSettings(
                SummaryNotificationSettings(
                    morningEnabled = morningSummaryEnabled.value,
                    morningTime = NotificationTime(morningHour, morningMinute),
                    eveningEnabled = eveningSummaryEnabled.value,
                    eveningTime = NotificationTime(eveningHour, eveningMinute)
                )
            )
            morningSummaryHourInput.value = morningHour.toString().padStart(2, '0')
            morningSummaryMinuteInput.value = morningMinute.toString().padStart(2, '0')
            eveningSummaryHourInput.value = eveningHour.toString().padStart(2, '0')
            eveningSummaryMinuteInput.value = eveningMinute.toString().padStart(2, '0')
            schoolMessage.value = SchoolMessage.SummaryNotificationSettingsSaved
        }
    }

    fun toggleFavoriteMenu(menu: String) {
        viewModelScope.launch {
            val currentMenus = mealRepository.favoriteMenus.first()
            val trimmedMenu = menu.trim()
            if (trimmedMenu.isBlank()) return@launch

            val matchingMenus = findMatchingFavoriteMenus(
                dishes = listOf(trimmedMenu),
                favoriteMenus = currentMenus
            ).toSet()

            val nextMenus = if (matchingMenus.isEmpty()) {
                currentMenus + trimmedMenu
            } else {
                currentMenus - matchingMenus
            }
            mealRepository.saveFavoriteMenus(nextMenus)
        }
    }

    fun searchSchools() {
        viewModelScope.launch {
            searchingSchools.value = true
            schoolMessage.value = null
            schoolResults.value = emptyList()
            when (val result = mealRepository.searchSchools(schoolQuery.value)) {
                is SchoolSearchResult.Success -> schoolResults.value = result.schools
                SchoolSearchResult.EmptyQuery -> schoolMessage.value = SchoolMessage.EmptyQuery
                SchoolSearchResult.MissingApiKey -> schoolMessage.value = SchoolMessage.MissingApiKey
                SchoolSearchResult.NetworkUnavailable -> schoolMessage.value = SchoolMessage.NetworkUnavailable
                SchoolSearchResult.ApiError -> schoolMessage.value = SchoolMessage.ApiError
                SchoolSearchResult.NoResult -> schoolMessage.value = SchoolMessage.NoResult
            }
            searchingSchools.value = false
        }
    }

    fun selectSchool(school: SchoolSearchItem) {
        viewModelScope.launch {
            mealRepository.saveSchool(school.config)
            schoolQuery.value = school.config.schoolName
            schoolResults.value = emptyList()
            schoolMessage.value = SchoolMessage.Saved
            refresh()
        }
    }

    fun updateTaskTitle(title: String) {
        taskTitleInput.value = title
        taskMessage.value = null
    }

    fun updateTaskSubject(subject: String) {
        taskSubjectInput.value = subject
        taskMessage.value = null
    }

    fun updateTaskDueDate(dueDate: String) {
        taskDueDateInput.value = dueDate.filter { char -> char.isDigit() || char == '-' }.take(10)
        taskMessage.value = null
    }

    fun saveTask() {
        viewModelScope.launch {
            val title = taskTitleInput.value.trim()
            if (title.isBlank()) {
                taskMessage.value = TaskMessage.EmptyTitle
                return@launch
            }

            val dueDate = taskDueDateInput.value.trim().takeIf(String::isNotBlank)
            if (dueDate != null && dueDate.toLocalDateOrNull() == null) {
                taskMessage.value = TaskMessage.InvalidDueDate
                return@launch
            }

            val editingId = editingTaskId.value
            taskRepository.saveTask(
                id = editingId,
                title = title,
                subjectName = taskSubjectInput.value.trim().takeIf(String::isNotBlank),
                dueDate = dueDate
            )
            clearTaskInput()
            taskMessage.value = if (editingId == null) TaskMessage.Saved else TaskMessage.Updated
        }
    }

    fun editTask(id: Long) {
        viewModelScope.launch {
            val task = taskRepository.getTask(id) ?: return@launch
            editingTaskId.value = task.id
            taskTitleInput.value = task.title
            taskSubjectInput.value = task.subjectName.orEmpty()
            taskDueDateInput.value = task.dueDate.orEmpty()
            taskMessage.value = null
        }
    }

    fun cancelTaskEdit() {
        clearTaskInput()
        taskMessage.value = null
    }

    fun setTaskDone(id: Long, done: Boolean) {
        viewModelScope.launch {
            taskRepository.setDone(id, done)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            taskRepository.deleteTask(id)
            if (editingTaskId.value == id) {
                clearTaskInput()
            }
            taskMessage.value = TaskMessage.Deleted
        }
    }

    private fun clearTaskInput() {
        editingTaskId.value = null
        taskTitleInput.value = ""
        taskSubjectInput.value = ""
        taskDueDateInput.value = ""
    }

    class Factory(
        private val mealRepository: MealRepository,
        private val taskRepository: TaskRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MealViewModel(mealRepository, taskRepository) as T
        }
    }
}

private data class SetupState(
    val school: SchoolConfig,
    val hasApiKey: Boolean,
    val hasSchool: Boolean
) {
    val isComplete: Boolean
        get() = hasApiKey && hasSchool
}

private data class MealDisplayState(
    val selectedDate: LocalDate,
    val selectedMeals: List<Meal>,
    val homeMeals: List<Meal>,
    val favoriteMenus: List<String>,
    val favoriteMealMatches: List<FavoriteMealMatchUiState>,
    val weekDays: List<MealDayUiState>,
    val selectedDateText: String,
    val weekRangeText: String
)

private data class ScreenState(
    val setup: SetupState,
    val mealDisplay: MealDisplayState,
    val refresh: RefreshState,
    val searchState: SchoolSearchUiState,
    val selectedTab: AppTab
)

private data class RefreshState(
    val isLoading: Boolean,
    val message: MealMessage?
)

private data class SchoolSearchUiState(
    val isSearching: Boolean,
    val apiKey: String,
    val query: String,
    val results: List<SchoolSearchItem>,
    val message: SchoolMessage?
)

private data class NotificationInputState(
    val hourInput: String,
    val minuteInput: String,
    val timeText: String
)

private data class SummaryNotificationInputState(
    val morningEnabled: Boolean,
    val morningHourInput: String,
    val morningMinuteInput: String,
    val morningTimeText: String,
    val eveningEnabled: Boolean,
    val eveningHourInput: String,
    val eveningMinuteInput: String,
    val eveningTimeText: String
)

private data class TaskInputState(
    val title: String,
    val subject: String,
    val dueDate: String,
    val editingTaskId: Long?,
    val message: TaskMessage?
)

private val neisDateFormatter = DateTimeFormatter.BASIC_ISO_DATE
private const val SCHOOL_WEEK_DAY_COUNT = 5L

private fun LocalDate.toNeisDate(): String = format(neisDateFormatter)

private fun LocalDate.endOfSchoolWeek(): LocalDate =
    plusDays(SCHOOL_WEEK_DAY_COUNT - 1)

private fun String.toLocalDateOrNull(): LocalDate? =
    runCatching { LocalDate.parse(this) }.getOrNull()

private fun TaskEntity.toUiState(): TaskUiState =
    TaskUiState(
        id = id,
        title = title,
        subjectName = subjectName.orEmpty(),
        dueDateText = dueDate?.let { date ->
            runCatching {
                LocalDate.parse(date).format(DateTimeFormatter.ofPattern("M.d E", Locale.KOREAN))
            }.getOrDefault(date)
        } ?: "기한 없음",
        isDone = status == TaskStatus.DONE
    )

private fun List<TaskEntity>.toTaskUiStates(
    category: TaskDateCategory,
    today: LocalDate
): List<TaskUiState> =
    filter { task -> classifyTaskDueDate(task.dueDate, today) == category }
        .map(TaskEntity::toUiState)

private fun List<Meal>.toHomeMealSummary(favoriteMenus: List<String>): HomeMealSummaryUiState? {
    if (isEmpty()) return null
    val lines = map { meal ->
        "${meal.mealName}: ${meal.dishes.take(3).joinToString(", ")}"
    }
    val favoriteMatches = findFavoriteMealMatches(favoriteMenus.toSet())
    return HomeMealSummaryUiState(
        title = "오늘 급식",
        lines = lines,
        favoriteText = favoriteMatches
            .takeIf(List<FavoriteMealMatchUiState>::isNotEmpty)
            ?.joinToString(" / ") { match -> "${match.mealName}: ${match.menus.joinToString(", ")}" }
    )
}

private fun List<Meal>.findFavoriteMealMatches(favoriteMenus: Set<String>): List<FavoriteMealMatchUiState> =
    mapNotNull { meal ->
        val matchedFavorites = findMatchingFavoriteMenus(
            dishes = meal.dishes,
            favoriteMenus = favoriteMenus
        )

        if (matchedFavorites.isEmpty()) {
            null
        } else {
            FavoriteMealMatchUiState(
                mealName = meal.mealName,
                menus = matchedFavorites
            )
        }
    }
