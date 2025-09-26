package com.antago30.a7dtd_lukomorie.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import java.time.LocalDateTime
import java.time.ZoneId

class BloodMoonNotificationManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "blood_moon_reminder"
        private const val KEY_IS_ACTIVE = "is_active"
        private const val KEY_TRIGGER_TIME = "trigger_time"
        private const val KEY_REMINDER_MINUTES = "reminder_minutes"
        private const val KEY_SHOULD_DISABLE = "should_disable"
        private const val REQUEST_CODE = 1001
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)!!

    fun scheduleReminder(nextBloodMoonTime: LocalDateTime, minutesBefore: Int, onScheduled: () -> Unit) {
        val triggerTime = nextBloodMoonTime.minusMinutes(minutesBefore.toLong())
        val triggerMillis = triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (triggerMillis < System.currentTimeMillis()) {
            Toast.makeText(context, "До луны меньше времени, чем установлено", Toast.LENGTH_SHORT).show()
            return
        }

        val bloodMoonMillis = nextBloodMoonTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()



        val intent = Intent(context, BloodMoonNotificationReceiver::class.java).apply {
            putExtra("original_blood_moon_millis", bloodMoonMillis)
            putExtra("reminder_minutes", minutesBefore)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )

            prefs.edit {
                putBoolean(KEY_IS_ACTIVE, true)
                    .putLong(KEY_TRIGGER_TIME, triggerMillis)
                    .putInt(KEY_REMINDER_MINUTES, minutesBefore)
            }

            onScheduled()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка при установке напоминания", Toast.LENGTH_LONG).show()
        }
    }

    fun cancelReminder() {
        val intent = Intent(context, BloodMoonNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }

        prefs.edit {
            putBoolean(KEY_IS_ACTIVE, false)
        }
    }

    fun isReminderActive(): Boolean = prefs.getBoolean(KEY_IS_ACTIVE, false)

    fun requestDisableReminder() {
        prefs.edit {
            putBoolean(KEY_SHOULD_DISABLE, true)
            putBoolean(KEY_IS_ACTIVE, false)
        }
    }

    fun shouldAutoDisable(): Boolean {
        // 1. Время напоминания уже прошло
        // 2. Worker запросил отключение (например, из-за смещения времени)
        val isActive = isReminderActive()
        val triggerTime = prefs.getLong(KEY_TRIGGER_TIME, 0)
        val shouldDisable = prefs.getBoolean(KEY_SHOULD_DISABLE, false)

        return shouldDisable || (isActive && triggerTime < System.currentTimeMillis())
    }

    fun clearAutoDisableFlag() {
        prefs.edit {
            putBoolean(KEY_SHOULD_DISABLE, false)
        }
    }

    fun getSavedReminderMinutes(): Int = prefs.getInt(KEY_REMINDER_MINUTES, 15)
}