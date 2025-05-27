package com.example.bubbledo.repository

import android.content.Context
import com.example.bubbledo.local.TaskDatabase
import com.example.bubbledo.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(context: Context) {

    private val taskDao = TaskDatabase.getDatabase(context.applicationContext).taskDao()

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun insertOrUpdateTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun deleteTask(taskId: String) {
        taskDao.deleteTask(taskId)
    }
}
