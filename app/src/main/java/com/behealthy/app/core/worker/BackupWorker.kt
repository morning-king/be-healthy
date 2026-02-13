package com.behealthy.app.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.behealthy.app.core.backup.BackupManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import android.util.Log

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupManager: BackupManager,
    private val notificationHelper: com.behealthy.app.core.notification.NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("BackupWorker", "Starting daily database backup...")
        return try {
            val result = backupManager.createBackup(isManual = false)
            if (result.isSuccess) {
                Log.d("BackupWorker", "Daily backup completed successfully")
                Result.success()
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                Log.e("BackupWorker", "Daily backup failed", result.exceptionOrNull())
                // Only notify on final failure to avoid spamming user on retries
                if (runAttemptCount >= 3) {
                     notificationHelper.showBackupFailureNotification(error)
                     Result.failure()
                } else {
                     Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e("BackupWorker", "Daily backup error", e)
            notificationHelper.showBackupFailureNotification(e.message ?: "Unknown exception")
            Result.failure()
        }
    }
}
