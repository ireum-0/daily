package com.ireum.daily.data.local

import androidx.room.Entity

@Entity(
    tableName = "meals",
    primaryKeys = ["educationOfficeCode", "schoolCode", "mealDate", "mealCode"]
)
data class MealEntity(
    val educationOfficeCode: String,
    val schoolCode: String,
    val schoolName: String,
    val mealDate: String,
    val mealCode: String,
    val mealName: String,
    val dishes: String,
    val originInfo: String,
    val calorieInfo: String,
    val nutrientInfo: String,
    val updatedAt: Long
)
