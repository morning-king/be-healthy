package com.behealthy.app.core.logger

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class LogEntry(
    val timestamp: LocalDateTime,
    val tag: String,
    val message: String
) {
    override fun toString(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
        return "[${timestamp.format(formatter)}] $tag: $message"
    }
}

object AppLogger {
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

    fun log(tag: String, message: String) {
        val now = LocalDateTime.now()
        val logEntry = LogEntry(now, tag, message)
        
        // Log to Android Logcat
        Log.d(tag, message)
        
        // Add to in-memory logs (keep last 1000 lines)
        val currentLogs = _logs.value.toMutableList()
        currentLogs.add(0, logEntry) // Add to top
        if (currentLogs.size > 1000) {
            currentLogs.removeAt(currentLogs.lastIndex)
        }
        _logs.value = currentLogs
    }

    fun clear() {
        _logs.value = emptyList()
    }
}
