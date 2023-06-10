package it.polito.sportsbooker.entities

import kotlinx.serialization.Serializable

@Serializable
data class Statistic(
    val sport: Sport,
    val gamesPlayed: Int,
    //val gamesWon: Int,
    //val gamesLost: Int,
    //val gamesDrawn: Int? = null
)
