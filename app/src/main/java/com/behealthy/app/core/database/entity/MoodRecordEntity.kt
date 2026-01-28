package com.behealthy.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_records")
data class MoodRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val mood: String, // Enum value: 开心, 美滋滋, etc.
    val note: String,
    val audioPath: String? = null,
    val audioDuration: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
