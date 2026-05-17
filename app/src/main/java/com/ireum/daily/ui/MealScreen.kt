package com.ireum.daily.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ireum.daily.R
import com.ireum.daily.core.util.matchesFavoriteMenu
import com.ireum.daily.data.SchoolSearchItem
import com.ireum.daily.model.Meal
import java.time.LocalDate

@Composable
fun MealScreen(viewModel: MealViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            DailyBottomBar(
                selectedTab = uiState.selectedTab,
                onSelectTab = viewModel::selectTab
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { contentPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (uiState.selectedTab) {
                AppTab.Home -> HomeTab(
                    uiState = uiState,
                    onRefresh = viewModel::refresh,
                    onSelectDate = viewModel::selectDate,
                    onToggleFavoriteMenu = viewModel::toggleFavoriteMenu,
                    onApiKeyChange = viewModel::updateApiKey,
                    onSaveApiKey = viewModel::saveApiKey,
                    onQueryChange = viewModel::updateSchoolQuery,
                    onSearch = viewModel::searchSchools,
                    onSelectSchool = viewModel::selectSchool
                )

                AppTab.Task -> TaskTab(
                    uiState = uiState,
                    onTitleChange = viewModel::updateTaskTitle,
                    onSubjectChange = viewModel::updateTaskSubject,
                    onDueDateChange = viewModel::updateTaskDueDate,
                    onSaveTask = viewModel::saveTask,
                    onEditTask = viewModel::editTask,
                    onCancelEdit = viewModel::cancelTaskEdit,
                    onSetDone = viewModel::setTaskDone,
                    onDeleteTask = viewModel::deleteTask
                )

                AppTab.Settings -> SettingsTab(
                    uiState = uiState,
                    onApiKeyChange = viewModel::updateApiKey,
                    onSaveApiKey = viewModel::saveApiKey,
                    onQueryChange = viewModel::updateSchoolQuery,
                    onSearch = viewModel::searchSchools,
                    onSelectSchool = viewModel::selectSchool,
                    onNotificationHourChange = viewModel::updateNotificationHour,
                    onNotificationMinuteChange = viewModel::updateNotificationMinute,
                    onSaveNotificationTime = viewModel::saveNotificationTime
                )
            }
        }
    }
}

@Composable
private fun DailyBottomBar(
    selectedTab: AppTab,
    onSelectTab: (AppTab) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == AppTab.Home,
            onClick = { onSelectTab(AppTab.Home) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_nav_home),
                    contentDescription = null
                )
            },
            label = { Text("홈") }
        )
        NavigationBarItem(
            selected = selectedTab == AppTab.Task,
            onClick = { onSelectTab(AppTab.Task) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_nav_task),
                    contentDescription = null
                )
            },
            label = { Text("과제") }
        )
        NavigationBarItem(
            selected = selectedTab == AppTab.Settings,
            onClick = { onSelectTab(AppTab.Settings) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_nav_settings),
                    contentDescription = null
                )
            },
            label = { Text("설정") }
        )
    }
}

@Composable
private fun TaskTab(
    uiState: MealUiState,
    onTitleChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit,
    onDueDateChange: (String) -> Unit,
    onSaveTask: () -> Unit,
    onEditTask: (Long) -> Unit,
    onCancelEdit: () -> Unit,
    onSetDone: (Long, Boolean) -> Unit,
    onDeleteTask: (Long) -> Unit
) {
    ScreenColumn {
        SectionTitle(
            title = "과제",
            description = "오늘 확인할 과제를 직접 추가하고 완료 처리하세요."
        )

        TaskEditorSection(
            uiState = uiState,
            onTitleChange = onTitleChange,
            onSubjectChange = onSubjectChange,
            onDueDateChange = onDueDateChange,
            onSaveTask = onSaveTask,
            onCancelEdit = onCancelEdit
        )

        TaskListSection(
            title = "할 일",
            emptyText = "진행 중인 과제가 없습니다.",
            tasks = uiState.tasks.filterNot(TaskUiState::isDone),
            onEditTask = onEditTask,
            onSetDone = onSetDone,
            onDeleteTask = onDeleteTask
        )

        TaskListSection(
            title = "완료",
            emptyText = "완료한 과제가 없습니다.",
            tasks = uiState.tasks.filter(TaskUiState::isDone),
            onEditTask = onEditTask,
            onSetDone = onSetDone,
            onDeleteTask = onDeleteTask
        )
    }
}

