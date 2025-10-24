package com.neverforget.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.neverforget.MainActivity
import com.neverforget.R
import com.neverforget.receivers.TaskCompleteReceiver
import com.neverforget.receivers.TaskSnoozeReceiver
import com.neverforget.utils.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Worker pour gérer le report (snooze) des notifications
 */
@HiltWorker
class TaskSnoozeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "task_snooze"
        const val CHANNEL_NAME = "Options de report"
        
        // Options de report en minutes
        const val SNOOZE_1_HOUR = 60L
        const val SNOOZE_3_HOURS = 180L
        const val SNOOZE_1_DAY = 1440L
        const val SNOOZE_3_DAYS = 4320L
        
        private const val SNOOZE_NOTIFICATION_ID_BASE = 2000
    }

    override suspend fun doWork(): Result {
        return try {
            val taskId = inputData.getString("task_id") ?: return Result.failure()
            val taskName = inputData.getString("task_name") ?: "Tâche inconnue"
            
            
            
            // Créer le canal de notification pour les options de snooze
            createSnoozeNotificationChannel()
            
            // Afficher la notification avec les options de report
            showSnoozeOptionsNotification(taskId, taskName)
            
            Result.success()
            
        } catch (e: Exception) {
            
            Result.failure()
        }
    }

    private fun createSnoozeNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Options de report pour les tâches"
                enableVibration(false)
                setShowBadge(false)
            }
            
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showSnoozeOptionsNotification(taskId: String, taskName: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Intent pour ouvrir l'application
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("task_id", taskId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Intent pour marquer comme fait
        val completeIntent = Intent(applicationContext, TaskCompleteReceiver::class.java).apply {
            putExtra("task_id", taskId)
        }
        
        val completePendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            taskId.hashCode() + 1,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Ajouter des actions supplémentaires via le style étendu
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText("Tâche: $taskName\n\nOptions de report:\n• 1 heure\n• 3 heures\n• 1 jour\n• 3 jours")
        
        val extendedNotification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Reporter: $taskName")
            .setContentText("Choisissez quand être rappelé")
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_check, "Fait", completePendingIntent)
            .addAction(R.drawable.ic_snooze, "1h", createSnoozeAction(taskId, taskName, SNOOZE_1_HOUR))
            .addAction(R.drawable.ic_snooze, "1j", createSnoozeAction(taskId, taskName, SNOOZE_1_DAY))
            .build()
        
        val notificationId = SNOOZE_NOTIFICATION_ID_BASE + taskId.hashCode()
        notificationManager.notify(notificationId, extendedNotification)
        
        
    }

    private fun createSnoozeAction(taskId: String, taskName: String, delayMinutes: Long): PendingIntent {
        // Programmer une nouvelle notification après le délai
        val workRequest = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
            .setInputData(workDataOf(
                TaskNotificationWorker.TASK_ID_KEY to taskId,
                TaskNotificationWorker.TASK_NAME_KEY to taskName,
                TaskNotificationWorker.NOTIFICATION_TYPE_KEY to TaskNotificationWorker.NOTIFICATION_TYPE_REMINDER
            ))
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .addTag("task_snooze")
            .addTag("task_$taskId")
            .build()
        
        WorkManager.getInstance(applicationContext).enqueue(workRequest)
        
        // Créer un intent factice pour l'action (la vraie logique est dans le WorkManager)
        val intent = Intent().apply {
            putExtra("snooze_scheduled", true)
        }
        
        return PendingIntent.getBroadcast(
            applicationContext,
            (taskId + delayMinutes.toString()).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}