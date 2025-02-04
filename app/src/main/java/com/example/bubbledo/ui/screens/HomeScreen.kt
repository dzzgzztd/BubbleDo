package com.example.bubbledo.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bubbledo.model.Task
import com.example.bubbledo.viewmodel.TaskViewModel

@Composable
fun HomeScreen(viewModel: TaskViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        tasks.forEach { task ->
            DraggableBubble(task, onUpdate = { dx, dy ->
                viewModel.updateTaskUrgency(task.id, dx, dy)
            })
        }
    }
}

@Composable
fun DraggableBubble(task: Task, onUpdate: (Float, Float) -> Unit) {
    var position by remember { mutableStateOf(Offset(100f, 100f)) }

    val size by animateFloatAsState(targetValue = (task.importance * 10).toFloat())

    Box(
        modifier = Modifier
            .offset(x = position.x.dp, y = position.y.dp)
            .size(size.dp)
            .background(Color.Blue, shape = CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    position = Offset(position.x + dragAmount.x, position.y + dragAmount.y)
                    onUpdate(position.x, position.y)
                }
            }
    ) {
        Text(
            text = task.title,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
