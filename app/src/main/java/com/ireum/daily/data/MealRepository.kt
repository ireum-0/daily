package com.ireum.daily.data

import com.ireum.daily.data.local.MealDao
import com.ireum.daily.data.local.MealEntity
import com.ireum.daily.data.preferences.NotificationTime
import com.ireum.daily.data.preferences.SchoolPreferences
import com.ireum.daily.data.preferences.SummaryNotificationSettings
import com.ireum.daily.data.remote.NeisMealApi
import com.ireum.daily.data.remote.RemoteMeal
import com.ireum.daily.data.remote.RemoteSchool
import com.ireum.daily.model.Meal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.coroutines.cancellation.CancellationException

class MealRepository(
    private val mealDao: MealDao,
    private val neisMealApi: NeisMealApi,
    private val schoolPreferences: SchoolPreferences,
    private val defaultNeisApiKey: String,
    private val defaultSchool: SchoolConfig
) {
    val schoolConfig: Flow<SchoolConfig> = schoolPreferences.schoolConfig
    val neisApiKey: Flow<String> = schoolPreferences.neisApiKey
    val favoriteMenus: Flow<Set<String>> = schoolPreferences.favoriteMenus
    val notificationTime: Flow<NotificationTime> = schoolPreferences.notificationTime
    val summaryNotificationSettings: Flow<SummaryNotificationSettings> =
        schoolPreferences.summaryNotificationSettings
    val hasNeisApiKey: Flow<Boolean> =
        schoolPreferences.neisApiKey.map { apiKey -> apiKey.isNotBlank() || defaultNeisApiKey.isNotBlank() }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeMeals(date: LocalDate): Flow<List<Meal>> =
        schoolPreferences.schoolConfig.flatMapLatest { school ->
            if (!school.hasRequiredCodes) {
                flowOf(emptyList())
            } else {
                mealDao.observeMeals(
                    educationOfficeCode = school.educationOfficeCode,
                    schoolCode = school.schoolCode,
                    mealDate = date.toNeisDate()
                ).map { entities -> entities.map(MealEntity::toMeal) }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeMeals(startDate: LocalDate, endDate: LocalDate): Flow<List<Meal>> =
        schoolPreferences.schoolConfig.flatMapLatest { school ->
            if (!school.hasRequiredCodes) {
                flowOf(emptyList())
            } else {
                mealDao.observeMealsBetween(
                    educationOfficeCode = school.educationOfficeCode,
                    schoolCode = school.schoolCode,
                    startDate = startDate.toNeisDate(),
                    endDate = endDate.toNeisDate()
                ).map { entities -> entities.map(MealEntity::toMeal) }
            }
        }

    suspend fun seedDefaultSchoolIfNeeded() {
        val current = schoolPreferences.schoolConfig.first()
        if (!current.hasRequiredCodes && defaultSchool.hasRequiredCodes) {
            schoolPreferences.saveSchool(defaultSchool)
        }
    }

    suspend fun saveSchool(schoolConfig: SchoolConfig) {
        schoolPreferences.saveSchool(schoolConfig)
    }

    suspend fun saveNeisApiKey(apiKey: String) {
        schoolPreferences.saveNeisApiKey(apiKey)
    }

    suspend fun saveFavoriteMenus(favoriteMenus: Set<String>) {
        schoolPreferences.saveFavoriteMenus(favoriteMenus)
    }

    suspend fun saveNotificationTime(notificationTime: NotificationTime) {
        schoolPreferences.saveNotificationTime(notificationTime)
    }

    suspend fun saveSummaryNotificationSettings(settings: SummaryNotificationSettings) {
        schoolPreferences.saveSummaryNotificationSettings(settings)
    }

    suspend fun searchSchools(query: String): SchoolSearchResult {
        val keyword = query.trim()
        if (keyword.length < 2) return SchoolSearchResult.EmptyQuery
        val apiKey = currentApiKey()
        if (apiKey.isBlank()) return SchoolSearchResult.MissingApiKey

        return try {
            val response = neisMealApi.searchSchools(
                apiKey = apiKey,
                schoolName = keyword
            )
            val resultCode = response.resultCode()
            if (resultCode != null && resultCode != "INFO-000" && resultCode != "INFO-200") {
                return SchoolSearchResult.ApiError
            }

            val schools = response.rows()
                .mapNotNull(RemoteSchool::toSearchItem)
                .distinctBy { item -> item.config.educationOfficeCode to item.config.schoolCode }

            if (schools.isEmpty()) {
                SchoolSearchResult.NoResult
            } else {
                SchoolSearchResult.Success(schools)
            }
        } catch (_: IOException) {
            SchoolSearchResult.NetworkUnavailable
        } catch (_: HttpException) {
            SchoolSearchResult.ApiError
        } catch (exception: RuntimeException) {
            if (exception is CancellationException) throw exception
            SchoolSearchResult.ApiError
        }
    }

    suspend fun refreshMeals(date: LocalDate): MealRefreshResult {
        return refreshMeals(startDate = date, endDate = date)
    }

    suspend fun refreshMeals(startDate: LocalDate, endDate: LocalDate): MealRefreshResult {
        val school = schoolPreferences.schoolConfig.first()
        if (!school.hasRequiredCodes) return MealRefreshResult.MissingSchool
        val apiKey = currentApiKey()
        if (apiKey.isBlank()) return MealRefreshResult.MissingApiKey

        val startMealDate = startDate.toNeisDate()
        val endMealDate = endDate.toNeisDate()
        return try {
            val response = neisMealApi.getMealServiceDietInfo(
                apiKey = apiKey,
                educationOfficeCode = school.educationOfficeCode,
                schoolCode = school.schoolCode,
                mealDate = if (startDate == endDate) startMealDate else null,
                mealStartDate = if (startDate == endDate) null else startMealDate,
                mealEndDate = if (startDate == endDate) null else endMealDate
            )
            val resultCode = response.resultCode()
            if (resultCode != null && resultCode != "INFO-000" && resultCode != "INFO-200") {
                return MealRefreshResult.ApiError
            }
            val remoteMeals = response.rows()
            if (remoteMeals.isEmpty()) {
                mealDao.replaceMealsBetween(
                    educationOfficeCode = school.educationOfficeCode,
                    schoolCode = school.schoolCode,
                    startDate = startMealDate,
                    endDate = endMealDate,
                    meals = emptyList()
                )
                MealRefreshResult.NoMeal
            } else {
                mealDao.replaceMealsBetween(
                    educationOfficeCode = school.educationOfficeCode,
                    schoolCode = school.schoolCode,
                    startDate = startMealDate,
                    endDate = endMealDate,
                    meals = remoteMeals.map { remoteMeal ->
                        remoteMeal.toEntity(
                            fallbackSchool = school,
                            fallbackDate = startMealDate
                        )
                    }
                )
                MealRefreshResult.Success
            }
        } catch (_: IOException) {
            MealRefreshResult.NetworkUnavailable
        } catch (_: HttpException) {
            MealRefreshResult.ApiError
        } catch (exception: RuntimeException) {
            if (exception is CancellationException) throw exception
            MealRefreshResult.ApiError
        }
    }

    private suspend fun currentApiKey(): String =
        schoolPreferences.neisApiKey.first().ifBlank { defaultNeisApiKey }
}

data class SchoolConfig(
    val educationOfficeCode: String = "",
    val schoolCode: String = "",
    val schoolName: String = ""
) {
    val hasRequiredCodes: Boolean
        get() = educationOfficeCode.isNotBlank() && schoolCode.isNotBlank()
}

sealed interface MealRefreshResult {
    data object Success : MealRefreshResult
    data object MissingApiKey : MealRefreshResult
    data object MissingSchool : MealRefreshResult
    data object NetworkUnavailable : MealRefreshResult
    data object ApiError : MealRefreshResult
    data object NoMeal : MealRefreshResult
}

data class SchoolSearchItem(
    val config: SchoolConfig,
    val educationOfficeName: String,
    val schoolKind: String,
    val address: String
)

sealed interface SchoolSearchResult {
    data class Success(val schools: List<SchoolSearchItem>) : SchoolSearchResult
    data object EmptyQuery : SchoolSearchResult
    data object MissingApiKey : SchoolSearchResult
    data object NetworkUnavailable : SchoolSearchResult
    data object ApiError : SchoolSearchResult
    data object NoResult : SchoolSearchResult
}

private val neisDateFormatter = DateTimeFormatter.BASIC_ISO_DATE

private fun LocalDate.toNeisDate(): String = format(neisDateFormatter)

private fun RemoteMeal.toEntity(fallbackSchool: SchoolConfig, fallbackDate: String): MealEntity =
    MealEntity(
        educationOfficeCode = educationOfficeCode.orEmpty().ifBlank { fallbackSchool.educationOfficeCode },
        schoolCode = schoolCode.orEmpty().ifBlank { fallbackSchool.schoolCode },
        schoolName = schoolName.orEmpty().ifBlank { fallbackSchool.schoolName },
        mealDate = mealDate.orEmpty().ifBlank { fallbackDate },
        mealCode = mealCode.orEmpty().ifBlank { mealName.orEmpty().ifBlank { "0" } },
        mealName = mealName.orEmpty().ifBlank { "급식" },
        dishes = dishName.orEmpty().toDisplayText(),
        originInfo = originInfo.orEmpty().toDisplayText(),
        calorieInfo = calorieInfo.orEmpty(),
        nutrientInfo = nutrientInfo.orEmpty().toDisplayText(),
        updatedAt = System.currentTimeMillis()
    )

private fun MealEntity.toMeal(): Meal =
    Meal(
        schoolName = schoolName,
        mealDate = mealDate,
        mealName = mealName,
        dishes = dishes.lines().filter(String::isNotBlank),
        calorieInfo = calorieInfo,
        nutrientInfo = nutrientInfo
    )

private fun String.toDisplayText(): String =
    replace("<br/>", "\n")
        .replace("<br>", "\n")
        .replace(Regex("\\([^)]*\\)"), "")
        .lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .joinToString("\n")

private fun RemoteSchool.toSearchItem(): SchoolSearchItem? {
    val config = SchoolConfig(
        educationOfficeCode = educationOfficeCode.orEmpty(),
        schoolCode = schoolCode.orEmpty(),
        schoolName = schoolName.orEmpty()
    )
    if (!config.hasRequiredCodes || config.schoolName.isBlank()) return null

    return SchoolSearchItem(
        config = config,
        educationOfficeName = educationOfficeName.orEmpty(),
        schoolKind = schoolKind.orEmpty(),
        address = roadAddress.orEmpty().ifBlank { locationName.orEmpty() }
    )
}
