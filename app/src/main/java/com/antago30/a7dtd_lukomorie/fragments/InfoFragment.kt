package com.antago30.a7dtd_lukomorie.fragments

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.logic.BloodMoonCalculator
import com.antago30.a7dtd_lukomorie.model.ServerInfo
import com.antago30.a7dtd_lukomorie.utils.Constants
import android.widget.Spinner
import com.google.android.material.progressindicator.CircularProgressIndicator
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.core.graphics.toColorInt
import android.view.animation.AccelerateDecelerateInterpolator


class InfoFragment : BaseFragment() {

    private lateinit var statusText: TextView
    private lateinit var timeText: TextView
    private lateinit var dayText: TextView
    private lateinit var playersOnlineText: TextView
    private lateinit var nextBloodMoonText: TextView

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
                // Действие при выборе
                //Toast.makeText(context, "Выбрано: $selected", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val switch = view.findViewById<SwitchMaterial>(R.id.switch_reminder)

        switch.trackTintList = ColorStateList.valueOf("#666666".toColorInt())

        switch.setOnCheckedChangeListener { _, isChecked ->
            switch.thumbTintList = ColorStateList.valueOf(
                if (isChecked) "#00FF00".toColorInt()
                else "#FF0000".toColorInt()
            )
            Toast.makeText(
                context,
                if (isChecked) "Напоминание включено" else "Напоминание выключено",
                Toast.LENGTH_SHORT
            ).show()
        }

        val timer = view.findViewById<CircularProgressIndicator>(R.id.circular_timer)

        // Убираем неопределённый режим
        timer.isIndeterminate = false

        val animator = ValueAnimator.ofInt(0, 85)
        animator.duration = 2000 // 2 секунды
        animator.interpolator = AccelerateDecelerateInterpolator()

        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            timer.progress = progress
        }

        animator.start()

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
        if (data is ServerInfo) {
            statusText.text = "${if (data.status == "в сети.") "Online" else "Offline"}"
            timeText.text = "${data.time}"
            if (data.day % 7 == 0) {
                dayText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
            } else {
                dayText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))
            }
            dayText.text = "${data.day}"
            playersOnlineText.text = "${data.playersOnline}"

            try {
                val calculator = BloodMoonCalculator()
                val nextBloodMoonDateTime = calculator.calculateNextBloodMoon(
                    currentGameDay = data.day,
                    currentGameTime = data.time
                )
                val formattedDate = calculator.formatDateTime(nextBloodMoonDateTime)
                nextBloodMoonText.text = "$formattedDate"
                nextBloodMoonText.visibility = View.VISIBLE
            } catch (e: Exception) {
                Log.e("InfoFragment", "Ошибка вычисления луны", e)
                nextBloodMoonText.text = "Ошибка: Не удалось вычислить луну"
                nextBloodMoonText.visibility = View.VISIBLE
            }
        } else {
            statusText.text = "Ошибка отображения"
        }
    }
}