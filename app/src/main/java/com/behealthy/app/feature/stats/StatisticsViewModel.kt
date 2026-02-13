package com.behealthy.app.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.behealthy.app.core.database.dao.FitnessTaskDao
import com.behealthy.app.core.database.dao.MoodDao
import com.behealthy.app.core.repository.FitnessPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class DateRange(val label: String) {
    WEEK("最近一周"),
    TWO_WEEKS("最近两周"),
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
    val maxSteps: Int = 0,
    val stepGoalDays: Int = 0, // Days with steps >= 6000 (default goal)
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
    private val moodDao: MoodDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState(isLoading = true))
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    private val _selectedDateRange = MutableStateFlow(DateRange.WEEK)
    private val _customDateRange = MutableStateFlow<Pair<LocalDate, LocalDate>?>(null)

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

    private fun observeStats() {
        viewModelScope.launch {
            try {
                combine(
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
                        DateRange.TWO_WEEKS -> LocalDate.now().minusDays(13)
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
                        
                        // Manual Tasks Only (Include incomplete tasks if they have actual data)
                        val daysTasks = allTasks.filter { it.date == dateStr }
                        
                        // Fix: Only use actualCalories if present. If not, use workExerciseCalories ONLY if completed.
                        val taskCalories = daysTasks.sumOf { 
                            if (it.actualCalories > 0) it.actualCalories 
                            else if (it.isCompleted) it.workExerciseCalories 
                            else 0
                        }
                        val taskMinutes = daysTasks.sumOf { 
                            if (it.actualMinutes > 0) it.actualMinutes
                            else if (it.isCompleted) it.workExerciseMinutes
                            else 0
                        }
                        val taskSteps = daysTasks.sumOf { 
                            if (it.actualSteps > 0) it.actualSteps
                            else if (it.isCompleted) it.workExerciseSteps
                            else 0
                        }
                        
                        DailyStatItem(
                            date = date,
                            calories = taskCalories,
                            minutes = taskMinutes,
                            steps = taskSteps,
                            mood = uniqueMoods.find { it.date == dateStr }?.mood,
                            moodScore = getMoodScore(uniqueMoods.find { it.date == dateStr }?.mood)
                        )
                    }
                    
                    // Plan Stats (Global)
                    val planStats = plans.map { plan -> 
                        val planTasks = allTasks.filter { it.planId == plan.id }
                        val completedCount = planTasks.count { it.isCompleted }
                        val totalCalories = planTasks.sumOf { 
                            if (it.actualCalories > 0) it.actualCalories 
                            else if (it.isCompleted) it.workExerciseCalories
                            else 0
                        }
                        
                        PlanStatItem(
                            planName = plan.name,
                            totalTasks = planTasks.size,
                            completedTasks = completedCount,
                            pendingTasks = planTasks.size - completedCount,
                            totalCalories = totalCalories
                        )
                    }
                    
                    // Calculate Summary
                    val totalSteps = dailyStats.sumOf { it.steps }
                    val totalCalories = dailyStats.sumOf { it.calories }
                    val totalDuration = dailyStats.sumOf { it.minutes }
                    val daysWithData = dailyStats.count { it.steps > 0 || it.calories > 0 || it.minutes > 0 }
                    
                    val avgCalories = if (daysWithData > 0) totalCalories / daysWithData else 0
                    val avgDuration = if (daysWithData > 0) totalDuration / daysWithData else 0
                    val avgSteps = if (daysWithData > 0) totalSteps / daysWithData else 0
                    val maxSteps = dailyStats.maxOfOrNull { it.steps } ?: 0
                    val stepGoalDays = dailyStats.count { it.steps >= 6000 } // Assuming 6000 is a reasonable default goal
                    
                    // Analysis
                    val exerciseAnalysis = generateExerciseAnalysis(dailyStats)
                    val moodAnalysis = generateMoodAnalysis(dailyStats)

                    val moodMap = dailyStats.mapNotNull { it.mood }.groupingBy { it }.eachCount()
                    
                    StatisticsUiState(
                        selectedDateRange = dateRange,
                        totalExerciseDays = daysWithData,
                        totalCaloriesBurned = totalCalories,
                        totalDurationMinutes = totalDuration,
                        totalSteps = totalSteps,
                        avgCaloriesBurned = avgCalories,
                        avgDurationMinutes = avgDuration,
                        avgSteps = avgSteps,
                        maxSteps = maxSteps,
                        stepGoalDays = stepGoalDays,
                        totalPlans = plans.size,
                        activePlans = plans.count { it.isActive },
                        completedPlans = plans.size - plans.count { it.isActive },
                        planStats = planStats,
                        moodStats = moodMap,
                        dailyStats = dailyStats,
                        exerciseAnalysis = exerciseAnalysis,
                        moodAnalysis = moodAnalysis,
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    private fun generateExerciseAnalysis(stats: List<DailyStatItem>): String {
        if (stats.isEmpty()) return "暂无数据"
        val totalSteps = stats.sumOf { it.steps }
        val totalCal = stats.sumOf { it.calories }
        if (totalSteps == 0 && totalCal == 0) return "这段时间没有完成健身任务，请继续加油！"
        
        // Simple trend analysis
        val firstHalf = stats.take(stats.size / 2)
        val secondHalf = stats.takeLast(stats.size - stats.size / 2)
        val firstAvg = if (firstHalf.isNotEmpty()) firstHalf.sumOf { it.calories } / firstHalf.size else 0
        val secondAvg = if (secondHalf.isNotEmpty()) secondHalf.sumOf { it.calories } / secondHalf.size else 0
        
        return when {
            secondAvg > firstAvg * 1.1 -> "相比前期，您的计划执行度有明显提升，非常自律！💪"
            secondAvg < firstAvg * 0.9 -> "近期任务完成量有所下降，调整状态重新出发吧！🏃"
            else -> "您的健身习惯保持得比较稳定，不错哦！👍"
        }
    }
    
    private fun generateMoodAnalysis(stats: List<DailyStatItem>): String {
        val validMoods = stats.filter { it.mood != null }
        if (validMoods.isEmpty()) return "这段时间没有记录心情哦。"
        
        val scores = validMoods.map { it.moodScore }
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
