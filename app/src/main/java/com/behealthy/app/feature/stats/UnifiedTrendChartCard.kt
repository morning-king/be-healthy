package com.behealthy.app.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.behealthy.app.ui.theme.BritishRed
import com.behealthy.app.ui.theme.OppoGreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max

// UnifiedTrendChartCard Implementation based on spec
@Composable
fun UnifiedTrendChartCard(dailyStats: List<DailyStatItem>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "综合运动趋势",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (dailyStats.isEmpty() || dailyStats.all { it.steps == 0 && it.calories == 0 && it.minutes == 0 }) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                UnifiedTrendChart(dailyStats)
            }
        }
    }
}

@Composable
fun UnifiedTrendChart(dailyStats: List<DailyStatItem>) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    // Colors
    val stepsColor = OppoGreen
    val caloriesColor = BritishRed
    val minutesColor = Color(0xFFE6A23C) // Orange
    
    // Data Preparation
    val dates = dailyStats.map { it.date }
    val steps = dailyStats.map { it.steps.toFloat() }
    val calories = dailyStats.map { it.calories.toFloat() }
    val minutes = dailyStats.map { it.minutes.toFloat() }
    
    // Max Values for Normalization
    val maxSteps = max(steps.maxOrNull() ?: 6000f, 100f) // Avoid div by zero
    val maxCalories = max(calories.maxOrNull() ?: 100f, 10f)
    val maxMinutes = max(minutes.maxOrNull() ?: 60f, 10f)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        // Calculate index based on X coordinate
                        val width = size.width
                        val itemWidth = width / (dailyStats.size + 0.5f) // Adjust for padding
                        val index = (offset.x / width * dailyStats.size).toInt().coerceIn(0, dailyStats.size - 1)
                        selectedIndex = if (selectedIndex == index) null else index
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val pointCount = dailyStats.size
            
            if (pointCount < 2) return@Canvas
            
            val xStep = width / (pointCount - 1).coerceAtLeast(1)
            
            // Draw Grid Lines (Horizontal)
            val gridLines = 5
            for (i in 0..gridLines) {
                val y = height * (i.toFloat() / gridLines)
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Helper to draw path
            fun drawChartPath(data: List<Float>, maxValue: Float, color: Color) {
                val path = Path()
                data.forEachIndexed { index, value ->
                    val x = index * xStep
                    val y = height * (1 - (value / maxValue))
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    
                    // Draw point
                    drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(x, y))
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            
            // Draw Paths
            drawChartPath(minutes, maxMinutes, minutesColor)
            drawChartPath(calories, maxCalories, caloriesColor)
            drawChartPath(steps, maxSteps, stepsColor)
            
            // Draw Selection Indicator
            selectedIndex?.let { index ->
                val x = index * xStep
                drawLine(
                    color = Color.Gray,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }
        }
        
        // Tooltip Overlay
        selectedIndex?.let { index ->
            val item = dailyStats[index]
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            
            // Position tooltip based on index (left or right side to avoid occlusion)
            val alignment = if (index < dailyStats.size / 2) Alignment.TopEnd else Alignment.TopStart
            
            Box(
                modifier = Modifier
                    .align(alignment)
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text(text = item.date.format(dateFormatter), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "步数: ${item.steps}", color = stepsColor, style = MaterialTheme.typography.bodySmall)
                    Text(text = "热量: ${item.calories} kcal", color = caloriesColor, style = MaterialTheme.typography.bodySmall)
                    Text(text = "时长: ${item.minutes} min", color = minutesColor, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        
        // Legend
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendItem("步数", stepsColor)
            LegendItem("热量", caloriesColor)
            LegendItem("时长", minutesColor)
        }
    }
}

@Composable
fun LegendItem(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall)
    }
}
