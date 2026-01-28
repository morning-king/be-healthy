package com.behealthy.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.behealthy.app.core.database.entity.FitnessPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessPlanDao {
    @Query("SELECT * FROM fitness_plans ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<FitnessPlanEntity>>

    @Query("SELECT * FROM fitness_plans WHERE id = :id")
    suspend fun getPlanById(id: Long): FitnessPlanEntity?

    @Query("SELECT * FROM fitness_plans WHERE isActive = 1")
    fun getActivePlans(): Flow<List<FitnessPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: FitnessPlanEntity): Long

    @Update
    suspend fun updatePlan(plan: FitnessPlanEntity)

    @Query("DELETE FROM fitness_plans WHERE id = :id")
    suspend fun deletePlanById(id: Long)
}
