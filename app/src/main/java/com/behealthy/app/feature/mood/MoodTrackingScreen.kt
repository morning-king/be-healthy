package com.behealthy.app.feature.mood

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.behealthy.app.core.database.entity.MoodRecordEntity
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.temporal.WeekFields
import com.behealthy.app.core.repository.WeatherInfo
import com.behealthy.app.ui.RunningLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodTrackingScreen(
    viewModel: MoodViewModel = hiltViewModel()
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
    
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showMoodDialog by remember { mutableStateOf(false) }
    
    // Mood Data from ViewModel
    val moodRecords by viewModel.moodRecords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var isSaving by remember { mutableStateOf(false) }


    val moodMap = remember(moodRecords) {
        moodRecords.associateBy { 
             try {
                 LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE)
             } catch (e: Exception) {
                 LocalDate.now() 
             }
        }
    }
    
    val currentPoem by viewModel.currentPoem.collectAsState()
    
    // Weather Data
    val weather by viewModel.weatherForSelectedDate.collectAsState()
    val monthlyWeather by viewModel.weatherForCurrentMonth.collectAsState()
    
    // Animation State
    var showAnimation by remember { mutableStateOf(false) }
    var lastSavedMood by remember { mutableStateOf("") }

    LaunchedEffect(isLoading) {
        if (!isLoading && isSaving) {
            isSaving = false
            showMoodDialog = false
            showAnimation = true
        }
    }
    
    LaunchedEffect(showAnimation) {
        if (showAnimation) {
            delay(3000)
            showAnimation = false
        }
    }

    LaunchedEffect(state.firstVisibleMonth) {
        viewModel.updateMonth(state.firstVisibleMonth.yearMonth)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Calendar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.firstVisibleMonth.yearMonth.year}年${state.firstVisibleMonth.yearMonth.monthValue}月",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Weather & Week Info
                val weekFields = WeekFields.of(Locale.getDefault())
                val weekNumber = selectedDate.get(weekFields.weekOfWeekBasedYear())
                
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
            }
            
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val calendarHeight = maxHeight * 0.5f
                val summaryHeight = maxHeight * 0.28f
                val poemHeight = maxHeight * 0.22f
                Column(modifier = Modifier.fillMaxSize()) {
                    HorizontalCalendar(
                        state = state,
                        dayContent = { day ->
                            val moodRecord = moodMap[day.date]
                            val weatherInfo = monthlyWeather[day.date]
                            MoodDay(
                                day = day, 
                                isSelected = selectedDate == day.date,
                                moodIcon = getMoodIcon(moodRecord?.mood),
                                hasAudio = moodRecord?.audioPath != null,
                                isToday = day.date == LocalDate.now(),
                                weather = weatherInfo
                            ) { date ->
                                selectedDate = date
                                showMoodDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(calendarHeight)
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 8.dp)
                    )
                    
                    PoemCard(
                        poem = currentPoem, 
                        onRefresh = { viewModel.refreshPoem() },
                        modifier = Modifier.height(poemHeight)
                    )
                    
                    MoodMonthlyStats(
                        currentMonth = state.firstVisibleMonth.yearMonth,
                        moodRecords = moodRecords,
                        modifier = Modifier.height(summaryHeight)
                    )
                }
            }
        }
        
        if (showMoodDialog) {
            val record = moodMap[selectedDate]
            MoodEntryDialog(
                date = selectedDate,
                 initialMood = record?.mood,
                 initialNote = record?.note ?: "",
                 initialAudioPath = record?.audioPath,
                 initialAudioDuration = record?.audioDuration ?: 0L,
                 isLoading = isLoading,
                 onDismiss = { showMoodDialog = false },
                onSave = { mood, note, audioPath, audioDuration ->
                    viewModel.saveMood(selectedDate, mood, note, audioPath, audioDuration)
                    lastSavedMood = mood
                    isSaving = true
                }
            )
        }
        
        if (showAnimation) {
            MoodAnimationOverlay(mood = lastSavedMood)
        }
    }
}


