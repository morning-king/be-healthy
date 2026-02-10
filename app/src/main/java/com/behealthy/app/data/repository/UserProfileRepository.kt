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
    val pageTransition: String = "Default"
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
            UserProfileSettings(themeStyle, backgroundAlpha, pageTransition)
        }
    ) { info, stats, settings ->
        UserProfile(
            nickname = info.nickname,
            birthday = info.birthday,
            note = info.note,
            avatarUri = info.avatarUri,
            totalWorkoutDays = stats.totalWorkoutDays,
            totalMoodRecords = stats.totalMoodRecords,
            currentStreak = stats.currentStreak,
            themeStyle = settings.themeStyle ?: "Default",
            backgroundAlpha = settings.backgroundAlpha ?: 0.3f,
            pageTransition = settings.pageTransition ?: "Default"
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
    
    private data class UserProfileSettings(
        val themeStyle: String?,
        val backgroundAlpha: Float?,
        val pageTransition: String?
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
}
