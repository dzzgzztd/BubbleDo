package com.example.bubbledo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bubbledo.R
import com.example.bubbledo.model.Task
import com.example.bubbledo.ui.components.BubbleLayoutCalculator
import com.example.bubbledo.ui.components.SplashEffect
import com.example.bubbledo.ui.components.TaskBubble
import com.example.bubbledo.viewmodel.TaskViewModel
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect

@Composable
fun HomeScreen(viewModel: TaskViewModel, onRequestGoogleSignIn: () -> Unit) {
    val tasks by viewModel.tasks.collectAsState()
    var showAddScreen by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var splashCenter by remember { mutableStateOf<Offset?>(null) }
    var splashRadius by remember { mutableStateOf<Float?>(null) }
    var deletedTask by remember { mutableStateOf<Task?>(null) }
    var showUndoSnackbar by remember { mutableStateOf(false) } 

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth.value

        if (showSettings) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { showSettings = false },
                onRequestGoogleSignIn = onRequestGoogleSignIn
            )
        } else if (showAddScreen || taskToEdit != null) {
            AddTaskScreen(
                viewModel = viewModel,
                taskToEdit = taskToEdit,
                onTaskAdded = {
                    showAddScreen = false
                    taskToEdit = null
                }
            )
        } else {
            val bubblePositions =
                BubbleLayoutCalculator.calculateBubblePositions(tasks, screenWidth)

            tasks.forEach { task ->
                val (position, radius) = bubblePositions[task.id] ?: Pair(
                    Offset(0f, 0f), 40f
                )

                TaskBubble(
                    task = task,
                    position = position,
                    radius = radius,
                    onClick = { taskToEdit = task },
                    onLongClick = {
                        splashCenter = position
                        splashRadius = radius
                        deletedTask = task
                        viewModel.deleteTask(task.id)
                        showUndoSnackbar = true
                    }
                )
            }

            FloatingActionButton(
                onClick = { showAddScreen = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("+")
            }

            IconButton(
                onClick = { showSettings = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.gear),
                    contentDescription = "Настройки",
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            if (splashCenter != null && splashRadius != null) {
                SplashEffect(
                    center = splashCenter!!,
                    radius = splashRadius!!,
                    onAnimationEnd = {
                        splashCenter = null
                        splashRadius = null
                    }
                )
            }
        }

        if (showUndoSnackbar && deletedTask != null) {
            androidx.compose.material3.Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(80.dp),
                action = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            deletedTask?.let { viewModel.addTask(it) }
                            showUndoSnackbar = false
                        }
                    ) {
                        Text(
                            "Отменить",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.inversePrimary
                        )
                    }
                }
            ) {
                Text("Задача выполнена!")
            }

            LaunchedEffect(showUndoSnackbar) {
                if (showUndoSnackbar) {
                    delay(5000)
                    showUndoSnackbar = false
                    deletedTask = null
                }
            }
        }
    }
}
