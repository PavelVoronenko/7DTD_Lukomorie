package com.antago30.a7dtd_lukomorie.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.logic.BloodMoonCalculator
import com.antago30.a7dtd_lukomorie.model.ServerInfo
import com.antago30.a7dtd_lukomorie.utils.Constants
import java.time.LocalDateTime

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

        statusText = view.findViewById(R.id.status_text)
        timeText = view.findViewById(R.id.time_text)
        dayText = view.findViewById(R.id.day_text)
        playersOnlineText = view.findViewById(R.id.players_online_text)
        nextBloodMoonText = view.findViewById(R.id.next_blood_moon_text)

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
            statusText.text = "Статус: ${data.status}"
            timeText.text = "Время: ${data.time}"
            dayText.text = "День: ${data.day}"
            playersOnlineText.text = "Онлайн: ${data.playersOnline}"

            try {
                val calculator = BloodMoonCalculator()
                val nextBloodMoonDateTime = calculator.calculateNextBloodMoon(
                    currentGameDay = data.day,
                    currentGameTime = data.time
                )
                val formattedDate = calculator.formatDateTime(nextBloodMoonDateTime)
                nextBloodMoonText.text = "След. кровавая луна: $formattedDate"
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