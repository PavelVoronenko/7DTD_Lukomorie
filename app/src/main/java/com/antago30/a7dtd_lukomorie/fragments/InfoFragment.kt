package com.antago30.a7dtd_lukomorie.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.logic.BloodMoonCalculator
import com.antago30.a7dtd_lukomorie.logic.BloodMoonProgressTimer
import com.antago30.a7dtd_lukomorie.logic.BloodMoonTimerController
import com.antago30.a7dtd_lukomorie.logic.BloodMoonReminderManager
import com.antago30.a7dtd_lukomorie.model.ServerInfo
import com.antago30.a7dtd_lukomorie.utils.Constants
import com.google.android.material.switchmaterial.SwitchMaterial
import java.time.LocalDateTime
import android.widget.Spinner

class InfoFragment : BaseFragment() {

    private lateinit var statusText: TextView
    private lateinit var timeText: TextView
    private lateinit var dayText: TextView
    private lateinit var playersOnlineText: TextView
    private lateinit var nextBloodMoonText: TextView
    private lateinit var spinnerReminder: Spinner
    private lateinit var switchReminder: SwitchMaterial
    private var bloodMoonTimer: BloodMoonProgressTimer? = null
    private var timerController: BloodMoonTimerController? = null
    private lateinit var reminderManager: BloodMoonReminderManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_info, container, false)

        statusText = view.findViewById(R.id.status_value)
        timeText = view.findViewById(R.id.time_value)
        dayText = view.findViewById(R.id.day_value)
        playersOnlineText = view.findViewById(R.id.players_value)
        nextBloodMoonText = view.findViewById(R.id.blood_moon_value)
        spinnerReminder = view.findViewById(R.id.spinner_reminder)
        switchReminder = view.findViewById(R.id.switch_reminder)

        // Инициализация менеджера напоминаний
        reminderManager = BloodMoonReminderManager(requireContext())

        // Настройка спиннера
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.reminder_times,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerReminder.adapter = adapter

        // Восстанавливаем СОХРАНЁННОЕ ВРЕМЯ и устанавливаем позицию спиннера
        val savedMinutes = reminderManager.getSavedReminderMinutes()
        val minutesArray = resources.getStringArray(R.array.reminder_times)
        val position = when (savedMinutes) {
            15 -> 0
            30 -> 1
            60 -> 2
            120 -> 3
            else -> 0
        }
        spinnerReminder.setSelection(position)

        // Обработчик выбора времени
        spinnerReminder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (switchReminder.isChecked) {
                    cachedNextBloodMoonDateTime?.let { nextBloodMoonTime ->
                        val minutes = parseMinutesFromSpinner(position)
                        reminderManager.cancelReminder()
                        reminderManager.scheduleReminder(nextBloodMoonTime, minutes) {
                            Toast.makeText(context, "Напоминание обновлено!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Настройка переключателя
        switchReminder.trackTintList = ColorStateList.valueOf("#666666".toColorInt())
        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            switchReminder.thumbTintList = ColorStateList.valueOf(
                if (isChecked) "#00FF00".toColorInt() else "#FF0000".toColorInt()
            )

            if (isChecked) {
                cachedNextBloodMoonDateTime?.let { nextBloodMoonTime ->
                    val position = spinnerReminder.selectedItemPosition
                    val minutes = parseMinutesFromSpinner(position)
                    reminderManager.scheduleReminder(nextBloodMoonTime, minutes) {
                        Toast.makeText(context, "Напоминание установлено!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                reminderManager.cancelReminder()
                Toast.makeText(context, "Напоминание отключено", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Восстанавливаем состояние переключателя
        if (reminderManager.shouldAutoDisable()) {
            switchReminder.isChecked = false
            reminderManager.cancelReminder()
        } else if (reminderManager.isReminderActive()) {
            switchReminder.isChecked = true
            // Дополнительно: если активно — убеждаемся, что спиннер в правильной позиции
            val savedMinutes = reminderManager.getSavedReminderMinutes()
            val position = when (savedMinutes) {
                15 -> 0
                30 -> 1
                60 -> 2
                120 -> 3
                else -> 0
            }
            spinnerReminder.setSelection(position)
        }
    }

    private fun parseMinutesFromSpinner(position: Int): Int {
        return when (position) {
            0 -> 15
            1 -> 30
            2 -> 60
            3 -> 120
            else -> 15
        }
    }

    override fun loadData(): Any {
        return try {
            webParser.parseInfo(Constants.INFO_URL)
        } catch (e: Exception) {
            Log.e("InfoFragment", "Ошибка загрузки данных", e)
            ServerInfo("Ошибка загрузки", "00:00", 0, 0, "22:00")
        }
    }

    override fun updateUI(data: Any) {
        if (data !is ServerInfo) {
            statusText.text = "Ошибка отображения"
            return
        }

        statusText.text = if (data.status == "в сети.") "Online" else "Offline"
        timeText.text = data.time
        dayText.text = "${data.day}"
        dayText.setTextColor(
            if (data.day % 7 == 0)
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
            else
                ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light)
        )
        playersOnlineText.text = "${data.playersOnline}"

        calculateAndDisplayBloodMoonTime(data)
        startBloodMoonTimer(data)
    }

    private fun calculateAndDisplayBloodMoonTime(data: ServerInfo) {
        val calculator = BloodMoonCalculator()
        try {
            val nextBloodMoonDateTime = calculator.calculateNextBloodMoon(
                currentGameDay = data.day,
                currentGameTime = data.time
            )
            nextBloodMoonText.text = calculator.formatDateTime(nextBloodMoonDateTime)
            nextBloodMoonText.visibility = View.VISIBLE
            cachedNextBloodMoonDateTime = nextBloodMoonDateTime

            if (switchReminder.isChecked) {
                val position = spinnerReminder.selectedItemPosition
                val minutes = parseMinutesFromSpinner(position)
                reminderManager.cancelReminder()
                reminderManager.scheduleReminder(nextBloodMoonDateTime, minutes) {}
            }
        } catch (e: Exception) {
            Log.e("InfoFragment", "Ошибка вычисления луны", e)
            nextBloodMoonText.text = "Ошибка: Не удалось вычислить луну"
            nextBloodMoonText.visibility = View.VISIBLE
            cachedNextBloodMoonDateTime = null
        }
    }

    private fun startBloodMoonTimer(data: ServerInfo) {
        val nextBloodMoonDateTime = cachedNextBloodMoonDateTime ?: return
        val gameDayLengthSeconds = 8111L
        val previousBloodMoonDateTime = nextBloodMoonDateTime.minusSeconds(7 * gameDayLengthSeconds)

        timerController = BloodMoonTimerController(
            circularTimer = requireView().findViewById(R.id.circular_timer),
            timerText = requireView().findViewById(R.id.timer_text),
            previousBloodMoon = previousBloodMoonDateTime,
            nextBloodMoon = nextBloodMoonDateTime
        )

        if (data.playersOnline == 0) {
            bloodMoonTimer?.stop()
            bloodMoonTimer = null
            timerController?.updateStaticState()
        } else {
            bloodMoonTimer = timerController?.startDynamicTimer(
                onTimerStop = { bloodMoonTimer?.stop() }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bloodMoonTimer?.stop()
    }

    private var cachedNextBloodMoonDateTime: LocalDateTime? = null
}