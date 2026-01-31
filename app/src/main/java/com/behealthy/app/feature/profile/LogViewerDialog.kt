package com.behealthy.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.behealthy.app.core.logger.AppLogger

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogViewerDialog(
    onDismiss: () -> Unit,
    onTriggerSync: () -> Unit
) {
    val logs by AppLogger.logs.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    
    // Group logs by date (descending)
    val groupedLogs = remember(logs) {
        logs.groupBy { it.timestamp.toLocalDate() }
            .toSortedMap(compareByDescending { it })
    }
    val dates = remember(groupedLogs) { groupedLogs.keys.toList() }
    
    val pagerState = rememberPagerState(pageCount = { if (dates.isEmpty()) 1 else dates.size })
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "应用日志 / 诊断信息",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (dates.isNotEmpty()) {
                            val currentDate = dates.getOrNull(pagerState.currentPage)
                            currentDate?.let {
                                Text(
                                    text = it.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Row {
                        IconButton(onClick = {
                            if (dates.isNotEmpty()) {
                                val currentDate = dates[pagerState.currentPage]
                                val currentLogs = groupedLogs[currentDate] ?: emptyList()
                                val text = currentLogs.joinToString("\n") { it.toString() }
                                clipboardManager.setText(AnnotatedString(text))
                            } else {
                                clipboardManager.setText(AnnotatedString("No logs"))
                            }
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Current Page Logs")
                        }
                        IconButton(onClick = { AppLogger.clear() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Logs")
                        }
                        IconButton(onClick = onTriggerSync) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Data")
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Divider()

                // Pager content
                if (logs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "暂无日志",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.05f))
                            .padding(8.dp)
                    ) { page ->
                        if (dates.isNotEmpty() && page < dates.size) {
                            val date = dates[page]
                            val pageLogs = groupedLogs[date] ?: emptyList()
                            
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(pageLogs) { log ->
                                    Text(
                                        text = log.toString(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                    Divider(color = Color.LightGray.copy(alpha = 0.2f), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                    
                    // Pager Indicator / Navigation
                    if (dates.size > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { 
                                    if (pagerState.currentPage > 0) {
                                        scope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1) 
                                        }
                                    }
                                },
                                enabled = pagerState.currentPage > 0
                            ) {
                                Icon(Icons.Default.ChevronLeft, "Prev")
                            }
                            
                            Text(
                                text = "${pagerState.currentPage + 1} / ${dates.size}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            IconButton(
                                onClick = { 
                                    if (pagerState.currentPage < dates.size - 1) {
                                         scope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1) 
                                         }
                                    }
                                },
                                enabled = pagerState.currentPage < dates.size - 1
                            ) {
                                Icon(Icons.Default.ChevronRight, "Next")
                            }
                        }
                    }
                }
            }
        }
    }
}
