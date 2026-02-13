package com.behealthy.app.feature.profile

data class ProfileUiState(
    val nickname: String? = null,
    val birthday: String? = null,
    val note: String? = null,
    val noteImageUri: String? = null,
    val avatarUri: String? = null,
    val totalWorkoutDays: Int = 0,
    val totalMoodRecords: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val positiveMoodCount: Int = 0,
    val negativeMoodCount: Int = 0,
    // 成就徽章
    val hasFirstWorkoutBadge: Boolean = false,
    val hasSevenDayStreakBadge: Boolean = false,
    val hasThirtyDayStreakBadge: Boolean = false,
    val hasHundredWorkoutsBadge: Boolean = false,
    val hasPositiveWeekBadge: Boolean = false,
    val hasMoodMonthBadge: Boolean = false,
    // 设置
    val birthdayReminderEnabled: Boolean = true,
    val avatarCropEnabled: Boolean = true,
    // 主题
    val themeStyle: String = "Default",
    val backgroundAlpha: Float = 0.3f,
    val pageTransition: String = "Default",
    // Zen Theme Settings
    val zenRotationEnabled: Boolean = true,
    val zenRotationSpeed: Float = 5f,
    val zenRotationDirection: String = "Clockwise",
    // Tech Theme Settings
    val techIntensity: String = "Standard",
    // General Settings
    val fontColorMode: String = "Auto"
)