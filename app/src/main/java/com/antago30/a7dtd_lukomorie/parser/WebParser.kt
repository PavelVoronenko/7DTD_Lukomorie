package com.antago30.a7dtd_lukomorie.parser

import android.util.Log
import com.antago30.a7dtd_lukomorie.model.BannedPlayer
import com.antago30.a7dtd_lukomorie.model.NewsItem
import com.antago30.a7dtd_lukomorie.model.PlayerItem
import com.antago30.a7dtd_lukomorie.model.ServerInfo
import com.antago30.a7dtd_lukomorie.model.VisitorItem
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WebParser {

    @Throws(IOException::class)
    fun parseOnlinePlayers(url: String): List<PlayerItem> {
        val document = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .timeout(10000)
            .get()

        val players = mutableListOf<PlayerItem>()
        val table = document.select("table").firstOrNull() ?: return emptyList()

        val rows = table.select("tr").drop(1)
        for (row in rows) {
            val cells = row.select("td")
            if (cells.size < 6) continue // Пропускаем неполные строки

            try {
                val nameElement = cells[0].selectFirst("a")
                val name = nameElement?.text()?.trim() ?: cells[0].text().trim()
                val steamProfileUrl = nameElement?.attr("href")?.takeIf { it.isNotBlank() }

                val level = cells[1].text().trim().toIntOrNull() ?: 0
                val deaths = cells[2].text().trim().toIntOrNull() ?: 0
                val zombiesKilled = cells[3].text().trim().toIntOrNull() ?: 0
                val playersKilled = cells[4].text().trim().toIntOrNull() ?: 0
                val totalScore = cells[5].text().trim().toIntOrNull() ?: 0

                players.add(
                    PlayerItem(
                        name = name,
                        level = level,
                        deaths = deaths,
                        zombiesKilled = zombiesKilled,
                        playersKilled = playersKilled,
                        totalScore = totalScore,
                        steamProfileUrl = steamProfileUrl
                    )
                )
            } catch (e: Exception) {
                // Пропускаем строку с ошибкой
            }
        }

        return players
    }

    @Throws(IOException::class)
    fun parseInfo(url: String): ServerInfo {
        val document = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36")
            .timeout(10000)
            .get()

        val fullText = document.body().text().trim()

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
        val document = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .timeout(10000)
            .get()

        val newsItems = mutableListOf<NewsItem>()
        val inputDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val baseUrl = "http://79.173.124.221:2000/"

        val headers = document.select("h3")

        for (header in headers) {
            val fullHeaderText = header.text().trim()
            val regex = Regex("""(\d{2}\.\d{2}\.\d{4})\s*-\s*(.+)""")
            val matchResult = regex.matchEntire(fullHeaderText)

            if (matchResult != null) {
                val (dateStr, title) = matchResult.destructured

                val contentBuilder = StringBuilder()
                var node: Node? = header.nextSibling()

                while (node != null) {
                    if (node is Element) {
                        val tagName = node.tagName().lowercase()
                        if (tagName == "hr" || tagName == "h3") break
                        contentBuilder.append(node.outerHtml())
                    } else {
                        contentBuilder.append(node.toString())
                    }
                    node = node.nextSibling()
                }

                var rawHtmlContent = contentBuilder.toString()

                rawHtmlContent = rawHtmlContent
                    .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), " ")
                    .replace(Regex("""href\s*=\s*["'](.*?)["']""")) { matchResult ->
                        val originalHref = matchResult.groupValues[1].trim()
                        if (originalHref.startsWith("http://") || originalHref.startsWith("https://")) {
                            "href=\"$originalHref\""
                        } else {
                            "href=\"$baseUrl$originalHref\""
                        }
                    }
                    .trim()
                    .replace(Regex("\\s+"), " ")

                newsItems.add(
                    NewsItem(
                        title = "\uD83D\uDCF0 $title",
                        content = rawHtmlContent,
                        timestamp = dateStr
                    )
                )
            }
        }

        return newsItems.sortedWith(compareByDescending { newsItem ->
            try {
                inputDateFormat.parse(newsItem.timestamp)
            } catch (e: Exception) {
                Date(0)
            }
        })
    }

    @Throws(IOException::class)
    fun parseLeaderboard(url: String): List<PlayerItem> {
        val document = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .timeout(15000)
            .get()

        val players = mutableListOf<PlayerItem>()
        val table = document.select("table").firstOrNull() ?: return emptyList()


        val rows = table.select("tr").drop(1)
        for (row in rows) {
            val cells = row.select("td")
            if (cells.size >= 7) {
                try {
                    val number = cells[0].text().trim()         // №
                    val name = cells[1].text().trim()           // Имя
                    val level = cells[2].text().trim().toIntOrNull() ?: 0       // Уровень
                    val deaths = cells[3].text().trim().toIntOrNull() ?: 0      // Смерти
                    val zombiesKilled = cells[4].text().trim().toIntOrNull() ?: 0   // Убито зомби
                    val playersKilled = cells[5].text().trim().toIntOrNull() ?: 0   // Убито игроков
                    val totalScore = cells[6].text().trim().toIntOrNull() ?: 0      // Всего очков

                    val fullName = "$number. $name"

                    players.add(
                        PlayerItem(
                            name = fullName,
                            level = level,
                            deaths = deaths,
                            zombiesKilled = zombiesKilled,
                            playersKilled = playersKilled,
                            totalScore = totalScore
                        )
                    )
                } catch (e: Exception) {
                    Log.w("WebParser", "Ошибка парсинга строки игрока: ${row.text()}", e)
                }
            }
        }

        return players
    }

    @Throws(IOException::class)
    fun parseVisitors(url: String): List<VisitorItem> {
        val document = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .timeout(15000)
            .get()

        val visitors = mutableListOf<VisitorItem>()
        val table = document.select("table").firstOrNull() ?: return emptyList()


        val rows = table.select("tr").drop(1)
        for (row in rows) {
            val cells = row.select("td")
            if (cells.size >= 2) {
                try {
                    val nameElement = cells[0].selectFirst("a")
                    val name = nameElement?.text()?.trim() ?: cells[0].text().trim()
                    val time = cells[1].text().trim()

                    visitors.add(
                        VisitorItem(
                            name = name,
                            time = time
                        )
                    )
                } catch (e: Exception) {
                    Log.w("WebParser", "Ошибка парсинга строки посетителя: ${row.text()}", e)
                }
            }
        }

        return visitors
    }

    @Throws(IOException::class)
    fun parseBanList(url: String): List<BannedPlayer> {
        val document = Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .timeout(15000)
            .get()

        val bannedPlayers = mutableListOf<BannedPlayer>()
        val table = document.select("table").firstOrNull() ?: return emptyList()

        val rows = table.select("tr").drop(1)

        for (row in rows) {
            val cells = row.select("td")
            if (cells.size >= 3) {
                try {
                    val nickname = cells[0].text().trim()
                    val unbanDate = cells[1].text().trim()
                    val reason = cells[2].text().trim()

                    bannedPlayers.add(BannedPlayer(nickname, unbanDate, reason))
                } catch (e: Exception) {
                    Log.w("WebParser", "Ошибка парсинга строки бан-листа: ${row.text()}", e)
                }
            }
        }

        return bannedPlayers
    }
}