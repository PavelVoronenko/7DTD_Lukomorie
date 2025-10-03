package com.antago30.a7dtd_lukomorie.model

data class FullServerData(
    val serverInfo: ServerInfo,
    val onlinePlayers: List<PlayerItem>,
    val visitors: List<VisitorItem>,
    val leaderboard: List<PlayerItem>,
    val bannedPlayers: List<BannedPlayer>,
    val news: List<NewsItem>
)