@Composable
fun PoemCard(
    poem: Poem, 
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "每日诗词", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onRefresh, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = poem.content,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "—— ${poem.author}",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun MoodMonthlyStats(
    currentMonth: YearMonth, 
    moodRecords: List<MoodRecordEntity>,
    modifier: Modifier = Modifier
) {
    val monthlyMoods = remember(currentMonth, moodRecords) {
        moodRecords.filter {
            try {
                val date = LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE)
                YearMonth.from(date) == currentMonth
            } catch (e: Exception) { false }
        }
    }
    
    val stats = remember(monthlyMoods) {
        monthlyMoods.groupingBy { it.mood }.eachCount()
    }
    
    val summary = remember(stats) {
        if (stats.isEmpty()) "本月暂无心情记录，开始记录吧！"
        else {
            val total = monthlyMoods.size
            // 正面心情统计
            val positiveMoods = stats.filterKeys { 
                it in listOf("开心", "美滋滋", "小确幸", "激动", "平静", "享受", "心动", "冥想", "兴奋") 
            }.values.sum()
            // 负面心情统计  
            val negativeMoods = stats.filterKeys { 
                it in listOf("难过", "气愤", "倒霉", "焦虑", "恐惧", "累", "颓废", "委屈", "孤独", "生气", "难以描述", "懵圈") 
            }.values.sum()
            // 中性心情
            val neutralMoods = total - positiveMoods - negativeMoods
            
            when {
                positiveMoods > total * 0.6 -> "本月心情很棒，积极情绪占主导，继续保持乐观的心态！"
                negativeMoods > total * 0.6 -> "本月情绪波动较大，建议多关注心理健康，适当放松调节。"
                positiveMoods > negativeMoods -> "本月正面情绪居多，整体状态良好，继续保持！"
                negativeMoods > positiveMoods -> "本月有些挑战，但每一次经历都是成长的机会。"
                else -> "本月心情平稳，平平淡淡才是真，珍惜当下的宁静。"
            }
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
            Text("本月心情小结", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(summary, style = MaterialTheme.typography.bodyMedium)
            
            if (stats.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    stats.entries.sortedByDescending { it.value }.take(3).forEach { (mood, count) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(getMoodIcon(mood) ?: "❓", style = MaterialTheme.typography.headlineSmall)
                            Text(mood, style = MaterialTheme.typography.bodySmall)
                            Text("$count 天", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MoodAnimationOverlay(mood: String) {
    val isGood = mood in listOf("开心", "美滋滋", "小确幸", "激动", "平静", "享受", "冥想")
    val isBad = mood in listOf("难过", "气愤", "倒霉", "焦虑", "恐惧", "累", "颓废", "委屈", "孤独", "生气")
    
    val text = when {
        isGood -> "心情真棒！\n继续保持这份快乐！"
        isBad -> "抱抱你！\n一切都会好起来的！"
        else -> "平平淡淡，\n也是一种幸福。"
    }
    
    val emoji = when {
        isGood -> "🎉 ✨ 🤩"
        isBad -> "🎆 🌈 💪" // Fireworks for bad mood as requested ("放个烟花")
        else -> "🍵 🍃 🧘"
    }
    
    val color = when {
        isGood -> Color(0xFFFFD700) // Gold
        isBad -> Color(0xFF87CEEB) // Sky Blue
        else -> Color(0xFF98FB98) // Pale Green
    }

    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .size(300.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(emoji, style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text, 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoodEntryDialog(
    date: LocalDate,
    initialMood: String?,
    initialNote: String,
    initialAudioPath: String?,
    initialAudioDuration: Long,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String, String, String?, Long) -> Unit
) {
    var isEditing by remember { mutableStateOf(initialMood == null) }
    var selectedMood by remember { mutableStateOf(initialMood) }
    var moodNote by remember { mutableStateOf(initialNote) }
    
    // Audio State
    val context = LocalContext.current
    var audioPath by remember { mutableStateOf(initialAudioPath) }
    var audioDuration by remember { mutableLongStateOf(initialAudioDuration) }
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableLongStateOf(0L) }
    
    // Recorder/Player References
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "mood_audio_${System.currentTimeMillis()}.3gp")
            try {
                mediaRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }
                audioPath = file.absolutePath
                isRecording = true
                recordingTime = 0
            } catch (e: Exception) {
                Toast.makeText(context, "录音失败，请检查权限", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        } else {
            Toast.makeText(context, "未授予录音权限", Toast.LENGTH_SHORT).show()
        }
    }

    // Recording Timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            val startTime = System.currentTimeMillis()
            while (isRecording) {
                recordingTime = (System.currentTimeMillis() - startTime) / 1000
                if (recordingTime >= 180) { // 3 minutes limit
                    // Stop recording automatically
                    try {
                        mediaRecorder?.stop()
                        mediaRecorder?.release()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    mediaRecorder = null
                    isRecording = false
                    audioDuration = recordingTime
                }
                delay(1000)
            }
        }
    }
    
    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaRecorder?.release()
                mediaPlayer?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val moodOptions = listOf(
        "开心" to "😄", "美滋滋" to "🥰", "心动" to "😍",
        "激动" to "🤩", "兴奋" to "😆", "享受" to "😋",
        "平静" to "😌", "小确幸" to "🍀", "冥想" to "🧘",
        "一般" to "😶", "还行" to "🙂", "平淡" to "😐",
        "懵圈" to "😵", "难以描述" to "😵‍💫",
        "难过" to "😢", "焦虑" to "😰", "气愤" to "😤",
        "倒霉" to "😫", "恐惧" to "😱", "累" to "😫",
        "颓废" to "🫠", "委屈" to "🥺", "生气" to "😡",
        "孤独" to "😔"
    )
    
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant // Theme-aware background
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isEditing && selectedMood != null) {
                    // View Mode
                    Text(
                        text = "今日心情",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    val icon = moodOptions.find { it.first == selectedMood }?.second ?: "😊"
                    Text(text = icon, fontSize = 80.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = selectedMood!!,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (moodNote.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = moodNote,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        )
                    }
                    
                    if (audioPath != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        // Simple Player UI for View Mode
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(50))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable {
                                    if (isPlaying) {
                                        mediaPlayer?.stop()
                                        mediaPlayer?.release()
                                        mediaPlayer = null
                                        isPlaying = false
                                    } else {
                                        try {
                                            mediaPlayer = MediaPlayer().apply {
                                                setDataSource(audioPath)
                                                prepare()
                                                start()
                                                setOnCompletionListener { 
                                                    isPlaying = false
                                                    it.release()
                                                    mediaPlayer = null
                                                }
                                            }
                                            isPlaying = true
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "播放失败", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                                contentDescription = "Play/Stop",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${audioDuration}s 语音心情", 
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("修改心情", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Edit Mode
                    Text(
                        text = if (selectedMood == null) "今天心情怎么样？" else "修改心情",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Mood Grid - 3 Columns, no scrolling, smaller icons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                    moodOptions.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { (mood, icon) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedMood = mood }
                                        .background(
                                            if (selectedMood == mood) MaterialTheme.colorScheme.primaryContainer 
                                            else Color.Transparent
                                        )
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = icon,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = mood,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (selectedMood == mood) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Note Input
                OutlinedTextField(
                    value = moodNote,
                    onValueChange = { moodNote = it },
                    label = { Text("写点什么...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Audio Recorder Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isRecording) {
                        Text("录音中... ${recordingTime}s / 180s", color = MaterialTheme.colorScheme.error)
                        IconButton(onClick = {
                            try {
                                mediaRecorder?.stop()
                                mediaRecorder?.release()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            mediaRecorder = null
                            isRecording = false
                            audioDuration = recordingTime
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Stop Recording", tint = MaterialTheme.colorScheme.error)
                        }
                    } else if (audioPath != null) {
                        // Playback Controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                if (isPlaying) {
                                    mediaPlayer?.stop()
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                    isPlaying = false
                                } else {
                                    try {
                                        mediaPlayer = MediaPlayer().apply {
                                            setDataSource(audioPath)
                                            prepare()
                                            start()
                                            setOnCompletionListener { 
                                                isPlaying = false
                                                it.release()
                                                mediaPlayer = null
                                            }
                                        }
                                        isPlaying = true
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "播放失败", Toast.LENGTH_SHORT).show()
                                        e.printStackTrace()
                                    }
                                }
                            }) {
                                Icon(
                                    if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Stop",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text("${audioDuration}s", style = MaterialTheme.typography.bodyMedium)
                            
                            IconButton(onClick = {
                                audioPath = null
                                audioDuration = 0
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Audio", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    } else {
                        // Start Recording Button
                        Text("点击录音 (最长3分钟)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        IconButton(onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                val file = File(context.cacheDir, "mood_audio_${System.currentTimeMillis()}.3gp")
                                try {
                                    mediaRecorder = MediaRecorder().apply {
                                        setAudioSource(MediaRecorder.AudioSource.MIC)
                                        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                                        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                                        setOutputFile(file.absolutePath)
                                        prepare()
                                        start()
                                    }
                                    audioPath = file.absolutePath
                                    isRecording = true
                                    recordingTime = 0
                                } catch (e: Exception) {
                                    Toast.makeText(context, "录音失败，请检查权限", Toast.LENGTH_SHORT).show()
                                    e.printStackTrace()
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Start Recording", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("取消", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { 
                            if (selectedMood != null) {
                                onSave(selectedMood!!, moodNote, audioPath, audioDuration)
                            }
                        },
                        enabled = selectedMood != null && !isRecording && !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        if (isLoading) {
                            RunningLoading(size = 24.dp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("保存中...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("确定", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
}
}

@Composable
fun MoodDay(
    day: CalendarDay,
    isSelected: Boolean,
    moodIcon: String?,
    hasAudio: Boolean,
    isToday: Boolean,
    weather: WeatherInfo? = null,
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
            .clickable(enabled = day.position == DayPosition.MonthDate) { onClick(day.date) },
        contentAlignment = Alignment.Center
    ) {
        if (day.position == DayPosition.MonthDate) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
                
                if (moodIcon != null) {
                    Text(text = moodIcon, fontSize = 12.sp)
                }
            }
            
            if (hasAudio) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Has Audio",
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(12.dp)
                        .padding(2.dp)
                )
            }

            if (weather != null) {
                val icon = when(weather.condition.name) {
                    "Sunny" -> "☀️"
                    "Cloudy" -> "☁️"
                    "Rainy" -> "🌧️"
                    "Snowy" -> "❄️"
                    else -> "🌥️"
                }
                Text(
                    text = icon,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(2.dp)
                )
            }
        }
    }
}

fun getMoodIcon(mood: String?): String? {
    return when (mood) {
        "开心" -> "😄"
        "美滋滋" -> "🥰"
        "心动" -> "😍"
        "激动" -> "🤩"
        "兴奋" -> "😆"
        "享受" -> "😋"
        "平静" -> "😌"
        "小确幸" -> "🍀"
        "冥想" -> "🧘"
        "一般" -> "😶"
        "还行" -> "🙂"
        "平淡" -> "😐"
        "懵圈" -> "😵"
        "难以描述" -> "😵‍💫"
        "难过" -> "😢"
        "焦虑" -> "😰"
        "气愤" -> "😤"
        "倒霉" -> "😫"
        "恐惧" -> "😱"
        "累" -> "😫"
        "颓废" -> "🫠"
        "委屈" -> "🥺"
        "生气" -> "😡"
        "孤独" -> "😔"
        else -> null
    }
}
