package com.behealthy.app.core.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.behealthy.app.core.database.entity.DailyActivityEntity
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
    private val dailyActivityRepository: DailyActivityRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_FULL_SYNC = "full_sync"
    }

    override suspend fun doWork(): Result {
        return try {
            val isFullSync = inputData.getBoolean(KEY_FULL_SYNC, false)
            AppLogger.log("SyncWorker", "Starting data sync with OPPO Health... (Full Sync: $isFullSync)")
            
            // Sync last 1 year if requested, otherwise last 7 days
            val today = LocalDate.now()
            val startDate = if (isFullSync) today.minusYears(1) else today.minusDays(7)
            
            var currentDate = startDate
            var processedCount = 0
            while (!currentDate.isAfter(today)) {
                val dateStr = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                
                // 1. Fetch data from OPPO Health (Simulated)
                val data = oppoHealthService.getDailyActivity(dateStr)
                
                AppLogger.log("SyncWorker", "Synced $dateStr: Steps=${data.steps}, Cal=${data.calories}")
                
                // 2. Save to local database
                val entity = DailyActivityEntity(
                    date = dateStr,
                    steps = data.steps,
                    calories = data.calories,
                    distanceMeters = data.distanceMeters,
                    durationMinutes = data.durationMinutes,
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
