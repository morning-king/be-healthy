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

        return Room.databaseBuilder(
            context,
            BeHealthyDatabase::class.java,
            "behealthy_db"
        )
        .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
        .fallbackToDestructiveMigration()
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
}
