package com.antago30.a7dtd_lukomorie.model

data class Player(
    val name: String,
    val level: Int,
    val deaths: Int,
    val zombiesKilled: Int,
    val playersKilled: Int,
    val totalScore: Int
)