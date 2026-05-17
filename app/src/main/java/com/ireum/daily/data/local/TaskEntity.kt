package com.ireum.daily.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ireum.daily.model.TaskStatus

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
