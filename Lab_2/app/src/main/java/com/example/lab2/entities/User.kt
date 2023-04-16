package com.example.lab2.entities

import com.example.lab2.DateAsLongSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Period

@Serializable
data class User(
    val full_name: String = "Achille Mago",
    val nickname: String = "achillemago",
    val address: String = "Turin",
    val description: String = "Lorem",
    val email: String = "achille.mago@polito.it",
    var image: String? = null,
    @Serializable(with = DateAsLongSerializer::class)
    var birthday: LocalDate = LocalDate.of(1995, 1, 1),
    var badges: Map<BadgeType, Int> = mapOf(
        BadgeType.SPEED to 2,
        BadgeType.PRECISION to 3,
        BadgeType.TEAM_WORK to 1
    ),
    var interests : MutableList<Sport> = mutableListOf(Sport.TENNIS, Sport.SOCCER, Sport.GOLF),
    var statistics : MutableMap<Sport, Statistic> = mutableMapOf(
        Sport.TENNIS to Statistic(
            sport = Sport.TENNIS,
            gamesPlayed = 50,
            gamesWon = 30,
            gamesLost = 18,
            gamesDrawn = 2
        ),
        Sport.SOCCER to Statistic(
            sport = Sport.SOCCER,
            gamesPlayed = 40,
            gamesWon = 20,
            gamesLost = 15,
            gamesDrawn = 5
        ),
        Sport.GOLF to Statistic(
            sport = Sport.GOLF,
            gamesPlayed = 10,
            gamesWon = 5,
            gamesLost = 5
        )
    )
) {

    fun getAge(): Int {
        val today = LocalDate.now()
        val period = Period.between(birthday, today)
        return period.years
    }

    fun toJson(): String =
        Json.encodeToString(this)

    companion object {

        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(jsonString: String): User =
            json.decodeFromString<User>(jsonString)


    }
}