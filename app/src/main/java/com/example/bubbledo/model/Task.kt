package com.example.bubbledo.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val importance: Int,  // 1-10 (размер пузыря)
    val urgency: Int,     // 1-10 (позиция на экране)
    val createdAt: Long = System.currentTimeMillis()
)
