package com.antago30.a7dtd_lukomorie.logic

import android.widget.TextView
import com.antago30.a7dtd_lukomorie.logic.timer.BloodMoonProgressTimer
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class BloodMoonDisplayManager(
    private val circularTimer: CircularProgressIndicator,
    private val timerText: TextView,
    private val previousBloodMoon: LocalDateTime,
    private val nextBloodMoon: LocalDateTime
) {

    companion object {
        const val BLOOD_MOON_DURATION_HOURS = 6L
    }

    private val bloodMoonEnd = nextBloodMoon.plusHours(BLOOD_MOON_DURATION_HOURS)

    fun updateStaticState() {
        val now = LocalDateTime.now()
        val isBloodMoonActive = now.isAfter(nextBloodMoon) && now.isBefore(bloodMoonEnd)

        val progress = if (isBloodMoonActive) {
            100
        } else {
            val elapsedMillis = ChronoUnit.MILLIS.between(previousBloodMoon, now).coerceAtLeast(0)
            val totalDurationMillis = ChronoUnit.MILLIS.between(previousBloodMoon, nextBloodMoon).coerceAtLeast(1)
            ((elapsedMillis.toDouble() / totalDurationMillis) * 100).toInt().coerceIn(0, 100)
        }

        val remainingMillis = if (isBloodMoonActive) {
            ChronoUnit.MILLIS.between(now, bloodMoonEnd).coerceAtLeast(0)
        } else {
            ChronoUnit.MILLIS.between(now, nextBloodMoon).coerceAtLeast(0)
        }

        val hours = remainingMillis / (1000 * 60 * 60)
        val minutes = (remainingMillis / (1000 * 60)) % 60
        val seconds = (remainingMillis / 1000) % 60
        timerText.text = String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds)

        circularTimer.progress = progress
        circularTimer.setIndicatorColor(
            if (isBloodMoonActive) android.graphics.Color.RED
            else when {
                progress > 60 -> android.graphics.Color.RED
                progress > 30 -> android.graphics.Color.YELLOW
                else -> android.graphics.Color.GREEN
            }
        )
    }

    fun startDynamicTimer(
        onTimerStop: () -> Unit
    ): BloodMoonProgressTimer {
        onTimerStop()

        return BloodMoonProgressTimer(
            previousBloodMoonStart = previousBloodMoon,
            nextBloodMoonStart = nextBloodMoon,
            onTick = { progress, timeFormatted, isBloodMoonNow ->
                circularTimer.progress = progress
                timerText.text = timeFormatted
                circularTimer.setIndicatorColor(
                    if (isBloodMoonNow) android.graphics.Color.RED
                    else when {
                        progress > 60 -> android.graphics.Color.RED
                        progress > 30 -> android.graphics.Color.YELLOW
                        else -> android.graphics.Color.GREEN
                    }
                )
            },
            onFinish = {
                timerText.text = "Луна началась! 🌕"
                circularTimer.setIndicatorColor(android.graphics.Color.RED)
                circularTimer.progress = 100
            }
        ).apply { start() }
    }
}