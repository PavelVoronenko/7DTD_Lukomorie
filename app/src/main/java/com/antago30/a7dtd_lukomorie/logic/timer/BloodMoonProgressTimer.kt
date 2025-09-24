package com.antago30.a7dtd_lukomorie.logic.timer

import android.os.Handler
import android.os.Looper
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale

class BloodMoonProgressTimer(
    private val previousBloodMoonStart: LocalDateTime,
    val nextBloodMoonStart: LocalDateTime,
    private val onTick: (progressPercent: Int, timeLeftFormatted: String, isBloodMoonActive: Boolean) -> Unit,
    private val onFinish: () -> Unit = {}
) {

    companion object {
        const val BLOOD_MOON_DURATION_HOURS = 6L
    }

    private val totalCycleDurationMillis = ChronoUnit.MILLIS.between(previousBloodMoonStart, nextBloodMoonStart).coerceAtLeast(1)
    private val bloodMoonEnd = nextBloodMoonStart.plusHours(BLOOD_MOON_DURATION_HOURS)

    private var isRunning = false
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L

    private val timerRunnable = object : Runnable {
        override fun run() {
            val now = LocalDateTime.now()
            val isBloodMoonActive = now.isAfter(nextBloodMoonStart) && now.isBefore(bloodMoonEnd)

            val remainingMillis = if (isBloodMoonActive) {
                ChronoUnit.MILLIS.between(now, bloodMoonEnd).coerceAtLeast(0)
            } else {
                ChronoUnit.MILLIS.between(now, nextBloodMoonStart).coerceAtLeast(0)
            }

            if (!isBloodMoonActive && remainingMillis <= 0) {
                onTick(100, "00:00:00", true)
                onFinish()
                stop()
                return
            }

            val progress = if (isBloodMoonActive) {
                100
            } else {
                val elapsedMillis = ChronoUnit.MILLIS.between(previousBloodMoonStart, now).coerceAtLeast(0)
                ((elapsedMillis.toDouble() / totalCycleDurationMillis) * 100).toInt().coerceIn(0, 100)
            }

            val hours = remainingMillis / (1000 * 60 * 60)
            val minutes = (remainingMillis / (1000 * 60)) % 60
            val seconds = (remainingMillis / 1000) % 60
            val timeFormatted = String.Companion.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds)

            onTick(progress, timeFormatted, isBloodMoonActive)
            handler.postDelayed(this, updateInterval)
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
        handler.removeCallbacks(timerRunnable)
    }

    fun isRunning(): Boolean = isRunning
}