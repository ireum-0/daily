package com.ireum.daily.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query(
        """
        SELECT * FROM meals
        WHERE educationOfficeCode = :educationOfficeCode
          AND schoolCode = :schoolCode
          AND mealDate = :mealDate
        ORDER BY mealCode
        """
    )
    fun observeMeals(
        educationOfficeCode: String,
        schoolCode: String,
        mealDate: String
    ): Flow<List<MealEntity>>

    @Query(
        """
        SELECT * FROM meals
        WHERE educationOfficeCode = :educationOfficeCode
          AND schoolCode = :schoolCode
          AND mealDate BETWEEN :startDate AND :endDate
        ORDER BY mealDate, mealCode
        """
    )
    fun observeMealsBetween(
        educationOfficeCode: String,
        schoolCode: String,
        startDate: String,
        endDate: String
    ): Flow<List<MealEntity>>

    @Query(
        """
        SELECT * FROM meals
        WHERE educationOfficeCode = :educationOfficeCode
          AND schoolCode = :schoolCode
          AND mealDate = :mealDate
        ORDER BY mealCode
        """
    )
    suspend fun getMealsForDate(
        educationOfficeCode: String,
        schoolCode: String,
        mealDate: String
    ): List<MealEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeals(meals: List<MealEntity>)

    @Query(
        """
        DELETE FROM meals
        WHERE educationOfficeCode = :educationOfficeCode
          AND schoolCode = :schoolCode
          AND mealDate = :mealDate
        """
    )
    suspend fun deleteMealsForDate(
        educationOfficeCode: String,
        schoolCode: String,
        mealDate: String
    )

    @Query(
        """
        DELETE FROM meals
        WHERE educationOfficeCode = :educationOfficeCode
          AND schoolCode = :schoolCode
          AND mealDate BETWEEN :startDate AND :endDate
        """
    )
    suspend fun deleteMealsBetween(
        educationOfficeCode: String,
        schoolCode: String,
        startDate: String,
        endDate: String
    )

    @Transaction
    suspend fun replaceMealsForDate(
        educationOfficeCode: String,
        schoolCode: String,
        mealDate: String,
        meals: List<MealEntity>
    ) {
        deleteMealsForDate(educationOfficeCode, schoolCode, mealDate)
        if (meals.isNotEmpty()) {
            upsertMeals(meals)
        }
    }

    @Transaction
    suspend fun replaceMealsBetween(
        educationOfficeCode: String,
        schoolCode: String,
        startDate: String,
        endDate: String,
        meals: List<MealEntity>
    ) {
        deleteMealsBetween(educationOfficeCode, schoolCode, startDate, endDate)
        if (meals.isNotEmpty()) {
            upsertMeals(meals)
        }
    }
}
