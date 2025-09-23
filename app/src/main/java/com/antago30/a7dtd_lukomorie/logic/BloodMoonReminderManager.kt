package com.antago30.a7dtd_lukomorie.logic

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.antago30.a7dtd_lukomorie.receivers.BloodMoonReminderReceiver
import java.time.LocalDateTime
import java.time.ZoneId
import androidx.core.content.edit

class BloodMoonReminderManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "blood_moon_reminder"
        private const val KEY_IS_ACTIVE = "is_active"
        private const val KEY_TRIGGER_TIME = "trigger_time"
        private const val KEY_REMINDER_MINUTES = "reminder_minutes"
        private const val REQUEST_CODE = 1001
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)!!

    fun scheduleReminder(nextBloodMoonTime: LocalDateTime, minutesBefore: Int, onScheduled: () -> Unit) {
        val triggerTime = nextBloodMoonTime.minusMinutes(minutesBefore.toLong())
        val triggerMillis = triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (triggerMillis < System.currentTimeMillis()) {
            Toast.makeText(context, "Осталось меньше установленного времени", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(context, BloodMoonReminderReceiver::class.java).apply {
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
        val intent = Intent(context, BloodMoonReminderReceiver::class.java)
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

    fun shouldAutoDisable(): Boolean {
        val isActive = isReminderActive()
        val triggerTime = prefs.getLong(KEY_TRIGGER_TIME, 0)
        return isActive && triggerTime < System.currentTimeMillis()
    }

    fun getSavedReminderMinutes(): Int = prefs.getInt(KEY_REMINDER_MINUTES, 15)
}