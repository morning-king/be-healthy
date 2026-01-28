package com.behealthy.app.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.behealthy.app.core.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WeeklyReportWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        notificationHelper.showNotification(
            NotificationHelper.WEEKLY_REPORT_ID,
            "周报已生成 📊",
            "点击查看本周的运动与心情总结，看看您的进步吧！"
        )
        return Result.success()
    }
}
