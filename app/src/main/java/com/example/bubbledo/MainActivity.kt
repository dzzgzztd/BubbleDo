package com.example.bubbledo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.bubbledo.ui.screens.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BubbleDoApp()
        }
    }
}

@Composable
fun BubbleDoApp() {
    MaterialTheme {
        HomeScreen()  // Вызываем основной экран
    }
}
