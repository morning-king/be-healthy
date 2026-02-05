package com.behealthy.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.behealthy.app.core.database.entity.HolidayEntity

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays WHERE date LIKE :year || '-' || :month || '-%'")
    suspend fun getHolidaysForMonth(year: String, month: String): List<HolidayEntity>
    
    @Query("SELECT * FROM holidays WHERE date LIKE :year || '-%'")
    suspend fun getHolidaysForYear(year: String): List<HolidayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolidays(holidays: List<HolidayEntity>)
}
