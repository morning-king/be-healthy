package com.behealthy.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fitness_plans")
data class FitnessPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    
    // Duration: MONTH, WEEK
    val durationType: String,
    val durationQuantity: Int = 1,
    
    val startDate: String, // YYYY-MM-DD
    val endDate: String,   // YYYY-MM-DD 
    
    val targetText: String,
    
    // Work Day Config
    val workDayDietEnabled: Boolean,
    val workDayExerciseEnabled: Boolean,
    val workDayExerciseMinutes: Int,
    val workDayExerciseSteps: Int,
    val workDayExerciseCalories: Int,
    
    // Rest Day Config
    val restDayDietEnabled: Boolean,
    val restDayExerciseEnabled: Boolean,
    val restDayExerciseMinutes: Int,
    val restDayExerciseCalories: Int,
    
    val note: String,
    
    val isActive: Boolean = true,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
