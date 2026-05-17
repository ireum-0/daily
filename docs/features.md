# Daily Feature Notes

## Purpose

Daily shows school meal information from NEIS and lets the user configure the data source, school, and favorite menu alerts.

This file is the working feature map for future additions.

## Navigation

- Home
  - Shows the active school's meal table.
  - Shows only school days: Monday, Tuesday, Wednesday, Thursday, Friday.
  - If the app is not configured yet, shows the initial setup form instead of the meal table.
- Settings
  - Stores the NEIS API key.
  - Searches and saves the school.
  - Selects the favorite breakfast notification time.

## Initial Setup

The app is considered configured when both conditions are true:

- A NEIS API key is available.
- A school has been selected.

The NEIS API key can come from:

- User input saved in app preferences.
- `local.properties` fallback value, via `BuildConfig.NEIS_API_KEY`.

The school can come from:

- User selection from NEIS `schoolInfo`.
- `local.properties` fallback values, via `NEIS_OFFICE_CODE`, `NEIS_SCHOOL_CODE`, and `NEIS_SCHOOL_NAME`.

## Meal Loading

- The app loads one school week at a time.
- On Monday through Friday, the active week starts on the current week's Monday.
- On Saturday or Sunday, the active week starts on the next Monday.
- The app requests the full week range from NEIS using:
  - `MLSV_FROM_YMD`
  - `MLSV_TO_YMD`
- Meals are cached in Room by:
  - education office code
  - school code
  - meal date
  - meal code

## Day Selection

- Home displays five day buttons in one row: Mon-Fri.
- The selected day controls which cached meals are shown.
- Weekend meals are not exposed in the UI.

## School Search

- School search uses NEIS `schoolInfo`.
- The user must enter at least two characters.
- Search results show school name, education office, school kind, and address when available.
- Selecting a school saves:
  - education office code
  - school code
  - school name
- After selecting a school, the app refreshes the active week meal table.

## Favorite Menus

- Favorite menus are stored as a set of strings in DataStore preferences.
- Favorite menus are toggled directly from meal dish rows.
- Each dish row has a favorite action:
  - `선호` stores that dish as a favorite keyword.
  - `해제` removes the matched favorite keyword.
- Matching ignores whitespace and case.
- Matching is substring-based:
  - Favorite menu `돈까스` matches `수제돈까스`.
- When the selected day's meal includes a favorite menu:
  - A banner is shown above the meal list.
  - Matching dish rows are visually highlighted.
  - Weekday buttons with favorite menu matches use a distinct color.

## Favorite Breakfast Notifications

- The app schedules a daily local alarm at the user-selected time.
- The default notification time is 11:40.
- The Settings tab lets the user enter notification hour and minute.
- Notification time uses a 24-hour clock.
- Invalid values are rejected instead of being saved:
  - hour must be 0-23
  - minute must be 0-59
- Changing the saved time reschedules the daily alarm.
- At alarm time, the app checks cached meals for the next day's breakfast.
- If next day's breakfast contains a favorite menu, the app sends a notification.
- Notification text format:
  - `다음날 조식에 돈까스가 포함돼 있어요.`
  - `다음날 조식에 밥이 포함돼 있어요.`
- The `이/가` particle is selected from the last Korean syllable's final consonant.
- Notifications require Android notification permission on Android 13 and newer.
- Alarms are rescheduled after device boot.

## Current Storage

- DataStore preferences:
  - NEIS API key
  - selected school
  - favorite menu keywords
- Room:
  - cached meal rows

## Known Follow-Up Ideas

- Move NEIS API key storage to encrypted storage for production use.
- Add previous/next week navigation.
- Add notification support for favorite menu matches.
- Add tests for week-start calculation and favorite menu matching.
- Add a nicer icon-based bottom navigation.