@Composable
private fun TaskEditorSection(
    uiState: MealUiState,
    onTitleChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit,
    onDueDateChange: (String) -> Unit,
    onSaveTask: () -> Unit,
    onCancelEdit: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionTitle(
                title = if (uiState.editingTaskId == null) "과제 추가" else "과제 수정",
                description = "제목은 필수이고, 기한은 2026-05-21 형식으로 입력하세요."
            )

            OutlinedTextField(
                value = uiState.taskTitleInput,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("제목") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = uiState.taskSubjectInput,
                onValueChange = onSubjectChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("과목") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = uiState.taskDueDateInput,
                onValueChange = onDueDateChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("기한") },
                placeholder = { Text("YYYY-MM-DD") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onSaveTask() })
            )

            uiState.taskMessage?.let { message ->
                val isError = message == TaskMessage.EmptyTitle ||
                    message == TaskMessage.InvalidDueDate
                Text(
                    text = message.toDisplayText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSaveTask) {
                    Text(if (uiState.editingTaskId == null) "추가" else "수정 저장")
                }
                if (uiState.editingTaskId != null) {
                    OutlinedButton(onClick = onCancelEdit) {
                        Text("취소")
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskListSection(
    title: String,
    emptyText: String,
    tasks: List<TaskUiState>,
    onEditTask: (Long) -> Unit,
    onSetDone: (Long, Boolean) -> Unit,
    onDeleteTask: (Long) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "$title ${tasks.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (tasks.isEmpty()) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            tasks.forEachIndexed { index, task ->
                if (index > 0) {
                    HorizontalDivider()
                }
                TaskRow(
                    task = task,
                    onEditTask = onEditTask,
                    onSetDone = onSetDone,
                    onDeleteTask = onDeleteTask
                )
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: TaskUiState,
    onEditTask: (Long) -> Unit,
    onSetDone: (Long, Boolean) -> Unit,
    onDeleteTask: (Long) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isDone,
            onCheckedChange = { checked -> onSetDone(task.id, checked) }
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (task.isDone) FontWeight.Normal else FontWeight.SemiBold,
                color = if (task.isDone) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = listOf(task.subjectName, task.dueDateText)
                    .filter(String::isNotBlank)
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(onClick = { onEditTask(task.id) }) {
            Text("수정")
        }
        TextButton(onClick = { onDeleteTask(task.id) }) {
            Text("삭제")
        }
    }
}

@Composable
private fun HomeTab(
    uiState: MealUiState,
    onRefresh: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onToggleFavoriteMenu: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onSaveApiKey: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelectSchool: (SchoolSearchItem) -> Unit
) {
    ScreenColumn {
        Header(
            schoolName = uiState.schoolName,
            weekRangeText = uiState.weekRangeText,
            isLoading = uiState.isLoading,
            onRefresh = onRefresh
        )

        if (!uiState.isSetupComplete) {
            SchoolSettingsSection(
                uiState = uiState,
                title = "처음 설정",
                description = "급식표를 보려면 NEIS API 키와 학교를 먼저 설정하세요.",
                onApiKeyChange = onApiKeyChange,
                onSaveApiKey = onSaveApiKey,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                onSelectSchool = onSelectSchool
            )
        } else {
            MealSection(
                uiState = uiState,
                onRefresh = onRefresh,
                onSelectDate = onSelectDate,
                onToggleFavoriteMenu = onToggleFavoriteMenu
            )
        }
    }
}

@Composable
private fun SettingsTab(
    uiState: MealUiState,
    onApiKeyChange: (String) -> Unit,
    onSaveApiKey: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelectSchool: (SchoolSearchItem) -> Unit,
    onNotificationHourChange: (String) -> Unit,
    onNotificationMinuteChange: (String) -> Unit,
    onSaveNotificationTime: () -> Unit
) {
    ScreenColumn {
        SectionTitle(
            title = "설정",
            description = if (uiState.isSetupComplete) {
                "${uiState.schoolName} 급식표를 사용 중입니다."
            } else {
                "NEIS API 키와 학교를 설정해 주세요."
            }
        )
        SchoolSettingsSection(
            uiState = uiState,
            title = "급식 정보",
            description = "API 키를 저장한 뒤 학교명을 검색해 선택하세요.",
            onApiKeyChange = onApiKeyChange,
            onSaveApiKey = onSaveApiKey,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            onSelectSchool = onSelectSchool
        )
        NotificationSettingsSection(
            uiState = uiState,
            onNotificationHourChange = onNotificationHourChange,
            onNotificationMinuteChange = onNotificationMinuteChange,
            onSaveNotificationTime = onSaveNotificationTime
        )
    }
}

@Composable
private fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        content = content
    )
}

