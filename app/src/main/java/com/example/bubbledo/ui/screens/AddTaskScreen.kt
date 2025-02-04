package com.example.bubbledo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bubbledo.viewmodel.TaskViewModel
import com.example.bubbledo.model.Task

@Composable
fun AddTaskScreen(viewModel: TaskViewModel = viewModel(), onTaskAdded: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var importance by remember { mutableStateOf(5) }
    var urgency by remember { mutableStateOf(5) }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Название задачи") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth()
        )
        SliderWithLabel("Важность", importance) { importance = it }
        SliderWithLabel("Срочность", urgency) { urgency = it }
        Button(
            onClick = {
                if (title.isNotBlank()) {
                    viewModel.addTask(Task(title = title, description = description, importance = importance, urgency = urgency))
                    onTaskAdded()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить задачу")
        }
    }
}

@Composable
fun SliderWithLabel(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("$label: $value")
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 9,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
