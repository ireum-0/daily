# Daily Implementation Plan

> This is the forward-looking implementation plan for Daily.
>
> `docs/features.md` is the current implemented feature map.  
> This file is the roadmap and implementation policy for future work.

---

## 0. Current Baseline

The app currently includes:

```text
- Bottom tabs: Home, Settings
- NEIS API key setup
- School search and selection
- Weekly meal loading from NEIS
- Mon-Fri meal selector
- Favorite menu toggle from meal rows
- Favorite menu highlights
- Favorite-menu day coloring
- Next-day breakfast favorite-menu notification
- Configurable notification time
- Room meal cache
- DataStore Preferences for settings
```

The intended final tab structure is:

```text
Home / Meal / Task / Settings
```

Do not convert the current 2-tab app into the final 4-tab app in one large change. Use this transition:

```text
1. Keep current Home/Settings stable.
2. Add the Task data layer and a simple Task screen.
3. Add a Task tab.
4. Split the current meal UI into a dedicated Meal tab.
5. Convert Home into a dashboard that combines Meal + Task summaries.
```

---

## 1. Product Direction

Daily is not a general productivity app or a full school portal clone. It should be a daily school-life dashboard that gives students a reason to open it every day.

Core loop:

```text
Check today's meal
-> Check today/tomorrow/this week's tasks
-> Mark tasks done
-> Receive morning/evening summary notifications
-> Import RiroSchool tasks by paste when needed
```

One-sentence definition:

```text
Daily is a school-life dashboard for meals, assignments, and deadline reminders.
```

Core values:

```text
- Show today's meals quickly.
- Help users avoid missing tasks due today, tomorrow, or this week.
- Move RiroSchool task text into Daily with minimal effort.
- Keep notifications low-volume and summary-oriented.
- Keep the app local-first, with cached meals and tasks available offline.
```

Feature acceptance checklist:

```text
1. Does it lead to an action today or tomorrow?
2. Is it directly related to meals, tasks, or deadline management?
3. Does it avoid heavy setup burden?
4. Does it avoid notification fatigue?
5. Does it preserve the local-first structure?
```

Deferred or excluded early features:

```text
- Timetable
- Academic calendar
- Heavy gamification
- Notion-style notes/wiki
- ClassDojo-style messaging
- RiroSchool automatic login
- Notification access based collection
- Full two-way calendar sync
- AI auto-scheduling
```

---

## 2. Target Tab Structure

Initial target tabs:

```text
Daily
├─ Home
├─ Meal
├─ Task
└─ Settings
```

### 2.1 Home

Home is the app's dashboard. It should show only items that matter today or tomorrow.

```text
Home
├─ Today's meal card
├─ Tasks due today card
├─ Tasks due tomorrow card
├─ Tasks due this week card
└─ Favorite menu card
```

Do not show these by default on Home:

```text
- Full task list
- All completed tasks
- All tasks without due dates
- Timetable
- Full academic calendar
- Complex stats
```

### 2.2 Meal

Keep and stabilize the current meal features:

```text
Meal
├─ Weekly meals
├─ Mon-Fri selector
├─ Breakfast/lunch/dinner display
├─ Favorite menu toggle
├─ Favorite-menu day highlight
├─ Favorite menu row highlight
└─ Next-day breakfast favorite-menu notification
```

### 2.3 Task

The Task tab is the core of Daily as a daily-management app.

```text
Task
├─ Today
├─ Tomorrow
├─ This week
├─ No due date
├─ Done
├─ Manual add
└─ RiroSchool paste import
```

### 2.4 Settings

Keep setup minimal:

```text
Settings
├─ School setup
├─ NEIS API key
├─ Notification settings
└─ Data deletion
```

Until automatic RiroSchool integration exists, RiroSchool settings should not become a large standalone settings area. Paste import belongs under Task.

---

## 3. MVP Split

The full product is too large for one MVP. Split it into MVP 1.0 and MVP 1.1.

### 3.1 MVP 1.0: Manual Task Management + Home Summary

Goal: validate that tasks are useful inside Daily without RiroSchool integration.

Include:

```text
- Keep existing meal features working
- Home dashboard revision
- Manual task creation
- Task done toggle
- Today/tomorrow/this-week classification
- Home task summary cards
- Morning summary notification
- Evening summary notification
```

