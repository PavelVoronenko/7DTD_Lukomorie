package com.antago30.a7dtd_lukomorie.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.logic.BloodMoonCalculator
import com.antago30.a7dtd_lukomorie.parser.WebParser
import com.antago30.a7dtd_lukomorie.utils.Constants
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class BloodMoonCheckWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        const val CHANNEL_ID = "blood_moon_update_channel"
        const val NOTIFICATION_ID = 2001
        const val KEY_ORIGINAL_TRIGGER_TIME = "original_trigger_time"
        const val KEY_REMINDER_MINUTES = "reminder_minutes"
    }

    private val webParser = WebParser()
    private val calculator = BloodMoonCalculator()

    override fun doWork(): Result {
        return try {
            // Парсим актуальные данные
            val serverInfo = webParser.parseInfo(Constants.INFO_URL)

            // Рассчитываем новое время следующей луны
            val newNextBloodMoon = calculator.calculateNextBloodMoon(
                currentGameDay = serverInfo.day,
                currentGameTime = serverInfo.time
            )

            // Получаем изначальное время срабатывания (когда пользователь включил напоминание)
            val originalTriggerTime = inputData.getLong(KEY_ORIGINAL_TRIGGER_TIME, 0)
            val reminderMinutes = inputData.getInt(KEY_REMINDER_MINUTES, 15)

            if (originalTriggerTime == 0L) {
                // Если нет оригинального времени — показываем стандартное уведомление
                showStandardNotification(reminderMinutes)
                return Result.success()
            }

            // Рассчитываем, когда должно было сработать напоминание (оригинальное время)
            val expectedReminderTime = originalTriggerTime + (reminderMinutes * 60 * 1000)

            // Проверяем, совпадает ли новое время луны с ожидаемым
            val newReminderTime = newNextBloodMoon.minusMinutes(reminderMinutes.toLong())
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val timeDifference = abs(expectedReminderTime - newReminderTime)

            if (timeDifference < 60 * 1000) { // Разница меньше 1 минуты
                showStandardNotification(reminderMinutes)
            } else {
                // Время сместилось — показываем обновлённое уведомление
                val minutesUntilNewMoon = ChronoUnit.MINUTES.between(LocalDateTime.now(), newNextBloodMoon)
                val formattedNewTime = newNextBloodMoon.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))

                showShiftedNotification(formattedNewTime, minutesUntilNewMoon)
            }

            Result.success()
        } catch (e: Exception) {
            showNotification(
                title = "Ошибка напоминания",
                content = "Не удалось обновить данные. Проверьте подключение."
            )
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
            content = "Игроков не было онлайн. Кровавая Луна перенесена на $newTime (через $minutesUntilNewMoon мин). Напоминание отключено"
        )
    }

    private fun showNotification(title: String, content: String) {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.app_icon)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(500, 500, 500))
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
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
}