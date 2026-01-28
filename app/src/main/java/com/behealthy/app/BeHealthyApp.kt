package com.behealthy.app

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.behealthy.app.core.worker.SyncWorker
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class BeHealthyApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        setupPeriodicSync()
        triggerImmediateSync()
    }

    private fun setupPeriodicSync() {
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailySync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    private fun triggerImmediateSync() {
        val syncRequest = androidx.work.OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .build()
        
        WorkManager.getInstance(this).enqueue(syncRequest)
    }

    private fun setupWeeklyNotifications() {
        // Weekly Report: Sunday 10 PM (22:00)
        val reportDelay = calculateInitialDelay(java.time.DayOfWeek.SUNDAY.value, 22)
        val reportRequest = PeriodicWorkRequestBuilder<com.behealthy.app.core.worker.WeeklyReportWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(reportDelay, TimeUnit.MILLISECONDS)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "WeeklyReport",
            ExistingPeriodicWorkPolicy.KEEP,
            reportRequest
        )

        // Weekly Plan: Monday 10 AM (10:00)
        val planDelay = calculateInitialDelay(java.time.DayOfWeek.MONDAY.value, 10)
        val planRequest = PeriodicWorkRequestBuilder<com.behealthy.app.core.worker.WeeklyPlanWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(planDelay, TimeUnit.MILLISECONDS)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "WeeklyPlan",
            ExistingPeriodicWorkPolicy.KEEP,
            planRequest
        )
    }

    private fun calculateInitialDelay(targetDayOfWeek: Int, targetHour: Int): Long {
        val now = java.time.LocalDateTime.now()
        var nextRun = now.withHour(targetHour).withMinute(0).withSecond(0).withNano(0)
        
        while (nextRun.dayOfWeek.value != targetDayOfWeek || nextRun.isBefore(now)) {
            nextRun = nextRun.plusDays(1)
        }
        
        return java.time.Duration.between(now, nextRun).toMillis()
    }
}
