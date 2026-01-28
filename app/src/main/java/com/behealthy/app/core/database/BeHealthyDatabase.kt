package com.behealthy.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.behealthy.app.core.database.dao.DailyActivityDao
import com.behealthy.app.core.database.dao.FitnessPlanDao
import com.behealthy.app.core.database.dao.FitnessTaskDao
import com.behealthy.app.core.database.dao.MoodDao
import com.behealthy.app.core.database.entity.DailyActivityEntity
import com.behealthy.app.core.database.entity.FitnessPlanEntity
import com.behealthy.app.core.database.entity.FitnessTaskEntity
import com.behealthy.app.core.database.entity.MoodRecordEntity

@Database(
    entities = [
        FitnessPlanEntity::class,
        FitnessTaskEntity::class,
        MoodRecordEntity::class,
        DailyActivityEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class BeHealthyDatabase : RoomDatabase() {
    abstract fun fitnessPlanDao(): FitnessPlanDao
    abstract fun fitnessTaskDao(): FitnessTaskDao
    abstract fun moodDao(): MoodDao
    abstract fun dailyActivityDao(): DailyActivityDao
}
