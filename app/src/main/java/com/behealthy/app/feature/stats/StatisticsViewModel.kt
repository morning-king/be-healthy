package com.behealthy.app.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.behealthy.app.core.database.dao.FitnessTaskDao
import com.behealthy.app.core.database.dao.MoodDao
import com.behealthy.app.core.repository.DailyActivityRepository
import com.behealthy.app.core.repository.FitnessPlanRepository
import com.behealthy.app.core.network.HealthConnectManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.roundToInt

enum class DateRange(val label: String) {
    WEEK("最近一周"),
    MONTH("最近一月"),
    THREE_MONTHS("最近三月"),
    SIX_MONTHS("最近半年"),
    YEAR("今年内"),
    CUSTOM("自定义")
}

data class PlanStatItem(
    val planName: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val pendingTasks: Int,
    val totalCalories: Int
)

data class DailyStatItem(
    val date: LocalDate,
    val calories: Int,
    val minutes: Int,
    val steps: Int,
    val mood: String?,
    val moodScore: Int // 1-5 scale
)

data class StatisticsUiState(
    val selectedDateRange: DateRange = DateRange.WEEK,
    val totalExerciseDays: Int = 0,
    val totalCaloriesBurned: Int = 0,
    val totalDurationMinutes: Int = 0,
    val totalSteps: Int = 0,
    val avgCaloriesBurned: Int = 0,
    val avgDurationMinutes: Int = 0,
    val avgSteps: Int = 0,
    val totalPlans: Int = 0,
    val activePlans: Int = 0,
    val completedPlans: Int = 0,
    val planStats: List<PlanStatItem> = emptyList(),
    val moodStats: Map<String, Int> = emptyMap(),
    val dailyStats: List<DailyStatItem> = emptyList(),
    val exerciseAnalysis: String = "",
    val moodAnalysis: String = "",
    val isMoodPieChart: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val planRepository: FitnessPlanRepository,
    private val taskDao: FitnessTaskDao,
    private val moodDao: MoodDao,
    private val dailyActivityRepository: DailyActivityRepository,
    private val workManager: androidx.work.WorkManager,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState(isLoading = true))
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    private val _selectedDateRange = MutableStateFlow(DateRange.WEEK)
    private val _customDateRange = MutableStateFlow<Pair<LocalDate, LocalDate>?>(null)
    
    private val _healthConnectStatus = MutableStateFlow<String>("")
    val healthConnectStatus: StateFlow<String> = _healthConnectStatus.asStateFlow()

    fun checkHealthConnectStatus() {
        viewModelScope.launch {
            val sdkStatus = healthConnectManager.getSdkStatus()
            val statusStr = StringBuilder()
            
            statusStr.append("SDK Status: ")
            when (sdkStatus) {
                androidx.health.connect.client.HealthConnectClient.SDK_AVAILABLE -> statusStr.append("Available\n")
                androidx.health.connect.client.HealthConnectClient.SDK_UNAVAILABLE -> statusStr.append("Unavailable\n")
                androidx.health.connect.client.HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> statusStr.append("Update Required\n")
            }
            
            val missing = healthConnectManager.getMissingPermissions()
            if (missing.isEmpty()) {
                statusStr.append("Permissions: All Granted\n")
            } else {
                statusStr.append("Missing Permissions:\n")
                missing.forEach { statusStr.append("- $it\n") }
            }
            
            _healthConnectStatus.value = statusStr.toString()
        }
    }
    
    fun clearHealthConnectStatus() {
        _healthConnectStatus.value = ""
    }

    fun syncData(onComplete: () -> Unit) {
        viewModelScope.launch {
            val syncData = androidx.work.Data.Builder()
                .putBoolean("full_sync", true)
                .build()

            val syncRequest = androidx.work.OneTimeWorkRequest.Builder(com.behealthy.app.core.worker.SyncWorker::class.java)
                .setInputData(syncData)
                .build()
            
            workManager.enqueue(syncRequest)
            
            workManager.getWorkInfoByIdFlow(syncRequest.id)
                .filter { it?.state?.isFinished == true }
                .first()
            
            // Add a small delay to ensure the loading animation is visible and feels responsive
            kotlinx.coroutines.delay(800)
            
            onComplete()
            refresh()
        }
    }

    private data class StatsIntermediate(
        val plans: List<com.behealthy.app.core.database.entity.FitnessPlanEntity>,
        val dailyStats: List<DailyStatItem>,
        val dateRange: DateRange,
        val customRange: Pair<LocalDate, LocalDate>?,
        val planStats: List<PlanStatItem>
    )

    init {
        observeStats()
    }
    
    fun setDateRange(range: DateRange) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        _selectedDateRange.value = range
    }

    fun setCustomDateRange(start: LocalDate, end: LocalDate) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        _customDateRange.value = start to end
        _selectedDateRange.value = DateRange.CUSTOM
    }

    fun toggleMoodChartType() {
        _uiState.value = _uiState.value.copy(isMoodPieChart = !_uiState.value.isMoodPieChart)
    }
    
    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    
    suspend fun hasHealthConnectPermissions(): Boolean {
        return healthConnectManager.hasPermissions()
    }

    fun getHealthConnectPermissions(): Set<String> {
        return healthConnectManager.getRequiredPermissions()
    }

    fun getHealthConnectSdkStatus(): Int {
        return healthConnectManager.getSdkStatus()
    }
    
    fun getHealthConnectPermissionIntent(): android.content.Intent? {
        return healthConnectManager.getPermissionRequestIntent()
    }

    private fun observeStats() {
        val localStatsFlow: Flow<StatsIntermediate> = combine(
            planRepository.allPlans,
            taskDao.getAllTasks(),
            moodDao.getAllMoods(),
            _selectedDateRange,
            _customDateRange
        ) { plans, allTasks, moods, dateRange, customRange ->
            // Deduplicate moods (keep latest by ID for each date)
            val uniqueMoods = moods.groupBy { it.date }
                .mapValues { (_, records) -> records.maxByOrNull { it.id }!! }
                .values.toList()

            // Filter Date Range
            val endDate = if (dateRange == DateRange.CUSTOM && customRange != null) customRange.second else LocalDate.now()
            val startDate = when (dateRange) {
                DateRange.WEEK -> LocalDate.now().minusDays(6)
                DateRange.MONTH -> LocalDate.now().minusMonths(1)
                DateRange.THREE_MONTHS -> LocalDate.now().minusMonths(3)
                DateRange.SIX_MONTHS -> LocalDate.now().minusMonths(6)
                DateRange.YEAR -> LocalDate.of(LocalDate.now().year, 1, 1)
                DateRange.CUSTOM -> customRange?.first ?: LocalDate.now().minusDays(6)
            }
            
            val daysCount = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
            val dates = (0 until daysCount).map { startDate.plusDays(it.toLong()) }
            
            val dailyStats = dates.map { date ->
                val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                
                // 1. Manual Tasks
                val daysTasks = allTasks.filter { it.date == dateStr && it.isCompleted }
                val taskCalories = daysTasks.sumOf { it.actualCalories.takeIf { c -> c > 0 } ?: (it.workExerciseCalories + it.restExerciseCalories) }
                val taskMinutes = daysTasks.sumOf { it.actualMinutes.takeIf { m -> m > 0 } ?: it.workExerciseMinutes }
                
                DailyStatItem(
                    date = date,
                    calories = taskCalories,
                    minutes = taskMinutes,
                    steps = 0,
                    mood = uniqueMoods.find { it.date == dateStr }?.mood,
                    moodScore = getMoodScore(uniqueMoods.find { it.date == dateStr }?.mood)
                )
            }
            
            // Plan Stats (Global)
            val planStats = plans.map { plan -> 
                val planTasks = allTasks.filter { it.planId == plan.id }
                val completedCount = planTasks.count { it.isCompleted }
                val totalCalories = planTasks.sumOf { 
                    it.actualCalories.takeIf { c -> c > 0 } ?: (it.workExerciseCalories + it.restExerciseCalories) 
                }
                
                PlanStatItem(
                    planName = plan.name,
                    totalTasks = planTasks.size,
                    completedTasks = completedCount,
                    pendingTasks = planTasks.size - completedCount,
                    totalCalories = totalCalories
                )
            }
            
            StatsIntermediate(plans, dailyStats, dateRange, customRange, planStats)
        }

        viewModelScope.launch {
            localStatsFlow.combine(dailyActivityRepository.getAllDailyActivities()) { intermediate, oppoActivities ->
                 val (plans, dailyStatsPartial, dateRange, customRange, planStats) = intermediate
                 
                 // Merge OPPO Data
                 val mergedDailyStats = dailyStatsPartial.map { stat ->
                     val dateStr = stat.date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                     val oppoData = oppoActivities.find { it.date == dateStr }
                     stat.copy(
                         steps = oppoData?.steps ?: 0,
                         calories = stat.calories + (oppoData?.calories ?: 0),
                         minutes = stat.minutes + (oppoData?.durationMinutes ?: 0)
                     )
                 }
                 
                 // Calculate Summary
                 val totalSteps = mergedDailyStats.sumOf { it.steps }
                 val totalCalories = mergedDailyStats.sumOf { it.calories }
                 val totalDuration = mergedDailyStats.sumOf { it.minutes }
                 val daysWithData = mergedDailyStats.count { it.steps > 0 || it.calories > 0 || it.minutes > 0 }
                 
                 val avgCalories = if (daysWithData > 0) totalCalories / daysWithData else 0
                 val avgDuration = if (daysWithData > 0) totalDuration / daysWithData else 0
                 
                 // Analysis
                 val exerciseAnalysis = generateExerciseAnalysis(mergedDailyStats)
                 val moodAnalysis = generateMoodAnalysis(mergedDailyStats)

                 val moodMap = mergedDailyStats.mapNotNull { it.mood }.groupingBy { it }.eachCount()
                 
                 StatisticsUiState(
                    selectedDateRange = dateRange,
                    totalExerciseDays = daysWithData,
                    totalCaloriesBurned = totalCalories,
                    totalDurationMinutes = totalDuration,
                    totalSteps = totalSteps,
                    avgCaloriesBurned = avgCalories,
                    avgDurationMinutes = avgDuration,
                    totalPlans = plans.size,
                    activePlans = plans.count { it.isActive },
                    completedPlans = plans.size - plans.count { it.isActive },
                    planStats = planStats,
                    moodStats = moodMap,
                    dailyStats = mergedDailyStats,
                    exerciseAnalysis = exerciseAnalysis,
                    moodAnalysis = moodAnalysis,
                    isLoading = false
                 )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    private fun generateExerciseAnalysis(stats: List<DailyStatItem>): String {
        if (stats.isEmpty()) return "暂无数据"
        val totalSteps = stats.sumOf { it.steps }
        val totalCal = stats.sumOf { it.calories }
        if (totalSteps == 0 && totalCal == 0) return "这段时间似乎没有运动记录，该动起来啦！"
        
        // Simple trend analysis
        val firstHalf = stats.take(stats.size / 2)
        val secondHalf = stats.takeLast(stats.size - stats.size / 2)
        val firstAvg = if (firstHalf.isNotEmpty()) firstHalf.sumOf { it.calories } / firstHalf.size else 0
        val secondAvg = if (secondHalf.isNotEmpty()) secondHalf.sumOf { it.calories } / secondHalf.size else 0
        
        return when {
            secondAvg > firstAvg * 1.1 -> "相比前期，您的运动量有明显提升，继续保持！💪"
            secondAvg < firstAvg * 0.9 -> "近期运动量有所下降，调整状态重新出发吧！🏃"
            else -> "您的运动习惯保持得比较稳定，不错哦！👍"
        }
    }
    
    private fun generateMoodAnalysis(stats: List<DailyStatItem>): String {
        val validMoods = stats.filter { it.mood != null }
        if (validMoods.isEmpty()) return "这段时间没有记录心情哦。"
        
        val scores = validMoods.map { it.moodScore }
        val avgScore = scores.average()
        val positiveCount = validMoods.count { it.moodScore >= 4 }
        val negativeCount = validMoods.count { it.moodScore <= 2 }
        
        return when {
            positiveCount > validMoods.size * 0.6 -> "这段时间您的心情整体非常阳光，充满正能量！🌟"
            negativeCount > validMoods.size * 0.4 -> "近期心情似乎有些低落，记得多关注自己的内心，适当放松。🍵"
            else -> "这段时间心情起伏正常，喜忧参半，这就是生活呀。🍃"
        }
    }

    private fun getMoodScore(mood: String?): Int {
        return when (mood) {
            "开心", "美滋滋", "心动", "激动", "兴奋", "享受" -> 5
            "平静", "小确幸", "冥想" -> 4
            "一般", "还行", "平淡" -> 3
            "难以描述", "懵圈" -> 2
            "难过", "焦虑", "气愤", "倒霉", "恐惧", "累", "颓废", "委屈", "生气", "孤独" -> 1
            else -> 0
        }
    }
}