Exclude:

```text
- RiroSchool paste parsing
- RiroSchool automatic login
- RiroSchool unofficial API calls
- Notification access based collection
- Share Intent import
- Academic calendar
- Timetable
- Widget
- Snooze
- Individual task reminders
```

MVP 1.0 done criteria:

```text
- Existing meal features are not broken.
- A user can save a task with title, subject, and due date.
- Saved tasks appear in the Task tab.
- Done tasks move to Done or are hidden from active sections.
- Today/tomorrow/this-week classification is correct.
- Done tasks disappear or collapse on Home.
- Tasks without due dates do not show on Home by default.
- Morning/evening summary notifications appear at configured times.
```

### 3.2 MVP 1.1: RiroSchool Paste Import

Goal: convert pasted RiroSchool task text into editable Daily task candidates.

Include:

```text
- Paste input screen
- Task candidate extraction
- Candidate edit screen
- Missing date/subject warnings
- Possible duplicate display
- Save/ignore/update existing
- Import failure recovery
```

MVP 1.1 done criteria:

```text
- Extract candidates from at least 7 of 10 realistic sample RiroSchool texts.
- Candidates without parsed dates can still be edited and saved.
- Re-pasting the same task shows possible duplicate status.
- If no candidate is found, the user can add manually or paste again.
- Do not auto-merge duplicates. Let the user decide.
```

---

## 4. Android Tech Stack

Use the existing Android stack:

```text
Language: Kotlin
UI: Jetpack Compose
Local DB: Room
Settings: DataStore Preferences
Network: Retrofit + OkHttp
Background work: WorkManager
Exact or user-visible alarms: AlarmManager only when needed
Notifications: NotificationManager
Secure storage: Android Keystore or EncryptedSharedPreferences family
Architecture: MVVM + Repository
```

Implementation note:

```text
Do not do the full architecture/package migration while adding task features.
First add task functionality in a small stable shape.
Move packages gradually after behavior is covered by build/tests.
```

---

## 5. Package Structure

Long-term target:

```text
com.ireum.daily
├─ MainActivity.kt
├─ app/
│  ├─ DailyApp.kt
│  ├─ AppNavGraph.kt
│  └─ DailyRoutes.kt
├─ core/
│  ├─ database/
│  │  ├─ AppDatabase.kt
│  │  ├─ dao/
│  │  └─ entity/
│  ├─ datastore/
│  │  ├─ AppPreferences.kt
│  │  └─ PreferenceKeys.kt
│  ├─ network/
│  │  ├─ NeisApiService.kt
│  │  ├─ NeisClient.kt
│  │  └─ dto/
│  ├─ notification/
│  ├─ security/
│  └─ util/
├─ domain/
│  ├─ model/
│  ├─ repository/
│  └─ usecase/
├─ feature/
│  ├─ home/
│  ├─ meal/
│  ├─ task/
│  ├─ setup/
│  └─ settings/
└─ worker/
```

Rules:

```text
- Do not put app-wide logic into meal.
- Task must work independently from Meal.
- Home should only compose data from Meal and Task.
- RiroSchool import belongs under Task.
- Avoid broad package moves while adding behavior.
- If package cleanup is needed, do it as a separate behavior-preserving refactor.
```

---

## 6. Storage Design

### 6.1 DataStore Preferences

Use DataStore only for settings-like data:

```text
neis_api_key_saved
selected_office_code
selected_school_code
selected_school_name
selected_school_kind

favorite_menu_keywords
favorite_meal_types

favorite_breakfast_notification_hour
favorite_breakfast_notification_minute

morning_summary_enabled
morning_summary_hour
morning_summary_minute

evening_summary_enabled
evening_summary_hour
evening_summary_minute

notification_permission_asked
onboarding_completed
dashboard_card_order
```

Current implementation stores the NEIS API key in DataStore. Before public distribution, move it to encrypted storage.

### 6.2 Room Tables

Keep early Room tables limited:

```text
meals
tasks
task_import_logs
task_source_snapshots
cache_meta
```

Do not add academic calendar or timetable tables during early MVP work.

### 6.3 Room Migration Policy

Room schema changes must be explicit.

