package com.ireum.daily.model

data class Meal(
    val schoolName: String,
    val mealDate: String,
    val mealName: String,
    val dishes: List<String>,
    val calorieInfo: String,
    val nutrientInfo: String
)
