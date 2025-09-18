package com.antago30.a7dtd_lukomorie.parser

import android.util.Log
import com.antago30.a7dtd_lukomorie.model.NewsItem
import com.antago30.a7dtd_lukomorie.model.Player
import com.antago30.a7dtd_lukomorie.model.ServerInfo
import org.jsoup.Jsoup
import java.io.IOException
import java.time.format.DateTimeFormatter

class WebParser {

    private val dateFormat = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    @Throws(IOException::class)
    fun parsePlayers(url: String): List<Player> {
        val document = Jsoup.connect(url).get()
        val table = document.select("table[border='0']")
        val rows = table.select("tr").drop(1)
        return rows.map { row ->
            val cells = row.select("td")
            Player(
                name = cells[0].select("a").text(),
                level = cells[1].text().toIntOrNull() ?: 0,
                deaths = cells[2].text().toIntOrNull() ?: 0,
                zombiesKilled = cells[3].text().toIntOrNull() ?: 0,
                playersKilled = cells[4].text().toIntOrNull() ?: 0,
                totalScore = cells[5].text().toIntOrNull() ?: 0
            )
        }
    }

    @Throws(IOException::class)
    fun parseInfo(url: String): ServerInfo {
        val document = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36")
            .timeout(20000)
            .get()

        val fullText = document.body().text().trim()
        Log.d("WebParser", "🔍 Полный текст страницы:\n$fullText")

        val statusPattern = Regex("Статус сервера:\\s*(.+?)\\s+Время")
        val statusMatch = statusPattern.find(fullText)
        val status = statusMatch?.groups?.get(1)?.value?.trim() ?: "Unknown"

        val timeDayPattern = Regex("Время сервера:\\s*(\\d{1,2}:\\d{1,2})\\s*День №\\s*(\\d+)")
        val timeDayMatch = timeDayPattern.find(fullText)
        val time = timeDayMatch?.groups?.get(1)?.value ?: "00:00"
        val day = timeDayMatch?.groups?.get(2)?.value?.toIntOrNull() ?: 0

        val playersPattern = Regex("Игроков на сервере:\\s*(\\d+)")
        val playersMatch = playersPattern.find(fullText)
        val playersOnline = playersMatch?.groups?.get(1)?.value?.toIntOrNull() ?: 0

        val bloodMoonHour = 22
        val bloodMoonTime = "${bloodMoonHour}:00"

        return ServerInfo(
            status = status,
            time = time,
            day = day,
            playersOnline = playersOnline,
            bloodMoonTime = bloodMoonTime
        )
    }

    @Throws(IOException::class)
    fun parseNews(url: String): List<NewsItem> {
        val document = Jsoup.connect(url).get()
        val newsItems = document.select(".news-item")
        return newsItems.map { item ->
            val title = item.select(".title").text()
            val content = item.select(".content").text()
            val timestamp = item.select(".timestamp").text()
            NewsItem(title, content, timestamp)
        }
    }

    @Throws(IOException::class)
    fun parseLeaderboard(url: String): List<Player> {
        val document = Jsoup.connect(url).get()
        val rows = document.select("table tr").drop(1)
        return rows.map { row ->
            val cells = row.select("td")
            Player(
                name = cells[0].text(),
                level = cells[1].text().toIntOrNull() ?: 0,
                deaths = cells[2].text().toIntOrNull() ?: 0,
                zombiesKilled = cells[3].text().toIntOrNull() ?: 0,
                playersKilled = cells[4].text().toIntOrNull() ?: 0,
                totalScore = cells[5].text().toIntOrNull() ?: 0
            )
        }
    }

    @Throws(IOException::class)
    fun parseVisitors(url: String): Int {
        val document = Jsoup.connect(url).get()
        val text = document.select("span.visitors-count").first()?.text() ?: "0"
        return text.toIntOrNull() ?: 0
    }
}