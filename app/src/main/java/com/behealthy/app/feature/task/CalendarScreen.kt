package com.behealthy.app.feature.task

import android.content.Context
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import java.time.temporal.WeekFields
import java.util.Locale
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.decode.VideoFrameDecoder
import coil.ImageLoader
import com.behealthy.app.core.database.entity.FitnessTaskEntity
import com.behealthy.app.core.database.entity.DailyActivityEntity
import com.behealthy.app.ui.RunningLoading
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID

import com.behealthy.app.core.repository.SportsData
import com.behealthy.app.core.repository.WeatherInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onAddPlanClick: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasksForSelectedDate by viewModel.tasksForSelectedDate.collectAsState()
    val tasksForCurrentMonth by viewModel.tasksForCurrentMonth.collectAsState()
    val quote by viewModel.dailyQuote.collectAsState()
    val sportsData by viewModel.currentSportsData.collectAsState()
    val dailyActivity by viewModel.dailyActivityForSelectedDate.collectAsState()
    
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

    LaunchedEffect(state.firstVisibleMonth) {
        viewModel.updateMonth(state.firstVisibleMonth.yearMonth)
    }

    Scaffold(
        floatingActionButton = {
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
                    tint = Color.White,
                    modifier = Modifier.rotate(fabRotation)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.firstVisibleMonth.yearMonth.year}年${state.firstVisibleMonth.yearMonth.monthValue}月",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Weather & Week Info
                val weekFields = WeekFields.of(Locale.getDefault())
                val weekNumber = selectedDate.get(weekFields.weekOfWeekBasedYear())
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "第${weekNumber}周",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        weather?.let { w ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val icon = when(w.condition.name) {
                                    "Sunny" -> "☀️"
                                    "Cloudy" -> "☁️"
                                    "Rainy" -> "🌧️"
                                    "Snowy" -> "❄️"
                                    else -> "🌥️"
                                }
                                Text(icon, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${w.condition.name} ${w.temperature}°C", style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(w.location, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    
                    // Add refresh button for sports data
                    IconButton(
                        onClick = { viewModel.forceRefreshSportsData() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新运动数据",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
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
                    HorizontalCalendar(
                        state = state,
                        dayContent = { day ->
                            val dateStr = day.date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            val status = dayStatusMap[dateStr] ?: 0
                            val weatherInfo = monthlyWeather[day.date]
                            
                            Day(
                                day = day, 
                                isSelected = selectedDate == day.date,
                                isToday = day.date == LocalDate.now(),
                                status = status,
                                weather = weatherInfo
                            ) { date ->
                                viewModel.updateSelectedDate(date)
                                showBottomSheet = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(calendarHeight)
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 4.dp)
                    )
                    
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
                            Text(
                                text = quote.content, 
                                style = MaterialTheme.typography.bodyLarge, 
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
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
                        currentMonth = state.firstVisibleMonth.yearMonth,
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
                    }
                )
            }
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
        val totalMinutes = monthlyTasks.filter { it.isCompleted }.sumOf { it.actualMinutes }
        
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                            .background(Color.LightGray)
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
    day: CalendarDay,
    isSelected: Boolean,
    isToday: Boolean,
    status: Int,
    weather: WeatherInfo?,
    onClick: (LocalDate) -> Unit
) {
    Box(
        modifier = Modifier
            .height(56.dp)
            .aspectRatio(1f)
            .padding(1.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .clickable(enabled = true) { onClick(day.date) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (day.date.dayOfMonth == 1) {
                Text(
                    text = "${day.date.monthValue}月",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            
            // Status Dot
            if (status > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            when (status) {
                                1 -> MaterialTheme.colorScheme.error // Todo
                                2 -> Color(0xFFFFA500) // Doing (Orange)
                                3 -> Color(0xFF4CAF50) // Done (Green)
                                4 -> Color(0xFFFFD700) // Exceeded (Gold)
                                else -> Color.Transparent
                            }
                        )
                )
            }
            
            // Weather Icon (Small)
            weather?.let { w ->
                Spacer(modifier = Modifier.height(2.dp))
                val icon = when(w.condition.name) {
                    "Sunny" -> "☀️"
                    "Cloudy" -> "☁️"
                    "Rainy" -> "🌧️"
                    "Snowy" -> "❄️"
                    else -> "🌥️"
                }
                Text(text = icon, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskDetailSheet(
    selectedDate: LocalDate,
    viewModel: TaskViewModel,
    onClose: () -> Unit,
    onDateChange: (LocalDate) -> Unit
) {
    BackHandler(onBack = onClose)
    
    val referenceDate = remember { selectedDate }
    
    // Use a reference date to prevent issues if date changes
    val initialDiff = java.time.temporal.ChronoUnit.DAYS.between(referenceDate, selectedDate).toInt()
    val initialPage = (Int.MAX_VALUE / 2) + initialDiff
    val pagerState = rememberPagerState(initialPage = initialPage) { Int.MAX_VALUE }
    
    val tasks by viewModel.tasksForSelectedDate.collectAsState()
    val dailyActivity by viewModel.dailyActivityForSelectedDate.collectAsState()
    val sportsData by viewModel.currentSportsData.collectAsState()
    
    // Sync pager with selected date
    LaunchedEffect(pagerState.currentPage) {
        val diff = pagerState.currentPage - (Int.MAX_VALUE / 2)
        val newDate = referenceDate.plusDays(diff.toLong())
        if (newDate != selectedDate) {
            onDateChange(newDate)
        }
    }
    
    // Sync selected date with pager (if changed externally)
    LaunchedEffect(selectedDate) {
        val diff = java.time.temporal.ChronoUnit.DAYS.between(referenceDate, selectedDate).toInt()
        val targetPage = (Int.MAX_VALUE / 2) + diff
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
    
    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
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
                modifier = Modifier.weight(1f)
            ) { page ->
                 val pageDiff = page - (Int.MAX_VALUE / 2)
                 val pageDate = referenceDate.plusDays(pageDiff.toLong())
                 
                 // Show content even if date doesn't match perfectly to avoid flickering/loading state
                 // The pager ensures we are looking at 'pageDate'.
                 
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
                            Text(
                                text = "$icon ${w.condition.name} ${w.temperature}°C ${w.location}",
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
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(
                                    items = tasks,
                                    key = { it.id } // Add key to prevent composition issues
                                ) { task ->
                                    TaskDetailItem(
                                        task = task, 
                                        isLoading = viewModel.isLoading.collectAsState().value,
                                        onUpdate = { viewModel.updateTask(it) },
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
                    // While swiping, show a placeholder or nothing to prevent accessing wrong data
                    Box(modifier = Modifier.fillMaxSize())
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
                if (!pagerState.isScrollInProgress) {
                    onDateChange(selectedDate.minusDays(1)) 
                }
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
                .offset(y = 80.dp) // Move down to avoid overlapping with card content
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
        ) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
        }
        
        IconButton(
            onClick = { 
                if (!pagerState.isScrollInProgress) {
                    onDateChange(selectedDate.plusDays(1))
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .offset(y = 80.dp) // Move down to avoid overlapping with card content
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
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
    onPickImage: (((List<String>) -> Unit) -> Unit)? = null,
    onPickVideo: (((List<String>) -> Unit) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var previewImage by remember { mutableStateOf<String?>(null) }
    var previewVideo by remember { mutableStateOf<String?>(null) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("健身任务", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(if (isExpanded) androidx.compose.material.icons.Icons.Filled.CheckCircle else androidx.compose.material.icons.Icons.Filled.Edit, contentDescription = "Edit")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Task Summary
            if (!isExpanded) {
                if (task.workExerciseMinutes > 0) {
                     Text("目标时长: ${task.workExerciseMinutes}分钟")
                }
                if (task.workExerciseSteps > 0) {
                     Text("目标步数: ${task.workExerciseSteps}步")
                }
                if (task.workExerciseCalories > 0) {
                     Text("目标消耗: ${task.workExerciseCalories}千卡")
                }
                
                val images = task.checkInImages.split(",").filter { it.isNotEmpty() }
                val videos = task.checkInVideos.split(",").filter { it.isNotEmpty() }
                
                if (images.isNotEmpty() || videos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("打卡记录:", style = MaterialTheme.typography.labelMedium)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(images) { path ->
                            MediaThumbnail(path = path, isVideo = false) { previewImage = path }
                        }
                        items(videos) { path ->
                            MediaThumbnail(path = path, isVideo = true) { previewVideo = path }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onUpdate(task.copy(isCompleted = !task.isCompleted)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.isCompleted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoading) {
                        RunningLoading(size = 24.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("保存中...")
                    } else {
                        Text(if (task.isCompleted) "已完成" else "打卡")
                    }
                }
            } else {
                // Edit Mode (Fission Display / Entry)
                TaskInputSection(task, isLoading, onUpdate, onPickImage, onPickVideo)
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
fun MediaThumbnail(path: String, isVideo: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.LightGray)
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
