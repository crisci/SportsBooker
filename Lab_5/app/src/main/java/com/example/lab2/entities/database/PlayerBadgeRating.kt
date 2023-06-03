package com.example.lab2.entities.database

data class PlayerBadgeRating(
    val playerId: Int,
    val badge: String,
    val rating: Int,
    val sport: String,
)