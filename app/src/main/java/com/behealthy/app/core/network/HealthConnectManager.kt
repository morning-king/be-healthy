package com.behealthy.app.core.network

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

import com.behealthy.app.core.logger.AppLogger

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    fun getSdkStatus(): Int {
        return HealthConnectClient.getSdkStatus(context)
    }

    fun getRequiredPermissions(): Set<String> {
        return setOf(
            androidx.health.connect.client.permission.HealthPermission.getReadPermission(StepsRecord::class),
            androidx.health.connect.client.permission.HealthPermission.getReadPermission(DistanceRecord::class),
            androidx.health.connect.client.permission.HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            androidx.health.connect.client.permission.HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            androidx.health.connect.client.permission.HealthPermission.getReadPermission(ExerciseSessionRecord::class)
        )
    }

    suspend fun hasPermissions(): Boolean {
        return try {
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            val required = getRequiredPermissions()
            val missing = required - grantedPermissions
            if (missing.isNotEmpty()) {
                val msg = "Missing permissions: $missing"
                android.util.Log.e("HealthConnect", msg)
                AppLogger.log("HealthConnect", msg)
            } else {
                AppLogger.log("HealthConnect", "All required permissions granted")
            }
            missing.isEmpty()
        } catch (e: Exception) {
            val msg = "Error checking permissions: ${e.message}"
            android.util.Log.e("HealthConnect", msg, e)
            AppLogger.log("HealthConnect", msg)
            false
        }
    }
    
    suspend fun getMissingPermissions(): Set<String> {
        return try {
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            getRequiredPermissions() - grantedPermissions
        } catch (e: Exception) {
            getRequiredPermissions()
        }
    }
    
    fun getPermissionRequestIntent(): android.content.Intent? {
        return try {
            PermissionController.createRequestPermissionResultContract().createIntent(
                context,
                getRequiredPermissions()
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getDailyActivity(date: LocalDate): DailyActivityData? = withContext(Dispatchers.IO) {
        try {
            val startTime = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endTime = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(
                        StepsRecord.COUNT_TOTAL,
                        DistanceRecord.DISTANCE_TOTAL,
                        TotalCaloriesBurnedRecord.ENERGY_TOTAL,
                        ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL
                    ),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            val steps = response[StepsRecord.COUNT_TOTAL] ?: 0L
            val distance = response[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0
            val totalCalories = response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories ?: 0.0
            val activeCalories = response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0
            val calories = if (totalCalories > 0) totalCalories else activeCalories
            
            // Duration is harder to get exactly from aggregates without raw reads, 
            // estimating based on steps for now if not available directly.
            // A more complex impl would read ExerciseSessionRecord.
            val durationMinutes = (steps / 110).toInt() 

            DailyActivityData(
                steps = steps.toInt(),
                calories = calories.toInt(),
                distanceMeters = distance.toInt(),
                durationMinutes = durationMinutes
            )
        } catch (e: Exception) {
            android.util.Log.e("HealthConnect", "Error getting daily activity for $date", e)
            e.printStackTrace()
            null
        }
    }
}
