package com.example.bubbledo.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.bubbledo.model.Task

@Composable
fun TaskBubble(task: Task, onUpdate: (Float, Float) -> Unit) {
    var position by remember { mutableStateOf(Offset(100f, 100f)) }
    val size by animateFloatAsState(targetValue = (task.importance * 10).toFloat())

    Box(
        modifier = Modifier
            .size(size.dp)
            .background(Color.Blue, shape = CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    position = Offset(position.x + dragAmount.x, position.y + dragAmount.y)
                    onUpdate(position.x, position.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = task.title, color = Color.White)
    }
}
