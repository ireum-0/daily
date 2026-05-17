package com.ireum.daily.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MealEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DailyDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
}