@Composable
private fun Header(
    schoolName: String,
    weekRangeText: String,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = schoolName.ifBlank { "학교를 설정해 주세요" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = weekRangeText.ifBlank { "1주일 급식표" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        OutlinedButton(
            onClick = onRefresh,
            enabled = !isLoading
        ) {
            Text("새로고침")
        }
    }
}

@Composable
private fun SchoolSettingsSection(
    uiState: MealUiState,
    title: String,
    description: String,
    onApiKeyChange: (String) -> Unit,
    onSaveApiKey: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelectSchool: (SchoolSearchItem) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionTitle(title = title, description = description)

            SetupStatus(uiState = uiState)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.apiKeyInput,
                    onValueChange = onApiKeyChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("NEIS API 키") },
                    placeholder = { Text("발급받은 인증키 입력") },
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedButton(onClick = onSaveApiKey) {
                    Text("저장")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.schoolQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("학교명") },
                    placeholder = { Text("예: 서울고등학교") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() })
                )
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onSearch,
                    enabled = !uiState.isSearchingSchools
                ) {
                    Text("검색")
                }
            }

            if (uiState.isSearchingSchools) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            uiState.schoolMessage
                ?.takeUnless { message ->
                    message == SchoolMessage.NotificationTimeSaved ||
                        message == SchoolMessage.InvalidNotificationTime
                }
                ?.let { message ->
                val isPositive = message == SchoolMessage.Saved ||
                    message == SchoolMessage.ApiKeySaved
                Text(
                    text = message.toDisplayText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isPositive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            uiState.schoolResults.forEach { school ->
                SchoolResultRow(
                    school = school,
                    onSelect = { onSelectSchool(school) }
                )
            }
        }
    }
}

@Composable
private fun SetupStatus(uiState: MealUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = uiState.hasApiKey,
            onClick = {},
            label = { Text(if (uiState.hasApiKey) "키 설정됨" else "키 필요") }
        )
        FilterChip(
            selected = uiState.hasSchool,
            onClick = {},
            label = { Text(if (uiState.hasSchool) "학교 설정됨" else "학교 필요") }
        )
    }
}

@Composable
private fun NotificationSettingsSection(
    uiState: MealUiState,
    onNotificationHourChange: (String) -> Unit,
    onNotificationMinuteChange: (String) -> Unit,
    onSaveNotificationTime: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionTitle(
                title = "알림",
                description = "다음날 조식 선호 메뉴 알림 시간: ${uiState.notificationTimeText}"
            )

            if (
                uiState.schoolMessage == SchoolMessage.NotificationTimeSaved ||
                uiState.schoolMessage == SchoolMessage.InvalidNotificationTime
            ) {
                val isValid = uiState.schoolMessage == SchoolMessage.NotificationTimeSaved
                Text(
                    text = uiState.schoolMessage.toDisplayText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isValid) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.notificationHourInput,
                    onValueChange = onNotificationHourChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("시") },
                    placeholder = { Text("0-23") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(":")
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = uiState.notificationMinuteInput,
                    onValueChange = onNotificationMinuteChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("분") },
                    placeholder = { Text("0-59") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onSaveNotificationTime() })
                )
                Spacer(modifier = Modifier.width(10.dp))
                Button(onClick = onSaveNotificationTime) {
                    Text("저장")
                }
            }
        }
    }
}

