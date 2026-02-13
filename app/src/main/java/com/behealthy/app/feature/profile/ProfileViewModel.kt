package com.behealthy.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.behealthy.app.data.repository.UserProfileRepository
import com.behealthy.app.data.repository.AchievementRepository
import com.behealthy.app.core.database.dao.FitnessTaskDao
import com.behealthy.app.core.database.dao.MoodDao
import com.behealthy.app.core.logger.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.behealthy.app.core.worker.SyncWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val achievementRepository: AchievementRepository,
    private val fitnessTaskDao: FitnessTaskDao,
    private val moodDao: MoodDao,
    private val workManager: WorkManager
) : ViewModel() {
    
    val uiState: StateFlow<ProfileUiState> = combine(
        userProfileRepository.userProfile,
        combine(
            fitnessTaskDao.getAllTasks(),
            moodDao.getAllMoods()
        ) { tasks, moods ->
            // Calculate stats from DB
            val completedTasks = tasks.filter { it.isCompleted }
            val workoutDays = completedTasks.map { it.date }.distinct().count()
            val moodRecords = moods.size
            
            // Streak Calculation
            val workoutDates = completedTasks.mapNotNull { 
                try {
                    LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE)
                } catch (e: Exception) { null }
            }.distinct().sorted()
            
            val currentStreak = calculateCurrentStreak(workoutDates)
            val bestStreak = calculateBestStreak(workoutDates) // Not storing best streak in DB yet, calculate dynamically
            
            val positiveCount = moods.count { it.mood in listOf("开心", "美滋滋", "小确幸", "激动", "平静", "享受", "心动", "冥想", "兴奋") }
            val negativeCount = moods.count { it.mood in listOf("难过", "气愤", "倒霉", "焦虑", "恐惧", "累", "颓废", "委屈", "孤独", "生气", "难以描述", "懵圈") }
            
            HealthStats(
                workoutDays = workoutDays,
                moodRecords = moodRecords,
                streak = currentStreak,
                bestStreak = bestStreak,
                positiveCount = positiveCount,
                negativeCount = negativeCount
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
        // Check for badge updates based on real stats
        checkBadges(healthStats)
        
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
            themeStyle = profile.themeStyle,
            backgroundAlpha = profile.backgroundAlpha
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState()
        )

    private fun calculateCurrentStreak(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        
        // If no workout today or yesterday, streak is 0
        if (!dates.contains(today) && !dates.contains(yesterday)) return 0
        
        var streak = 0
        var current = if (dates.contains(today)) today else yesterday
        
        while (dates.contains(current)) {
            streak++
            current = current.minusDays(1)
        }
        return streak
    }
    
    private fun calculateBestStreak(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        var maxStreak = 0
        var currentStreak = 0
        var prevDate: LocalDate? = null
        
        for (date in dates) {
            if (prevDate == null) {
                currentStreak = 1
            } else {
                if (ChronoUnit.DAYS.between(prevDate, date) == 1L) {
                    currentStreak++
                } else if (ChronoUnit.DAYS.between(prevDate, date) > 1L) {
                    currentStreak = 1
                }
            }
            if (currentStreak > maxStreak) maxStreak = currentStreak
            prevDate = date
        }
        return maxStreak
    }
    
    private fun checkBadges(stats: HealthStats) {
        viewModelScope.launch {
            if (stats.workoutDays >= 1) achievementRepository.unlockBadge("first_workout")
            if (stats.streak >= 7) achievementRepository.unlockBadge("7_day_streak")
            if (stats.streak >= 30) achievementRepository.unlockBadge("30_day_streak")
            if (stats.workoutDays >= 100) achievementRepository.unlockBadge("100_workouts")
            // Logic for other badges can be refined
        }
    }

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
    
    fun updateBackgroundAlpha(alpha: Float) {
        viewModelScope.launch {
            userProfileRepository.updateBackgroundAlpha(alpha)
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