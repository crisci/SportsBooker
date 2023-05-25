package com.example.lab2.database.court_review

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.lab2.database.court.Court
import com.example.lab2.database.player.Player
import com.example.lab2.database.reservation.Reservation
import com.google.gson.Gson

@Entity(
    tableName = "courts_reviews",
    primaryKeys = ["playerId", "courtId"],
    foreignKeys = [
        ForeignKey(Player::class, parentColumns = ["playerId"], childColumns = ["playerId"]),
        ForeignKey(Court::class, parentColumns = ["courtId"], childColumns = ["courtId"])
    ]
    )
data class CourtReview(

    @ColumnInfo(name = "playerId", index = true)
    val playerId: Int,
    @ColumnInfo(name = "courtId", index = true)
    val courtId: Int,

    val review: String,

    val cleanlinessRating: Float,
    val maintenanceRating: Float,
    val lightingRating: Float,
)