package com.behealthy.app.core.logger

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class LogEntry(
    val timestamp: String,
    val tag: String,
    val message: String
)

object AppLogger {
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs = _logs.asStateFlow()
    
    private val dateFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")
    
    fun log(tag: String, message: String) {
        val entry = LogEntry(
            timestamp = LocalDateTime.now().format(dateFormatter),
            tag = tag,
            message = message
        )
        // Keep last 1000 logs
        val currentLogs = _logs.value.toMutableList()
        currentLogs.add(0, entry) // Add to top
        if (currentLogs.size > 1000) {
            _logs.value = currentLogs.take(1000)
        } else {
            _logs.value = currentLogs
        }
        
        // Also log to system log
        android.util.Log.d(tag, message)
    }
    
    fun clear() {
        _logs.value = emptyList()
    }
}
