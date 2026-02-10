package com.behealthy.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fitness_tasks",
    foreignKeys = [
        ForeignKey(
            entity = FitnessPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["planId", "date"], unique = true)]
)
data class FitnessTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val planId: Long,
    
    // Format: YYYY-MM-DD
    val date: String,
    
    // Diet
    val breakfastImage: String? = null,
    val breakfastCalories: Int = 0,
    val lunchImage: String? = null,
    val lunchCalories: Int = 0,
    val dinnerImage: String? = null,
    val dinnerCalories: Int = 0,
    
    // Work Day Exercise
    // Comma separated list of types e.g. "WALK,BILLIARDS"
    val workExerciseTypes: String = "", 
    val workExerciseMinutes: Int = 0,
    // Comma separated list of image paths
    val workExerciseImages: String = "", 
    val workExerciseSteps: Int = 0,
    val workExerciseCalories: Int = 0,
    
    // Rest Day Exercise
    // Comma separated list
    val restExerciseTypes: String = "",
    val restExerciseMinutes: Int = 0,
    val restExerciseCalories: Int = 0,
    val restExerciseImages: String = "",
    
    val isCompleted: Boolean = false,
    
    val note: String = "",
    
    // Actual Performance
    val actualMinutes: Int = 0,
    val actualSteps: Int = 0,
    val actualCalories: Int = 0,
    val actualDistanceMeters: Int = 0,
    val weight: Float = 0f,

    // Check-in Media (Comma separated paths)
    val checkInImages: String = "",
    val checkInVideos: String = "",
    
    val updatedAt: Long = System.currentTimeMillis()
)
