package com.ireum.daily.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MealEntity::class,
        TaskEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class DailyDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun taskDao(): TaskDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tasks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `subjectName` TEXT,
                        `dueDate` TEXT,
                        `dueAt` INTEGER,
                        `hasSpecificTime` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `memo` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
