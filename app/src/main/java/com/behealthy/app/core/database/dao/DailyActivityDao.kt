package com.behealthy.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.behealthy.app.core.database.entity.DailyActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyActivityDao {
    @Query("SELECT * FROM daily_activities WHERE date = :date")
    fun getDailyActivity(date: String): Flow<DailyActivityEntity?>

    @Query("SELECT * FROM daily_activities")
    fun getAllDailyActivities(): Flow<List<DailyActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyActivity(activity: DailyActivityEntity)
}