```text
- Do not use destructive migration for user data.
- Increment DB version when adding tables or columns.
- Add Migration objects for every released schema change.
- Keep migrations small and tied to the feature that needs them.
- Add basic migration/build verification before release.
```

Expected near-term versions:

```text
v1: meals
v2: tasks
v3: task import metadata, if needed
```

---

## 7. Task Data Model

### 7.1 MVP 1.0 TaskEntity

Start with a small model for manual task management:

```kotlin
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val subjectName: String?,
    val dueDate: String?,
    val dueAt: Long?,
    val hasSpecificTime: Boolean,

    val status: TaskStatus,
    val memo: String?,

    val createdAt: Long,
    val updatedAt: Long
)
```

Fields for import and duplicate detection should wait until MVP 1.1:

```text
- source
- externalId
- sourceTextHash
- rawTitle
- importedAt
- import warnings
```

### 7.2 Import Metadata

When RiroSchool paste import is implemented, extend the model or add metadata tables for:

```text
- TaskSource
- sourceTextHash
- rawTitle
- importedAt
- task_import_logs
- task_source_snapshots
```

### 7.3 TaskSource

```kotlin
enum class TaskSource {
    MANUAL,
    RIROSCHOOL_CLIPBOARD,
    SHARE_INTENT,
    NOTIFICATION_IMPORT
}
```

MVP 1.0 uses only `MANUAL`. MVP 1.1 uses `RIROSCHOOL_CLIPBOARD`.

### 7.4 TaskStatus

```kotlin
enum class TaskStatus {
    TODO,
    DONE,
    SUBMITTED,
    UNKNOWN
}
```

Do not use `HIDDEN` as a status. If hiding is needed, add a separate visibility field later.

---

## 8. Task Classification

### 8.1 Home Priority

```text
1. Incomplete tasks due today
2. Incomplete tasks due tomorrow
3. Incomplete tasks due this week
4. Overdue incomplete tasks
5. Tasks without due dates
```

### 8.2 Home Display Rules

```text
Due today: show
Due tomorrow: show
Due this week: show up to 3
Overdue: show warning
No due date: hidden from Home by default
Done: hidden from Home
Hidden: hidden from Home and default lists
```

### 8.3 Tasks Without Due Dates

Do not keep no-due-date tasks permanently on Home. Put them in a dedicated Task section:

```text
Tasks without due dates: 2
- English presentation prep
- Science research notes

[Add due date]
```

---

## 9. Home UI

Do not build the final Home dashboard before Task storage and classification are stable.

Home should be a composition layer, not where Task or Meal business rules live.

Target HomeUiState:

```kotlin
data class HomeUiState(
    val dateLabel: String,
    val schoolName: String?,
    val todayMealSummary: MealSummary?,
    val favoriteMealSignal: FavoriteMealSignal?,
    val todayDueTasks: List<TaskSummary>,
    val tomorrowDueTasks: List<TaskSummary>,
    val weeklyDueTasks: List<TaskSummary>,
    val overdueTasks: List<TaskSummary>,
    val isOffline: Boolean,
    val lastUpdatedAt: Long?
)
```

Example card text:

```text
You have 2 tasks due today.
You have 1 task due tomorrow.
You have 4 tasks due this week.
Your favorite menu is in today's lunch.
Your favorite menu is in tomorrow's breakfast.
You have 2 tasks without due dates. Check the Task tab.
```

Empty states:

```text
No tasks due today.
No tasks due tomorrow.
No tasks due this week.
Meal info is not available yet.
Connect to the internet and refresh meals.
```

---

## 10. Notification Policy

Separate notification responsibilities by type.

Current notification:

```text
- Next-day breakfast favorite-menu notification
- User-selected notification time
- Uses cached meal data
```

Future notifications:

```text
- Morning summary notification
- Evening summary notification
```

Do not merge all notification logic into one receiver. Keep scheduling and content generation separate enough to test independently.

### 10.1 Morning Summary Notification

Purpose: start the day with today's key items.

Example:

```text
You have 2 tasks due today.
Your favorite menu is in today's lunch.
```

Includes:

```text
- Count of tasks due today
- Count of overdue incomplete tasks
- Whether today's meals include favorite menus
```

