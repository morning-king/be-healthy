package com.behealthy.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val source: String,
    val category: String, // "Taoism", "Buddhism", "Western", "Strategy", etc.
    val tags: String, // comma separated tags
    val translation: String? = null // Vernacular translation
)
