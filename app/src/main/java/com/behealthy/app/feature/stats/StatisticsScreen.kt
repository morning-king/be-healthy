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
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.BarChart
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
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.behealthy.app.ui.RunningLoading
import com.behealthy.app.ui.theme.BritishBlue
import com.behealthy.app.ui.theme.BritishRed
import com.behealthy.app.ui.theme.OppoGreen
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import android.graphics.Paint
import android.graphics.Typeface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.filled.BugReport
import com.behealthy.app.feature.profile.LogViewerDialog
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
    val healthConnectStatus by viewModel.healthConnectStatus.collectAsState()
    var showDetailDialog by remember { mutableStateOf<DetailType?>(null) }
    var isSyncing by remember { mutableStateOf(false) }
    var showHealthConnectDialog by remember { mutableStateOf(false) }
    var showLogDialog by remember { mutableStateOf(false) }
    var healthConnectDialogMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    // Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (grantedPermissions.containsAll(viewModel.getHealthConnectPermissions())) {
            isSyncing = true
            viewModel.syncData(onComplete = { isSyncing = false })
        } else {
            // Permission denied or partial grant
            healthConnectDialogMessage = "权限请求未完成，请手动在设置中开启所有 Health Connect 权限"
            showHealthConnectDialog = true
        }
    }

    if (showLogDialog) {
        LogViewerDialog(
            onDismiss = { showLogDialog = false },
            onTriggerSync = { viewModel.syncData(onComplete = {}) }
        )
    }

    if (showHealthConnectDialog) {
        AlertDialog(
            onDismissRequest = { showHealthConnectDialog = false },
            title = { Text("Health Connect") },
            text = { Text(healthConnectDialogMessage) },
            confirmButton = {
                TextButton(onClick = { 
                    showHealthConnectDialog = false
                    // Try to open Health Connect settings if it's a permission issue
                    if (healthConnectDialogMessage.contains("权限")) {
                        try {
                            val intent = android.content.Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS")
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to app details
                            try {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = android.net.Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            } catch (e2: Exception) {
                                // Ignore
                            }
                        }
                    }
                }) {
                    Text(if (healthConnectDialogMessage.contains("权限")) "去设置" else "知道了")
                }
            },
            dismissButton = {
                if (healthConnectDialogMessage.contains("权限")) {
                    TextButton(onClick = { showHealthConnectDialog = false }) {
                        Text("取消")
                    }
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Bug Report Button
                SmallFloatingActionButton(
                    onClick = { showLogDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = "诊断")
                }

                FloatingActionButton(
                    onClick = { 
                        // Check permissions first
                        scope.launch {
                            val status = viewModel.getHealthConnectSdkStatus()
                            if (status == HealthConnectClient.SDK_UNAVAILABLE) {
                                healthConnectDialogMessage = "此设备不支持 Health Connect"
                                showHealthConnectDialog = true
                            } else if (status == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
                                healthConnectDialogMessage = "需更新 Health Connect 组件"
                                showHealthConnectDialog = true
                            } else {
                                if (viewModel.hasHealthConnectPermissions()) {
                                    isSyncing = true
                                    viewModel.syncData(onComplete = { isSyncing = false })
                                } else {
                                    try {
                                        permissionLauncher.launch(viewModel.getHealthConnectPermissions())
                                    } catch (e: Exception) {
                                        healthConnectDialogMessage = "启动权限请求失败，请手动在设置中开启 Health Connect 权限"
                                        showHealthConnectDialog = true
                                    }
                                }
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    if (isSyncing) {
                        RunningLoading(size = 32.dp, color = Color.White)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync Data")
                    }
                }
            }
        }
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

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                RunningLoading()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
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
                        ExerciseCurveChartCard(state.dailyStats)
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
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(DateRange.values()) { range ->
            val isSelected = range == selectedRange
            FilterChip(
                selected = isSelected,
                onClick = { onRangeSelected(range) },
                label = { Text(range.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OppoGreen,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("运动趋势 (热量/时长)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            ExerciseCurveChart(dailyStats)
        }
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
        .height(200.dp)) {
        
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
                    color = android.graphics.Color.parseColor("#B71C1C") 
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
                    color = android.graphics.Color.parseColor("#E6A23C") 
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
                    textPaint
                )
            }
            
            // Draw Y-axis value (every point or sparse?)
            // Draw value for max points or all points if not too crowded
             if (showLabel) {
                 val yCal = chartHeight - (item.calories.toFloat() / maxCalories * chartHeight)
                 drawContext.canvas.nativeCanvas.drawText(
                     "${item.calories}",
                     x,
                     yCal - 15f,
                     textPaint.apply { textSize = 20f; color = android.graphics.Color.parseColor("#B71C1C") } // BritishRed
                 )
                 
                 val yMin = chartHeight - (item.minutes.toFloat() / maxMinutes * chartHeight)
                  // Offset yMin text if it overlaps with yCal
                 val yMinTextY = if (Math.abs(yCal - yMin) < 40f) yMin + 35f else yMin - 15f
                 
                 drawContext.canvas.nativeCanvas.drawText(
                     "${item.minutes}",
                     x,
                     yMinTextY,
                     textPaint.apply { textSize = 20f; color = android.graphics.Color.parseColor("#E6A23C") } // Orange
                 )
                 
                 // Reset paint
                 textPaint.color = android.graphics.Color.GRAY
                 textPaint.textSize = 24f
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
        .height(150.dp)) {
        
        val width = size.width
        val height = size.height
        val paddingBottom = 40f
        val chartHeight = height - paddingBottom
        val pointWidth = width / (dailyStats.size + 0.5f)
        
        // 1-5 scale
        val maxScore = 5f
        
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
                    textPaint
                )
            }

            if (item.moodScore > 0) {
                val y = chartHeight - (item.moodScore / maxScore * chartHeight)
                
                if (showLabel) {
                    drawContext.canvas.nativeCanvas.drawText(
                        "${item.moodScore.toInt()}",
                        x,
                        y - 15f,
                        textPaint.apply { textSize = 20f; color = android.graphics.Color.parseColor("#00247D") } // BritishBlue
                    )
                     // Reset paint
                     textPaint.color = android.graphics.Color.GRAY
                     textPaint.textSize = 24f
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    style = valueStyle,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
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
                        Divider(color = Color.LightGray.copy(alpha = 0.2f))
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
