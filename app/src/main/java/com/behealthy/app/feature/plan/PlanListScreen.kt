package com.behealthy.app.feature.plan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanListScreen(
    viewModel: PlanViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var startDateQuery by remember { mutableStateOf("") }
    var endDateQuery by remember { mutableStateOf("") }
    
    // Filter logic
    val filteredPlans = remember(uiState.plans, searchQuery, startDateQuery, endDateQuery) {
        uiState.plans.filter { plan ->
            val matchesName = plan.name.contains(searchQuery, ignoreCase = true)
            val matchesStart = if (startDateQuery.isNotEmpty()) plan.startDate >= startDateQuery else true
            val matchesEnd = if (endDateQuery.isNotEmpty()) plan.endDate <= endDateQuery else true
            matchesName && matchesStart && matchesEnd
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("健身计划列表") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val today = LocalDate.now().toString()
                    startDateQuery = today
                    endDateQuery = today
                    searchQuery = ""
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("回到今天", modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Search Fields
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("搜索计划名称") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startDateQuery,
                    onValueChange = { startDateQuery = it },
                    label = { Text("开始日期 (YYYY-MM-DD)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = endDateQuery,
                    onValueChange = { endDateQuery = it },
                    label = { Text("截止日期 (YYYY-MM-DD)") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredPlans) { plan ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = plan.name, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "目标: ${plan.targetText}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "时间: ${plan.startDate} ~ ${plan.endDate}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "状态: ${if (plan.isActive) "进行中" else "已结束"}", color = if (plan.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}
