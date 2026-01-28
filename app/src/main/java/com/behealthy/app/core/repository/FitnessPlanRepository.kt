package com.behealthy.app.core.repository

import com.behealthy.app.core.database.dao.FitnessPlanDao
import com.behealthy.app.core.database.entity.FitnessPlanEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FitnessPlanRepository @Inject constructor(
    private val fitnessPlanDao: FitnessPlanDao,
    private val fitnessTaskDao: com.behealthy.app.core.database.dao.FitnessTaskDao
) {
    val allPlans: Flow<List<FitnessPlanEntity>> = fitnessPlanDao.getAllPlans()
    val activePlans: Flow<List<FitnessPlanEntity>> = fitnessPlanDao.getActivePlans()

    suspend fun getPlanById(id: Long): FitnessPlanEntity? {
        return fitnessPlanDao.getPlanById(id)
    }

    suspend fun createPlan(plan: FitnessPlanEntity): Long {
        val planId = fitnessPlanDao.insertPlan(plan)
        generateTasksForPlan(plan.copy(id = planId))
        return planId
    }

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

    suspend fun updatePlan(plan: FitnessPlanEntity) {
        fitnessPlanDao.updatePlan(plan)
    }

    suspend fun deletePlan(id: Long) {
        fitnessPlanDao.deletePlanById(id)
    }
}
