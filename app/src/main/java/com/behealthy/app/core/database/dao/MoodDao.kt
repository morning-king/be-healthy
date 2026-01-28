package com.behealthy.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.behealthy.app.core.database.entity.MoodRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_records ORDER BY date DESC")
    fun getAllMoods(): Flow<List<MoodRecordEntity>>

    @Query("SELECT * FROM mood_records WHERE date = :date LIMIT 1")
    suspend fun getMoodByDate(date: String): MoodRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodRecordEntity)

    @Query("SELECT * FROM mood_records WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getMoodsBetweenDates(startDate: String, endDate: String): Flow<List<MoodRecordEntity>>
}
