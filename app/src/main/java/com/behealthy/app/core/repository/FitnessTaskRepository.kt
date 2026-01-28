package com.behealthy.app.core.repository

import com.behealthy.app.core.database.dao.FitnessTaskDao
import com.behealthy.app.core.database.entity.FitnessTaskEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FitnessTaskRepository @Inject constructor(
    private val fitnessTaskDao: FitnessTaskDao
) {
    fun getAllTasks(): Flow<List<FitnessTaskEntity>> {
        return fitnessTaskDao.getAllTasks()
    }

    fun getTasksByDate(date: String): Flow<List<FitnessTaskEntity>> {
        return fitnessTaskDao.getTasksByDate(date)
    }

    fun getTasksBetweenDates(startDate: String, endDate: String): Flow<List<FitnessTaskEntity>> {
        return fitnessTaskDao.getTasksBetweenDates(startDate, endDate)
    }

    fun getTasksByPlanId(planId: Long): Flow<List<FitnessTaskEntity>> {
        return fitnessTaskDao.getTasksByPlanId(planId)
    }
    
    suspend fun getTaskByDateAndPlan(date: String, planId: Long): FitnessTaskEntity? {
        return fitnessTaskDao.getTaskByDateAndPlan(date, planId)
    }

    suspend fun createTask(task: FitnessTaskEntity): Long {
        return fitnessTaskDao.insertTask(task)
    }

    suspend fun updateTask(task: FitnessTaskEntity) {
        fitnessTaskDao.updateTask(task)
    }
    
    suspend fun getCompletionStats(planId: Long): Pair<Int, Int> {
        val total = fitnessTaskDao.getTotalTaskCount(planId)
        val completed = fitnessTaskDao.getCompletedTaskCount(planId)
        return Pair(completed, total)
    }
}
