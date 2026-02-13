package com.behealthy.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.behealthy.app.MainActivity
import com.behealthy.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ID_BACKUP = "backup_channel"
        const val CHANNEL_ID_DEFAULT = "default_channel"
        const val NOTIFICATION_ID_BACKUP_FAILURE = 1001
        const val NOTIFICATION_ID_BACKUP_SUCCESS = 1002
        const val WEEKLY_PLAN_ID = 1003
        const val WEEKLY_REPORT_ID = 1004
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Backup Channel
            val backupName = "数据备份"
            val backupDesc = "数据库备份状态通知"
            val backupImportance = NotificationManager.IMPORTANCE_DEFAULT
            val backupChannel = NotificationChannel(CHANNEL_ID_BACKUP, backupName, backupImportance).apply {
                description = backupDesc
            }
            notificationManager.createNotificationChannel(backupChannel)

            // Default Channel (for weekly reports etc)
            val defaultName = "常规提醒"
            val defaultDesc = "周报、计划等常规提醒"
            val defaultImportance = NotificationManager.IMPORTANCE_DEFAULT
            val defaultChannel = NotificationChannel(CHANNEL_ID_DEFAULT, defaultName, defaultImportance).apply {
                description = defaultDesc
            }
            notificationManager.createNotificationChannel(defaultChannel)
        }
    }

    fun showNotification(id: Int, title: String, content: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_DEFAULT)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Use generic info icon
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }
    }

    fun showBackupSuccessNotification(fileName: String) {
        // Optional: User might find daily success notifications annoying, so maybe only show on failure or important milestones
        // But for "Monitoring", logs are better for success, notifications for failure.
        // Let's stick to failure only for now, unless requested otherwise.
        // Actually, let's just log success.
    }

    fun showBackupFailureNotification(error: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_BACKUP)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Use a system icon or app icon if available
            .setContentTitle("备份失败")
            .setContentText("自动备份遇到问题: $error")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_BACKUP_FAILURE, builder.build())
        }
    }
}
