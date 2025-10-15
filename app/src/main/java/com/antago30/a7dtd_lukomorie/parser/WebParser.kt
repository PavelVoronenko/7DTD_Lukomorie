package com.antago30.a7dtd_lukomorie.parser

import com.antago30.a7dtd_lukomorie.model.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class WebParser {

    @Throws(Exception::class)
    fun parseInfo(url: String): ServerInfo {
        val data = parseFullDataFromUrl(url)
        return data.serverInfo
    }

    @Throws(Exception::class)
    fun parseOnlinePlayers(url: String): List<PlayerItem> {
        val data = parseFullDataFromUrl(url)
        return data.onlinePlayers
    }

    @Throws(Exception::class)
    fun parseVisitors(url: String): List<VisitorItem> {
        val data = parseFullDataFromUrl(url)
        return data.visitors
    }

    @Throws(Exception::class)
    fun parseLeaderboard(url: String): List<PlayerItem> {
        val data = parseFullDataFromUrl(url)
        return data.leaderboard
    }

    @Throws(Exception::class)
    fun parseBanList(url: String): List<BannedPlayer> {
        val data = parseFullDataFromUrl(url)
        return data.bannedPlayers
    }

    @Throws(Exception::class)
    fun parseNews(url: String): List<NewsItem> {
        val data = parseFullDataFromUrl(url)
        return data.news
    }

    // Загружает и парсит XML
    @Throws(Exception::class)
    private fun parseFullDataFromUrl(url: String): FullServerData {
        val xml = loadXmlWithoutBom(url)
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var serverOnline = "Unknown"
        var serverTime = "00:00"
        var serverDays = 0
        val onlinePlayers = mutableListOf<PlayerItem>()
        val visitors = mutableListOf<VisitorItem>()
        val leaderboard = mutableListOf<PlayerItem>()
        val bannedPlayers = mutableListOf<BannedPlayer>()
        val news = mutableListOf<NewsItem>()

        var currentSection = ""
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "status" -> currentSection = "status"
                        "online_players_list" -> currentSection = "online"
                        "visitors_today_list" -> currentSection = "visitors"
                        "leaderboard_list" -> currentSection = "leaderboard"
                        "blacklist" -> currentSection = "blacklist"
                        "news_list" -> currentSection = "news"

                        "parameter" -> {
                            if (currentSection == "status") {
                                val name = parser.getAttributeValue(null, "name")
                                val value = parser.getAttributeValue(null, "value")
                                when (name) {
                                    "ServerOnline" -> serverOnline = if (value == "True") "в сети." else "оффлайн."
                                    "ServerTime" -> serverTime = value
                                    "ServerDays" -> serverDays = value.toIntOrNull() ?: 0
                                }
                            }
                        }

                        "player" -> {
                            when (currentSection) {
                                "online", "leaderboard" -> {
                                    val name = parser.getAttributeValue(null, "name")
                                    val level = parser.getAttributeValue(null, "level")?.toIntOrNull() ?: 0
                                    val zombies = parser.getAttributeValue(null, "zombies")?.toIntOrNull() ?: 0
                                    val players = parser.getAttributeValue(null, "players")?.toIntOrNull() ?: 0
                                    val deaths = parser.getAttributeValue(null, "deaths")?.toIntOrNull() ?: 0
                                    val score = parser.getAttributeValue(null, "score")?.toIntOrNull() ?: 0

                                    val item = PlayerItem(
                                        name = name,
                                        level = level,
                                        deaths = deaths,
                                        zombiesKilled = zombies,
                                        playersKilled = players,
                                        totalScore = score,
                                        steamProfileUrl = null
                                    )

                                    if (currentSection == "online") {
                                        onlinePlayers.add(item)
                                    } else {
                                        leaderboard.add(item)
                                    }
                                }
                                "visitors" -> {
                                    val name = parser.getAttributeValue(null, "name")
                                    val dateAndTime = (parser.getAttributeValue(null, "date") ?: "").split(" ")
                                    val date = dateAndTime[0]
                                    val time = dateAndTime[1]
                                    visitors.add(VisitorItem(name, date, time))
                                }
                                "blacklist" -> {
                                    val name = parser.getAttributeValue(null, "name")
                                    val unbanDate = parser.getAttributeValue(null, "unbandate") ?: ""
                                    val reason = parser.getAttributeValue(null, "reason") ?: ""
                                    bannedPlayers.add(BannedPlayer(name, unbanDate, reason))
                                }
                            }
                        }

                        "Message" -> {
                            if (currentSection == "news") {
                                val date = parser.getAttributeValue(null, "date") ?: ""
                                val header = parser.getAttributeValue(null, "header") ?: ""
                                val message = parser.getAttributeValue(null, "message") ?: ""
                                news.add(
                                    NewsItem(
                                        title = "\uD83D\uDCF0 $header",
                                        content = message,
                                        timestamp = date
                                    )
                                )
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        // Сортировка новостей по дате (новые — первые)
        val sortedNews = news.sortedWith(compareByDescending { item ->
            try {
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(item.timestamp)
            } catch (e: Exception) {
                Date(0)
            }
        })

        val serverInfo = ServerInfo(
            status = serverOnline,
            time = serverTime,
            day = serverDays,
            playersOnline = onlinePlayers.size,
            bloodMoonTime = "22:00"
        )

        return FullServerData(
            serverInfo = serverInfo,
            onlinePlayers = onlinePlayers,
            visitors = visitors,
            leaderboard = leaderboard,
            bannedPlayers = bannedPlayers,
            news = sortedNews
        )
    }

    // Удаление BOM
    @Throws(Exception::class)
    private fun loadXmlWithoutBom(url: String): String {
        return URL(url).openStream().use { stream ->
            val bytes = stream.readBytes()
            if (bytes.size >= 3 &&
                bytes[0] == 0xEF.toByte() &&
                bytes[1] == 0xBB.toByte() &&
                bytes[2] == 0xBF.toByte()
            ) {
                String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
            } else {
                String(bytes, Charsets.UTF_8)
            }
        }
    }
}