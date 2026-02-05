package com.behealthy.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holidays")
data class HolidayEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val name: String,
    val type: Int, // 1: Holiday, 2: Workday
    val wage: Int, // Multiplier
    val holiday: Boolean // Convenience flag
)
