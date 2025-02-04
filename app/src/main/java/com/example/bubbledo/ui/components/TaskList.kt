package com.example.bubbledo.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bubbledo.model.Task
import com.example.bubbledo.viewmodel.TaskViewModel

@Composable
fun TaskList(viewModel: TaskViewModel = viewModel()) {
    val tasks = viewModel.tasks.collectAsState().value

    Box(modifier = Modifier.fillMaxSize()) {
        tasks.forEach { task ->
            TaskBubble(task, onUpdate = { x, y ->
                viewModel.updateTaskUrgency(task.id, x, y)
            })
        }
    }
}
