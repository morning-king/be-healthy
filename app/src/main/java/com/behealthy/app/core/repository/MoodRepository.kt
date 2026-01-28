package com.behealthy.app.core.repository

import com.behealthy.app.core.database.dao.MoodDao
import com.behealthy.app.core.database.entity.MoodRecordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoodRepository @Inject constructor(
    private val moodDao: MoodDao
) {
    fun getAllMoods(): Flow<List<MoodRecordEntity>> = moodDao.getAllMoods()

    fun getMoodsBetween(startDate: String, endDate: String): Flow<List<MoodRecordEntity>> =
        moodDao.getMoodsBetweenDates(startDate, endDate)

    suspend fun saveMood(mood: MoodRecordEntity) {
        val existing = moodDao.getMoodByDate(mood.date)
        val moodToSave = if (existing != null) {
            mood.copy(id = existing.id)
        } else {
            mood
        }
        moodDao.insertMood(moodToSave)
    }
}