### 10.2 Evening Summary Notification

Purpose: help the user prepare for tomorrow.

Example:

```text
You have 1 task due tomorrow.
Your favorite menu is in tomorrow's breakfast.
```

Includes:

```text
- Count of tasks due tomorrow
- Count of remaining tasks this week
- Whether tomorrow's meals include favorite menus
```

### 10.3 Early Exclusions

```text
- Individual task reminders
- 3-hour-before deadline reminders
- Snooze
- Subject-specific reminders
- Repeating reminders
- RiroSchool auto-sync notifications
```

### 10.4 Notification Permission

Android 13+ notification permission policy:

```text
- Ask when the user enables or edits notification settings.
- If permission is denied, show an actionable Settings message.
- Do not assume alarms can produce visible notifications without permission.
```

Current implementation asks on app start. Before release, align with the policy above.

---

## 11. Favorite Breakfast Notification

Current behavior:

```text
- The user enters a notification time in Settings.
- Time uses a 24-hour clock.
- Default time is 11:40.
- Invalid values are rejected:
  - hour: 0-23
  - minute: 0-59
- Changing the saved time reschedules the daily alarm.
- At alarm time, the app checks cached meals for tomorrow's breakfast.
- If a favorite menu is found, it sends a notification.
```

Notification text:

```text
다음날 조식에 돈까스가 포함돼 있어요.
다음날 조식에 밥이 포함돼 있어요.
```

Korean particle logic:

```text
- Use "이" when the last Korean syllable has a final consonant.
- Use "가" otherwise.
```

Important limitation:

```text
This notification checks cached meals only.
The relevant meal week must have been loaded previously.
```

---

## 12. RiroSchool Paste Import

### 12.1 Principles

```text
- Do not implement automatic login.
- Do not call unofficial APIs.
- Do not use notification access based collection.
- Prefer candidate-edit UX over parser perfection.
- Do not auto-merge duplicate tasks.
```

### 12.2 Flow

```text
Task tab
-> Import from RiroSchool
-> Paste text
-> Analyze candidates
-> Edit candidates
-> Check possible duplicates
-> Save
```

### 12.3 Paste Instruction Text

```text
Paste your RiroSchool task list here.
Copying both task title and due date improves detection.
```

### 12.4 Candidate Edit Screen

Parser output must not be saved directly. The user confirms it first:

```text
[Title] Math performance report
[Subject] Math
[Due date] 2026-05-21
[Due time] 23:59
[Status] Not submitted

[Save] [Ignore]
```

If date is missing:

```text
Could not find a due date.
Please choose one manually.

[Choose due date]
[Save]
[Ignore]
```

### 12.5 ImportedTaskCandidate

```kotlin
data class ImportedTaskCandidate(
    val title: String,
    val subjectName: String?,
    val dueAt: Long?,
    val dueDate: String?,
    val hasSpecificTime: Boolean,
    val status: TaskStatus,
    val confidence: Float,
    val rawText: String,
    val warnings: List<ImportWarning>
)
```

### 12.6 ImportWarning

```kotlin
enum class ImportWarning {
    MISSING_DUE_DATE,
    MISSING_SUBJECT,
    LOW_CONFIDENCE,
    POSSIBLE_DUPLICATE,
    TOO_MANY_DATES
}
```

---

## 13. Duplicate Handling

Do not auto-merge duplicates. Show possible duplicates and let the user decide.

High confidence:

```text
- Same subject
- Same due date
- Similar title
```

Medium confidence:

```text
- Same due date
- Similar title
```

Low confidence:

```text
- Similar title only
```

Duplicate UI:

```text
A similar task already exists.

Existing:
Math report / May 21

New:
Math performance report / May 21

[Update existing]
[Add as new]
[Ignore]
```

Rules:

```text
- Never auto-merge, even with high confidence.
- Updating an existing task requires user confirmation.
- Always offer add-as-new and ignore.
- Use sourceTextHash to detect identical pasted input.
```

---

## 14. Failure States

RiroSchool parsing failure is normal. Always provide recovery paths.

Failure states:

```text
- No task candidates found
- Date could not be parsed
- Subject could not be parsed
- Too many candidates
- Existing task conflict
- Save failure
- Notification scheduling failure
```

