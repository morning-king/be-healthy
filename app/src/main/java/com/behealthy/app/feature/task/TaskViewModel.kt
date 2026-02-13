package com.behealthy.app.feature.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.behealthy.app.core.database.entity.FitnessTaskEntity
import com.behealthy.app.core.repository.FitnessPlanRepository
import com.behealthy.app.core.repository.FitnessTaskRepository
import com.behealthy.app.core.repository.SportsDataRepository
import com.behealthy.app.core.repository.DailyActivityRepository
import com.behealthy.app.core.database.entity.DailyActivityEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

import com.behealthy.app.core.repository.SportsData

import com.behealthy.app.core.network.HolidayDetail
import com.behealthy.app.data.repository.HolidayRepository
import com.behealthy.app.core.repository.ContentRepository
import com.behealthy.app.core.database.entity.QuoteEntity
import com.behealthy.app.core.database.entity.PoemEntity


@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel @Inject constructor(
    private val taskRepository: FitnessTaskRepository,
    private val planRepository: FitnessPlanRepository,
    private val sportsDataRepository: SportsDataRepository,
    private val dailyActivityRepository: DailyActivityRepository,
    private val weatherRepository: com.behealthy.app.core.repository.WeatherRepository,
    private val holidayRepository: HolidayRepository,
    private val contentRepository: ContentRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            contentRepository.initializeDataIfNeeded()
        }
    }

    val currentSportsData = sportsDataRepository.currentSportsData
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SportsData(0, 0, 0, 0))

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()
    
    val holidaysForCurrentYear = _currentMonth.flatMapLatest { month ->
        flow {
            emit(holidayRepository.getHolidaysForYear(month.year))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    
    val weatherForSelectedDate = _selectedDate.flatMapLatest { date ->
        weatherRepository.getWeatherForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val weatherForCurrentMonth = _currentMonth.flatMapLatest { month ->
        flow {
            emit(weatherRepository.getWeatherForMonth(month.year, month.monthValue))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _refreshTrigger = MutableStateFlow(0)
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()
    
    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasksForSelectedDate: StateFlow<List<FitnessTaskEntity>> = combine(_selectedDate, _refreshTrigger) { date, _ ->
        date
    }.flatMapLatest { date ->
            taskRepository.getTasksByDate(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyActivityForSelectedDate: StateFlow<DailyActivityEntity?> = _selectedDate
        .flatMapLatest { date ->
            dailyActivityRepository.getDailyActivity(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    // Daily Content Logic
    private val _dailyQuoteState = MutableStateFlow<QuoteEntity?>(null)
    val dailyQuoteState: StateFlow<QuoteEntity?> = _dailyQuoteState.asStateFlow()

    private val _dailyPoemState = MutableStateFlow<PoemEntity?>(null)
    val dailyPoemState: StateFlow<PoemEntity?> = _dailyPoemState.asStateFlow()

    private var lastLoadedDate: LocalDate? = null

    init {
        viewModelScope.launch {
            _selectedDate.collect { date ->
                if (lastLoadedDate != date) {
                    loadDailyContent(date)
                    lastLoadedDate = date
                }
            }
        }
        
        // Trigger generation of tasks for today if needed
        checkAndGenerateTasks(LocalDate.now())
    }

    private suspend fun loadDailyContent(date: LocalDate, forceRefreshQuote: Boolean = false, forceRefreshPoem: Boolean = false) {
        // Load Quote
        if (forceRefreshQuote || _dailyQuoteState.value == null || lastLoadedDate != date) {
             val quote = contentRepository.getDailyQuote(date, forceRefresh = forceRefreshQuote)
             _dailyQuoteState.value = quote
        }
        
        // Load Poem
        if (forceRefreshPoem || _dailyPoemState.value == null || lastLoadedDate != date) {
            val poem = contentRepository.getDailyPoem(date, forceRefresh = forceRefreshPoem)
            _dailyPoemState.value = poem
        }
    }
    
    fun refreshDailyQuote() {
        viewModelScope.launch {
            loadDailyContent(_selectedDate.value, forceRefreshQuote = true)
        }
    }

    fun refreshDailyPoem() {
        viewModelScope.launch {
            loadDailyContent(_selectedDate.value, forceRefreshPoem = true)
        }
    }

    fun forceRefreshSportsData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Force refresh today's sports data
                val today = LocalDate.now()
                val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
                
                // Get fresh data from repository
                val freshData = sportsDataRepository.currentSportsData.first()
                
                // Update tasks with fresh data
                val tasks = taskRepository.getTasksByDate(todayStr).first()
                tasks.forEach { task ->
                    if (task.actualSteps != freshData.steps ||
                        task.actualCalories != freshData.calories ||
                        task.actualMinutes != freshData.durationMinutes ||
                        task.actualDistanceMeters != freshData.distanceMeters) {
                            
                        val updatedTask = task.copy(
                            actualSteps = freshData.steps,
                            actualCalories = freshData.calories,
                            actualMinutes = freshData.durationMinutes,
                            actualDistanceMeters = freshData.distanceMeters
                        )
                        taskRepository.updateTask(updatedTask)
                    }
                }
                
                // Also trigger a manual sync if needed
                android.util.Log.d("TaskViewModel", "Force refreshed sports data: $freshData")
                kotlinx.coroutines.delay(500) // Ensure loading is visible
            } catch (e: Exception) {
                android.util.Log.e("TaskViewModel", "Error force refreshing sports data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }


    /*
    private fun startSyncingSportsData() {
        viewModelScope.launch {
            try {
                sportsDataRepository.currentSportsData.collect { sportsData ->
                    val today = LocalDate.now()
                    val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    try {
                        val tasks = taskRepository.getTasksByDate(todayStr).first()
                        
                        tasks.forEach { task ->
                            if (task.actualSteps != sportsData.steps ||
                                task.actualCalories != sportsData.calories ||
                                task.actualMinutes != sportsData.durationMinutes ||
                                task.actualDistanceMeters != sportsData.distanceMeters) {
                                    
                                val updatedTask = task.copy(
                                    actualSteps = sportsData.steps,
                                    actualCalories = sportsData.calories,
                                    actualMinutes = sportsData.durationMinutes,
                                    actualDistanceMeters = sportsData.distanceMeters
                                )
                                taskRepository.updateTask(updatedTask)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    */

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasksForCurrentMonth: StateFlow<List<FitnessTaskEntity>> = combine(_currentMonth, _refreshTrigger) { month: YearMonth, _: Int ->
        month
    }.flatMapLatest { month ->
            val start = month.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
            val end = month.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE)
            taskRepository.getTasksBetweenDates(start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        checkAndGenerateTasks(date)
    }

    fun updateMonth(month: YearMonth) {
        _currentMonth.value = month
    }

    fun toggleTaskCompletion(task: FitnessTaskEntity, isCompleted: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            taskRepository.updateTask(task.copy(isCompleted = isCompleted))
            _refreshTrigger.value += 1
            kotlinx.coroutines.delay(500) // Show loading animation
            _isLoading.value = false
        }
    }

    data class SubmissionAchievement(
        val isSuccess: Boolean,
        val streakDays: Int,
        val newRecords: List<String> // e.g., "New Calorie Record!", "Longest Workout!"
    )

    private val _submissionAchievement = kotlinx.coroutines.flow.MutableSharedFlow<SubmissionAchievement>()
    val submissionAchievement = _submissionAchievement.asSharedFlow()

    fun saveAndCompleteTask(task: FitnessTaskEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // 1. Save Task as Completed
            val completedTask = task.copy(isCompleted = true)
            taskRepository.updateTask(completedTask)
            
            // 2. Analyze History for Gamification
            val history = taskRepository.getCompletedTasksHistory()
            
            // A. Calculate Streak
            // Sort unique dates descending
            val uniqueDates = history.map { it.date }.distinct().sortedDescending()
            var streak = 0
            if (uniqueDates.isNotEmpty()) {
                val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val taskDateStr = task.date
                
                // Start checking from the task date (or today if it's recent)
                // Actually, streak implies consecutive days ending TODAY or YESTERDAY.
                // But for this specific task submission, if I fill in yesterday's task, does it count?
                // Let's count consecutive days backwards from the latest completed date.
                
                var currentDate = LocalDate.parse(uniqueDates[0], DateTimeFormatter.ISO_LOCAL_DATE)
                streak = 1
                
                for (i in 1 until uniqueDates.size) {
                    val prevDate = LocalDate.parse(uniqueDates[i], DateTimeFormatter.ISO_LOCAL_DATE)
                    if (prevDate.plusDays(1) == currentDate) {
                        streak++
                        currentDate = prevDate
                    } else {
                        break
                    }
                }
            }
            
            // B. Calculate Records (Personal Best)
            val newRecords = mutableListOf<String>()
            
            // Only consider records if the value is significant (>0)
            if (completedTask.actualCalories > 0) {
                val maxCals = history.maxOfOrNull { it.actualCalories } ?: 0
                // If current is the max and it's the only one (or we just celebrate matching PB)
                // Let's go with "New Record" if it strictly beats previous max (excluding self if we hadn't saved yet, but we did).
                // So if count of items with this max value is 1, it's a new unique record.
                val countMax = history.count { it.actualCalories == maxCals }
                if (completedTask.actualCalories == maxCals && countMax == 1) {
                    newRecords.add("消耗创新高！ \uD83D\uDD25") // Fire
                }
            }
            
            if (completedTask.actualMinutes > 0) {
                val maxMins = history.maxOfOrNull { it.actualMinutes } ?: 0
                val countMax = history.count { it.actualMinutes == maxMins }
                if (completedTask.actualMinutes == maxMins && countMax == 1) {
                    newRecords.add("时长创新高！ \u23F1\uFE0F") // Stopwatch
                }
            }
            
            if (completedTask.actualSteps > 0) {
                val maxSteps = history.maxOfOrNull { it.actualSteps } ?: 0
                val countMax = history.count { it.actualSteps == maxSteps }
                if (completedTask.actualSteps == maxSteps && countMax == 1) {
                    newRecords.add("步数创新高！ \uD83D\uDC63") // Footprints
                }
            }

            _submissionAchievement.emit(
                SubmissionAchievement(
                    isSuccess = true,
                    streakDays = streak,
                    newRecords = newRecords
                )
            )

            _refreshTrigger.value += 1
            kotlinx.coroutines.delay(500) 
            _isLoading.value = false
        }
    }

    fun updateTask(task: FitnessTaskEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            taskRepository.updateTask(task)
            _refreshTrigger.value += 1
            kotlinx.coroutines.delay(1000) // Show loading animation
            _isLoading.value = false
        }
    }

    private fun checkAndGenerateTasks(date: LocalDate) {
        viewModelScope.launch {
            try {
                val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                
                // Simplified logic: Load all active plans, check if task exists for each plan on this date, if not create one.
                val plans = planRepository.activePlans.first()
                plans.forEach { plan ->
                    try {
                        // Check if date is within plan range
                        val planStart = LocalDate.parse(plan.startDate, DateTimeFormatter.ISO_LOCAL_DATE)
                        val planEnd = LocalDate.parse(plan.endDate, DateTimeFormatter.ISO_LOCAL_DATE)
                        
                        if (!date.isBefore(planStart) && !date.isAfter(planEnd)) {
                            val existing = taskRepository.getTaskByDateAndPlan(dateStr, plan.id)
                            if (existing == null) {
                                // Create new task
                                val isWorkDay = isWorkDay(date)
                                
                                // Check if task should be enabled for this day type
                                val dietEnabled = if (isWorkDay) plan.workDayDietEnabled else plan.restDayDietEnabled
                                val exerciseEnabled = if (isWorkDay) plan.workDayExerciseEnabled else plan.restDayExerciseEnabled
                                
                                if (dietEnabled || exerciseEnabled) {
                                    val newTask = FitnessTaskEntity(
                                        planId = plan.id,
                                        date = dateStr,
                                        // Populate based on plan config
                                        workExerciseMinutes = if (isWorkDay) plan.workDayExerciseMinutes else 0,
                                        workExerciseSteps = if (isWorkDay) plan.workDayExerciseSteps else 0,
                                        workExerciseCalories = if (isWorkDay) plan.workDayExerciseCalories else 0,
                                        restExerciseMinutes = if (!isWorkDay) plan.restDayExerciseMinutes else 0,
                                        restExerciseCalories = if (!isWorkDay) plan.restDayExerciseCalories else 0,
                                        // Initialize empty fields
                                        workExerciseTypes = if (isWorkDay && exerciseEnabled) "WALK" else "",
                                        note = ""
                                    )
                                    taskRepository.createTask(newTask)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("TaskViewModel", "Error processing plan ${plan.id}", e)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TaskViewModel", "Error generating tasks", e)
            }
        }
    }

    private fun isWorkDay(date: LocalDate): Boolean {
        // Simple logic: Mon-Fri is work day. 
        // Real logic might need holiday calendar.
        val dayOfWeek = date.dayOfWeek.value
        return dayOfWeek in 1..5
    }
}
