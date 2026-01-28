package com.behealthy.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

private val Context.fitnessDataStore: DataStore<Preferences> by preferencesDataStore(name = "fitness_data")
private val Context.moodDataStore: DataStore<Preferences> by preferencesDataStore(name = "mood_data")

@Singleton
class HealthDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        // 健身数据键
        val TOTAL_WORKOUT_DAYS = intPreferencesKey("total_workout_days")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LAST_WORKOUT_DATE = stringPreferencesKey("last_workout_date")
        val BEST_STREAK = intPreferencesKey("best_streak")
        
        // 心情数据键
        val TOTAL_MOOD_RECORDS = intPreferencesKey("total_mood_records")
        val LAST_MOOD_DATE = stringPreferencesKey("last_mood_date")
        val POSITIVE_MOOD_COUNT = intPreferencesKey("positive_mood_count")
        val NEGATIVE_MOOD_COUNT = intPreferencesKey("negative_mood_count")
    }
    
    // 健身数据
    val totalWorkoutDays: Flow<Int> = context.fitnessDataStore.data
        .map { preferences ->
            preferences[TOTAL_WORKOUT_DAYS] ?: 0
        }
    
    val currentStreak: Flow<Int> = context.fitnessDataStore.data
        .map { preferences ->
            preferences[CURRENT_STREAK] ?: 0
        }
    
    val bestStreak: Flow<Int> = context.fitnessDataStore.data
        .map { preferences ->
            preferences[BEST_STREAK] ?: 0
        }
    
    val lastWorkoutDate: Flow<String?> = context.fitnessDataStore.data
        .map { preferences ->
            preferences[LAST_WORKOUT_DATE]
        }
    
    // 心情数据
    val totalMoodRecords: Flow<Int> = context.moodDataStore.data
        .map { preferences ->
            preferences[TOTAL_MOOD_RECORDS] ?: 0
        }
    
    val lastMoodDate: Flow<String?> = context.moodDataStore.data
        .map { preferences ->
            preferences[LAST_MOOD_DATE]
        }
    
    val positiveMoodCount: Flow<Int> = context.moodDataStore.data
        .map { preferences ->
            preferences[POSITIVE_MOOD_COUNT] ?: 0
        }
    
    val negativeMoodCount: Flow<Int> = context.moodDataStore.data
        .map { preferences ->
            preferences[NEGATIVE_MOOD_COUNT] ?: 0
        }
    
    // 更新健身数据
    suspend fun updateWorkoutStats(
        totalDays: Int,
        streak: Int,
        best: Int,
        lastDate: String
    ) {
        context.fitnessDataStore.edit { preferences ->
            preferences[TOTAL_WORKOUT_DAYS] = totalDays
            preferences[CURRENT_STREAK] = streak
            preferences[BEST_STREAK] = best
            preferences[LAST_WORKOUT_DATE] = lastDate
        }
    }
    
    // 更新心情数据
    suspend fun updateMoodStats(
        totalRecords: Int,
        positiveCount: Int,
        negativeCount: Int,
        lastDate: String
    ) {
        context.moodDataStore.edit { preferences ->
            preferences[TOTAL_MOOD_RECORDS] = totalRecords
            preferences[POSITIVE_MOOD_COUNT] = positiveCount
            preferences[NEGATIVE_MOOD_COUNT] = negativeCount
            preferences[LAST_MOOD_DATE] = lastDate
        }
    }
}
