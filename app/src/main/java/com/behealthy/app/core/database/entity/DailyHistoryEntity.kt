package com.behealthy.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_history")
data class DailyHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val type: String, // "quote" or "poem"
    val itemId: Long
)
