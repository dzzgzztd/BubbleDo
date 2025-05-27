package com.example.bubbledo.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bubbledo.viewmodel.TaskViewModel


@Composable
fun SettingsScreen(
    viewModel: TaskViewModel,
    onBack: () -> Unit,
    onRequestGoogleSignIn: () -> Unit
) {
    val isSyncEnabled by viewModel.isSyncEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            FloatingActionButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Text("<")
            }

            Text(
                text = "Настройки",
                color = Color.Black,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        Column(modifier = Modifier.padding(8.dp)) {
            Surface(
                color = fieldColor,
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Синхронизация", modifier = Modifier.weight(1f))
                    Switch(
                        checked = isSyncEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                onRequestGoogleSignIn()
                            } else {
                                viewModel.setSyncEnabled(false)
                            }
                        }
                    )
                }
            }
        }
    }
}