package com.example.bubbledo.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bubbledo.model.Task
import com.example.bubbledo.viewmodel.TaskViewModel
import java.util.UUID
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

val fieldColor = Color(0xFFE0D7FA)

@Composable
fun AddTaskScreen(
    viewModel: TaskViewModel,
    taskToEdit: Task? = null,
    onTaskAdded: () -> Unit
) {
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var importance by remember { mutableStateOf(taskToEdit?.importance ?: 4) }
    var urgency by remember { mutableStateOf(taskToEdit?.urgency ?: 4) }
    var deadline by remember { mutableStateOf(taskToEdit?.deadline) }
    var reminderEnabled by remember { mutableStateOf(taskToEdit?.reminderEnabled ?: false) }

    val context = LocalContext.current

    fun showDatePicker() {
        val calendar = Calendar.getInstance()
        deadline?.let { calendar.timeInMillis = it }

        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth, 6, 0, 0)
            deadline = cal.timeInMillis
        },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(modifier = Modifier.padding(16.dp)) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            FloatingActionButton(
                onClick = onTaskAdded,
                modifier = Modifier.size(48.dp)
            ) {
                Text("<")
            }

            Text(
                text = if (taskToEdit != null) "Редактирование задачи" else "Добавление задачи",
                color = Color.Black,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Название задачи") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = fieldColor,
                unfocusedContainerColor = fieldColor,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        SliderWithLabel("Важность", importance) { importance = it }
        SliderWithLabel("Срочность", urgency) { urgency = it }
        val isEdit = taskToEdit != null

        TextButton(onClick = { showDatePicker() }) {
            Text(text = deadline?.let {
                java.text.SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
            } ?: "Установить дедлайн")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = reminderEnabled,
                onCheckedChange = { reminderEnabled = it },
                enabled = deadline != null
            )
            Text("Включить напоминания")
        }

        Button(
            onClick = {
                if (title.isNotBlank()) {
                    val newTask = Task(
                        id = taskToEdit?.id ?: UUID.randomUUID().toString(),
                        title = title,
                        importance = importance,
                        urgency = urgency,
                        deadline = deadline,
                        reminderEnabled = reminderEnabled
                    )
                    if (isEdit) viewModel.updateTask(newTask)
                    else viewModel.addTask(newTask)
                    onTaskAdded()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isEdit) "Сохранить задачу" else "Добавить задачу")
        }

    }
}

@Composable
fun SliderWithLabel(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("$label: $value")
        var sliderValue by remember { mutableStateOf(value.toFloat()) }

        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = {
                val rounded = sliderValue.roundToInt().coerceIn(1, 7)
                sliderValue = rounded.toFloat()
                onValueChange(rounded)
            },
            valueRange = 1f..7f,
            steps = 5,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
