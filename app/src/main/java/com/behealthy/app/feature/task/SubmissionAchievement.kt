package com.behealthy.app.feature.task

data class SubmissionAchievement(
    val isSuccess: Boolean,
    val streakDays: Int,
    val newRecords: List<String> // e.g., "New Calorie Record!", "Longest Workout!"
)
