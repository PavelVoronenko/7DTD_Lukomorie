package com.antago30.a7dtd_lukomorie.logic

import android.graphics.Color
import android.widget.TextView
import com.antago30.a7dtd_lukomorie.R
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale

class BloodMoonTimerController(
    private val circularTimer: CircularProgressIndicator,
    private val timerText: TextView,
    private val previousBloodMoon: LocalDateTime,
    private val nextBloodMoon: LocalDateTime
) {

    companion object {
        const val BLOOD_MOON_DURATION_HOURS = 6L
    }

    private val bloodMoonEnd = nextBloodMoon.plusHours(BLOOD_MOON_DURATION_HOURS)

    //статичное состояние (для 0 игроков)
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
        timerText.text = String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds)

        circularTimer.progress = progress
        circularTimer.setIndicatorColor(
            if (isBloodMoonActive) Color.RED
            else when {
                progress > 60 -> Color.RED
                progress > 30 -> Color.YELLOW
                else -> Color.GREEN
            }
        )
    }

    //Запускает таймер (для игроков > 0)
    fun startDynamicTimer(
        onTimerStop: () -> Unit,
    ): BloodMoonProgressTimer {
        onTimerStop()

        return BloodMoonProgressTimer(
            previousBloodMoonStart = previousBloodMoon,
            nextBloodMoonStart = nextBloodMoon,
            onTick = { progress, timeFormatted, isBloodMoonNow ->
                circularTimer.progress = progress
                timerText.text = timeFormatted
                circularTimer.setIndicatorColor(
                    if (isBloodMoonNow) Color.RED
                    else when {
                        progress > 60 -> R.color.red
                        progress > 30 -> Color.YELLOW
                        else -> Color.GREEN
                    }
                )
            },
            onFinish = {
                timerText.text = "Луна началась! 🌕"
                circularTimer.setIndicatorColor(Color.RED)
                circularTimer.progress = 100
            }
        ).apply { start() }
    }
}