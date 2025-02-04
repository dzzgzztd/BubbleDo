package com.example.bubbledo.repository

import com.example.bubbledo.model.Task

class TaskRepository {

    private val tasks = mutableListOf<Task>()

    // Получаем все задачи
    fun getAllTasks(): List<Task> {
        // Здесь будет логика загрузки данных
        return tasks
    }

    // Обновление срочности задачи в репозитории
    fun updateTaskUrgency(taskId: String, newUrgency: Int) {
        val taskIndex = tasks.indexOfFirst { it.id == taskId }
        if (taskIndex != -1) {
            tasks[taskIndex] = tasks[taskIndex].copy(urgency = newUrgency)
        }
    }
}
