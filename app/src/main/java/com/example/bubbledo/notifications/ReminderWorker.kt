package com.example.bubbledo.notifications

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bubbledo.R
import java.util.Date

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getString("TASK_ID") ?: return Result.failure()
        val taskTitle = inputData.getString("TASK_TITLE") ?: "Задача"
        val notifyTime = inputData.getLong("NOTIFY_TIME", -1L)

        if (notifyTime != -1L) {
            val notifyDate = Date(notifyTime)
            Log.d("ReminderWorker", "Reminder triggered for task '$taskTitle' at: $notifyDate")
            Log.d("ReminderWorker", "Current time: ${Date()}")
        }

        showNotification(taskId, taskTitle)

        return Result.success()
    }

    private fun showNotification(taskId: String, taskTitle: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
            .setContentTitle("Напоминание о задаче")
            .setContentText("Срок задачи \"$taskTitle\" скоро истекает!")
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(taskId.hashCode(), notification)
    }
}
