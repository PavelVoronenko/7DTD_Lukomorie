package com.antago30.a7dtd_lukomorie.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.logic.BloodMoonCalculator
import com.antago30.a7dtd_lukomorie.parser.WebParser
import com.antago30.a7dtd_lukomorie.utils.Constants
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue
import androidx.core.content.edit
import com.antago30.a7dtd_lukomorie.MainActivity

class BloodMoonCheckWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        const val CHANNEL_ID = "blood_moon_update_channel"
        const val NOTIFICATION_ID = 2001
        const val KEY_REMINDER_MINUTES = "reminder_minutes"
        const val KEY_ORIGINAL_BLOOD_MOON_MILLIS = "original_blood_moon_millis"
    }

    private val webParser = WebParser()
    private val calculator = BloodMoonCalculator()

    override fun doWork(): Result {
        return try {
            // 1. Получаем актуальное время Blood Moon
            val serverInfo = webParser.parseInfo(Constants.FULL_DATA_URL)
            val actualBloodMoon = calculator.calculateNextBloodMoon(serverInfo.day, serverInfo.time)

            // 2. Получаем ожидаемое время из inputData
            val expectedBloodMoonMillis = inputData.getLong(KEY_ORIGINAL_BLOOD_MOON_MILLIS, 0)
            val reminderMinutes = inputData.getInt(KEY_REMINDER_MINUTES, 15)

            if (expectedBloodMoonMillis == 0L) {
                showStandardNotification(reminderMinutes)
                return Result.success()
            }

            val expectedBloodMoon = Instant.ofEpochMilli(expectedBloodMoonMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            // 3. Сравниваем
            val diff = ChronoUnit.MINUTES.between(expectedBloodMoon, actualBloodMoon).absoluteValue

            if (diff <= 1) {
                // Всё по плану
                showStandardNotification(reminderMinutes)
            } else {
                // Время сместилось!
                val formattedNewTime = actualBloodMoon.format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))
                showShiftedNotification(formattedNewTime, ChronoUnit.MINUTES.between(LocalDateTime.now(), actualBloodMoon))
            }

            Result.success()
        } catch (e: Exception) {
            showNotification("Ошибка", "Не удалось проверить время Blood Moon")
            Result.failure()
        }
    }

    private fun showStandardNotification(minutes: Int) {
        showNotification(
            title = "Кровавая Луна",
            content = "Начнётся через $minutes минут!"
        )
    }

    private fun showShiftedNotification(newTime: String, minutesUntilNewMoon: Long) {
        showNotification(
            title = "Время луны перенеслось",
            content = "Игроков не было онлайн. Кровавая Луна перенесена на $newTime (через $minutesUntilNewMoon мин)"
        )
    }

    private fun showNotification(title: String, content: String) {
        createNotificationChannel()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.app_icon)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(content)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(500, 500, 500))
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Обновления Кровавой Луны",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Уведомления об изменении времени Кровавой Луны"
            enableVibration(true)
            enableLights(true)
        }
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun disableReminderGlobally() {
        val prefs = applicationContext.getSharedPreferences("blood_moon_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean("reminder_should_be_disabled", true)
                .putBoolean("reminder_active", false)
        }
    }
}