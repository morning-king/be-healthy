package com.behealthy.app.core.repository

import com.behealthy.app.core.database.dao.DailyActivityDao
import com.behealthy.app.core.database.entity.DailyActivityEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing daily activity data.
 *
 * Acts as the Single Source of Truth for daily activity data (steps, calories, etc.),
 * mediating between the local database and the rest of the application.
 */
@Singleton
class DailyActivityRepository @Inject constructor(
    private val dailyActivityDao: DailyActivityDao
) {
    /**
     * Observes the daily activity data for a specific date.
     *
     * @param date The date string in "YYYY-MM-DD" format.
     * @return A [Flow] emitting the [DailyActivityEntity] or null if not found.
     */
    fun getDailyActivity(date: String): Flow<DailyActivityEntity?> {
        return dailyActivityDao.getDailyActivity(date)
    }

    /**
     * Observes all recorded daily activities.
     *
     * @return A [Flow] emitting a list of all [DailyActivityEntity] records.
     */
    fun getAllDailyActivities(): Flow<List<DailyActivityEntity>> {
        return dailyActivityDao.getAllDailyActivities()
    }

    /**
     * Saves or updates a daily activity record.
     *
     * @param activity The [DailyActivityEntity] to save.
     */
    suspend fun saveDailyActivity(activity: DailyActivityEntity) {
        dailyActivityDao.insertDailyActivity(activity)
    }
}
