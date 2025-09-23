package com.antago30.a7dtd_lukomorie.fragments

import com.antago30.a7dtd_lukomorie.logic.BloodMoonProgressTimer
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.logic.BloodMoonCalculator
import com.antago30.a7dtd_lukomorie.model.ServerInfo
import com.antago30.a7dtd_lukomorie.utils.Constants
import com.antago30.a7dtd_lukomorie.logic.BloodMoonTimerController
import java.time.LocalDateTime
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.core.graphics.toColorInt

class InfoFragment : BaseFragment() {

    private lateinit var statusText: TextView
    private lateinit var timeText: TextView
    private lateinit var dayText: TextView
    private lateinit var playersOnlineText: TextView
    private lateinit var nextBloodMoonText: TextView

    private var bloodMoonTimer: BloodMoonProgressTimer? = null
    private var timerController: BloodMoonTimerController? = null

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

        // Инициализация спиннера
        val spinnerReminder = view.findViewById<Spinner>(R.id.spinner_reminder)
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.reminder_times,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerReminder.adapter = adapter

        spinnerReminder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Инициализация переключателя
        val switch = view.findViewById<SwitchMaterial>(R.id.switch_reminder)
        switch.trackTintList = ColorStateList.valueOf("#666666".toColorInt())
        switch.setOnCheckedChangeListener { _, isChecked ->
            switch.thumbTintList = ColorStateList.valueOf(
                if (isChecked) "#00FF00".toColorInt() else "#FF0000".toColorInt()
            )
            Toast.makeText(
                context,
                if (isChecked) "Напоминание включено" else "Напоминание выключено",
                Toast.LENGTH_SHORT
            ).show()
        }

        return view
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