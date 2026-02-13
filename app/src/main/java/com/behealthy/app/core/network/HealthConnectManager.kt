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

/**
 * Manages interactions with Android Health Connect API.
 *
 * This class handles permission checks, requests, and data retrieval from Health Connect.
 * It serves as the primary gateway for accessing health data like steps, calories, and distance.
 */
@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    /**
     * Checks the availability of Health Connect SDK on the device.
     *
     * @return The status code indicating availability (e.g., [HealthConnectClient.SDK_AVAILABLE]).
     */
    fun getSdkStatus(): Int {
        return HealthConnectClient.getSdkStatus(context)
    }

    /**
     * Defines the set of permissions required by the application.
     *
     * @return A set of permission strings for reading Steps, Distance, Calories, etc.
     */
    fun getRequiredPermissions(): Set<String> {
        return setOf(
            androidx.health.connect.client.permission.HealthPermission.getReadPermission(StepsRecord::class),
            androidx.health.connect.client.permission.HealthPermission.getReadPermission(DistanceRecord::class),
            androidx.health.connect.client.permission.HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            androidx.health.connect.client.permission.HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            androidx.health.connect.client.permission.HealthPermission.getReadPermission(ExerciseSessionRecord::class)
        )
    }

    /**
     * Checks if all required permissions are granted.
     *
     * @return `true` if all permissions in [getRequiredPermissions] are granted, `false` otherwise.
     *         Also returns `false` if an error occurs during the check.
     */
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
    
    /**
     * Retrieves missing permissions that need to be requested from the user.
     *
     * @return A set of missing permission strings. Returns all required permissions if an error occurs.
     */
    suspend fun getMissingPermissions(): Set<String> {
        return try {
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            getRequiredPermissions() - grantedPermissions
        } catch (e: Exception) {
            getRequiredPermissions()
        }
    }
    
    /**
     * Creates an Intent to request permissions from the user.
     *
     * @return An [android.content.Intent] to launch the permission request flow, or `null` if creation fails.
     */
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

    /**
     * Aggregates daily activity data (steps, calories, distance) for a specific date.
     *
     * @param date The date for which to retrieve data.
     * @return A [DailyActivityData] object containing the aggregated data, or `null` if the request fails.
     * @throws Exception Exceptions are caught internally and result in a null return, but logged.
     */
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
            
            AppLogger.log("HealthConnect", "=== Daily Data for $date ===")
            AppLogger.log("HealthConnect", "Query Range: $startTime to $endTime (Zone: ${ZoneId.systemDefault()})")
            AppLogger.log("HealthConnect", "Aggregated Data:")
            AppLogger.log("HealthConnect", "- Steps: $steps")
            AppLogger.log("HealthConnect", "- Distance: $distance m")
            AppLogger.log("HealthConnect", "- Total Calories (BMR+Active): $totalCalories kcal")
            AppLogger.log("HealthConnect", "- Active Calories: $activeCalories kcal")

            // Debug: Check raw steps records if steps are 0
            if (steps == 0L) {
                 try {
                     val rawSteps = healthConnectClient.readRecords(
                        androidx.health.connect.client.request.ReadRecordsRequest<StepsRecord>(
                            timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                            pageSize = 5
                        )
                    )
                    AppLogger.log("HealthConnect", "Raw Steps Check: Found ${rawSteps.records.size} records.")
                    rawSteps.records.forEach { record ->
                        AppLogger.log("HealthConnect", " - Step Record: ${record.count} from ${record.startTime} to ${record.endTime}") 
                    }
                 } catch (e: Exception) {
                     AppLogger.log("HealthConnect", "Failed to read raw steps: ${e.message}")
                 }
            }

            // Prefer Active Calories for "Daily Activity" tracking as it reflects exercise/movement.
            // Total Calories includes BMR which can be confusing (e.g., 1500+ kcal even if sedentary).
            val calories = if (activeCalories > 0) activeCalories else 0.0
            
            AppLogger.log("HealthConnect", "Calories Check: Total=$totalCalories, Active=$activeCalories. Using Active=$calories")
            if (totalCalories > 0 && activeCalories > 0) {
                 AppLogger.log("HealthConnect", "Note: Total includes BMR (~${(totalCalories - activeCalories).toInt()} kcal).")
            }
            
            // Read ExerciseSession for accurate duration
            val sessionsResponse = healthConnectClient.readRecords(
                androidx.health.connect.client.request.ReadRecordsRequest<ExerciseSessionRecord>(
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val sessionDurationMinutes = sessionsResponse.records.sumOf { 
                java.time.Duration.between(it.startTime, it.endTime).toMinutes() 
            }.toInt()
            
            AppLogger.log("HealthConnect", "Session Data:")
            AppLogger.log("HealthConnect", "- Sessions Count: ${sessionsResponse.records.size}")
            AppLogger.log("HealthConnect", "- Session Duration: $sessionDurationMinutes min")
            sessionsResponse.records.forEach {
                 AppLogger.log("HealthConnect", "  * Session: ${it.title} (${java.time.Duration.between(it.startTime, it.endTime).toMinutes()} min)")
            }

            val durationMinutes = if (sessionDurationMinutes > 0) {
                sessionDurationMinutes
            } else {
                 // Fallback: If no sessions recorded but steps exist, use 0 to be accurate to "recorded sessions".
                 0
            }

            DailyActivityData(
                steps = steps.toInt(),
                calories = calories.toInt(),
                distanceMeters = distance.toInt(),
                durationMinutes = durationMinutes
            )
        } catch (e: SecurityException) {
            AppLogger.log("HealthConnect", "Security Exception (Likely background access denied): ${e.message}")
            null
        } catch (e: Exception) {
            android.util.Log.e("HealthConnect", "Error getting daily activity for $date", e)
            e.printStackTrace()
            null
        }
    }
}
