package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "homework_tasks")
data class HomeworkTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val priority: String, // Low, Medium, High
    val isCompleted: Boolean = false,
    val aiStudyPlan: String? = null // AI synthesized study plan / step-by-step
)

@Entity(tableName = "study_slots")
data class StudySlot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val title: String,
    val date: String, // e.g., "May 25, 2026"
    val timeRange: String, // e.g., "16:00 - 17:30"
    val focusItems: String, // AI recommended topics or custom
    val isCompleted: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tutorSubject: String, // e.g. "Math", "Science", "History", "English"
    val sender: String, // "user", "ai"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
