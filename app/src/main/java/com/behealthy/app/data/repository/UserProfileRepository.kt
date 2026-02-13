package com.behealthy.app.data.repository

import com.behealthy.app.data.local.UserProfileDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

data class UserProfile(
    val nickname: String? = null,
    val birthday: String? = null,
    val note: String? = null,
    val noteImageUri: String? = null,
    val avatarUri: String? = null,
    val totalWorkoutDays: Int = 0,
    val totalMoodRecords: Int = 0,
    val currentStreak: Int = 0,
    val themeStyle: String = "Default",
    val backgroundAlpha: Float = 0.3f,
    val pageTransition: String = "Default",
    val zenRotationEnabled: Boolean = true,
    val zenRotationSpeed: Float = 5f,
    val zenRotationDirection: String = "Clockwise",
    val techIntensity: String = "Standard",
    val fontColorMode: String = "Auto"
)

@Singleton
class UserProfileRepository @Inject constructor(
    private val dataSource: UserProfileDataSource
) {
    
    val userProfile: Flow<UserProfile> = combine(
        combine(
            dataSource.nickname,
            dataSource.birthday,
            dataSource.note,
            dataSource.avatarUri
        ) { nickname, birthday, note, avatarUri ->
            UserProfileInfo(nickname, birthday, note, avatarUri)
        },
        combine(
            dataSource.totalWorkoutDays,
            dataSource.totalMoodRecords,
            dataSource.currentStreak
        ) { totalWorkoutDays, totalMoodRecords, currentStreak ->
            UserProfileStats(totalWorkoutDays, totalMoodRecords, currentStreak)
        },
        combine(
            dataSource.themeStyle,
            dataSource.backgroundAlpha,
            dataSource.pageTransition
        ) { themeStyle, backgroundAlpha, pageTransition ->
            BasicThemeSettings(themeStyle, backgroundAlpha, pageTransition)
        },
        combine(
            dataSource.zenRotationEnabled,
            dataSource.zenRotationSpeed,
            dataSource.zenRotationDirection
        ) { enabled, speed, direction ->
            ZenSettings(enabled, speed, direction)
        },
        combine(
            dataSource.techIntensity,
            dataSource.fontColorMode
        ) { techIntensity, fontColorMode ->
            AdvancedSettings(techIntensity, fontColorMode)
        }
    ) { info, stats, basicTheme, zen, advanced ->
        UserProfile(
            nickname = info.nickname,
            birthday = info.birthday,
            note = info.note,
            avatarUri = info.avatarUri,
            totalWorkoutDays = stats.totalWorkoutDays,
            totalMoodRecords = stats.totalMoodRecords,
            currentStreak = stats.currentStreak,
            themeStyle = basicTheme.themeStyle ?: "Default",
            backgroundAlpha = basicTheme.backgroundAlpha ?: 0.3f,
            pageTransition = basicTheme.pageTransition ?: "Default",
            zenRotationEnabled = zen.enabled,
            zenRotationSpeed = zen.speed,
            zenRotationDirection = zen.direction,
            techIntensity = advanced.techIntensity,
            fontColorMode = advanced.fontColorMode
        )
    }

    private data class UserProfileInfo(
        val nickname: String?,
        val birthday: String?,
        val note: String?,
        val avatarUri: String?
    )

    private data class UserProfileStats(
        val totalWorkoutDays: Int,
        val totalMoodRecords: Int,
        val currentStreak: Int
    )
    
    private data class BasicThemeSettings(
        val themeStyle: String?,
        val backgroundAlpha: Float?,
        val pageTransition: String?
    )
    
    private data class ZenSettings(
        val enabled: Boolean,
        val speed: Float,
        val direction: String
    )
    
    private data class AdvancedSettings(
        val techIntensity: String,
        val fontColorMode: String
    )
    
    suspend fun updateNickname(nickname: String) {
        dataSource.updateNickname(nickname)
    }
    
    suspend fun updateBirthday(birthday: String) {
        dataSource.updateBirthday(birthday)
    }
    
    suspend fun updateNote(note: String) {
        dataSource.updateNote(note)
    }

    suspend fun updateNoteImage(uri: String) {
        dataSource.updateNoteImageUri(uri)
    }
    
    suspend fun updateAvatar(uri: String) {
        dataSource.updateAvatarUri(uri)
    }
    
    suspend fun updateStats(totalWorkoutDays: Int, totalMoodRecords: Int, currentStreak: Int) {
        dataSource.updateStats(totalWorkoutDays, totalMoodRecords, currentStreak)
    }

    suspend fun updateThemeStyle(style: String) {
        dataSource.updateThemeStyle(style)
    }
    
    suspend fun updateBackgroundAlpha(alpha: Float) {
        dataSource.updateBackgroundAlpha(alpha)
    }

    suspend fun updatePageTransition(transition: String) {
        dataSource.updatePageTransition(transition)
    }
    
    suspend fun updateZenRotationEnabled(enabled: Boolean) {
        dataSource.updateZenRotationEnabled(enabled)
    }
    
    suspend fun updateZenRotationSpeed(speed: Float) {
        dataSource.updateZenRotationSpeed(speed)
    }
    
    suspend fun updateZenRotationDirection(direction: String) {
        dataSource.updateZenRotationDirection(direction)
    }
    
    suspend fun updateTechIntensity(intensity: String) {
        dataSource.updateTechIntensity(intensity)
    }
    
    suspend fun updateFontColorMode(mode: String) {
        dataSource.updateFontColorMode(mode)
    }
}
