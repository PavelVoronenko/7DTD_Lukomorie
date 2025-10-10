package com.antago30.a7dtd_lukomorie.logic

import com.antago30.a7dtd_lukomorie.utils.Constants
import java.time.LocalDateTime
import java.time.LocalTime

class BloodMoonCalculator(
    private val gameDayLengthSeconds: Long = Constants.LENGTH_OF_DAY,
    private val bloodMoonDay: Int = 7,
    private val bloodMoonHour: Double = 22.0
) {

    companion object {
        private const val GAME_HOURS_PER_DAY = 24.0
    }

    /**
     * Рассчитывает дату и время следующей кровавой луны.
    */
    fun calculateNextBloodMoon(
        currentGameDay: Int,
        currentGameTime: String,
        now: LocalDateTime = LocalDateTime.now()
    ): LocalDateTime {
        // Определяем, какой игровой день будет следующим "днём луны"
        val daysUntilNextBloodMoonDay = calculateDaysUntilNextBloodMoonDay(currentGameDay)

        // Определяем игровое время начала луны
        val bloodMoonGameTime = LocalTime.of(bloodMoonHour.toInt(), ((bloodMoonHour % 1) * 60).toInt())

        // Парсим текущее игровое время
        val parsedCurrentTime = try {
            LocalTime.parse(currentGameTime)
        } catch (e: Exception) {
            // Если не удалось распарсить, считаем 00:00
            LocalTime.MIDNIGHT
        }

        // Проверяем, уже ли прошла луна сегодня (если сегодня день луны)
        val isTodayBloodMoonDay = (currentGameDay % bloodMoonDay == 0)
        val isBloodMoonPassedToday = isTodayBloodMoonDay && parsedCurrentTime.isAfter(bloodMoonGameTime)

        // Если луна уже прошла сегодня, ждём следующего дня луны
        val finalDaysToAdd = if (isBloodMoonPassedToday) daysUntilNextBloodMoonDay + bloodMoonDay else daysUntilNextBloodMoonDay

        // Считаем, сколько реальных секунд осталось до начала луны
        val secondsUntilBloodMoonStarts = calculateSecondsUntilGameTime(
            currentGameDay,
            parsedCurrentTime,
            finalDaysToAdd,
            bloodMoonGameTime
        )

        // Добавляем секунды к текущему реальному времени
        return now.plusSeconds(secondsUntilBloodMoonStarts.toLong())
    }

    /**
     * Считает, сколько игровых дней осталось до ближайшего "дня луны".
     */
    private fun calculateDaysUntilNextBloodMoonDay(currentGameDay: Int): Int {
        val remainder = currentGameDay % bloodMoonDay
        return if (remainder == 0) 0 else bloodMoonDay - remainder
    }

    /**
     * Считает, сколько реальных секунд осталось до определённого игрового времени.
     */
    private fun calculateSecondsUntilGameTime(
        currentGameDay: Int,
        currentTime: LocalTime,
        daysToAdd: Int,
        targetTime: LocalTime
    ): Double {
        // Общее количество игровых дней, в которых будет луна
        val targetGameDay = currentGameDay + daysToAdd

        // Сколько игровых часов прошло с начала текущего дня
        val currentHours = currentTime.hour + currentTime.minute / 60.0

        // Сколько игровых часов до начала луны (в том же дне)
        val hoursUntilTargetInSameDay = bloodMoonHour - currentHours

        // Если луна в том же дне и ещё не наступила
        if (daysToAdd == 0 && hoursUntilTargetInSameDay > 0) {
            return hoursUntilTargetInSameDay * (gameDayLengthSeconds / GAME_HOURS_PER_DAY)
        }

        // Если луна в будущем дне
        // Считаем общее количество игровых часов до луны
        // (сколько часов осталось до конца текущего дня) + (полные дни) + (часы в дне луны)
        val hoursUntilEndOfCurrentDay = GAME_HOURS_PER_DAY - currentHours
        val fullDaysInHours = (daysToAdd - 1) * GAME_HOURS_PER_DAY
        val hoursFromStartOfTargetDay = bloodMoonHour

        val totalGameHoursUntilBloodMoon = hoursUntilEndOfCurrentDay + fullDaysInHours + hoursFromStartOfTargetDay

        return totalGameHoursUntilBloodMoon * (gameDayLengthSeconds / GAME_HOURS_PER_DAY)
    }

    /**
     * Форматирует LocalDateTime в строку.
     */
    fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    }
}