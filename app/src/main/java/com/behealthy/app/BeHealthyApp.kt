package com.behealthy.app

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.behealthy.app.core.worker.BackupWorker
import com.behealthy.app.core.worker.SyncWorker
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

@HiltAndroidApp
class BeHealthyApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory
    
    @Inject
    lateinit var backupManager: com.behealthy.app.core.backup.BackupManager
    
    @Inject
    lateinit var notificationHelper: com.behealthy.app.core.notification.NotificationHelper

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannels()
        checkPreMigrationBackup()
        setupPeriodicSync()
        triggerImmediateSync()
        setupBackupWorker()
    }
    
    private fun checkPreMigrationBackup() {
        // Simple check: In a real scenario, we might track DB version in Preferences vs Code Version
        // For now, we rely on the daily backup, but we could trigger one on every app start just to be safe (though expensive)
        // Or check if today's backup exists.
        // Let's rely on the scheduled worker for now, as "DDL Forced Backup" is hard to hook into Room builder from here without
        // refactoring the DatabaseModule to callback here. 
        // A simple "Backup on Start" is a good safety net for a local app.
        
        // Actually, let's implement the DDL requirement by checking a SharedPref version flag.
        val prefs = getSharedPreferences("db_version_pref", android.content.Context.MODE_PRIVATE)
        val lastVersion = prefs.getInt("last_db_version", 0)
        // Hardcoded current DB version from DatabaseModule/Entity (We know it is 10)
        val currentVersion = 10 
        
        if (currentVersion > lastVersion) {
            // Version changed (Migration likely), force backup!
            // Note: DB isn't open yet, so file copy is safe.
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    backupManager.createBackup(isManual = false)
                    prefs.edit().putInt("last_db_version", currentVersion).apply()
                    android.util.Log.d("BeHealthyApp", "Pre-migration backup created successfully.")
                } catch (e: Exception) {
                    android.util.Log.e("BeHealthyApp", "Failed to create pre-migration backup", e)
                }
            }
        }
    }

    private fun setupBackupWorker() {
        val constraints = androidx.work.Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(true) // Run when user is not using phone (usually night)
            .build()
        
        // Schedule for 2:00 AM
        val initialDelay = calculateDailyDelay(2, 0)

        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyDatabaseBackup",
            ExistingPeriodicWorkPolicy.KEEP,
            backupRequest
        )
    }

    private fun calculateDailyDelay(targetHour: Int, targetMinute: Int): Long {
        val now = java.time.LocalDateTime.now()
        var nextRun = now.withHour(targetHour).withMinute(targetMinute).withSecond(0).withNano(0)
        
        if (nextRun.isBefore(now)) {
            nextRun = nextRun.plusDays(1)
        }
        
        return java.time.Duration.between(now, nextRun).toMillis()
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
