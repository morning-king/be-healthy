package com.behealthy.app.core.repository

import com.behealthy.app.core.database.dao.FitnessPlanDao
import com.behealthy.app.core.database.entity.FitnessPlanEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing fitness plans.
 *
 * Handles CRUD operations for [FitnessPlanEntity] and encapsulates the business logic
 * for generating daily tasks based on plan configurations.
 */
@Singleton
class FitnessPlanRepository @Inject constructor(
    private val fitnessPlanDao: FitnessPlanDao,
    private val fitnessTaskDao: com.behealthy.app.core.database.dao.FitnessTaskDao
) {
    /** Flow of all fitness plans in the database. */
    val allPlans: Flow<List<FitnessPlanEntity>> = fitnessPlanDao.getAllPlans()
    
    /** Flow of only currently active fitness plans. */
    val activePlans: Flow<List<FitnessPlanEntity>> = fitnessPlanDao.getActivePlans()

    /**
     * Retrieves a specific fitness plan by its ID.
     *
     * @param id The unique identifier of the plan.
     * @return The [FitnessPlanEntity] if found, null otherwise.
     */
    suspend fun getPlanById(id: Long): FitnessPlanEntity? {
        return fitnessPlanDao.getPlanById(id)
    }

    /**
     * Creates a new fitness plan and automatically generates associated daily tasks.
     *
     * This method first inserts the plan into the database, then iterates through the
     * plan's duration (start date to end date) to create [FitnessTaskEntity] records
     * based on the work/rest day configurations.
     *
     * @param plan The [FitnessPlanEntity] to create.
     * @return The ID of the newly created plan.
     */
    suspend fun createPlan(plan: FitnessPlanEntity): Long {
        val planId = fitnessPlanDao.insertPlan(plan)
        generateTasksForPlan(plan.copy(id = planId))
        return planId
    }

    /**
     * Generates daily tasks for a given plan.
     *
     * Iterates from start date to end date. For each day:
     * 1. Determines if it's a work day (Mon-Fri) or rest day (Sat-Sun).
     * 2. Checks if diet or exercise is enabled for that day type.
     * 3. Creates a [FitnessTaskEntity] with appropriate targets if enabled.
     *
     * @param plan The plan for which to generate tasks.
     */
    private suspend fun generateTasksForPlan(plan: FitnessPlanEntity) {
        val startDate = LocalDate.parse(plan.startDate, DateTimeFormatter.ISO_LOCAL_DATE)
        val endDate = LocalDate.parse(plan.endDate, DateTimeFormatter.ISO_LOCAL_DATE)
        
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val dayOfWeek = currentDate.dayOfWeek
            val isWorkDay = dayOfWeek != java.time.DayOfWeek.SATURDAY && dayOfWeek != java.time.DayOfWeek.SUNDAY

            val dietEnabled = if (isWorkDay) plan.workDayDietEnabled else plan.restDayDietEnabled
            val exerciseEnabled = if (isWorkDay) plan.workDayExerciseEnabled else plan.restDayExerciseEnabled
            
            // Only create task if there is something to do
            if (dietEnabled || exerciseEnabled) {
                val task = com.behealthy.app.core.database.entity.FitnessTaskEntity(
                    planId = plan.id,
                    date = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    
                    // Exercise defaults based on config
                    workExerciseMinutes = if (isWorkDay) plan.workDayExerciseMinutes else 0,
                    workExerciseSteps = if (isWorkDay) plan.workDayExerciseSteps else 0,
                    workExerciseCalories = if (isWorkDay) plan.workDayExerciseCalories else 0,
                    
                    restExerciseMinutes = if (!isWorkDay) plan.restDayExerciseMinutes else 0,
                    restExerciseCalories = if (!isWorkDay) plan.restDayExerciseCalories else 0,
                    
                    // Initialize other fields as empty/default
                    workExerciseTypes = if (isWorkDay && exerciseEnabled) "WALK,BILLIARDS" else "",
                    note = ""
                )
                fitnessTaskDao.insertTask(task)
            }
            currentDate = currentDate.plusDays(1)
        }
    }

    /**
     * Updates an existing fitness plan.
     *
     * Note: This does NOT regenerate tasks. If plan dates or configs change,
     * existing tasks remain as is.
     *
     * @param plan The updated [FitnessPlanEntity].
     */
    suspend fun updatePlan(plan: FitnessPlanEntity) {
        fitnessPlanDao.updatePlan(plan)
    }

    /**
     * Deletes a fitness plan by its ID.
     *
     * @param id The ID of the plan to delete.
     */
    suspend fun deletePlan(id: Long) {
        fitnessPlanDao.deletePlanById(id)
    }
}
