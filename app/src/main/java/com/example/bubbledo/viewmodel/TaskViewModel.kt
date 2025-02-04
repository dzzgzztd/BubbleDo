package com.example.bubbledo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubbledo.model.Task
import com.example.bubbledo.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(private val taskRepository: TaskRepository = TaskRepository()) : ViewModel() {

    // StateFlow для хранения списка задач
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    init {
        // Загружаем задачи при инициализации
        loadTasks()
    }

    fun addTask(task: Task) {
        _tasks.value = _tasks.value + task
    }

    // Загрузка задач из репозитория
    private fun loadTasks() {
        viewModelScope.launch {
            _tasks.value = taskRepository.getAllTasks()
        }
    }

    // Обновление срочности задачи
    fun updateTaskUrgency(taskId: String, dx: Float, dy: Float) {
        val updatedTasks = _tasks.value.map { task ->
            if (task.id == taskId) {
                val newUrgency = (task.urgency + dy).toInt().coerceIn(1, 10) // Ограничиваем от 1 до 10
                task.copy(urgency = newUrgency)
            } else {
                task
            }
        }

        // Обновляем состояние
        _tasks.value = updatedTasks

        // Сохраняем обновление в репозитории
        viewModelScope.launch {
            taskRepository.updateTaskUrgency(taskId, updatedTasks.first { it.id == taskId }.urgency)
        }
    }
}
