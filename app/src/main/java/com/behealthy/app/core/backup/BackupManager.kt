package com.behealthy.app.core.backup

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "BackupManager"
        private const val DB_NAME = "behealthy_db"
        private const val BACKUP_DIR_NAME = "database_backups"
        private const val DATE_FORMAT = "yyyyMMdd_HHmmss"
        private const val MAX_BACKUPS = 30
    }

    private val backupDir: File by lazy {
        File(context.filesDir, BACKUP_DIR_NAME).apply {
            if (!exists()) mkdirs()
        }
    }

    suspend fun createBackup(isManual: Boolean = false): Result<File> = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("Database file not found"))
            }

            // Close DB connections? ideally yes, but for SQLite simple file copy usually works if WAL is checkpointed.
            // Since we can't easily close the singleton DB from here without a reference, we rely on WAL mode or just copy.
            // Copying WAL and SHM files is also required if they exist.

            val timestamp = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
            val prefix = if (isManual) "manual" else "auto"
            val backupFileName = "${DB_NAME}_${prefix}_${timestamp}.bak"
            val backupFile = File(backupDir, backupFileName)

            // Copy main DB file
            copyFile(dbFile, backupFile)

            // Copy WAL/SHM if they exist (important for Room/SQLite)
            val walFile = File(dbFile.parent, "$DB_NAME-wal")
            if (walFile.exists()) {
                copyFile(walFile, File(backupDir, "$backupFileName-wal"))
            }
            val shmFile = File(dbFile.parent, "$DB_NAME-shm")
            if (shmFile.exists()) {
                copyFile(shmFile, File(backupDir, "$backupFileName-shm"))
            }

            // Cleanup old backups
            cleanupOldBackups()

            Log.d(TAG, "Backup created successfully: ${backupFile.absolutePath}")
            Result.success(backupFile)
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(backupFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!backupFile.exists()) {
                return@withContext Result.failure(Exception("Backup file not found"))
            }

            val dbFile = context.getDatabasePath(DB_NAME)
            val dbDir = dbFile.parentFile ?: return@withContext Result.failure(Exception("DB dir not found"))

            // 1. Validate backup integrity (basic size check)
            if (backupFile.length() == 0L) {
                return@withContext Result.failure(Exception("Backup file is empty"))
            }

            // 2. Close app/DB connections is tricky here. 
            // In a real app, we might need to kill the process after restore or ensure no DAO is active.
            // For now, we overwrite.

            // 3. Delete current DB files
            val walFile = File(dbDir, "$DB_NAME-wal")
            val shmFile = File(dbDir, "$DB_NAME-shm")
            
            if (dbFile.exists()) dbFile.delete()
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            // 4. Restore main DB file
            copyFile(backupFile, dbFile)

            // 5. Restore WAL/SHM if they exist in backup
            val backupWal = File(backupFile.parent, "${backupFile.name}-wal")
            if (backupWal.exists()) {
                copyFile(backupWal, walFile)
            }
            
            val backupShm = File(backupFile.parent, "${backupFile.name}-shm")
            if (backupShm.exists()) {
                copyFile(backupShm, shmFile)
            }

            Log.d(TAG, "Restore successful from: ${backupFile.absolutePath}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            Result.failure(e)
        }
    }

    suspend fun getBackups(): List<File> = withContext(Dispatchers.IO) {
        backupDir.listFiles { file -> 
            file.name.startsWith(DB_NAME) && file.name.endsWith(".bak") 
        }?.sortedByDescending { it.lastModified() }?.toList() ?: emptyList()
    }

    private fun cleanupOldBackups() {
        val backups = backupDir.listFiles { file -> 
            file.name.startsWith(DB_NAME) && file.name.endsWith(".bak") 
        }?.sortedByDescending { it.lastModified() } ?: return

        if (backups.size > MAX_BACKUPS) {
            backups.drop(MAX_BACKUPS).forEach { file ->
                file.delete()
                // Delete associated WAL/SHM
                File(file.parent, "${file.name}-wal").delete()
                File(file.parent, "${file.name}-shm").delete()
            }
        }
    }

    private fun copyFile(source: File, dest: File) {
        FileInputStream(source).use { input ->
            FileOutputStream(dest).use { output ->
                input.copyTo(output)
            }
        }
    }
}
