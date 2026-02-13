package com.behealthy.app.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import kotlin.math.roundToInt
import com.behealthy.app.ui.RunningLoading
import com.behealthy.app.ui.theme.BritishBlue
import com.behealthy.app.ui.theme.BritishRed
import com.behealthy.app.ui.theme.OppoGreen
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.toColorInt
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.launch

// Mood icon mapping
private val moodIcons = mapOf(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel(),
    onPlanOverviewClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val state by viewModel.uiState.collectAsState()
    var showDetailDialog by remember { mutableStateOf<DetailType?>(null) }

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        // Custom Date Picker State
        var showDateRangePicker by remember { mutableStateOf(false) }
        
        if (showDateRangePicker) {
            val datePickerState = rememberDateRangePickerState()
            DatePickerDialog(
                onDismissRequest = { showDateRangePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val startMillis = datePickerState.selectedStartDateMillis
                            val endMillis = datePickerState.selectedEndDateMillis
                            if (startMillis != null && endMillis != null) {
                                val start = java.time.Instant.ofEpochMilli(startMillis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                val end = java.time.Instant.ofEpochMilli(endMillis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                viewModel.setCustomDateRange(start, end)
                            }
                            showDateRangePicker = false
                        }
                    ) { Text("确定", color = OppoGreen) }
                },
                dismissButton = {
                    TextButton(onClick = { showDateRangePicker = false }) { Text("取消", color = MaterialTheme.colorScheme.error) }
                }
            ) {
                DateRangePicker(
                    state = datePickerState,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Date Range Selector
                DateRangeSelector(
                    selectedRange = state.selectedDateRange,
                    onRangeSelected = { 
                        if (it == DateRange.CUSTOM) {
                            showDateRangePicker = true
                        } else {
                            viewModel.setDateRange(it) 
                        }
                    }
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Summary Section
                    item {
                        SummaryGrid(state, onDetailClick = { showDetailDialog = it })
                    }

                    // Analysis Section
                    item {
                        AnalysisCard(
                            title = "运动分析",
                            content = state.exerciseAnalysis,
                            icon = "🏃"
                        )
                    }
                    
                    item {
                        AnalysisCard(
                            title = "心情分析",
                            content = state.moodAnalysis,
                            icon = "🧘"
                        )
                    }

                    // Charts Section
                    item {
                        Text(
                            text = "趋势统计",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        UnifiedTrendChartCard(state.dailyStats)
                    }

                    item {
                        MoodCurveChartCard(state.dailyStats)
                    }

                    // Mood Distribution
                    item {
                         Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "心情分布",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { viewModel.toggleMoodChartType() }) {
                                Icon(
                                    imageVector = if (state.isMoodPieChart) Icons.Default.BarChart else Icons.Default.PieChart,
                                    contentDescription = "Toggle Chart Type",
                                    tint = OppoGreen
                                )
                            }
                        }
                        if (state.isMoodPieChart) {
                            MoodPieChart(state.moodStats)
                        } else {
                            MoodDistributionChart(state.moodStats)
                        }
                    }

                    // Plan Stats
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "计划详情",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onPlanOverviewClick) {
                                Text("查看全部", color = OppoGreen)
                            }
                        }
                    }

                    items(state.planStats) { planStat ->
                        PlanStatCard(planStat)
                    }
                }
            }

            // Loading Overlay
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)) // Make loading overlay also semi-transparent
                        .pointerInput(Unit) { detectTapGestures {} },
                    contentAlignment = Alignment.Center
                ) {
                    RunningLoading()
                }
            }
        }
        
        if (showDetailDialog != null) {
            DetailDialog(
                type = showDetailDialog!!,
                dailyStats = state.dailyStats,
                onDismiss = { showDetailDialog = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeSelector(
    selectedRange: DateRange,
    onRangeSelected: (DateRange) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Reduced vertical padding
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp) // Reduced spacing
    ) {
        items(DateRange.values()) { range ->
            val isSelected = range == selectedRange
            FilterChip(
                selected = isSelected,
                onClick = { onRangeSelected(range) },
                label = { 
                    Text(
                        text = range.label, 
                        style = MaterialTheme.typography.labelSmall // Reduced font size
                    ) 
                },
                enabled = true,
                modifier = Modifier.height(32.dp), // Reduced height (approx 30% less than standard 48dp)
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OppoGreen,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) OppoGreen else MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
fun SummaryGrid(state: StatisticsUiState, onDetailClick: (DetailType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCard(
                title = "总运动天数",
                value = "${state.totalExerciseDays}",
                unit = "天",
                color = BritishBlue,
                modifier = Modifier.weight(1f).clickable { onDetailClick(DetailType.ExerciseDays) }
            )
            SummaryCard(
                title = "总消耗热量",
                value = "${state.totalCaloriesBurned}",
                unit = "Kcal",
                color = BritishRed,
                modifier = Modifier.weight(1f).clickable { onDetailClick(DetailType.Calories) }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCard(
                title = "总运动时长",
                value = "${state.totalDurationMinutes}",
                unit = "分钟",
                color = Color(0xFFE6A23C), // Warning/Orange
                modifier = Modifier.weight(1f).clickable { onDetailClick(DetailType.Duration) }
            )
            SummaryCard(
                title = "总步数",
                value = "${state.totalSteps}",
                unit = "步",
                color = OppoGreen,
                modifier = Modifier.weight(1f).clickable { onDetailClick(DetailType.Steps) }
            )
        }
        
        // Average Stats
        Text(
            text = "日均数据 (仅计算运动日)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                        title = "日均热量",
                        value = "${state.avgCaloriesBurned}",
                        unit = "Kcal",
                        color = BritishRed,
                        modifier = Modifier.weight(1f),
                        valueStyle = MaterialTheme.typography.titleMedium
                    )
                    SummaryCard(
                        title = "日均时长",
                        value = "${state.avgDurationMinutes}",
                        unit = "分钟",
                        color = Color(0xFFE6A23C),
                        modifier = Modifier.weight(1f),
                        valueStyle = MaterialTheme.typography.titleMedium
                    )
                    SummaryCard(
                        title = "日均步数",
                        value = "${state.avgSteps}",
                        unit = "步",
                        color = OppoGreen,
                        modifier = Modifier.weight(1f),
                        valueStyle = MaterialTheme.typography.titleMedium
                    )
        }
    }
}

@Composable
fun AnalysisCard(title: String, content: String, icon: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = icon, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(end = 12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ExerciseCurveChartCard(dailyStats: List<DailyStatItem>) {
    var isHistogram by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("运动趋势 (热量/时长)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Legend
                    Box(modifier = Modifier.size(8.dp).background(BritishRed, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("热量", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFE6A23C), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("时长", style = MaterialTheme.typography.labelSmall)
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Toggle Button
                    IconButton(
                        onClick = { isHistogram = !isHistogram },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isHistogram) Icons.Default.BarChart else Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = "Toggle Chart",
                            tint = OppoGreen
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isHistogram) {
                ExerciseHistogramChart(dailyStats)
            } else {
                ExerciseCurveChart(dailyStats)
            }
        }
    }
}

@Composable
fun ExerciseHistogramChart(dailyStats: List<DailyStatItem>) {
    if (dailyStats.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxCalories = dailyStats.maxOfOrNull { it.calories }?.takeIf { it > 0 } ?: 100
    val maxMinutes = dailyStats.maxOfOrNull { it.minutes }?.takeIf { it > 0 } ?: 60
    
    // Animation
    var animationPlayed by remember { mutableStateOf(false) }
    val progress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "barGrowth"
    )
    
    LaunchedEffect(Unit) {
        animationPlayed = true
    }
    
    // Tech Scan Effect
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "scan")
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(2000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "scanLine"
    )

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT
        }
    }

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    val width = size.width
                    val barWidth = width / dailyStats.size
                    val index = (offset.x / barWidth).toInt().coerceIn(dailyStats.indices)
                    selectedIndex = if (selectedIndex == index) null else index
                }
            )
        }
    ) {
        val width = size.width
        val height = size.height
        val paddingBottom = 40f
        val chartHeight = height - paddingBottom
        val barWidth = width / dailyStats.size
        val barSpacing = barWidth * 0.2f
        val actualBarWidth = barWidth - barSpacing
        
        // Grid
        val gridLines = 4
        val rowHeight = chartHeight / gridLines
        for (i in 0..gridLines) {
            val y = i * rowHeight
            drawLine(
                color = Color.LightGray.copy(alpha = 0.2f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        dailyStats.forEachIndexed { index, item ->
            val x = index * barWidth + barSpacing / 2
            
            // 1. Calories Bar (Background/Wider)
            val calHeight = (item.calories.toFloat() / maxCalories * chartHeight) * progress
            val calTop = chartHeight - calHeight
            
            drawRoundRect(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(BritishRed.copy(alpha = 0.8f), BritishRed.copy(alpha = 0.3f)),
                    startY = calTop,
                    endY = chartHeight
                ),
                topLeft = Offset(x, calTop),
                size = androidx.compose.ui.geometry.Size(actualBarWidth, calHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            
            // 2. Minutes Bar (Foreground/Narrower)
            val minHeight = (item.minutes.toFloat() / maxMinutes * chartHeight) * progress
            val minTop = chartHeight - minHeight
            val minBarWidth = actualBarWidth * 0.5f
            val minBarX = x + (actualBarWidth - minBarWidth) / 2
            
            drawRoundRect(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFFE6A23C).copy(alpha = 0.9f), Color(0xFFE6A23C).copy(alpha = 0.4f)),
                    startY = minTop,
                    endY = chartHeight
                ),
                topLeft = Offset(minBarX, minTop),
                size = androidx.compose.ui.geometry.Size(minBarWidth, minHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            
            // Date Label
            val showLabel = if (dailyStats.size > 10) index % 3 == 0 else true
            if (showLabel) {
                drawContext.canvas.nativeCanvas.drawText(
                    item.date.format(DateTimeFormatter.ofPattern("MM-dd")),
                    x + actualBarWidth / 2,
                    height - 10f,
                    textPaint.apply { color = android.graphics.Color.GRAY; textSize = 24f }
                )
            }
            
            // Tooltip
            if (index == selectedIndex) {
                // Highlight
                drawRect(
                    color = Color.White.copy(alpha = 0.1f),
                    topLeft = Offset(index * barWidth, 0f),
                    size = androidx.compose.ui.geometry.Size(barWidth, height)
                )
                
                // Tooltip Box (Same logic as Line Chart)
                val tooltipWidth = 260f
                val tooltipHeight = 130f
                val tooltipX = if (x + tooltipWidth > width) x - tooltipWidth - 20f else x + 20f
                val tooltipY = 10f
                
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.95f),
                    topLeft = Offset(tooltipX, tooltipY),
                    size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                )
                drawRoundRect(
                    color = OppoGreen, // Tech Green Border
                    topLeft = Offset(tooltipX, tooltipY),
                    size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                    style = Stroke(width = 2.dp.toPx())
                )
                
                // Text
                val dateStr = item.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                drawContext.canvas.nativeCanvas.drawText(
                    dateStr,
                    tooltipX + 20f,
                    tooltipY + 40f,
                    textPaint.apply { textSize = 26f; color = android.graphics.Color.BLACK; textAlign = Paint.Align.LEFT; typeface = Typeface.DEFAULT_BOLD }
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "热量: ${item.calories} Kcal",
                    tooltipX + 20f,
                    tooltipY + 75f,
                    textPaint.apply { textSize = 24f; color = android.graphics.Color.DKGRAY; typeface = Typeface.DEFAULT }
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "时长: ${item.minutes} min",
                    tooltipX + 20f,
                    tooltipY + 105f,
                    textPaint.apply { textSize = 24f; color = android.graphics.Color.DKGRAY }
                )
                
                // Reset
                textPaint.color = android.graphics.Color.GRAY
                textPaint.textAlign = Paint.Align.CENTER
            }
        }
        
        // Tech Scan Line Overlay
        val scanLineY = scanY * height
        drawLine(
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                colors = listOf(Color.Transparent, OppoGreen.copy(alpha = 0.5f), Color.Transparent)
            ),
            start = Offset(0f, scanLineY),
            end = Offset(width, scanLineY),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun StepTrendChartCard(dailyStats: List<DailyStatItem>) {
    var isHistogram by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("步数趋势", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Legend
                    Box(modifier = Modifier.size(8.dp).background(OppoGreen, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("步数", style = MaterialTheme.typography.labelSmall)
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Toggle Button
                    IconButton(
                        onClick = { isHistogram = !isHistogram },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isHistogram) Icons.Default.BarChart else Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = "Toggle Chart",
                            tint = OppoGreen
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isHistogram) {
                StepHistogramChart(dailyStats)
            } else {
                StepCurveChart(dailyStats)
            }
        }
    }
}

@Composable
fun StepHistogramChart(dailyStats: List<DailyStatItem>) {
    if (dailyStats.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxSteps = dailyStats.maxOfOrNull { it.steps }?.takeIf { it > 0 } ?: 6000
    
    // Animation
    var animationPlayed by remember { mutableStateOf(false) }
    val progress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "barGrowth"
    )
    
    LaunchedEffect(Unit) {
        animationPlayed = true
    }
    
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT
        }
    }

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    val width = size.width
                    val barWidth = width / dailyStats.size
                    val index = (offset.x / barWidth).toInt().coerceIn(dailyStats.indices)
                    selectedIndex = if (selectedIndex == index) null else index
                }
            )
        }
    ) {
        val width = size.width
        val height = size.height
        val paddingBottom = 40f
        val chartHeight = height - paddingBottom
        val barWidth = width / dailyStats.size
        val barSpacing = barWidth * 0.2f
        val actualBarWidth = barWidth - barSpacing
        
        // Grid
        val gridLines = 4
        val rowHeight = chartHeight / gridLines
        for (i in 0..gridLines) {
            val y = i * rowHeight
            drawLine(
                color = Color.LightGray.copy(alpha = 0.2f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        dailyStats.forEachIndexed { index, item ->
            val x = index * barWidth + barSpacing / 2
            
            // Steps Bar
            val stepHeight = (item.steps.toFloat() / maxSteps * chartHeight) * progress
            val stepTop = chartHeight - stepHeight
            
            drawRoundRect(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(OppoGreen.copy(alpha = 0.8f), OppoGreen.copy(alpha = 0.3f)),
                    startY = stepTop,
                    endY = chartHeight
                ),
                topLeft = Offset(x, stepTop),
                size = androidx.compose.ui.geometry.Size(actualBarWidth, stepHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            
            // Date Label
            val showLabel = if (dailyStats.size > 10) index % 3 == 0 else true
            if (showLabel) {
                drawContext.canvas.nativeCanvas.drawText(
                    item.date.format(DateTimeFormatter.ofPattern("MM-dd")),
                    x + actualBarWidth / 2,
                    height - 10f,
                    textPaint.apply { color = android.graphics.Color.GRAY; textSize = 24f }
                )
            }
            
            // Tooltip
            if (index == selectedIndex) {
                 // Highlight
                drawRect(
                    color = Color.White.copy(alpha = 0.1f),
                    topLeft = Offset(index * barWidth, 0f),
                    size = androidx.compose.ui.geometry.Size(barWidth, height)
                )

                val tooltipWidth = 200f
                val tooltipHeight = 100f
                val tooltipX = if (x + tooltipWidth > width) x - tooltipWidth - 20f else x + 20f
                val tooltipY = 10f
                
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.95f),
                    topLeft = Offset(tooltipX, tooltipY),
                    size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                )
                drawRoundRect(
                    color = OppoGreen,
                    topLeft = Offset(tooltipX, tooltipY),
                    size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                    style = Stroke(width = 2.dp.toPx())
                )
                
                val dateStr = item.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                drawContext.canvas.nativeCanvas.drawText(
                    dateStr,
                    tooltipX + 20f,
                    tooltipY + 40f,
                    textPaint.apply { textSize = 26f; color = android.graphics.Color.BLACK; textAlign = Paint.Align.LEFT; typeface = Typeface.DEFAULT_BOLD }
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "步数: ${item.steps}",
                    tooltipX + 20f,
                    tooltipY + 75f,
                    textPaint.apply { textSize = 24f; color = android.graphics.Color.DKGRAY; typeface = Typeface.DEFAULT }
                )
            }
        }
    }
}

@Composable
fun StepCurveChart(dailyStats: List<DailyStatItem>) {
    if (dailyStats.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxSteps = dailyStats.maxOfOrNull { it.steps }?.takeIf { it > 0 } ?: 6000
    
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT
        }
    }

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    val width = size.width
                    val pointWidth = width / (dailyStats.size + 0.5f)
                    val index = ((offset.x - pointWidth / 2) / pointWidth).roundToInt()
                    if (index in dailyStats.indices) {
                        selectedIndex = if (selectedIndex == index) null else index
                    } else {
                        selectedIndex = null
                    }
                }
            )
        }
    ) {
        val width = size.width
        val height = size.height
        val paddingBottom = 40f
        val chartHeight = height - paddingBottom
        val pointWidth = width / (dailyStats.size + 0.5f)
        
        // Grid
        val gridLines = 4
        val rowHeight = chartHeight / gridLines
        for (i in 0..gridLines) {
            val y = i * rowHeight
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
            
            val stepValue = (maxSteps * (1 - i.toFloat()/gridLines)).toInt()
            drawContext.canvas.nativeCanvas.drawText(
                "$stepValue",
                0f,
                y - 5f,
                textPaint.apply { 
                    textSize = 20f
                    color = android.graphics.Color.GRAY
                    textAlign = Paint.Align.LEFT
                }
            )
        }
        
        val stepsPath = Path()
        
        dailyStats.forEachIndexed { index, item ->
            val x = index * pointWidth + pointWidth / 2
            val y = chartHeight - (item.steps.toFloat() / maxSteps * chartHeight)
            
            if (index == 0) stepsPath.moveTo(x, y) else stepsPath.lineTo(x, y)
            
            drawCircle(
                color = OppoGreen,
                center = Offset(x, y),
                radius = 3.dp.toPx()
            )
            
            // Date Label
            val showLabel = if (dailyStats.size > 10) index % 3 == 0 else true
            if (showLabel) {
                drawContext.canvas.nativeCanvas.drawText(
                    item.date.format(DateTimeFormatter.ofPattern("MM-dd")),
                    x,
                    height - 10f,
                    textPaint.apply { color = android.graphics.Color.GRAY; textSize = 24f; textAlign = Paint.Align.CENTER }
                )
            }
            
            if (index == selectedIndex) {
                 drawLine(
                     color = Color.Gray.copy(alpha = 0.5f),
                     start = Offset(x, 0f),
                     end = Offset(x, chartHeight),
                     strokeWidth = 1.dp.toPx(),
                     pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                 )
                 
                 val tooltipWidth = 200f
                 val tooltipHeight = 100f
                 val tooltipX = if (x + tooltipWidth > width) x - tooltipWidth - 20f else x + 20f
                 val tooltipY = 10f
                 
                 drawRoundRect(
                     color = Color.White.copy(alpha = 0.95f),
                     topLeft = Offset(tooltipX, tooltipY),
                     size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                     cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                 )
                 drawRoundRect(
                     color = Color.LightGray,
                     topLeft = Offset(tooltipX, tooltipY),
                     size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                     cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                     style = Stroke(width = 1.dp.toPx())
                 )
                 
                 val dateStr = item.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                 drawContext.canvas.nativeCanvas.drawText(
                     dateStr,
                     tooltipX + 20f,
                     tooltipY + 40f,
                     textPaint.apply { textSize = 26f; color = android.graphics.Color.BLACK; textAlign = Paint.Align.LEFT; typeface = Typeface.DEFAULT_BOLD }
                 )
                 drawContext.canvas.nativeCanvas.drawText(
                     "步数: ${item.steps}",
                     tooltipX + 20f,
                     tooltipY + 75f,
                     textPaint.apply { textSize = 24f; color = android.graphics.Color.DKGRAY; typeface = Typeface.DEFAULT }
                 )
            }
        }
        
        drawPath(
            path = stepsPath,
            color = OppoGreen,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun MoodCurveChartCard(dailyStats: List<DailyStatItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("心情曲线", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            MoodCurveChart(dailyStats)
        }
    }
}

@Composable
fun ExerciseCurveChart(dailyStats: List<DailyStatItem>) {
    if (dailyStats.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxCalories = dailyStats.maxOfOrNull { it.calories }?.takeIf { it > 0 } ?: 100
    val maxMinutes = dailyStats.maxOfOrNull { it.minutes }?.takeIf { it > 0 } ?: 60
    
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT
        }
    }

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    val width = size.width
                    val pointWidth = width / (dailyStats.size + 0.5f)
                    val index = ((offset.x - pointWidth / 2) / pointWidth).roundToInt()
                    if (index in dailyStats.indices) {
                        selectedIndex = if (selectedIndex == index) null else index
                    } else {
                        selectedIndex = null
                    }
                }
            )
        }
    ) {
        
        val width = size.width
        val height = size.height
        val paddingBottom = 40f
        val chartHeight = height - paddingBottom
        val pointWidth = width / (dailyStats.size + 0.5f)
        
        // Draw Grid
        val gridLines = 4
        val rowHeight = chartHeight / gridLines
        for (i in 0..gridLines) {
            val y = i * rowHeight
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
            
            // Y-Axis Labels
            // Calories (Left, Red)
            val calValue = (maxCalories * (1 - i.toFloat()/gridLines)).toInt()
            drawContext.canvas.nativeCanvas.drawText(
                "$calValue",
                0f,
                y - 5f,
                textPaint.apply { 
                    textSize = 20f
                    color = "#B71C1C".toColorInt() 
                    textAlign = Paint.Align.LEFT
                }
            )
            
            // Minutes (Right, Orange)
            val minValue = (maxMinutes * (1 - i.toFloat()/gridLines)).toInt()
            drawContext.canvas.nativeCanvas.drawText(
                "$minValue",
                width,
                y - 5f,
                textPaint.apply { 
                    textSize = 20f
                    color = "#E6A23C".toColorInt() 
                    textAlign = Paint.Align.RIGHT
                }
            )
            
            // Reset Paint
            textPaint.color = android.graphics.Color.GRAY
            textPaint.textSize = 24f
            textPaint.textAlign = Paint.Align.CENTER
        }
        
        // Draw Calories Line (Red)
        val caloriesPath = Path()
        // Draw Minutes Line (Orange)
        val minutesPath = Path()

        dailyStats.forEachIndexed { index, item ->
            val x = index * pointWidth + pointWidth / 2
            
            // Calories
            val yCal = chartHeight - (item.calories.toFloat() / maxCalories * chartHeight)
            if (index == 0) caloriesPath.moveTo(x, yCal) else caloriesPath.lineTo(x, yCal)
            
            drawCircle(
                color = BritishRed,
                center = Offset(x, yCal),
                radius = 3.dp.toPx()
            )

            // Minutes
            val yMin = chartHeight - (item.minutes.toFloat() / maxMinutes * chartHeight)
            if (index == 0) minutesPath.moveTo(x, yMin) else minutesPath.lineTo(x, yMin)

            drawCircle(
                color = Color(0xFFE6A23C),
                center = Offset(x, yMin),
                radius = 3.dp.toPx()
            )
            
            // Date Label (every 2-3 items if too many)
            val showLabel = if (dailyStats.size > 10) index % 3 == 0 else true
            if (showLabel) {
                drawContext.canvas.nativeCanvas.drawText(
                    item.date.format(DateTimeFormatter.ofPattern("MM-dd")),
                    x,
                    height - 10f,
                    textPaint.apply { color = android.graphics.Color.GRAY; textSize = 24f; textAlign = Paint.Align.CENTER }
                )
            }
            
            // Tooltip for Selected Index
            if (index == selectedIndex) {
                 // Draw vertical line
                 drawLine(
                     color = Color.Gray.copy(alpha = 0.5f),
                     start = Offset(x, 0f),
                     end = Offset(x, chartHeight),
                     strokeWidth = 1.dp.toPx(),
                     pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                 )
                 
                 // Draw Detail Box (Tooltip)
                 val tooltipWidth = 260f
                 val tooltipHeight = 130f
                 val tooltipX = if (x + tooltipWidth > width) x - tooltipWidth - 20f else x + 20f
                 val tooltipY = 10f
                 
                 drawRoundRect(
                     color = Color.White.copy(alpha = 0.95f),
                     topLeft = Offset(tooltipX, tooltipY),
                     size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                     cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                 )
                 drawRoundRect(
                     color = Color.LightGray,
                     topLeft = Offset(tooltipX, tooltipY),
                     size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                     cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                     style = Stroke(width = 1.dp.toPx())
                 )
                 
                 // Text in Tooltip
                 val dateStr = item.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                 drawContext.canvas.nativeCanvas.drawText(
                     dateStr,
                     tooltipX + 20f,
                     tooltipY + 40f,
                     textPaint.apply { 
                         textSize = 26f 
                         color = android.graphics.Color.BLACK 
                         textAlign = Paint.Align.LEFT
                         typeface = Typeface.DEFAULT_BOLD
                     }
                 )
                 
                 drawContext.canvas.nativeCanvas.drawText(
                     "热量: ${item.calories} Kcal",
                     tooltipX + 20f,
                     tooltipY + 75f,
                     textPaint.apply { textSize = 24f; color = android.graphics.Color.DKGRAY; typeface = Typeface.DEFAULT }
                 )
                 
                 drawContext.canvas.nativeCanvas.drawText(
                     "时长: ${item.minutes} min",
                     tooltipX + 20f,
                     tooltipY + 105f,
                     textPaint.apply { textSize = 24f; color = android.graphics.Color.DKGRAY }
                 )
                 
                 // Reset Paint
                 textPaint.color = android.graphics.Color.GRAY
                 textPaint.textSize = 24f
                 textPaint.textAlign = Paint.Align.CENTER
            }
        }
        
        drawPath(
            path = caloriesPath,
            color = BritishRed,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        drawPath(
            path = minutesPath,
            color = Color(0xFFE6A23C),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun MoodCurveChart(dailyStats: List<DailyStatItem>) {
    if (dailyStats.isEmpty()) {
         Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
            Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT
        }
    }
    
    // Representative icons for Y-axis
    val yAxisIcons = mapOf(
        5 to "😄", // Excellent
        4 to "😌", // Good
        3 to "😐", // Average
        2 to "😵", // Poor
        1 to "😢"  // Bad
    )

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(180.dp) // Increased height for better visibility
        .padding(start = 24.dp) // Padding for Y-axis
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    val width = size.width
                    val pointWidth = width / (dailyStats.size + 0.5f)
                    val index = ((offset.x - pointWidth / 2) / pointWidth).roundToInt()
                    if (index in dailyStats.indices) {
                        selectedIndex = if (selectedIndex == index) null else index
                    } else {
                        selectedIndex = null
                    }
                }
            )
        }
    ) {
        
        val width = size.width
        val height = size.height
        val paddingBottom = 40f
        val chartHeight = height - paddingBottom
        val pointWidth = width / (dailyStats.size + 0.5f)
        
        // 1-5 scale
        val maxScore = 5f
        
        // Draw Y-Axis Icons
        yAxisIcons.forEach { (score, icon) ->
            val y = chartHeight - (score / maxScore * chartHeight)
            drawContext.canvas.nativeCanvas.drawText(
                icon,
                -20f, // Draw to the left of the chart area
                y + 10f, // Center vertically relative to the grid line
                textPaint.apply { textSize = 30f; textAlign = Paint.Align.LEFT }
            )
            
            // Draw faint grid line
            drawLine(
                color = Color.LightGray.copy(alpha = 0.2f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // Draw Mood Line (Blue)
        val moodPath = Path()
        var hasStart = false
        
        dailyStats.forEachIndexed { index, item ->
            val x = index * pointWidth + pointWidth / 2
            
            // Date Label
            val showLabel = if (dailyStats.size > 10) index % 3 == 0 else true
            if (showLabel) {
                drawContext.canvas.nativeCanvas.drawText(
                    item.date.format(DateTimeFormatter.ofPattern("MM-dd")),
                    x,
                    height - 10f,
                    textPaint.apply { textSize = 24f; textAlign = Paint.Align.CENTER; color = android.graphics.Color.GRAY }
                )
            }

            if (item.moodScore > 0) {
                val y = chartHeight - (item.moodScore / maxScore * chartHeight)
                
                // Draw icon at data point if space permits or if it's a key point
                if (showLabel) {
                    val icon = moodIcons[item.mood] ?: "😊"
                    drawContext.canvas.nativeCanvas.drawText(
                        icon,
                        x,
                        y - 15f,
                        textPaint.apply { textSize = 32f } 
                    )
                }
                
                if (!hasStart) {
                    moodPath.moveTo(x, y)
                    hasStart = true
                } else {
                    moodPath.lineTo(x, y)
                }
                
                drawCircle(
                    color = BritishBlue,
                    center = Offset(x, y),
                    radius = 3.dp.toPx()
                )
                
                // Tooltip for Selected Index
                if (index == selectedIndex) {
                     // Draw vertical line
                     drawLine(
                         color = Color.Gray.copy(alpha = 0.5f),
                         start = Offset(x, 0f),
                         end = Offset(x, chartHeight),
                         strokeWidth = 1.dp.toPx(),
                         pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                     )
                     
                     // Draw Detail Box (Tooltip)
                     val tooltipWidth = 240f
                     val tooltipHeight = 120f
                     val tooltipX = if (x + tooltipWidth > width) x - tooltipWidth - 20f else x + 20f
                     val tooltipY = 10f
                     
                     drawRoundRect(
                         color = Color.White.copy(alpha = 0.95f),
                         topLeft = Offset(tooltipX, tooltipY),
                         size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                         cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                     )
                     drawRoundRect(
                         color = Color.LightGray,
                         topLeft = Offset(tooltipX, tooltipY),
                         size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                         cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                         style = Stroke(width = 1.dp.toPx())
                     )
                     
                     // Text in Tooltip
                     val dateStr = item.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                     drawContext.canvas.nativeCanvas.drawText(
                         dateStr,
                         tooltipX + 20f,
                         tooltipY + 40f,
                         textPaint.apply { 
                             textSize = 26f 
                             color = android.graphics.Color.BLACK 
                             textAlign = Paint.Align.LEFT
                             typeface = Typeface.DEFAULT_BOLD
                         }
                     )
                     
                     val moodIcon = moodIcons[item.mood] ?: "😊"
                     drawContext.canvas.nativeCanvas.drawText(
                         "$moodIcon ${item.mood}",
                         tooltipX + 20f,
                         tooltipY + 80f,
                         textPaint.apply { textSize = 28f; color = android.graphics.Color.DKGRAY; typeface = Typeface.DEFAULT }
                     )
                     
                     // Reset Paint
                     textPaint.color = android.graphics.Color.GRAY
                     textPaint.textSize = 24f
                     textPaint.textAlign = Paint.Align.CENTER
                }
            }
        }
        
        if (hasStart) {
            drawPath(
                path = moodPath,
                color = BritishBlue,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun MoodPieChart(moodStats: Map<String, Int>) {
    if (moodStats.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("暂无心情记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val total = moodStats.values.sum()
    val sortedStats = moodStats.entries.sortedByDescending { it.value }
    
    // Simple Palette
    val colors = listOf(
        BritishBlue, 
        BritishRed, 
        OppoGreen, 
        Color(0xFFE6A23C), // Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFF00BCD4), // Cyan
        Color(0xFFFF9800), // Deep Orange
        Color(0xFF795548)  // Brown
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pie Chart
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val radius = minOf(width, height) / 2
                val center = Offset(width / 2, height / 2)
                
                var startAngle = -90f
                
                sortedStats.forEachIndexed { index, entry ->
                    val sweepAngle = (entry.value.toFloat() / total) * 360f
                    val color = colors.getOrElse(index) { Color.Gray }
                    
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        topLeft = Offset(center.x - radius, center.y - radius)
                    )
                    
                    startAngle += sweepAngle
                }
                
                // Donut hole
                drawCircle(
                    color = Color.White, 
                    radius = radius * 0.5f,
                    center = center
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Legend
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            sortedStats.take(5).forEachIndexed { index, entry ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(colors.getOrElse(index) { Color.Gray })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = moodIcons[entry.key] ?: "😊",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${entry.key} (${(entry.value.toFloat() / total * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoodDistributionChart(moodStats: Map<String, Int>) {
    if (moodStats.isEmpty()) {
        Text("暂无心情记录", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
        return
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        moodStats.entries.sortedByDescending { it.value }.forEach { (mood, count) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.width(80.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = moodIcons[mood] ?: "😊",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = mood,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp
                    )
                }
                
                // Bar
                val maxCount = moodStats.values.maxOrNull() ?: 1
                val fraction = count.toFloat() / maxCount
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(BritishBlue)
                    )
                }
                
                Text(
                    text = "$count",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = valueStyle,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
    }
}

@Composable
fun PlanStatCard(planStat: PlanStatItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = planStat.planName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "完成 ${planStat.completedTasks} / 总计 ${planStat.totalTasks}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "${planStat.totalCalories} Kcal",
                style = MaterialTheme.typography.labelMedium,
                color = BritishRed,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

enum class DetailType {
    ExerciseDays, Calories, Duration, Steps
}

@Composable
fun DetailDialog(
    type: DetailType,
    dailyStats: List<DailyStatItem>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = when (type) {
                DetailType.ExerciseDays -> "运动天数详情"
                DetailType.Calories -> "热量消耗详情"
                DetailType.Duration -> "运动时长详情"
                DetailType.Steps -> "步数详情"
            })
        },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                items(dailyStats.reversed()) { item ->
                    if ((type == DetailType.ExerciseDays && (item.minutes > 0 || item.calories > 0)) ||
                        (type == DetailType.Calories && item.calories > 0) ||
                        (type == DetailType.Duration && item.minutes > 0) ||
                        (type == DetailType.Steps && item.steps > 0)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.date.format(DateTimeFormatter.ofPattern("MM-dd")))
                            Text(
                                when (type) {
                                    DetailType.ExerciseDays -> "${item.minutes}分钟"
                                    DetailType.Calories -> "${item.calories}Kcal"
                                    DetailType.Duration -> "${item.minutes}分钟"
                                    DetailType.Steps -> "${item.steps}步"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}


