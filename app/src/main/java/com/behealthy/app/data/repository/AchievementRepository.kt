package com.behealthy.app.data.repository

import com.behealthy.app.data.local.AchievementDataSource
import com.behealthy.app.data.local.HealthDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val achievementDataSource: AchievementDataSource,
    private val healthDataSource: HealthDataSource
) {
    
    // 徽章状态
    val badges: Flow<Map<String, Boolean>> = achievementDataSource.getAllBadges()

    // 生日提醒状态
    val birthdayReminderEnabled: Flow<Boolean> = achievementDataSource.birthdayReminderEnabled

    // 头像裁剪状态
    val avatarCropEnabled: Flow<Boolean> = achievementDataSource.avatarCropEnabled
    
    // 健身成就检查
    suspend fun unlockBadge(badgeKey: String) {
        val key = when(badgeKey) {
            "first_workout" -> AchievementDataSource.BADGE_FIRST_WORKOUT
            "7_day_streak" -> AchievementDataSource.BADGE_7_DAY_STREAK
            "30_day_streak" -> AchievementDataSource.BADGE_30_DAY_STREAK
            "100_workouts" -> AchievementDataSource.BADGE_100_WORKOUTS
            "positive_week" -> AchievementDataSource.BADGE_POSITIVE_WEEK
            "mood_month" -> AchievementDataSource.BADGE_MOOD_MONTH
            else -> return
        }
        achievementDataSource.unlockBadge(key)
    }

    suspend fun checkWorkoutAchievements() {
        val workoutDays = healthDataSource.totalWorkoutDays.first()
        val currentStreak = healthDataSource.currentStreak.first()
        val bestStreak = healthDataSource.bestStreak.first()
        
        // 首次健身徽章
        if (workoutDays > 0) {
            achievementDataSource.unlockBadge(AchievementDataSource.BADGE_FIRST_WORKOUT)
        }
        
        // 连续7天徽章
        if (currentStreak >= 7) {
            achievementDataSource.unlockBadge(AchievementDataSource.BADGE_7_DAY_STREAK)
        }
        
        // 连续30天徽章
        if (currentStreak >= 30) {
            achievementDataSource.unlockBadge(AchievementDataSource.BADGE_30_DAY_STREAK)
        }
        
        // 100次健身徽章
        if (workoutDays >= 100) {
            achievementDataSource.unlockBadge(AchievementDataSource.BADGE_100_WORKOUTS)
        }
    }
    
    // 心情成就检查
    suspend fun checkMoodAchievements() {
        val totalRecords = healthDataSource.totalMoodRecords.first()
        val positiveCount = healthDataSource.positiveMoodCount.first()
        val negativeCount = healthDataSource.negativeMoodCount.first()
        
        // 积极一周徽章（连续7天好心情）
        if (positiveCount >= 7) {
            achievementDataSource.unlockBadge(AchievementDataSource.BADGE_POSITIVE_WEEK)
        }
        
        // 情绪管理月徽章（一个月内积极情绪占主导）
        if (totalRecords >= 30 && positiveCount > negativeCount * 2) {
            achievementDataSource.unlockBadge(AchievementDataSource.BADGE_MOOD_MONTH)
        }
    }
    
    // 生日提醒相关
    suspend fun shouldShowBirthdayReminder(currentYear: Int): Boolean {
        val enabled = achievementDataSource.birthdayReminderEnabled.first()
        if (!enabled) return false
        
        val lastYear = achievementDataSource.lastBirthdayReminderYear.first()
        return lastYear != currentYear.toString()
    }
    
    suspend fun markBirthdayReminderShown(year: Int) {
        achievementDataSource.setLastBirthdayReminderYear(year.toString())
    }
    
    // 头像裁剪设置
    suspend fun isAvatarCropEnabled(): Boolean {
        return achievementDataSource.avatarCropEnabled.first()
    }
    
    suspend fun setAvatarCropEnabled(enabled: Boolean) {
        achievementDataSource.setAvatarCropEnabled(enabled)
    }
    
    // 生日提醒设置
    suspend fun isBirthdayReminderEnabled(): Boolean {
        return achievementDataSource.birthdayReminderEnabled.first()
    }
    
    suspend fun setBirthdayReminderEnabled(enabled: Boolean) {
        achievementDataSource.setBirthdayReminderEnabled(enabled)
    }
    
    // 获取成就数据用于展示
    suspend fun getAchievementData(): AchievementData {
        val badges = achievementDataSource.getAllBadges().first()
        val workoutDays = healthDataSource.totalWorkoutDays.first()
        val currentStreak = healthDataSource.currentStreak.first()
        val bestStreak = healthDataSource.bestStreak.first()
        val totalMoodRecords = healthDataSource.totalMoodRecords.first()
        
        return AchievementData(
            badges = badges,
            totalWorkoutDays = workoutDays,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            totalMoodRecords = totalMoodRecords,
            avatarCropEnabled = achievementDataSource.avatarCropEnabled.first(),
            birthdayReminderEnabled = achievementDataSource.birthdayReminderEnabled.first()
        )
    }
}

data class AchievementData(
    val badges: Map<String, Boolean>,
    val totalWorkoutDays: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val totalMoodRecords: Int,
    val avatarCropEnabled: Boolean,
    val birthdayReminderEnabled: Boolean
)