package com.antago30.a7dtd_lukomorie.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.antago30.a7dtd_lukomorie.workers.BloodMoonCheckWorker
import java.util.concurrent.TimeUnit

class BloodMoonNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Отменяем напоминание в менеджере
        val reminderManager = BloodMoonNotificationManager(context)
        reminderManager.requestDisableReminder()
        reminderManager.cancelReminder()

        // Получаем данные
        val originalBloodMoonMillis = intent.getLongExtra("original_blood_moon_millis", 0)
        val minutes = intent.getIntExtra("reminder_minutes", 15)

        // Настраиваем ограничения для WorkManager
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Создаём и запускаем Worker
        val workRequest = OneTimeWorkRequestBuilder<BloodMoonCheckWorker>()
            .setInputData(
                workDataOf(
                    BloodMoonCheckWorker.KEY_ORIGINAL_BLOOD_MOON_MILLIS to originalBloodMoonMillis,
                    BloodMoonCheckWorker.KEY_REMINDER_MINUTES to minutes
                )
            )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}