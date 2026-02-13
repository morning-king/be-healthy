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
    val category: String, 
    val tags: String, // comma separated tags
    val translation: String? = null,
    val notes: String? = null,
    val appreciation: String? = null,
    val background: String? = null,
    val genre: String? = null // "WuJue", "QiJue", "WuLv", "QiLv", "CiPai"
)
