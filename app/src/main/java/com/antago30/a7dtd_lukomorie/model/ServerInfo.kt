package com.antago30.a7dtd_lukomorie.model

data class ServerInfo(
    val status: String,
    val time: String,
    val day: Int,
    val playersOnline: Int,
    val bloodMoonTime: String
)