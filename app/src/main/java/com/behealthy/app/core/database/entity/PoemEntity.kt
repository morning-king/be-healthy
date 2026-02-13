package com.behealthy.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poems")
data class PoemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val author: String,
    val title: String,
    val dynasty: String,
    val category: String, // "favorite", "other"
    val tags: String // comma separated tags
)
