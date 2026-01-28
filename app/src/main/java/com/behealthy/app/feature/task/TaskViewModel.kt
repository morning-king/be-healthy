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

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: FitnessTaskRepository,
    private val planRepository: FitnessPlanRepository,
    private val sportsDataRepository: SportsDataRepository,
    private val dailyActivityRepository: DailyActivityRepository,
    private val weatherRepository: com.behealthy.app.core.repository.WeatherRepository
) : ViewModel() {

    val currentSportsData = sportsDataRepository.currentSportsData
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SportsData(0, 0, 0, 0))

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()
    
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


    data class Quote(val content: String, val source: String)

    private val chineseQuotes = listOf(
        // 道德经
        Quote("道可道，非常道；名可名，非常名。", "道德经 · 第一章"),
        Quote("上善若水。水善利万物而不争，处众人之所恶，故几于道。", "道德经 · 第八章"),
        Quote("致虚极，守静笃。万物并作，吾以观复。", "道德经 · 第十六章"),
        Quote("知人者智，自知者明。胜人者有力，自胜者强。", "道德经 · 第三十三章"),
        Quote("人法地，地法天，天法道，道法自然。", "道德经 · 第二十五章"),
        Quote("千里之行，始于足下。", "道德经 · 第六十四章"),
        
        // 庄子
        Quote("北冥有鱼，其名为鲲。鲲之大，不知其几千里也。", "庄子 · 逍遥游"),
        Quote("吾生也有涯，而知也无涯。", "庄子 · 养生主"),
        Quote("相濡以沫，不如相忘于江湖。", "庄子 · 大宗师"),
        Quote("君子之交淡若水，小人之交甘若醴。", "庄子 · 山木"),
        
        // 传习录 (王阳明)
        Quote("知是行之始，行是知之成。", "传习录"),
        Quote("无善无恶心之体，有善有恶意之动。", "传习录"),
        Quote("知之真切笃实处即是行，行之明觉精察处即是知。", "传习录"),
        Quote("心即理也。天下又有心外之事，心外之理乎？", "传习录")
    )

    private val westernQuotes = listOf(
        // 古希腊三杰
        Quote("未经审视的人生是不值得过的。", "苏格拉底"),
        Quote("知足是天然的财富，奢侈是人为的贫穷。", "苏格拉底"),
        Quote("思维是灵魂的自我对话。", "柏拉图"),
        Quote("衡量一个人的标准，是看他有权力时如何行事。", "柏拉图"),
        Quote("幸福属于那些自给自足的人。", "亚里士多德"),
        Quote("优秀不是一种行为，而是一种习惯。", "亚里士多德"),

        // 英国哲学家
        Quote("知识就是力量。", "弗朗西斯·培根"),
        Quote("习惯若不是最好的仆人，便是最差的主人。", "大卫·休谟"),
        Quote("美好的人生是为爱所激励，为知识所引导的。", "伯特兰·罗素"),

        // 德国哲学家
        Quote("世界上只有两样东西是值得我们深深景仰的：头上的灿烂星空和内心的崇高道德准则。", "康德"),
        Quote("那些杀不死你的，终将使你更强大。", "尼采"),
        Quote("凡是现实的都是合理的，凡是合理的都是现实的。", "黑格尔"),

        // 法国哲学家
        Quote("我思故我在。", "笛卡尔"),
        Quote("人生而自由，却无往不在枷锁之中。", "卢梭"),
        Quote("他人即地狱。", "萨特"),
        Quote("雪崩时，没有一片雪花是无辜的。", "伏尔泰")
    )

    private val _quoteRefreshTrigger = MutableStateFlow(0)

    val dailyQuote: StateFlow<Quote> = combine(_selectedDate, _quoteRefreshTrigger) { date, trigger ->
        val seed = date.toEpochDay() + trigger
        // Weight logic: Chinese (5 days) vs Western (2 days)
        // We use seed % 7 to simulate a weekly cycle or probability distribution
        val cycleIndex = (seed % 7).toInt()
        
        if (cycleIndex < 5) {
             // 5/7 probability -> Chinese
             val index = (seed % chineseQuotes.size).toInt()
             chineseQuotes[kotlin.math.abs(index)]
        } else {
             // 2/7 probability -> Western
             val index = (seed % westernQuotes.size).toInt()
             westernQuotes[kotlin.math.abs(index)]
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), chineseQuotes[0])
    
    fun refreshQuote() {
        _quoteRefreshTrigger.value += 1
    }

    fun forceRefreshSportsData() {
        viewModelScope.launch {
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
            } catch (e: Exception) {
                android.util.Log.e("TaskViewModel", "Error force refreshing sports data", e)
            }
        }
    }

    init {
        // Trigger generation of tasks for today if needed
        checkAndGenerateTasks(LocalDate.now())
        startSyncingSportsData()
    }

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
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            // Fix Item 4: Sync historical data if available
            // This ensures if user views past date, task data is consistent with daily_activity (Oppo history)
            val dailyActivity = dailyActivityRepository.getDailyActivity(dateStr).first()
            if (dailyActivity != null) {
                val tasks = taskRepository.getTasksByDate(dateStr).first()
                tasks.forEach { task ->
                     if (task.actualSteps != dailyActivity.steps ||
                         task.actualCalories != dailyActivity.calories ||
                         task.actualMinutes != dailyActivity.durationMinutes ||
                         task.actualDistanceMeters != dailyActivity.distanceMeters) {
                             
                         val updatedTask = task.copy(
                             actualSteps = dailyActivity.steps,
                             actualCalories = dailyActivity.calories,
                             actualMinutes = dailyActivity.durationMinutes,
                             actualDistanceMeters = dailyActivity.distanceMeters
                         )
                         taskRepository.updateTask(updatedTask)
                     }
                }
            }
            
            // Simplified logic: Load all active plans, check if task exists for each plan on this date, if not create one.
            val plans = planRepository.activePlans.first()
            plans.forEach { plan ->
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
