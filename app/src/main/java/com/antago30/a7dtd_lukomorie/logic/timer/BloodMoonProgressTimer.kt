package com.antago30.a7dtd_lukomorie.logic.timer

import android.os.Handler
import android.os.Looper
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale

class BloodMoonProgressTimer(
    private val previousBloodMoonStart: LocalDateTime,
    val nextBloodMoonStart: LocalDateTime,
    private val bloodMoonEnd: LocalDateTime,
    private val onTick: (progressPercent: Int, timeLeftFormatted: String, isBloodMoonActive: Boolean) -> Unit,
    private val onFinish: () -> Unit = {}
) {

    private val totalCycleDurationMillis = ChronoUnit.MILLIS.between(previousBloodMoonStart, nextBloodMoonStart).coerceAtLeast(1)

    private var isRunning = false
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L // 1 секунда

    private val timerRunnable = object : Runnable {
        override fun run() {
            val now = LocalDateTime.now()
            val isBloodMoonActive = now.isAfter(nextBloodMoonStart) && now.isBefore(bloodMoonEnd)

            val remainingMillis = if (isBloodMoonActive) {
                ChronoUnit.MILLIS.between(now, bloodMoonEnd).coerceAtLeast(0)
            } else {
                ChronoUnit.MILLIS.between(now, nextBloodMoonStart).coerceAtLeast(0)
            }

            val progress = if (isBloodMoonActive) {
                onFinish()
                stop()
                100
            } else {
                val elapsedMillis = ChronoUnit.MILLIS.between(previousBloodMoonStart, now).coerceAtLeast(0)
                ((elapsedMillis.toDouble() / totalCycleDurationMillis) * 100).toInt().coerceIn(0, 100)
            }

            /*val timeFormatted = if (isBloodMoonActive) {
                "Сейчас!"
            } else {
                val hours = remainingMillis / (1000 * 60 * 60)
                val minutes = (remainingMillis / (1000 * 60)) % 60
                val seconds = (remainingMillis / 1000) % 60
                String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds)
            }*/
            val hours = remainingMillis / (1000 * 60 * 60)
            val minutes = (remainingMillis / (1000 * 60)) % 60
            val seconds = (remainingMillis / 1000) % 60
            val timeFormatted = String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds)

            onTick(progress, timeFormatted, isBloodMoonActive)
            if (isRunning) {
                handler.postDelayed(this, updateInterval)
            }
        }
    }

    fun start() {
        if (!isRunning) {
            isRunning = true
            handler.post(timerRunnable)
        }
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    fun isRunning(): Boolean = isRunning
}