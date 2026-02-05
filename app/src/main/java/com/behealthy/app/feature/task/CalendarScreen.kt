package com.behealthy.app.feature.task

import android.content.Context
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import java.time.temporal.WeekFields
import java.util.Locale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction
import com.behealthy.app.ui.TypewriterText
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.activity.compose.BackHandler
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.decode.VideoFrameDecoder
import coil.ImageLoader
import com.behealthy.app.core.database.entity.FitnessTaskEntity
import com.behealthy.app.core.database.entity.DailyActivityEntity
import com.behealthy.app.ui.RunningLoading
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.File
import java.util.UUID
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import com.behealthy.app.core.repository.SportsData
import com.behealthy.app.core.network.HolidayDetail

import com.behealthy.app.core.repository.WeatherInfo
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.DialogProperties

import androidx.compose.foundation.pager.PagerState
import java.time.DayOfWeek

// Helper to generate days for the grid
fun getMonthGridDays(yearMonth: YearMonth, firstDayOfWeek: DayOfWeek): List<LocalDate?> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    
    // Calculate start padding (empty cells before 1st day)
    // firstDayOfWeek value: 1 (Mon) to 7 (Sun)
    // If grid starts on Mon (1):
    //   1st is Mon (1) -> padding 0
    //   1st is Tue (2) -> padding 1
    // Padding = (dayOfWeek - firstDayOfWeek + 7) % 7
    val startPadding = (firstDayOfMonth.dayOfWeek.value - firstDayOfWeek.value + 7) % 7
    
    val grid = mutableListOf<LocalDate?>()
    
    // Add padding
    repeat(startPadding) { grid.add(null) }
    
    // Add actual days
    for (i in 1..daysInMonth) {
        grid.add(yearMonth.atDay(i))
    }
    
    // Optional: Add end padding to fill the last row (up to multiple of 7)
    while (grid.size % 7 != 0) {
        grid.add(null)
    }
    
    return grid
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onAddPlanClick: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(120) } // Increased range
    val endMonth = remember { currentMonth.plusMonths(120) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    // Use Pager instead of Calendar library
    val initialPage = 120 // Center (matches minusMonths(120))
    val pagerState = rememberPagerState(initialPage = initialPage) { 241 } // 120 + 1 + 120

    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasksForSelectedDate by viewModel.tasksForSelectedDate.collectAsState()
    val tasksForCurrentMonth by viewModel.tasksForCurrentMonth.collectAsState()
    val quote by viewModel.dailyQuote.collectAsState()
    val sportsData by viewModel.currentSportsData.collectAsState()
    val dailyActivity by viewModel.dailyActivityForSelectedDate.collectAsState()
    val holidays by viewModel.holidaysForCurrentYear.collectAsState()
    
    // Sync Feedback
    val syncMessage by viewModel.syncMessage.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearSyncMessage()
        }
    }
    
    // Weather Data
    val weather by viewModel.weatherForSelectedDate.collectAsState()
    val monthlyWeather by viewModel.weatherForCurrentMonth.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Map date to task status
    // Status: 0 = No task, 1 = Has tasks (none completed), 2 = Partial, 3 = All Completed, 4 = Exceeded
    val dayStatusMap = remember(tasksForCurrentMonth) {
        tasksForCurrentMonth.groupBy { it.date }.mapValues { (_, tasks) ->
            if (tasks.isEmpty()) 0
            else if (tasks.all { it.isCompleted }) {
                 // Check if any task exceeded target (assuming target > 0)
                 if (tasks.any { it.workExerciseMinutes > 0 && it.actualMinutes >= it.workExerciseMinutes }) 4 else 3
            }
            else if (tasks.any { it.isCompleted }) 2
            else 1
        }
    }
    
    // Bottom Sheet State
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    
    // FAB Animation
    var isFabRotated by remember { mutableStateOf(false) }
    val fabRotation by animateFloatAsState(
        targetValue = if (isFabRotated) 360f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "fab_rotation"
    )

    // Calculate currently visible month based on pager
    val visibleMonth = remember(pagerState.currentPage) {
        currentMonth.plusMonths((pagerState.currentPage - initialPage).toLong())
    }

    LaunchedEffect(visibleMonth) {
        viewModel.updateMonth(visibleMonth)
    }

    // Achievement Overlay Logic
    var achievementToShow by remember { mutableStateOf<TaskViewModel.SubmissionAchievement?>(null) }
    LaunchedEffect(viewModel) {
        viewModel.submissionAchievement.collect { value ->
            achievementToShow = value
        }
    }
    
    // Check if "Today" is visible or selected
    val isTodayVisible = remember(visibleMonth, selectedDate) {
        val today = LocalDate.now()
        val isCurrentMonth = YearMonth.from(today) == visibleMonth
        val isTodaySelected = selectedDate == today
        isCurrentMonth && isTodaySelected
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // "Back to Today" Button
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isTodayVisible,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { it / 2 },
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically { it / 2 }
                ) {
                    FloatingActionButton(
                        onClick = {
                            val today = LocalDate.now()
                            // Scroll Pager to Today's Month
                            val diffMonths = YearMonth.from(today).monthValue - currentMonth.monthValue + 
                                            (YearMonth.from(today).year - currentMonth.year) * 12
                            val targetPage = initialPage + diffMonths
                            scope.launch {
                                pagerState.animateScrollToPage(targetPage)
                                viewModel.updateSelectedDate(today)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(bottom = 16.dp).size(48.dp),
                        shape = CircleShape
                    ) {
                        Text("今", fontWeight = FontWeight.Bold)
                    }
                }
                
                FloatingActionButton(
                    onClick = {
                        isFabRotated = !isFabRotated
                        onAddPlanClick()
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = "Add Plan", 
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.rotate(fabRotation)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${visibleMonth.year}年${visibleMonth.monthValue}月",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Weather & Week Info
                val weekFields = WeekFields.of(Locale.getDefault())
                val weekNumber = selectedDate.get(weekFields.weekOfWeekBasedYear())
                
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "第${weekNumber}周",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        // Sync Button (Consistent with Mood Screen)
                        IconButton(
                            onClick = { viewModel.forceRefreshSportsData() },
                            modifier = Modifier.size(20.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                RunningLoading(size = 14.dp, color = MaterialTheme.colorScheme.primary)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "刷新运动数据",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                        
                    weather?.let { w ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = when(w.condition.name) {
                                "Sunny" -> "☀️"
                                "Cloudy" -> "☁️"
                                "Rainy" -> "🌧️"
                                "Snowy" -> "❄️"
                                else -> "🌥️"
                            }
                            val conditionText = when(w.condition.name) {
                                "Sunny" -> "晴"
                                "Cloudy" -> "多云"
                                "Rainy" -> "雨"
                                "Snowy" -> "雪"
                                else -> "阴"
                            }
                            Text(icon, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$conditionText ${w.temperature}°C", style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(w.location, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val calendarHeight = maxHeight * 0.50f
                val summaryHeight = maxHeight * 0.28f
                val quoteHeight = maxHeight * 0.22f
                Column(modifier = Modifier.fillMaxSize()) {
                    
                    // Month Pager
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(calendarHeight)
                    ) { page ->
                        val pageMonth = currentMonth.plusMonths((page - initialPage).toLong())
                        val days = remember(pageMonth) { getMonthGridDays(pageMonth, firstDayOfWeek) }
                        
                        // Month Grid
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                        ) {
                            // Weekday Headers
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (i in 0..6) {
                                    val dayOfWeek = firstDayOfWeek.plus(i.toLong())
                                    Text(
                                        text = dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()),
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            // Days Grid
                            // Use weight to distribute height evenly among rows, avoiding overflow
                            Column(modifier = Modifier.weight(1f)) {
                                val weeks = days.chunked(7)
                                weeks.forEach { week ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                    ) {
                                        week.forEach { date ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (date != null) {
                                                    val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                                    val status = dayStatusMap[dateStr] ?: 0
                                                    val weatherInfo = monthlyWeather[date]
                                                    val holidayDetail = holidays[dateStr.substring(5)]
                                                    
                                                    Day(
                                                        date = date, 
                                                        isSelected = selectedDate == date,
                                                        isToday = date == LocalDate.now(),
                                                        status = status,
                                                        weather = weatherInfo,
                                                        holidayDetail = holidayDetail
                                                    ) { d ->
                                                        viewModel.updateSelectedDate(d)
                                                        showBottomSheet = true
                                                    }
                                                }
                                            }
                                        }
                                        // Fill remaining cells in the row if week is incomplete
                                        if (week.size < 7) {
                                            repeat(7 - week.size) {
                                                Box(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                                // If fewer than 6 rows, add spacers to keep grid size consistent or just let it fill?
                                // To keep cells roughly square-ish or consistent height, we can fill remaining space.
                                // However, `weight(1f)` on existing rows will expand them.
                                // If we want to prevent expansion (keep them top-aligned), we shouldn't use weight on rows in a column that fills height.
                                // But the user wants to "adjust height to not overflow", implying squeezing.
                                // So distributing available height (weight) is the correct approach for handling 6 rows.
                                // For 5 rows, they will be slightly taller, which is fine.
                            }
                        }
                    }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(quoteHeight)
                            .padding(horizontal = 16.dp)
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.Top
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "每日一言", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                IconButton(
                                    onClick = { viewModel.refreshQuote() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            TypewriterText(
                                text = quote.content, 
                                style = MaterialTheme.typography.bodyLarge, 
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                rainbow = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "—— ${quote.source}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    FitnessMonthlyStats(
                        currentMonth = visibleMonth,
                        tasks = tasksForCurrentMonth,
                        modifier = Modifier.height(summaryHeight)
                    )
                }
            }
        }
        
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { 
                    scope.launch { sheetState.hide() }
                    showBottomSheet = false
                },
                sheetState = sheetState
            ) {
                TaskDetailSheet(
                    selectedDate = selectedDate,
                    viewModel = viewModel,
                    onDateChange = { newDate -> viewModel.updateSelectedDate(newDate) },
                    onClose = { 
                        scope.launch { sheetState.hide() }
                        showBottomSheet = false
                    },
                    onSaveAndComplete = { task -> 
                        viewModel.saveAndCompleteTask(task)
                        scope.launch { sheetState.hide() }
                        showBottomSheet = false
                    }
                )
            }
        }
        
        if (achievementToShow != null) {
            AchievementOverlay(
                achievement = achievementToShow!!,
                onDismiss = { achievementToShow = null }
            )
        }
    }
}

@Composable
fun FitnessMonthlyStats(
    currentMonth: YearMonth,
    tasks: List<FitnessTaskEntity>,
    modifier: Modifier = Modifier
) {
    val monthlyTasks = remember(currentMonth, tasks) {
        tasks.filter {
            try {
                val date = LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE)
                YearMonth.from(date) == currentMonth
            } catch (e: Exception) { false }
        }
    }

    val stats = remember(monthlyTasks) {
        val daysWithTasks = monthlyTasks.map { it.date }.distinct().count()
        val completedTasks = monthlyTasks.count { it.isCompleted }
        val totalTasks = monthlyTasks.size
        // Group by date to check full completion per day
        val tasksByDate = monthlyTasks.groupBy { it.date }
        val fullyCompletedDays = tasksByDate.count { (_, dayTasks) -> 
            dayTasks.isNotEmpty() && dayTasks.all { it.isCompleted }
        }
        val totalMinutes = monthlyTasks.filter { it.isCompleted }.sumOf { 
            if (it.actualMinutes > 0) it.actualMinutes else it.workExerciseMinutes 
        }
        
        Triple(daysWithTasks, fullyCompletedDays, totalMinutes)
    }
    
    val (daysWithTasks, fullyCompletedDays, totalMinutes) = stats
    
    val summary = remember(stats) {
        if (monthlyTasks.isEmpty()) "本月暂无健身记录，制定个计划吧！"
        else {
            if (fullyCompletedDays > daysWithTasks / 2 && daysWithTasks > 5) "本月坚持得真棒！汗水不会辜负你！💪"
            else if (fullyCompletedDays > 0) "已经在路上了，继续加油，坚持就是胜利！🏃"
            else "万事开头难，动起来就是最好的开始。🌟"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("本月健身小结", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(summary, style = MaterialTheme.typography.bodyMedium)
            
            if (monthlyTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    val items = listOf(
                        Triple("📅", "打卡天数", "$fullyCompletedDays/$daysWithTasks"),
                        Triple("⏱️", "运动时长", "${totalMinutes}分钟")
                    )
                    items.forEach { (icon, label, value) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(icon, style = MaterialTheme.typography.headlineSmall)
                            Text(label, style = MaterialTheme.typography.bodySmall)
                            Text(value, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MediaSection(
    title: String,
    mediaList: List<String>,
    isVideo: Boolean,
    maxCount: Int,
    onAddMedia: () -> Unit,
    onRemoveMedia: (String) -> Unit,
    onMediaClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$title (${mediaList.size}/$maxCount)", style = MaterialTheme.typography.bodyMedium)
            if (mediaList.size < maxCount) {
                IconButton(onClick = onAddMedia) {
                    Icon(Icons.Default.Add, contentDescription = "Add $title")
                }
            }
        }
        
        if (mediaList.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(mediaList) { path ->
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onMediaClick(path) }
                    ) {
                        val context = LocalContext.current
                        val imageLoader = remember {
                            ImageLoader.Builder(context)
                                .components {
                                    add(VideoFrameDecoder.Factory())
                                }
                                .build()
                        }
                        
                        AsyncImage(
                            model = if (isVideo) {
                                ImageRequest.Builder(context)
                                    .data(File(path))
                                    .decoderFactory(VideoFrameDecoder.Factory())
                                    .build()
                            } else {
                                File(path)
                            },
                            contentDescription = null,
                            imageLoader = imageLoader,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        if (isVideo) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        
                        IconButton(
                            onClick = { onRemoveMedia(path) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }
        ) {
            AsyncImage(
                model = File(imageUrl),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

@Composable
fun FullScreenVideoDialog(videoUrl: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { context ->
                    VideoView(context).apply {
                        setVideoPath(videoUrl)
                        val mediaController = android.widget.MediaController(context)
                        mediaController.setAnchorView(this)
                        setMediaController(mediaController)
                        start()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

suspend fun copyUriToInternalStorage(context: Context, uri: Uri, type: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val fileName = "${type}_${UUID.randomUUID()}.${if (type == "video") "mp4" else "jpg"}"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
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

@Composable
fun Day(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    status: Int,
    weather: WeatherInfo?,
    holidayDetail: HolidayDetail?,
    onClick: (LocalDate) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize() // Use fillMaxSize to adapt to container
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background Circle (Constrained by smallest dimension)
        Box(
            modifier = Modifier
                .aspectRatio(1f) // Ensure it's a circle/square
                .fillMaxHeight() // Try to fill height
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .border(
                    width = if (isToday) 2.dp else 0.dp,
                    color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                )
                .clickable(enabled = true) { onClick(date) }
        )
        
        // Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (date.dayOfMonth == 1) {
                Text(
                    text = "${date.monthValue}月",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            
            // Status Dot
            if (status > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            when (status) {
                                1 -> MaterialTheme.colorScheme.error // Todo
                                2 -> Color(0xFFFB8C00) // Doing (Deep Orange)
                                3 -> Color(0xFF43A047) // Done (Green)
                                4 -> Color(0xFFFBC02D) // Exceeded (Darker Gold)
                                else -> Color.Transparent
                            }
                        )
                )
            }
            
            // Weather Icon (Small)
            weather?.let { w ->
                Spacer(modifier = Modifier.height(1.dp))
                val icon = when(w.condition.name) {
                    "Sunny" -> "☀️"
                    "Cloudy" -> "☁️"
                    "Rainy" -> "🌧️"
                    "Snowy" -> "❄️"
                    else -> "🌥️"
                }
                Text(text = icon, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp)
            }
        }

        // Holiday/Work Tag (Top Right)
    if (holidayDetail != null) {
        val text = if (holidayDetail.holiday) "休" else "班"
        val color = if (holidayDetail.holiday) Color(0xFF9C27B0) else Color(0xFFFF9800)
        
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 8.sp,
            color = color,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 2.dp, end = 4.dp)
        )
    } else {
        // Fallback to old HolidayUtils if null? No, we deleted it.
        // Just check if we still have the old block here.
        // The grep showed the file still had HolidayUtils reference in the error log.
        // "val holidayStatus = remember(date) { HolidayUtils.getHolidayStatus(date) }"
        // So I need to remove that block if it exists.
    }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskDetailSheet(
    selectedDate: LocalDate,
    viewModel: TaskViewModel,
    onClose: () -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onSaveAndComplete: (FitnessTaskEntity) -> Unit
) {
    BackHandler(onBack = onClose)
    
    val referenceDate = remember { selectedDate }
    
    // Use a reference date to prevent issues if date changes
    val initialDiff = java.time.temporal.ChronoUnit.DAYS.between(referenceDate, selectedDate).toInt()
    // Use a reasonable range (e.g., +/- 50 years = ~36500 days) instead of Int.MAX_VALUE to avoid potential overflow/performance issues
    val maxPages = 40000
    val centerPage = maxPages / 2
    val initialPage = centerPage + initialDiff
    val pagerState = rememberPagerState(initialPage = initialPage) { maxPages }
    
    val tasks by viewModel.tasksForSelectedDate.collectAsState()
    val dailyActivity by viewModel.dailyActivityForSelectedDate.collectAsState()
    val sportsData by viewModel.currentSportsData.collectAsState()
    
    // Sync pager with selected date
    LaunchedEffect(pagerState.currentPage) {
        val diff = pagerState.currentPage - centerPage
        val newDate = referenceDate.plusDays(diff.toLong())
        if (newDate != selectedDate) {
            onDateChange(newDate)
        }
    }
    
    // Sync selected date with pager (if changed externally)
    LaunchedEffect(selectedDate) {
        val diff = java.time.temporal.ChronoUnit.DAYS.between(referenceDate, selectedDate).toInt()
        val targetPage = centerPage + diff
        if (pagerState.currentPage != targetPage && !pagerState.isScrollInProgress) {
            pagerState.scrollToPage(targetPage)
        }
    }

    // Launcher for media picking - Hoisted from TaskInputSection
    var pendingMediaCallback by remember { mutableStateOf<((List<String>) -> Unit)?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
        scope.launch {
            val paths = uris.mapNotNull { copyUriToInternalStorage(context, it, "image") }
            pendingMediaCallback?.invoke(paths)
            pendingMediaCallback = null
        }
    }
    
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(3)) { uris ->
        scope.launch {
            val paths = uris.mapNotNull { copyUriToInternalStorage(context, it, "video") }
            pendingMediaCallback?.invoke(paths)
            pendingMediaCallback = null
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .navigationBarsPadding() // Handle bottom nav bar overlap
            // Removed imePadding from root to prevent whole sheet jumping
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header (Date only, no buttons)
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE", Locale.CHINESE)),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                beyondViewportPageCount = 1
            ) { page ->
                 val pageDiff = page - centerPage
                 val pageDate = referenceDate.plusDays(pageDiff.toLong())
                 
                if (pageDate == selectedDate) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Weather Info in Detail Page (Item 5)
                        val weather = viewModel.weatherForSelectedDate.collectAsState().value
                        weather?.let { w ->
                            val icon = when(w.condition.name) {
                                "Sunny" -> "☀️"
                                "Cloudy" -> "☁️"
                                "Rainy" -> "🌧️"
                                "Snowy" -> "❄️"
                                else -> "🌥️"
                            }
                            val conditionText = when(w.condition.name) {
                                "Sunny" -> "晴"
                                "Cloudy" -> "多云"
                                "Rainy" -> "雨"
                                "Snowy" -> "雪"
                                else -> "阴"
                            }
                            Text(
                                text = "$icon $conditionText ${w.temperature}°C ${w.location}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (tasks.isEmpty()) {
                             // Always try to show OPPO data if no tasks
                             val isToday = pageDate == LocalDate.now()
                             
                             // Use dailyActivity if available, otherwise 0
                             val steps = dailyActivity?.steps ?: (if (isToday) sportsData.steps else 0)
                             val calories = dailyActivity?.calories ?: (if (isToday) sportsData.calories else 0)
                             val distance = dailyActivity?.distanceMeters ?: (if (isToday) sportsData.distanceMeters else 0)
                             val duration = dailyActivity?.durationMinutes ?: (if (isToday) sportsData.durationMinutes else 0)

                             // Show OPPO Sync Data (even if 0, to look "exquisite")
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "OPPO运动同步数据",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    OppoDataParam("步数", "$steps", "步", Icons.Default.DirectionsRun)
                                    OppoDataParam("消耗", "$calories", "千卡", Icons.Default.LocalFireDepartment)
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    OppoDataParam("距离", "$distance", "米", Icons.Default.Straighten)
                                    OppoDataParam("时长", "$duration", "分钟", Icons.Default.Timer)
                                }
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                Text(
                                    text = if (isToday) "今日暂无健身计划，已自动同步运动数据" else "当日无健身计划，已显示历史同步数据",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.imePadding() // Apply imePadding here to push list up
                            ) {
                                items(
                                    items = tasks,
                                    key = { it.id }
                                ) { task ->
                                    TaskDetailItem(
                                        task = task, 
                                        isLoading = viewModel.isLoading.collectAsState().value,
                                        onUpdate = { viewModel.updateTask(it) },
                                        onSaveAndComplete = onSaveAndComplete,
                                        onPickImage = { callback ->
                                            pendingMediaCallback = callback
                                            imageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        },
                                        onPickVideo = { callback ->
                                            pendingMediaCallback = callback
                                            videoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Show a placeholder to indicate loading/transition
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }
        }
            
            // Close Button
            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Text("关闭")
            }
        }
        
        // Navigation Buttons (Vertically Centered)
        IconButton(
            onClick = { 
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                .size(48.dp) // Larger touch area
        ) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
        }
        
        IconButton(
            onClick = { 
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                .size(48.dp) // Larger touch area
        ) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
        }
    }
}

@Composable
fun TaskDetailItem(
    task: FitnessTaskEntity, 
    isLoading: Boolean,
    onUpdate: (FitnessTaskEntity) -> Unit,
    onSaveAndComplete: (FitnessTaskEntity) -> Unit,
    onPickImage: (((List<String>) -> Unit) -> Unit)? = null,
    onPickVideo: (((List<String>) -> Unit) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var previewImage by remember { mutableStateOf<String?>(null) }
    var previewVideo by remember { mutableStateOf<String?>(null) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Icon (Clickable to toggle completion)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (task.isCompleted) MaterialTheme.colorScheme.primaryContainer 
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { onUpdate(task.copy(isCompleted = !task.isCompleted)) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (task.isCompleted) {
                            Icon(
                                Icons.Default.CheckCircle, 
                                contentDescription = "Completed",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), CircleShape)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "健身任务", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (task.isCompleted) {
                            Text(
                                "已完成",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.Edit, 
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Metrics Row (Compact View)
            if (!isExpanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricItem(
                        icon = Icons.Default.Timer, 
                        value = "${if (task.actualMinutes > 0) task.actualMinutes else task.workExerciseMinutes}", 
                        unit = "分钟",
                        label = "时长"
                    )
                    MetricItem(
                        icon = Icons.Default.DirectionsRun, 
                        value = "${if (task.actualSteps > 0) task.actualSteps else task.workExerciseSteps}", 
                        unit = "步",
                        label = "步数"
                    )
                    MetricItem(
                        icon = Icons.Default.LocalFireDepartment, 
                        value = "${if (task.actualCalories > 0) task.actualCalories else task.workExerciseCalories}", 
                        unit = "千卡",
                        label = "消耗"
                    )
                }
                
                // Media Preview (Thumbnail strip)
                val images = task.checkInImages.split(",").filter { it.isNotEmpty() }
                val videos = task.checkInVideos.split(",").filter { it.isNotEmpty() }
                if (images.isNotEmpty() || videos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("打卡记录", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(images) { path ->
                            MediaThumbnail(path = path, isVideo = false, size = 56.dp) { previewImage = path }
                        }
                        items(videos) { path ->
                            MediaThumbnail(path = path, isVideo = true, size = 56.dp) { previewVideo = path }
                        }
                    }
                }
            } else {
                // Expanded View (Input Fields)
                // Local state for editing
                var minutesInput by remember(task.id) { mutableStateOf(task.workExerciseMinutes.toString()) }
                var stepsInput by remember(task.id) { mutableStateOf(task.workExerciseSteps.toString()) }
                var caloriesInput by remember(task.id) { mutableStateOf(task.workExerciseCalories.toString()) }
                var noteInput by remember(task.id) { mutableStateOf(task.note) }
                
                var imagesInput by remember(task.id) { mutableStateOf(task.checkInImages.split(",").filter { it.isNotEmpty() }) }
                var videosInput by remember(task.id) { mutableStateOf(task.checkInVideos.split(",").filter { it.isNotEmpty() }) }

                val focusRequester = remember { FocusRequester() }
                
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                // Duration Input
                OutlinedTextField(
                    value = minutesInput,
                    onValueChange = { minutesInput = it },
                    label = { Text("时长 (分钟)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Steps Input
                OutlinedTextField(
                    value = stepsInput,
                    onValueChange = { stepsInput = it },
                    label = { Text("步数") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Calories Input
                OutlinedTextField(
                    value = caloriesInput,
                    onValueChange = { caloriesInput = it },
                    label = { Text("消耗 (千卡)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Note Input
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Media Section
                MediaSection(
                    title = "打卡图片",
                    mediaList = imagesInput,
                    isVideo = false,
                    maxCount = 5,
                    onAddMedia = { onPickImage?.invoke { newPaths ->
                        imagesInput = (imagesInput + newPaths).take(5)
                    }},
                    onRemoveMedia = { path ->
                        imagesInput = imagesInput - path
                    },
                    onMediaClick = { previewImage = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                MediaSection(
                    title = "打卡视频",
                    mediaList = videosInput,
                    isVideo = true,
                    maxCount = 3,
                    onAddMedia = { onPickVideo?.invoke { newPaths ->
                        videosInput = (videosInput + newPaths).take(3)
                    }},
                    onRemoveMedia = { path ->
                        videosInput = videosInput - path
                    },
                    onMediaClick = { previewVideo = it }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Save Button (Collapses expanded view)
                Button(
                    onClick = { 
                        val updatedTask = task.copy(
                            workExerciseMinutes = minutesInput.toIntOrNull() ?: 0,
                            workExerciseSteps = stepsInput.toIntOrNull() ?: 0,
                            workExerciseCalories = caloriesInput.toIntOrNull() ?: 0,
                            note = noteInput,
                            checkInImages = imagesInput.joinToString(","),
                            checkInVideos = videosInput.joinToString(",")
                        )
                        onSaveAndComplete(updatedTask)
                        isExpanded = false 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        RunningLoading(size = 20.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("保存中...")
                    } else {
                        Text("保存并完成")
                    }
                }
            }
        }
    }
    
    if (previewImage != null) FullScreenImageDialog(previewImage!!) { previewImage = null }
    if (previewVideo != null) FullScreenVideoDialog(previewVideo!!) { previewVideo = null }
}

@Composable
fun OppoDataParam(label: String, value: String, unit: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = "$label ($unit)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MediaThumbnail(path: String, isVideo: Boolean, size: androidx.compose.ui.unit.Dp = 100.dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
    ) {
         val context = LocalContext.current
         val imageLoader = remember {
             ImageLoader.Builder(context)
                 .components {
                     add(VideoFrameDecoder.Factory())
                 }
                 .build()
         }
         
         AsyncImage(
             model = if (isVideo) {
                 ImageRequest.Builder(context)
                     .data(File(path))
                     .decoderFactory(VideoFrameDecoder.Factory())
                     .build()
             } else {
                 File(path)
             },
             contentDescription = null,
             imageLoader = imageLoader,
             contentScale = ContentScale.Crop,
             modifier = Modifier.fillMaxSize()
         )
         
         if (isVideo) {
             Icon(
                 imageVector = Icons.Default.PlayArrow,
                 contentDescription = "Play",
                 tint = Color.White,
                 modifier = Modifier.align(Alignment.Center).size(24.dp)
             )
         }
    }
}

@Composable
fun MetricItem(icon: ImageVector, value: String, unit: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TaskInputSection(
    task: FitnessTaskEntity, 
    isLoading: Boolean,
    onUpdate: (FitnessTaskEntity) -> Unit,
    onPickImage: (((List<String>) -> Unit) -> Unit)? = null,
    onPickVideo: (((List<String>) -> Unit) -> Unit)? = null
) {
    var minutes by remember { mutableStateOf(task.workExerciseMinutes.toString()) }
    var steps by remember { mutableStateOf(task.workExerciseSteps.toString()) }
    var calories by remember { mutableStateOf(task.workExerciseCalories.toString()) }
    var note by remember { mutableStateOf(task.note) }
    
    // Media State
    var images by remember { mutableStateOf(task.checkInImages.split(",").filter { it.isNotEmpty() }) }
    var videos by remember { mutableStateOf(task.checkInVideos.split(",").filter { it.isNotEmpty() }) }
    
    var previewImage by remember { mutableStateOf<String?>(null) }
    var previewVideo by remember { mutableStateOf<String?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = minutes,
            onValueChange = { minutes = it },
            label = { Text("时长 (分钟)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = steps,
            onValueChange = { steps = it },
            label = { Text("步数") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it },
            label = { Text("消耗 (千卡)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("备注") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        
        MediaSection(
            title = "打卡图片",
            mediaList = images,
            isVideo = false,
            maxCount = 5,
            onAddMedia = { 
                onPickImage?.invoke { paths ->
                    val newImages = images.toMutableList()
                    newImages.addAll(paths)
                    images = newImages.take(5)
                }
            },
            onRemoveMedia = { path -> images = images - path },
            onMediaClick = { previewImage = it }
        )
        
        MediaSection(
            title = "打卡视频",
            mediaList = videos,
            isVideo = true,
            maxCount = 3,
            onAddMedia = { 
                onPickVideo?.invoke { paths ->
                    val newVideos = videos.toMutableList()
                    newVideos.addAll(paths)
                    videos = newVideos.take(3)
                }
            },
            onRemoveMedia = { path -> videos = videos - path },
            onMediaClick = { previewVideo = it }
        )
        
        Button(
            onClick = {
                val updatedTask = task.copy(
                    workExerciseMinutes = minutes.toIntOrNull() ?: 0,
                    workExerciseSteps = steps.toIntOrNull() ?: 0,
                    workExerciseCalories = calories.toIntOrNull() ?: 0,
                    note = note,
                    checkInImages = images.joinToString(","),
                    checkInVideos = videos.joinToString(",")
                )
                onUpdate(updatedTask)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                RunningLoading(size = 24.dp, color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("保存中...")
            } else {
                Text("保存修改")
            }
        }
    }
    
    if (previewImage != null) FullScreenImageDialog(previewImage!!) { previewImage = null }
    if (previewVideo != null) FullScreenVideoDialog(previewVideo!!) { previewVideo = null }
}
