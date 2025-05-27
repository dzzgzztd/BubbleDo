package com.example.bubbledo.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val importance: Int,  // 1-7 (размер пузыря)
    val urgency: Int,     // 1-7 (позиция на экране)
    val createdAt: Long = System.currentTimeMillis(),

    val deadline: Long? = null,       // timestamp в millis, nullable
    val reminderEnabled: Boolean = false // включены ли напоминания
)
