package com.behealthy.app.di

import android.content.Context
import androidx.room.Room
import com.behealthy.app.core.database.BeHealthyDatabase
import com.behealthy.app.core.database.dao.FitnessPlanDao
import com.behealthy.app.core.database.dao.FitnessTaskDao
import com.behealthy.app.core.database.dao.DailyActivityDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.behealthy.app.core.database.dao.HolidayDao
import com.behealthy.app.core.database.dao.QuoteDao
import com.behealthy.app.core.database.dao.PoemDao
import com.behealthy.app.core.database.dao.DailyHistoryDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BeHealthyDatabase {
        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE fitness_tasks ADD COLUMN actualDistanceMeters INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE mood_records ADD COLUMN audioPath TEXT")
                database.execSQL("ALTER TABLE mood_records ADD COLUMN audioDuration INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `daily_activities` (`date` TEXT NOT NULL, `steps` INTEGER NOT NULL, `calories` INTEGER NOT NULL, `distanceMeters` INTEGER NOT NULL, `durationMinutes` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`date`))")
            }
        }

        val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `holidays` (`date` TEXT NOT NULL, `name` TEXT NOT NULL, `type` INTEGER NOT NULL, `wage` INTEGER NOT NULL, `holiday` INTEGER NOT NULL, PRIMARY KEY(`date`))")
            }
        }

        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `quotes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `content` TEXT NOT NULL, `source` TEXT NOT NULL, `category` TEXT NOT NULL, `tags` TEXT NOT NULL)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `poems` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `content` TEXT NOT NULL, `author` TEXT NOT NULL, `title` TEXT NOT NULL, `dynasty` TEXT NOT NULL, `category` TEXT NOT NULL, `tags` TEXT NOT NULL)")
            }
        }

        val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE quotes ADD COLUMN translation TEXT")
                database.execSQL("ALTER TABLE poems ADD COLUMN translation TEXT")
                database.execSQL("ALTER TABLE poems ADD COLUMN notes TEXT")
                database.execSQL("ALTER TABLE poems ADD COLUMN appreciation TEXT")
                database.execSQL("ALTER TABLE poems ADD COLUMN background TEXT")
                database.execSQL("ALTER TABLE poems ADD COLUMN genre TEXT")
                database.execSQL("CREATE TABLE IF NOT EXISTS `daily_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `type` TEXT NOT NULL, `itemId` INTEGER NOT NULL)")
            }
        }

        return Room.databaseBuilder(
            context,
            BeHealthyDatabase::class.java,
            "behealthy_db"
        )
        .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
        // .fallbackToDestructiveMigration() // Disabled to prevent data loss
        .build()
    }

    @Provides
    fun provideFitnessPlanDao(database: BeHealthyDatabase): FitnessPlanDao {
        return database.fitnessPlanDao()
    }

    @Provides
    fun provideFitnessTaskDao(database: BeHealthyDatabase): FitnessTaskDao {
        return database.fitnessTaskDao()
    }

    @Provides
    fun provideMoodDao(database: BeHealthyDatabase): com.behealthy.app.core.database.dao.MoodDao {
        return database.moodDao()
    }

    @Provides
    fun provideDailyActivityDao(database: BeHealthyDatabase): DailyActivityDao {
        return database.dailyActivityDao()
    }

    @Provides
    fun provideHolidayDao(database: BeHealthyDatabase): HolidayDao {
        return database.holidayDao()
    }

    @Provides
    fun provideQuoteDao(database: BeHealthyDatabase): QuoteDao {
        return database.quoteDao()
    }

    @Provides
    fun providePoemDao(database: BeHealthyDatabase): PoemDao {
        return database.poemDao()
    }

    @Provides
    fun provideDailyHistoryDao(database: BeHealthyDatabase): DailyHistoryDao {
        return database.dailyHistoryDao()
    }
}
