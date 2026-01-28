package com.behealthy.app.core.repository

import com.behealthy.app.core.database.dao.DailyActivityDao
import com.behealthy.app.core.database.entity.DailyActivityEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyActivityRepository @Inject constructor(
    private val dailyActivityDao: DailyActivityDao
) {
    fun getDailyActivity(date: String): Flow<DailyActivityEntity?> {
        return dailyActivityDao.getDailyActivity(date)
    }

    fun getAllDailyActivities(): Flow<List<DailyActivityEntity>> {
        return dailyActivityDao.getAllDailyActivities()
    }

    suspend fun saveDailyActivity(activity: DailyActivityEntity) {
        dailyActivityDao.insertDailyActivity(activity)
    }
}
