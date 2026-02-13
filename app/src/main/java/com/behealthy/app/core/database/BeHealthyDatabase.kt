package com.behealthy.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.behealthy.app.core.database.dao.DailyActivityDao
import com.behealthy.app.core.database.dao.FitnessPlanDao
import com.behealthy.app.core.database.dao.FitnessTaskDao
import com.behealthy.app.core.database.dao.MoodDao
import com.behealthy.app.core.database.dao.HolidayDao
import com.behealthy.app.core.database.dao.QuoteDao
import com.behealthy.app.core.database.dao.PoemDao
import com.behealthy.app.core.database.dao.DailyHistoryDao
import com.behealthy.app.core.database.entity.DailyActivityEntity
import com.behealthy.app.core.database.entity.FitnessPlanEntity
import com.behealthy.app.core.database.entity.FitnessTaskEntity
import com.behealthy.app.core.database.entity.MoodRecordEntity
import com.behealthy.app.core.database.entity.HolidayEntity
import com.behealthy.app.core.database.entity.QuoteEntity
import com.behealthy.app.core.database.entity.PoemEntity
import com.behealthy.app.core.database.entity.DailyHistoryEntity

/**
 * The main Room Database definition for the application.
 *
 * Defines the database configuration, including:
 * - List of entities (tables).
 * - Database version.
 * - DAOs (Data Access Objects) for accessing the tables.
 */
@Database(
    entities = [
        FitnessPlanEntity::class,
        FitnessTaskEntity::class,
        MoodRecordEntity::class,
        DailyActivityEntity::class,
        HolidayEntity::class,
        QuoteEntity::class,
        PoemEntity::class,
        DailyHistoryEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class BeHealthyDatabase : RoomDatabase() {
    /** Returns the DAO for Fitness Plans. */
    abstract fun fitnessPlanDao(): FitnessPlanDao
    
    /** Returns the DAO for Fitness Tasks (Daily records). */
    abstract fun fitnessTaskDao(): FitnessTaskDao
    
    /** Returns the DAO for Mood Records. */
    abstract fun moodDao(): MoodDao
    
    /** Returns the DAO for Daily Activity sync data. */
    abstract fun dailyActivityDao(): DailyActivityDao
    
    /** Returns the DAO for Holiday/Calendar data. */
    abstract fun holidayDao(): HolidayDao

    /** Returns the DAO for Quotes. */
    abstract fun quoteDao(): QuoteDao

    /** Returns the DAO for Poems. */
    abstract fun poemDao(): PoemDao

    /** Returns the DAO for Daily History. */
    abstract fun dailyHistoryDao(): DailyHistoryDao
}
