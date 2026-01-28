package com.behealthy.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.behealthy.app.data.repository.UserProfileRepository
import com.behealthy.app.data.repository.AchievementRepository
import com.behealthy.app.data.local.HealthDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.behealthy.app.core.worker.SyncWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val achievementRepository: AchievementRepository,
    private val healthDataSource: HealthDataSource,
    private val workManager: WorkManager
) : ViewModel() {
    
    val uiState: StateFlow<ProfileUiState> = combine(
        userProfileRepository.userProfile,
        combine(
            healthDataSource.totalWorkoutDays,
            healthDataSource.totalMoodRecords,
            healthDataSource.currentStreak,
            healthDataSource.bestStreak,
            healthDataSource.positiveMoodCount,
            healthDataSource.negativeMoodCount
        ) { stats ->
            HealthStats(
                stats[0] as Int, stats[1] as Int, stats[2] as Int, 
                stats[3] as Int, stats[4] as Int, stats[5] as Int
            )
        },
        combine(
            achievementRepository.badges,
            achievementRepository.birthdayReminderEnabled,
            achievementRepository.avatarCropEnabled
        ) { badges, birthdayReminder, avatarCrop ->
            AchievementStats(badges, birthdayReminder, avatarCrop)
        }
    ) { profile, healthStats, achievementStats ->
        ProfileUiState(
            nickname = profile.nickname,
            birthday = profile.birthday,
            note = profile.note,
            avatarUri = profile.avatarUri,
            totalWorkoutDays = healthStats.workoutDays,
            totalMoodRecords = healthStats.moodRecords,
            currentStreak = healthStats.streak,
            bestStreak = healthStats.bestStreak,
            positiveMoodCount = healthStats.positiveCount,
            negativeMoodCount = healthStats.negativeCount,
            hasFirstWorkoutBadge = achievementStats.badges["first_workout"] ?: false,
            hasSevenDayStreakBadge = achievementStats.badges["7_day_streak"] ?: false,
            hasThirtyDayStreakBadge = achievementStats.badges["30_day_streak"] ?: false,
            hasHundredWorkoutsBadge = achievementStats.badges["100_workouts"] ?: false,
            hasPositiveWeekBadge = achievementStats.badges["positive_week"] ?: false,
            hasMoodMonthBadge = achievementStats.badges["mood_month"] ?: false,
            birthdayReminderEnabled = achievementStats.birthdayReminder,
            avatarCropEnabled = achievementStats.avatarCrop,
            themeStyle = profile.themeStyle
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState()
        )

    private data class HealthStats(
        val workoutDays: Int,
        val moodRecords: Int,
        val streak: Int,
        val bestStreak: Int,
        val positiveCount: Int,
        val negativeCount: Int
    )

    private data class AchievementStats(
        val badges: Map<String, Boolean>,
        val birthdayReminder: Boolean,
        val avatarCrop: Boolean
    )
    
    fun updateNickname(nickname: String) {
        viewModelScope.launch {
            userProfileRepository.updateNickname(nickname)
        }
    }
    
    fun updateBirthday(birthday: String) {
        viewModelScope.launch {
            userProfileRepository.updateBirthday(birthday)
        }
    }
    
    fun updateNote(note: String) {
        viewModelScope.launch {
            userProfileRepository.updateNote(note)
        }
    }
    
    fun triggerSync() {
        val syncRequest = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .build()
        workManager.enqueue(syncRequest)
        com.behealthy.app.core.logger.AppLogger.log("ProfileViewModel", "Manual sync triggered by user")
    }

    fun updateNoteImage(uri: String) {
        viewModelScope.launch {
            userProfileRepository.updateNoteImage(uri)
        }
    }
    
    fun updateAvatar(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            // Copy to internal storage to persist (Fix Item 21)
            val internalPath = copyUriToInternalStorage(context, uri, "avatar")
            internalPath?.let {
                userProfileRepository.updateAvatar(it)
            }
        }
    }

    private suspend fun copyUriToInternalStorage(context: android.content.Context, uri: android.net.Uri, type: String): String? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                val fileName = "${type}_${java.util.UUID.randomUUID()}.jpg"
                val file = java.io.File(context.filesDir, fileName)
                val outputStream = java.io.FileOutputStream(file)
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    fun setBirthdayReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            achievementRepository.setBirthdayReminderEnabled(enabled)
        }
    }
    
    fun setAvatarCropEnabled(enabled: Boolean) {
        viewModelScope.launch {
            achievementRepository.setAvatarCropEnabled(enabled)
        }
    }
    
    fun updateThemeStyle(style: String) {
        viewModelScope.launch {
            userProfileRepository.updateThemeStyle(style)
        }
    }
    
    fun checkBirthdayReminder(birthday: LocalDate) {
        viewModelScope.launch {
            val currentYear = LocalDate.now().year
            if (achievementRepository.shouldShowBirthdayReminder(currentYear)) {
                // 这里可以触发生日提醒通知
                // 暂时只记录日志
                println("生日提醒：今天是${birthday.monthValue}月${birthday.dayOfMonth}日，生日快乐！")
                achievementRepository.markBirthdayReminderShown(currentYear)
            }
        }
    }
}