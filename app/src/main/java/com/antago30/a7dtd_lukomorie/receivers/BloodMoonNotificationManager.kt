package com.antago30.a7dtd_lukomorie.receivers

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import java.time.LocalDateTime
import java.time.ZoneId
import androidx.core.net.toUri
import java.time.Instant

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
    private var isScheduling = false

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleReminder(nextBloodMoonTime: LocalDateTime, minutesBefore: Int, onScheduled: () -> Unit) {
        if (isScheduling) return
        isScheduling = true

        Log.d("ALARM_DEBUG", "nextBloodMoonTime: $nextBloodMoonTime")
        Log.d("ALARM_DEBUG", "minutesBefore: $minutesBefore")

        val triggerTime = nextBloodMoonTime.minusMinutes(minutesBefore.toLong())

        Log.d("ALARM_DEBUG", "triggerTime: $triggerTime")

        val triggerMillis = triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        Log.d("ALARM_DEBUG", "triggerMillis: $triggerMillis")
        Log.d("ALARM_DEBUG", "Formatted trigger time: ${Instant.ofEpochMilli(triggerMillis).atZone(ZoneId.systemDefault())}")

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
            alarmManager.setExactAndAllowWhileIdle(
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
        } catch (e: SecurityException) {
            // Обработка случая, когда пользователь отключил точные будильники
            Toast.makeText(
                context,
                "Разрешите точные будильники в настройках",
                Toast.LENGTH_LONG
            ).show()

            // Опционально: открыть настройки
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${context.packageName}".toUri()
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка при установке напоминания", Toast.LENGTH_LONG).show()
        } finally {
            isScheduling = false
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

        val pendingIntentForCancel = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntentForCancel)
        pendingIntentForCancel.cancel()

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