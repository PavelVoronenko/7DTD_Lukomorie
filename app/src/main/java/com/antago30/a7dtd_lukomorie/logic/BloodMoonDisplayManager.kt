package com.antago30.a7dtd_lukomorie.logic

import android.util.Log
import android.widget.TextView
import com.antago30.a7dtd_lukomorie.logic.timer.BloodMoonProgressTimer
import com.antago30.a7dtd_lukomorie.utils.Constants
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale

class BloodMoonDisplayManager(
    private val circularTimer: CircularProgressIndicator,
    private val timerText: TextView,
    private val previousBloodMoon: LocalDateTime,
    private val currentBloodMoon: LocalDateTime,
    private val nextBloodMoon: LocalDateTime,

    private val nextBloodMoonTv: TextView,
    private val currentBloodMoonTv: TextView,
    private val bloodMoonEndTv: TextView,
    private val isBloodMoonNowTv: TextView,
) {

    companion object {
        const val BLOOD_MOON_DURATION_HOURS = 6L
    }


    // Вычисляем длительность Blood Moon в секундах (игровое время → реальное)
    private val bloodMoonDurationSeconds = (BLOOD_MOON_DURATION_HOURS * Constants.LENGTH_OF_DAY / 24)

    // Окончание Blood Moon в реальном времени
    //--------------
    /*val nextBloodMoonTest = LocalDateTime.of(2025, 10, 10, 10, 4)
    private val bloodMoonEnd = LocalDateTime.of(2025, 10, 10, 10, 7)*/
    //--------------
    private val bloodMoonEnd: LocalDateTime = currentBloodMoon.plusSeconds(bloodMoonDurationSeconds)

    fun updateStaticState() {
        val now = LocalDateTime.now()
        val isBloodMoon = isBloodMoonActive(now)

        val progress = if (isBloodMoon) {
            100
        } else {
            val elapsedMillis = ChronoUnit.MILLIS.between(previousBloodMoon, now).coerceAtLeast(0)
            val totalDurationMillis = ChronoUnit.MILLIS.between(previousBloodMoon, currentBloodMoon).coerceAtLeast(1)
            ((elapsedMillis.toDouble() / totalDurationMillis) * 100).toInt().coerceIn(0, 100)
        }

        if (isBloodMoon) {
            timerText.text = "Сейчас!"
        } else {
            val remainingMillis = ChronoUnit.MILLIS.between(now, currentBloodMoon).coerceAtLeast(0)
            val hours = remainingMillis / (1000 * 60 * 60)
            val minutes = (remainingMillis / (1000 * 60)) % 60
            val seconds = (remainingMillis / 1000) % 60
            timerText.text = String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds)
        }

        Log.d("BloodMoon", "nextBloodMoon: $nextBloodMoon")
        Log.d("BloodMoon", "currentBloodMoon: $currentBloodMoon")
        Log.d("BloodMoon", "bloodMoonEnd: $bloodMoonEnd")
        Log.d("BloodMoon", "isBloodMoon: $isBloodMoon")

        nextBloodMoonTv.text = "nextBloodMoon: $nextBloodMoon".replace("T", " | ")
        currentBloodMoonTv.text = "currentBloodMoon: $currentBloodMoon".replace("T", " | ")
        bloodMoonEndTv.text = "bloodMoonEnd: $bloodMoonEnd".replace("T", " | ")
        isBloodMoonNowTv.text = "isBloodMoon: $isBloodMoon"

        circularTimer.progress = progress
        circularTimer.setIndicatorColor(
            if (isBloodMoon) android.graphics.Color.RED
            else when {
                progress > 80 -> android.graphics.Color.RED
                progress > 50 -> android.graphics.Color.YELLOW
                else -> android.graphics.Color.GREEN
            }
        )
    }

    fun startDynamicTimer(onTimerStop: () -> Unit): BloodMoonProgressTimer {
        onTimerStop()

        return BloodMoonProgressTimer(
            previousBloodMoonStart = previousBloodMoon,
            nextBloodMoonStart = nextBloodMoon,
            bloodMoonEnd = bloodMoonEnd,
            currentBloodMoon = currentBloodMoon,
            onTick = { progress, timeFormatted, isBloodMoonNow ->

                if (isBloodMoonNow) {
                    timerText.text = "Сейчас!"
                    circularTimer.progress = 100
                } else {
                    timerText.text = timeFormatted
                    circularTimer.progress = progress
                }

                Log.d("BloodMoon", "nextBloodMoon: $nextBloodMoon")
                Log.d("BloodMoon", "currentBloodMoon: $currentBloodMoon")
                Log.d("BloodMoon", "bloodMoonEnd: $bloodMoonEnd")
                Log.d("BloodMoon", "isBloodMoonNow: $isBloodMoonNow")

                nextBloodMoonTv.text = "nextBloodMoon: $nextBloodMoon".replace("T", " | ")
                currentBloodMoonTv.text = "currentBloodMoon: $currentBloodMoon".replace("T", " | ")
                bloodMoonEndTv.text = "bloodMoonEnd: $bloodMoonEnd".replace("T", " | ")
                isBloodMoonNowTv.text = "isBloodMoon: $isBloodMoonNow"

                circularTimer.setIndicatorColor(
                    if (isBloodMoonNow) android.graphics.Color.RED
                    else when {
                        progress > 80 -> android.graphics.Color.RED
                        progress > 50 -> android.graphics.Color.YELLOW
                        else -> android.graphics.Color.GREEN
                    }
                )
            },
            onFinish = {
                /*timerText.text = "Сейчас!"
                circularTimer.setIndicatorColor(android.graphics.Color.RED)
                circularTimer.progress = 100*/
            }
        ).apply { start() }
    }

    fun isBloodMoonActive(now: LocalDateTime = LocalDateTime.now()): Boolean {
        return now.isAfter(currentBloodMoon) && now.isBefore(bloodMoonEnd)
    }
}