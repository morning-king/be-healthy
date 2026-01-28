package com.behealthy.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

private val Context.achievementDataStore: DataStore<Preferences> by preferencesDataStore(name = "achievement_data")

@Singleton
class AchievementDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        // 徽章解锁状态
        val BADGE_FIRST_WORKOUT = booleanPreferencesKey("badge_first_workout")
        val BADGE_7_DAY_STREAK = booleanPreferencesKey("badge_7_day_streak")
        val BADGE_30_DAY_STREAK = booleanPreferencesKey("badge_30_day_streak")
        val BADGE_100_WORKOUTS = booleanPreferencesKey("badge_100_workouts")
        val BADGE_POSITIVE_WEEK = booleanPreferencesKey("badge_positive_week")
        val BADGE_MOOD_MONTH = booleanPreferencesKey("badge_mood_month")
        
        // 生日提醒
        val BIRTHDAY_REMINDER_ENABLED = booleanPreferencesKey("birthday_reminder_enabled")
        val LAST_BIRTHDAY_REMINDER_YEAR = stringPreferencesKey("last_birthday_reminder_year")
        
        // 头像裁剪
        val AVATAR_CROP_ENABLED = booleanPreferencesKey("avatar_crop_enabled")
        val LAST_AVATAR_UPDATE_TIME = stringPreferencesKey("last_avatar_update_time")
    }
    
    // 徽章状态
    val firstWorkoutBadge: Flow<Boolean> = context.achievementDataStore.data
        .map { preferences ->
            preferences[BADGE_FIRST_WORKOUT] ?: false
        }
    
    val sevenDayStreakBadge: Flow<Boolean> = context.achievementDataStore.data
        .map { preferences ->
            preferences[BADGE_7_DAY_STREAK] ?: false
        }
    
    val thirtyDayStreakBadge: Flow<Boolean> = context.achievementDataStore.data
        .map { preferences ->
            preferences[BADGE_30_DAY_STREAK] ?: false
        }
    
    val hundredWorkoutsBadge: Flow<Boolean> = context.achievementDataStore.data
        .map { preferences ->
            preferences[BADGE_100_WORKOUTS] ?: false
        }
    
    val positiveWeekBadge: Flow<Boolean> = context.achievementDataStore.data
        .map { preferences ->
            preferences[BADGE_POSITIVE_WEEK] ?: false
        }
    
    val moodMonthBadge: Flow<Boolean> = context.achievementDataStore.data
        .map { preferences ->
            preferences[BADGE_MOOD_MONTH] ?: false
        }
    
    // 生日提醒
    val birthdayReminderEnabled: Flow<Boolean> = context.achievementDataStore.data
        .map { preferences ->
            preferences[BIRTHDAY_REMINDER_ENABLED] ?: true
        }
    
    val lastBirthdayReminderYear: Flow<String?> = context.achievementDataStore.data
        .map { preferences ->
            preferences[LAST_BIRTHDAY_REMINDER_YEAR]
        }
    
    // 头像裁剪
    val avatarCropEnabled: Flow<Boolean> = context.achievementDataStore.data
        .map { preferences ->
            preferences[AVATAR_CROP_ENABLED] ?: true
        }
    
    val lastAvatarUpdateTime: Flow<String?> = context.achievementDataStore.data
        .map { preferences ->
            preferences[LAST_AVATAR_UPDATE_TIME]
        }
    
    // 解锁徽章
    suspend fun unlockBadge(badgeKey: Preferences.Key<Boolean>) {
        context.achievementDataStore.edit { preferences ->
            preferences[badgeKey] = true
        }
    }
    
    // 设置生日提醒
    suspend fun setBirthdayReminderEnabled(enabled: Boolean) {
        context.achievementDataStore.edit { preferences ->
            preferences[BIRTHDAY_REMINDER_ENABLED] = enabled
        }
    }
    
    suspend fun setLastBirthdayReminderYear(year: String) {
        context.achievementDataStore.edit { preferences ->
            preferences[LAST_BIRTHDAY_REMINDER_YEAR] = year
        }
    }
    
    // 设置头像裁剪
    suspend fun setAvatarCropEnabled(enabled: Boolean) {
        context.achievementDataStore.edit { preferences ->
            preferences[AVATAR_CROP_ENABLED] = enabled
        }
    }
    
    suspend fun setLastAvatarUpdateTime(time: String) {
        context.achievementDataStore.edit { preferences ->
            preferences[LAST_AVATAR_UPDATE_TIME] = time
        }
    }
    
    // 获取所有徽章状态
    fun getAllBadges(): Flow<Map<String, Boolean>> {
        return context.achievementDataStore.data.map { prefs ->
            mapOf(
                "first_workout" to (prefs[BADGE_FIRST_WORKOUT] ?: false),
                "7_day_streak" to (prefs[BADGE_7_DAY_STREAK] ?: false),
                "30_day_streak" to (prefs[BADGE_30_DAY_STREAK] ?: false),
                "100_workouts" to (prefs[BADGE_100_WORKOUTS] ?: false),
                "positive_week" to (prefs[BADGE_POSITIVE_WEEK] ?: false),
                "mood_month" to (prefs[BADGE_MOOD_MONTH] ?: false)
            )
        }
    }
}