Examples:

No candidates:

```text
Could not find task candidates.
Paste a RiroSchool task title and due date together.

[Add task manually]
[Paste again]
```

Date parse failure:

```text
Could not find a due date.
Choose a date manually to save this task.

[Choose due date]
[Save without due date]
[Ignore]
```

Too many candidates:

```text
Too many candidates were detected.
Copy only the tasks you need to submit and paste again.

[Paste again]
[Add task manually]
```

Save failure:

```text
Could not save the task.
Try again later.

[Retry]
[Cancel]
```

---

## 15. Repository and Scheduler Responsibilities

### 15.1 MealRepository

```text
- Call NEIS meal APIs
- Save meals into Room cache
- Observe selected weekly meals
- Provide today/tomorrow meal summaries
- Provide data needed for favorite menu matching
```

### 15.2 TaskRepository

```text
- Add/edit/delete tasks
- Toggle done
- Query today/tomorrow/this-week tasks
- Query overdue tasks
- Query tasks without due dates
- Save RiroSchool import results
- Query possible duplicates
```

### 15.3 NotificationScheduler / NotificationRepository

Suggested split:

```text
NotificationScheduler
- schedule/cancel alarms or WorkManager jobs
- handle boot reschedule

NotificationContentBuilder
- create title/body text
- own Korean particle formatting

NotificationPermissionState
- check whether notifications can be posted
```

---

## 16. Development Stages

### 16.1 Stage 1: Stabilize Current Meal Features

Tasks:

```text
- Keep current meal behavior working
- Keep Room meal cache
- Extract FavoriteMatchUtils
- Extract KoreanParticleUtils
- Extract SchoolWeekUtils
- Keep current breakfast notification behavior
```

Important:

```text
Do not migrate the whole package structure at once.
Extract small utilities first.
```

Done criteria:

```text
- Meals still display from cache without internet.
- Favorite matching ignores whitespace/case and supports substring matching.
- 돈까스 matches 수제돈까스.
- Breakfast notification still works.
```

### 16.2 Stage 2: Manual Task Management

Recommended order:

```text
1. Add TaskEntity/TaskDao with Room migration.
2. Add TaskRepository.
3. Add a simple Task tab.
4. Add task add/edit UI.
5. Add done/todo toggle.
6. Add classification helpers after CRUD works.
```

Done criteria:

```text
- User can save a task with title, subject, and due date.
- Saved tasks appear in the list.
- User can edit tasks.
- User can delete or hide tasks.
- Done tasks move to Done or disappear from active lists.
```

### 16.3 Stage 3: Home Task Summary

Tasks:

```text
- Split Meal tab from current Home/Settings if not already done.
- Combine MealRepository and TaskRepository in HomeViewModel.
- Add due-today card.
- Add due-tomorrow card.
- Add due-this-week card.
- Add overdue warning.
```

Done criteria:

```text
- Today/tomorrow/this-week tasks are classified correctly.
- Done tasks disappear from Home.
- Tasks without due dates are hidden from Home by default.
- Overdue tasks show warning.
```

### 16.4 Stage 4: Summary Notifications

Tasks:

```text
- Add morning summary notification settings.
- Add evening summary notification settings.
- Handle Android 13+ notification permission.
- Generate notification content.
- Reschedule notifications after boot.
```

Done criteria:

```text
- Configured time shows today/tomorrow task counts.
- Favorite menu info is included when relevant.
- Permission missing state is visible in Settings.
- Disabling notification cancels scheduled work.
```

### 16.5 Stage 5: RiroSchool Paste Import

Tasks:

```text
- Add paste screen.
- Add RiroSchoolTextParser.
- Add ImportedTaskCandidate model.
- Add candidate edit screen.
- Show possible duplicates.
- Save/ignore/update handling.
```

Done criteria:

```text
- Extract candidates from at least 7 of 10 sample texts.
- User can edit and save candidates with missing dates.
- Re-pasting the same task shows possible duplicate status.
- Parser failure leads to manual add or paste-again path.
```

### 16.6 Later Review

Consider only after the core loop is stable:

```text
- Academic calendar
- Share Intent import
- Widget
- Favorite menu weekly preview
- Routine/habit checks
- Memo/attachment
- One-way calendar export
```

