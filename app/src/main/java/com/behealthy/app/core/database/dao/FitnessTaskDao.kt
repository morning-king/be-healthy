package com.behealthy.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.behealthy.app.core.database.entity.FitnessTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessTaskDao {
    @Query("SELECT * FROM fitness_tasks")
    fun getAllTasks(): Flow<List<FitnessTaskEntity>>

    @Query("SELECT * FROM fitness_tasks WHERE date = :date")
    fun getTasksByDate(date: String): Flow<List<FitnessTaskEntity>>

    @Query("SELECT * FROM fitness_tasks WHERE date >= :startDate AND date <= :endDate")
    fun getTasksBetweenDates(startDate: String, endDate: String): Flow<List<FitnessTaskEntity>>

    @Query("SELECT * FROM fitness_tasks WHERE planId = :planId")
    fun getTasksByPlanId(planId: Long): Flow<List<FitnessTaskEntity>>

    @Query("SELECT * FROM fitness_tasks WHERE date = :date AND planId = :planId LIMIT 1")
    suspend fun getTaskByDateAndPlan(date: String, planId: Long): FitnessTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: FitnessTaskEntity): Long

    @Update
    suspend fun updateTask(task: FitnessTaskEntity)
    
    // Statistics
    @Query("SELECT COUNT(*) FROM fitness_tasks WHERE planId = :planId")
    suspend fun getTotalTaskCount(planId: Long): Int

    @Query("SELECT COUNT(*) FROM fitness_tasks WHERE planId = :planId AND isCompleted = 1")
    suspend fun getCompletedTaskCount(planId: Long): Int

    @Query("SELECT * FROM fitness_tasks WHERE isCompleted = 1 ORDER BY date DESC")
    suspend fun getCompletedTasksHistory(): List<FitnessTaskEntity>
}
