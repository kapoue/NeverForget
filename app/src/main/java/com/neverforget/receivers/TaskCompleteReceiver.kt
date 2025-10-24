package com.neverforget.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.neverforget.utils.Logger
import com.neverforget.workers.TaskCompleteWorker

/**
 * BroadcastReceiver pour marquer une tâche comme terminée depuis une notification
 */
class TaskCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("task_id") ?: return
        
        
        
        // Annuler la notification immédiatement
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 1000 + taskId.hashCode()
        notificationManager.cancel(notificationId)
        
        
        
        // Lancer un Worker pour traiter la validation en arrière-plan
        val workRequest = OneTimeWorkRequestBuilder<TaskCompleteWorker>()
            .setInputData(workDataOf("task_id" to taskId))
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
        
        
    }
}