@Composable
private fun MealSection(
    uiState: MealUiState,
    onRefresh: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onToggleFavoriteMenu: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle(
                title = "급식표",
                description = uiState.dateText
            )

            DaySelector(
                weekDays = uiState.weekDays,
                selectedDate = uiState.selectedDate,
                onSelectDate = onSelectDate
            )

            if (uiState.favoriteMealMatches.isNotEmpty()) {
                FavoriteMatchBanner(favoriteMealMatches = uiState.favoriteMealMatches)
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            uiState.mealMessage?.let { message ->
                Text(
                    text = message.toDisplayText(),
                    color = if (message == MealMessage.NoMeal) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (uiState.meals.isEmpty() && !uiState.isLoading && uiState.mealMessage == null) {
                EmptyMeal(onRefresh = onRefresh)
            }

            uiState.meals.forEachIndexed { index, meal ->
                if (index > 0) {
                    HorizontalDivider()
                }
                MealContent(
                    meal = meal,
                    favoriteMenus = uiState.favoriteMenus,
                    onToggleFavoriteMenu = onToggleFavoriteMenu
                )
            }
        }
    }
}

@Composable
private fun FavoriteMatchBanner(favoriteMealMatches: List<FavoriteMealMatchUiState>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "선호 메뉴 포함",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
            favoriteMealMatches.forEach { match ->
                Text(
                    text = "${match.mealName}: ${match.menus.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun DaySelector(
    weekDays: List<MealDayUiState>,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        weekDays.forEach { day ->
            FilterChip(
                selected = day.date == selectedDate,
                onClick = { onSelectDate(day.date) },
                modifier = Modifier.weight(1f),
                colors = if (day.hasFavoriteMenu) {
                    FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                    )
                } else {
                    FilterChipDefaults.filterChipColors()
                },
                label = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(day.dayLabel)
                    }
                }
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    description: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SchoolResultRow(
    school: SchoolSearchItem,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = school.config.schoolName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = listOf(
                        school.educationOfficeName,
                        school.schoolKind,
                        school.address
                    ).filter(String::isNotBlank).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            TextButton(onClick = onSelect) {
                Text("선택")
            }
        }
    }
}

@Composable
private fun EmptyMeal(onRefresh: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "선택한 날짜에 표시할 급식이 없습니다.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(onClick = onRefresh) {
            Text("다시 가져오기")
        }
    }
}

@Composable
private fun MealContent(
    meal: Meal,
    favoriteMenus: List<String>,
    onToggleFavoriteMenu: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = meal.mealName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        meal.dishes.forEach { dish ->
            val isFavorite = dish.matchesFavoriteMenu(favoriteMenus)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = if (isFavorite) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ) {
                DishRow(
                    dish = dish,
                    isFavorite = isFavorite,
                    onToggleFavoriteMenu = onToggleFavoriteMenu,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp)
                )
            }
        }
    }
}

@Composable
private fun DishRow(
    dish: String,
    isFavorite: Boolean,
    onToggleFavoriteMenu: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dish,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isFavorite) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontWeight = if (isFavorite) FontWeight.SemiBold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(onClick = { onToggleFavoriteMenu(dish) }) {
            Text(if (isFavorite) "해제" else "선호")
        }
    }
}

private fun MealMessage.toDisplayText(): String =
    when (this) {
        MealMessage.MissingApiKey -> "NEIS API Key가 설정되지 않았습니다."
        MealMessage.MissingSchool -> "학교를 설정하면 급식표를 가져올 수 있습니다."
        MealMessage.NetworkUnavailable -> "인터넷 연결이 없어 저장된 급식표를 표시합니다."
        MealMessage.ApiError -> "급식 정보를 불러오지 못했습니다. 저장된 급식표를 표시합니다."
        MealMessage.NoMeal -> "선택한 날짜에 등록된 급식이 없습니다."
    }

private fun SchoolMessage.toDisplayText(): String =
    when (this) {
        SchoolMessage.EmptyQuery -> "학교명을 두 글자 이상 입력해 주세요."
        SchoolMessage.MissingApiKey -> "NEIS API Key가 설정되지 않았습니다."
        SchoolMessage.NetworkUnavailable -> "인터넷 연결을 확인해 주세요."
        SchoolMessage.ApiError -> "학교 정보를 불러오지 못했습니다."
        SchoolMessage.NoResult -> "검색 결과가 없습니다."
        SchoolMessage.Saved -> "학교가 저장되었습니다. 급식표를 가져오는 중입니다."
        SchoolMessage.ApiKeySaved -> "NEIS API 키가 저장되었습니다."
        SchoolMessage.NotificationTimeSaved -> "알림 시간이 저장되었습니다."
        SchoolMessage.InvalidNotificationTime -> "알림 시간은 시 0-23, 분 0-59 범위로 입력해 주세요."
    }

private fun TaskMessage.toDisplayText(): String =
    when (this) {
        TaskMessage.EmptyTitle -> "과제 제목을 입력해 주세요."
        TaskMessage.InvalidDueDate -> "기한은 YYYY-MM-DD 형식으로 입력해 주세요."
        TaskMessage.Saved -> "과제가 추가되었습니다."
        TaskMessage.Updated -> "과제가 수정되었습니다."
        TaskMessage.Deleted -> "과제가 삭제되었습니다."
    }
