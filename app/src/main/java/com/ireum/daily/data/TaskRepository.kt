package com.ireum.daily.data

import com.ireum.daily.data.local.TaskDao
import com.ireum.daily.data.local.TaskEntity
import com.ireum.daily.model.TaskStatus
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao
) {
    fun observeTasks(): Flow<List<TaskEntity>> = taskDao.observeTasks()

    suspend fun getTask(id: Long): TaskEntity? = taskDao.getTask(id)

    suspend fun getTasks(): List<TaskEntity> = taskDao.getTasks()

    suspend fun saveTask(
        id: Long?,
        title: String,
        subjectName: String?,
        dueDate: String?
    ) {
        val now = System.currentTimeMillis()
        if (id == null) {
            taskDao.insertTask(
                TaskEntity(
                    title = title,
                    subjectName = subjectName,
                    dueDate = dueDate,
                    dueAt = null,
                    hasSpecificTime = false,
                    status = TaskStatus.TODO,
                    memo = null,
                    createdAt = now,
                    updatedAt = now
                )
            )
            return
        }

        val current = taskDao.getTask(id) ?: return
        taskDao.updateTask(
            current.copy(
                title = title,
                subjectName = subjectName,
                dueDate = dueDate,
                updatedAt = now
            )
        )
    }

    suspend fun setDone(id: Long, done: Boolean) {
        taskDao.updateStatus(
            id = id,
            status = if (done) TaskStatus.DONE else TaskStatus.TODO,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun deleteTask(id: Long) {
        taskDao.getTask(id)?.let { task ->
            taskDao.deleteTask(task)
        }
    }
}
