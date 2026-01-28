package com.behealthy.app.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.behealthy.app.core.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WeeklyPlanWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        notificationHelper.showNotification(
            NotificationHelper.WEEKLY_PLAN_ID,
            "新的一周开始啦 💪",
            "新的健身计划已准备就绪，快来开启元气满满的一周！"
        )
        return Result.success()
    }
}
