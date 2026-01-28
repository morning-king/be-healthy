package com.behealthy.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_activities")
data class DailyActivityEntity(
    @PrimaryKey
    val date: String, // YYYY-MM-DD
    
    val steps: Int = 0,
    val calories: Int = 0,
    val distanceMeters: Int = 0,
    val durationMinutes: Int = 0,
    
    val updatedAt: Long = System.currentTimeMillis()
)
