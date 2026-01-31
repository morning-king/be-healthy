package com.behealthy.app.feature.plan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.behealthy.app.ui.RunningLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanCreationScreen(
    viewModel: PlanViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.resetSaveState()
            onNavigateBack()
        }
    }

    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var durationType by remember { mutableStateOf("MONTH") }
    var durationQuantity by remember { mutableStateOf("1") }
    
    // Date Selection
    var showDatePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    
    // Work Day Config
    var workDayDiet by remember { mutableStateOf(true) }
    var workDayExercise by remember { mutableStateOf(true) }
    var workDayMinutes by remember { mutableStateOf("30") }
    var workDaySteps by remember { mutableStateOf("5000") }
    var workDayCalories by remember { mutableStateOf("300") }
    
    // Rest Day Config
    var restDayDiet by remember { mutableStateOf(true) }
    var restDayExercise by remember { mutableStateOf(false) }
    var restDayMinutes by remember { mutableStateOf("0") }
    var restDayCalories by remember { mutableStateOf("0") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("创建健身计划") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Info
            Text("基本信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("计划名称") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = target,
                onValueChange = { target = it },
                label = { Text("计划目标") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Duration Type & Quantity
            Text("持续时间", style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = durationType == "MONTH", onClick = { durationType = "MONTH" })
                Text("按月")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = durationType == "WEEK", onClick = { durationType = "WEEK" })
                Text("按周")
            }
            TextField(
                value = durationQuantity,
                onValueChange = { durationQuantity = it },
                label = { Text("持续数量 (例如: 1个月/2周)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Start Date
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("开始日期: ${startDate.format(formatter)}")
            }
            
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                startDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            }
                            showDatePicker = false
                        }) {
                            Text("确定")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("取消")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            HorizontalDivider()

            // Work Day Config
            Text("工作日配置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("包含饮食计划", modifier = Modifier.weight(1f))
                Switch(checked = workDayDiet, onCheckedChange = { workDayDiet = it })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("包含运动计划", modifier = Modifier.weight(1f))
                Switch(checked = workDayExercise, onCheckedChange = { workDayExercise = it })
            }
            if (workDayExercise) {
                TextField(
                    value = workDayMinutes,
                    onValueChange = { workDayMinutes = it },
                    label = { Text("运动目标时间 (分钟)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = workDaySteps,
                    onValueChange = { workDaySteps = it },
                    label = { Text("运动目标步数") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = workDayCalories,
                    onValueChange = { workDayCalories = it },
                    label = { Text("运动目标消耗热量 (Kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider()

            // Rest Day Config
            Text("非工作日配置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("包含饮食计划", modifier = Modifier.weight(1f))
                Switch(checked = restDayDiet, onCheckedChange = { restDayDiet = it })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("包含运动计划", modifier = Modifier.weight(1f))
                Switch(checked = restDayExercise, onCheckedChange = { restDayExercise = it })
            }
            if (restDayExercise) {
                TextField(
                    value = restDayMinutes,
                    onValueChange = { restDayMinutes = it },
                    label = { Text("运动目标时间 (分钟)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = restDayCalories,
                    onValueChange = { restDayCalories = it },
                    label = { Text("运动目标消耗热量 (Kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Button(
                onClick = {
                    viewModel.addPlan(
                        name = name,
                        durationType = durationType,
                        durationQuantity = durationQuantity.toIntOrNull() ?: 1,
                        startDate = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        endDate = calculateEndDate(
                            startDate, 
                            durationType, 
                            durationQuantity.toIntOrNull() ?: 1
                        ).format(DateTimeFormatter.ISO_LOCAL_DATE),
                        targetText = target,
                        workDayDiet = workDayDiet,
                        workDayExercise = workDayExercise,
                        workDayMinutes = workDayMinutes.toIntOrNull() ?: 0,
                        workDaySteps = workDaySteps.toIntOrNull() ?: 0,
                        workDayCalories = workDayCalories.toIntOrNull() ?: 0,
                        restDayDiet = restDayDiet,
                        restDayExercise = restDayExercise,
                        restDayMinutes = restDayMinutes.toIntOrNull() ?: 0,
                        restDayCalories = restDayCalories.toIntOrNull() ?: 0,
                        note = ""
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    RunningLoading(size = 24.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("保存中...")
                } else {
                    Text("保存计划")
                }
            }
        }
    }
}

private fun calculateEndDate(
    startDate: LocalDate,
    durationType: String,
    durationQuantity: Int
): LocalDate {
    return when (durationType) {
        "MONTH" -> startDate.plusMonths(durationQuantity.toLong()).minusDays(1)
        "WEEK" -> startDate.plusWeeks(durationQuantity.toLong()).minusDays(1)
        else -> startDate
    }
}
