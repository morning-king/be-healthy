package com.behealthy.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.behealthy.app.core.database.entity.DailyHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyHistoryDao {
    @Query("SELECT * FROM daily_history WHERE type = :type AND date = :date LIMIT 1")
    suspend fun getHistoryForDate(type: String, date: String): DailyHistoryEntity?

    @Query("SELECT itemId FROM daily_history WHERE type = :type ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentItemIds(type: String, limit: Int): List<Long>

    @Query("SELECT * FROM daily_history WHERE type = :type ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentHistory(type: String, limit: Int): List<DailyHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: DailyHistoryEntity)
}
