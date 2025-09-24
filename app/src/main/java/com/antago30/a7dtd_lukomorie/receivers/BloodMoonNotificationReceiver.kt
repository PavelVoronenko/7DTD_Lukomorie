package com.antago30.a7dtd_lukomorie.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.antago30.a7dtd_lukomorie.workers.BloodMoonCheckWorker
import java.util.concurrent.TimeUnit

class BloodMoonNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BloodMoonReceiver", "Получено напоминание — запускаем проверку данных")

        // Отменяем напоминание в менеджере
        val reminderManager = BloodMoonNotificationManager(context)
        reminderManager.cancelReminder()

        // Получаем данные
        val originalTriggerTime = intent.getLongExtra("original_trigger_time", 0)
        val minutes = intent.getIntExtra("reminder_minutes", 15)

        // Настраиваем ограничения для WorkManager
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Создаём и запускаем Worker
        val workRequest = OneTimeWorkRequestBuilder<BloodMoonCheckWorker>()
            .setInputData(
                workDataOf(
                    BloodMoonCheckWorker.KEY_ORIGINAL_TRIGGER_TIME to originalTriggerTime,
                    BloodMoonCheckWorker.KEY_REMINDER_MINUTES to minutes
                )
            )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}