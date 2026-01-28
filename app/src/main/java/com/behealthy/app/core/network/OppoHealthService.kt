package com.behealthy.app.core.network

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import com.behealthy.app.core.logger.AppLogger

data class DailyActivityData(
    val steps: Int,
    val calories: Int,
    val distanceMeters: Int,
    val durationMinutes: Int
)

@Singleton
class OppoHealthService @Inject constructor(
    private val healthConnectManager: HealthConnectManager
) {
    
    // Simulate fetching data from OPPO Health
    suspend fun getDailyActivity(date: String): DailyActivityData {
        AppLogger.log("OppoHealthService", "Requesting data for $date")
        val localDate = try {
            LocalDate.parse(date)
        } catch (e: Exception) {
            AppLogger.log("OppoHealthService", "Invalid date format: $date")
            null
        }

        if (localDate == null || localDate.isAfter(LocalDate.now())) {
            AppLogger.log("OppoHealthService", "Date is null or in future, returning empty")
            return DailyActivityData(0, 0, 0, 0)
        }

        if (!healthConnectManager.hasPermissions()) {
            val msg = "No permissions for Health Connect"
            android.util.Log.e("OppoHealthService", msg)
            AppLogger.log("OppoHealthService", msg)
            return DailyActivityData(0, 0, 0, 0)
        }

        val realData = healthConnectManager.getDailyActivity(localDate)
        if (realData == null) {
             val msg = "Failed to get data for $date"
             android.util.Log.e("OppoHealthService", msg)
             AppLogger.log("OppoHealthService", msg)
        } else {
             val msg = "Got data for $date: $realData"
             android.util.Log.d("OppoHealthService", msg)
             // HealthConnectManager already logs details, but we confirm here
             AppLogger.log("OppoHealthService", "Successfully retrieved data")
        }
        return realData ?: DailyActivityData(0, 0, 0, 0)
    }
}