---

## 17. Test Plan

### 17.1 Unit Tests

Date and week logic:

```text
- Weekday uses current Monday.
- Weekend uses next Monday.
- Today due task classification.
- Tomorrow due task classification.
- This-week due task classification.
- Overdue task classification.
- No-due-date task handling.
```

Notification time validation:

```text
- hour 0-23 accepted
- minute 0-59 accepted
- invalid hour rejected
- invalid minute rejected
- blank input rejected
```

Favorite menu matching:

```text
- Ignore whitespace
- Ignore case
- Substring matching
- 돈까스 ↔ 수제돈까스
```

Korean particle:

```text
- 돈까스가
- 밥이
- 우유가
- 국이
- 돈까스, 우유가
```

RiroSchool parser:

```text
- Text with date
- Text with time
- Text without date
- Text without subject
- Multiple tasks in one paste
- Re-pasted same task
```

### 17.2 Repository Tests

```text
- Add task and query it.
- Done task excluded from Home.
- No-due-date task excluded from Home.
- Duplicate candidate detected.
- Meal cache displays after network failure.
```

### 17.3 UI Tests

```text
- Initial setup shown before configuration.
- Home shown after configuration.
- Home meal card opens Meal tab.
- Home task card opens Task tab.
- Done toggle updates Home cards.
- Paste import failure shows recovery buttons.
```

---

## 18. Codex Work Rules

Do not ask Codex to change the whole app in one request. Keep tasks staged and narrow.

Common instruction:

```text
This is a staged Daily app implementation.
Do not modify features outside the requested scope.
Do not rewrite unrelated meal, settings, or notification code.
If writing results into a file, do not repeat the full same content in chat.
After implementing, run a build or relevant test.
If verification fails, explain the failure cause.
Do not confuse docs/features.md with this implementation plan.
When current implemented behavior changes, update docs/features.md.
```

Example task instructions:

```text
Stage 1:
Keep existing meal behavior unchanged and extract small utilities:
SchoolWeekUtils, FavoriteMatchUtils, KoreanParticleUtils.

Stage 2:
Add manual task management.
Create TaskEntity, TaskDao, TaskRepository, task list/add/edit/done UI.
Do not implement RiroSchool import yet.

Stage 3:
Add task summary cards to Home:
due today, due tomorrow, due this week, overdue.
Do not show done tasks or no-due-date tasks by default.

Stage 4:
Implement morning and evening summary notifications.
Do not implement individual task reminders, snooze, or 3-hour-before reminders.

Stage 5:
Implement RiroSchool paste import only.
Do not implement automatic login, unofficial API calls, or notification access collection.
Show candidates and let users edit before saving.
Do not auto-merge duplicates.
```

---

## 19. Roadmap

| Step | Feature | Done Criteria |
|---:|---|---|
| 1 | Keep meal stable | Existing meal, favorite menu, breakfast notification still work |
| 2 | Manual task add | Title/subject/due date can be saved and listed |
| 3 | Done toggle | Done tasks disappear or collapse from active views |
| 4 | Home task cards | Today/tomorrow/this-week classification works |
| 5 | Morning/evening summaries | Notifications show task counts and meal summary |
| 6 | RiroSchool paste | Extract candidates from at least 7/10 samples |
| 7 | Candidate edit | Failed/partial candidates can be edited and saved |
| 8 | Duplicate check | Re-pasted tasks show duplicate candidates |
| 9 | Academic calendar | Review after task feature stabilizes |
| 10 | Auto integration | Review only after official or acceptable approach is confirmed |

---

## 20. Do Not Implement Early

```text
- Timetable
- RiroSchool account/password storage
- RiroSchool unofficial automatic API calls
- CAPTCHA/security bypass
- Access to all user notifications
- Automatic duplicate merging
- Excessive individual notifications
- Complex Home with academic calendar
- AI auto-scheduling
```

---

## 21. Final Recommended Order

```text
Keep meal stable
-> Manual task management
-> Home task summary
-> Summary notifications
-> RiroSchool paste import
-> Duplicate handling
-> Academic calendar or automatic integration review
```

This is the safest order. First prove that tasks are useful inside Daily, then add RiroSchool import.
