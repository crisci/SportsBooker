package com.example.lab2.entities

enum class Sport {
    TENNIS,
    PADEL,
    SOCCER,
    BASKETBALL,
    BASEBALL,
    GOLF
}

fun getSportFromString(sport: String): Sport {
    return when(sport.lowercase()) {
        "Tennis" -> Sport.TENNIS
        "Padel" -> Sport.PADEL
        "Soccer" -> Sport.SOCCER
        "Basketball" -> Sport.BASKETBALL
        "Baseball" -> Sport.BASEBALL
        "Golf" -> Sport.GOLF
        else -> Sport.TENNIS
    }
}
