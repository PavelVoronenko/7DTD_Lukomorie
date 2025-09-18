package com.antago30.a7dtd_lukomorie.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.model.ServerInfo
import com.antago30.a7dtd_lukomorie.parser.WebParser
import com.antago30.a7dtd_lukomorie.utils.Constants

class InfoFragment : BaseFragment() {

    private lateinit var statusText: TextView
    private lateinit var timeText: TextView
    private lateinit var dayText: TextView
    private lateinit var playersOnlineText: TextView
    private lateinit var bloodMoonText: TextView

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
        bloodMoonText = view.findViewById(R.id.blood_moon_time_text)

        return view
    }

    // 👇 ПАРСИНГ — В ФОНЕ
    override fun loadData(): Any {
        return try {
            webParser.parseInfo(Constants.INFO_URL)
        } catch (e: Exception) {
            Log.e("InfoFragment", "Ошибка парсинга", e)
            ServerInfo("Ошибка загрузки", "00:00", 0, 0, "22:00") // 👈 Фейковый ответ при ошибке
        }
    }

    override fun updateUI(data: Any) {
        if (data is ServerInfo) {
            when {
                data.status == "Ошибка загрузки" -> {
                    statusText.text = "Ошибка загрузки"
                    timeText.text = ""
                    dayText.text = ""
                    playersOnlineText.text = ""
                    bloodMoonText.text = ""
                }
                else -> {
                    statusText.text = "Статус: ${data.status}"
                    timeText.text = "Время: ${data.time}"
                    dayText.text = "День: ${data.day}"
                    playersOnlineText.text = "Онлайн: ${data.playersOnline}"
                    bloodMoonText.text = "Кровавая луна: ${data.bloodMoonTime}"
                }
            }
        } else {
            statusText.text = "Непредвиденные данные"
        }
    }
}