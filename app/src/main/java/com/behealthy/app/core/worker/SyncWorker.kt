package com.behealthy.app.core.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.behealthy.app.core.database.entity.DailyActivityEntity
import com.behealthy.app.core.network.HealthConnectManager
import com.behealthy.app.core.network.OppoHealthService
import com.behealthy.app.core.repository.DailyActivityRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.behealthy.app.core.logger.AppLogger

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val oppoHealthService: OppoHealthService,
    private val healthConnectManager: HealthConnectManager,
    private val dailyActivityRepository: DailyActivityRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_FULL_SYNC = "full_sync"
    }

    override suspend fun doWork(): Result {
        return try {
            val isFullSync = inputData.getBoolean(KEY_FULL_SYNC, false)
            AppLogger.log("SyncWorker", "Starting data sync... (Full Sync: $isFullSync)")
            
            // Sync last 1 year if requested, otherwise last 7 days
            val today = LocalDate.now()
            val startDate = if (isFullSync) today.minusYears(1) else today.minusDays(7)
            
            var currentDate = startDate
            var processedCount = 0
            
            // Check Health Connect permissions first
            val hasHcPermissions = healthConnectManager.hasPermissions()
            AppLogger.log("SyncWorker", "Health Connect Permissions: $hasHcPermissions")
            
            while (!currentDate.isAfter(today)) {
                val dateStr = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                
                // 1. Fetch data from Health Connect (Priority) or OPPO Health (Fallback)
                var steps = 0
                var calories = 0
                var distance = 0
                var duration = 0
                
                if (hasHcPermissions) {
                    val hcData = healthConnectManager.getDailyActivity(currentDate)
                    if (hcData != null) {
                        steps = hcData.steps
                        calories = hcData.calories
                        distance = hcData.distanceMeters
                        duration = hcData.durationMinutes
                        AppLogger.log("SyncWorker", "Fetched from HealthConnect for $dateStr: Steps=$steps, Cal=$calories")
                    } else {
                        AppLogger.log("SyncWorker", "HealthConnect returned null for $dateStr")
                    }
                }
                
                // Fallback to OPPO Service if HC data is empty (and we don't trust it being 0 if permission missing)
                // However, if HC permission is granted and returns 0, it means 0.
                if (!hasHcPermissions && steps == 0) {
                     try {
                        val data = oppoHealthService.getDailyActivity(dateStr)
                        steps = data.steps
                        calories = data.calories
                        distance = data.distanceMeters
                        duration = data.durationMinutes
                        AppLogger.log("SyncWorker", "Fetched from OppoService for $dateStr: Steps=$steps")
                     } catch (e: Exception) {
                        AppLogger.log("SyncWorker", "OppoService failed for $dateStr: ${e.message}")
                     }
                }
                
                // 2. Save to local database
                val entity = DailyActivityEntity(
                    date = dateStr,
                    steps = steps,
                    calories = calories,
                    distanceMeters = distance,
                    durationMinutes = duration,
                    updatedAt = System.currentTimeMillis()
                )
                
                dailyActivityRepository.saveDailyActivity(entity)
                currentDate = currentDate.plusDays(1)
                processedCount++
            }
            
            AppLogger.log("SyncWorker", "Data sync completed successfully. Processed $processedCount days.")
            Result.success()
        } catch (e: Exception) {
            AppLogger.log("SyncWorker", "Error syncing data: ${e.message}")
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
